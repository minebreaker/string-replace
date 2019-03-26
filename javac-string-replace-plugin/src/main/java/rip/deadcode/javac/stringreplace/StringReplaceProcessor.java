package rip.deadcode.javac.stringreplace;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreeScanner;
import com.sun.source.util.Trees;
import com.sun.tools.javac.model.JavacTypes;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;


public final class StringReplaceProcessor extends AbstractProcessor {

    @Override public boolean process( Set<? extends TypeElement> annotations, RoundEnvironment roundEnv ) {

        if ( annotations.isEmpty() ) {
            return false;
        }

        ResourceBundle resourceBundle = Toolbox.getInstance().get( ResourceBundle.class );

        if ( !processingEnv.getOptions().containsKey( PROPERTY_KEY ) ) {
            processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.ERROR, resourceBundle.getString( "rip.deadcode.javac.stringreplace.1" ) );
            return false;
        }

        JavacProcessingEnvironment p = (JavacProcessingEnvironment) processingEnv;
        JavacTypes types = p.getTypeUtils();
        Trees trees = Trees.instance( p );
        Context context = p.getContext();
        TreeMaker treeMaker = TreeMaker.instance( context );

        annotations.forEach( annotation -> {
            roundEnv.getRootElements().forEach( element -> {
                trees.getPath( element ).getCompilationUnit().accept( new TreeScanner<Object, Object>() {
                    @Override
                    public Object visitClass( ClassTree node, Object o ) {

                        node.getMembers().stream()
                            .filter( e -> e.getKind().equals( Tree.Kind.VARIABLE ) )
                            .map( e -> (JCTree.JCVariableDecl) e )
                            .filter( f -> f.getModifiers().getAnnotations().stream()
                                           .anyMatch( e -> types.isSameType( e.type, annotation.asType() ) ) )
                            .forEach( field -> {

                                JCTree.JCAnnotation replace = field.getModifiers().getAnnotations().stream()
                                                                   .filter( e -> types.isSameType( e.type, annotation.asType() ) )
                                                                   .findAny()
                                                                   .get();
                                JCTree.JCAssign arg = (JCTree.JCAssign) replace.getArguments().get( 0 );
                                JCTree.JCLiteral argValue = (JCTree.JCLiteral) arg.getExpression();
                                System.out.println( argValue.value );
                                System.out.println( argValue.value.equals( "VERSION" ) );

                                roundEnv.getElementsAnnotatedWith( Replace.class ).forEach( e -> {
                                    VariableElement e2 = (VariableElement) e;
                                    System.out.println( e2.asType() );
                                    System.out.println( trees.getPath( e2 ).getLeaf() );
                                } );


                                field.init = treeMaker.at( field.init.pos ).Literal( "Ypa!!!" );
                            } );

                        return super.visitClass( node, o );
                    }
                }, null );
            } );
        } );

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
}
