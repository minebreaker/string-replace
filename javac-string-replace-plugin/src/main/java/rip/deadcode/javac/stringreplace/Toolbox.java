package rip.deadcode.javac.stringreplace;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;

import java.util.ResourceBundle;


/**
 * Dependency holder class.
 */
public final class Toolbox {

    private static final class Holder {
        private static final Toolbox singleton = new Toolbox();
    }

    private Toolbox() {
        // Amazon Corretto 11.0.2_9 fails this, which seems to be the bug
        set( ResourceBundle.class, ResourceBundle.getBundle( "messages" ) );
    }

    public static Toolbox getInstance() {
        return Holder.singleton;
    }

    private final ClassToInstanceMap<Object> map = MutableClassToInstanceMap.create();

    public <T> T get( Class<T> cls ) {
        return map.getInstance( cls );
    }

    public <T> void set( Class<T> cls, T instance ) {
        map.putInstance( cls, instance );
    }
}
