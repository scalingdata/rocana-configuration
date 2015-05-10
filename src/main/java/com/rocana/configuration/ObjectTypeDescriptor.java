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
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;

class ObjectTypeDescriptor implements TypeDescriptor {

  private Class<?> targetType;
  private Map<String, Field> children;

  public ObjectTypeDescriptor(Class<?> targetType, Map<String, Field> children) {
    this.targetType = targetType;
    this.children = children;
  }

  @Override
  public Class<?> getTargetType() {
    return targetType;
  }

  @Override
  public boolean hasChildren() {
    return true;
  }

  @Override
  public List<TypeDescriptor> getChildren() {
    return Lists.<TypeDescriptor>newArrayList(children.values());
  }

  public Map<String, Field> getChildMap() {
    return children;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
      .add("targetType", targetType)
      .add("children", children)
      .toString();
  }

}
