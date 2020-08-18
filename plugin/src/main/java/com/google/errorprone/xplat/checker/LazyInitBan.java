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
import com.sun.source.tree.SynchronizedTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.tools.javac.code.Symbol;
import java.util.HashSet;
import java.util.List;
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
          Matchers.not(Matchers.methodIsConstructor())
      );

  private enum IfTreeReturn {
    NO_MATCH, MATCH, WRONG_ASSIGNMENT_ORDER
  }

  private static class ExpressionBox {

    public ExpressionTree tree;
    public Name firstVar;
    public Name secondVar;

    ExpressionBox() {

    }

    public void editBox(ExpressionTree tree, Name firstVar, Name secondVar) {
      this.tree = tree;
      this.firstVar = firstVar;
      this.secondVar = secondVar;
    }
  }

  private boolean checkFieldSymbolSync(Symbol field, boolean methodSync) {
    boolean annotation = field.getAnnotationsByType(LazyInit.class).length > 0;
    boolean is_field = field.getKind() == ElementKind.FIELD;
    boolean is_final = field.getModifiers().contains(Modifier.FINAL);
    boolean is_vol = field.getModifiers().contains(Modifier.VOLATILE);
    return is_field && (((is_vol || annotation) && methodSync) || is_final);
  }

  private boolean ifTreeSync(IfTree ifTree, Set<Name> foundIdents, boolean methodSync) {
    ExpressionTree exp = ifTree.getCondition();

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

    Symbol fieldSymbol = ASTHelpers.getSymbol(left).enclClass().members().findFirst(
        (com.sun.tools.javac.util.Name) ((IdentifierTree) left).getName());

    if (fieldSymbol == null || checkFieldSymbolSync(fieldSymbol, methodSync)) {
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

  private boolean checkFieldSymbolNonSync(Symbol field) {
    boolean annotation = field.getAnnotationsByType(LazyInit.class).length > 0;
    boolean is_field = field.getKind() == ElementKind.FIELD;
    boolean is_final = field.getModifiers().contains(Modifier.FINAL);
    boolean is_vol = field.getModifiers().contains(Modifier.VOLATILE);
    return is_field && ((is_vol || annotation) || is_final);
  }

  private IfTreeReturn ifTreeNonSync(IfTree ifTree, Set<Name> foundIdents, Symbol nonSyncIdent,
      ExpressionBox wrongAssignmentTree) {
    ExpressionTree exp = ifTree.getCondition();

    if (exp.getKind() != Kind.PARENTHESIZED) {
      return IfTreeReturn.NO_MATCH;
    }

    exp = ((ParenthesizedTree) exp).getExpression();

    if (exp.getKind() != Kind.EQUAL_TO) {
      return IfTreeReturn.NO_MATCH;
    }

    ExpressionTree left = ((BinaryTree) exp).getLeftOperand();

    ExpressionTree right = ((BinaryTree) exp).getRightOperand();

    if (right.getKind() != Kind.NULL_LITERAL || left.getKind() != Kind.IDENTIFIER) {
      return IfTreeReturn.NO_MATCH;
    }

    if (!foundIdents.contains(((IdentifierTree) left).getName())) {
      return IfTreeReturn.NO_MATCH;
    }

    StatementTree tree = ifTree.getThenStatement();

    if (tree.getKind() != Kind.BLOCK) {
      return IfTreeReturn.NO_MATCH;
    }

    boolean foundAssignment = false;

    for (StatementTree stmt : ((BlockTree) tree).getStatements()) {
      if (stmt.getKind() == Kind.EXPRESSION_STATEMENT) {
        ExpressionTree exp_stmt = ((ExpressionStatementTree) stmt).getExpression();

        if (exp_stmt.getKind() == Kind.ASSIGNMENT) {
          ExpressionTree var = ((AssignmentTree) exp_stmt).getVariable();

          if (var.getKind() != Kind.IDENTIFIER) {
            continue;
          }

          ExpressionTree nested = ((AssignmentTree) exp_stmt).getExpression();

          if (nested.getKind() != Kind.ASSIGNMENT) {
            continue;
          }

          ExpressionTree nestedVar = ((AssignmentTree) ((AssignmentTree) exp_stmt).getExpression())
              .getVariable();

          if (nestedVar.getKind() != Kind.IDENTIFIER) {
            continue;
          }

          if (!(((IdentifierTree) var).getName().equals(nonSyncIdent.getSimpleName()) && foundIdents
              .contains(((IdentifierTree) nestedVar).getName()))) {
            wrongAssignmentTree.editBox(var, ((IdentifierTree) var).getName(),
                ((IdentifierTree) nestedVar).getName());
            return IfTreeReturn.WRONG_ASSIGNMENT_ORDER;
          }
          foundAssignment = true;
        }
      }
    }

    boolean result = foundAssignment && !checkFieldSymbolNonSync(nonSyncIdent);

    if (result) {
      return IfTreeReturn.MATCH;
    } else {
      return IfTreeReturn.NO_MATCH;
    }
  }


  private Description statementLoop(List<? extends StatementTree> statements, Set<Name> foundIdents,
      boolean methodSync, MethodTree tree, VisitorState state) {

    boolean nonSync = false;
    Symbol nonSyncIdent = null;
    boolean checkReturn = false;
    boolean wrongAssignmentOrder = false;
    ExpressionBox wrongAssignmentTree = new ExpressionBox();

    for (StatementTree stmt : statements) {
      if (stmt.getKind() == Kind.VARIABLE) {

        VariableTree var = ((VariableTree) stmt);

        if (var.getInitializer().getKind() != Kind.IDENTIFIER) {
          return Description.NO_MATCH;
        }

        Name name = ((IdentifierTree) var.getInitializer()).getName();

        Symbol fieldSymbol = ASTHelpers.getSymbol(var).enclClass().members().findFirst(
            (com.sun.tools.javac.util.Name) name);

        if (fieldSymbol.isLocal()) {
          return Description.NO_MATCH;
        }

        foundIdents.add(name);
        foundIdents.add(var.getName());
        nonSyncIdent = fieldSymbol;
        nonSync = true;

      } else if (stmt.getKind() == Kind.IF) {

        if (nonSync) {
          IfTreeReturn result = ifTreeNonSync((IfTree) stmt, foundIdents, nonSyncIdent,
              wrongAssignmentTree);
          if (result == IfTreeReturn.NO_MATCH) {
            checkReturn = true;
          } else if (result == IfTreeReturn.WRONG_ASSIGNMENT_ORDER) {
            wrongAssignmentOrder = true;
          }

        } else {
          if (!ifTreeSync((IfTree) stmt, foundIdents, methodSync)) {
            return Description.NO_MATCH;
          }
        }

      } else if (stmt.getKind() == Kind.SYNCHRONIZED) {
        return statementLoop(((SynchronizedTree) stmt).getBlock().getStatements(), foundIdents,
            true, tree, state);

      } else if (stmt.getKind() == Kind.RETURN) {
        ExpressionTree returnTree = ((ReturnTree) stmt).getExpression();

        if (returnTree.getKind() != Kind.IDENTIFIER) {
          return Description.NO_MATCH;
        }

        if (!foundIdents.contains(((IdentifierTree) returnTree).getName())) {
          return Description.NO_MATCH;
        }

        if (wrongAssignmentOrder) {
          return buildDescription(wrongAssignmentTree.tree)
              .setMessage(String.format("An error prone lazy init pattern has been detected."
                      + " Please swap the order of %s and %s in this assignment.",
                  wrongAssignmentTree.firstVar, wrongAssignmentTree.secondVar))
              .build();
        }

        if (checkReturn && !((IdentifierTree) returnTree).getName()
            .equals(nonSyncIdent.getSimpleName())) {
          return Description.NO_MATCH;
        } else if (checkReturn) {
          return buildDescription(returnTree)
              .setMessage("An error prone lazy init pattern has been detected."
                  + " Please return the local variable instead of the field.")
              .build();
        }

        return buildDescription(tree)
            .setMessage(String.format("An error prone lazy init pattern has been detected."
                    + " Please use @LazyInit on the field %s. See go/why-lazyinit for more.",
                nonSync ? nonSyncIdent : ((IdentifierTree) returnTree).getName()))
            .build();
      }
    }
    return Description.NO_MATCH;
  }

  @Override
  public Description matchMethod(MethodTree tree, VisitorState state) {

    if (METHOD_MATCHER.matches(tree, state)) {

      Set<Name> foundIdents = new HashSet<>();

      boolean methodSync = Matchers.hasModifier(Modifier.SYNCHRONIZED).matches(tree, state);

      return statementLoop(tree.getBody().getStatements(), foundIdents, methodSync, tree, state);
    }

    return Description.NO_MATCH;
  }

}
