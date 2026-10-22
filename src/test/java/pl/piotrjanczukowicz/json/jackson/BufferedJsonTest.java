package pl.piotrjanczukowicz.json.jackson;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.List;
import org.junit.jupiter.api.Test;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.json.JsonMapper;

class BufferedJsonTest {

    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    @Test
    void isEmptyObject_shouldBeFalse_whenValueIsNotObject() {
        try (BufferedJson json = capture("[]")) {
            assertThat(json.isEmptyObject()).isFalse();
        }
    }

    @Test
    void hasField_shouldBeFalse_whenValueIsNotObject() {
        try (BufferedJson json = capture("null")) {
            assertThat(json.hasField("field")).isFalse();
        }
    }

    @Test
    void requiredText_shouldThrow_whenFieldMissing() {
        try (BufferedJson json = capture("{\"a\":\"x\"}")) {
            final Throwable thrown = catchThrowable(() -> json.requiredText("missing"));

            assertThat(thrown).isInstanceOf(DatabindException.class).hasMessageContaining("missing");
        }
    }

    @Test
    void requiredText_shouldSayWrongShape_whenFieldIsObjectOrArray() {
        try (BufferedJson object = capture("{\"a\":{}}");
                BufferedJson array = capture("{\"a\":[]}")) {
            final Throwable fromObject = catchThrowable(() -> object.requiredText("a"));
            final Throwable fromArray = catchThrowable(() -> array.requiredText("a"));

            assertThat(fromObject)
                    .isInstanceOf(DatabindException.class)
                    .hasMessageContaining("must be a string, a number or a boolean");
            assertThat(fromArray)
                    .isInstanceOf(DatabindException.class)
                    .hasMessageContaining("must be a string, a number or a boolean");
        }
    }

    @Test
    void requiredText_shouldSayMissing_whenFieldIsNull() {
        try (BufferedJson json = capture("{\"a\":null}")) {
            final Throwable thrown = catchThrowable(() -> json.requiredText("a"));

            assertThat(thrown).isInstanceOf(DatabindException.class).hasMessageContaining("Missing required field a");
        }
    }

    @Test
    void requiredLong_shouldThrow_whenFieldMissingOrNull() {
        try (BufferedJson missing = capture("{}");
                BufferedJson nil = capture("{\"n\":null}")) {
            final Throwable thrown = catchThrowable(() -> missing.requiredLong("n"));

            assertThat(thrown).isInstanceOf(DatabindException.class).hasMessageContaining("n");
            assertThat(catchThrowable(() -> nil.requiredLong("n")))
                    .isInstanceOf(DatabindException.class)
                    .hasMessageContaining("n");
        }
    }

    @Test
    void requiredLong_shouldThrow_whenValueIsNotAnInteger() {
        try (BufferedJson fraction = capture("{\"n\":1.9}");
                BufferedJson text = capture("{\"n\":\"12\"}")) {
            final Throwable fractionFailure = catchThrowable(() -> fraction.requiredLong("n"));
            final Throwable textFailure = catchThrowable(() -> text.requiredLong("n"));

            assertSoftly(softly -> {
                softly.assertThat(fractionFailure)
                        .isInstanceOf(DatabindException.class)
                        .hasMessageContaining("must be an integer");
                softly.assertThat(textFailure)
                        .isInstanceOf(DatabindException.class)
                        .hasMessageContaining("must be an integer");
            });
        }
    }

    @Test
    void requiredLong_shouldReturnValue_whenValueIsAnInteger() {
        try (BufferedJson json = capture("{\"n\":12}")) {
            assertThat(json.requiredLong("n")).isEqualTo(12L);
        }
    }

    @Test
    void requiredWire_shouldReturnConstant_whenValueKnown() {
        try (BufferedJson json = capture("{\"kind\":\"a\"}")) {
            final String converted = json.requiredWire("kind", String::toUpperCase);

            assertThat(converted).isEqualTo("A");
        }
    }

    @Test
    void requiredWire_shouldThrowDatabindException_whenConverterRejectsValue() {
        try (BufferedJson json = capture("{\"kind\":\"zzz\"}")) {
            final Throwable thrown = catchThrowable(() -> json.requiredWire("kind", value -> {
                throw new IllegalArgumentException("Unknown kind: " + value);
            }));

            assertThat(thrown)
                    .isInstanceOf(DatabindException.class)
                    .hasMessageStartingWith("Unknown kind: zzz")
                    .hasCauseInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    void requiredWire_shouldThrowDatabindException_whenFieldMissing() {
        try (BufferedJson json = capture("{}")) {
            final Throwable thrown = catchThrowable(() -> json.requiredWire("kind", String::toUpperCase));

            assertThat(thrown).isInstanceOf(DatabindException.class).hasMessageContaining("kind");
        }
    }

    @Test
    void mismatch_shouldReturnDatabindException_whenCalled() {
        try (BufferedJson json = capture("{}")) {
            assertThat(json.mismatch("unexpected shape"))
                    .isInstanceOf(DatabindException.class)
                    .hasMessageStartingWith("unexpected shape");
        }
    }

    @Test
    void readRequiredField_shouldThrow_whenFieldMissing() {
        try (BufferedJson json = capture("{}")) {
            final Throwable thrown = catchThrowable(() -> json.readRequiredField("field", String.class));

            assertThat(thrown).isInstanceOf(DatabindException.class).hasMessageContaining("field");
        }
    }

    @Test
    void hasNonNull_shouldBeFalse_whenFieldIsJsonNull() {
        try (BufferedJson json = capture("{\"field\":null}")) {
            assertThat(json.hasNonNull("field")).isFalse();
        }
    }

    @Test
    void readField_shouldReturnNull_whenFieldIsJsonNull() {
        try (BufferedJson json = capture("{\"field\":null}")) {
            assertThat(json.readField("field", String.class)).isNull();
        }
    }

    private static BufferedJson capture(final String json) {
        try (JsonParser parser = MAPPER.createParser(json)) {
            parser.nextToken();
            return BufferedJson.capture(parser, MAPPER._deserializationContext());
        }
    }

    @Test
    void readReplacingText_shouldReplaceTheField_whenItIsPresent() {
        try (BufferedJson json = capture("{\"kind\":\"old\",\"n\":1}")) {
            final Tagged tagged = json.readReplacingText("kind", "new", Tagged.class);

            assertThat(tagged).isEqualTo(new Tagged("new", 1));
        }
    }

    @Test
    void readReplacingText_shouldAddTheField_whenItIsAbsent() {
        try (BufferedJson json = capture("{\"n\":2}")) {
            final Tagged tagged = json.readReplacingText("kind", "added", Tagged.class);

            assertThat(tagged).isEqualTo(new Tagged("added", 2));
        }
    }

    private record Tagged(String kind, int n) {}

    @Test
    void isEmptyObject_shouldBeTrueOnlyForEmptyObject() {
        try (BufferedJson empty = capture("{}");
                BufferedJson filled = capture("{\"a\":1}")) {
            assertThat(empty.isEmptyObject()).isTrue();
            assertThat(filled.isEmptyObject()).isFalse();
        }
    }

    @Test
    void readField_shouldBindGenericType_whenFieldPresent() {
        final JavaType type = MAPPER.constructType(List.class);
        try (BufferedJson json = capture("{\"a\":[1,2]}")) {
            final Object value = json.readField("a", type);

            assertThat(value).isEqualTo(List.of(1, 2));
        }
    }

    @Test
    void readRequiredField_shouldReturnValue_whenFieldPresent() {
        try (BufferedJson json = capture("{\"a\":\"x\"}")) {
            final String value = json.readRequiredField("a", String.class);

            assertThat(value).isEqualTo("x");
        }
    }

    @Test
    void readReplacingText_shouldThrow_whenValueIsNotObject() {
        try (BufferedJson array = capture("[1]");
                BufferedJson scalar = capture("\"text\"")) {
            final Throwable fromArray = catchThrowable(() -> array.readReplacingText("a", "x", String.class));
            final Throwable fromScalar = catchThrowable(() -> scalar.readReplacingText("a", "x", String.class));

            assertThat(fromArray).isInstanceOf(DatabindException.class).hasMessageContaining("Expected an object");
            assertThat(fromScalar).isInstanceOf(DatabindException.class).hasMessageContaining("Expected an object");
        }
    }
}
