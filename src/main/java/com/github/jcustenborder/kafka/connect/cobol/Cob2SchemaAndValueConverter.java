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

import com.legstar.base.converter.AbstractCob2ObjectConverter;
import com.legstar.base.converter.FromHostResult;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaAndValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Cob2SchemaAndValueConverter extends AbstractCob2ObjectConverter<SchemaAndValue> {
  private static final Logger log = LoggerFactory.getLogger(Cob2SchemaAndValueConverter.class);
  final Schema schema;
  final FromCopybookConfig config;

  private Cob2SchemaAndValueConverter(Builder builder) {
    super(builder);
    this.config = builder.config;
    CopybookSchemaBuilder schemaBuilder = new CopybookSchemaBuilder(this.config);

    this.schema = schemaBuilder.build();



//
//    CobolComplexType complexType = this.getCobolComplexType();
//
//    SchemaBuilder schemaBuilder = SchemaBuilder.struct()
//        .name(complexType.getName());
//
//    for (Map.Entry<String, CobolType> kvp : complexType.getFields().entrySet()) {
//      log.trace("Field({}) type = {}", kvp.getKey(), kvp.getValue().getCobolName());
//    }
//
//    this.schema = schemaBuilder.build();
  }

  @Override
  public FromHostResult<SchemaAndValue> convert(byte[] bytes, int i, int i1) {

    return null;
  }

  public static class Builder extends AbstractCob2ObjectConverter.Builder<SchemaAndValue, Builder> {
    final FromCopybookConfig config;

    public Builder(FromCopybookConfig config) {
      this.config = config;
    }

    public Cob2SchemaAndValueConverter build() {
      return new Cob2SchemaAndValueConverter(this);
    }


    protected Builder self() {
      return this;
    }
  }
}
