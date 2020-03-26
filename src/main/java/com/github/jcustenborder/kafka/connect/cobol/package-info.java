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
@Introduction("The plugin contains transformations that are used to read binary data that is encoded as a " +
    "Cobol copybook to structured data that is native Kafka Connect. For example this functionality could " +
    "be used to convert data from Cobol copybooks to JSON or Avro when utilizing the proper Converter.")
@Title("Cobol Copybook Transformations")
@PluginOwner("jcustenborder")
@PluginName("kafka-connect-transform-cobol")
@DocumentationWarning("This project is still in an experimental phase.")
@DocumentationImportant("This project needs data for unit testing. Any copybook " +
    "definitions with corresponding example data will greatly aid the overall quality of this " +
    "project. If you can provide some example(s) please open an issue.")
package com.github.jcustenborder.kafka.connect.cobol;

import com.github.jcustenborder.kafka.connect.utils.config.DocumentationImportant;
import com.github.jcustenborder.kafka.connect.utils.config.DocumentationWarning;
import com.github.jcustenborder.kafka.connect.utils.config.Introduction;
import com.github.jcustenborder.kafka.connect.utils.config.PluginName;
import com.github.jcustenborder.kafka.connect.utils.config.PluginOwner;
import com.github.jcustenborder.kafka.connect.utils.config.Title;