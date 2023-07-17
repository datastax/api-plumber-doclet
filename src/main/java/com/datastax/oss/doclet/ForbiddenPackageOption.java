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

import java.util.ArrayList;
import java.util.List;
import jdk.javadoc.doclet.Doclet;

class ForbiddenPackageOption implements Doclet.Option {

  private List<String> forbiddenPackages = new ArrayList<>();

  @Override
  public int getArgumentCount() {
    return 1;
  }

  @Override
  public String getDescription() {
    return "Specifies package that should not be leaked";
  }

  @Override
  public Kind getKind() {
    return Kind.STANDARD;
  }

  @Override
  public List<String> getNames() {
    return List.of(ApiPlumber.FORBIDDEN_PACKAGE_OPTION);
  }

  @Override
  public String getParameters() {
    return "package";
  }

  @Override
  public boolean process(String option, List<String> arguments) {
    // consider also checking arguments[0] looks like a package
    if (ApiPlumber.FORBIDDEN_PACKAGE_OPTION.equals(option) && arguments.size() >= 1) {
      forbiddenPackages.add(arguments.get(0));
      return true;
    }
    return false;
  }

  public List<String> getForbiddenPackages() {
    return forbiddenPackages;
  }
}
