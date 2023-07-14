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

import java.math.BigInteger;
import java.util.UUID;

public class Fields {

  public String string;

  /* public */

  public BigInteger publicBigInteger;

  /** @leaks-private-api */
  public BigInteger publicAnnotatedBigInteger;

  /* protected */

  protected BigInteger protectedBigInteger;

  /** @leaks-private-api */
  public BigInteger protectedAnnotatedBigInteger;

  /* package-private */

  UUID packagePrivateUuid;

  /* private */

  private UUID privateUuid;
}
