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

import com.google.common.io.Resources;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class TestConfigurationParser {

  @Test
  public void testObject() throws IOException {
    ConfigurationParser parser = new ConfigurationParser();

    try (InputStream inputStream = Resources.getResource("conf/object.conf").openStream()) {
      FlatObject object = parser.parse(
        new InputStreamReader(
          inputStream
        ),
        FlatObject.class
      );

      Assert.assertNotNull(object);
      Assert.assertEquals("1 minute", object.getDurationValue());
      Assert.assertEquals("1 GB", object.getSizeValue());
      Assert.assertEquals("Hello world", object.getStringValue());
      Assert.assertEquals(1.0d, object.getFloatValue().doubleValue(), 0.0d);
      Assert.assertEquals(1, object.getIntegerValue().intValue());
      Assert.assertEquals(1L, object.getLongValue().longValue());
    }
  }

  @Test
  public void testListScalar() throws IOException {
    ConfigurationParser parser = new ConfigurationParser();

    try (InputStream inputStream = Resources.getResource("conf/list-scalar.conf").openStream()) {
      ListScalar object = parser.parse(
        new InputStreamReader(
          inputStream
        ),
        ListScalar.class
      );

      Assert.assertNotNull(object);
    }
  }

  @Test
  public void testListObject() throws IOException {
    ConfigurationParser parser = new ConfigurationParser();

    try (InputStream inputStream = Resources.getResource("conf/list-object.conf").openStream()) {
      ListObject object = parser.parse(
        new InputStreamReader(
          inputStream
        ),
        ListObject.class
      );

      Assert.assertNotNull(object);
    }
  }

  @Test
  public void testMapScalar() throws IOException {
    ConfigurationParser parser = new ConfigurationParser();

    try (InputStream inputStream = Resources.getResource("conf/map-scalar.conf").openStream()) {
      MapScalar object = parser.parse(
        new InputStreamReader(
          inputStream
        ),
        MapScalar.class
      );

      Assert.assertNotNull(object);
    }
  }

  @Test
  public void testMapObject() throws IOException {
    ConfigurationParser parser = new ConfigurationParser();

    try (InputStream inputStream = Resources.getResource("conf/map-object.conf").openStream()) {
      MapObject object = parser.parse(
        new InputStreamReader(
          inputStream
        ),
        MapObject.class
      );

      Assert.assertNotNull(object);
    }
  }

}
