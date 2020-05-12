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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.jcustenborder.kafka.connect.utils.data.NamedTest;
import com.github.jcustenborder.kafka.connect.utils.data.TestDataUtils;
import com.google.common.collect.ImmutableMap;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.github.jcustenborder.kafka.connect.utils.AssertSchema.assertSchema;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public class CopybookSchemaBuilderTest {
  private static final Logger log = LoggerFactory.getLogger(CopybookSchemaBuilderTest.class);

  @BeforeEach
  public void setup() {

  }

  public static class FieldBuilderTestCase implements NamedTest {
    public Map<String, Object> input;
    public Schema expected;

    @JsonIgnore
    private String name;

    @Override
    public String testName() {
      return this.name;
    }

    @Override
    public void testName(String s) {
      this.name = s;
    }
  }

  @TestFactory
  public Stream<DynamicTest> fieldBuilder() throws IOException {
    List<FieldBuilderTestCase> testCases = TestDataUtils.loadJsonResourceFiles(this.getClass().getPackage().getName() + ".schema.fieldbuilder", FieldBuilderTestCase.class);

    final String input = "            01 Ams-Vendor.\n" +
        "               03 Brand               Pic x(3).\n" +
        "               03 Vendor-Number       Pic 9(8).\n" +
        "               03 Vendor-Name         Pic X(40).\n" +
        "               03 Filler-1            Pic X(15).\n" +
        "               03 Code-850            Pic 999.\n" +
        "               03 Value-P             Pic X.\n" +
        "               03 Filler              Pic X(3).\n" +
        "               03 Zero-Value          Pic 999.";
    Map<String, String> settings = ImmutableMap.of(FromCopybookConfig.COPYBOOK_CONF, input);
    FromCopybookConfig config = new FromCopybookConfig(settings);
    CopybookSchemaBuilder schemaBuilder = new CopybookSchemaBuilder(config);

    return testCases.stream().map(testCase -> dynamicTest(testCase.testName(), () -> {
      CopybookSchemaBuilder.CobolField field = new CopybookSchemaBuilder.CobolField("test", testCase.input);
      SchemaBuilder builder = schemaBuilder.fieldBuilder(field);
      assertNotNull(builder, "builder should not be null.");
      Schema actual = builder.build();
      assertSchema(testCase.expected, actual);
    }));
  }

  public static class SchemaBuilderTestCase implements NamedTest {
    public String input;
    public Schema expected;

    @JsonIgnore
    private String name;

    @Override
    public String testName() {
      return this.name;
    }

    @Override
    public void testName(String s) {
      this.name = s;
    }
  }

  @TestFactory
  public Stream<DynamicTest> build() throws IOException {
    List<SchemaBuilderTestCase> testCases = TestDataUtils.loadJsonResourceFiles(this.getClass().getPackage().getName() + ".schema.builder", SchemaBuilderTestCase.class);

    return testCases.stream().map(testCase -> dynamicTest(testCase.testName(), () -> {
      Map<String, String> settings = ImmutableMap.of(CopybookConfig.COPYBOOK_CONF, testCase.input);
      FromCopybookConfig config = new FromCopybookConfig(settings);
      CopybookSchemaBuilder schemaBuilder = new CopybookSchemaBuilder(config);
      Schema actual = schemaBuilder.build();
      assertSchema(testCase.expected, actual);
    }));
  }
}
