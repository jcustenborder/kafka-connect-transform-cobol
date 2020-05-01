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

import com.github.jcustenborder.kafka.connect.utils.config.Description;
import com.github.jcustenborder.kafka.connect.utils.config.DocumentationTip;
import com.github.jcustenborder.kafka.connect.utils.transformation.BaseKeyValueTransformation;
import com.legstar.base.context.AsciiCobolContext;
import com.legstar.base.context.CobolContext;
import com.legstar.base.context.EbcdicCobolContext;
import com.legstar.base.converter.FromHostResult;
import com.legstar.base.type.composite.CobolComplexType;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigException;
import org.apache.kafka.connect.connector.ConnectRecord;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaAndValue;
import org.apache.kafka.connect.data.Struct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Map;

@Description("The FromCopybook transformation is used to convert binary data that is stored as a " +
    "Cobol Copybook and converts the data to a Kafka Connect Structure.")
@DocumentationTip("This transformation expects that the incoming data will be formatted as BYTES.")
public class FromCopybook<R extends ConnectRecord<R>> extends BaseKeyValueTransformation<R> {
  private static final Logger log = LoggerFactory.getLogger(FromCopybook.class);
  FromCopybookConfig config;

  protected FromCopybook(boolean isKey) {
    super(isKey);
  }

  @Override
  public ConfigDef config() {
    return FromCopybookConfig.config();
  }

  @Override
  public void close() {

  }


  Cob2StructConverter converter;

  @Override
  public void configure(Map<String, ?> settings) {
    this.config = new FromCopybookConfig(settings);
    CobolContext cobolContext;
    switch (this.config.contextType) {
      case Ascii:
        cobolContext = new AsciiCobolContext();
        break;
      case Ebcdic:
        cobolContext = new EbcdicCobolContext();
        break;
      default:
        throw new ConfigException(String.format("%s is not a supported context", this.config.contextType));
    }
    CopybookLoader loader = new CopybookLoader();
    CopybookLoader.State state;
    log.info("configure() - Loading copybook definition from {}", this.config.copybook);
    try (Reader reader = new StringReader(this.config.copybook)) {
      state = loader.load(reader);
    } catch (IOException e) {
      ConfigException exception = new ConfigException(
          FromCopybookConfig.COPYBOOK_CONF,
          this.config.copybook,
          "Exception thrown while loading copybook"
      );
      exception.initCause(e);
      throw exception;
    }
    if (!(state.cobolType instanceof CobolComplexType)) {
      throw new ConfigException(
          FromCopybookConfig.COPYBOOK_CONF,
          this.config.copybook,
          String.format(
              "Cobol Type of '%s' is not supported. This transformation only supports complex types.",
              state.cobolType.getClass().getName()
          )
      );
    }
    CobolComplexType complexType = (CobolComplexType) state.cobolType;
    this.converter = new Cob2StructConverter.Builder()
        .cobolContext(cobolContext)
        .cobolComplexType(complexType)
        .schema(state.schema)
        .build();
  }

  @Override
  protected SchemaAndValue processBytes(R record, Schema inputSchema, byte[] input) {
    FromHostResult<Struct> result = converter.convert(input, 0, input.length - 1);
    log.trace("processBytes() - processed {} of {} byte(s).", result.getBytesProcessed(), input.length);
    Struct struct = result.getValue();
    return new SchemaAndValue(struct.schema(), struct);
  }

  @Override
  protected SchemaAndValue processString(R record, Schema inputSchema, String text) {
    byte[] bytes = text.getBytes(this.config.charset);
    return processBytes(record, Schema.BYTES_SCHEMA, bytes);
  }


  public static class Key extends FromCopybook {
    public Key() {
      super(true);
    }
  }

  public static class Value extends FromCopybook {
    public Value() {
      super(false);
    }
  }
}
