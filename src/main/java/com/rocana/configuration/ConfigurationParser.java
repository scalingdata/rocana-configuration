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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.google.common.io.CharSource;
import com.rocana.configuration.antlr.ConfigurationBaseVisitor;
import com.rocana.configuration.antlr.ConfigurationLexer;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringEscapeUtils;

public class ConfigurationParser {

  private static final Logger logger = LoggerFactory.getLogger(ConfigurationParser.class);

  public <T> T parse(CharSource source, Class<T> targetType) {
    try (Reader reader = source.openBufferedStream()) {
      return parse(reader, targetType);
    } catch (IOException e) {
      throw new ConfigurationException("Unable to read configuration data from source", e);
    }
  }

  public <T> T parse(Reader reader, Class<T> targetType) throws IOException {
    logger.debug("Parsing configuration for type:{}", targetType);

    ANTLRInputStream inputStream = new ANTLRInputStream(reader);
    ConfigurationLexer lexer = new ConfigurationLexer(inputStream);
    CommonTokenStream tokenStream = new CommonTokenStream(lexer);

    com.rocana.configuration.antlr.ConfigurationParser parser = new com.rocana.configuration.antlr.ConfigurationParser(tokenStream);
    TypeDescriptor typeDescriptor = TypeMapping.ofType(targetType);

    ParseTree parseTree = parser.config();

    logger.debug("Parsed configuration:{}", parseTree.toStringTree(parser));

    Visitor<T> visitor = new Visitor<>(typeDescriptor);

    visitor.visit(parseTree);

    logger.debug("Configured object:{}", visitor.getResult());

    return visitor.getResult();
  }

  private static class Visitor<T> extends ConfigurationBaseVisitor<List<Object>> {

    private static final Logger logger = LoggerFactory.getLogger(Visitor.class);
    private static final Set<String> booleanTrueValues = Sets.newHashSet("true", "on", "enabled", "yes");
    private static final Pattern patternLong = Pattern.compile("^(\\d+)(?:\\s*[lL])?$");
    private static final Pattern patternFloat = Pattern.compile("^(\\d+(?:\\.\\d+)?)(?:\\s*[fF])?$");

    private Deque<TypeDescriptor> typeStack;
    private Deque<Object> valueStack;
    private T configuration;

    public Visitor(TypeDescriptor typeDescriptor) {
      this.typeStack = Queues.newArrayDeque();
      this.valueStack = Queues.newArrayDeque();

      this.typeStack.push(typeDescriptor);
    }

    @Override
    public List<Object> visitValueString(com.rocana.configuration.antlr.ConfigurationParser.ValueStringContext ctx) {
      String text = ctx.QUOTED_STRING().getText();

      String value = StringEscapeUtils.unescapeJava(text.substring(1, text.length() - 1));

      return Lists.<Object>newArrayList(value);
    }

    @Override
    public List<Object> visitValueInteger(com.rocana.configuration.antlr.ConfigurationParser.ValueIntegerContext ctx) {
      return Lists.<Object>newArrayList(Integer.parseInt(ctx.INT().getText()));
    }

    @Override
    public List<Object> visitValueBoolean(com.rocana.configuration.antlr.ConfigurationParser.ValueBooleanContext ctx) {
      return Lists.<Object>newArrayList(booleanTrueValues.contains(ctx.BOOLEAN().getText()));
    }

    @Override
    public List<Object> visitValueLong(com.rocana.configuration.antlr.ConfigurationParser.ValueLongContext ctx) {
      Matcher matcher = patternLong.matcher(ctx.LONG().getText());

      if (!matcher.matches()) {
        throw new ConfigurationException("Long value " + ctx.LONG().getText() + " can not be parsed");
      }

      return Lists.<Object>newArrayList(Long.parseLong(matcher.group(1)));
    }

    @Override
    public List<Object> visitValueFloat(com.rocana.configuration.antlr.ConfigurationParser.ValueFloatContext ctx) {
      Matcher matcher = patternFloat.matcher(ctx.FLOAT().getText());

      if (!matcher.matches()) {
        throw new ConfigurationException("Float value " + ctx.FLOAT().getText() + " can not be parsed");
      }

      return Lists.<Object>newArrayList(Float.parseFloat(matcher.group(1)));
    }

    @Override
    public List<Object> visitValueSize(com.rocana.configuration.antlr.ConfigurationParser.ValueSizeContext ctx) {
      return Lists.<Object>newArrayList(ctx.SIZE().getText());
    }

    @Override
    public List<Object> visitValueDuration(com.rocana.configuration.antlr.ConfigurationParser.ValueDurationContext ctx) {
      return Lists.<Object>newArrayList(ctx.DURATION().getText());
    }

    @Override
    public List<Object> visitDictionary(com.rocana.configuration.antlr.ConfigurationParser.DictionaryContext ctx) {
      logger.debug("Visit dictionary. typeStack:{}", typeStack);

      TypeDescriptor currentType = typeStack.peek();

      if (currentType instanceof ObjectTypeDescriptor) {
        try {
          Object target = currentType.getTargetType().newInstance();

          logger.debug("Created target:{}", target);

          valueStack.push(target);
        } catch (InstantiationException | IllegalAccessException e) {
          e.printStackTrace();  // TODO: Unhandled catch block!
        }
      } else if (currentType instanceof MapTypeDescriptor) {
        Object target = Maps.newHashMap();

        logger.debug("Created target:{}", target);

        valueStack.push(target);
      } else {
        throw new ConfigurationException("Found a dictionary when expecting type " + currentType);
      }

      List<Object> result = Lists.newArrayList();

      visitChildren(ctx);

      result.add(valueStack.pop());

      logger.debug("Result:{}", result);

      return result;
    }

    @Override
    public List<Object> visitField(com.rocana.configuration.antlr.ConfigurationParser.FieldContext ctx) {
      String fieldName = ctx.ID().getText();
      List<Object> value = null;

      logger.debug("Visit field:{} typeStack:{}", fieldName, typeStack);

      TypeDescriptor currentType = typeStack.peek();

      if (currentType instanceof ObjectTypeDescriptor) {
        ObjectTypeDescriptor objectDescriptor = (ObjectTypeDescriptor) currentType;

        Field field = objectDescriptor.getChildMap().get(fieldName);

        typeStack.push(field.getTypeDescriptor());

        value = visitChildren(ctx);
        logger.debug("Result:{}", value);

        typeStack.pop();

        try {
          field.getMethod().invoke(valueStack.peek(), value.get(0));
        } catch (IllegalAccessException | InvocationTargetException e) {
          e.printStackTrace();  // TODO: Unhandled catch block!
        }
      } else if (currentType instanceof MapTypeDescriptor) {
        MapTypeDescriptor mapTypeDescriptor = (MapTypeDescriptor) currentType;

        typeStack.push(mapTypeDescriptor.getChildren().get(0));

        value = visitChildren(ctx);

        logger.debug("Result:{}", value);

        typeStack.pop();

        ((Map<String, Object>) valueStack.peek()).put(fieldName, value.get(0));
      } else {
        throw new IllegalStateException("Parsing a field but type description is " + currentType);
      }

      return value;
    }

    @Override
    public List<Object> visitArray(com.rocana.configuration.antlr.ConfigurationParser.ArrayContext ctx) {
      logger.debug("Visit array. typeStack:{}", typeStack);

      List<Object> values = Lists.newArrayList();
      List<Object> result = Lists.newArrayList();

      TypeDescriptor typeDescriptor = typeStack.peek().getChildren().get(0);
      typeStack.push(typeDescriptor);
      valueStack.push(values);

      visitChildren(ctx);

      typeStack.pop();
      valueStack.pop();

      result.add(values);

      return result;
    }

    @Override
    public List<Object> visitArrayItem(com.rocana.configuration.antlr.ConfigurationParser.ArrayItemContext ctx) {
      logger.debug("Visit array item. typeStack:{}", typeStack);

      List<Object> result = visitChildren(ctx);

      ((List<Object>) valueStack.peek()).add(result.get(0));

      logger.debug("Result:{}", result);

      return null;
    }

    @Override
    public List<Object> visitConfig(com.rocana.configuration.antlr.ConfigurationParser.ConfigContext ctx) {
      logger.debug("Visit configuration");

      List<Object> value = visitDictionary(ctx.dictionary());

      logger.debug("Result:{}", value);

      configuration = (T) value.get(0);

      return null;
    }

    public T getResult() {
      return configuration;
    }

  }

}
