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

public class ApiPlumberFieldTest extends ApiPlumberTestBase {

  @Test
  public void should_succeed_if_forbidden_package_not_used() {
    DocletResult result = runDoclet("src/test/java/samples/Fields.java", "java.text");
    assertThat(result.returnCode).isEqualTo(0);
  }

  @Test
  public void should_succeed_if_forbidden_package_used_in_private_fields_only() {
    DocletResult result = runDoclet("src/test/java/samples/Fields.java", "java.util");
    assertThat(result.returnCode).isEqualTo(0);
  }

  @Test
  public void should_fail_if_forbidden_package_used_in_public_and_protected_fields() {
    DocletResult result = runDoclet("src/test/java/samples/Fields.java", "java.math");
    assertThat(result.returnCode).isNotEqualTo(0);
    assertThat(result.errorOutput)
        .contains(
            "Field samples.Fields.publicBigInteger leaks java.math.BigInteger",
            "Field samples.Fields.protectedBigInteger leaks java.math.BigInteger",
            "Found 2 errors");
  }
}
