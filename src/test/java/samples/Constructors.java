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

public class Constructors {

  /* public */

  public static class PublicConstructors {

    public PublicConstructors(String s) {}

    public PublicConstructors(BigDecimal bd) {}

    /** @leaks-private-api */
    public PublicConstructors(BigDecimal bd, boolean b) {}
  }

  /* protected */

  public static class ProtectedConstructors {

    protected ProtectedConstructors(String s) {}

    protected ProtectedConstructors(BigDecimal bd) {}

    /** @leaks-private-api */
    protected ProtectedConstructors(BigDecimal bd, boolean b) {}
  }

  /* package-private */

  public static class PackagePrivateConstructors {

    PackagePrivateConstructors(String s) {}

    PackagePrivateConstructors(BigDecimal bd) {}
  }

  /* private */

  public static class PrivateConstructors {

    private PrivateConstructors(String s) {}

    private PrivateConstructors(BigDecimal bd) {}
  }
}
