package com.google.errorprone.xplat.checker;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.LOCAL_VARIABLE;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that indicates the explicit use of a legacy time class, in order to override the
 * LegacyTimeBan xplat error prone checker.
 */
@Target({CONSTRUCTOR, METHOD, PARAMETER, FIELD, LOCAL_VARIABLE})
@Retention(RetentionPolicy.SOURCE)
public @interface AllowLegacyTime {

  /**
   * Allows for this annotation to be self-documenting by adding a comment inside it. For example,
   * {@code @AllowLegacyTime("Legacy API Foo only accepts java.util.Calendar")}
   */
  String value() default "";
}