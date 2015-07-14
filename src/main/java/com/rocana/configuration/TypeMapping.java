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
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.rocana.configuration.annotations.ConfigurationCollection;
import com.rocana.configuration.annotations.ConfigurationFactory;
import com.rocana.configuration.annotations.ConfigurationFieldName;
import com.rocana.configuration.annotations.ConfigurationProperty;
import java.lang.reflect.InvocationTargetException;
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

    if (scalarTypes.contains(clazz)) {
      supplier.setTypeDescriptor(new ScalarTypeDescriptor(clazz));
      return supplier;
    }

    Map<String, Field> propertyMapping = Maps.newHashMap();

    for (Method method : clazz.getMethods()) {
      if (method.isAnnotationPresent(ConfigurationFactory.class)) {
        List<Field> fields = analyzeConfigurationFactory(method, clazz);
        for (Field fieldDescriptor : fields) {
          propertyMapping.put(fieldDescriptor.getName(), fieldDescriptor);
        }
      } else {
        Optional<Field> fieldDescriptorOptional = analyzeMethod(method);

        if (fieldDescriptorOptional.isPresent()) {
          Field fieldDescriptor = fieldDescriptorOptional.get();
          propertyMapping.put(fieldDescriptor.getName(), fieldDescriptor);
        }
      }
    }

    supplier.setTypeDescriptor(new ObjectTypeDescriptor(clazz, propertyMapping));
    logger.debug("Built typeDescriptor:{}", supplier.get());

    return supplier;
  }

  private List<Field> analyzeConfigurationFactory(Method method, Class<?> clazz) {
    List<Field> fields = Lists.newArrayList();

    try {
      List<Map.Entry<Class<?>, Method>> classMethodPairs = (List<Map.Entry<Class<?>, Method>>) method.invoke(null);
      for (Map.Entry<Class<?>, Method> classMethodPair : classMethodPairs) {
        Optional<Field> fieldDescriptorOptional = analyzeArgumentType(classMethodPair.getKey(), classMethodPair.getValue());

        if (fieldDescriptorOptional.isPresent()) {
          fields.add(fieldDescriptorOptional.get());
        }
      }
    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | ClassCastException ex) {
      logger.error("Unable to invoke method {}.{}(): {}", clazz.getSimpleName(), method.getName(), ex.getMessage());
      logger.debug("Stack trace follows.", ex);
    }

    return fields;
  }

  private Optional<Field> analyzeArgumentType(Class<?> argumentType, Method method) {
    ConfigurationProperty configurationPropertyAnnotation = argumentType.getAnnotation(ConfigurationProperty.class);
    ConfigurationCollection configurationCollectionAnnotation = argumentType.getAnnotation(ConfigurationCollection.class);
    ConfigurationFieldName configurationFieldName = argumentType.getAnnotation(ConfigurationFieldName.class);

    Class<?>[] parameterTypes = method.getParameterTypes();
    Preconditions.checkArgument(
      parameterTypes.length == 1,
      "Annotated method %s takes more than one argument (parameters:%s)",
      method.getName(),
      parameterTypes
    );

    Preconditions.checkArgument(
      parameterTypes[0].isAssignableFrom(argumentType),
      "Annotated method %s takes unsupported argument type %s expected %s",
      method.getName(),
      parameterTypes[0],
      argumentType
    );

    return buildField(method, argumentType, configurationPropertyAnnotation, configurationCollectionAnnotation, configurationFieldName);
  }

  private Optional<Field> analyzeMethod(Method method) {
    ConfigurationProperty configurationPropertyAnnotation = method.getAnnotation(ConfigurationProperty.class);
    ConfigurationCollection configurationCollectionAnnotation = method.getAnnotation(ConfigurationCollection.class);
    ConfigurationFieldName configurationFieldName = method.getAnnotation(ConfigurationFieldName.class);

    Class<?> argumentType = null;
    Class<?>[] parameterTypes = method.getParameterTypes();
    if (configurationPropertyAnnotation != null || configurationFieldName != null) {
      Preconditions.checkArgument(
        parameterTypes.length == 1,
        "Annotated method %s takes more than one argument (parameters:%s)",
        method.getName(),
        parameterTypes
      );

      argumentType = parameterTypes[0];
    }

    return buildField(method, argumentType, configurationPropertyAnnotation, configurationCollectionAnnotation, configurationFieldName);
  }

  private Optional<Field> buildField(Method method, Class<?> argumentType, ConfigurationProperty configurationPropertyAnnotation,
    ConfigurationCollection configurationCollectionAnnotation, ConfigurationFieldName configurationFieldName) {
    Field fieldDescriptor = null;

    if (configurationFieldName != null) {
      logger.debug("Found annotated method:{} annotation:{}", method, configurationFieldName);

      Preconditions.checkArgument(
        argumentType.equals(String.class),
        "Field names can only be mapped to string types. Found type:%s on method:%s",
        argumentType,
        method
      );

      fieldDescriptor = new Field("{key}", method, new TypeDescriptorSupplier(new ScalarTypeDescriptor(String.class)));
    } else if (configurationPropertyAnnotation != null) {
      logger.debug("Found annotated method:{} annotation:{}", method, configurationPropertyAnnotation);

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
