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

import com.google.common.io.CharSource;
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
      Assert.assertNotNull(object.getObjectValue());
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
  public void testEscapedStrings() throws IOException {
    ConfigurationParser parser = new ConfigurationParser();

    try (InputStream inputStream = Resources.getResource("conf/escaped-strings.conf").openStream()) {
      ListScalar object = parser.parse(
        new InputStreamReader(
          inputStream
        ),
        ListScalar.class
      );

      Assert.assertNotNull(object);

      List<String> names = object.getNames();

      Assert.assertNotNull(names);
      Assert.assertEquals(5, names.size());
      Assert.assertEquals("one\"one", names.get(0));
      Assert.assertEquals("two\"two\\", names.get(1));
      Assert.assertEquals("three\"three", names.get(2));
      Assert.assertEquals("four\nfour", names.get(3));
      Assert.assertEquals("five\tfive", names.get(4));
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

  @Test
  public void testRuntimeObject() throws IOException {
    ConfigurationParser parser = new ConfigurationParser();

    try (InputStream inputStream = Resources.getResource("conf/runtime-object.conf").openStream()) {
      RuntimeObject object = parser.parse(
        new InputStreamReader(
          inputStream
        ),
        RuntimeObject.class
      );

      Assert.assertNotNull(object);
      Assert.assertNotNull(object.getChildren());
      Assert.assertEquals(3, object.getChildren().size());

      RuntimeProperty prop;

      prop = (RuntimeProperty) object.getChildren().get(0);
      Assert.assertEquals("a", prop.getValue());

      prop = (RuntimeProperty) object.getChildren().get(1);
      Assert.assertEquals("b", prop.getValue());

      prop = (RuntimeProperty) object.getChildren().get(2);
      Assert.assertEquals("c", prop.getValue());
    }
  }

  @Test(expected = ConfigurationException.class)
  public void testUnknownOption() throws IOException {
    ConfigurationParser parser = new ConfigurationParser();

    try (InputStream inputStream = Resources.getResource("conf/unknown-option.conf").openStream()) {
      parser.parse(
        new InputStreamReader(
          inputStream
        ),
        FlatObject.class
      );
    }
  }

  @Test(expected = ConfigurationException.class)
  public void testUnexpectedType() throws IOException {
    ConfigurationParser parser = new ConfigurationParser();

    try (InputStream inputStream = Resources.getResource("conf/unexpected-type.conf").openStream()) {
      parser.parse(
        new InputStreamReader(
          inputStream
        ),
        FlatObject.class
      );
    }
  }

  /*
   * Invalid syntax 1: A dot instead of a comma as a separator between fields.
   */
  @Test(expected = ConfigurationException.class)
  public void testInvalidSyntax1() throws Exception {
    ConfigurationParser parser = new ConfigurationParser();

    parser.parse(CharSource.wrap("{ a: \"1\". b: \"2\" }"), FlatObject.class);
  }

  /*
   * Invalid syntax 2: Missing a closing curly brace.
   */
  @Test(expected =  ConfigurationException.class)
  public void testInvalidSyntax2() throws Exception {
    ConfigurationParser parser = new ConfigurationParser();

    parser.parse(CharSource.wrap("{ a: 1"), FlatObject.class);
  }

  /*
   * Invalid syntax 3: Ambiguous duration value vs. field ID. A human could
   * think this is "field a of type int, field minutes of type int" with no
   * comma rather than "field a of type duration, missing ID in field 2." which
   * is what it really is.
   */
  @Test(expected = ConfigurationException.class)
  public void testInvalidSyntax3() throws Exception {
    ConfigurationParser parser = new ConfigurationParser();

    parser.parse(CharSource.wrap("{ a: 1 minutes: 2"), FlatObject.class);
  }

  /*
   * Invalid syntax 4: Field names can not begin with a number. This parses as
   * "INT : INT" (illegal) rather than "ID : INT".
   */
  @Test(expected = ConfigurationException.class)
  public void testInvalidSyntax4() throws Exception {
    ConfigurationParser parser = new ConfigurationParser();

    parser.parse(CharSource.wrap("{ 7: 1 }"), FlatObject.class);
  }

}
