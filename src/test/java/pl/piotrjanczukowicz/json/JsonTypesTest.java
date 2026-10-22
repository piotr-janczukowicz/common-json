package pl.piotrjanczukowicz.json;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class JsonTypesTest {

    private static final class Holder {
        @SuppressWarnings({"unused", "PMD.UnusedPrivateField"})
        private List<String> names;
    }

    @Test
    void of_shouldEqualSameClass_whenCalledTwice() {
        assertThat(JsonTypes.of(String.class)).isEqualTo(JsonTypes.of(String.class));
    }

    @Test
    void of_shouldBuildParameterizedType_whenRawAndArgumentProvided() {
        final JsonType<List<String>> type = JsonTypes.of(List.class, String.class);

        assertThat(type.toString()).contains("List").contains("String");
        assertThat(type).isNotEqualTo(JsonTypes.of(List.class));
    }

    @ParameterizedTest
    @ValueSource(classes = {String.class, Map.class})
    void of_shouldThrow_whenRawTypeDoesNotHaveExactlyOneTypeParameter(final Class<?> rawType) {
        final Throwable thrown = catchThrowable(() -> JsonTypes.of(rawType, Integer.class));

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("expected exactly 1");
    }

    @Test
    void listOf_shouldEqualJdkParameterizedType_whenSameRawAndArgument() throws Exception {
        final Type jdkType = Holder.class.getDeclaredField("names").getGenericType();
        final Type ours = JsonTypes.listOf(String.class).javaType();

        assertSoftly(softly -> {
            softly.assertThat(ours).isEqualTo(jdkType);
            softly.assertThat(jdkType).isEqualTo(ours);
            softly.assertThat(ours.hashCode()).isEqualTo(jdkType.hashCode());
        });
    }

    @Test
    void of_shouldThrow_whenRawTypeIsNull() {
        final Throwable thrown = catchThrowable(() -> JsonTypes.of(null, String.class));

        assertThat(thrown).isInstanceOf(NullPointerException.class).hasMessageContaining("rawType");
    }

    @Test
    void of_shouldThrow_whenTypeArgumentIsNull() {
        final Throwable thrown = catchThrowable(() -> JsonTypes.of(List.class, null));

        assertThat(thrown).isInstanceOf(NullPointerException.class).hasMessageContaining("typeArgument");
    }

    @Test
    void listOf_shouldThrow_whenElementTypeIsNull() {
        final Throwable thrown = catchThrowable(() -> JsonTypes.listOf(null));

        assertThat(thrown).isInstanceOf(NullPointerException.class).hasMessageContaining("elementType");
    }

    @Test
    void mapOf_shouldThrow_whenValueTypeIsNull() {
        final Throwable thrown = catchThrowable(() -> JsonTypes.mapOf(null));

        assertThat(thrown).isInstanceOf(NullPointerException.class).hasMessageContaining("valueType");
    }

    @Test
    void listOf_shouldEqualSameElementType_whenCalledTwice() {
        final JsonType<List<String>> first = JsonTypes.listOf(String.class);

        final JsonType<List<String>> second = JsonTypes.listOf(String.class);

        assertSoftly(softly -> {
            softly.assertThat(first).isEqualTo(second).hasSameHashCodeAs(second);
            softly.assertThat(first).isEqualTo(first).isNotEqualTo(null).isNotEqualTo("list");
            softly.assertThat(first).isNotEqualTo(JsonTypes.listOf(Integer.class));
            softly.assertThat(first.javaType()).isEqualTo(first.javaType()).isNotEqualTo(String.class);
            softly.assertThat(first.javaType())
                    .isNotEqualTo(JsonTypes.mapOf(String.class).javaType());
            softly.assertThat(((ParameterizedType) first.javaType()).getOwnerType())
                    .isNull();
            softly.assertThat(first.javaType()).isNotEqualTo(ownedListOfString());
        });
    }

    @Test
    void javaType_shouldPrintTypeName_whenParameterized() {
        final Type listType = JsonTypes.listOf(String.class).javaType();
        final Type mapType = JsonTypes.mapOf(Integer.class).javaType();

        assertSoftly(softly -> {
            softly.assertThat(listType).hasToString("java.util.List<java.lang.String>");
            softly.assertThat(mapType).hasToString("java.util.Map<java.lang.String, java.lang.Integer>");
        });
    }

    @Test
    void mapOf_shouldDescribeStringKeys_whenCalled() {
        final JsonType<Map<String, Integer>> type = JsonTypes.mapOf(Integer.class);

        assertThat(type.toString()).contains("Map").contains("String").contains("Integer");
    }

    @Test
    void of_shouldThrow_whenClassIsNull() {
        final Throwable thrown = catchThrowable(() -> JsonTypes.of(null));

        assertThat(thrown).isInstanceOf(NullPointerException.class).hasMessageContaining("type");
    }

    @Test
    void constructor_shouldRejectInstantiation_whenCalledViaReflection() throws Exception {
        final var constructor = JsonTypes.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        final Throwable thrown = catchThrowable(constructor::newInstance);

        assertThat(thrown).hasCauseInstanceOf(UnsupportedOperationException.class);
    }

    private static ParameterizedType ownedListOfString() {
        return new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return new Type[] {String.class};
            }

            @Override
            public Type getRawType() {
                return List.class;
            }

            @Override
            public Type getOwnerType() {
                return Map.class;
            }
        };
    }
}
