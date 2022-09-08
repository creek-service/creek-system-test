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

package org.creekservice.api.system.test.test.extension;

import static java.lang.System.getenv;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.creekservice.api.platform.metadata.ResourceDescriptor;
import org.creekservice.api.platform.metadata.ResourceHandler;
import org.creekservice.api.system.test.extension.CreekSystemTest;
import org.creekservice.api.system.test.extension.CreekTestExtension;
import org.creekservice.api.system.test.extension.test.model.ExpectationHandler.Verifier;
import org.creekservice.api.system.test.test.service.extension.TestResource;

/** Extension used for testing */
public final class TestCreekTestExtension implements CreekTestExtension {

    public static final String ENV_FAIL_VALIDATE_RESOURCE_ID = "creek-test-ext-fail-validate-id";
    public static final String ENV_FAIL_ENSURE_RESOURCE_ID = "creek-test-ext-fail-ensure-id";

    private static final String VALIDATE_FAIL_ID =
            getenv().getOrDefault(ENV_FAIL_VALIDATE_RESOURCE_ID, "");
    private static final String ENSURE_FAIL_ID =
            getenv().getOrDefault(ENV_FAIL_ENSURE_RESOURCE_ID, "");

    @Override
    public String name() {
        return "test system test extension";
    }

    @Override
    public void initialize(final CreekSystemTest api) {
        api.component().model().addResource(TestResource.class, new TestResourceHandler());
        api.test().model().addExpectation(TestExpectation.class, this::prepareExpectation);
    }

    private Verifier prepareExpectation(final Collection<TestExpectation> expectations) {
        return () -> System.out.println("Verifying expectations: " + expectations);
    }

    private static final class TestResourceHandler implements ResourceHandler<TestResource> {

        @Override
        public void validate(final Collection<TestResource> resourceGroup) {
            if (resourceGroup.isEmpty()) {
                throw new AssertionError("Validate called with empty resourceGroup");
            }

            final URI id = validateAllMatchingIds(resourceGroup);

            if (id.toString().equals(VALIDATE_FAIL_ID)) {
                throw new ValidateFailedException(id);
            }

            System.out.println(
                    "Validating resource group: " + id + ", count: " + resourceGroup.size());
        }

        @Override
        public void ensure(final Collection<TestResource> resources) {
            if (resources.isEmpty()) {
                throw new AssertionError("ensure called with empty resources");
            }

            final List<URI> ids = ids(resources);
            if (ids.stream().map(URI::toString).anyMatch(id -> id.equals(ENSURE_FAIL_ID))) {
                throw new EnsureFailedException(ENSURE_FAIL_ID);
            }

            System.out.println("Ensuring resources: " + ids);
        }

        private URI validateAllMatchingIds(final Collection<TestResource> resourceGroup) {
            final Set<URI> ids =
                    resourceGroup.stream().map(TestResource::id).collect(Collectors.toSet());
            if (ids.size() != 1) {
                throw new AssertionError("Validate called with multiple ids: " + ids);
            }
            return ids.iterator().next();
        }

        private static List<URI> ids(final Collection<TestResource> resourceGroup) {
            return resourceGroup.stream().map(ResourceDescriptor::id).collect(Collectors.toList());
        }

        private static final class ValidateFailedException extends RuntimeException {
            ValidateFailedException(final URI id) {
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
