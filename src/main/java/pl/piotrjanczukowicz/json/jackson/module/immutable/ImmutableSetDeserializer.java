package pl.piotrjanczukowicz.json.jackson.module.immutable;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

/**
 * Wraps Jackson's own set deserializer so element types are preserved, then copies the result into an immutable set.
 *
 * <p>Null elements are dropped. JSON {@code null} and absent properties are left to Jackson, so they stay {@code null};
 * an empty-value coercion becomes an empty immutable set.
 */
final class ImmutableSetDeserializer extends ImmutableCollectionDeserializer {

    private static final Set<Class<?>> REQUESTED_TYPES = Set.of(Set.class);

    ImmutableSetDeserializer(final ValueDeserializer<?> delegatee) {
        super(delegatee, REQUESTED_TYPES);
    }

    @Override
    public Object deserialize(final JsonParser parser, final DeserializationContext context) {
        final Collection<?> source = (Collection<?>) _delegatee.deserialize(parser, context);
        return source.stream().filter(Objects::nonNull).collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Object getEmptyValue(final DeserializationContext context) {
        return Set.of();
    }

    @Override
    protected ValueDeserializer<?> newDelegatingInstance(final ValueDeserializer<?> newDelegatee) {
        return new ImmutableSetDeserializer(newDelegatee);
    }
}
