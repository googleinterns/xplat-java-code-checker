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
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.ClassTreeMatcher;
import com.google.errorprone.bugpatterns.BugChecker.MethodTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.matchers.Matchers;
import com.google.errorprone.matchers.MethodVisibility.Visibility;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.lang.model.type.TypeKind;

@BugPattern(
    name = "J2ObjCMethodName",
    summary = "Warns against long J2ObjC translated methods.",
    explanation =
        "J2ObjC translates Java methods in a specific format that can lead to unreasonably long"
            + " Objective-C methods when the Java method has many parameters and/or uses long"
            + " types. This checker calls out problematic methods.",
    severity = WARNING)
public class J2objcMethodName extends BugChecker implements MethodTreeMatcher,
    ClassTreeMatcher {

  private final Map<String, String> foundObjcClassNames = new HashMap<>();

  private static final Matcher<MethodTree> MATCHER =
      Matchers.anyOf(
          Matchers.isStatic(),
          Matchers.methodHasVisibility(Visibility.PRIVATE)
      );

  private static final Matcher<Tree> OBJC_NAME_MATCHER =
      Matchers.hasAnnotation("com.google.j2objc.annotations.ObjectiveCName");

  private String packageToCamelCase(String enclosingClass, String outermostClass, Symbol symbol) {
    StringBuilder newName = new StringBuilder();

    if (foundObjcClassNames.containsKey(symbol.outermostClass().getSimpleName().toString())) {
      newName.append(foundObjcClassNames.get(symbol.outermostClass().getSimpleName().toString()));
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

  private String packageToCamelCase(String enclosingClass, String outermostClass) {
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

      if (type == null) {
        continue;
      }

      if (type.getKind() == TypeKind.TYPEVAR) {
        newList.add("Id");
      } else {

        String typeStr = type.toString();

        if (typeStr != null) {
          int index = typeStr.indexOf("<");

          if (index != -1) {
            newList.add(packageToCamelCase(
                typeStr.substring(0, index),
                ASTHelpers.outermostClass(ASTHelpers.getSymbol(var.getType())).toString()));
          } else {
            newList.add(packageToCamelCase(
                typeStr,
                ASTHelpers.outermostClass(ASTHelpers.getSymbol(var.getType())).toString()));
          }
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
      output.append(packageToCamelCase(symbol.enclClass().toString(),
          symbol.outermostClass().toString(), symbol));
    }

    output.append("_");

    if (!tree.getModifiers().getAnnotations().isEmpty()) {
      for (AnnotationTree annTree : tree.getModifiers().getAnnotations()) {
        if (state.getSourceForNode(annTree.getAnnotationType()).equals("ObjectiveCName")) {
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

  @Override
  public Description matchMethod(MethodTree tree, VisitorState state) {
    if (MATCHER.matches(tree, state)) {
      //System.out.println(ASTHelpers.getSymbol(tree).enclClass());
      System.out.println(ASTHelpers.getSymbol(tree).name);
      System.out.println(methodNameMangle(tree, state));
      System.out.println();
    }

    return Description.NO_MATCH;
  }

  @Override
  public Description matchClass(ClassTree tree, VisitorState state) {
    if (OBJC_NAME_MATCHER.matches(tree, state)) {
      for (AnnotationTree annTree : tree.getModifiers().getAnnotations()) {
        if (state.getSourceForNode(annTree.getAnnotationType()).equals("ObjectiveCName")) {
          String value = annTree.getArguments().toString();
          foundObjcClassNames.put(tree.getSimpleName().toString(),
              value.substring(value.indexOf("\"") + 1, value.lastIndexOf("\"")));
          break;
        }
      }
    }

    return Description.NO_MATCH;
  }
}
