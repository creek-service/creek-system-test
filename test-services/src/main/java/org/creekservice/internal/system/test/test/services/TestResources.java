/*
 * Copyright 2022 Creek Contributors (https://github.com/creek-service)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.creekservice.internal.system.test.test.services;


import java.net.URI;
import org.creekservice.api.system.test.test.service.extension.TestResource;
import org.creekservice.api.system.test.test.service.extension.TestResourceInput;
import org.creekservice.api.system.test.test.service.extension.TestResourceInternal;
import org.creekservice.api.system.test.test.service.extension.TestResourceOutput;
import org.creekservice.api.system.test.test.service.extension.TestResourceShared;

public final class TestResources {
    private TestResources() {}

    public static TestResourceShared shared(final String name) {
        return new Shared(name);
    }

    public static TestResourceInternal internal(final String name) {
        return new Internal(name);
    }

    public static TestResourceOutput output(final String name) {
        return new OwnedOutput(name);
    }

    private abstract static class Base implements TestResource {
        final String name;
        private final URI id;

        Base(final String name) {
            this.name = name;
            this.id = URI.create("test://" + name);
        }

        @Override
        public URI id() {
            return id;
        }

        public TestResourceInput toInput() {
            return new UnownedInput(name);
        }
    }

    private static class Shared extends Base implements TestResourceShared {
        Shared(final String name) {
            super(name);
        }
    }

    private static class Internal extends Base implements TestResourceInternal {
        Internal(final String name) {
            super(name);
        }
    }

    private static class UnownedInput extends Base implements TestResourceInput {
        UnownedInput(final String name) {
            super(name);
        }
    }

    private static class OwnedOutput extends Base implements TestResourceOutput {
        OwnedOutput(final String name) {
            super(name);
        }
    }
}
