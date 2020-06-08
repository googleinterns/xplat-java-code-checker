package com.google.errorprone.xplat.checker;

import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;

import com.google.common.collect.ImmutableSet;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.ImportTreeMatcher;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.bugpatterns.BugChecker.MethodTreeMatcher;
import com.google.errorprone.bugpatterns.BugChecker.NewClassTreeMatcher;
import com.google.errorprone.bugpatterns.BugChecker.VariableTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.util.ASTHelpers;
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

  private static final ImmutableSet<String> PACKAGE_NAMES =
      ImmutableSet.of(
          "org.joda.time.chrono", "org.joda.time.convert", "org.joda.time.field",
          "org.joda.time.format", "org.joda.time.tz"
      );

  private static final ImmutableSet<String> CLASS_NAMES =
      ImmutableSet.of(
          "org.joda.time.Chronology", "org.joda.time.DateMidnight",
          "org.joda.time.DateTimeComparator", "org.joda.time.DateTimeField",
          "org.joda.time.DateTimeFieldType", "org.joda.time.DateTimeUtils",
          "org.joda.time.Days", "org.joda.time.DurationField", "org.joda.time.DurationFieldType",
          "org.joda.time.Hours", "org.joda.time.IllegalFieldValueException",
          "org.joda.time.IllegalInstantException", "org.joda.time.JodaTimePermission",
          "org.joda.time.Minutes", "org.joda.time.MonthDay", "org.joda.time.Months",
          "org.joda.time.MutableDateTime", "org.joda.time.MutableInterval",
          "org.joda.time.MutablePeriod", "org.joda.time.Partial", "org.joda.time.Years",
          "org.joda.time.PeriodType", "org.joda.time.ReadWritableDateTime",
          "org.joda.time.ReadWritableInstant", "org.joda.time.ReadWritableInterval",
          "org.joda.time.ReadWritablePeriod", "org.joda.time.Seconds",
          "org.joda.time.TimeOfDay", "org.joda.time.Weeks", "org.joda.time.YearMonthDay"
      );

  public Description standardMessage(Tree tree, String target) {
    return buildDescription(tree)
        .setMessage(
            String.format("Use of %s has been banned due to cross"
                + " platform incompatibility.", target))
        .build();
  }

  public Description methodCallMessage(Tree tree, String method, String target) {
    return buildDescription(tree)
        .setMessage(
            String.format("Use of %s is not allowed, as %s has been banned due to cross"
                + " platform incompatibility.", method, target))
        .build();
  }

  public Description constructorMessage(Tree tree, String constructor, String target) {
    return buildDescription(tree)
        .setMessage(
            String.format(
                "Use of this constructor (%s) is not allowed, as %s"
                    + " is banned due to cross platform incompatibility.",
                constructor, target))
        .build();
  }

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {

    Symbol methodSymbol = ASTHelpers.getSymbol(tree);
    Type methodRecvType = ASTHelpers.getReceiverType(tree);
    Type methodType = ASTHelpers.getType(tree);

    if (methodType != null && methodSymbol != null) {

      // checks receiver for banned classes/packages
      if (CLASS_NAMES.contains(methodRecvType.toString())) {
        return standardMessage(tree, methodRecvType.toString());
      } else if (PACKAGE_NAMES.contains(methodSymbol.packge().toString())) {
        return standardMessage(tree, methodSymbol.packge().toString());
      }

      // checks caller for banned classes
      if (CLASS_NAMES.contains(methodType.toString())) {
        return methodCallMessage(tree, methodSymbol.toString(), methodType.toString());
      }
      // checks caller for banned packages
      for (String pack_name : PACKAGE_NAMES) {
        if (methodType.toString().startsWith(pack_name)) {
          return methodCallMessage(tree, methodSymbol.toString(), pack_name);
        }
      }
    }

    // checks arguments for banned classes/packages
    for (ExpressionTree arg : tree.getArguments()) {
      Symbol argSymbol = ASTHelpers.getSymbol(arg);
      Type argType = ASTHelpers.getType(arg);

      if (argSymbol != null && argType != null && methodSymbol != null) {
        if (CLASS_NAMES.contains(argType.toString())) {
          return standardMessage(tree, methodSymbol.toString());
        } else if (PACKAGE_NAMES.contains(argSymbol.packge().toString())) {
          return standardMessage(tree, methodSymbol.toString());
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
      if (CLASS_NAMES.contains(constructorType.toString())) {
        return standardMessage(tree, constructorType.toString());
      } else if (PACKAGE_NAMES.contains(constructorSymbol.packge().toString())) {
        return standardMessage(tree, constructorSymbol.packge().toString());
      }

      // checks parameters for banned classes/packages
      for (VarSymbol param : constructorSymbol.getParameters()) {
        if (CLASS_NAMES.contains(param.type.toString())) {
          return constructorMessage(tree, constructorSymbol.toString(), param.type.toString());
        } else if (PACKAGE_NAMES.contains(param.packge().toString())) {
          return constructorMessage(tree, constructorSymbol.toString(),
              param.packge().toString());
        }
      }
    }
    return Description.NO_MATCH;
  }

  @Override
  public Description matchImport(ImportTree tree, VisitorState state) {
    Symbol importSymbol = ASTHelpers.getSymbol(tree.getQualifiedIdentifier());

    if (CLASS_NAMES.contains(importSymbol.toString()) ||
        PACKAGE_NAMES.contains(importSymbol.packge().toString())) {
      return standardMessage(tree, importSymbol.toString());
    }

    return Description.NO_MATCH;
  }

  @Override
  public Description matchVariable(VariableTree tree, VisitorState state) {
    Type varType = ASTHelpers.getType(tree);

    if (varType != null) {
      if (CLASS_NAMES.contains(varType.toString())) {
        return standardMessage(tree, varType.toString());
      }

      for (String packName : PACKAGE_NAMES) {
        if (varType.toString().startsWith(packName)) {
          return standardMessage(tree, packName);
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
      if (CLASS_NAMES.contains(methodType.toString())) {
        return standardMessage(tree, methodType.toString());
      }

      for (String packName : PACKAGE_NAMES) {
        if (methodType.toString().startsWith(packName)) {
          return standardMessage(tree, packName);
        }
      }
    }
    return Description.NO_MATCH;
  }
}

