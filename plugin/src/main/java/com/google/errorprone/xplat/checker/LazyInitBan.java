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
    // TODO(lukhnos): Write more detailed explanation and summary, describe why @LazyInit is needed
    summary = "Lazy Init is an error prone pattern on iOS. Please do it by the book.",
    explanation =
        "TBD",
    severity = ERROR)
public class LazyInitBan extends BugChecker implements MethodTreeMatcher {

  private static final Matcher<MethodTree> METHODS_EXCEPT_CONSTURCTOR_MATCHER =
      Matchers.allOf(
          Matchers.not(Matchers.methodIsConstructor())
      );

  /**
   * Used by IfTreeNonSync as a return value to get needed information across.
   */
  private enum IfTreeReturn {
    NO_MATCH, MATCH, WRONG_ASSIGNMENT_ORDER
  }

  /*
   * Holds needed information for error messages when WRONG_ASSIGNMENT_ORDER is found.
   */
  private static class ExpressionBox {

    public ExpressionTree tree;
    public Name firstVar;
    public Name secondVar;

    public void editBox(ExpressionTree tree, Name firstVar, Name secondVar) {
      this.tree = tree;
      this.firstVar = firstVar;
      this.secondVar = secondVar;
    }
  }

  /**
   * Checks the given field to see if it is "correct" for the lazy init pattern.
   *
   * @param field      symbol of field
   * @param methodSync whether the method is synchronized
   * @return true if the check passes
   */
  private boolean checkFieldSymbolSync(Symbol field, boolean methodSync) {
    boolean annotation = field.getAnnotationsByType(LazyInit.class).length > 0;
    boolean is_field = field.getKind() == ElementKind.FIELD;
    boolean is_final = field.getModifiers().contains(Modifier.FINAL);
    boolean is_vol = field.getModifiers().contains(Modifier.VOLATILE);
    return is_field && (((is_vol || annotation) && methodSync) || is_final);
  }

  /**
   * Checks if the given IfTree matches one of the invalid patterns.
   *
   * @param ifTree      if statement tree
   * @param foundIdents the set of found identifiers
   * @param methodSync  whether the method is synchronized
   * @return whether a pattern matches or not
   */
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

    boolean found_assignment = false;

    if (tree.getKind() == Kind.EXPRESSION_STATEMENT) {
      ExpressionTree exp_stmt = ((ExpressionStatementTree) tree).getExpression();

      if (exp_stmt.getKind() != Kind.ASSIGNMENT) {
        return false;
      }
      ExpressionTree var = ((AssignmentTree) exp_stmt).getVariable();

      if (var.getKind() != Kind.IDENTIFIER) {
        return false;
      }

      if (((IdentifierTree) var).getName().equals(((IdentifierTree) left).getName())) {
        found_assignment = true;
        foundIdents.add(((IdentifierTree) var).getName());
      }
    } else if (tree.getKind() == Kind.BLOCK) {

      for (StatementTree stmt : ((BlockTree) tree).getStatements()) {
        if (stmt.getKind() != Kind.EXPRESSION_STATEMENT) {
          continue;
        }
        ExpressionTree exp_stmt = ((ExpressionStatementTree) stmt).getExpression();

        if (exp_stmt.getKind() != Kind.ASSIGNMENT) {
          continue;
        }
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
    } else {
      return false;
    }

    return found_assignment;
  }

  /**
   * Checks the given field to see if it is "correct" for the lazy init pattern.
   *
   * @param field symbol of field
   * @return true if field passes the checks
   */
  private boolean checkFieldSymbolNonSync(Symbol field) {
    boolean annotation = field.getAnnotationsByType(LazyInit.class).length > 0;
    boolean is_field = field.getKind() == ElementKind.FIELD;
    boolean is_final = field.getModifiers().contains(Modifier.FINAL);
    boolean is_vol = field.getModifiers().contains(Modifier.VOLATILE);
    return is_field && ((is_vol || annotation) || is_final);
  }

  /**
   * Checks if the given IfTree matches one of the invalid patterns, and returns an IfTreeReturn
   * based on the pattern found.
   *
   * @param ifTree              if statement tree
   * @param foundIdents         the set of found identifiers
   * @param nonSyncIdent        the field being lazy inited
   * @param wrongAssignmentTree holds the assignment statement needed to create a proper error
   * @return a IfTreeReturn that represents the state of the IfTree for further processing
   */
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

    boolean foundAssignment = false;

    if (tree.getKind() == Kind.EXPRESSION_STATEMENT) {
      ExpressionTree exp_stmt = ((ExpressionStatementTree) tree).getExpression();

      if (exp_stmt.getKind() != Kind.ASSIGNMENT) {
        return IfTreeReturn.NO_MATCH;
      }
      ExpressionTree var = ((AssignmentTree) exp_stmt).getVariable();

      if (var.getKind() != Kind.IDENTIFIER) {
        return IfTreeReturn.NO_MATCH;
      }

      ExpressionTree nested = ((AssignmentTree) exp_stmt).getExpression();

      if (nested.getKind() != Kind.ASSIGNMENT) {
        return IfTreeReturn.NO_MATCH;
      }

      ExpressionTree nestedVar = ((AssignmentTree) ((AssignmentTree) exp_stmt).getExpression())
          .getVariable();

      if (nestedVar.getKind() != Kind.IDENTIFIER) {
        return IfTreeReturn.NO_MATCH;
      }

      // if the assignments are in the wrong order
      if (!(((IdentifierTree) var).getName().equals(nonSyncIdent.getSimpleName()) && foundIdents
          .contains(((IdentifierTree) nestedVar).getName()))) {
        wrongAssignmentTree.editBox(var, ((IdentifierTree) var).getName(),
            ((IdentifierTree) nestedVar).getName());
        return IfTreeReturn.WRONG_ASSIGNMENT_ORDER;
      }
      foundAssignment = true;

    } else if (tree.getKind() == Kind.BLOCK) {

      for (StatementTree stmt : ((BlockTree) tree).getStatements()) {

        if (stmt.getKind() != Kind.EXPRESSION_STATEMENT) {
          continue;
        }

        ExpressionTree exp_stmt = ((ExpressionStatementTree) stmt).getExpression();

        if (exp_stmt.getKind() != Kind.ASSIGNMENT) {
          continue;
        }
        ExpressionTree var = ((AssignmentTree) exp_stmt).getVariable();

        if (var.getKind() != Kind.IDENTIFIER) {
          continue;
        }

        ExpressionTree nested = ((AssignmentTree) exp_stmt).getExpression();

        if (nested.getKind() != Kind.ASSIGNMENT) {
          continue;
        }

        ExpressionTree nestedVar = ((AssignmentTree) ((AssignmentTree) exp_stmt)
            .getExpression())
            .getVariable();

        if (nestedVar.getKind() != Kind.IDENTIFIER) {
          continue;
        }

        // if the assignments are in the wrong order
        if (!(((IdentifierTree) var).getName().equals(nonSyncIdent.getSimpleName())
            && foundIdents
            .contains(((IdentifierTree) nestedVar).getName()))) {
          wrongAssignmentTree.editBox(var, ((IdentifierTree) var).getName(),
              ((IdentifierTree) nestedVar).getName());
          return IfTreeReturn.WRONG_ASSIGNMENT_ORDER;
        }
        foundAssignment = true;
      }
    } else {
      return IfTreeReturn.NO_MATCH;
    }

    // result should be true if an assignment was found and the field is not configured correctly
    boolean result = foundAssignment && !checkFieldSymbolNonSync(nonSyncIdent);

    if (result) {
      return IfTreeReturn.MATCH;
    } else {
      return IfTreeReturn.NO_MATCH;
    }
  }

  /**
   * Main loop to iterate though the statements in a block and check for matched lazy init patters.
   *
   * @param statements  list of statements inside a block
   * @param foundIdents set of found identifiers
   * @param methodSync  whether the method is synchronized
   * @param tree        matched method tree
   * @return Description that describes the error or non error found
   */
  private Description statementLoop(List<? extends StatementTree> statements, Set<Name> foundIdents,
      boolean methodSync, MethodTree tree, VisitorState state) {

    //true if method is nonsync variety
    boolean nonSync = false;
    Symbol nonSyncIdent = null;

    //true if return needs to be checked for the local var instead of the field
    boolean checkReturn = false;

    //true if the assignment order is wrong
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
            checkReturn = true;
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
          state.reportMatch(buildDescription(wrongAssignmentTree.tree)
              // TODO(lukhnos): Write more detailed explanation
              .setMessage(String.format("An error prone lazy init pattern has been detected."
                      + " Please swap the order of %s and %s in this assignment.",
                  wrongAssignmentTree.firstVar, wrongAssignmentTree.secondVar))
              .build());
        }

        if (checkReturn && !((IdentifierTree) returnTree).getName()
            .equals(nonSyncIdent.getSimpleName())) {
          return Description.NO_MATCH;
        } else if (checkReturn) {
          return buildDescription(returnTree)
              // TODO(lukhnos): Write more detailed explanation
              .setMessage("An error prone lazy init pattern has been detected."
                  + " Please return the local variable instead of the field.")
              .build();
        }

        return buildDescription(tree)
            // TODO(lukhnos): describe why @LazyInit is needed
            .setMessage(String.format("An error prone lazy init pattern has been detected."
                    + " Please use @LazyInit on the field %s.",
                nonSync ? nonSyncIdent : ((IdentifierTree) returnTree).getName()))
            .build();
      }
    }
    return Description.NO_MATCH;
  }

  @Override
  public Description matchMethod(MethodTree tree, VisitorState state) {

    if (METHODS_EXCEPT_CONSTURCTOR_MATCHER.matches(tree, state)) {

      Set<Name> foundIdents = new HashSet<>();

      boolean methodSync = Matchers.hasModifier(Modifier.SYNCHRONIZED).matches(tree, state);

      return statementLoop(tree.getBody().getStatements(), foundIdents, methodSync, tree, state);
    }

    return Description.NO_MATCH;
  }

}
