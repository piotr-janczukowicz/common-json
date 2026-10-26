# common-json

Shared JSON utilities for Java callers: a Jackson-free `JsonCodec` contract, an `OptionalNull` wrapper for PATCH-style payloads, and Jackson 3 adapters for union-shaped JSON.

## Dependency

Java library. Add Maven coordinates `io.github.piotr-janczukowicz:common-json:1`.

```xml
<dependency>
  <groupId>io.github.piotr-janczukowicz</groupId>
  <artifactId>common-json</artifactId>
  <version>1</version>
</dependency>
```

## Usage

The `io.github.piotr_janczukowicz.json` package has no Jackson types: `JsonCodec` (decode and encode bytes), `JsonType` and `JsonTypes` (generic target types), `OptionalNull`, and the `JsonDecodeException` / `JsonEncodeException` failures. Implement `JsonCodec` over your own mapper; the contract is in its Javadoc. The adapters live in `io.github.piotr_janczukowicz.json.jackson` and target Jackson 3 (`tools.jackson`). All of them register on a `SimpleModule`.

### OptionalNull

`OptionalNull<T>` tells apart an absent field (Java `null`), an explicit JSON `null` (`OptionalNull.ofNull()`) and a value (`OptionalNull.of(value)`). Register both parts, and put `@JsonInclude(NON_NULL)` on the enclosing record so an absent field is omitted:

```java
SimpleModule module = new SimpleModule();
module.addDeserializer(OptionalNull.class, new OptionalNullDeserializer());
module.setMixInAnnotation(OptionalNull.class, OptionalNullMixin.class);
```

A value that the inner deserializer reads as `null` (for example an unknown enum constant under `READ_UNKNOWN_ENUM_VALUES_AS_NULL`) fails instead of becoming `OptionalNull.ofNull()`, so a PATCH cannot clear a field by accident.

### Union adapters

| Adapter | Use it for |
| --- | --- |
| `StringOrObjectAdapters` | a value that is either a string or an object |
| `ArrayOrWrappedAdapters` | a string array that is either bare or wrapped in one named property |
| `PolymorphicObjectAdapters` | an object whose type is picked by the first present, non-null property |
| `ScalarUnionReaders` | building a value from a string, number, boolean, string array or `null` inside your own deserializer |

```java
StringOrObjectAdapters.register(
        module,
        Label.class,
        Label::name,
        Label::details,
        Label::ofName,
        Label::ofDetails,
        Label.Details.class);
```

Every `register` method rejects a `null` argument when `register` is called. A union value that holds neither of its forms fails to serialize instead of being written as `null`.

### Immutable collections

`ImmutableCollectionsModule` makes `List`, `Collection`, `Set` and `Map` deserialize as immutable collections, keeping element types. JSON `null` and absent properties stay `null`. Null elements and map entries with a null value are dropped, and the iteration order of sets and maps is unspecified.

```java
JsonMapper mapper = JsonMapper.builder().addModule(new ImmutableCollectionsModule()).build();
```

### Mapper expectations

Do not enable default typing on a mapper that reads untrusted JSON. Keep Jackson's `StreamReadConstraints` at their defaults or tighter: they bound the size of numbers, and `ScalarUnionReaders` additionally rejects a number with an exponent beyond 1000 decimal places.

## License

MIT License
