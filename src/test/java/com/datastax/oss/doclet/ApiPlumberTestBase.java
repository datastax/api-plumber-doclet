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
import java.io.IOException;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;

public abstract class ApiPlumberTestBase {

  private static final String BASE_PATH;
  private static final String CLASSES_PATH;
  private static final String TOOLS_JAR_PATH;
  private static final String JAVADOC_PATH;

  static {
    FastClasspathScanner scanner = new FastClasspathScanner("!!", "jar:tools.jar");
    String classesPath = null;
    String toolsJarPath = null;
    for (File file : scanner.getUniqueClasspathElements()) {
      String path = file.getAbsolutePath();
      if (path.endsWith("target/classes")) {
        classesPath = path;
      } else if (path.endsWith("tools.jar")) {
        toolsJarPath = path;
      }
    }
    assertThat(classesPath).isNotNull();
    assertThat(toolsJarPath).isNotNull();
    CLASSES_PATH = classesPath;
    BASE_PATH = CLASSES_PATH.substring(0, CLASSES_PATH.length() - "target/classes".length());
    TOOLS_JAR_PATH = toolsJarPath;
    JAVADOC_PATH =
        TOOLS_JAR_PATH.substring(0, TOOLS_JAR_PATH.length() - "lib/tools.jar".length())
            + "bin/javadoc";
  }

  /**
   * @param sourceFile the Java file to process, relative to the project base.
   * @param forbiddenPackages the packages that must not be leaked (possibly empty).
   */
  protected DocletResult runDoclet(String sourceFile, String... forbiddenPackages) {
    CommandLine commandLine = new CommandLine(JAVADOC_PATH);

    for (String forbiddenPackage : forbiddenPackages) {
      commandLine =
          commandLine
              .addArgument(ApiPlumber.FORBIDDEN_PACKAGE_OPTION)
              .addArgument(forbiddenPackage);
    }
    commandLine
        .addArgument("-cp")
        .addArgument(TOOLS_JAR_PATH)
        .addArgument("-docletpath")
        .addArgument(CLASSES_PATH)
        .addArgument("-doclet")
        .addArgument(ApiPlumber.class.getName())
        .addArgument(BASE_PATH + sourceFile);

    try {

      Executor executor = new DefaultExecutor();

      executor.setWatchdog(new ExecuteWatchdog(60_000));

      ByteArrayOutputStream standardOutputStream = new ByteArrayOutputStream();
      ByteArrayOutputStream errorOutputStream = new ByteArrayOutputStream();
      executor.setStreamHandler(new PumpStreamHandler(standardOutputStream, errorOutputStream));

      // Consider all exit values valid (never throw), the tests will check themselves.
      executor.setExitValues(null);

      return new DocletResult(
          executor.execute(commandLine),
          // note: we should use the console's encoding here, but in practice it shouldn't matter
          standardOutputStream.toString(),
          errorOutputStream.toString());
    } catch (IOException e) {
      throw new AssertionError("Unexpected exception while running " + commandLine, e);
    }
  }
}
