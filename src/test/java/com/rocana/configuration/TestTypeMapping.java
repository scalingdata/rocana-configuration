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

import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;

public class TestTypeMapping {

  @Test
  public void testObject() {
    TypeDescriptor typeDescriptor = TypeMapping.ofType(FlatObject.class);

    Assert.assertNotNull(typeDescriptor);

    Assert.assertEquals(true, typeDescriptor.hasChildren());
    Assert.assertEquals(FlatObject.class, typeDescriptor.getTargetType());

    List<TypeDescriptor> children = typeDescriptor.getChildren();

    Assert.assertEquals(8, children.size());
  }

  @Test
  public void testObjectListScalar() {
    TypeDescriptor typeDescriptor = TypeMapping.ofType(ListScalar.class);

    Assert.assertNotNull(typeDescriptor);

    Assert.assertEquals(true, typeDescriptor.hasChildren());
    Assert.assertEquals(ListScalar.class, typeDescriptor.getTargetType());

    TypeDescriptor namesFieldDescriptor = typeDescriptor.getChildren().get(0);

    Assert.assertEquals(true, namesFieldDescriptor.hasChildren());
    Assert.assertEquals(List.class, namesFieldDescriptor.getTargetType());
    Assert.assertEquals(1, namesFieldDescriptor.getChildren().size());

    TypeDescriptor scalarDescriptor = namesFieldDescriptor.getChildren().get(0);

    Assert.assertEquals(false, scalarDescriptor.hasChildren());
    Assert.assertEquals(String.class, scalarDescriptor.getTargetType());
  }

  @Test
  public void testObjectMapScalar() {
    TypeDescriptor typeDescriptor = TypeMapping.ofType(MapScalar.class);

    Assert.assertNotNull(typeDescriptor);

    Assert.assertEquals(true, typeDescriptor.hasChildren());
    Assert.assertEquals(MapScalar.class, typeDescriptor.getTargetType());
    Assert.assertEquals(1, typeDescriptor.getChildren().size());

    TypeDescriptor mapDescriptor = typeDescriptor.getChildren().get(0);

    Assert.assertEquals(true, mapDescriptor.hasChildren());
    Assert.assertEquals(Map.class, mapDescriptor.getTargetType());
    Assert.assertEquals(1, mapDescriptor.getChildren().size());

    TypeDescriptor integerDescriptor = mapDescriptor.getChildren().get(0);

    Assert.assertEquals(false, integerDescriptor.hasChildren());
    Assert.assertEquals(Integer.class, integerDescriptor.getTargetType());
  }

  @Test
  public void testObjectListObject() {
    TypeDescriptor typeDescriptor = TypeMapping.ofType(ListObject.class);

    Assert.assertNotNull(typeDescriptor);

    TypeDescriptor listDescriptor = typeDescriptor.getChildren().get(0);

    Assert.assertEquals(true, listDescriptor.hasChildren());
    Assert.assertEquals(List.class, listDescriptor.getTargetType());

    TypeDescriptor flatObjectDescriptor = listDescriptor.getChildren().get(0);

    Assert.assertEquals(true, flatObjectDescriptor.hasChildren());
    Assert.assertEquals(FlatObject.class, flatObjectDescriptor.getTargetType());
    Assert.assertEquals(8, flatObjectDescriptor.getChildren().size());
  }

  @Test
  public void testObjectMapObject() {
    TypeDescriptor typeDescriptor = TypeMapping.ofType(MapObject.class);

    Assert.assertNotNull(typeDescriptor);

    TypeDescriptor mapDescriptor = typeDescriptor.getChildren().get(0);

    Assert.assertEquals(true, mapDescriptor.hasChildren());
    Assert.assertEquals(Map.class, mapDescriptor.getTargetType());

    TypeDescriptor flatObjectDescriptor = mapDescriptor.getChildren().get(0);

    Assert.assertEquals(true, flatObjectDescriptor.hasChildren());
    Assert.assertEquals(FlatObject.class, flatObjectDescriptor.getTargetType());
    Assert.assertEquals(8, flatObjectDescriptor.getChildren().size());
  }

  @Test
  public void testObjectRecursiveObject() {
    TypeDescriptor typeDescriptor = TypeMapping.ofType(RecursiveObject.class);

    Assert.assertNotNull(typeDescriptor);
    Assert.assertEquals(ObjectTypeDescriptor.class, typeDescriptor.getClass());
    ObjectTypeDescriptor recursiveObjectDescriptor = (ObjectTypeDescriptor) typeDescriptor;

    TypeDescriptor childTypeDescriptor = recursiveObjectDescriptor.getChildMap().get("value");
    Assert.assertEquals(Field.class, childTypeDescriptor.getClass());

    Field valueDescriptor = (Field) childTypeDescriptor;
    Assert.assertEquals(String.class, valueDescriptor.getTargetType());
    Assert.assertEquals(ScalarTypeDescriptor.class, valueDescriptor.getTypeDescriptor().getClass());
    Assert.assertFalse(valueDescriptor.getTypeDescriptor().hasChildren());

    childTypeDescriptor = recursiveObjectDescriptor.getChildMap().get("child");
    Assert.assertEquals(Field.class, childTypeDescriptor.getClass());

    Field childDescriptor = (Field) childTypeDescriptor;
    Assert.assertEquals(ObjectTypeDescriptor.class, childDescriptor.getTypeDescriptor().getClass());
    Assert.assertEquals(RecursiveObject.class, childDescriptor.getTargetType());
    Assert.assertEquals(recursiveObjectDescriptor, childDescriptor.getTypeDescriptor());

    childTypeDescriptor = recursiveObjectDescriptor.getChildMap().get("children");
    Assert.assertEquals(Field.class, childTypeDescriptor.getClass());

    Field childrenDescriptor = (Field) childTypeDescriptor;
    Assert.assertEquals(ListTypeDescriptor.class, childrenDescriptor.getTypeDescriptor().getClass());
    Assert.assertEquals(List.class, childrenDescriptor.getTargetType());
    Assert.assertEquals(recursiveObjectDescriptor, childrenDescriptor.getTypeDescriptor().getChildren().get(0));

    childTypeDescriptor = recursiveObjectDescriptor.getChildMap().get("map");
    Assert.assertEquals(Field.class, childTypeDescriptor.getClass());

    Field mapDescriptor = (Field) childTypeDescriptor;
    Assert.assertEquals(MapTypeDescriptor.class, mapDescriptor.getTypeDescriptor().getClass());
    Assert.assertEquals(Map.class, mapDescriptor.getTargetType());
    Assert.assertEquals(typeDescriptor, mapDescriptor.getChildren().get(0));
  }
}