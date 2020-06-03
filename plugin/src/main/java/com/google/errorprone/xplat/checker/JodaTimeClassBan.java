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
import com.sun.source.tree.VariableTree;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.code.Type;

/**
 * Check for usage of Joda-Time's {@code org.joda.time.base.* org.joda.time.chrono.*
 * org.joda.time.convert.*, org.joda.time.field.*, org.joda.time.format.*, org.joda.time.tz.*,
 * org.joda.time.Chronology, org.joda.time.DateMidnight, org.joda.time.DateTimeComparator,
 * org.joda.time.DateTimeField, org.joda.time.DateTimeFieldType, org.joda.time.DateTimeUtils,
 * org.joda.time.Days, org.joda.time.DurationField, org.joda.time.DurationFieldType,
 * org.joda.time.Hours, org.joda.time.IllegalFieldValueException, org.joda.time.IllegalInstantException,
 * org.joda.time.JodaTimePermission, org.joda.time.Minutes, org.joda.time.MonthDay,
 * org.joda.time.Months, org.joda.time.MutableDateTime, org.joda.time.MutableInterval,
 * org.joda.time.MutablePeriod, org.joda.time.Partial, org.joda.time.PeriodType,
 * org.joda.time.ReadWritableDateTime, org.joda.time.ReadWritableInstant,
 * org.joda.time.ReadWritableInterval, org.joda.time.ReadWritablePeriod org.joda.time.Seconds,
 * org.joda.time.TimeOfDay org.joda.time.Weeks org.joda.time.YearMonthDay org.joda.time.Years}.
 * These calls are banned due to their incompatibility with cross platform development.
 */
@BugPattern(
    name = "JodaTimeClassBan",
    summary = "Joda Time cross platform class ban",
    explanation =
        "The usage of several Joda Time classes are banned from cross platform development due"
            + " to incompatibilities. They are unsupported on the web and should also not be"
            + " used on supported platforms.",
    severity = ERROR)
public class JodaTimeClassBan extends BugChecker
    implements MethodInvocationTreeMatcher, NewClassTreeMatcher, ImportTreeMatcher,
    VariableTreeMatcher, MethodTreeMatcher {

  private static final ImmutableSet<String> PACKAGE_NAMES =
      ImmutableSet.of(
          "org.joda.time.base.", "org.joda.time.chrono", "org.joda.time.convert",
          "org.joda.time.field", "org.joda.time.format", "org.joda.time.tz"
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

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {

    Symbol methodSymbol = ASTHelpers.getSymbol(tree);
    Type methodRecvType = ASTHelpers.getReceiverType(tree);
    Type methodType = ASTHelpers.getType(tree);

    if (methodType != null && methodSymbol != null) {

      //checks receiver
      if (CLASS_NAMES.contains(methodRecvType.toString())) {
        return buildDescription(tree)
            .setMessage(
                String.format("Use of %s has been banned due to cross"
                    + " platform incompatibility.", methodRecvType))
            .build();
      } else if (PACKAGE_NAMES.contains(methodSymbol.packge().toString())) {
        return buildDescription(tree)
            .setMessage(
                String.format("Use of %s has been banned due to cross"
                    + " platform incompatibility.", methodSymbol.packge()))
            .build();
      }

      //checks caller
      if (CLASS_NAMES.contains(methodType.toString())) {
        return buildDescription(tree)
            .setMessage(
                String.format("Use of %s is not allowed, as %s has been banned due to cross"
                    + " platform incompatibility.", methodSymbol, methodType))
            .build();
      }

      for (String pack_name : PACKAGE_NAMES) {
        if (methodType.toString().startsWith(pack_name)) {
          return buildDescription(tree)
              .setMessage(
                  String.format("Use of %s is not allowed, as %s has been banned due to cross"
                      + " platform incompatibility.", methodSymbol, pack_name))
              .build();
        }
      }
    }

    //checks arguments
    for (ExpressionTree arg : tree.getArguments()) {
      Symbol argSymbol = ASTHelpers.getSymbol(arg);
      Type argType = ASTHelpers.getType(arg);

      if (argSymbol != null && argType != null) {
        if (CLASS_NAMES.contains(argType.toString())) {
          return buildDescription(tree)
              .setMessage(
                  String.format("Use of %s has been banned due to cross"
                          + " platform incompatibility.",
                      methodSymbol))
              .build();
        } else if (PACKAGE_NAMES.contains(argSymbol.packge().toString())) {
          return buildDescription(tree)
              .setMessage(
                  String.format("Use of %s has been banned due to cross"
                          + " platform incompatibility.",
                      methodSymbol))
              .build();
        }
      }
    }

    return Description.NO_MATCH;
  }

  @Override
  public Description matchNewClass(NewClassTree tree, VisitorState state) {

    MethodSymbol constructorSymbol = ASTHelpers.getSymbol(tree);
    Type constructorType = ASTHelpers.getType(tree);

    //checks constructor
    if (constructorSymbol != null && constructorType != null) {
      if (CLASS_NAMES.contains(constructorType.toString())) {
        return buildDescription(tree)
            .setMessage(
                String.format("Use of %s has been banned due to cross"
                        + " platform incompatibility.",
                    constructorType))
            .build();
      } else if (PACKAGE_NAMES.contains(constructorSymbol.packge().toString())) {
        return buildDescription(tree)
            .setMessage(
                String.format("Use of %s has been banned due to cross"
                        + " platform incompatibility.",
                    constructorSymbol.packge().toString()))
            .build();
      }
    }

    //checks parameters
    if (constructorSymbol != null) {
      for (VarSymbol param : constructorSymbol.getParameters()) {
        if (CLASS_NAMES.contains(param.type.toString())) {
          return buildDescription(tree)
              .setMessage(
                  String.format(
                      "Use of this constructor (%s) is not allowed, as %s"
                          + " is banned due to cross platform incompatibility.",
                      constructorSymbol, param.type.toString()))
              .build();
        } else if (PACKAGE_NAMES.contains(param.packge().toString())) {
          return buildDescription(tree)
              .setMessage(
                  String.format(
                      "Use of this constructor (%s) is not allowed, as %s"
                          + " is banned due to cross platform incompatibility.",
                      constructorSymbol, param.packge().toString()))
              .build();
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
      return buildDescription(tree)
          .setMessage(
              String.format("Use of %s has been banned due to cross"
                  + " platform incompatibility.", importSymbol.toString()))
          .build();
    }

    return Description.NO_MATCH;
  }

  @Override
  public Description matchVariable(VariableTree tree, VisitorState state) {
    Type varType = ASTHelpers.getType(tree);

    if (varType != null) {
      if (CLASS_NAMES.contains(varType.toString())) {
        return buildDescription(tree)
            .setMessage(
                String.format("Use of %s has been banned due to cross"
                    + " platform incompatibility.", varType))
            .build();
      }

      for (String packName : PACKAGE_NAMES) {
        if (varType.toString().startsWith(packName)) {
          return buildDescription(tree)
              .setMessage(
                  String.format("Use of %s has been banned due to cross"
                      + " platform incompatibility.", packName))
              .build();
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
        return buildDescription(tree)
            .setMessage(
                String.format("Use of %s has been banned due to cross"
                    + " platform incompatibility.", methodType))
            .build();
      }

      for (String packName : PACKAGE_NAMES) {
        if (methodType.toString().startsWith(packName)) {
          return buildDescription(tree)
              .setMessage(
                  String.format("Use of %s has been banned due to cross"
                      + " platform incompatibility.", packName))
              .build();
        }
      }
    }

    return Description.NO_MATCH;
  }
}

