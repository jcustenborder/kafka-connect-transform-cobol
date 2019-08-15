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

import com.legstar.base.generator.Xsd2CobolTypesModelBuilder;
import com.legstar.cob2xsd.Cob2Xsd;
import com.legstar.cob2xsd.Cob2XsdConfig;
import com.legstar.cob2xsd.antlr.RecognizerException;
import com.legstar.cobol.model.CobolDataItem;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.errors.DataException;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

class CopybookSchemaBuilder {
  private static final Logger log = LoggerFactory.getLogger(CopybookSchemaBuilder.class);
  final Map<String, Schema> schemas = new HashMap<>();
  final FromCopybookConfig config;


  public CopybookSchemaBuilder(FromCopybookConfig config) {
    this.config = config;
  }

  SchemaBuilder fieldBuilder(Map<String, Object> fieldProperties) {
    SchemaBuilder builder;
    log.trace("fieldBuilder({})", fieldProperties);
    String javaTypeName = (String) fieldProperties.get("javaTypeName");

    switch (javaTypeName) {
      case "String":
        builder = SchemaBuilder.string();
        break;
      case "Integer":
        builder = SchemaBuilder.int32();
        break;
      case "Long":
        builder = SchemaBuilder.int64();
        break;
      default:
        throw new UnsupportedOperationException(javaTypeName + " is not supported. " + fieldProperties);
    }

    //TODO: Figure out how to determine if something is optional
    builder.optional();


    Map<String, String> parameters = new LinkedHashMap<>(fieldProperties.size());
    for (Map.Entry<String, Object> kvp : fieldProperties.entrySet()) {
      parameters.put(kvp.getKey(), kvp.getValue().toString());
    }
    builder.parameters(parameters);

    return builder;
  }


  public Schema build() {
    Properties properties = Cob2XsdConfig.getDefaultConfigProps();
    properties.put("codeFormat", "FREE_FORMAT");

    Cob2XsdConfig config = new Cob2XsdConfig(properties);
    Cob2Xsd cob2Xsd = new Cob2Xsd(config);

    List<CobolDataItem> cobolDataItems;

    log.trace("build() - Parsing copybook\n{}", this.config.copybook);
    try (StringReader reader = new StringReader(this.config.copybook)) {
      cobolDataItems = cob2Xsd.toModel(reader);
      for (String error : cob2Xsd.getErrorHistory()) {
        log.warn("build() - Recoverable error detected. {}", error);
      }
    } catch (RecognizerException e) {
      throw new DataException("Exception thrown while trying to parse copybook", e);
    }

    XmlSchema xmlSchema = cob2Xsd.emitXsd(cobolDataItems, null);

    try (StringWriter writer = new StringWriter()) {
      xmlSchema.write(writer);
      writer.flush();
      log.trace("build() - Schema generated as:\n{}", writer);
      try (StringReader reader = new StringReader(writer.toString())) {
        xmlSchema = new XmlSchemaCollection().read(reader);
      }
    } catch (IOException e) {
      throw new DataException(e);
    }

    Xsd2CobolTypesModelBuilder typesModelBuilder = new Xsd2CobolTypesModelBuilder();
    Map<String, Xsd2CobolTypesModelBuilder.RootCompositeType> typesToModels = typesModelBuilder.build(xmlSchema);
    Map<String, Schema> complexTypes = new LinkedHashMap<>();

    String rootModel = null;

    for (Map.Entry<String, Xsd2CobolTypesModelBuilder.RootCompositeType> kvp : typesToModels.entrySet()) {
      Xsd2CobolTypesModelBuilder.RootCompositeType compositeType = kvp.getValue();
      rootModel = kvp.getKey();
      for (Map.Entry<String, Object> entry : compositeType.complexTypes.entrySet()) {
        String schemaName = entry.getKey();
        log.trace("build() - Building schema for {}", schemaName);
        SchemaBuilder structBuilder = SchemaBuilder.struct()
            .name(String.format("%s.%s", this.config.namespace, schemaName))
            .parameter("cobolName", compositeType.cobolName)
            .parameter("name", schemaName);
        for (Map.Entry<String, Map<String, Object>> fieldProperties : ((Map<String, Map<String, Object>>) entry.getValue()).entrySet()) {
          log.trace("build() - Processing field {}", fieldProperties.getKey());
          SchemaBuilder fieldSchemaBuilder = fieldBuilder(fieldProperties.getValue());
          structBuilder.field(
              fieldProperties.getKey(),
              fieldSchemaBuilder.build()
          );
        }

        Schema schema = structBuilder.build();
        complexTypes.put(entry.getKey(), schema);
      }
    }

    log.trace("{}", complexTypes);

    return complexTypes.get(rootModel);
  }
}
