package com.google.errorprone.xplat.checker;

import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;

import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.bugpatterns.BugChecker.NewClassTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.code.Type;
import java.util.List;

/**
 * Bans the usage of Joda-Time methods and constructors that have an Object parameter. If the
 * parameter that is passed in is a boxed long, it is permitted.
 */
@BugPattern(
    name = "JodaTimeObjectParamBan",
    summary = "Bans the usage of Joda-Time constructors that have an Object parameter.",
    explanation =
        "The usage of Joda-Time constructors that have an Object parameter are"
            + " banned from cross platform development due to the dangers of passing null as the"
            + " parameter. If the parameter is a boxed long, it is allowed to be used.",
    severity = ERROR)
public class JodaTimeObjectParamBan extends BugChecker implements NewClassTreeMatcher {

  @Override
  public Description matchNewClass(NewClassTree tree, VisitorState state) {
    Type constructorType = ASTHelpers.getType(tree);

    if (constructorType != null && constructorType.toString().startsWith("org.joda.time")) {

      MethodSymbol symbol = ASTHelpers.getSymbol(tree);

      if (symbol != null) {
        List<VarSymbol> argTypes = symbol.params();

        for (int i = 0; i < argTypes.size(); i++) {
          if (argTypes.get(i).asType().toString().equals("java.lang.Object")) {
            Type argType = ASTHelpers.getType(tree.getArguments().get(i));

            if (!state.getTypes().unboxedType(argType).toString().equals("long")) {
              return buildDescription(tree)
                  .setMessage(
                      String.format(
                          "%s is a banned constructor, as Joda-Time constructors that have"
                              + " java.lang.Object as a parameter are banned unless the parameter"
                              + " is a boxed long.", symbol.toString()))
                  .build();
            }
          }
        }

      }
    }

    return Description.NO_MATCH;
  }
}
