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
import java.util.List;
import java.util.Map;

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

      List<String> names = object.getNames();

      Assert.assertNotNull(names);
      Assert.assertEquals(4, names.size());
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

      List<FlatObject> flatObjects = object.getFlatObjects();

      Assert.assertNotNull(flatObjects);
      Assert.assertEquals(2, flatObjects.size());
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

      Map<String, FlatObject> flatObjectMap = object.getFlatObjectMap();

      Assert.assertNotNull(flatObjectMap);
      Assert.assertEquals(2, flatObjectMap.size());
      Assert.assertEquals(true, flatObjectMap.containsKey("one"));
      Assert.assertEquals(true, flatObjectMap.containsKey("two"));

      FlatObject one = flatObjectMap.get("one");
      FlatObject two = flatObjectMap.get("two");

      Assert.assertNotNull(one);
      Assert.assertEquals(1, one.getIntegerValue().intValue());
      Assert.assertEquals("Hello world", one.getStringValue());

      Assert.assertNotNull(two);
      Assert.assertEquals(2, two.getIntegerValue().intValue());
      Assert.assertEquals("Goodbye world", two.getStringValue());
    }
  }

  @Test
  public void testRecursiveObject() throws IOException {
    ConfigurationParser parser = new ConfigurationParser();

    try (InputStream inputStream = Resources.getResource("conf/recursive-object.conf").openStream()) {
      RecursiveObject object = parser.parse(
          new InputStreamReader(
              inputStream
          ),
          RecursiveObject.class
      );

      Assert.assertNotNull(object);

      Assert.assertEquals("value", object.getValue());

      Assert.assertEquals("child", object.getChild().getValue());
      Assert.assertEquals("grandchild", object.getChild().getChild().getValue());

      Assert.assertEquals("0", object.getChildren().get(0).getValue());
      Assert.assertEquals("1", object.getChildren().get(1).getValue());
      Assert.assertEquals("2", object.getChildren().get(2).getValue());
      Assert.assertEquals("3", object.getChildren().get(3).getValue());
      Assert.assertEquals("4", object.getChildren().get(4).getValue());

      Assert.assertEquals("0", object.getMap().get("zero").getValue());
      Assert.assertEquals("1", object.getMap().get("one").getValue());
      Assert.assertEquals("2", object.getMap().get("two").getValue());
      Assert.assertEquals("3", object.getMap().get("three").getValue());
      Assert.assertEquals("4", object.getMap().get("four").getValue());
    }
  }
}
