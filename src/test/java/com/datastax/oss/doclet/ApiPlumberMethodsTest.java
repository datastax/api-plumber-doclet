/*
 * Copyright (C) 2017-2017 DataStax Inc.
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
package com.datastax.oss.doclet;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class ApiPlumberMethodsTest extends ApiPlumberTestBase {

  @Test
  public void should_fail_if_method_uses_type_from_forbidden_package() {
    DocletResult result = runDoclet("src/test/java/samples/Methods.java", "java.math");
    assertThat(result.returnCode).isNotEqualTo(0);
    assertThat(result.errorOutput)
        .contains(
            "Method samples.Methods.leaksReturnType leaks java.math.BigDecimal (as its return type)",
            "Method samples.Methods.leaksParameter leaks java.math.BigDecimal (as parameter 'value')",
            "Method samples.Methods.leaksArrayOfReturnType leaks java.math.BigDecimal[] (as its return type)",
            "Found 3 errors");
  }
}
