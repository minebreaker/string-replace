package rip.deadcode.javac.stringreplace;

import java.lang.annotation.*;


/**
 * The value of the field annotated by this annotation will be replaced
 * by the annotation argument passed to javac.
 */
@Retention( RetentionPolicy.CLASS )
@Target( ElementType.FIELD )
@Documented
public @interface Replace {
    public String value();
}
