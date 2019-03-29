package rip.deadcode.javac.stringreplace;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;


class StringReplaceProcessorTest {

    @Test
    void testCompile() throws Exception {

        Class<?> cls = Tester.compile( "Test1", ImmutableMap.<String, String>builder()
                .put( "KEY_FIELD_STR", "VALUE_STR" )
                .build()
        );

        assertThat( cls.getField( "FIELD_STR" ).get( null ) ).isEqualTo( "VALUE_STR" );
    }
}
