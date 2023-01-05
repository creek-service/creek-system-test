/*
 * Copyright 2022-2023 Creek Contributors (https://github.com/creek-service)
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

package org.creekservice.api.system.test.test.extension;

import static java.lang.System.getenv;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.creekservice.api.platform.metadata.ComponentDescriptor;
import org.creekservice.api.platform.metadata.ResourceDescriptor;
import org.creekservice.api.platform.metadata.ResourceHandler;
import org.creekservice.api.service.extension.CreekExtension;
import org.creekservice.api.service.extension.CreekExtensionProvider;
import org.creekservice.api.service.extension.CreekService;
import org.creekservice.api.system.test.test.service.extension.TestResource;

public final class TestCreekExtensionProvider
        implements CreekExtensionProvider<TestCreekExtensionProvider.TestCreekExtension> {

    public static final String ENV_FAIL_VALIDATE_RESOURCE_ID = "creek-test-ext-fail-validate-id";
    public static final String ENV_FAIL_ENSURE_RESOURCE_ID = "creek-test-ext-fail-ensure-id";

    private static final String VALIDATE_FAIL_ID =
            getenv().getOrDefault(ENV_FAIL_VALIDATE_RESOURCE_ID, "");
    private static final String ENSURE_FAIL_ID =
            getenv().getOrDefault(ENV_FAIL_ENSURE_RESOURCE_ID, "");

    public TestCreekExtensionProvider() {}

    @Override
    public TestCreekExtension initialize(final CreekService api) {
        validate(api.components().descriptors().stream());
        api.components().model().addResource(TestResource.class, new TestResourceHandler());
        return new TestCreekExtension();
    }

    private void validate(final Stream<ComponentDescriptor> components) {

        final boolean fail =
                components
                        .flatMap(ComponentDescriptor::resources)
                        .anyMatch(res -> res.id().toString().equals(VALIDATE_FAIL_ID));
        if (fail) {
            throw new TestResourceHandler.ValidateFailedException(VALIDATE_FAIL_ID);
        }
    }

    public static final class TestCreekExtension implements CreekExtension {
        TestCreekExtension() {}

        @Override
        public String name() {
            return "test";
        }
    }

    private static final class TestResourceHandler implements ResourceHandler<TestResource> {

        @Override
        public void ensure(final Collection<? extends TestResource> resources) {
            if (resources.isEmpty()) {
                throw new AssertionError("ensure called with empty resources");
            }

            final List<URI> ids = ids(resources);
            if (ids.stream().map(URI::toString).anyMatch(id -> id.equals(ENSURE_FAIL_ID))) {
                throw new EnsureFailedException(ENSURE_FAIL_ID);
            }

            System.out.println("Ensuring resources: " + ids);
        }

        private static List<URI> ids(final Collection<? extends TestResource> resourceGroup) {
            return resourceGroup.stream().map(ResourceDescriptor::id).collect(Collectors.toList());
        }

        private static final class ValidateFailedException extends RuntimeException {
            ValidateFailedException(final String id) {
                super("Validation failed for resource group: " + id);
            }
        }

        private static final class EnsureFailedException extends RuntimeException {
            EnsureFailedException(final String failId) {
                super("Ensure failed for resource: " + failId);
            }
        }
    }
}
