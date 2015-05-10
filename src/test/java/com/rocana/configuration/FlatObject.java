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
import com.rocana.configuration.annotations.ConfigurationFieldName;
import com.rocana.configuration.annotations.ConfigurationProperty;

class FlatObject {

  private String fieldName;

  private Integer integerValue;
  private Boolean booleanValue;
  private String stringValue;
  private Long longValue;
  private Float floatValue;
  private String sizeValue;
  private String durationValue;

  public String getFieldName() {
    return fieldName;
  }

  @ConfigurationFieldName
  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }

  public Integer getIntegerValue() {
    return integerValue;
  }

  @ConfigurationProperty(name = "integer-value")
  public void setIntegerValue(Integer integerValue) {
    this.integerValue = integerValue;
  }

  public Boolean isBoolValue() {
    return booleanValue;
  }

  @ConfigurationProperty(name = "boolean-value")
  public void setBooleanValue(Boolean booleanValue) {
    this.booleanValue = booleanValue;
  }

  public String getStringValue() {
    return stringValue;
  }

  @ConfigurationProperty(name = "string-value")
  public void setStringValue(String stringValue) {
    this.stringValue = stringValue;
  }

  public Long getLongValue() {
    return longValue;
  }

  @ConfigurationProperty(name = "long-value")
  public void setLongValue(Long longValue) {
    this.longValue = longValue;
  }

  public Float getFloatValue() {
    return floatValue;
  }

  @ConfigurationProperty(name = "float-value")
  public void setFloatValue(Float floatValue) {
    this.floatValue = floatValue;
  }

  public String getSizeValue() {
    return sizeValue;
  }

  @ConfigurationProperty(name = "size-value")
  public void setSizeValue(String sizeValue) {
    this.sizeValue = sizeValue;
  }

  public String getDurationValue() {
    return durationValue;
  }

  @ConfigurationProperty(name = "duration-value")
  public void setDurationValue(String durationValue) {
    this.durationValue = durationValue;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
      .add("fieldName", fieldName)
      .add("integerValue", integerValue)
      .add("booleanValue", booleanValue)
      .add("stringValue", stringValue)
      .add("longValue", longValue)
      .add("floatValue", floatValue)
      .add("sizeValue", sizeValue)
      .add("durationValue", durationValue)
      .toString();
  }

}
