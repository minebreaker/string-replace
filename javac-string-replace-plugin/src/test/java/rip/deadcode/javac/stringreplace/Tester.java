package rip.deadcode.javac.stringreplace;

import com.google.common.collect.ImmutableList;

import javax.tools.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;
import static java.util.stream.Collectors.joining;


public final class Tester {

    public static Class<?> compile( String name, Map<String, String> params ) {

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager standardFileManager = compiler.getStandardFileManager( null, null, null );

        ByteArrayOutputStream interruptingOutputStream = new ByteArrayOutputStream();

        JavaFileManager fileManager = new ForwardingJavaFileManager<StandardJavaFileManager>( standardFileManager ) {
            @Override public JavaFileObject getJavaFileForOutput(
                    Location location, String className, JavaFileObject.Kind kind, FileObject sibling ) throws IOException {

                JavaFileObject fileObject = super.getJavaFileForOutput( location, className, kind, sibling );
                return new ForwardingJavaFileObject<JavaFileObject>( fileObject ) {
                    @Override public OutputStream openOutputStream() {
                        return interruptingOutputStream;
                    }
                };
            }
        };

        //FIXME current working directory
        URL javFileUri = Tester.class.getResource( "/it/Test1.java" );
        List<File> files;
        try {
            files = ImmutableList.of( new File( javFileUri.toURI() ) );
        } catch ( URISyntaxException e ) {
            throw new RuntimeException( e );
        }
        Iterable<? extends JavaFileObject> compilationUnits = standardFileManager.getJavaFileObjectsFromFiles( files );

        String stingParam = serializeParams( params );
        List<String> optionList = ImmutableList.of( "-Arip.deadcode.javac.stringreplace.properties=" + stingParam );

        JavaCompiler.CompilationTask task = compiler.getTask( null, fileManager, null, optionList, null, compilationUnits );
        boolean result = task.call();

        assertThat( result ).isTrue();

        byte[] classFile = interruptingOutputStream.toByteArray();

        return TestClassLoader.load( "Test1", classFile );
    }

    private static String serializeParams( Map<String, String> params ) {
        return params.entrySet().stream()
                     .map( e -> e.getKey() + "=" + e.getValue() )
                     .collect( joining( "," ) );
    }
}
