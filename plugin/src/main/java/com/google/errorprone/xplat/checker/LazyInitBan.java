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
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.annotations.concurrent.LazyInit;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.matchers.Matchers;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.tools.javac.code.Symbol;
import java.util.HashSet;
import java.util.Set;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;

@AutoService(BugChecker.class)
@BugPattern(
    name = "LazyInitBan",
    summary = "Lazy Init is an error prone pattern on mobile.",
    explanation =
        "TBD",
    severity = ERROR)
public class LazyInitBan extends BugChecker implements MethodTreeMatcher {

  private static final Matcher<MethodTree> METHOD_MATCHER =
      Matchers.allOf(
          Matchers.not(Matchers.methodIsConstructor()),
          Matchers.not(Matchers.hasModifier(Modifier.SYNCHRONIZED))
      );

  private boolean checkFieldSymbol(Symbol field) {
    boolean annotation = field.getAnnotationsByType(LazyInit.class).length == 0;
    boolean is_field = field.getKind() == ElementKind.FIELD;
    boolean not_final = !field.getModifiers().contains(Modifier.FINAL);
    boolean not_local = !field.isLocal();
    return annotation && is_field && not_final && not_local;
  }

  private boolean checkIfTree(IfTree ifTree, VisitorState state, Set<Name> foundIdents) {
    ExpressionTree exp = ifTree.getCondition();

    if (Matchers.inSynchronized().matches(exp, state)) {
      return false;
    }

    if (exp.getKind() != Kind.PARENTHESIZED) {
      return false;
    }

    exp = ((ParenthesizedTree) exp).getExpression();

    if (exp.getKind() != Kind.EQUAL_TO) {
      return false;
    }

    ExpressionTree left = ((BinaryTree) exp).getLeftOperand();

    ExpressionTree right = ((BinaryTree) exp).getRightOperand();

    if (right.getKind() != Kind.NULL_LITERAL || left.getKind() != Kind.IDENTIFIER) {
      return false;
    }

    if (ASTHelpers.getSymbol(left).isLocal()) {
      return false;
    }

    Symbol fieldSymbol = ASTHelpers.getSymbol(left).location().enclClass().members().findFirst(
        (com.sun.tools.javac.util.Name) ((IdentifierTree) left).getName());

    if (fieldSymbol == null || !checkFieldSymbol(fieldSymbol)) {
      return false;
    }

    StatementTree tree = ifTree.getThenStatement();

    if (tree.getKind() != Kind.BLOCK) {
      return false;
    }

    boolean found_assignment = false;

    for (StatementTree stmt : ((BlockTree) tree).getStatements()) {
      if (stmt.getKind() == Kind.EXPRESSION_STATEMENT) {
        ExpressionTree exp_stmt = ((ExpressionStatementTree) stmt).getExpression();

        if (exp_stmt.getKind() == Kind.ASSIGNMENT) {
          ExpressionTree var = ((AssignmentTree) exp_stmt).getVariable();

          if (var.getKind() != Kind.IDENTIFIER) {
            continue;
          }

          if (((IdentifierTree) var).getName().equals(((IdentifierTree) left).getName())) {
            found_assignment = true;
            foundIdents.add(((IdentifierTree) var).getName());
            break;
          }

        }
      }
    }

    return found_assignment;
  }

  @Override
  public Description matchMethod(MethodTree tree, VisitorState state) {

    if (METHOD_MATCHER.matches(tree, state)) {

      Set<Name> foundIdents = new HashSet<>();

      for (StatementTree stmt : tree.getBody().getStatements()) {
        if (stmt.getKind() == Kind.IF) {

          if (!checkIfTree((IfTree) stmt, state, foundIdents)) {
            return Description.NO_MATCH;
          }

        } else if (stmt.getKind() == Kind.RETURN) {
          ExpressionTree returnTree = ((ReturnTree) stmt).getExpression();

          if (returnTree.getKind() != Kind.IDENTIFIER) {
            return Description.NO_MATCH;
          }

          if (!foundIdents.contains(((IdentifierTree) returnTree).getName())) {
            return Description.NO_MATCH;
          }

          return buildDescription(tree)
              .setMessage(String.format("An error prone lazy init pattern has been detected."
                      + " Please use @LazyInit on the field %s. See go/why-lazyinit for more.",
                  ((IdentifierTree) returnTree).getName()))
              .build();
        }
      }

    }

    return Description.NO_MATCH;
  }

}
