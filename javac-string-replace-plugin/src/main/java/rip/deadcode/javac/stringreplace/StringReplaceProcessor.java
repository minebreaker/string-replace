package rip.deadcode.javac.stringreplace;

import com.google.common.collect.ImmutableMap;
import com.sun.source.util.Trees;
import com.sun.tools.javac.model.JavacTypes;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.HashSet;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import static com.google.common.base.Preconditions.checkState;


public final class StringReplaceProcessor extends AbstractProcessor {

    @Override public boolean process( Set<? extends TypeElement> annotations, RoundEnvironment roundEnv ) {

        if ( annotations.isEmpty() ) {
            return false;
        }

        // Should only have @Replace
        assert annotations.size() == 1;
        TypeElement annotationElement = annotations.iterator().next();

        ResourceBundle resourceBundle = Toolbox.getInstance().get( ResourceBundle.class );
        MessagerHelper message = new MessagerHelper( processingEnv.getMessager(), resourceBundle );

        String rawOptions = processingEnv.getOptions().get( PROPERTY_KEY );
        if ( rawOptions == null ) {
            message.error( "1" );
            return false;
        }

        Map<String, String> options = getOptions( rawOptions );

        JavacProcessingEnvironment p = (JavacProcessingEnvironment) processingEnv;
        JavacTypes types = p.getTypeUtils();
        Trees trees = Trees.instance( p );
        Context context = p.getContext();
        TreeMaker treeMaker = TreeMaker.instance( context );

        for ( Element fieldElement : roundEnv.getElementsAnnotatedWith( Replace.class ) ) {
            VariableElement fieldElement2 = (VariableElement) fieldElement;

            JCTree.JCVariableDecl field = (JCTree.JCVariableDecl) trees.getPath( fieldElement2 ).getLeaf();

            //noinspection OptionalGetWithoutIsPresent  // Should have @Replace
            JCTree.JCAnnotation replaceAnnotation = field.getModifiers().getAnnotations().stream()
                                                         .filter( e -> types.isSameType( e.type, annotationElement.asType() ) )
                                                         .findAny().get();
            JCTree.JCAssign arg = (JCTree.JCAssign) replaceAnnotation.getArguments().get( 0 );
            String replaceKey = ( (JCTree.JCLiteral) arg.getExpression() ).value.toString().toLowerCase();
            String replacingValue = options.get( replaceKey );
            if ( replacingValue == null ) {
                message.error( "2", replaceKey );
                return false;
            }

            // TODO type check
            field.init = treeMaker.at( field.init.pos ).Literal( replacingValue );
        }

        return true;
    }

    @Override public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override public Set<String> getSupportedAnnotationTypes() {
        Set<String> s = new HashSet<>();
        s.add( Replace.class.getCanonicalName() );
        return s;
    }

    private static final String PROPERTY_KEY = "rip.deadcode.javac.stringreplace.properties";

    @Override public Set<String> getSupportedOptions() {
        Set<String> s = new HashSet<>();
        s.add( PROPERTY_KEY );
        return s;
    }

    private static Map<String, String> getOptions( String raw ) {

        String[] pairs = raw.split( "," );

        ImmutableMap.Builder<String, String> m = ImmutableMap.builder();

        for ( String pair : pairs ) {
            int commaPos = pair.indexOf( '=' );
            checkState( commaPos >= 0 );

            String key = pair.substring( 0, commaPos ).toLowerCase();
            String value = pair.substring( commaPos + 1 );
            m.put( key, value );
        }

        return m.build();
    }
}
