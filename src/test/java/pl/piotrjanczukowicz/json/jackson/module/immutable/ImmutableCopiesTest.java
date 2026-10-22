package pl.piotrjanczukowicz.json.jackson.module.immutable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ImmutableCopiesTest {

    @Test
    void shouldReturnEmptyCollections_whenSourceIsEmpty() {
        assertSoftly(softly -> {
            softly.assertThat(ImmutableCopies.list(new ArrayList<>())).isEmpty();
            softly.assertThat(ImmutableCopies.set(new ArrayList<>())).isEmpty();
            softly.assertThat(ImmutableCopies.map(new HashMap<>())).isEmpty();
        });
    }

    @Test
    void shouldCopyWithoutNulls_whenSourceHasNone() {
        final Map<String, Integer> map = new HashMap<>(Map.of("a", 1));

        assertSoftly(softly -> {
            softly.assertThat(ImmutableCopies.list(new ArrayList<>(List.of("a", "b"))))
                    .containsExactly("a", "b");
            softly.assertThat(ImmutableCopies.set(new ArrayList<>(List.of("a", "a"))))
                    .containsExactly("a");
            softly.assertThat(ImmutableCopies.map(map)).containsExactly(Map.entry("a", 1));
        });
    }

    @Test
    void shouldDropNulls() {
        final Map<String, Integer> map = new HashMap<>();
        map.put("a", 1);
        map.put("b", null);

        assertSoftly(softly -> {
            softly.assertThat(ImmutableCopies.list(Arrays.asList("a", null, "b")))
                    .containsExactly("a", "b");
            softly.assertThat(ImmutableCopies.set(Arrays.asList("a", null, "a")))
                    .isEqualTo(Set.of("a"));
            softly.assertThat(ImmutableCopies.map(map)).containsExactly(Map.entry("a", 1));
        });
    }

    @Test
    void shouldReturnUnmodifiableCopies() {
        final Throwable thrown = catchThrowable(
                () -> ImmutableCopies.list(Arrays.asList("a", null)).add("c"));

        assertThat(thrown).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void constructor_shouldRejectInstantiation() throws Exception {
        final Constructor<ImmutableCopies> constructor = ImmutableCopies.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        final Throwable thrown = catchThrowable(constructor::newInstance);

        assertThat(thrown)
                .isInstanceOf(InvocationTargetException.class)
                .hasCauseInstanceOf(UnsupportedOperationException.class);
    }
}
