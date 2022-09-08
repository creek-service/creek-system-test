module creek.system.test.extension {
    requires transitive creek.platform.metadata;
    requires transitive creek.service.extension;

    exports org.creekservice.api.system.test.extension;
    exports org.creekservice.api.system.test.extension.test.model;
    exports org.creekservice.api.system.test.extension.test.suite;
    exports org.creekservice.api.system.test.extension.component.definition;
    exports org.creekservice.api.system.test.extension.test.suite.service;

    uses org.creekservice.api.system.test.extension.CreekTestExtension;
}
