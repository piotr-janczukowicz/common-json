package pl.piotrjanczukowicz.json.jackson;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.junit.jupiter.api.Test;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DatabindException;

class JsonTokensTest {

    private static final JsonParser PARSER = mock(JsonParser.class);

    @Test
    void requireToken_shouldReturn_whenTokenMatches() {
        final Throwable thrown = catchThrowable(() ->
                JsonTokens.requireToken(PARSER, JsonToken.VALUE_STRING, JsonToken.VALUE_STRING, "must be a string"));

        assertThat(thrown).isNull();
    }

    @Test
    void requireToken_shouldThrow_whenTokenDiffers() {
        final Throwable thrown = catchThrowable(() -> JsonTokens.requireToken(
                PARSER, JsonToken.VALUE_NUMBER_INT, JsonToken.VALUE_STRING, "must be a string"));

        assertThat(thrown).isInstanceOf(DatabindException.class).hasMessageStartingWith("must be a string");
    }

    @Test
    void requireToken_shouldThrow_whenTokenIsNull() {
        final Throwable thrown =
                catchThrowable(() -> JsonTokens.requireToken(PARSER, null, JsonToken.END_ARRAY, "must end the array"));

        assertThat(thrown).isInstanceOf(DatabindException.class).hasMessageStartingWith("must end the array");
    }

    @Test
    void constructor_shouldRejectInstantiation() throws Exception {
        final Constructor<JsonTokens> constructor = JsonTokens.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        final Throwable thrown = catchThrowable(constructor::newInstance);

        assertThat(thrown)
                .isInstanceOf(InvocationTargetException.class)
                .hasCauseInstanceOf(UnsupportedOperationException.class);
    }
}
