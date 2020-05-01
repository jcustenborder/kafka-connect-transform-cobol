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
import com.github.jcustenborder.kafka.connect.utils.config.validators.Validators;
import org.apache.kafka.common.config.ConfigDef;

import java.nio.charset.Charset;
import java.util.Map;

class FromCopybookConfig extends CopybookConfig {
  public static final String NAMESPACE_CONFIG = "namespace";
  public static final String NAMESPACE_DOC = "Namespace for the generated schemas";
  public static final String CHARSET_CONFIG = "charset";
  public static final String CHARSET_DOC = "The charset to use when the copybook data is written as a String.";

  public final String namespace;
  public final Charset charset;

  public FromCopybookConfig(Map<String, ?> settings) {
    super(config(), settings);
    this.namespace = this.getString(NAMESPACE_CONFIG);
    this.charset = ConfigUtils.charset(this, CHARSET_CONFIG);
  }

  public static ConfigDef config() {
    return CopybookConfig.config()
        .define(
            ConfigKeyBuilder.of(NAMESPACE_CONFIG, ConfigDef.Type.STRING)
                .defaultValue(FromCopybookConfig.class.getPackage().getName())
                .importance(ConfigDef.Importance.HIGH)
                .documentation(NAMESPACE_DOC)
                .build()
        ).define(
            ConfigKeyBuilder.of(CHARSET_CONFIG, ConfigDef.Type.STRING)
                .defaultValue("UTF-8")
                .importance(ConfigDef.Importance.HIGH)
                .validator(Validators.validCharset())
                .documentation(CHARSET_DOC)
                .build()
        );
  }
}
