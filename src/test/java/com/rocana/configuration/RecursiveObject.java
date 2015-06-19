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

import com.rocana.configuration.annotations.ConfigurationCollection;
import com.rocana.configuration.annotations.ConfigurationProperty;
import java.util.List;
import java.util.Map;


public class RecursiveObject {

  private String value;
  private RecursiveObject child;
  private List<RecursiveObject> children;
  private Map<String, RecursiveObject> map;

  public String getValue() {
    return value;
  }

  @ConfigurationProperty(name = "value")
  public void setValue(String value) {
    this.value = value;
  }

  public RecursiveObject getChild() {
    return child;
  }

  @ConfigurationProperty(name = "child")
  public void setChild(RecursiveObject child) {
    this.child = child;
  }

  public List<RecursiveObject> getChildren() {
    return children;
  }

  @ConfigurationCollection(elementType = RecursiveObject.class)
  @ConfigurationProperty(name = "children")
  public void setChildren(List<RecursiveObject> children) {
    this.children = children;
  }

  public Map<String, RecursiveObject> getMap() {
    return map;
  }

  @ConfigurationCollection(elementType = RecursiveObject.class)
  @ConfigurationProperty(name = "map")
  public void setMap(Map<String, RecursiveObject> map) {
    this.map = map;
  }
  
}
