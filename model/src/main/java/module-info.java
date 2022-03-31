module creek.system.test.model {
    requires transitive com.fasterxml.jackson.annotation;
    requires transitive com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires creek.base.type;

    exports org.creek.system.test.model.api;
}
