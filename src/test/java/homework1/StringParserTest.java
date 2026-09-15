package homework1;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

class StringParserTest {


    @ParameterizedTest(name = "Проверка на пробелы")
    @ValueSource(strings = {" ","   ","    "})
    public void test_for_space(String test){
        StringParser stringParser = new StringParser();
        assertThrows(IllegalArgumentException.class, () -> stringParser.parse(test));

    }
    @ParameterizedTest(name="Проверка на пустую строку и null")

    @EmptySource
    @NullSource
    public void test_empty_string(String test){
        StringParser stringParser = new StringParser();
        assertThrows(IllegalArgumentException.class,() -> stringParser.parse(test));
    }
    @ParameterizedTest(name="Проверка при позитивном условии строки.")
    @ValueSource(strings = {"Джава","Спринг","База данных","Менторство"})
    public void test_positive_string(String test){
        StringParser stringParser = new StringParser();
        org.assertj.core.api.Assertions.assertThatCode(() -> {
          stringParser.parse(test);
        }).doesNotThrowAnyException();

    }

}