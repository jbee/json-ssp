package se.jbee.json.stream;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonProperty {

  /*
  Mapping
   */

  String name() default "";

  boolean key() default false;

  /**
   * @return by default if a JSON object member is either not defined or defined JSON {@code null} a "empty value" is used. For example an empty list. If instead Java {@code null} should be used this can be set true.
   */
  boolean undefinedAsNull() default false; //TODO implement

  /*
  Validation
   */

  boolean required() default false;

  int minOccur() default 0;

  int maxOccur() default Integer.MAX_VALUE;
}
