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

import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Resources;
import com.google.errorprone.BugPattern;
import com.google.errorprone.ErrorProneFlags;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.ImportTreeMatcher;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.bugpatterns.BugChecker.MethodTreeMatcher;
import com.google.errorprone.bugpatterns.BugChecker.NewClassTreeMatcher;
import com.google.errorprone.bugpatterns.BugChecker.VariableTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.util.ASTHelpers;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.code.Type;
import com.google.gson.JsonElement;
import com.google.common.base.Charsets;
import java.io.IOException;
import java.util.Map;


/**
 * Check for usage of some Joda-Time's classes, which can be found in {@code CLASS_NAMES}. Also
 * checks for the usage of several Joda-Time packages, which can be found in {@code PACKAGE_NAMES}.
 * These calls are banned due to their incompatibility with cross platform development.
 */
@BugPattern(
    name = "JodaTimeClassBan",
    summary = "Bans the usage of certain Joda-Time classes and packages for cross platform use.",
    explanation =
        "The usage of several Joda-Time classes and packages are banned from cross"
            + " platform development due to incompatibilities. They are unsupported on the web"
            + " and should also not be used on supported platforms.",
    severity = ERROR)
public class JodaTimeClassBan extends BugChecker
    implements MethodInvocationTreeMatcher, NewClassTreeMatcher, ImportTreeMatcher,
    VariableTreeMatcher, MethodTreeMatcher {

  private final ImmutableMap<String, String> PACKAGE_NAMES;

  private final ImmutableMap<String, String> CLASS_NAMES;

  private final ImmutableMap<String, String> METHOD_NAMES;


  private static ImmutableMap<String, String> getJsonData(String fileName) {
    java.lang.reflect.Type mapType = new TypeToken<Map<String, String>>() {
    }.getType();

    Map<String, String> map;
    JsonElement root = null;
    try {
      root = JsonParser
          .parseString(Resources.toString(Resources.getResource(fileName), Charsets.UTF_8));
    } catch (IOException e) {
      System.err.println("Resource missing - Xplatpackagebans.json}");
      System.exit(1);
    }
    map = new Gson().fromJson(root, mapType);
    return ImmutableMap.copyOf(map);
  }

  public JodaTimeClassBan() {
    PACKAGE_NAMES = getJsonData("Xplatpackagebans.json");
    CLASS_NAMES = getJsonData("Xplatclassbans.json");
    METHOD_NAMES = getJsonData("Xplatmethodbans.json");
  }

  public Description standardMessage(Tree tree, String target, String reason) {
    if (reason.length() == 0) {
      reason = "cross platform incompatibility.";
    }

    return buildDescription(tree)
        .setMessage(
            String.format("Use of %s has been banned due to %s", target, reason))
        .build();
  }

  public Description methodCallMessage(Tree tree, String method, String target, String reason) {
    if (reason.length() == 0) {
      reason = "cross platform incompatibility.";
    }

    return buildDescription(tree)
        .setMessage(
            String.format("Use of %s is not allowed, as %s has been banned due to %s", method,
                target, reason))
        .build();
  }

  public Description constructorMessage(Tree tree, String constructor, String target,
      String reason) {
    if (reason.length() == 0) {
      reason = "cross platform incompatibility.";
    }

    return buildDescription(tree)
        .setMessage(
            String.format(
                "Use of this constructor (%s) is not allowed, as %s"
                    + " is banned due to %s", constructor, target, reason))
        .build();
  }

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {

    Symbol methodSymbol = ASTHelpers.getSymbol(tree);
    Type methodRecvType = ASTHelpers.getReceiverType(tree);
    Type methodType = ASTHelpers.getType(tree);

    if (methodType != null && methodSymbol != null) {

      // checks receiver for banned classes/packages
      if (CLASS_NAMES.containsKey(methodRecvType.toString())) {
        return standardMessage(tree, methodRecvType.toString(),
            CLASS_NAMES.get(methodRecvType.toString()));
      } else if (PACKAGE_NAMES.containsKey(methodSymbol.packge().toString())) {
        return standardMessage(tree, methodSymbol.packge().toString(),
            PACKAGE_NAMES.get(methodSymbol.packge().toString()));
      }

      // checks caller for banned classes
      if (CLASS_NAMES.containsKey(methodType.toString())) {
        return methodCallMessage(tree, methodSymbol.toString(), methodType.toString(),
            CLASS_NAMES.get(methodType.toString()));
      }
      // checks caller for banned packages
      for (String pack_name : PACKAGE_NAMES.keySet()) {
        if (methodType.toString().startsWith(pack_name)) {
          return methodCallMessage(tree, methodSymbol.toString(), pack_name,
              PACKAGE_NAMES.get(pack_name));
        }
      }
    }

    // checks arguments for banned classes/packages
    for (ExpressionTree arg : tree.getArguments()) {
      Symbol argSymbol = ASTHelpers.getSymbol(arg);
      Type argType = ASTHelpers.getType(arg);

      if (argSymbol != null && argType != null && methodSymbol != null) {
        if (CLASS_NAMES.containsKey(argType.toString())) {
          return standardMessage(tree, methodSymbol.toString(),
              CLASS_NAMES.get(argType.toString()));
        } else if (PACKAGE_NAMES.containsKey(argSymbol.packge().toString())) {
          return standardMessage(tree, methodSymbol.toString(),
              PACKAGE_NAMES.get(argSymbol.packge().toString()));
        }
      }
    }

    return Description.NO_MATCH;
  }

  @Override
  public Description matchNewClass(NewClassTree tree, VisitorState state) {

    MethodSymbol constructorSymbol = ASTHelpers.getSymbol(tree);
    Type constructorType = ASTHelpers.getType(tree);

    if (constructorSymbol != null && constructorType != null) {
      // checks constructor for banned classes/packages
      if (CLASS_NAMES.containsKey(constructorType.toString())) {
        return standardMessage(tree, constructorType.toString(),
            CLASS_NAMES.get(constructorType.toString()));
      } else if (PACKAGE_NAMES.containsKey(constructorSymbol.packge().toString())) {
        return standardMessage(tree, constructorSymbol.packge().toString(),
            PACKAGE_NAMES.get(constructorSymbol.packge().toString()));
      }

      // checks parameters for banned classes/packages
      for (VarSymbol param : constructorSymbol.getParameters()) {
        if (CLASS_NAMES.containsKey(param.type.toString())) {
          return constructorMessage(tree, constructorSymbol.toString(), param.type.toString(),
              CLASS_NAMES.get(param.type.toString()));
        } else if (PACKAGE_NAMES.containsKey(param.packge().toString())) {
          return constructorMessage(tree, constructorSymbol.toString(),
              param.packge().toString(), PACKAGE_NAMES.get(param.packge().toString()));
        }
      }
    }
    return Description.NO_MATCH;
  }

  @Override
  public Description matchImport(ImportTree tree, VisitorState state) {
    Symbol importSymbol = ASTHelpers.getSymbol(tree.getQualifiedIdentifier());

    if (CLASS_NAMES.containsKey(importSymbol.toString())) {
      return standardMessage(tree, importSymbol.toString(),
          CLASS_NAMES.get(importSymbol.toString()));
    } else if (PACKAGE_NAMES.containsKey(importSymbol.packge().toString())) {
      return standardMessage(tree, importSymbol.toString(),
          PACKAGE_NAMES.get(importSymbol.packge().toString()));
    }

    return Description.NO_MATCH;
  }

  @Override
  public Description matchVariable(VariableTree tree, VisitorState state) {
    Type varType = ASTHelpers.getType(tree);

    if (varType != null) {
      if (CLASS_NAMES.containsKey(varType.toString())) {
        return standardMessage(tree, varType.toString(), CLASS_NAMES.get(varType.toString()));
      }

      for (String packName : PACKAGE_NAMES.keySet()) {
        if (varType.toString().startsWith(packName)) {
          return standardMessage(tree, packName, PACKAGE_NAMES.get(packName));
        }
      }
    }

    return Description.NO_MATCH;
  }

  @Override
  public Description matchMethod(MethodTree tree, VisitorState state) {
    Type methodType = ASTHelpers.getType(tree);

    if (methodType != null) {
      methodType = methodType.getReturnType();
      if (CLASS_NAMES.containsKey(methodType.toString())) {
        return standardMessage(tree, methodType.toString(), CLASS_NAMES.get(methodType.toString()));
      }

      for (String packName : PACKAGE_NAMES.keySet()) {
        if (methodType.toString().startsWith(packName)) {
          return standardMessage(tree, packName, PACKAGE_NAMES.get(packName));
        }
      }
    }
    return Description.NO_MATCH;
  }
}

