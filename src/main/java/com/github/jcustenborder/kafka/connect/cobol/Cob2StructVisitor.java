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

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.legstar.base.context.CobolContext;
import com.legstar.base.type.CobolType;
import com.legstar.base.type.composite.CobolArrayType;
import com.legstar.base.type.composite.CobolChoiceType;
import com.legstar.base.type.composite.CobolComplexType;
import com.legstar.base.type.primitive.CobolPrimitiveType;
import com.legstar.base.visitor.FromCobolChoiceStrategy;
import com.legstar.base.visitor.FromCobolVisitor;
import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Struct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

class Cob2StructVisitor extends FromCobolVisitor {
  private static final Logger log = LoggerFactory.getLogger(Cob2StructVisitor.class);
  private Object resultObject;

  private final PrimitiveTypeHandler primitiveTypeHandler = new ConnectPrimitiveTypeHandler();
  private final ChoiceTypeAlternativeHandler choiceTypeHandler = new ConnectChoiceTypeAlternativeHandler();

  public Object getResultObject() {
    return resultObject;
  }

  static void buildSchemaLookup(Map<String, Schema> schemas, Schema schema) {
    if (Schema.Type.STRUCT == schema.type()) {
      String cobolName = null != schema.parameters() ? schema.parameters().get(Constants.COBOL_NAME) : null;
      if (!Strings.isNullOrEmpty(cobolName)) {
        schemas.put(cobolName, schema);
      }
      for (Field field : schema.fields()) {
        buildSchemaLookup(schemas, field.schema());
      }
    } else if (Schema.Type.ARRAY == schema.type()) {
      buildSchemaLookup(schemas, schema.valueSchema());
    }
  }

  private final Map<String, Schema> schemas;

  public Cob2StructVisitor(CobolContext cobolContext, byte[] hostData, int start, int length, FromCobolChoiceStrategy customChoiceStrategy, Set<String> customVariables, Schema schema) {
    super(cobolContext, hostData, start, length, customChoiceStrategy, customVariables);
    Map<String, Schema> schemas = new LinkedHashMap<>();
    buildSchemaLookup(schemas, schema);
    this.schemas = ImmutableMap.copyOf(schemas);
  }

  @Override
  public void visit(CobolComplexType type) {
    log.trace("visit(CobolComplexType) - {}", type.getCobolName());
    Schema schema = this.schemas.get(type.getCobolName());
    Struct struct = new Struct(schema);
    super.visitComplexType(type, new ConnectComplexTypeChildHandler(struct));
    this.resultObject = struct;
  }

  @Override
  public void visit(CobolArrayType type) {
    log.trace("visit(CobolArrayType) - {} itemType={}", type.getCobolName(), type.getItemType().getCobolName());
    final List<Object> list = new ArrayList<Object>();
    super.visitCobolArrayType(type, new ConnectArrayTypeItemHandler(list));
    resultObject = list;
  }

  @Override
  public void visit(CobolChoiceType type) {
    log.trace("visit(CobolChoiceType) - {}", type.getCobolName());
    super.visitCobolChoiceType(type, choiceTypeHandler);
  }

  @Override
  public void visit(CobolPrimitiveType<?> type) {
    log.trace("visit(CobolPrimitiveType) - {}", type.getCobolName());
    super.visitCobolPrimitiveType(type, primitiveTypeHandler);
  }

  class ConnectComplexTypeChildHandler implements ComplexTypeChildHandler {
    private final Logger log = LoggerFactory.getLogger(ConnectComplexTypeChildHandler.class);
    private final Struct struct;
    private String previousFieldName;

    ConnectComplexTypeChildHandler(Struct struct) {
      this.struct = struct;
    }

    @Override
    public boolean preVisit(String fieldName, int fieldIndex, CobolType child) {
      log.info("preVisit() - fieldName='{}' fieldIndex={} child='{}({})'", fieldName, fieldIndex, child.getClass().getSimpleName(), child.getCobolName());
      previousFieldName = fieldName;
      return true;
    }

    @Override
    public boolean postVisit(String fieldName, int fieldIndex, CobolType child) {
      log.info("postVisit() - fieldName='{}' fieldIndex={} child='{}({})'", fieldName, fieldIndex, child.getClass().getSimpleName(), child.getCobolName());
      struct.put(this.previousFieldName, resultObject);
      return true;
    }
  }

  class ConnectArrayTypeItemHandler implements ArrayTypeItemHandler {
    private final Logger log = LoggerFactory.getLogger(ConnectArrayTypeItemHandler.class);
    private final List<Object> list;

    public ConnectArrayTypeItemHandler(List<Object> list) {
      this.list = list;
    }

    public boolean preVisit(int itemIndex, CobolType item) {
      log.trace("preVisit() - itemIndex={} cobolName = '{}'", itemIndex, item.getCobolName());
      return true;
    }

    public boolean postVisit(int itemIndex, CobolType item) {
      log.trace("postVisit() - itemIndex={} cobolName = '{}'", itemIndex, item.getCobolName());
      list.add(resultObject);
      return true;
    }

  }

  class ConnectPrimitiveTypeHandler implements PrimitiveTypeHandler {
    private final Logger log = LoggerFactory.getLogger(ConnectPrimitiveTypeHandler.class);

    public void preVisit(CobolPrimitiveType<?> type) {
      log.trace("preVisit() - {}", type.getCobolName());
    }

    @Override
    public void postVisit(CobolPrimitiveType<?> type, Object value) {
      log.trace("postVisit() - {}", type.getCobolName());
      if (value instanceof String) {
        String s = (String) value;
        if (Strings.isNullOrEmpty(s)) {
          resultObject = null;
        } else {
          resultObject = value;
        }
      } else if (value instanceof BigInteger) {
        resultObject = ((BigInteger) value).longValue();
      } else {
        resultObject = value;
      }
    }
  }

  class ConnectChoiceTypeAlternativeHandler implements
      ChoiceTypeAlternativeHandler {


    public void preVisit(String alternativeName, int alternativeIndex,
                         CobolType alternative) {
      // Set the alternative schema as current
//      previousSchema = currentSchema;
//      currentSchema = currentSchema.getTypes().get(alternativeIndex);
    }

    public void postVisit(String alternativeName, int alternativeIndex,
                          CobolType alternative) {
      // Restore the Avro schema context
//      currentSchema = previousSchema;
    }

  }
}
