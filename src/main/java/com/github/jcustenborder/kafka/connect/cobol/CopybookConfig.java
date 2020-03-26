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

import com.github.jcustenborder.kafka.connect.utils.config.ConfigKeyBuilder;
import com.github.jcustenborder.kafka.connect.utils.config.ConfigUtils;
import com.github.jcustenborder.kafka.connect.utils.config.recommenders.Recommenders;
import com.github.jcustenborder.kafka.connect.utils.config.validators.Validators;
import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;

import java.util.Map;

public abstract class CopybookConfig extends AbstractConfig {
  public static final String COPYBOOK_CONF = "cobol.copybook";
  static final String COPYBOOK_DOC = "The text of the Cobol copybook structure inline as a string.";

  public static final String CONTEXT_TYPE_CONF = "cobol.context.type";
  static final String CONTEXT_TYPE_DOC = "The type of Cobol context to create. This is used to " +
      "determine how data is encoded. " + ConfigUtils.enumDescription(CobolContextType.class);

  public final String copybook;
  public final CobolContextType contextType;


  public CopybookConfig(ConfigDef configDef, Map<String, ?> parsedConfig) {
    super(configDef, parsedConfig);
    this.copybook = this.getString(COPYBOOK_CONF);
    this.contextType = ConfigUtils.getEnum(CobolContextType.class, this, CONTEXT_TYPE_CONF);
  }

  public static ConfigDef config() {
    return new ConfigDef()
        .define(
            ConfigKeyBuilder.of(COPYBOOK_CONF, ConfigDef.Type.STRING)
                .documentation(COPYBOOK_DOC)
                .importance(ConfigDef.Importance.HIGH)
                .build()
        )
        .define(
            ConfigKeyBuilder.of(CONTEXT_TYPE_CONF, ConfigDef.Type.STRING)
                .documentation(CONTEXT_TYPE_DOC)
                .importance(ConfigDef.Importance.HIGH)
                .validator(Validators.validEnum(CobolContextType.class))
                .recommender(Recommenders.enumValues(CobolContextType.class))
                .defaultValue(CobolContextType.Ebcdic.toString())
                .build()
        );
  }
}
