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

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.ConstructorDoc;
import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.ExecutableMemberDoc;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.Type;
import java.util.HashSet;
import java.util.Set;

public class ApiPlumber {

  /** The command-line option to specify the packages that must not be leaked. */
  static final String FORBIDDEN_PACKAGE_OPTION = "-preventleak";

  /** The javadoc tag that causes the tool to ignore an element. */
  private static final String EXCLUDE_TAG_NAME = "leaks-private-api";

  public static int optionLength(String option) {
    switch (option) {
      case FORBIDDEN_PACKAGE_OPTION:
        return 2;
      default:
        return 0;
    }
  }

  public static boolean validOptions(String options[][], DocErrorReporter reporter) {
    if (parseForbiddenPackages(options).size() == 0) {
      reporter.printError(
          String.format(
              "Usage: javadoc %1$s com.package1 [%1$s com.package2] " + "-doclet %2$s ...",
              FORBIDDEN_PACKAGE_OPTION, ApiPlumber.class.getName()));
      return false;
    }
    return true;
  }

  public static boolean start(RootDoc root) {
    return new ApiPlumber(root).start();
  }

  private static Set<String> parseForbiddenPackages(String[][] options) {
    Set<String> result = new HashSet<>();
    for (String[] option : options) {
      if (option[0].equals(FORBIDDEN_PACKAGE_OPTION)) {
        result.add(option[1]);
      }
    }
    return result;
  }

  private final RootDoc root;
  private final Set<String> forbiddenPackages;
  private int errorCount;

  private ApiPlumber(RootDoc root) {
    this.root = root;
    this.forbiddenPackages = parseForbiddenPackages(root.options());
  }

  private boolean start() {
    for (ClassDoc clazz : root.classes()) {
      String classTypeName = clazz.qualifiedTypeName();

      for (Type interfaceType : clazz.interfaceTypes()) {
        System.out.println(interfaceType.asClassDoc());
        if (clazz.tags(EXCLUDE_TAG_NAME).length == 0) {
          String interfaceTypeName = interfaceType.qualifiedTypeName();
          checkAllowed(
              interfaceTypeName,
              "Type %s leaks %s (as a parent interface)%n",
              classTypeName,
              interfaceTypeName);
          // TODO check type arguments in implemented interfaces as in `Foo implements Comparable<ForbiddenType>`
          // For some reason, even if interfaceType is a GenericType, its typeArguments() are still
          // empty, I don't know how else to get them, but there must be a way since the standard
          // doclet prints them in the "implements" section.
          // (In most cases this should be caught anyway, because the type arguments are most likely
          // used elsewhere as parameter or return types.)
        }
      }

      Type superClassType = clazz.superclassType();
      if (superClassType != null) {
        String superclassTypeName = superClassType.qualifiedTypeName();
        if (clazz.tags(EXCLUDE_TAG_NAME).length == 0) {
          checkAllowed(
              superclassTypeName,
              "Type %s leaks %s (as a superclass)%n",
              classTypeName,
              superclassTypeName);
          // TODO check type arguments in superclass (same as interfaces above)
        }
      }

      for (FieldDoc field : clazz.fields()) {
        if (field.tags(EXCLUDE_TAG_NAME).length == 0) {
          String fieldTypeName = field.type().qualifiedTypeName();
          checkAllowed(fieldTypeName, "Field %s leaks %s%n", field.qualifiedName(), fieldTypeName);
        }
      }

      for (MethodDoc method : clazz.methods()) {
        if (method.tags(EXCLUDE_TAG_NAME).length == 0) {
          String methodName = method.qualifiedName();
          String returnTypeName = method.returnType().qualifiedTypeName();
          checkAllowed(
              returnTypeName,
              "Method %s leaks %s (as its return type)%n",
              methodName,
              returnTypeName);

          checkParametersAllowed(method);
        }
      }

      for (ConstructorDoc constructor : clazz.constructors()) {
        checkParametersAllowed(constructor);
      }
    }
    if (errorCount > 0) {
      System.err.printf("%nFound %d error%s%n", errorCount, errorCount == 1 ? "" : "s");
    }
    return errorCount == 0;
  }

  private void checkAllowed(String typeName, String errorFormat, Object... errorArguments) {
    for (String forbiddenPackage : forbiddenPackages) {
      if (typeName.startsWith(forbiddenPackage)) {
        System.err.printf(errorFormat, errorArguments);
        errorCount += 1;
      }
    }
  }

  private void checkParametersAllowed(ExecutableMemberDoc executable) {
    String name = executable.qualifiedName();
    for (Parameter parameter : executable.parameters()) {
      String parameterTypeName = parameter.type().qualifiedTypeName();
      checkAllowed(
          parameterTypeName,
          "%s %s leaks %s (as parameter '%s')%n",
          executable instanceof ConstructorDoc ? "Constructor" : "Method",
          name,
          parameterTypeName,
          parameter.name());
    }
  }
}
