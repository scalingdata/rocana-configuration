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

import java.lang.reflect.Method;
import java.util.List;

class Field implements TypeDescriptor {

  private String name;
  private Method method;
  private TypeDescriptor typeDescriptor;

  public Field(String name, Method method, TypeDescriptor typeDescriptor) {
    this.name = name;
    this.method = method;
    this.typeDescriptor = typeDescriptor;
  }

  public String getName() {
    return name;
  }

  public Method getMethod() {
    return method;
  }

  public TypeDescriptor getTypeDescriptor() {
    return typeDescriptor;
  }

  @Override
  public Class<?> getTargetType() {
    return typeDescriptor.getTargetType();
  }

  @Override
  public boolean hasChildren() {
    return true;
  }

  @Override
  public List<TypeDescriptor> getChildren() {
    return typeDescriptor.getChildren();
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
      .add("name", name)
      .add("method", method)
      .add("typeDescriptor", typeDescriptor)
      .toString();
  }

}
