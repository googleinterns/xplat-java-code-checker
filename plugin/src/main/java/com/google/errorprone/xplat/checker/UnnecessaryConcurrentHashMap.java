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

import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.NewClassTreeMatcher;
import com.google.errorprone.bugpatterns.BugChecker.VariableTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.matchers.Matchers;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePathScanner;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import java.util.Optional;

/**
 * Checks for usage of ConcurrentHashMap and suggests the use of Collections.synchronizedMap.
 * ConcurrentHashMap is not well supported on iOS.
 */
@BugPattern(
    name = "UnnecessaryConcurrentHashMap",
    summary = "Suggests the use of Collections.synchronizedMap instead of ConcurrentHashMap.",
    explanation =
        "ConcurrentHashMap is not well supported on iOS."
            + " For this reason, Collections.synchronizedMap is suggested to be used"
            + " in its place for better cross-platform compatibility.",
    severity = WARNING)
public class UnnecessaryConcurrentHashMap extends BugChecker implements NewClassTreeMatcher,
    VariableTreeMatcher {

  private static final Matcher<Tree> CONCURRENT_HASH_MAP_MATCHER =
      Matchers.isSameType("java.util.concurrent.ConcurrentHashMap");

  private static final Matcher<Tree> MAP_MATCHER =
      Matchers.isSameType("java.util.Map");

  private static final Matcher<Tree> OTHER_MAP_INTERFACE_MATCHER =
      Matchers.allOf(
          Matchers.not(Matchers.isSameType("java.util.Map")),
          Matchers.not(Matchers.isSameType("java.util.concurrent.ConcurrentHashMap"))
      );

  private static final String STANDARD_MESSAGE =
      "ConcurrentHashMap is not well supported on iOS. Use"
          + " Collections.synchronizedMap instead.";

  private Description standardDescription(Tree tree, SuggestedFix fix, String message) {
    return buildDescription(tree)
        .setMessage(message)
        .addFix(fix)
        .build();
  }


  @Override
  public Description matchNewClass(NewClassTree tree, VisitorState state) {
    if (CONCURRENT_HASH_MAP_MATCHER.matches(tree, state)) {

      SuggestedFix.Builder fix = SuggestedFix.builder()
          .addImport("java.util.Collections")
          .addImport("java.util.HashMap")
          .replace(
              ((JCTree) tree).getStartPosition(),
              state.getEndPosition(tree),
              "Collections.synchronizedMap(new HashMap<>())");

      Tree variable = state.getPath().getParentPath().getLeaf();

      if (variable == null) {
        return Description.NO_MATCH;
      } else if (variable.getKind() == Kind.VARIABLE) {
        String source = state.getSourceForNode(variable);

        if (source != null && source.contains("<") && !MAP_MATCHER.matches(variable, state)) {
          fix.addImport("java.util.Map");
          fix.replace(
              ((JCTree) variable).getStartPosition(),
              ((JCTree) variable).getStartPosition() + source.indexOf("<"),
              "Map");
        }

      } else if (variable.getKind() == Kind.ASSIGNMENT) {
        Optional<Description> desc = incompatibleInterfaceDesc(variable, state,
            state.getSourceForNode(variable));

        if (desc.isPresent()) {
          state.reportMatch(standardDescription(tree, fix.build(), STANDARD_MESSAGE));

          return desc.get();
        } else {
          return standardDescription(tree, fix.build(), STANDARD_MESSAGE +
              " Make sure that this variable is declared with the Map interface.");
        }
      }
      return standardDescription(tree, fix.build(), STANDARD_MESSAGE);
    }

    return Description.NO_MATCH;
  }

  /**
   * Handles the edge case where the ConcurrentHashMap is declared with an interface that is
   * incompatible with Collections.synchronizedMap on a different line than it is instantiated. This
   * only handles the case where both are in the same scope. Otherwise, it is on the user to verify
   * that the variable is declared with a valid interface.
   */
  private Optional<Description> incompatibleInterfaceDesc(Tree tree, VisitorState state,
      String source) {

    String treeSource = state.getSourceForNode(tree);

    if (treeSource == null || source == null || !source.contains("=")) {
      return Optional.empty();
    }

    String originName = source.substring(0, treeSource.indexOf("=")).trim();

    VariableTree origin =
        new TreePathScanner<VariableTree, Void>() {
          @Override
          public VariableTree reduce(VariableTree a, VariableTree b) {
            return a == null ? b : a;
          }

          @Override
          public VariableTree visitVariable(VariableTree node, Void unused) {
            if (node.getName().toString().equals(originName)) {
              return node;
            }
            return super.visitVariable(node, unused);
          }
        }.scan(state.getPath().getParentPath().getParentPath().getParentPath(), null);

    if (origin == null) {
      return Optional.empty();
    }

    String originSource = state.getSourceForNode(origin);

    if (originSource != null && OTHER_MAP_INTERFACE_MATCHER.matches(origin, state)) {

      return Optional.of(buildDescription(origin)
          .setMessage("This variable is declared with an interface that is not compatible"
              + " with Collections.synchronizedMap, which is suggested to be used"
              + " in the previous warning.")
          .addFix(SuggestedFix.builder()
              .addImport("java.util.Map")
              .replace(
                  ((JCTree) origin).getStartPosition(),
                  ((JCTree) origin).getStartPosition() + originSource.indexOf("<"),
                  "Map")
              .build())
          .build());
    }

    return Optional.empty();
  }

  /**
   * This matcher only matches on variables that are declared with type ConcurrentHashMap and are
   * not assigned a value on the same line. An example would be {@code ConcurrentHashMap<String,
   * Integer> map;}
   */
  @Override
  public Description matchVariable(VariableTree tree, VisitorState state) {

    String source = state.getSourceForNode(tree);

    if (source != null && !source.contains("=") &&
        CONCURRENT_HASH_MAP_MATCHER.matches(tree, state)) {

      return buildDescription(tree)
          .setMessage(STANDARD_MESSAGE)
          .addFix(
              SuggestedFix.builder()
                  .addImport("java.util.Map")
                  .replace(
                      ((JCTree) tree).getStartPosition(),
                      ((JCTree) tree).getStartPosition() + 14,
                      "")
                  .build())
          .build();
    }

    return Description.NO_MATCH;
  }

}
