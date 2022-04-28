module creek.system.test.executor {
    requires creek.base.type;
    requires info.picocli;
    requires org.apache.logging.log4j;

    exports org.creek.api.system.test.executor;

    opens org.creek.internal.system.test.executor to
            info.picocli;
}
