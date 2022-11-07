/**
 * Contains base types system test extensions implement and the API the system tests exposes to
 * extensions.
 */
module creek.system.test.extension {
    requires transitive creek.platform.metadata;
    requires transitive creek.service.extension;

    exports org.creekservice.api.system.test.extension;
    exports org.creekservice.api.system.test.extension.component.definition;
    exports org.creekservice.api.system.test.extension.test.model;
    exports org.creekservice.api.system.test.extension.test.env;
    exports org.creekservice.api.system.test.extension.test.env.suite;
    exports org.creekservice.api.system.test.extension.test.env.suite.service;
    exports org.creekservice.api.system.test.extension.test.env.listener;

    uses org.creekservice.api.system.test.extension.CreekTestExtension;
}
