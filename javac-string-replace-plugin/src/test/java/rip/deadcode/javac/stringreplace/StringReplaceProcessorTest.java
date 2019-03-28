package rip.deadcode.javac.stringreplace;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.stream.Stream;

import static com.google.common.truth.Truth.assertThat;
import static rip.deadcode.izvestia.Core.test;
import static rip.deadcode.izvestia.Core.testCase;


class StringReplaceProcessorTest {

    @TestFactory
    Stream<DynamicTest> testCompile() {


        return test( "Tests for StringReplaceProcessor" ).parameterized(

                testCase( "Test1", "KEY_FIELD_STR", "FIELD_STR", "VALUE_STR" )

        ).run( ( name, key, fieldName, expected ) -> {

            Class<?> cls = Tester.compile( name, ImmutableMap.of( key, expected ) );

            assertThat( cls.getField( fieldName ).get( null ) ).isEqualTo( expected );
        } );
    }
}
