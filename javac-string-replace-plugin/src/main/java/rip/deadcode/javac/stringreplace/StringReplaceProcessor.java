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
import javax.lang.model.type.TypeKind;
import java.util.HashSet;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import static com.google.common.base.Preconditions.checkState;
import static rip.deadcode.javac.stringreplace.ReplacerException.check;


public final class StringReplaceProcessor extends AbstractProcessor {

    @Override public boolean process( Set<? extends TypeElement> annotations, RoundEnvironment roundEnv ) {

        if ( annotations.isEmpty() ) {
            return false;
        }

        ResourceBundle resourceBundle = Toolbox.getInstance().get( ResourceBundle.class );
        MessagerHelper message = new MessagerHelper( processingEnv.getMessager(), resourceBundle );

        try {

            // Should only have @Replace
            assert annotations.size() == 1;
            TypeElement annotationElement = annotations.iterator().next();

            String rawOptions = processingEnv.getOptions().get( PROPERTY_KEY );
            check( rawOptions != null, "1" );

            Map<String, String> options = getOptions( rawOptions );

            JavacProcessingEnvironment p = (JavacProcessingEnvironment) processingEnv;
            JavacTypes types = p.getTypeUtils();
            Trees trees = Trees.instance( p );
            Context context = p.getContext();
            TreeMaker treeMaker = TreeMaker.instance( context );

            for ( Element fieldElement : roundEnv.getElementsAnnotatedWith( Replace.class ) ) {

                // Get the field node annotated by @Replace
                JCTree.JCVariableDecl field = (JCTree.JCVariableDecl) trees.getPath( fieldElement ).getLeaf();

                // Look for the @Replace node
                //noinspection OptionalGetWithoutIsPresent  // Should have @Replace
                JCTree.JCAnnotation replaceAnnotation =
                        field.getModifiers().getAnnotations().stream()
                             .filter( e -> types.isSameType( e.type, annotationElement.asType() ) )
                             .findAny().get();
                // @Replace should have one and only one arg 'value'
                JCTree.JCAssign arg = (JCTree.JCAssign) replaceAnnotation.getArguments().get( 0 );
                // TODO Should be able to handle non-literal values
                check( arg.getExpression() instanceof JCTree.JCLiteral, "3" );

                String replaceKey = ( (JCTree.JCLiteral) arg.getExpression() ).value.toString().toLowerCase();
                String replacingValue = options.get( replaceKey );
                check( replacingValue != null, "2", replaceKey );

                field.init = treeMaker.at( field.init.pos ).Literal( convert( fieldElement, replacingValue ) );
            }

        } catch ( ReplacerException e ) {
            message.error( e.getMessageKey(), e.getArgs() );
            return false;
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

    private static Object convert( Element element, String value ) {

        TypeKind kind = element.asType().getKind();

        // TODO check if parsable
        if ( kind.equals( TypeKind.BOOLEAN ) ) {
            return Boolean.parseBoolean( value );

        } else if ( kind.equals( TypeKind.SHORT ) ) {
            return Short.parseShort( value );

        } else if ( kind.equals( TypeKind.INT ) ) {
            return Integer.parseInt( value );

        } else if ( kind.equals( TypeKind.LONG ) ) {
            return Long.parseLong( value );

        } else if ( kind.equals( TypeKind.CHAR ) ) {
            char[] chars = value.toCharArray();
            checkState( chars.length == 1 );  // TODO error message
            return chars[0];

        } else if ( kind.equals( TypeKind.FLOAT ) ) {
            return Float.parseFloat( value );

        } else if ( kind.equals( TypeKind.DOUBLE ) ) {
            return Double.parseDouble( value );

        } else if ( kind.equals( TypeKind.DECLARED ) ) {  // Class or interface
            checkState( element.asType().toString().equals( "java.lang.String" ) );  // TODO error message
            return value;

            // Should treat null as a special case?
        } else {
            throw new RuntimeException();
        }
    }
}
