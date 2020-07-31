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

import com.google.auto.service.AutoService;
import com.google.common.io.Resources;
import com.google.errorprone.BugPattern;
import com.google.errorprone.ErrorProneFlags;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.bugpatterns.BugChecker.MethodTreeMatcher;
import com.google.errorprone.bugpatterns.BugChecker.NewClassTreeMatcher;
import com.google.errorprone.bugpatterns.BugChecker.VariableTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.code.Type;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Check for usage of some classes and packages, which can be found in resources/Xplatbans.json.
 * These calls are banned due to their incompatibility with cross platform development. Additional
 * classes can be banned using the command line argument {@code -XepOpt:XplatBans:JSON=X}, where X
 * is the path to a JSON file containing custom bans. Errors can be suppressed with the {@link
 * XplatBanSuppression} annotation.
 *
 * <p>The JSON file should be formatted in this way:
 *
 * <pre>
 * {
 *   "classes": {
 *     "java.util.HashMap": "a reason."
 *   },
 *   "packages": {
 *     "java.io": "another reason.",
 *     "foo.bar": "yet another reason."
 *   },
 *   "methods": {
 *     "java.util.HashSet": {
 *       "contains": "a further reason.",
 *       "remove": ""
 *     }
 *   }
 * }
 * </pre>
 *
 * In each error message, the string following the ban fills in the %s in the following string:
 * {@code "has been banned due to %s"}. If an empty string is provided, the default message will be
 * used. The JSON file should include each top level name {@code (classes, packages, methods)}, even
 * if they have no content.
 */
@AutoService(BugChecker.class)
@BugPattern(
    name = "XplatBans",
    summary = "Bans the usage of certain classes and packages for cross platform use.",
    explanation =
        "The usage of several classes and packages are banned from cross platform development due"
            + " to incompatibilities. Additional bans can be configured with the command line"
            + " argument -XepOpt:XplatBans:JSON=X, where X is the path to a JSON file containing"
            + " custom bans.",
    severity = ERROR,
    suppressionAnnotations = XplatBanSuppression.class)
public class XplatBans extends BugChecker
    implements MethodInvocationTreeMatcher,
        NewClassTreeMatcher,
        VariableTreeMatcher,
        MethodTreeMatcher {

  private final Map<String, String> packageNames = new HashMap<>();

  private final Map<String, String> classNames = new HashMap<>();

  private final Map<String, Map<String, String>> methodNames = new HashMap<>();


  /**
   * A helper function that iterates though the JSON keys and puts them inside a map.
   *
   * @param json The JSONObject to be iterated through.
   * @param map  The map to be added to.
   * @throws JSONException This function should never throw a JSONException, as the only time
   *                       getString() is used, it is being used with keys returned from keys().
   */
  private void addJsonToMap(JSONObject json, Map<String, String> map) throws JSONException {
    for (Iterator<?> it = json.keys(); it.hasNext(); ) {
      String key = it.next().toString();
      map.put(key, json.getString(key));
    }
  }

  /**
   * Given a correctly formatted JSON file, adds the bans to the respective maps.
   *
   * @param json     A string containing the file contents.
   * @param fileName The name of the file to be displayed in error messages.
   */
  private void getJsonData(String json, String fileName) {
    JSONObject obj;

    try {
      obj = new JSONObject(json);
    } catch (JSONException e) {
      System.err.println(String.format("JSON file '%s' is invalid. Unable to parse.", fileName));
      e.printStackTrace();
      return;
    }

    try {
      addJsonToMap(obj.getJSONObject("classes"), this.classNames);

    } catch (JSONException e) {
      System.err
          .println(String.format("Missing \"classes\" top level JSON name inside '%s'.", fileName));
      e.printStackTrace();
    }

    try {
      addJsonToMap(obj.getJSONObject("packages"), this.packageNames);

    } catch (JSONException e) {
      System.err.println(
          String.format("Missing \"packages\" top level JSON name inside '%s'.", fileName));
      e.printStackTrace();
    }

    try {
      JSONObject containingClasses = obj.getJSONObject("methods");

      for (Iterator<?> cont = containingClasses.keys(); cont.hasNext(); ) {
        String curClass = cont.next().toString();
        Map<String, String> localMap = new HashMap<>();

        addJsonToMap(containingClasses.getJSONObject(curClass), localMap);

        this.methodNames.put(curClass, localMap);
      }
    } catch (JSONException e) {
      System.err
          .println(String.format("Missing \"methods\" top level JSON name inside '%s'.", fileName));
      e.printStackTrace();
    }
  }

  /**
   * Adds bans to the maps based on the {@code Xplatbans.json} file. If the flag {@code
   * XplatBans:JSON} is used, tries to add bans to the maps from the given file.
   */
  public XplatBans(ErrorProneFlags flags) {
    try {
      getJsonData(readResourceAsString("Xplatbans.json"), "Xplatbans.json");
    } catch (IOException e) {
      System.err.println("Xplatbans.json resource file for XplatBan checker could not"
          + " be converted to a String.");
      throw new UncheckedIOException(e);
    } catch (IllegalArgumentException e) {
      System.err.println("Xplatbans.json resource file for XplatBan checker could not"
          + " be found.");
      throw new IllegalArgumentException(e);
    }

    Optional<String> arg;

    arg = flags.get("XplatBans:JSONResource");
    if (arg.isPresent()) {
      try {
        String jsonDataString = readResourceAsString(arg.get());
        getJsonData(jsonDataString, arg.get());
      } catch (IOException e) {
        System.err.println(
            "JSON resource argument for XplatBan checker could not"
                + " be found/read. Custom bans will not be in effect.");
        e.printStackTrace();
      }
    }

    arg = flags.get("XplatBans:JSON");
    if (arg.isPresent()) {
      try {
        String jsonDataString = readFileAsString(Paths.get(arg.get()));
        getJsonData(jsonDataString, arg.get());
      } catch (IOException e) {
        System.err.println("JSON file argument for XplatBan checker could not"
            + " be found/read. Custom bans will not be in effect.");
        e.printStackTrace();
      }
    }
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

  /**
   * Given a type, returns a String that removes type parameters.
   *
   * @param type the type that is needed for lookup in a map.
   * @return String that contains the type without type parameters.
   */
  private String typeToString(Type type) {
    if (type == null) {
      return "nullType";
    }
    String typeString = type.toString();

    if (typeString.contains("<")) {
      return typeString.substring(0, typeString.indexOf("<"));
    }
    return typeString;
  }

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {

    Symbol methodSymbol = ASTHelpers.getSymbol(tree);
    String methodRecvType = typeToString(ASTHelpers.getReceiverType(tree));
    String methodType = typeToString(ASTHelpers.getType(tree));

    if (methodSymbol != null) {

      // checks receiver for banned classes/packages
      if (classNames.containsKey(methodRecvType)) {
        return standardMessage(tree, methodRecvType,
            classNames.get(methodRecvType));
      } else if (packageNames.containsKey(methodSymbol.packge().toString())) {
        return standardMessage(tree, methodSymbol.packge().toString(),
            packageNames.get(methodSymbol.packge().toString()));
      }

      // checks if method was banned directly
      if (methodNames.containsKey(methodRecvType) && methodNames.get(methodRecvType)
          .containsKey(methodSymbol.getSimpleName().toString())) {
        return methodCallMessage(tree, methodSymbol.getSimpleName().toString() + "()",
            methodRecvType,
            methodNames.get(methodRecvType).get(methodSymbol.getSimpleName().toString()));
      }

      // checks caller for banned classes
      if (classNames.containsKey(methodType)) {
        return methodCallMessage(tree, methodSymbol.toString(), methodType,
            classNames.get(methodType));
      }

      // checks caller for banned packages
      for (String pack_name : packageNames.keySet()) {
        if (methodType.startsWith(pack_name)) {
          return methodCallMessage(tree, methodSymbol.toString(), pack_name,
              packageNames.get(pack_name));
        }
      }
    }

    // checks arguments for banned classes/packages
    for (ExpressionTree arg : tree.getArguments()) {
      Symbol argSymbol = ASTHelpers.getSymbol(arg);
      String argType = typeToString(ASTHelpers.getType(arg));

      if (argSymbol != null && methodSymbol != null) {
        if (classNames.containsKey(argType)) {
          return standardMessage(tree, methodSymbol.toString(),
              classNames.get(argType));
        } else if (packageNames.containsKey(argSymbol.packge().toString())) {
          return standardMessage(tree, methodSymbol.toString(),
              packageNames.get(argSymbol.packge().toString()));
        }
      }
    }

    return Description.NO_MATCH;
  }

  @Override
  public Description matchNewClass(NewClassTree tree, VisitorState state) {

    MethodSymbol constructorSymbol = ASTHelpers.getSymbol(tree);
    String constructorType = typeToString(ASTHelpers.getType(tree));

    if (constructorSymbol != null) {
      // checks constructor for banned classes/packages
      if (classNames.containsKey(constructorType)) {
        return standardMessage(tree, constructorType,
            classNames.get(constructorType));
      } else if (packageNames.containsKey(constructorSymbol.packge().toString())) {
        return standardMessage(tree, constructorSymbol.packge().toString(),
            packageNames.get(constructorSymbol.packge().toString()));
      }

      // checks parameters for banned classes/packages
      for (VarSymbol param : constructorSymbol.getParameters()) {
        String paramType = typeToString(param.type);

        if (classNames.containsKey(paramType)) {
          return constructorMessage(tree, constructorSymbol.toString(), paramType,
              classNames.get(paramType));
        } else if (packageNames.containsKey(param.packge().toString())) {
          return constructorMessage(tree, constructorSymbol.toString(),
              param.packge().toString(), packageNames.get(param.packge().toString()));
        }
      }
    }
    return Description.NO_MATCH;
  }

  @Override
  public Description matchVariable(VariableTree tree, VisitorState state) {
    String varType = typeToString(ASTHelpers.getType(tree));

    if (classNames.containsKey(varType)) {
      return standardMessage(tree, varType, classNames.get(varType));
    }

    for (String packName : packageNames.keySet()) {
      if (varType.startsWith(packName)) {
        return standardMessage(tree, packName, packageNames.get(packName));
      }
    }

    return Description.NO_MATCH;
  }

  @Override
  public Description matchMethod(MethodTree tree, VisitorState state) {
    Type type = ASTHelpers.getType(tree);

    if (type != null) {
      String methodType = typeToString(type.getReturnType());
      if (classNames.containsKey(methodType)) {
        return standardMessage(tree, methodType, classNames.get(methodType));
      }

      for (String packName : packageNames.keySet()) {
        if (methodType.startsWith(packName)) {
          return standardMessage(tree, packName, packageNames.get(packName));
        }
      }
    }
    return Description.NO_MATCH;
  }

  /** A re-implementation of Files.readString() since it's not available until JDK 11. */
  private String readFileAsString(Path path) throws IOException {
    byte[] ba = Files.readAllBytes(path);
    return new String(ba, StandardCharsets.UTF_8);
  }

  private String readResourceAsString(String resource) throws IOException {
    return Resources.toString(Resources.getResource(resource), StandardCharsets.UTF_8);
  }
}

