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

import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.rocana.configuration.annotations.ConfigurationFactory;
import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

public class RuntimeObject {

  private List<Object> children;

  public RuntimeObject() {
    this.children = Lists.newArrayList();
  }

  public void addProperty(Object child) {
    children.add(child);
  }

  public List<Object> getChildren() {
    return children;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
      .add("children", children)
      .toString();
  }

  @ConfigurationFactory
  public static List<Map.Entry<Class<?>, Method>> getRuntimeConfigurationProperties() {
    List<Map.Entry<Class<?>, Method>> classMethodPairs = Lists.newArrayList();
    try {
      Class<?> argumentType = RuntimeProperty.class;
      Method method = RuntimeObject.class.getDeclaredMethod("addProperty", Object.class);

      classMethodPairs.add(new AbstractMap.SimpleEntry<Class<?>, Method>(argumentType, method));
    } catch (NoSuchMethodException | SecurityException ex) {
      throw Throwables.propagate(ex);
    }

    return classMethodPairs;
  }
}
