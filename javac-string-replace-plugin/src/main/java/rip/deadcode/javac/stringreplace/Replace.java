package rip.deadcode.javac.stringreplace;

import java.lang.annotation.*;


@Retention( RetentionPolicy.CLASS )
@Target( ElementType.FIELD )
@Documented
public @interface Replace {
    public String value();
}
