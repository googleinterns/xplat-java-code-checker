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
import com.sun.tools.javac.tree.JCTree;
import java.util.HashSet;
import java.util.Set;

@BugPattern(
    name = "UnnecessaryConcurrentHashMap",
    summary = "Suggests the use of synchronizedMap instead of ConcurrentHashMap",
    explanation =
        "ConcurrentHashMap is not well suited for use on Android devices. For this reason,"
            + " synchronizedMap is suggested to be used in its place for better compatibility.",
    severity = WARNING)
public class UnnecessaryConcurrentHashMap extends BugChecker implements NewClassTreeMatcher,
    VariableTreeMatcher {

  private static final Matcher<Tree> MATCHER =
      Matchers.isSameType("java.util.concurrent.ConcurrentHashMap");

  private static final Matcher<Tree> OTHER_MAP_INTERFACE_MATCHER =
      Matchers.allOf(
          Matchers.not(Matchers.isSameType("java.util.Map")),
          Matchers.not(Matchers.isSameType("java.util.concurrent.ConcurrentHashMap"))
      );

  private static final Set<NewClassTree> seenConstructors = new HashSet<>();

  @Override
  public Description matchNewClass(NewClassTree tree, VisitorState state) {
    if (MATCHER.matches(tree, state) && !seenConstructors.contains(tree)) {

      Tree variable = state.getPath().getParentPath().getLeaf();

      SuggestedFix.Builder fix = SuggestedFix.builder()
          .addImport("java.util.Collections")
          .replace(
              ((JCTree) tree).getStartPosition(),
              state.getEndPosition(tree),
              "Collections.synchronizedMap(new HashMap<>())");

      SuggestedFix.Builder fix2 = SuggestedFix.builder();

      if (variable != null && variable.getKind() == Kind.VARIABLE) {
        String source = state.getSourceForNode(variable);

        if (source != null && source.contains("<")) {
          fix.addImport("java.util.Map");
          fix.replace(
              ((JCTree) variable).getStartPosition(),
              ((JCTree) variable).getStartPosition() + source.indexOf("<"),
              "Map");
        }

      } else if (variable != null && variable.getKind() == Kind.ASSIGNMENT) {
        String originName = state.getSourceForNode(state.getPath().getParentPath().getLeaf())
            .substring(0,
                state.getSourceForNode(state.getPath().getParentPath().getLeaf()).indexOf("="))
            .trim();

        VariableTree origin =
            new TreePathScanner<VariableTree, Void>() {
              @Override
              public VariableTree reduce(VariableTree a, VariableTree b) {
                return a == null ? b : a;
              }

              @Override
              public VariableTree visitVariable(VariableTree node, Void aVoid) {
                if (node.getName().toString().equals(originName)) {
                  return node;
                }
                return super.visitVariable(node, aVoid);
              }
            }.scan(state.getPath().getParentPath().getParentPath().getParentPath(), null);

        if (origin != null && OTHER_MAP_INTERFACE_MATCHER.matches(origin, state)) {
          String originSource = state.getSourceForNode(origin);
          fix2.setShortDescription("Hello can you hear me?")
              .addImport("java.util.Map")
              .replace(
                  ((JCTree) origin).getStartPosition(),
                  ((JCTree) origin).getStartPosition() + originSource.indexOf("<"),
                  "Map");
        }
      }
      
      return buildDescription(tree)
          .setMessage("ConcurrentHashMap is not advised for cross platform use. Use"
              + " Collections.synchronizedMap instead.")
          .addFix(fix.build())
          .build();


    }
    return Description.NO_MATCH;
  }

  /**
   * This matcher only matches on variables that are declared with type ConcurrentHashMap and are
   * not assigned a value on the same line. An example would be {@code ConcurrentHashMap<String,
   * Integer> map;}
   */
  @Override
  public Description matchVariable(VariableTree tree, VisitorState state) {

    String source = state.getSourceForNode(tree);
    if (source != null && !source.contains("=") && MATCHER.matches(tree, state)) {

      return buildDescription(tree)
          .setMessage("ConcurrentHashMap is not advised for cross platform use. Use"
              + " Collections.synchronizedMap instead.")
          .addFix(
              SuggestedFix.builder()
                  .addImport("java.util.Map")
                  .replace(
                      ((JCTree) tree).getStartPosition(),
                      ((JCTree) tree).getStartPosition() + 14,
                      "").build())
          .build();
    }

    return Description.NO_MATCH;
  }
}
