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

import com.google.common.collect.ImmutableMap;
import com.legstar.base.context.CobolContext;
import com.legstar.base.context.EbcdicCobolContext;
import com.legstar.base.converter.FromHostResult;
import com.legstar.base.type.CobolType;
import com.legstar.base.type.composite.CobolComplexType;
import com.legstar.base.type.primitive.CobolPackedDecimalType;
import com.legstar.base.type.primitive.CobolStringType;
import com.legstar.base.type.primitive.CobolZonedDecimalType;
import com.legstar.base.utils.HexUtils;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Struct;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@Disabled
public class Cob2StructConverterTest {

  static class CobolFlat01Record extends CobolComplexType {

    public CobolFlat01Record() {
      super(new CobolComplexType.Builder()
          .name("Flat01Record")
          .cobolName("FLAT01-RECORD")
          .fields(createFlat01RecordFields())
      );
    }

    private static Map<String, CobolType> createFlat01RecordFields() {

      Map<String, CobolType> fields = new LinkedHashMap<String, CobolType>();

      CobolZonedDecimalType<Long> comNumber =
          new CobolZonedDecimalType.Builder<Long>(Long.class)
              .cobolName("COM-NUMBER")
              .totalDigits(6)
              .build();
      fields.put("comNumber", comNumber);

      CobolStringType<String> comName =
          new CobolStringType.Builder<String>(String.class)
              .cobolName("COM-NAME")
              .charNum(20)
              .build();
      fields.put("comName", comName);

      CobolPackedDecimalType<BigDecimal> comAmount =
          new CobolPackedDecimalType.Builder<java.math.BigDecimal>(java.math.BigDecimal.class)
              .cobolName("COM-AMOUNT")
              .totalDigits(7)
              .fractionDigits(2)
              .build();
      fields.put("comAmount", comAmount);

      return fields;

    }


  }



  @Test
  public void foo() {
    CobolContext context = new EbcdicCobolContext();
    final String input = "       01 FLAT01-RECORD.\n" +
        "          05 COM-NUMBER         PIC 9(6).\n" +
        "          05 COM-NAME           PIC X(20).\n" +
        "          05 COM-AMOUNT         PIC 9(5)V99 COMP-3.";
    Map<String, String> settings = ImmutableMap.of(
        FromCopybookConfig.COPYBOOK_CONF, input,
        FromCopybookConfig.CONTEXT_TYPE_CONF, CobolContextType.Ascii.toString()
    );
    FromCopybookConfig config = new FromCopybookConfig(settings);
    CopybookSchemaBuilder schemaBuilder = new CopybookSchemaBuilder(config);
    Schema schema = schemaBuilder.build();
    Cob2StructConverter converter = new Cob2StructConverter.Builder()
        .cobolContext(context)
        .cobolComplexType(new CobolFlat01Record())
        .schema(schema)
        .build();

    FromHostResult<Struct> result = converter.convert(
        HexUtils.decodeHex("F0F0F1F0F4F3D5C1D4C5F0F0F0F0F4F3404040404040404040400215000F")
    );
    Struct struct = result.getValue();
    assertNotNull(struct);
  }

}
