module creek.system.test.executor {
    requires creek.base.type;
    requires creek.platform.metadata;
    requires creek.system.test.extension;
    requires creek.system.test.parser;
    requires info.picocli;
    requires org.apache.logging.log4j;
    requires java.management;
    requires com.github.spotbugs.annotations;

    exports org.creekservice.api.system.test.executor;

    opens org.creekservice.internal.system.test.executor.cli to
            info.picocli;

    uses org.creekservice.api.platform.metadata.ComponentDescriptor;
}
