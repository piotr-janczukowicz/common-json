package pl.piotrjanczukowicz.json;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

class TestCodecTest {

    private static final JsonCodec CODEC = TestCodec.defaults();

    @Test
    void encode_shouldThrow_whenValueCannotBeWritten() {
        final Throwable thrown = catchThrowable(() -> CODEC.encode(new Unwritable()));

        assertThat(thrown).isInstanceOf(JsonEncodeException.class).hasMessageContaining(Unwritable.class.getName());
    }

    @Test
    void decode_shouldThrow_whenJsonIsMalformed() {
        final Throwable thrown = catchThrowable(() -> CODEC.decode("{".getBytes(StandardCharsets.UTF_8), Object.class));

        assertThat(thrown).isInstanceOf(JsonDecodeException.class);
    }

    @Test
    void decode_shouldThrow_whenJsonTypeMismatches() {
        final JsonType<List<String>> type = JsonTypes.of(List.class, String.class);

        final Throwable thrown = catchThrowable(() -> CODEC.decode("{}".getBytes(StandardCharsets.UTF_8), type));

        assertThat(thrown).isInstanceOf(JsonDecodeException.class);
    }

    @Test
    void decode_shouldThrow_whenBodyIsNull() {
        final Throwable thrown =
                catchThrowable(() -> CODEC.decode("null".getBytes(StandardCharsets.UTF_8), Object.class));

        assertThat(thrown).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void decode_shouldThrow_whenGenericBodyIsNull() {
        final JsonType<List<String>> type = JsonTypes.of(List.class, String.class);

        final Throwable thrown = catchThrowable(() -> CODEC.decode("null".getBytes(StandardCharsets.UTF_8), type));

        assertThat(thrown).isInstanceOf(IllegalStateException.class);
    }

    public static final class Unwritable {

        public String getBoom() {
            throw new IllegalStateException("no value");
        }
    }
}
