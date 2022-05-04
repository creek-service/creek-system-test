
import org.creekservice.api.system.test.test.extension.TestExtension;

module creek.system.test.test.extension {
    requires transitive creek.system.test.extension;
    requires transitive com.fasterxml.jackson.annotation;

    exports org.creekservice.api.system.test.test.extension;

    provides org.creekservice.api.system.test.extension.CreekTestExtension with
            TestExtension;
}
