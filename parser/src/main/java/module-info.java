module creek.system.test.parser {
    requires transitive creek.system.test.model;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.dataformat.yaml;
    requires com.fasterxml.jackson.datatype.jdk8;

    exports org.creek.api.system.test.parser;
}
