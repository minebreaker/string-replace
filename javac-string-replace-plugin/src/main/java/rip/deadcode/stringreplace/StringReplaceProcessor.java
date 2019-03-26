package rip.deadcode.stringreplace;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


public final class StringReplaceProcessor extends AbstractProcessor {

    @Override public boolean process( Set<? extends TypeElement> annotations, RoundEnvironment roundEnv ) {

//        processingEnv.getMessager().printMessage( Diagnostic.Kind.NOTE, "Start processing" );

        annotations.forEach( annotation -> {
            System.out.println( annotation.getKind() );
            System.out.println( annotation.getQualifiedName() );


            roundEnv.getElementsAnnotatedWith( Replace.class ).forEach( f -> {
                VariableElement field = (VariableElement) f;
                System.out.println( field.getKind() );
                System.out.println( field.getSimpleName() );
                System.out.println( field.getConstantValue() );
                System.out.println( Arrays.toString( field.getClass().getInterfaces() ) );
            } );

            try {
                FileObject file = processingEnv.getFiler().getResource(
                        StandardLocation.CLASS_OUTPUT, "rip.deadcode.stringreplace.sample", "Main" );
                System.out.println(file.getName());
                System.out.println( file.toUri() );

                Path p = Paths.get( file.toUri()).getParent().resolve( "Main.class" );
                System.out.println( Files.exists( p ) );
                Files.createDirectories( p.getParent() );
                Files.write( p, "Fuck yes!".getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING );

            } catch ( IOException e ) {
                e.printStackTrace();
            }
        } );

        return true;
    }

    @Override public Set<String> getSupportedAnnotationTypes() {
        Set<String> s = new HashSet<>();
        s.add( Replace.class.getCanonicalName() );
        return s;
    }

    @Override public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}
