[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Coverage Status](https://coveralls.io/repos/github/creek-service/creek-system-test/badge.svg?branch=main)](https://coveralls.io/github/creek-service/creek-system-test?branch=main)
[![build](https://github.com/creek-service/creek-system-test/actions/workflows/build.yml/badge.svg)](https://github.com/creek-service/creek-system-test/actions/workflows/build.yml)
[![Maven Central](https://img.shields.io/maven-central/v/org.creekservice/creek-system-test-executor.svg)](https://central.sonatype.dev/search?q=creek-system-test-*)
[![CodeQL](https://github.com/creek-service/creek-system-test/actions/workflows/codeql.yml/badge.svg)](https://github.com/creek-service/creek-system-test/actions/workflows/codeql.yml)
[![OpenSSF Scorecard](https://api.securityscorecards.dev/projects/github.com/creek-service/creek-system-test/badge)](https://api.securityscorecards.dev/projects/github.com/creek-service/creek-system-test)
[![OpenSSF Best Practices](https://bestpractices.coreinfrastructure.org/projects/6899/badge)](https://bestpractices.coreinfrastructure.org/projects/6899)

# Creek System Test

This repo provides functionality for executing system tests.

See [CreekService.org](https://www.creekservice.org) for info on Creek Service.

> ### NOTE
> There is a [Gradle plugin][gradle-plugin] for running system tests as part of a Gradle build that should be used in preference
> to invoking the test executor manually!

Simply put, system tests provide black-box testing of microservices. The system tests start your service or services,
and any additional 3rd-party services they need e.g. Kafka, in local docker containers, so that it can run through
the system test suites you've defined.

System tests are written in YAML and define any initial seed data; the inputs to feed into the system; and the
expected output or final state of the system.

### Table of contents

* [Terminology](#terminology)
* [Modules in this repository](#modules-in-this-repository)
* [Writing system tests](#writing-system-tests)
    * [System test directory structure](#system-test-directory-structure)
    * [Example system test package](#example-system-test-package)
    * [Testing multiple services](#testing-multiple-services)
    * [Disabling tests](#disabling-tests)
* [Running system tests](#running-system-tests) 
* [Debugging system tests](#debugging-system-tests)

## Terminology

First, let's define the terminology we'll be using:

| Term | Meaning |
|------|---------|
| Test Case | A single test, defined within a _test suite_, that defines the inputs to feed in and expected output. The test either passes or fails. |
| Test Suite | A YAML file containing a collection of _test cases_ and additional metadata, e.g. which services should be started. |
| Test Package | A directory containing _seed data_ and one or more _test suites_. |
| Seed Data | A common set of pre-requisites to feed into the system before starting any of the services under test, e.g. records in a Kafka topic, stored withing a _test package_. |
| Input Data | Data the system tests feed into the system as part of running a test case, i.e. the input that should trigger the expected output, e.g. a record produced to a Kafka topic. |
| Expectations | Expectations the system test will check once all an _input_ has been fed into the environment for a test case, e.g. an expectation of a certain record in a Kafka topic. |
| Test Environment | The set of Docker containers started by the system tests to run your services and any other services they require, e.g. a Kafka cluster. |

## Modules in this repository

* **[executor](executor):** functionality for executing system tests.
* **[extension](extension):** defines types required to implement extensions to the system tests.
* **[model](model):** system test model.
* **[parser](parser):** code for parsing system tests.
* **[test-service](test-service):** creates a docker image container a simple microservice used for testing within this repo
* **[test-services](test-services):** creates a jar containing the metadata for [test-service](test-service), i.e. the service descriptor. 
* **[test-service-extension-metadata](test-service-extension-metadata):** the metadata types for a fictitious service extension used internally during testing.
* **[test-system-test-extension](test-system-test-extension):** a system test extension used internally during testing.
* **[test-util](test-util)**: contains some utilities to help test system test extensions.

## Writing system tests

### System test directory structure

A test package is made up of a directory containing one or more test suites, and sub-directories containing _inputs_,
_expectations_ and, optionally, _seed_ data:

| Directory | Content |
|-----------|---------|
| <root>    | Aside from containing the below directories, the root also contains one or more _test suite_ YAML files. |
| seed      | (Optional) Each YAML file within the directory defines _seed data_ that is fed into the system before the services under test are started |
| inputs    | Each YAML file defines a set of input data to be fed into the system that a test case can reference by name, e.g. producing a Kafka record, writing to a file, etc. |
| expectations | Each YAML file defines a set of expectations on the state of the system that a test case can reference by name, e.g. a Kafka record in a topic or a file in s3 etc. |

The directory structure of the system tests can contain a single test package:

```
 $testsDir
    |--seed
    |   |--a.seed.yml
    |   |--another.seed.yml
    |--inputs
    |   |--an.input.yaml
    |   |--another.input.yaml
    |--expectations
    |   |--an.expectation.yaml
    |   |--another.expectation.yaml
    |--a.test.suite.yaml
    |--another.test.suite.yaml
```

... or multiple test packages (YAML files removed for brevity):

```
 $testsDir
    |--package-1
    |  |--seed
    |  |--inputs
    |  |--expectations
    |--package-2
    |  |--seed
    |  |--inputs
    |  |--expectations
    ...    
```

... or indeed any number of levels of sub-directories you may wish to use to organise things:

```
 $testsDir
    |--package-1
    |  |--seed
    |  |--inputs
    |  |--expectations
    |  |--sub-package-a
    |     |--seed
    |     |--inputs
    |     |--expectations
    |--dir
    |  |--package-2
    |    |--seed
    |    |--inputs
    |    |--expectations
    ...    
```

### Example system test package

The easiest way to get familiar with the format is to look at an example.

The example will be a Kafka-Streams based microservice, so the examples below will be making use of the
[Kafka Streams system test extensions][kafka-test-ext]. The extensions define _seed_ and _input_ extensions for producing data to
Kafka and _expectation_ extensions for checking data is in Kafka.  See the [Kafka Streams system test extension docs][kafka-test-ext]
for more info.

Let's look at testing a fictitious `balance-service` that tracks _user_ balances.
The `balance-service` consumes a stream of deposits and withdrawals from the `deposit_withdrawal` topic and outputs an
aggregated _account balance_ for a _user_ to the `account_balance` changelog topic.
For the benefit of this example, let's also say the `balance-service` reads a `user` changelog topic which contains a
table of _user_ data. Our service uses this _user_ data to enrich its own output, (a questionable architectural design,
but useful for the sake of our example!).

![balance service](images/balance_service.png "Balance Service")

#### Seeding the test environment

To test the system, we may want to seed the test environment with some initial _user_ data:

##### **`seed/user.yml`**
```yaml
---
'@type': kafka_topic
notes: Seed environment with base set of user data
topic: user
records:
  - key: 1876
    value:
      name: Bob Smith
  - key: 20368
    value:
      name: Alice Peterson
```

The `@type` field in `seed/user.yml` is set to `kafka_topic`, which indicates to the system tests that the file holds
records that should be produced to Kafka. The rest of the fields within the file are specific to the `kafka_topic`
seed type extension (See [Kafka system test extensions][kafka-test-ext] for more details). However, they're hopefully pretty
self-explanatory:

The `seed/user.yml` file defines two _users_ that will be produced to the `user` topic before any of out `balance-service`
is started. The key of the record is a numeric _user-id_ and the value holds information about the user, which is just
their name in our simple example.

A test package can define multiple seed files defining multiple sets of seed data that should be used to seed the
test environment with data.

#### Writing a test suite:

With the environment seeded with some user data, lets go ahead and define out first test suite:

##### **`smoke-test.yml`**
```yaml
---
name: smoke test
services:
  - balance-service
tests:
  - name: processes deposit
    inputs:
      - alice_deposit_100
    expectations:
      - alice_balance_100
  - name: processes withdrawl
    inputs:
      - alice_withdraw_25
    expectations:
      - alice_balance_75
```

Multiple test suites can be defined by adding more files in the root directory.
Each test suite is run with a clean test environment.  
Bringing up and seeding a test environment is a relatively time-consuming task, which you may want to consider before
breaking things up into many suites.

Test cases within a suite run sequentially after the seed data has been published and all services started.  
This means the second and subsequent test case are affected by previous test cases and the system state they result in.

This example file defines a test suite that starts our `balance-service` and runs two test cases. The fist test case
is playing in a `alice_deposit_100` input:

#### **`inputs/alice_deposit_100.yml`**
```yaml
---
'@type': kafka_topic
topic: deposit_withdrawal
records:
   - key: 20368
     value: 100
```

...this plays in a single Kafka record, keyed on the _user-id_, in this case Alice's, and the value contains amount
deposited.

The first test case also defines a single `alice_balance_100` expectation:

#### **`expectations/alice_balance_100.yml`**
```yaml
---
'@type': kafka_topic
topic: account_balance
records:
   - key: Alice Peterson
     value: 100
```

...which checks that a record has been produced to the `account_balance` topic with the user's name as the key and
the users balance as the value.

The second test case plays in:

#### **`inputs/alice_withdraw_25.yml`**
```yaml
---
'@type': kafka_topic
topic: deposit_withdrawal
records:
   - key: 20368
     value: -25
```

...and expects:

#### **`expectations/alice_balance_75.yml`**
```yaml
---
'@type': kafka_topic
topic: account_balance
records:
   - key: Alice Peterson
     value: 75
```

The test package directory for the above example would look like:

```
 $testsDir
    |--seed
    |   |--user.yml
    |--inputs
    |   |--alice_deposit_100.yaml
    |   |--alice_withdraw_25.yaml
    |--expectations
    |   |--alice_balance_100.yaml
    |   |--alice_balance_75.yaml
    |--smoke-test.yml.yaml
```

### Testing multiple services

Testing multiple services together is straight forward. Simply list all the services that should be started in the test
suite, in the order they should be started, and then define _seed_, _inputs_ and _expectations_ as normal. The system
tests will start your services, pump in the inputs and assert the expectations.

### Disabling tests

Test cases and whole test suites can be disabled by adding a `disabled` `reason` and optional associated `issue` url.
For example:

##### **`suite-with-disabled-test.yml`**
```yaml
---
name: example test suite with disabled tests
services:
  - some-service
tests:
  - name: disabled test
    disabled:
      justification: put your justificiation/notes on why its disabled here.
      issue: http://link.to.associated.issue.tracking.the.disabled.test
    inputs:
      - some_input
    expectations:
      - expected_output
  - name: enabled test
    inputs:
      - some_input
    expectations:
      - expected_output
```

##### **`disabled-suite.yml`**
```yaml
---
name: example disabled suite
services:
  - some-service
disabled:
  justification: put your justificiation/notes on why its disabled here.
  issue: http://link.to.associated.issue.tracking.the.disabled.test
tests:
  - name: test
    inputs:
      - some_input
    expectations:
      - expected_output
```

## Running system tests

There is a [Gradle plugin][gradle-plugin] for running system tests as part of a Gradle build.

Happy to take contributions for a [Maven plugin](https://github.com/creek-service/creek-system-test/issues/2).

## Debugging system tests

The system tests leverage the [AttachMe][attachme-plugin] IntelliJ plugin to simplify attaching the debugger to Creek based services
running within Docker containers. You can read more about the AttachMe plugin [here][attachme-blog].  

Assuming the AttachMe plugin is installed, running and listening on the correct port, and the microservice is
[correctly configured](#configuring-a-service-for-debugging), then debugging the service is as simple as 
setting breakpoints in the code and then passing the service, or service-instance, name(s) on the command 
line when running system tests.

Specifying service names allows all instances of the service to be debugged.
The service name to use is the same as the names used in the  `services` field of a test suite,
for example, `my-service`.  

Specifying service instance names allows only a specific instance of a service to be debugged.
Instance names are in the form <service-name>-<instance-number>, where instance-number is zero indexed.
For example, `my-service-1` would mean the second instance of `my-service` to be started.

The exact mechanism / command line required to debug a service will depend on the build plugin used.
See the [System Test Gradle plugin][gradle-plugin] documentation for more information.

### Configuring a service for debugging

> ### NOTE
> Template repos, such as [Aggregate Template][aggregate-template] are correctly preconfigured for service debugging

To be able to debug a Creek Service the following must be true:

1. The AttachMe agent must be present on the local machine at `~/.attachme/attacheme-agent-x.x.x.jar`.
   Running an AttachMe run configuration in IntelliJ will cause the plugin to download the agent jar to your home directory.
2. The service's `DockerFile` needs to be tweaked to handle the agent.
   This includes installing the `lsof` command, as this is required by the agent.
   ```dockerfile
   RUN yum install -y lsof
   ```

### How it works

When the AttachMe plugin is first run it downloads an agent jar and script to the user's home directory under  
`~/.attachme`.

To use AttachMe normally, a user would create and run an `AttachMe` configuration and then `source` the `~/.attachme/config.sh` 
script in a terminal window to set the `JAVA_TOOL_OPTIONS` environment variable to install the agent and set up the
debugging parameters. Any java process started from the terminal, or any child processes it starts, will
automatically pick up the `JAVA_TOOL_OPTIONS` and spawn a new debug window in IntelliJ.  
This is very cool and useful in itself, and I'd recommend you read the [blog post][attachme-blog] on the topic to learn more.

When debugging services during system testing the agent jar is made available to the service by mounting a directory
containing the jar into the service's container, and configuring the Java agent via the `JAVA_TOOLS_OPTIONS` environment 
variable when creating the Docker container.
When the service starts up, the AttachMe agent calls out to the AttachMe plugin, letting it know what port
the service is listening on for the debugger to attach. The plugin passes this on to the IntelliJ debugger, which
then connects to the service.

> ### NOTE
> the service is configured to wait for the debugger to attach, so if the AttachMe Configuration is not running
> the service will timeout on startup.

### Known failures to debug

#### Cannot run program "lsof"

If you see an error like the following:

```
Picked up JAVA_TOOL_OPTIONS: -javaagent:/opt/creek/mounts/debug/attachme-agent-1.2.1.jar=host:host.docker.internal,port:7857 -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:8000
Listening for transport dt_socket at address: 8000
[attachme] Initialized agent for process PID=1
java.lang.RuntimeException: java.io.IOException: Cannot run program "lsof": error=2, No such file or directory
        at com.attachme.agent.CommandPortResolver.getPortCandidates(CommandPortResolver.java:45)
        at com.attachme.agent.Agent$DebugPortTask.run(Agent.java:94)
        at java.base/java.lang.Thread.run(Thread.java:829)
Caused by: java.io.IOException: Cannot run program "lsof": error=2, No such file or directory
        at java.base/java.lang.ProcessBuilder.start(ProcessBuilder.java:1128)
        at java.base/java.lang.ProcessBuilder.start(ProcessBuilder.java:1071)
        at com.attachme.agent.CommandPortResolver.getPortCandidates(CommandPortResolver.java:36)
        ... 2 more
Caused by: java.io.IOException: error=2, No such file or directory
        at java.base/java.lang.ProcessImpl.forkAndExec(Native Method)
        at java.base/java.lang.ProcessImpl.<init>(ProcessImpl.java:340)
        at java.base/java.lang.ProcessImpl.start(ProcessImpl.java:271)
        at java.base/java.lang.ProcessBuilder.start(ProcessBuilder.java:1107)
        ... 4 more
```
...then the docker image is missing `lsof`.
See [Configuring a service for debugging](#configuring-a-service-for-debugging)

#### Service being debugged hangs on startup

The service will block waiting for the debugger to attach. Therefore, it's likely the debugger is not attaching.
The most common causes this are:

1. An AttachMe configuration is not running. Once the Intellij plugin is installed you will need to create a new 
   "run configuration" in IntelliJ to run it. See the [blog post][attachme-blog] for details.
2. The port the AttachMe plugin is listening does not match the one the service is calling out on. The default port
   used by the AttachMe plugin is `7857`. The same default is used by Creek. However, if you need to change the
   plugin's default for any reason, you'll need to also tell the Creek system tests. 
   See the documentation for the build plugin you are using.

## Extending system tests

Creek is designed to be extendable. See how to [extend system tests](extension).

[kafka-test-ext]: https://github.com/creek-service/creek-kafka/tree/main/test-extension
[gradle-plugin]: https://github.com/creek-service/creek-system-test-gradle-plugin
[attachme-plugin]: https://plugins.jetbrains.com/plugin/13263-attachme
[attachme-blog]: https://blog.jetbrains.com/scala/2020/01/14/attachme-attach-the-intellij-idea-debugger-to-forked-jvms-automatically/
[aggregate-template]: https://github.com/creek-service/aggregate-template