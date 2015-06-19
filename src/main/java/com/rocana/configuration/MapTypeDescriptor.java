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
import com.google.common.base.Supplier;

import java.util.Collections;
import java.util.List;

class MapTypeDescriptor implements TypeDescriptor {

  private Class<?> targetType;
  private Supplier<TypeDescriptor> valueTypeDescriptor;

  public MapTypeDescriptor(Class<?> targetType, Supplier<TypeDescriptor> valueTypeDescriptor) {
    this.targetType = targetType;
    this.valueTypeDescriptor = valueTypeDescriptor;
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
    return Collections.singletonList(valueTypeDescriptor.get());
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
      .add("targetType", targetType)
      .add("valueTypeDescriptor", valueTypeDescriptor)
      .toString();
  }

}
