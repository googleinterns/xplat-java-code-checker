package com.google.errorprone.xplat.checker;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.LOCAL_VARIABLE;

import java.lang.annotation.Target;

/**
 * Annotation that indicates the explicit use of java.util.Calendar, in order to override the
 * CalendarClassBan xplat error prone checker.
 */
@Target({CONSTRUCTOR, METHOD, PARAMETER, FIELD, LOCAL_VARIABLE})
public @interface AllowLegacyTime {

}