module common.json {
    requires transitive tools.jackson.databind;
    requires common.util;
    requires static org.jspecify;
    requires static com.github.spotbugs.annotations;

    exports pl.piotrjanczukowicz.json;
    exports pl.piotrjanczukowicz.json.jackson;
    exports pl.piotrjanczukowicz.json.jackson.module.immutable;

    opens pl.piotrjanczukowicz.json to
            tools.jackson.databind;
    opens pl.piotrjanczukowicz.json.jackson to
            tools.jackson.databind;
    opens pl.piotrjanczukowicz.json.jackson.module.immutable to
            tools.jackson.databind;
}
