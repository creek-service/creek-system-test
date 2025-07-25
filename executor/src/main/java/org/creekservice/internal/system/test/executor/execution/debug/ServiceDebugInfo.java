/*
 * Copyright 2022-2025 Creek Contributors (https://github.com/creek-service)
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

package org.creekservice.internal.system.test.executor.execution.debug;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toUnmodifiableSet;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.creekservice.api.base.annotation.VisibleForTesting;
import org.creekservice.api.base.type.Preconditions;
import org.creekservice.api.system.test.executor.ExecutorOptions;

/** Implementation of {@link ExecutorOptions.ServiceDebugInfo}. */
public final class ServiceDebugInfo implements ExecutorOptions.ServiceDebugInfo {

    private final int baseServicePort;
    private final Set<String> serviceNames;
    private final Set<String> instanceNames;
    private final Map<String, String> env;

    /**
     * Create debug info.
     *
     * @param baseServicePort The port the first service being debugged will listen on for the
     *     debugger to attach. Subsequent services being debugged will use sequential port numbers.
     * @param serviceNames the names of services to debug.
     * @param serviceInstanceNames the names of service instances to debug.
     * @param env the environment variables to set on service being debugged.
     * @return the debug info.
     * @see <a
     *     href="https://github.com/creek-service/creek-system-test#debugging-system-tests">Service
     *     Debugging</a>
     */
    public static ServiceDebugInfo serviceDebugInfo(
            final int baseServicePort,
            final Set<String> serviceNames,
            final Set<String> serviceInstanceNames,
            final Map<String, String> env) {
        return serviceNames.isEmpty() && serviceInstanceNames.isEmpty()
                ? new ServiceDebugInfo(0, Set.of(), Set.of(), Map.of())
                : new ServiceDebugInfo(baseServicePort, serviceNames, serviceInstanceNames, env);
    }

    /**
     * Create an immutable copy of {@code info}
     *
     * @param info the info to copy
     * @return the immutable copy.
     */
    public static ServiceDebugInfo copyOf(final ExecutorOptions.ServiceDebugInfo info) {
        if (info instanceof ServiceDebugInfo) {
            return (ServiceDebugInfo) info;
        }
        return serviceDebugInfo(
                info.baseServicePort(),
                info.serviceNames(),
                info.serviceInstanceNames(),
                info.env());
    }

    /**
     * @return an empty instance.
     */
    public static ServiceDebugInfo none() {
        return serviceDebugInfo(0, Set.of(), Set.of(), Map.of());
    }

    @VisibleForTesting
    ServiceDebugInfo(
            final int baseServicePort,
            final Set<String> serviceNames,
            final Set<String> instanceNames,
            final Map<String, String> env) {
        this.baseServicePort = baseServicePort;
        this.serviceNames = toLower(requireNonNull(serviceNames, "serviceNames"));
        this.instanceNames = toLower(requireNonNull(instanceNames, "instanceNames"));
        this.env = Map.copyOf(requireNonNull(env, "env"));

        if (!serviceNames.isEmpty()) {
            Preconditions.require(
                    baseServicePort > 0,
                    "baseServicePort must be greater than zero. baseServicePort: "
                            + baseServicePort);
        }
    }

    @Override
    public int baseServicePort() {
        return baseServicePort;
    }

    @Override
    public Set<String> serviceNames() {
        return Set.copyOf(serviceNames);
    }

    @Override
    public Set<String> serviceInstanceNames() {
        return Set.copyOf(instanceNames);
    }

    @Override
    public Map<String, String> env() {
        return Map.copyOf(env);
    }

    /**
     * Helper method to determine if a service instance should be debugged.
     *
     * @param serviceName the instance's service name.
     * @param instanceName the instance's name.
     * @return {@code true} if it should be debugged, {@code false} otherwise.
     */
    public boolean shouldDebug(final String serviceName, final String instanceName) {
        return serviceNames.contains(serviceName.toLowerCase())
                || instanceNames.contains(instanceName.toLowerCase());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ServiceDebugInfo that = (ServiceDebugInfo) o;
        return baseServicePort == that.baseServicePort
                && Objects.equals(serviceNames, that.serviceNames)
                && Objects.equals(instanceNames, that.instanceNames)
                && Objects.equals(env, that.env);
    }

    @Override
    public int hashCode() {
        return Objects.hash(baseServicePort, serviceNames, instanceNames, env);
    }

    private static Set<String> toLower(final Set<String> names) {
        return names.stream().map(String::toLowerCase).collect(toUnmodifiableSet());
    }
}
