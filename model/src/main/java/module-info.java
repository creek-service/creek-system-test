module creek.system.test.model {
    requires transitive creek.system.test.extension;
    requires transitive com.fasterxml.jackson.annotation;
    requires transitive com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires creek.base.type;

    exports org.creek.api.system.test.model;
}
