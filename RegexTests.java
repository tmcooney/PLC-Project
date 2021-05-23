package plc.homework;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Contains JUnit tests for {@link Regex}. Test structure for steps 1 & 2 are
 * provided, you must create this yourself for step 3.
 *
 * To run tests, either click the run icon on the left margin, which can be used
 * to run all tests or only a specific test. You should make sure your tests are
 * run through IntelliJ (File > Settings > Build, Execution, Deployment > Build
 * Tools > Gradle > Run tests using <em>IntelliJ IDEA</em>). This ensures the
 * name and inputs for the tests are displayed correctly in the run window.
 */
public class RegexTests {

    /**
     * This is a parameterized test for the {@link Regex#EMAIL} regex. The
     * {@link ParameterizedTest} annotation defines this method as a
     * parameterized test, and {@link MethodSource} tells JUnit to look for the
     * static method {@link #testEmailRegex()}.
     *
     * For personal preference, I include a test name as the first parameter
     * which describes what that test should be testing - this is visible in
     * IntelliJ when running the tests (see above note if not working).
     */
    @ParameterizedTest
    @MethodSource
    public void testEmailRegex(String test, String input, boolean success) {
        test(input, Regex.EMAIL, success);
    }

    /**
     * This is the factory method providing test cases for the parameterized
     * test above - note that it is static, takes no arguments, and has the same
     * name as the test. The {@link Arguments} object contains the arguments for
     * each test to be passed to the function above.
     */
    public static Stream<Arguments> testEmailRegex() {
        return Stream.of(
                Arguments.of("Alphanumeric", "thelegend27@gmail.com", true),
                Arguments.of("UF Domain", "otherdomain@ufl.edu", true),
                Arguments.of("Dot in name", "lt.dan@gmail.com", true),
                Arguments.of("Underscore In Name", "big_cheese@gmail.com", true),
                Arguments.of("Uppercase Letters", "BigDaddy@gmail.com", true),

                Arguments.of("Missing Domain Dot", "missingdot@gmailcom", false),
                Arguments.of("No Username", "@gmail.com", false),
                Arguments.of("No 'At' Symbol", "usernamegmail.com", false),
                Arguments.of("Symbols", "symbols#$%@gmail.com", false),
                Arguments.of("Invalid Domain", "username@gmail.regex", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testEvenStringsRegex(String test, String input, boolean success) {
        test(input, Regex.EVEN_STRINGS, success);
    }

    public static Stream<Arguments> testEvenStringsRegex() {
        return Stream.of(
                //what has ten letters and starts with gas?
                Arguments.of("10 Characters", "automobile", true),
                Arguments.of("12 Characters", "Hello World!", true),
                Arguments.of("14 Characters", "i<3pancakes10!", true),
                Arguments.of("16 Characters", "Florida Panthers", true),
                Arguments.of("20 Characters", "This ain't it chief.", true),
                Arguments.of("12 Tabs", "\t\t\t\t\t\t\t\t\t\t\t\t", true),
                Arguments.of("12 Spaces", "            ", true),

                Arguments.of("Empty String", "", false),
                Arguments.of("Tab", "\t", false),
                Arguments.of("6 Characters", "6chars", false),
                Arguments.of("9 Characters", "Go Gators", false),
                Arguments.of("11 Characters", "Scooby Doo!", false),
                Arguments.of("13 Characters", "i<3pancakes9!", false),
                Arguments.of("21 Characters", "University of Florida", false),
                Arguments.of("22 Characters", "University of Florida.", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testIntegerListRegex(String test, String input, boolean success) {
        test(input, Regex.INTEGER_LIST, success);
    }

    public static Stream<Arguments> testIntegerListRegex() {
        return Stream.of(
                Arguments.of("Single Element", "[1]", true),

                Arguments.of("Multiple Elements", "[1,2,3]", true),
                Arguments.of("With Spaces", "[1, 2, 3]", true),
                Arguments.of("Empty List", "[]", true),
                Arguments.of("Mixed Spaces", "[1,2, 3]",true ),

                Arguments.of("Missing Brackets", "1,2,3", false),
                Arguments.of("Empty String", "", false),
                Arguments.of("Floats", "[1.1,2.2,3.3]", false),
                Arguments.of("Missing Commas", "[1 2 3]", false),
                Arguments.of("Trailing Comma", "[1,2,3,]", false),
                Arguments.of("Trailing Comma", "[1,]", false),
                Arguments.of("Negative Integer", "[-1,2,3]", false),
                Arguments.of("Unnecessary Comma", "[1,]", false),
                Arguments.of("Too Many Spaces", "[1,  2, 3]", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testNumberRegex(String test, String input, boolean success) {
        test(input, Regex.NUMBER, success);
        //throw new UnsupportedOperationException(); //TODO
    }

    public static Stream<Arguments> testNumberRegex() {

        return Stream.of(
                Arguments.of("Single Element", "1", true),
                Arguments.of("Floating Decimal", "123.456", true),
                Arguments.of("Negative Integer", "-100", true),
                Arguments.of("Positive Float", "+99.99", true),
                Arguments.of("Preceding Zeros", "0000.000001", true),
                Arguments.of("Trailing Zeros", "0000.10000", true),

                Arguments.of("Preceding Decimal", ".1", false),
                Arguments.of("Empty String", "", false),
                Arguments.of("Not a Number", "hello there :)", false),
                Arguments.of("Alphanumeric", "100.784b9", false),
                Arguments.of("Extra Decimal", "1.1.12", false),
                Arguments.of("Preceding Decimal", ".1", false),
                Arguments.of("Trailing Decimal", "1.", false)

        );
        //throw new UnsupportedOperationException(); //TODO
    }

    @ParameterizedTest
    @MethodSource
    public void testStringRegex(String test, String input, boolean success) {
        test(input, Regex.STRING, success);
        //throw new UnsupportedOperationException(); //TODO
    }

    public static Stream<Arguments> testStringRegex() {
        System.out.print("");
        return Stream.of(
                Arguments.of("Just Quotes", "\"\"", true),
                Arguments.of("Just Digits", "\"678678\"", true),
                Arguments.of("Hello, World!", "\"Hello, World!\"", true),
                Arguments.of("1\t2", "\"1\\t2\"", true),
                Arguments.of("Single Tab", "\"\t\"", true),
                Arguments.of("Escaped Things","\"\b\n\r\t\'\"\\\\\"", true),
                Arguments.of("Alphanumeric", "\"4u2Pn2!?\"", true),

                Arguments.of("No Closing Comma", "\"unterminated", false),
                Arguments.of("No Opening Comma", "uninitiated\"", false),
                Arguments.of("No Comma", "unterminated", false),
                Arguments.of("Empty String", "", false),
                Arguments.of("invalid/escape", "\"invalid\\escape\"", false)

        );
        //throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Asserts that the input matches the given pattern. This method doesn't do
     * much now, but you will see this concept in future assignments.
     */
    private static void test(String input, Pattern pattern, boolean success) {
        Assertions.assertEquals(success, pattern.matcher(input).matches());
    }

}
