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

import com.google.common.base.MoreObjects;
import com.legstar.base.generator.Xsd2CobolTypesModelBuilder;
import com.legstar.cob2xsd.Cob2Xsd;
import com.legstar.cob2xsd.Cob2XsdConfig;
import com.legstar.cob2xsd.antlr.RecognizerException;
import com.legstar.cobol.model.CobolDataItem;
import org.apache.kafka.connect.data.Decimal;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public class CopybookSchemaBuilder {
  private static final Logger log = LoggerFactory.getLogger(CopybookSchemaBuilder.class);
  //  final Map<String, Schema> schemas = new HashMap<>();
  final FromCopybookConfig config;


  public CopybookSchemaBuilder(FromCopybookConfig config) {
    this.config = config;
  }

  SchemaBuilder fieldBuilder(CobolField field) {
    SchemaBuilder builder;

    switch (field.javaTypeName()) {
      case "String":
        builder = SchemaBuilder.string();
        break;
      case "Integer":
        builder = SchemaBuilder.int32();
        break;
      case "Long":
        builder = SchemaBuilder.int64();
        break;
      case "java.math.BigDecimal":
        int scale = field.getFractionDigits();
        builder = Decimal.builder(scale);
        break;
      default:
        throw new DataException(
            String.format("%s is not a supported type. %s", field.fieldName(), field)
        );
    }


    //TODO: Figure out how to determine if something is optional
    builder.optional();

    //Copy over the parameters to the schema.
    Map<String, String> parameters = new LinkedHashMap<>();
    field.values.forEach((key, value) -> parameters.put(key, value.toString()));
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


      compositeType.complexTypes.forEach((schemaName, o) -> {
        final Map<String, Map<String, Object>> values = (Map<String, Map<String, Object>>) o;
        final List<CobolField> cobolFields = values.entrySet().stream()
            .map(CobolField::new)
            .collect(Collectors.toList());



        log.trace("build() - Building schema for {}. {}", schemaName, values);
        SchemaBuilder structBuilder = SchemaBuilder.struct()
            .optional()
            .name(String.format("%s.%s", this.config.namespace, schemaName))
            .parameter("cobolName", compositeType.cobolName)
            .parameter("name", schemaName);

        cobolFields.forEach(cobolField -> {
          log.trace("build() - Processing field {}.", cobolField);
          Schema fieldSchema;
          if (cobolField.isComplexType()) {
            fieldSchema = complexTypes.get(cobolField.complexTypeName());
          } else {
            SchemaBuilder fieldSchemaBuilder = fieldBuilder(cobolField);
            fieldSchema = fieldSchemaBuilder.build();
          }

          if (cobolField.isArray()) {
            structBuilder.field(
                cobolField.fieldName(),
                SchemaBuilder.array(fieldSchema).build()
            );
          } else {
            structBuilder.field(
                cobolField.fieldName(),
                fieldSchema
            );
          }
        });

        Schema schema = structBuilder.build();
        complexTypes.put(schemaName, schema);
      });
    }
    log.trace("{}", complexTypes);

    return complexTypes.get(rootModel);
  }

  static class CobolField {
    final String fieldName;
    final Map<String, Object> values;

    public String javaTypeName() {
      return (String) this.values.get("javaTypeName");
    }

    public boolean isComplexType() {
      return (boolean) this.values.getOrDefault("complexType", false);
    }

    CobolField(Map.Entry<String, Map<String, Object>> entry) {
      this.fieldName = entry.getKey();
      this.values = entry.getValue();
    }

    CobolField(String fieldName, Map<String, Object> values) {
      this.fieldName = fieldName;
      this.values = values;
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("fieldName", this.fieldName)
          .add("values", this.values)
          .toString();
    }

    public Integer fieldIndex() {
      return (Integer) this.values.get("fieldIndex");
    }

    public String complexTypeName() {
      return (String) this.values.get("complexTypeName");
    }

    public String cobolName() {
      return (String) this.values.get("cobolName");
    }

    public Long minOccurs() {
      return (Long) this.values.get("minOccurs");
    }

    public Long maxOccurs() {
      return (Long) this.values.get("maxOccurs");
    }

    public String fieldName() {
      return this.fieldName;
    }

    public int getFractionDigits() {
      return (Integer) this.values.get("fractionDigits");
    }

    public boolean isArray() {
      return (minOccurs() != null && minOccurs() > 0) | (maxOccurs() != null && maxOccurs() > 0);
    }
  }
}
