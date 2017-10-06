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

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ApiPlumberInnerClassTest extends ApiPlumberTestBase {

  @Test
  public void should_fail_if_forbidden_package_used_in_field_of_inner_class() {
    DocletResult result = runDoclet("src/test/java/samples/InnerClasses.java", "java.math");
    assertThat(result.returnCode).isNotEqualTo(0);
    assertThat(result.errorOutput)
        .contains(
            "Field samples.InnerClasses.Inner.bigInteger leaks java.math.BigInteger",
            "Found 1 error");
  }
}
