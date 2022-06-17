module creek.system.test.executor {
    requires creek.base.type;
    requires creek.platform.metadata;
    requires transitive creek.system.test.extension; // Todo: undo transative
    requires creek.system.test.parser;
    requires info.picocli;
    requires org.slf4j;
    requires org.apache.logging.log4j;
    requires java.management;
    requires com.github.spotbugs.annotations;
    requires testcontainers;

    exports org.creekservice.api.system.test.executor;
    exports org.creekservice.api.system.test.executor.api.testsuite.service;

    opens org.creekservice.internal.system.test.executor.cli to
            info.picocli;

    uses org.creekservice.api.platform.metadata.ComponentDescriptor;
}
