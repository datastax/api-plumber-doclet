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

import com.sun.source.doctree.BlockTagTree;
import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree;
import com.sun.source.util.DocTrees;
import java.util.Locale;
import java.util.Set;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import jdk.javadoc.doclet.Doclet;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;

public class ApiPlumber implements Doclet {
  /** The command-line option to specify the packages that must not be leaked. */
  static final String FORBIDDEN_PACKAGE_OPTION = "-preventleak";

  /** The javadoc tag that causes the tool to ignore an element. */
  private static final String EXCLUDE_TAG_NAME = "leaks-private-api";

  @Override
  public String getName() {
    return "ApiPlumber";
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.RELEASE_5;
  }

  @Override
  public Set<? extends Option> getSupportedOptions() {
    return Set.of(forbiddenPackageOption);
  }

  private Reporter reporter;
  private ForbiddenPackageOption forbiddenPackageOption;
  private int errorCount;

  public ApiPlumber() {
    this.forbiddenPackageOption = new ForbiddenPackageOption();
  }

  private static boolean hasTag(DocCommentTree docCommentTree, String tag) {
    if (docCommentTree == null) {
      return false;
    }
    for (DocTree blockTag : docCommentTree.getBlockTags()) {
      if (blockTag instanceof BlockTagTree && ((BlockTagTree) blockTag).getTagName().equals(tag)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void init(Locale locale, Reporter reporter) {
    this.reporter = reporter;
  }

  @Override
  public boolean run(DocletEnvironment environment) {
    if (forbiddenPackageOption.getForbiddenPackages().isEmpty()) {
      reporter.print(
          Diagnostic.Kind.ERROR,
          String.format(
              "Usage: javadoc %1$s com.package1 [%1$s com.package2] " + "-doclet %2$s ...",
              FORBIDDEN_PACKAGE_OPTION, ApiPlumber.class.getName()));
      return false;
    }

    DocTrees docTrees = environment.getDocTrees();
    Types typeUtils = environment.getTypeUtils();
    for (Element el : environment.getIncludedElements()) {
      // only check classes visible outside this package
      if (!visibleOutsidePackage(el)) {
        continue;
      }

      // check we have class or interface type
      if (!el.getKind().isClass() && !el.getKind().isInterface()) {
        continue;
      }

      TypeElement clazz = (TypeElement) el;

      String classTypeName = qualifiedTypeName(clazz);

      // skip interfaces and superclasses if marked with java-doc tag
      if (!hasTag(docTrees.getDocCommentTree(clazz), EXCLUDE_TAG_NAME)) {
        // check interfaces
        for (TypeMirror interfaceTypeMirror : clazz.getInterfaces()) {
          checkSuperType(typeUtils, interfaceTypeMirror, classTypeName, "parent interface");
        }

        // check superclass
        TypeMirror superclassTypeMirror = clazz.getSuperclass();
        if (superclassTypeMirror.getKind() != TypeKind.NONE) {
          checkSuperType(typeUtils, superclassTypeMirror, classTypeName, "superclass");
        }
      }

      // check contained fields, methods & constructors
      for (Element enclosed : clazz.getEnclosedElements()) {
        if (!visibleOutsidePackage(enclosed)
            || hasTag(docTrees.getDocCommentTree(enclosed), EXCLUDE_TAG_NAME)) {
          continue;
        }

        if (enclosed.getKind().isField()) {
          VariableElement field = (VariableElement) enclosed;
          TypeMirror fieldTypeMirror = field.asType();

          if (isCheckableType(fieldTypeMirror)) {
            String fieldTypeName = qualifiedTypeName(typeUtils, fieldTypeMirror);
            checkAllowed(
                fieldTypeName,
                "Field %s.%s leaks %s%n",
                classTypeName,
                field.getSimpleName().toString(),
                fieldTypeName);
          }
        }

        if (enclosed.getKind() == ElementKind.METHOD) {
          ExecutableElement method = (ExecutableElement) enclosed;
          String methodName = method.getSimpleName().toString();
          TypeMirror returnType = method.getReturnType();
          if (isCheckableType(returnType)) {
            String returnTypeName = qualifiedTypeName(typeUtils, returnType);
            checkAllowed(
                returnTypeName,
                "Method %s.%s leaks %s (as its return type)%n",
                classTypeName,
                methodName,
                returnTypeName);
          }

          checkMethodParameters(typeUtils, method, classTypeName, "Method");
        }

        if (enclosed.getKind() == ElementKind.CONSTRUCTOR) {
          checkMethodParameters(
              typeUtils, (ExecutableElement) enclosed, classTypeName, "Constructor");
        }
      }
    }
    if (errorCount > 0) {
      reporter.print(
          Diagnostic.Kind.ERROR,
          String.format("%nFound %d error%s%n", errorCount, errorCount == 1 ? "" : "s"));
    }
    return errorCount == 0;
  }

  private static String qualifiedTypeName(Element el) {
    if (el instanceof TypeElement) {
      return ((TypeElement) el).getQualifiedName().toString();
    }

    // we only want to return types for TypeElements, if we got this far is a bug in the type
    // checking logic
    throw new RuntimeException(
        String.format("cannot get qualified type name for non-TypeElement '%s'", el));
  }

  private String qualifiedTypeName(Types typeUtils, TypeMirror typeMirror) {
    // for arrays we want to get the name of the component type instead
    if (typeMirror.getKind() == TypeKind.ARRAY) {
      return qualifiedTypeName(typeUtils, ((ArrayType) typeMirror).getComponentType()) + "[]";
    }
    return qualifiedTypeName(typeUtils.asElement(typeMirror));
  }

  private void checkAllowed(String typeName, String errorFormat, Object... errorArguments) {
    for (String forbiddenPackage : forbiddenPackageOption.getForbiddenPackages()) {
      if (typeName.startsWith(forbiddenPackage)) {
        reporter.print(Diagnostic.Kind.ERROR, String.format(errorFormat, errorArguments));
        errorCount += 1;
      }
    }
  }

  private void checkSuperType(
      Types typeUtils, TypeMirror superTypeMirror, String classTypeName, String relationship) {
    String interfaceTypeName = qualifiedTypeName(typeUtils, superTypeMirror);
    checkAllowed(
        interfaceTypeName,
        "Type %s leaks %s (as a %s)%n",
        classTypeName,
        interfaceTypeName,
        relationship);

    for (TypeMirror argumentTypeMirror : ((DeclaredType) superTypeMirror).getTypeArguments()) {
      if (!isCheckableType(argumentTypeMirror)) {
        continue;
      }
      String typeArgumentName = qualifiedTypeName(typeUtils, argumentTypeMirror);
      checkAllowed(
          typeArgumentName,
          "Type %s leaks %s (as a type argument of its %s %s)%n",
          classTypeName,
          typeArgumentName,
          relationship,
          interfaceTypeName);
    }
  }

  private void checkMethodParameters(
      Types typeUtils, ExecutableElement method, String classTypeName, String methodKind) {
    String methodName = method.getSimpleName().toString();
    for (VariableElement parameter : method.getParameters()) {
      TypeMirror parameterTypeMirror = parameter.asType();
      if (!isCheckableType(parameterTypeMirror)) {
        continue;
      }

      String parameterTypeName = qualifiedTypeName(typeUtils, parameterTypeMirror);
      checkAllowed(
          parameterTypeName,
          "%s %s.%s leaks %s (as parameter '%s')%n",
          methodKind,
          classTypeName,
          methodName,
          parameterTypeName,
          parameter.getSimpleName().toString());
    }
  }

  private boolean isCheckableType(TypeMirror type) {
    TypeKind kind = type.getKind();
    if (kind == TypeKind.VOID || kind.isPrimitive() || kind == TypeKind.TYPEVAR) {
      return false;
    }

    if (kind == TypeKind.ARRAY) {
      return isCheckableType(((ArrayType) type).getComponentType());
    }
    return true;
  }

  private boolean visibleOutsidePackage(Element el) {
    return el.getModifiers().contains(Modifier.PUBLIC)
        || el.getModifiers().contains(Modifier.PROTECTED);
  }
}
