package rip.deadcode.javac.stringreplace;


public final class TestClassLoader extends ClassLoader {

    private final String name;
    private final byte[] classfile;

    private TestClassLoader( String name, byte[] classfile ) {
        this.name = name;
        this.classfile = classfile;
    }

    @Override protected Class<?> findClass( String name ) {
        return defineClass( this.name, classfile, 0, classfile.length );
    }

    public static Class<?> load( String name, byte[] classfile ) {
        try {
            return new TestClassLoader( name, classfile ).loadClass( name );

        } catch ( ClassNotFoundException e ) {
            throw new RuntimeException( e );
        }
    }
}
