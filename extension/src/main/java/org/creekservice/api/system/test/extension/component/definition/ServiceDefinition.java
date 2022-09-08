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

package org.creekservice.api.system.test.extension.component.definition;


import java.util.Optional;
import org.creekservice.api.platform.metadata.ServiceDescriptor;
import org.creekservice.api.system.test.extension.test.env.suite.service.ConfigurableServiceInstance;
import org.creekservice.api.system.test.extension.test.env.suite.service.ServiceInstance;

/** Information required by Creek System Test to start a service */
public interface ServiceDefinition extends ComponentDefinition {

    /** @return the docker image name, without version info. */
    String dockerImage();

    /**
     * An optional service descriptor.
     *
     * <p>Any service discovered on the class or module path, i.e. the services being tested, will
     * have an associated service definition. 3rd-party services started by extensions to facilitate
     * testing will not.
     *
     * @return the service definition, if present.
     */
    default Optional<? extends ServiceDescriptor> descriptor() {
        return Optional.empty();
    }

    /**
     * An optional callback that is invoked after a service instance if created from this
     * definition.
     *
     * <p>Overriding this method allows the definition class itself to define how to configure the
     * instance. The same functionality can be achieved by configuring the instance returned from
     * {@link
     * org.creekservice.api.system.test.extension.test.env.suite.service.ServiceInstanceContainer#add},
     * e.g.
     *
     * <pre>{@code
     * void foo(final SystemTest api) {
     *    final ConfigurableServiceInstance instance = api.test().suite().services().add(def);
     *    instance
     *       .addExposedPort(22)
     *       .addEnv("key", "value");
     * }
     * }</pre>
     *
     * <p>Is equivalent to:
     *
     * <pre>{@code
     * class MyDef implements ServiceDefinition {
     *     ...
     *     public void configureInstance(final ConfigurableServiceInstance instance) {
     *          instance
     *             .addExposedPort(22)
     *             .addEnv("key", "value");
     *     }
     *     ...
     * }
     *
     * void foo(final SystemTest api) {
     *    final ServiceInstance instance = api.test().suite().services().add(new MyDef());
     * }
     * }</pre>
     *
     * @param instance the newly created instance.
     */
    default void configureInstance(final ConfigurableServiceInstance instance) {}

    /**
     * An optional callback that is invoked after a service instance is started.
     *
     * <p>Overriding this method allows the definition class itself to define what to do with an
     * instance once its started. The same functionality can be achieved by performing the same
     * actions on the instance in the code where the service is started:
     *
     * <pre>{@code
     * void foo(final SystemTest api) {
     *    final ServiceInstance instance = api.test().suite().services().add(def);
     *    instance.start();
     *    instance.execOnInstance("some-command");
     * }
     * }</pre>
     *
     * <p>Is equivalent to:
     *
     * <pre>{@code
     * class MyDef implements ServiceDefinition {
     *     ...
     *     public void instanceStarted(final ServiceInstance instance) {
     *          instance.execOnInstance("some-command");
     *     }
     *     ...
     * }
     *
     * void foo(final SystemTest api) {
     *    final ServiceInstance instance = api.test().suite().services().add(new MyDef());
     *    instance.start();
     * }
     * }</pre>
     *
     * @param instance the newly created instance.
     */
    default void instanceStarted(final ServiceInstance instance) {}
}
