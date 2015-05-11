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
import com.rocana.configuration.antlr.ConfigurationLexer;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;

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
    ANTLRInputStream inputStream = new ANTLRInputStream(reader);
    ConfigurationLexer lexer = new ConfigurationLexer(inputStream);
    CommonTokenStream tokenStream = new CommonTokenStream(lexer);

    com.rocana.configuration.antlr.ConfigurationParser parser = new com.rocana.configuration.antlr.ConfigurationParser(tokenStream);

    ParseTree parseTree = parser.config();

    logger.debug("Parsed configuration:{}", parseTree.toStringTree(parser));

    /*
     * Use either new ParserTreeWalker(listener, parseTree) or
     * new ConfigurationHandler().visit(parseTree)
     * where listener is an impl of ConfigurationBaseListener or
     * ConfigurationHandler is an impl of ConfigurationBaseVisitor.
     */

    return null;
  }

}
