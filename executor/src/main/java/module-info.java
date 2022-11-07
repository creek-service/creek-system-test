/** Executor of system tests. */
module creek.system.test.executor {
    requires transitive creek.platform.metadata;
    requires transitive creek.system.test.extension;
    requires creek.base.type;
    requires creek.system.test.parser;
    requires creek.observability.lifecycle;
    requires creek.service.api;
    requires creek.platform.resource;
    requires info.picocli;
    requires org.slf4j;
    requires org.apache.logging.log4j;
    requires java.management;
    requires com.github.spotbugs.annotations;
    requires testcontainers;
    requires docker.java.api;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.dataformat.xml;
    requires com.fasterxml.jackson.datatype.jdk8;

    exports org.creekservice.api.system.test.executor;
    exports org.creekservice.internal.system.test.executor.api.test.env.suite.service to
            creek.system.test.test.util;
    exports org.creekservice.internal.system.test.executor.api.component.definition to
            creek.system.test.test.util;
    exports org.creekservice.internal.system.test.executor.execution.debug to
            creek.system.test.test.util;

    opens org.creekservice.internal.system.test.executor.cli to
            info.picocli;

    exports org.creekservice.internal.system.test.executor.api.test.model to
            creek.system.test.test.util;
    exports org.creekservice.internal.system.test.executor.result.xml to
            com.fasterxml.jackson.databind;
}
