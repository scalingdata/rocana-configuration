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
import com.rocana.configuration.annotations.ConfigurationCollection;
import com.rocana.configuration.annotations.ConfigurationProperty;

import java.util.Map;

class MapScalar {

  private Map<String, Integer> nameCounts;

  public Map<String, Integer> getNameCounts() {
    return nameCounts;
  }

  @ConfigurationCollection(elementType = Integer.class)
  @ConfigurationProperty(name = "name-counts")
  public void setNameCounts(Map<String, Integer> nameCounts) {
    this.nameCounts = nameCounts;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
      .add("nameCounts", nameCounts)
      .toString();
  }

}
