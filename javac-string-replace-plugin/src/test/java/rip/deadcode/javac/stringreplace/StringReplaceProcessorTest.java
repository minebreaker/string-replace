package rip.deadcode.javac.stringreplace;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;


class StringReplaceProcessorTest {

    @Test
    void testCompile() throws Exception {

        //noinspection OptionalGetWithoutIsPresent
        Class<?> cls = Tester.compile( "Test1", ImmutableMap.<String, String>builder()
                .put( "KEY_FIELD_BOOLEAN", "true" )
                .put( "KEY_FIELD_BYTE", "1" )
                .put( "KEY_FIELD_SHORT", "2" )
                .put( "KEY_FIELD_INT", "3" )
                .put( "KEY_FIELD_LONG", "4" )
                .put( "KEY_FIELD_FLOAT", "1.2e+3" )
                .put( "KEY_FIELD_DOUBLE", "4.5e+6" )
                .put( "KEY_FIELD_CHAR", "a" )
                .put( "KEY_FIELD_STR", "VALUE_STR" )
                .build()
        ).get();

        assertThat( cls.getField( "FIELD_BOOLEAN" ).getBoolean( null ) ).isEqualTo( true );
        assertThat( cls.getField( "FIELD_BYTE" ).getByte( null ) ).isEqualTo( 1 );
        assertThat( cls.getField( "FIELD_SHORT" ).getShort( null ) ).isEqualTo( 2 );
        assertThat( cls.getField( "FIELD_INT" ).getInt( null ) ).isEqualTo( 3 );
        assertThat( cls.getField( "FIELD_LONG" ).getLong( null ) ).isEqualTo( 4 );
        assertThat( cls.getField( "FIELD_FLOAT" ).getFloat( null ) ).isEqualTo( 1.2e+3f );
        assertThat( cls.getField( "FIELD_DOUBLE" ).getDouble( null ) ).isEqualTo( 4.5e+6 );
        assertThat( cls.getField( "FIELD_CHAR" ).getChar( null ) ).isEqualTo( 'a' );
        assertThat( cls.getField( "FIELD_STR" ).get( null ) ).isEqualTo( "VALUE_STR" );
    }

    @Test
    void testRequireInitializer() throws Exception {

        Optional<Class<?>> resultTruthy = Tester.compile(
                "Test2",
                ImmutableMap.of( "KEY_LACKS_INITIALIZER", "value" )
        );
        assertThat( resultTruthy ).isEmpty();

        //noinspection OptionalGetWithoutIsPresent
        Class<?> resultFalse = Tester.compile(
                "Test2",
                ImmutableMap.of( "KEY_LACKS_INITIALIZER", "value" ),
                ImmutableList.of( "-Arip.deadcode.javac.stringreplace.requireInitializer=false" )
        ).get();
        assertThat( resultFalse.getField( "LACKS_INITIALIZER" ).get( null ) ).isEqualTo( "value" );
    }
}
