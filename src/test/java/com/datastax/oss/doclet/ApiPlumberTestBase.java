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

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.spi.ToolProvider;

public abstract class ApiPlumberTestBase {

  private static final String BASE_PATH;
  private static final String CLASSES_PATH;

  static {
    FastClasspathScanner scanner = new FastClasspathScanner("!!");
    String classesPath = null;
    String toolsJarPath = null;
    for (File file : scanner.getUniqueClasspathElements()) {
      String path = file.getAbsolutePath();
      if (path.endsWith("target/classes")) {
        classesPath = path;
      }
    }
    assertThat(classesPath).isNotNull();
    CLASSES_PATH = classesPath;
    BASE_PATH = CLASSES_PATH.substring(0, CLASSES_PATH.length() - "target/classes".length());
  }

  /**
   * @param sourceFile the Java file to process, relative to the project base.
   * @param forbiddenPackages the packages that must not be leaked (possibly empty).
   */
  protected DocletResult runDoclet(String sourceFile, String... forbiddenPackages) {
    List<String> args = new ArrayList<>();
    for (String forbiddenPackage : forbiddenPackages) {
      args.add(ApiPlumber.FORBIDDEN_PACKAGE_OPTION);
      args.add(forbiddenPackage);
    }
    args.add("-docletpath");
    args.add(CLASSES_PATH);
    args.add("-doclet");
    args.add(ApiPlumber.class.getName());
    args.add(BASE_PATH + sourceFile);

    ByteArrayOutputStream standardOutputStream = new ByteArrayOutputStream();
    ByteArrayOutputStream errorOutputStream = new ByteArrayOutputStream();

    return new DocletResult(
        ToolProvider.findFirst("javadoc")
            .orElseThrow()
            .run(
                new PrintStream(standardOutputStream),
                new PrintStream(errorOutputStream),
                args.toArray(new String[0])),
        // note: we should use the console's encoding here, but in practice it shouldn't matter
        standardOutputStream.toString(),
        errorOutputStream.toString());
  }
}
