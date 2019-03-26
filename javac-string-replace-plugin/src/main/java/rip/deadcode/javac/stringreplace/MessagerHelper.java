package rip.deadcode.javac.stringreplace;

import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;
import java.util.ResourceBundle;


public final class MessagerHelper {

    private final Messager messager;
    private final ResourceBundle resourceBundle;

    public MessagerHelper( Messager messager, ResourceBundle resourceBundle ) {
        this.messager = messager;
        this.resourceBundle = resourceBundle;
    }

    public final void error( String key, String... args ) {
        String template = resourceBundle.getString( "rip.deadcode.javac.stringreplace." + key );
        messager.printMessage(
                Diagnostic.Kind.ERROR, String.format( template, (Object[]) args ) );
    }
}
