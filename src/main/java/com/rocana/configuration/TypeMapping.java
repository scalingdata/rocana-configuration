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

  private Map<Class<?>, Supplier<TypeDescriptor>> descriptorRegistry;

  private Set<Class<?>> seenClasses;

  public TypeMapping() {
    this.seenClasses = Sets.newHashSet();
  }

  public static TypeDescriptor ofType(Class<?> targetType) {
    TypeMapping mapping = new TypeMapping();

    TypeDescriptor descriptor = mapping.analyzeClass(targetType);

    logger.debug("Type mapping for targetType:{} - {}", targetType, descriptor);

    return descriptor;
  }

  private TypeDescriptor analyzeClass(Class<?> clazz) {
    if (seenClasses.contains(clazz)) {
      logger.debug("Skipping class:{} - already scanned", clazz);
      return null;
    }

    logger.debug("Scanning class:{}", clazz);

    seenClasses.add(clazz);

    Map<String, Field> propertyMapping = Maps.newHashMap();

    for (Method method : clazz.getMethods()) {
      Optional<Field> fieldDescriptorOptional = analyzeMethod(method);

      if (fieldDescriptorOptional.isPresent()) {
        Field fieldDescriptor = fieldDescriptorOptional.get();
        propertyMapping.put(fieldDescriptor.getName(), fieldDescriptor);
      }
    }

    if (propertyMapping.isEmpty()) {
      logger.debug("No annotated methods found.");
      return new ScalarTypeDescriptor(clazz);
    } else {
      ObjectTypeDescriptor typeDescriptor = new ObjectTypeDescriptor(clazz, propertyMapping);
      logger.debug("Built typeDescriptor:{}", typeDescriptor);
      return typeDescriptor;
    }
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

      fieldDescriptor = new Field("{key}", method, new ScalarTypeDescriptor(String.class));
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

      if (configurationCollectionAnnotation != null) {
        if (List.class.isAssignableFrom(argumentType)) {
          fieldDescriptor = new Field(configurationPropertyAnnotation.name(), method, new ListTypeDescriptor(argumentType, analyzeClass(configurationCollectionAnnotation.elementType())));
        } else if (Map.class.isAssignableFrom(argumentType)) {
          fieldDescriptor = new Field(configurationPropertyAnnotation.name(), method, new MapTypeDescriptor(argumentType, analyzeClass(configurationCollectionAnnotation.elementType())));
        } else {
          throw new UnsupportedOperationException("Unable to analyze type " + argumentType + " on method " + method);
        }
      } else if (scalarTypes.contains(argumentType)) {
        fieldDescriptor = new Field(configurationPropertyAnnotation.name(), method, new ScalarTypeDescriptor(argumentType));
      } else {
        analyzeClass(argumentType);
      }
    }

    return Optional.fromNullable(fieldDescriptor);
  }

}
