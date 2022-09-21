module creek.system.test.test.util {
    requires transitive com.github.spotbugs.annotations;
    requires transitive creek.system.test.extension;
    requires transitive creek.system.test.executor;
    requires creek.system.test.parser;
    requires com.fasterxml.jackson.databind;

    exports org.creekservice.api.system.test.test.util;
}
