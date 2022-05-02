module creek.system.test.executor {
    requires creek.base.type;
    requires info.picocli;
    requires org.apache.logging.log4j;
    requires java.management;

    exports org.creekservice.api.system.test.executor;

    opens org.creekservice.internal.system.test.executor to
            info.picocli;
}
