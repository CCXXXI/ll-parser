import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvFileSource
import java.io.LineNumberReader
import java.io.StringReader
import kotlin.test.assertEquals

class LLParserTest {
    @ParameterizedTest
    @CsvFileSource(resources = ["data.csv"])
    fun test(input: String, expected: String) {
        val parser = Java_LLParserAnalysis.LLParser(LineNumberReader(StringReader(input)))
        assertEquals(expected.trim(), parser.parse().toString().trim())
    }
}
