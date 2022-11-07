/**
 * The base model that system tests are deserialized as.
 *
 * <p>System test extensions can extend this model.
 */
module creek.system.test.model {
    requires transitive creek.system.test.extension;
    requires transitive com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires creek.base.type;

    exports org.creekservice.api.system.test.model;
}
