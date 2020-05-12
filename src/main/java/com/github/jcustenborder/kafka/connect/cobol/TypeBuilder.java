/**
 * Copyright © 2017 Jeremy Custenborder (jcustenborder@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jcustenborder.kafka.connect.cobol;

import com.google.common.base.Preconditions;
import com.legstar.base.type.CobolType;
import com.legstar.base.type.composite.CobolArrayType;
import com.legstar.base.type.primitive.CobolStringType;
import com.legstar.cobol.model.CobolDataItem;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

abstract class TypeBuilder {
  static final Map<String, TypeBuilder> TYPE_BUILDERS;

  static {
    TYPE_BUILDERS =
        Stream.of(
            new StringTypeBuilder()
        )
            .collect(
                Collectors.toMap(
                    k -> k.type,
                    v -> v
                )
            );
  }

  static final Pattern PICTURE_PATTERN = Pattern.compile("^(\\S+)\\((\\d+)\\)$");

  public static TypeBuilder get(CobolDataItem cobolDataItem) {
    Preconditions.checkNotNull(cobolDataItem, "cobolDataItem cannot be null");

    TypeBuilder result;

    if (!cobolDataItem.getChildren().isEmpty()) {
      result = new ComplexTypeBuilder();
    } else {

      Matcher matcher = PICTURE_PATTERN.matcher(cobolDataItem.getPicture());
      Preconditions.checkState(
          matcher.matches(),
          "Could not match %s with '%s'",
          cobolDataItem,
          PICTURE_PATTERN.pattern()
      );

      String type = matcher.group(1);
      result = TYPE_BUILDERS.get(type);
    }


    return result;
  }

  public SchemaBuilder schemaBuilder(CobolDataItem cobolDataItem) {
    SchemaBuilder result = createSchemaBuilder(cobolDataItem);

    if (cobolDataItem.isArray()) {
      result = SchemaBuilder.array(result);
    }

    return result;
  }

  public CobolType cobolType(CobolDataItem cobolDataItem) {
    CobolType result = createCobolType(cobolDataItem);
    if (cobolDataItem.isArray()) {
      result = new CobolArrayType.Builder()
          .dependingOn(cobolDataItem.getDependingOn())
          .itemType(result)
          .minOccurs(cobolDataItem.getMinOccurs())
          .maxOccurs(cobolDataItem.getMaxOccurs())
          .build();
    }
    return result;
  }


  abstract SchemaBuilder createSchemaBuilder(CobolDataItem cobolDataItem);

  abstract CobolType createCobolType(CobolDataItem cobolDataItem);

  protected SchemaBuilder createSchemaBuilder(CobolDataItem cobolDataItem, Schema.Type type) {
    SchemaBuilder schemaBuilder = SchemaBuilder.type(type);
    schemaBuilder.optional();
    //TODO: Copy over all the cobol specifics to the params
    schemaBuilder.parameter("copybook.field.level.number", Integer.toString(cobolDataItem.getLevelNumber()));
    schemaBuilder.parameter("copybook.field.name", cobolDataItem.getCobolName());
    schemaBuilder.parameter(Constants.COBOL_SOURCE_LINE, Integer.toString(cobolDataItem.getSrceLine()));
    return schemaBuilder;
  }

  private static class ComplexTypeBuilder extends TypeBuilder {


    @Override
    SchemaBuilder createSchemaBuilder(CobolDataItem cobolDataItem) {
      return null;
    }

    @Override
    CobolType createCobolType(CobolDataItem cobolDataItem) {
      return null;
    }
  }

  private static abstract class PrimitiveTypeBuilder extends TypeBuilder {
    public final String type;

    protected PrimitiveTypeBuilder(String type) {
      this.type = type;
    }
  }


  private static class StringTypeBuilder extends PrimitiveTypeBuilder {
    public StringTypeBuilder() {
      super("X");
    }

    int length(CobolDataItem cobolDataItem) {
      Matcher matcher = PICTURE_PATTERN.matcher(cobolDataItem.getPicture());
      matcher.matches();
      String length = matcher.group(2);
      return Integer.parseInt(length);
    }


    @Override
    public SchemaBuilder createSchemaBuilder(CobolDataItem cobolDataItem) {
      SchemaBuilder result = createSchemaBuilder(cobolDataItem, Schema.Type.STRING);
      int length = length(cobolDataItem);
      result.parameter("copybook.field.length", Integer.toString(length));
      return result;
    }

    @Override
    public CobolType createCobolType(CobolDataItem cobolDataItem) {
      int length = length(cobolDataItem);
      return new CobolStringType.Builder<>(String.class)
          .cobolName(cobolDataItem.getCobolName())
          .charNum(length)
          .build();
    }
  }
}
