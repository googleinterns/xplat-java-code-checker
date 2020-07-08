// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.google.errorprone.xplat.checker;

import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;

import com.google.common.base.Splitter;
import com.google.errorprone.BugPattern;
import com.google.errorprone.ErrorProneFlags;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.ClassTreeMatcher;
import com.google.errorprone.bugpatterns.BugChecker.CompilationUnitTreeMatcher;
import com.google.errorprone.bugpatterns.BugChecker.MethodTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.matchers.Matchers;
import com.google.errorprone.matchers.MethodVisibility.Visibility;
import com.google.errorprone.util.ASTHelpers;
import com.google.j2objc.annotations.ObjectiveCName;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.lang.model.type.TypeKind;

/**
 * Checks for Java methods that would become unreasonably long when translated by J2ObjC into a
 * function. If a method has 6+ parameters and its name would be 80+ characters, a warning is given.
 * By default, any method which would have a name with 300+ characters is warned against. This can
 * be changed with the command line argument {@code -XepOpt:J2ObjCMethodName:MethodNameLength=X}
 * where X is the minimum number of characters to warn on. For example, {@code
 * -XepOpt:J2ObjCMethodName:MethodNameLength=100} will warn against all methods that would result in
 * 100 or more characters after being translated to an Objective-C function.
 */
@BugPattern(
    name = "J2ObjCMethodName",
    summary = "Warns against long J2ObjC translated methods.",
    explanation =
        "J2ObjC translates Java methods in a specific format that can lead to unreasonably long"
            + " Objective-C functions when the Java method has many parameters and/or uses long"
            + " types. This checker calls out problematic methods and offers a fix.",
    severity = WARNING)
public class J2objcMethodName extends BugChecker implements MethodTreeMatcher,
    ClassTreeMatcher, CompilationUnitTreeMatcher {

  private final Map<String, String> foundObjcClassNames = new HashMap<>();

  private final Set<String> seenClasses = new HashSet<>();

  private final Map<String, Integer> usedMethodNames = new HashMap<>();

  private String packageAnnotation;

  private int methodNameLength = 300;

  private static final Matcher<MethodTree> MATCHER =
      Matchers.allOf(
          Matchers.isStatic(),
          Matchers.methodHasVisibility(Visibility.PUBLIC)
      );

  private static final Matcher<Tree> OBJC_NAME_MATCHER =
      Matchers.hasAnnotation("com.google.j2objc.annotations.ObjectiveCName");

  private Description genDescription(String message, MethodTree tree) {
    return buildDescription(tree)
        .setMessage(message)
        .addFix(SuggestedFix.builder()
            .addImport("com.google.j2objc.annotations.ObjectiveCName")
            .prefixWith(tree, String.format("@ObjectiveCName(\"%s\")\n",
                getMethodName(tree.getName().toString())))
            .build())
        .build();
  }

  private String classToCamelCase(String enclosingClass, String outermostClass, Symbol symbol) {
    StringBuilder newName = new StringBuilder();

    if (foundObjcClassNames.containsKey(symbol.outermostClass().getSimpleName().toString())) {
      newName.append(foundObjcClassNames.get(symbol.outermostClass().getSimpleName().toString()));
    } else if (packageAnnotation != null) {
      newName.append(packageAnnotation);
      newName.append(symbol.outermostClass().getSimpleName());
    } else {
      Iterable<String> packageParts = Splitter.on('.').split(outermostClass);

      for (String part : packageParts) {
        newName.append(part.substring(0, 1).toUpperCase());
        newName.append(part.substring(1));
      }
    }

    if (outermostClass.length() < enclosingClass.length()) {
      Iterable<String> remainingParts = Splitter.on('.')
          .split(enclosingClass.substring(outermostClass.length() + 1));

      for (String part : remainingParts) {
        newName.append("_");

        if (foundObjcClassNames.containsKey(part)) {
          newName = new StringBuilder(foundObjcClassNames.get(part));
        } else {
          newName.append(part.substring(0, 1).toUpperCase());
          newName.append(part.substring(1));
        }
      }
    }
    return newName.toString();
  }

  private String typeToCamelCase(String enclosingClass, String outermostClass) {
    StringBuilder newName = new StringBuilder();
    Iterable<String> packageParts = Splitter.on('.').split(outermostClass);

    for (String part : packageParts) {
      newName.append(part.substring(0, 1).toUpperCase());
      newName.append(part.substring(1));
    }

    if (outermostClass.length() < enclosingClass.length()) {
      Iterable<String> remainingParts = Splitter.on('.')
          .split(enclosingClass.substring(outermostClass.length() + 1));

      for (String part : remainingParts) {
        newName.append("_");
        newName.append(part.substring(0, 1).toUpperCase());
        newName.append(part.substring(1));
      }
    }
    return newName.toString();
  }

  private List<String> paramatersToCamelCase(List<? extends VariableTree> paramList) {
    List<String> newList = new ArrayList<>();

    for (VariableTree var : paramList) {
      Type type = ASTHelpers.getType(var);
      Symbol symbol = ASTHelpers.getSymbol(var);
      Symbol typeSymbol = ASTHelpers.getSymbol(var.getType());

      if (type == null) {
        continue;
      }

      if (type.getKind() == TypeKind.TYPEVAR) {
        newList.add("Id");
      } else {
        String typeStr = type.toString();
        ClassSymbol outermost = ASTHelpers.outermostClass(ASTHelpers.getSymbol(var.getType()));
        ClassSymbol containingClass = ASTHelpers.outermostClass(symbol);

        if (outermost == null || containingClass == null) {
          continue;
        }

        // If the type of the parameter is local to this class, uses classToCamelCase instead
        if (outermost.equals(containingClass)) {
          String className = typeSymbol.toString()
              .substring(typeSymbol.toString().lastIndexOf(".") + 1);

          ObjectiveCName[] objCNames = typeSymbol.enclClass()
              .getAnnotationsByType(ObjectiveCName.class);

          // If the class has not been visited yet, manually check if it has an ObjectiveCName
          if (!seenClasses.contains(className) && objCNames.length != 0) {
            foundObjcClassNames.put(className, objCNames[0].value());
            seenClasses.add(className);
          }

          newList.add(classToCamelCase(typeStr, outermost.toString(), symbol));
        } else {
          newList.add(typeToCamelCase(typeSymbol.enclClass().toString(), outermost.toString()));
        }
      }
    }
    return newList;
  }

  private String nameAndArgsToCamelCase(String name, List<String> argList) {
    StringBuilder output = new StringBuilder(name);

    for (int i = 0; i < argList.size(); i++) {
      if (i == 0) {
        output.append("With");
      } else {
        output.append("_with");
      }
      output.append(argList.get(i));
    }

    output.append("_");
    return output.toString();
  }

  private String methodNameMangle(MethodTree tree, VisitorState state) {
    StringBuilder output = new StringBuilder();

    Symbol symbol = ASTHelpers.getSymbol(tree);

    if (foundObjcClassNames.containsKey(symbol.enclClass().getSimpleName().toString())) {
      output.append(foundObjcClassNames.get(symbol.enclClass().getSimpleName().toString()));
    } else {
      output.append(classToCamelCase(symbol.enclClass().toString(),
          symbol.outermostClass().toString(), symbol));
    }

    output.append("_");

    if (!tree.getModifiers().getAnnotations().isEmpty()) {
      for (AnnotationTree annTree : tree.getModifiers().getAnnotations()) {
        String source = state.getSourceForNode(annTree.getAnnotationType());

        if (source != null && source.equals("ObjectiveCName")) {
          String value = annTree.getArguments().toString();
          output.append(value.substring(value.indexOf("\"") + 1, value.lastIndexOf("\"")));
          break;
        }
      }
    } else {
      if (tree.getParameters().isEmpty()) {
        output.append(symbol.name);
      } else {
        output.append(nameAndArgsToCamelCase(symbol.name.toString(),
            paramatersToCamelCase(tree.getParameters())));
      }
    }
    return output.toString();
  }

  private String getMethodName(String methodName) {
    if (usedMethodNames.containsKey(methodName)) {
      usedMethodNames.put(methodName, usedMethodNames.get(methodName) + 1);
      return String.format("%s%d", methodName, usedMethodNames.get(methodName));
    } else {
      usedMethodNames.put(methodName, 1);
      return methodName;
    }
  }

  public J2objcMethodName(ErrorProneFlags flags) {

    Optional<Integer> arg = flags.getInteger("J2ObjCMethodName:MethodNameLength");

    if (arg.isPresent()) {
      this.methodNameLength = arg.get();
    }
  }

  @Override
  public Description matchMethod(MethodTree tree, VisitorState state) {
    if (MATCHER.matches(tree, state)) {
      String mangledName = methodNameMangle(tree, state);

      if (mangledName.length() >= 100 && tree.getParameters().size() > 5) {
        return genDescription(
            String.format("This method should likely be refactored to have fewer"
                + " parameters and its name will be %d characters when translated to"
                + " Objective-C: %s", mangledName.length(), mangledName), tree);

      } else if (mangledName.length() >= methodNameLength) {
        return genDescription(
            String.format("This method name will be %d characters when translated to"
                + " Objective-C: %s", mangledName.length(), mangledName), tree);
      }
    }

    return Description.NO_MATCH;
  }

  /**
   * Looks for an ObjectiveCName annotation on a class. If one is found, stores the class name as
   * the key and the annotation value as the value in the map foundObjcClassNames.
   */
  @Override
  public Description matchClass(ClassTree tree, VisitorState state) {
    // If class was checked manually already, it can be skipped
    if (seenClasses.contains(tree.getSimpleName().toString())) {
      return Description.NO_MATCH;
    }

    if (OBJC_NAME_MATCHER.matches(tree, state)) {
      for (AnnotationTree annTree : tree.getModifiers().getAnnotations()) {
        String source = state.getSourceForNode(annTree.getAnnotationType());

        if (source != null && source.equals("ObjectiveCName")) {
          String value = annTree.getArguments().toString();
          foundObjcClassNames.put(tree.getSimpleName().toString(),
              value.substring(value.indexOf("\"") + 1, value.lastIndexOf("\"")));
          break;
        }
      }
    }
    seenClasses.add(tree.getSimpleName().toString());
    return Description.NO_MATCH;
  }

  /**
   * Looks for an ObjectiveCName annotation on a package. If one is found, stores the value in
   * packageAnnotation.
   */
  @Override
  public Description matchCompilationUnit(CompilationUnitTree tree, VisitorState state) {
    if (tree.getSourceFile() == null) {
      return Description.NO_MATCH;
    }
    String name = tree.getSourceFile().getName();
    int idx = name.lastIndexOf('/');
    if (idx != -1) {
      name = name.substring(idx + 1);
    }
    if (name.equals("package-info.java")) {
      for (AnnotationTree annTree : tree.getPackageAnnotations()) {
        String source = state.getSourceForNode(annTree.getAnnotationType());

        if (source != null && source.equals("ObjectiveCName")) {
          String value = annTree.getArguments().toString();
          this.packageAnnotation = value
              .substring(value.indexOf("\"") + 1, value.lastIndexOf("\""));
          break;
        }
      }
    }
    return Description.NO_MATCH;
  }

}
