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

import com.google.common.base.CaseFormat;
import com.google.common.base.MoreObjects;
import com.legstar.base.type.CobolType;
import com.legstar.base.type.composite.CobolArrayType;
import com.legstar.base.type.composite.CobolComplexType;
import com.legstar.cob2xsd.Cob2Xsd;
import com.legstar.cob2xsd.Cob2XsdConfig;
import com.legstar.cob2xsd.antlr.RecognizerException;
import com.legstar.cobol.model.CobolDataItem;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

class CopybookLoader {
  private static final Logger log = LoggerFactory.getLogger(CopybookLoader.class);


  public static class State {
    public final CobolType cobolType;
    public final Schema schema;

    State(CobolType cobolType, Schema schema) {
      this.cobolType = cobolType;
      this.schema = schema;
    }

    public static State of(CobolType complexType, Schema schema) {
      return new State(complexType, schema);
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("cobolType", cobolType)
          .add("schema", schema)
          .toString();
    }
  }

  public State loadText(String copybookText) throws IOException {
    log.trace("loadText() - copybookText = '\n{}\n'", copybookText);
    try (StringReader reader = new StringReader(copybookText)) {
      return load(reader);
    }
  }

  List<CobolDataItem> loadCobolDataItems(Reader reader) throws IOException {
    Properties properties = Cob2XsdConfig.getDefaultConfigProps();
    properties.put("codeFormat", "FREE_FORMAT");
    Cob2XsdConfig config = new Cob2XsdConfig(properties);
    Cob2Xsd cob2Xsd = new Cob2Xsd(config);
    List<CobolDataItem> result;
    try {
      result = cob2Xsd.toModel(reader);
    } catch (RecognizerException e) {
      throw new IOException("Exception while parsing copybook to model", e);
    }
    for (String error : cob2Xsd.getErrorHistory()) {
      log.warn("build() - Recoverable error detected. {}", error);
    }
    return result;
  }

  String javaClassName(CobolDataItem cobolDataItem) {
    String result = cobolDataItem.getCobolName().replace('-', '_');
    result = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, result);
    return result;
  }

  String javaFieldName(CobolDataItem cobolDataItem) {
    String result = cobolDataItem.getCobolName().replace('-', '_');
    result = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, result);
    return result;
  }


  State convert(CobolDataItem cobolDataItem) {
    Schema schema;
    CobolType cobolType;

    if (cobolDataItem.getChildren().isEmpty()) {
      log.trace("convert() - isArray={} cobolDataItem = '{}'", cobolDataItem.isArray(), cobolDataItem);
      TypeBuilder typeBuilder = TypeBuilder.get(cobolDataItem);
      schema = typeBuilder.schemaBuilder(cobolDataItem).build();
      cobolType = typeBuilder.cobolType(cobolDataItem);
    } else {
      String javaName = javaClassName(cobolDataItem);
      log.trace("convert() - isArray={} javaName = '{}' cobolDataItem = '{}'", cobolDataItem.isArray(), javaName, cobolDataItem);
      SchemaBuilder schemaBuilder = SchemaBuilder.struct()
          .name(javaName)
          .parameter(Constants.COBOL_NAME, cobolDataItem.getCobolName())
          .parameter(Constants.COBOL_SOURCE_LINE, Integer.toString(cobolDataItem.getSrceLine()));

      CobolComplexType.Builder complexTypeBuilder = new CobolComplexType.Builder()
          .name(javaName)
          .cobolName(cobolDataItem.getCobolName());
      Map<String, CobolType> cobolFields = new LinkedHashMap<>();
      cobolDataItem.getChildren().forEach(childDataItem -> {
        String fieldName = javaFieldName(childDataItem);
        State state = convert(childDataItem);
        cobolFields.put(fieldName, state.cobolType);
        schemaBuilder.field(fieldName, state.schema);
      });
      complexTypeBuilder.fields(cobolFields);
      cobolType = complexTypeBuilder.build();
      schema = schemaBuilder.build();
      if (cobolDataItem.isArray()) {
        cobolType = new CobolArrayType.Builder()
            .itemType(cobolType)
            .maxOccurs(cobolDataItem.getMaxOccurs())
//            .minOccurs(Math.max(1, cobolDataItem.getMinOccurs()))
            .minOccurs(cobolDataItem.getMaxOccurs())
//            .dependingOn(cobolDataItem.getDependingOn())
            .build();
        schema = SchemaBuilder.array(schema);
      }
    }
    return State.of(cobolType, schema);
  }

  public State load(Reader reader) throws IOException {
    List<CobolDataItem> model = loadCobolDataItems(reader);
    List<State> states = model.stream()
        .map(this::convert)
        .collect(Collectors.toList());
    if (log.isTraceEnabled()) {
      states.forEach(state -> log.trace("load() - state = '{}'", state));
    }
    return states.get(0);
  }


}
