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
import org.apache.kafka.common.config.ConfigDef;

import java.util.Map;

class FromCopybookConfig extends CopybookConfig {
  public static final String NAMESPACE_CONFIG = "namespace";
  public static final String NAMESPACE_DOC = "Namespace for the generated schemas";

  public final String namespace;

  public FromCopybookConfig(Map<String, ?> settings) {
    super(config(), settings);
    this.namespace = this.getString(NAMESPACE_CONFIG);
  }

  public static ConfigDef config() {
    return CopybookConfig.config()
        .define(
            ConfigKeyBuilder.of(NAMESPACE_CONFIG, ConfigDef.Type.STRING)
                .defaultValue(FromCopybookConfig.class.getPackage().getName())
                .importance(ConfigDef.Importance.HIGH)
                .documentation(NAMESPACE_DOC)
                .build()
        );
  }
}
