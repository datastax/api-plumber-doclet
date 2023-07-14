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
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public class Interfaces {

  public class StringIdentity implements Function<String, String> {
    @Override
    public String apply(String s) {
      return s;
    }
  }

  /** @leaks-private-api */
  public class AnnotatedStringIdentity implements Function<String, String> {
    /** @leaks-private-api */
    @Override
    public String apply(String s) {
      return s;
    }
  }

  public interface BigDecimalComparator extends Comparator<BigDecimal> {}

  /** @leaks-private-api */
  public interface AnnotatedBigDecimalComparator extends Comparator<BigDecimal> {}

  public interface BigDecimalListOfComparator extends Comparator<List<BigDecimal>> {}

  /** @leaks-private-api */
  public interface AnnotatedBigDecimalListOfComparator extends Comparator<List<BigDecimal>> {}

  /* check recursive types */
  public interface Builder<T> { }
  public interface MiddleBuilder<T> extends Builder<T> { }
  public interface TopLevelBuilder extends MiddleBuilder<TopLevelBuilder> { }
}
