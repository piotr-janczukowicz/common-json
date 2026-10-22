package pl.piotrjanczukowicz.json.jackson;

import org.jspecify.annotations.Nullable;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DatabindException;

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
}
