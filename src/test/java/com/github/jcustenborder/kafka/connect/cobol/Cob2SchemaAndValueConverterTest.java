/**
 * Copyright © 2017 Jeremy Custenborder (jcustenborder@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jcustenborder.kafka.connect.cobol;

import com.legstar.base.converter.Cob2ObjectConverter;
import com.legstar.base.converter.FromHostResult;
import com.legstar.base.type.CobolType;
import com.legstar.base.type.composite.CobolComplexType;
import com.legstar.base.type.primitive.CobolPackedDecimalType;
import com.legstar.base.type.primitive.CobolStringType;
import com.legstar.base.type.primitive.CobolZonedDecimalType;
import com.legstar.base.utils.HexUtils;
import org.apache.kafka.connect.data.SchemaAndValue;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

public class Cob2SchemaAndValueConverterTest {
  private static final Logger log = LoggerFactory.getLogger(Cob2SchemaAndValueConverterTest.class);

  @Test
  public void foo() {
    CobolComplexType.Builder typeBuilder = new CobolComplexType.Builder();
    typeBuilder.cobolName("Ams-Vendor");
    typeBuilder.name("AmsVendor");

    Map<String, CobolType> fieldTypes = new LinkedHashMap<>();
    typeBuilder.fields(fieldTypes);

    Class<?> cls = String.class;
    CobolStringType.Builder stringTypeBuilder = new CobolStringType.Builder(cls);
    stringTypeBuilder.cobolName("Brand");
    stringTypeBuilder.charNum(3);
    CobolType stringType = stringTypeBuilder.build();

    fieldTypes.put("Brand", stringType);

    CobolComplexType complexType = typeBuilder.build();

  }

}
