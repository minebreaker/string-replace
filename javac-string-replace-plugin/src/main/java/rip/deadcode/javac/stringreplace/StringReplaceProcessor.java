package rip.deadcode.javac.stringreplace;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
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
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import static com.google.common.base.Preconditions.checkState;
import static rip.deadcode.javac.stringreplace.ReplacerException.check;


public final class StringReplaceProcessor extends AbstractProcessor {

    private static final String PROPERTY_KEY = "rip.deadcode.javac.stringreplace.properties";
    private static final String REQUIRE_INITIALIZER_KEY = "rip.deadcode.javac.stringreplace.requireInitializer";
    private static final String CHECK_STATIC_FINAL_KEY = "rip.deadcode.javac.stringreplace.checkStaticFinal";

    @Override public boolean process( Set<? extends TypeElement> annotations, RoundEnvironment roundEnv ) {

        if ( annotations.isEmpty() ) {
            return false;
        }

        ResourceBundle resourceBundle = Toolbox.getInstance().get( ResourceBundle.class );
        MessagerHelper message = new MessagerHelper( processingEnv.getMessager(), resourceBundle );

        JavacProcessingEnvironment p = (JavacProcessingEnvironment) processingEnv;
        JavacTypes types = p.getTypeUtils();
        Trees trees = Trees.instance( p );
        Context context = p.getContext();
        TreeMaker treeMaker = TreeMaker.instance( context );

        try {

            // Should only have @Replace
            assert annotations.size() == 1;
            TypeElement annotationElement = annotations.iterator().next();

            String rawProperties = processingEnv.getOptions().get( PROPERTY_KEY );
            check( rawProperties != null, "1" );
            Map<String, String> properties = getProperties( rawProperties );

            for ( Element fieldElement : roundEnv.getElementsAnnotatedWith( Replace.class ) ) {

                // Get the field node annotated by @Replace
                JCTree.JCVariableDecl field = (JCTree.JCVariableDecl) trees.getTree( fieldElement );

                String requireInitializerFlag = processingEnv.getOptions().get( REQUIRE_INITIALIZER_KEY );
                if ( requireInitializerFlag == null || !requireInitializerFlag.equalsIgnoreCase( "false" ) ) {
                    check( field.init != null, "4", field.getName().toString() );
                }
                String checkStaticFinalFlag = processingEnv.getOptions().get( CHECK_STATIC_FINAL_KEY );
                if ( checkStaticFinalFlag == null || !checkStaticFinalFlag.equalsIgnoreCase( "false" ) ) {
                    Set<Modifier> flags = field.getModifiers().getFlags();
                    check( flags.contains( Modifier.STATIC ) && flags.contains( Modifier.FINAL ), "5", field.getName().toString() );
                }

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
                String replacingValue = properties.get( replaceKey );
                check( replacingValue != null, "2", replaceKey );

                field.init = treeMaker.Literal( convert( fieldElement, replacingValue ) );
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
        return ImmutableSet.of( Replace.class.getCanonicalName() );
    }

    @Override public Set<String> getSupportedOptions() {
        return ImmutableSet.of( PROPERTY_KEY, REQUIRE_INITIALIZER_KEY, CHECK_STATIC_FINAL_KEY );
    }

    /**
     * Parse option string to {@code Map}.
     * Example: {@code key1=value1,key2=value2}
     *
     * @param raw String option
     * @return Parsed options
     */
    private static Map<String, String> getProperties( String raw ) {

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

    /**
     * Convert {@link String} value to the appropriate value for the field represented by the {@link Element}.
     *
     * TODO error message
     *
     * @param element Field element which the value should be converted to the type of
     * @param value The value to convert
     * @return Converted value
     */
    private static Object convert( Element element, String value ) {

        TypeKind kind = element.asType().getKind();

        // TODO check if parsable
        if ( kind.equals( TypeKind.BOOLEAN ) ) {
            return Boolean.parseBoolean( value );

            // TypeKind byte/short/char must be cast to int
            // This may be related to the JVM spec §4.7.2
            // https://docs.oracle.com/javase/specs/jvms/se12/html/jvms-4.html#jvms-4.7.2

        } else if ( kind.equals( TypeKind.BYTE ) ) {
            return (int) Byte.parseByte( value );

        } else if ( kind.equals( TypeKind.SHORT ) ) {
            return (int) Short.parseShort( value );

        } else if ( kind.equals( TypeKind.INT ) ) {
            return Integer.parseInt( value );

        } else if ( kind.equals( TypeKind.LONG ) ) {
            return Long.parseLong( value );

        } else if ( kind.equals( TypeKind.FLOAT ) ) {
            return Float.parseFloat( value );

        } else if ( kind.equals( TypeKind.DOUBLE ) ) {
            return Double.parseDouble( value );

        } else if ( kind.equals( TypeKind.CHAR ) ) {
            char[] chars = value.toCharArray();
            checkState( chars.length == 1 );
            return (int) chars[0];

        } else if ( kind.equals( TypeKind.DECLARED ) ) {  // Class or interface
            checkState( element.asType().toString().equals( "java.lang.String" ) );
            return value;

            // Should treat null as a special case?
        } else {
            throw new RuntimeException( "Unknown type: " + kind );
        }
    }
}
