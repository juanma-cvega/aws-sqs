package org.jusoft.aws.sqs.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method parameter to be mapped to the message attribute specified in its value. The only valid type to map
 * message attributes to is {@link String}.
 *
 * @author Juan Manuel Carnicero Vega
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface SqsAttribute {

  /**
   * The name of the attribute to map the parameter from
   */
  String value();
}
