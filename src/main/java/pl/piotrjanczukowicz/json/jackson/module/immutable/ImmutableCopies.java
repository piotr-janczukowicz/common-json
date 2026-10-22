package pl.piotrjanczukowicz.json.jackson.module.immutable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Single-pass immutable copies of Jackson-built collections that drop null elements and null map values. */
final class ImmutableCopies {

    private ImmutableCopies() {
        throw new UnsupportedOperationException("Utility class");
    }

    static <E> List<E> list(final Collection<? extends E> source) {
        if (source.isEmpty()) {
            return List.of();
        }
        if (hasNull(source)) {
            return List.copyOf(withoutNulls(new ArrayList<>(source.size()), source));
        }
        return List.copyOf(source);
    }

    static <E> Set<E> set(final Collection<? extends E> source) {
        if (source.isEmpty()) {
            return Set.of();
        }
        if (hasNull(source)) {
            return Set.copyOf(withoutNulls(HashSet.newHashSet(source.size()), source));
        }
        return Set.copyOf(source);
    }

    @SuppressWarnings("unchecked")
    static <K, V> Map<K, V> map(final Map<? extends K, ? extends V> source) {
        if (source.isEmpty()) {
            return Map.of();
        }
        if (hasNull(source.values())) {
            final List<Map.Entry<K, V>> kept = new ArrayList<>(source.size());
            source.forEach((key, value) -> {
                if (value != null) {
                    kept.add(Map.entry(key, value));
                }
            });
            return Map.ofEntries(kept.toArray(new Map.Entry[0]));
        }
        return Map.copyOf(source);
    }

    private static boolean hasNull(final Collection<?> source) {
        for (final Object element : source) {
            if (element == null) {
                return true;
            }
        }
        return false;
    }

    private static <E, C extends Collection<E>> C withoutNulls(final C target, final Collection<? extends E> source) {
        for (final E element : source) {
            if (element != null) {
                target.add(element);
            }
        }
        return target;
    }
}
