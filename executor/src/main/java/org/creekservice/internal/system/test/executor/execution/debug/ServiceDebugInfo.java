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

package org.creekservice.internal.system.test.executor.execution.debug;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toUnmodifiableSet;

import java.util.Objects;
import java.util.Set;
import org.creekservice.api.base.annotation.VisibleForTesting;
import org.creekservice.api.base.type.Preconditions;
import org.creekservice.api.system.test.executor.ExecutorOptions;

/** Implementation of {@link ExecutorOptions.ServiceDebugInfo}. */
public final class ServiceDebugInfo implements ExecutorOptions.ServiceDebugInfo {

    private final int attachMePort;
    private final int baseServicePort;
    private final Set<String> serviceNames;
    private final Set<String> instanceNames;

    /**
     * Create debug info.
     *
     * @param attachMePort the port the attachMe plugin is listening on.
     * @param baseServicePort The port the first service being debugged will listen on for the
     *     debugger to attach. Subsequent services being debugged will use sequential port numbers.
     * @param serviceNames the names of services to debug.
     * @param serviceInstanceNames the names of service instances to debug.
     * @return the debug info.
     * @see <a
     *     href="https://github.com/creek-service/creek-system-test#debugging-system-tests">Service
     *     Debugging</a>
     */
    public static ServiceDebugInfo serviceDebugInfo(
            final int attachMePort,
            final int baseServicePort,
            final Set<String> serviceNames,
            final Set<String> serviceInstanceNames) {
        return serviceNames.isEmpty() && serviceInstanceNames.isEmpty()
                ? new ServiceDebugInfo(0, 0, Set.of(), Set.of())
                : new ServiceDebugInfo(
                        attachMePort, baseServicePort, serviceNames, serviceInstanceNames);
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
                info.attachMePort(),
                info.baseServicePort(),
                info.serviceNames(),
                info.serviceInstanceNames());
    }

    /**
     * @return an empty instance.
     */
    public static ServiceDebugInfo none() {
        return serviceDebugInfo(0, 0, Set.of(), Set.of());
    }

    @VisibleForTesting
    ServiceDebugInfo(
            final int attachMePort,
            final int baseServicePort,
            final Set<String> serviceNames,
            final Set<String> instanceNames) {
        this.attachMePort = attachMePort;
        this.baseServicePort = baseServicePort;
        this.serviceNames = toLower(requireNonNull(serviceNames, "serviceNames"));
        this.instanceNames = toLower(requireNonNull(instanceNames, "instanceNames"));

        if (!serviceNames.isEmpty()) {
            Preconditions.require(
                    attachMePort > 0,
                    "attachMePort must be greater than zero. attachMePort: " + attachMePort);
            Preconditions.require(
                    baseServicePort > 0,
                    "baseServicePort must be greater than zero. baseServicePort: "
                            + baseServicePort);
            Preconditions.require(
                    attachMePort != baseServicePort,
                    "baseServicePort must not equal attachMePort. port: " + baseServicePort);
        }
    }

    @Override
    public int attachMePort() {
        return attachMePort;
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
        return attachMePort == that.attachMePort
                && baseServicePort == that.baseServicePort
                && Objects.equals(serviceNames, that.serviceNames)
                && Objects.equals(instanceNames, that.instanceNames);
    }

    @Override
    public int hashCode() {
        return Objects.hash(attachMePort, baseServicePort, serviceNames, instanceNames);
    }

    private static Set<String> toLower(final Set<String> names) {
        return names.stream().map(String::toLowerCase).collect(toUnmodifiableSet());
    }
}
