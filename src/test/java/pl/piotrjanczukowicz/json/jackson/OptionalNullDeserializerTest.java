package pl.piotrjanczukowicz.json.jackson;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonValue;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import pl.piotrjanczukowicz.json.JsonCodec;
import pl.piotrjanczukowicz.json.JsonDecodeException;
import pl.piotrjanczukowicz.json.JsonType;
import pl.piotrjanczukowicz.json.JsonTypes;
import pl.piotrjanczukowicz.json.OptionalNull;
import pl.piotrjanczukowicz.json.TestCodec;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.DeserializationContext;

class OptionalNullDeserializerTest {

    private static final JsonCodec CODEC = TestCodec.defaults();
    private static final JsonType<OptionalNull<String>> STRING_OPTIONAL_NULL =
            JsonTypes.of(OptionalNull.class, String.class);

    @Test
    void decode_shouldWrapValue_whenRootOptionalNullPresent() {
        final OptionalNull<String> decoded =
                CODEC.decode("\"octocat\"".getBytes(StandardCharsets.UTF_8), STRING_OPTIONAL_NULL);

        assertThat(decoded.value()).isEqualTo("octocat");
    }

    @Test
    void decode_shouldWrapJsonNull_whenRootOptionalNullNull() {
        final OptionalNull<String> decoded =
                CODEC.decode("null".getBytes(StandardCharsets.UTF_8), STRING_OPTIONAL_NULL);

        assertThat(decoded.value()).isNull();
    }

    @Test
    void decode_shouldWrapString_whenFieldPresentNullOrAbsent() {
        final StringPayload present =
                CODEC.decode("{\"field\":\"octocat\"}".getBytes(StandardCharsets.UTF_8), StringPayload.class);
        final StringPayload jsonNull =
                CODEC.decode("{\"field\":null}".getBytes(StandardCharsets.UTF_8), StringPayload.class);
        final StringPayload absent = CODEC.decode("{}".getBytes(StandardCharsets.UTF_8), StringPayload.class);

        assertThat(present.field()).isNotNull();
        assertThat(present.field().value()).isEqualTo("octocat");
        assertThat(jsonNull.field()).isNotNull();
        assertThat(jsonNull.field().value()).isNull();
        assertThat(absent.field()).isNull();
    }

    @Test
    void decode_shouldFail_whenWrappedEnumValueIsUnknown() {
        final Throwable thrown = catchThrowable(
                () -> CODEC.decode("{\"field\":\"not-a-color\"}".getBytes(StandardCharsets.UTF_8), EnumPayload.class));

        assertThat(thrown).isInstanceOf(JsonDecodeException.class).hasMessageContaining("OptionalNull");
    }

    @Test
    void decode_shouldWrapEnum_whenWrappedEnumValueIsKnown() {
        final EnumPayload decoded =
                CODEC.decode("{\"field\":\"red\"}".getBytes(StandardCharsets.UTF_8), EnumPayload.class);

        assertThat(decoded.field().value()).isEqualTo(Color.RED);
    }

    @Test
    void decode_shouldWrapList_whenFieldPresentNullOrAbsent() {
        final ListPayload present =
                CODEC.decode("{\"field\":[\"a\",\"b\"]}".getBytes(StandardCharsets.UTF_8), ListPayload.class);
        final ListPayload jsonNull =
                CODEC.decode("{\"field\":null}".getBytes(StandardCharsets.UTF_8), ListPayload.class);
        final ListPayload absent = CODEC.decode("{}".getBytes(StandardCharsets.UTF_8), ListPayload.class);

        assertThat(present.field()).isNotNull();
        assertThat(present.field().value()).containsExactly("a", "b");
        assertThat(jsonNull.field()).isNotNull();
        assertThat(jsonNull.field().value()).isNull();
        assertThat(absent.field()).isNull();
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    record StringPayload(@Nullable OptionalNull<String> field) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    record ListPayload(@Nullable OptionalNull<List<String>> field) {}

    private enum Color {
        RED;

        @JsonValue
        String wire() {
            return "red";
        }

        @JsonCreator
        static Color fromWire(final String value) {
            if ("red".equals(value)) {
                return RED;
            }
            throw new IllegalArgumentException("unknown color " + value);
        }
    }

    private record EnumPayload(@Nullable OptionalNull<Color> field) {}

    @Test
    void decode_shouldFail_whenWrappedValueReadsAsNull() {
        final Throwable thrown = catchThrowable(
                () -> CODEC.decode("{\"field\":\"gone\"}".getBytes(StandardCharsets.UTF_8), NullingPayload.class));

        assertThat(thrown).isInstanceOf(JsonDecodeException.class).hasMessageContaining("NullingPayload");
    }

    private enum Nulling {
        PRESENT;

        @JsonCreator
        static @Nullable Nulling fromWire(final String value) {
            return "present".equals(value) ? PRESENT : null;
        }
    }

    private record NullingPayload(@Nullable OptionalNull<Nulling> field) {}

    @Test
    void deserialize_shouldThrow_whenNotContextualized() {
        final OptionalNullDeserializer deserializer = new OptionalNullDeserializer();

        final Throwable thrown = catchThrowable(() -> deserializer.deserialize(null, null));

        assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessageContaining("contextualized");
    }

    @Test
    void createContextual_shouldThrow_whenNoTypeIsKnown() {
        final DeserializationContext context = mock(DeserializationContext.class);

        final Throwable thrown = catchThrowable(() -> new OptionalNullDeserializer().createContextual(context, null));

        assertThat(thrown).isInstanceOf(DatabindException.class);
    }
}
