/*
 * Copyright (c) 2015 Rocana
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rocana.configuration;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.rocana.configuration.annotations.ConfigurationCollection;
import com.rocana.configuration.annotations.ConfigurationFieldName;
import com.rocana.configuration.annotations.ConfigurationProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TypeMapping {

  private static final Logger logger = LoggerFactory.getLogger(TypeMapping.class);
  private static final Set<Class<?>> scalarTypes = Sets.<Class<?>>newHashSet(
    Boolean.class,
    String.class,
    Integer.class,
    Float.class,
    Long.class
  );

  private final Map<Class<?>, Supplier<TypeDescriptor>> descriptorRegistry;

  public TypeMapping() {
    this.descriptorRegistry = Maps.newHashMap();
  }

  public static TypeDescriptor ofType(Class<?> targetType) {
    TypeMapping mapping = new TypeMapping();

    TypeDescriptor descriptor = mapping.analyzeClass(targetType).get();

    logger.debug("Type mapping for targetType:{} - {}", targetType, descriptor);

    return descriptor;
  }

  private Supplier<TypeDescriptor> analyzeClass(Class<?> clazz) {
    if (descriptorRegistry.containsKey(clazz)) {
      logger.debug("Skipping class:{} - already scanned", clazz);
      return descriptorRegistry.get(clazz);
    }

    logger.debug("Scanning class:{}", clazz);

    TypeDescriptorSupplier supplier = new TypeDescriptorSupplier();
    descriptorRegistry.put(clazz, supplier);

    Map<String, Field> propertyMapping = Maps.newHashMap();

    for (Method method : clazz.getMethods()) {
      Optional<Field> fieldDescriptorOptional = analyzeMethod(method);

      if (fieldDescriptorOptional.isPresent()) {
        Field fieldDescriptor = fieldDescriptorOptional.get();
        propertyMapping.put(fieldDescriptor.getName(), fieldDescriptor);
      }
    }

    if (propertyMapping.isEmpty()) {
      logger.debug("No annotated methods found:{}", clazz);
      supplier.setTypeDescriptor(new ScalarTypeDescriptor(clazz));
    } else {
      supplier.setTypeDescriptor(new ObjectTypeDescriptor(clazz, propertyMapping));
      logger.debug("Built typeDescriptor:{}", supplier.get());
    }

    return supplier;
  }

  private Optional<Field> analyzeMethod(Method method) {
    Field fieldDescriptor = null;

    ConfigurationProperty configurationPropertyAnnotation = method.getAnnotation(ConfigurationProperty.class);
    ConfigurationCollection configurationCollectionAnnotation = method.getAnnotation(ConfigurationCollection.class);
    ConfigurationFieldName configurationFieldName = method.getAnnotation(ConfigurationFieldName.class);

    if (configurationFieldName != null) {
      logger.debug("Found annotated method:{} annotation:{}", method, configurationFieldName);

      Class<?>[] parameterTypes = method.getParameterTypes();
      Preconditions.checkArgument(
        parameterTypes.length == 1,
        "Annotated method %s takes more than one argument (parameters:%s)",
        method.getName(),
        parameterTypes
      );

      Class<?> argumentType = parameterTypes[0];

      Preconditions.checkArgument(
        argumentType.equals(String.class),
        "Field names can only be mapped to string types. Found type:%s on method:%s",
        argumentType,
        method
      );

      fieldDescriptor = new Field("{key}", method, new TypeDescriptorSupplier(new ScalarTypeDescriptor(String.class)));
    } else if (configurationPropertyAnnotation != null) {
      logger.debug("Found annotated method:{} annotation:{}", method, configurationPropertyAnnotation);

      Class<?>[] parameterTypes = method.getParameterTypes();
      Preconditions.checkArgument(
        parameterTypes.length == 1,
        "Annotated method %s takes more than one argument (parameters:%s)",
        method.getName(),
        parameterTypes
      );

      Class<?> argumentType = parameterTypes[0];
      String name = configurationPropertyAnnotation.name();

      if (configurationCollectionAnnotation != null) {
        Class<?> elementType = configurationCollectionAnnotation.elementType();
        if (List.class.isAssignableFrom(argumentType)) {
          fieldDescriptor = new Field(name, method, new TypeDescriptorSupplier(new ListTypeDescriptor(argumentType, analyzeClass(elementType))));
        } else if (Map.class.isAssignableFrom(argumentType)) {
          fieldDescriptor = new Field(name, method, new TypeDescriptorSupplier(new MapTypeDescriptor(argumentType, analyzeClass(elementType))));
        } else {
          throw new UnsupportedOperationException("Unable to analyze type " + argumentType + " on method " + method);
        }
      } else if (scalarTypes.contains(argumentType)) {
        fieldDescriptor = new Field(name, method, new TypeDescriptorSupplier(new ScalarTypeDescriptor(argumentType)));
      } else {
        fieldDescriptor = new Field(name, method, analyzeClass(argumentType));
      }
    }

    return Optional.fromNullable(fieldDescriptor);
  }

}
