/** Parser of YAML system test packages. */
module creek.system.test.parser {
    requires transitive creek.system.test.model;
    requires creek.base.type;
    requires creek.base.schema;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.dataformat.yaml;
    requires com.fasterxml.jackson.datatype.jdk8;
    requires com.github.spotbugs.annotations;

    exports org.creekservice.api.system.test.parser;
    exports org.creekservice.internal.system.test.parser to
            creek.system.test.test.util;
}
