package pl.piotrjanczukowicz.json.jackson;

import org.jspecify.annotations.Nullable;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.DeserializationContext;

/** Checks on the token a parser is positioned on. */
public final class JsonTokens {

    private JsonTokens() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Requires the parser's token to be {@code expected}.
     *
     * @param parser parser the token comes from, which locates the failure
     * @param actual token the parser is on, or {@code null} at the end of input
     * @param expected token the caller needs
     * @param message exception text
     * @throws DatabindException if {@code actual} is not {@code expected}
     */
    public static void requireToken(
            final JsonParser parser, final @Nullable JsonToken actual, final JsonToken expected, final String message) {
        if (actual != expected) {
            throw DatabindException.from(parser, message);
        }
    }

    /**
     * Binds the object the parser is on to {@code type}, unless it is empty, without buffering it.
     *
     * @param parser parser positioned on the value; it ends after the value
     * @param context context used for the read
     * @param type target class
     * @param <T> result type
     * @return bound value, or {@code null} when the value is an object with no properties
     */
    public static <T> @Nullable T readUnlessEmptyObject(
            final JsonParser parser, final DeserializationContext context, final Class<T> type) {
        if (parser.currentToken() == JsonToken.START_OBJECT && parser.nextToken() == JsonToken.END_OBJECT) {
            return null;
        }
        return context.readValue(parser, type);
    }
}
