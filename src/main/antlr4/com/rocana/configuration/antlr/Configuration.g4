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

grammar Configuration;

/*
 * Top level rule: config.
 *
 * A configuration may be a formal dictionary or a dictionary field list, for
 * convenience. The latter is primarily for configuration files.
 */
// config: (dictionary | field_list) EOF;
config: dictionary EOF;

/*
 * Container types.
 */
dictionary: BRACE_LEFT field_list? BRACE_RIGHT;
array: ARRAY_LEFT array_item_list? ARRAY_RIGHT;

field_list: field (COMMA? field)* # FieldList;
field: ID COLON value;

array_item_list: array_item (COMMA? array_item)* # ArrayItemList;
array_item: value # ArrayItem;

/*
 * Primary value type. Encompasses all other legal value types.
 */
value: value_variable |
  value_size |
  value_duration |
  value_long |
  value_float |
  value_double |
  value_int |
  value_string |
  value_boolean |
  dictionary |
  array;

/*
 * Specific value types.
 */
value_size: SIZE # ValueSize;
value_duration: (DURATION_ISO8601 | DURATION_SIMPLE) # ValueDuration;
value_long: LONG # ValueLong;
value_float: FLOAT # ValueFloat;
value_double: DOUBLE # ValueDouble;
value_int: INT # ValueInteger;
value_string: QUOTED_STRING # ValueString;
value_variable: VARIABLE # ValueVariable;
value_boolean: BOOLEAN # ValueBoolean;

/*
 * Lexer rules.
 */

COMMENT_BLOCK: COMMENT_BLOCK_START (.*?) COMMENT_BLOCK_END -> channel(HIDDEN);
COMMENT_LINE: COMMENT_LINE_START ~'\n'* -> channel(HIDDEN);

/*
 * Sigils.
 */
COLON: ':';
COMMA: ',';
DOT: '.';
DOLLAR: '$';
QUOTE_ESCAPED: '\\' (QUOTE_DOUBLE | QUOTE_SINGLE);
QUOTE_DOUBLE: '"';
QUOTE_SINGLE: '\'';
SLASH_ESCAPED: SLASH SLASH;
SLASH: '\\';

COMMENT_BLOCK_START: '/*';
COMMENT_BLOCK_END: '*/';
COMMENT_LINE_START: '//';

ARRAY_LEFT: '[';
ARRAY_RIGHT: ']';
BRACE_LEFT: '{';
BRACE_RIGHT: '}';

/*
 * Literal types.
 */
LONG: '-'? DIGITS 'L';
FLOAT: '-'? DIGITS ((DOT DIGITS) 'F'? | 'F');
DOUBLE: '-'? DIGITS ((DOT DIGITS) 'D' | 'D');
DURATION_ISO8601: 'P' (
  (FLOAT_IMPLICIT 'Y' FLOAT_IMPLICIT 'M' FLOAT_IMPLICIT 'D' DURATION_TIME_ISO8601) |
  (FLOAT_IMPLICIT 'Y' FLOAT_IMPLICIT 'M' FLOAT_IMPLICIT 'D') |
  (FLOAT_IMPLICIT 'Y' FLOAT_IMPLICIT 'M') |
  (FLOAT_IMPLICIT 'Y') |

  (FLOAT_IMPLICIT 'Y' FLOAT_IMPLICIT 'D' DURATION_TIME_ISO8601) |
  (FLOAT_IMPLICIT 'Y' FLOAT_IMPLICIT 'D') |

  (FLOAT_IMPLICIT 'Y' DURATION_TIME_ISO8601) |

  (FLOAT_IMPLICIT 'M' FLOAT_IMPLICIT 'D' DURATION_TIME_ISO8601) |
  (FLOAT_IMPLICIT 'M' FLOAT_IMPLICIT 'D') |
  (FLOAT_IMPLICIT 'M') |

  (FLOAT_IMPLICIT 'M' DURATION_TIME_ISO8601) |

  (FLOAT_IMPLICIT 'D' DURATION_TIME_ISO8601) |
  (FLOAT_IMPLICIT 'D') |

  (DURATION_TIME_ISO8601)
);
DURATION_SIMPLE: (
  (FLOAT_IMPLICIT WS UNIT_TIME_YEAR) |
  (FLOAT_IMPLICIT WS UNIT_TIME_MONTH) |
  (FLOAT_IMPLICIT WS UNIT_TIME_DAY) |
  (FLOAT_IMPLICIT WS UNIT_TIME_HOUR) |
  (FLOAT_IMPLICIT WS UNIT_TIME_MINUTE) |
  (FLOAT_IMPLICIT WS UNIT_TIME_SECOND) |
  (FLOAT_IMPLICIT WS UNIT_TIME_MILLI) |
  (FLOAT_IMPLICIT WS UNIT_TIME_NANO) |
  (FLOAT_IMPLICIT WS UNIT_TIME_MICRO)
);
SIZE: '-'? DIGITS (DOT DIGITS)? WS? [KMGTPEZY]? [bB];
INT: '-'? DIGITS;
QUOTED_STRING: QUOTE_DOUBLE (QUOTE_ESCAPED|SLASH_ESCAPED|.)*? QUOTE_DOUBLE;
BOOLEAN: BOOLEAN_TRUE | BOOLEAN_FALSE;

/*
 * Variables.
 */
VARIABLE: DOLLAR BRACE_LEFT ID BRACE_RIGHT;

/*
 * Time units.
 *
 * These are fragments so they do not interfere with identifiers.
 */
fragment UNIT_TIME_MICRO: 'micro' 's'?;
fragment UNIT_TIME_NANO: 'nano' 's'?;
fragment UNIT_TIME_MILLI: 'milli' 's'?;
fragment UNIT_TIME_SECOND: 'second' 's'?;
fragment UNIT_TIME_MINUTE: 'minute' 's'?;
fragment UNIT_TIME_HOUR: 'hour' 's'?;
fragment UNIT_TIME_DAY: 'day' 's'?;
fragment UNIT_TIME_WEEK: 'week' 's'?;
fragment UNIT_TIME_MONTH: 'month' 's'?;
fragment UNIT_TIME_YEAR: 'year' 's'?;

fragment BOOLEAN_TRUE: 'true' | 't' | 'yes' | 'enable' | 'on';
fragment BOOLEAN_FALSE: 'false' | 'f' | 'no' | 'disable' | 'off';

fragment FLOAT_IMPLICIT: '-'? DIGITS (DOT DIGITS)?;

fragment DURATION_TIME_ISO8601: 'T' (
  (FLOAT_IMPLICIT 'H' FLOAT_IMPLICIT 'M' FLOAT_IMPLICIT 'S') |
  (FLOAT_IMPLICIT 'H' FLOAT_IMPLICIT 'M') |
  (FLOAT_IMPLICIT 'H' FLOAT_IMPLICIT 'S') |
  (FLOAT_IMPLICIT 'M' FLOAT_IMPLICIT 'S') |
  (FLOAT_IMPLICIT 'H') |
  (FLOAT_IMPLICIT 'M') |
  (FLOAT_IMPLICIT 'S')
);

/*
 * Identifier type.
 *
 * This must be low priority, so it's down here on purpose.
 */
ID: [a-zA-Z] ( [a-zA-Z0-9_\-\.] )*;

/*
 * Whitespace.
 *
 * Whitespace isn't discarded so parser have the option of preserving it if they
 * so choose. It is, however, placed on on a separate channel.
 */
WS: [ \r\n\t]+ -> channel(HIDDEN);

/*
 * Other useful fragments.
 */
fragment DIGITS: [0-9]+;
