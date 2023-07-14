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
package samples;

import java.math.BigDecimal;

public class Methods {

  /* public */

  public BigDecimal leaksReturnType() {
    return null;
  }

  public void leaksParameter(String key, BigDecimal value) {}

  /** @leaks-private-api */
  public BigDecimal annotatedReturnType() {
    return null;
  }

  /** @leaks-private-api */
  public void annotatedParameter(String key, BigDecimal value) {}

  /* protected */

  protected BigDecimal protectedLeaksReturnType() {
    return null;
  }

  protected void protectedLeaksParameter(String key, BigDecimal value) {}

  /** @leaks-private-api */
  protected BigDecimal protectedAnnotatedReturnType() {
    return null;
  }

  /** @leaks-private-api */
  protected void protectedAnnotatedParameter(String key, BigDecimal value) {}

  /* package-private */

  BigDecimal packagePrivateLeaksReturnType() {
    return null;
  }

  void packagePrivateLeaksParameter(String key, BigDecimal value) {}

  /* private */

  BigDecimal privateLeaksReturnType() {
    return null;
  }

  void privateLeaksParameter(String key, BigDecimal value) {}
}
