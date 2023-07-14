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
import java.util.AbstractList;

public class Superclasses {

  /* public */

  public abstract class PublicBigDecimalList extends AbstractList<BigDecimal> {}

  public abstract class PublicBigDecimalListList extends AbstractList<AbstractList<BigDecimal>> {}

  /** @leaks-private-api */
  public abstract class PublicAnnotatedBigDecimalList extends AbstractList<BigDecimal> {}

  /** @leaks-private-api */
  public abstract class PublicAnnotatedBigDecimalListList
      extends AbstractList<AbstractList<BigDecimal>> {}

  /* protected */

  protected abstract class ProtectedBigDecimalList extends AbstractList<BigDecimal> {}

  protected abstract class ProtectedBigDecimalListList
      extends AbstractList<AbstractList<BigDecimal>> {}

  /** @leaks-private-api */
  public abstract class ProtectedAnnotatedBigDecimalList extends AbstractList<BigDecimal> {}

  /** @leaks-private-api */
  public abstract class ProtectedAnnotatedBigDecimalListList
      extends AbstractList<AbstractList<BigDecimal>> {}

  /* package-private */

  abstract class PackagePrivateBigDecimalList extends AbstractList<BigDecimal> {}

  abstract class PackagePrivateBigDecimalListList extends AbstractList<AbstractList<BigDecimal>> {}

  /* private */

  abstract class PrivateBigDecimalList extends AbstractList<BigDecimal> {}

  abstract class PrivateBigDecimalListList extends AbstractList<AbstractList<BigDecimal>> {}
}
