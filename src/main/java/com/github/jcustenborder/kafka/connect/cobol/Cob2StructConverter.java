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
import com.legstar.base.converter.AbstractCob2ObjectConverter;
import com.legstar.base.converter.FromHostResult;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Struct;

public class Cob2StructConverter extends AbstractCob2ObjectConverter<Struct> {
  final Schema schema;

  public Cob2StructConverter(Builder builder) {
    super(builder);
    this.schema = builder.schema;
  }

  public static class Builder extends AbstractCob2ObjectConverter.Builder<Struct, Builder> {
    Schema schema;

    public Builder schema(Schema schema) {
      this.schema = schema;
      return this;
    }

    @Override
    protected Builder self() {
      return this;
    }

    public Cob2StructConverter build() {
      Preconditions.checkNotNull(this.schema, "schema cannot be null.");
      return new Cob2StructConverter(this);
    }
  }


  @Override
  public FromHostResult<Struct> convert(byte[] hostData, int start, int length) {
    Cob2StructVisitor visitor = new Cob2StructVisitor(getCobolContext(), hostData,
        start, length, getCustomChoiceStrategy(), getCustomVariables(), schema);
    visitor.visit(getCobolComplexType());
    return new FromHostResult<>(visitor.getLastPos(), (Struct) visitor.getResultObject());
  }
}
