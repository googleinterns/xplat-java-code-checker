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

  private final Set<String> visitedClasses = new HashSet<>();

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

  private String localClassLookupName(String enclosingClass, String outermostClass) {
    return enclosingClass.substring(outermostClass.lastIndexOf(".") + 1);
  }

  private String localClassNameMangle(String enclosingClass, String outermostClass) {
    StringBuilder newName = new StringBuilder();
    String outermost = outermostClass.substring(outermostClass.lastIndexOf(".") + 1);

    if (foundObjcClassNames.containsKey(outermost)) {
      newName.append(foundObjcClassNames.get(outermost));
    } else if (packageAnnotation != null) {
      newName.append(packageAnnotation);
      newName.append(outermost);
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
      StringBuilder lookupName = new StringBuilder(outermost);

      for (String part : remainingParts) {
        newName.append("_");
        lookupName.append(".");
        lookupName.append(part);

        if (foundObjcClassNames.containsKey(lookupName.toString())) {
          newName = new StringBuilder(foundObjcClassNames.get(lookupName.toString()));
        } else {
          newName.append(part.substring(0, 1).toUpperCase());
          newName.append(part.substring(1));
        }
      }
    }
    return newName.toString();
  }

  private String externalTypeNameMangle(String enclosingClass, String outermostClass) {
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

  private String parameterNameMangle(String name, List<? extends VariableTree> paramList) {
    StringBuilder output = new StringBuilder(name);
    boolean firstIter = true;

    for (VariableTree var : paramList) {
      Type type = ASTHelpers.getType(var);
      Symbol symbol = ASTHelpers.getSymbol(var);
      Symbol typeSymbol = ASTHelpers.getSymbol(var.getType());

      if (type == null) {
        continue;
      }

      if (firstIter) {
        output.append("With");
        firstIter = false;
      } else {
        output.append("_with");
      }

      if (type.getKind() == TypeKind.TYPEVAR) {
        output.append("Id");
      } else {
        String typeStr = type.toString();
        ClassSymbol outermost = typeSymbol.outermostClass();
        ClassSymbol containingClass = symbol.outermostClass();

        // Use localClassLookupName if the parameter type is local to this class
        if (outermost.equals(containingClass)) {
          String lookupName = localClassLookupName(typeSymbol.toString(),
              outermost.toString());

          ObjectiveCName[] objCNames = typeSymbol.enclClass()
              .getAnnotationsByType(ObjectiveCName.class);

          // If the class has not been visited yet, manually check if it has an ObjectiveCName
          if (!visitedClasses.contains(lookupName) && objCNames.length != 0) {
            foundObjcClassNames.put(lookupName, objCNames[0].value());
            visitedClasses.add(lookupName);
          }

          output.append(localClassNameMangle(typeStr, outermost.toString()));
        } else {
          output.append(
              externalTypeNameMangle(typeSymbol.enclClass().toString(), outermost.toString()));
        }
      }
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
      output.append(
          localClassNameMangle(symbol.enclClass().toString(), symbol.outermostClass().toString()));
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
        output.append(parameterNameMangle(symbol.name.toString(), tree.getParameters()));
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
    String encl = ASTHelpers.getSymbol(tree).enclClass().toString();
    String outer = ASTHelpers.getSymbol(tree).outermostClass().toString();
    String lookupName = localClassLookupName(encl, outer);

    // If class was checked manually already, it can be skipped
    if (visitedClasses.contains(lookupName)) {
      return Description.NO_MATCH;
    }

    if (OBJC_NAME_MATCHER.matches(tree, state)) {
      for (AnnotationTree annTree : tree.getModifiers().getAnnotations()) {
        String source = state.getSourceForNode(annTree.getAnnotationType());

        if (source != null && source.equals("ObjectiveCName")) {
          String value = annTree.getArguments().toString();
          foundObjcClassNames.put(lookupName,
              value.substring(value.indexOf("\"") + 1, value.lastIndexOf("\"")));
          break;
        }
      }
    }
    visitedClasses.add(lookupName);
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
