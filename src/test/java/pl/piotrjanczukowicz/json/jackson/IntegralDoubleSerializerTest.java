package pl.piotrjanczukowicz.json.jackson;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import pl.piotrjanczukowicz.json.JsonCodec;
import pl.piotrjanczukowicz.json.TestCodec;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.json.JsonMapper;

class IntegralDoubleSerializerTest {

    private static final JsonCodec CODEC = TestCodec.defaults();

    private static final JsonMapper TREE = JsonMapper.builder().build();

    @Test
    void serialize_shouldWriteInteger_whenValueIsWhole() {
        final JsonNode encoded = TREE.readTree(CODEC.encode(commit(1.0)));

        assertThat(encoded.get("line").isIntegralNumber()).isTrue();
        assertThat(encoded.get("line").asLong()).isEqualTo(1L);
    }

    @Test
    void serialize_shouldKeepTheSign_whenValueIsNegativeZero() {
        final JsonNode encoded = TREE.readTree(CODEC.encode(commit(-0.0)));

        assertThat(encoded.get("line").isIntegralNumber()).isFalse();
        assertThat(encoded.get("line").asDouble()).isEqualTo(-0.0);
    }

    @Test
    void serialize_shouldWriteInteger_whenValueIsPositiveZero() {
        final JsonNode encoded = TREE.readTree(CODEC.encode(commit(0.0)));

        assertThat(encoded.get("line").isIntegralNumber()).isTrue();
        assertThat(encoded.get("line").asLong()).isZero();
    }

    @Test
    void serialize_shouldWriteDecimal_whenValueHasFraction() {
        final JsonNode encoded = TREE.readTree(CODEC.encode(commit(1.5)));

        assertThat(encoded.get("line").isIntegralNumber()).isFalse();
        assertThat(encoded.get("line").asDouble()).isEqualTo(1.5);
    }

    @Test
    void serialize_shouldWriteNonInteger_whenValueIsNaN() {
        final JsonNode encoded = TREE.readTree(CODEC.encode(commit(Double.NaN)));

        assertThat(encoded.get("line").isIntegralNumber()).isFalse();
    }

    @Test
    void serialize_shouldWriteNonInteger_whenValueIsInfinite() {
        final JsonNode encoded = TREE.readTree(CODEC.encode(commit(Double.POSITIVE_INFINITY)));

        assertThat(encoded.get("line").isIntegralNumber()).isFalse();
    }

    @Test
    void serialize_shouldWriteNonInteger_whenValueExceedsLongRange() {
        final JsonNode encoded = TREE.readTree(CODEC.encode(commit(1.0e20)));

        assertThat(encoded.get("line").isIntegralNumber()).isFalse();
    }

    @Test
    void serialize_shouldWriteNonInteger_whenValueIsBelowLongRange() {
        final JsonNode encoded = TREE.readTree(CODEC.encode(commit(-1.0e20)));

        assertThat(encoded.get("line").isIntegralNumber()).isFalse();
    }

    private static Location commit(final double line) {
        return new Location(line);
    }

    @Test
    void serialize_shouldWriteNonInteger_whenValueIsExactlyTwoPow63() {
        final JsonNode encoded = TREE.readTree(CODEC.encode(commit(0x1p63)));

        assertThat(encoded.get("line").isIntegralNumber()).isFalse();
        assertThat(encoded.get("line").asDouble()).isEqualTo(0x1p63);
    }

    @Test
    void serialize_shouldWriteInteger_whenValueIsLargestDoubleBelowTwoPow63() {
        final double largest = Math.nextDown(0x1p63);

        final JsonNode encoded = TREE.readTree(CODEC.encode(commit(largest)));

        assertThat(encoded.get("line").isIntegralNumber()).isTrue();
        assertThat(encoded.get("line").asLong()).isEqualTo((long) largest);
    }

    @Test
    void serialize_shouldWriteInteger_whenValueIsExactlyMinusTwoPow63() {
        final JsonNode encoded = TREE.readTree(CODEC.encode(commit(-0x1p63)));

        assertThat(encoded.get("line").isIntegralNumber()).isTrue();
        assertThat(encoded.get("line").asLong()).isEqualTo(Long.MIN_VALUE);
    }

    @Test
    void serialize_shouldWriteNonInteger_whenValueIsJustBelowMinusTwoPow63() {
        final JsonNode encoded = TREE.readTree(CODEC.encode(commit(Math.nextDown(-0x1p63))));

        assertThat(encoded.get("line").isIntegralNumber()).isFalse();
    }

    record Location(
            @JsonSerialize(using = IntegralDoubleSerializer.class)
            double line) {}
}
