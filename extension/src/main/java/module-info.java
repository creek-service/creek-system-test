module creek.system.test.extension {
    requires transitive creek.platform.metadata;

    exports org.creekservice.api.system.test.extension;
    exports org.creekservice.api.system.test.extension.model;
    exports org.creekservice.api.system.test.extension.testsuite;
    exports org.creekservice.api.system.test.extension.service;

    uses org.creekservice.api.system.test.extension.CreekTestExtension;
}
