[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Coverage Status](https://coveralls.io/repos/github/creek-service/creek-system-test/badge.svg?branch=main)](https://coveralls.io/github/creek-service/creek-system-test?branch=main)
[![build](https://github.com/creek-service/creek-system-test/actions/workflows/gradle.yml/badge.svg)](https://github.com/creek-service/creek-system-test/actions/workflows/gradle.yml)
[![CodeQL](https://github.com/creek-service/creek-system-test/actions/workflows/codeql.yml/badge.svg)](https://github.com/creek-service/creek-system-test/actions/workflows/codeql.yml)

# Creek System Test

This repo provides functionality for executing system tests.

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
* [Debugging system test](#debugging-system-test)

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
* **[test-extension](test-extension):** a system test extension used internally during testing.

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
[Kafka Streams system test extensions][1]. The extensions define _seed_ and _input_ extensions for producing data to
Kafka and _expectation_ extensions for checking data is in Kafka.  See the [Kafka Streams system test extension docs][1]
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
description: Seed environment with base set of user data
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
seed type extension (See [Kafka system test extensions][1] for more details). However, they're hopefully pretty
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

There is a [gradle plugin][2] for running system tests as part of a Gradle build.

Happy to take contributions for a [Maven plugin](https://github.com/creek-service/creek-system-test/issues/2).

## Debugging system test

[Coming soon...](https://github.com/creek-service/creek-system-test/issues/22)

## Extending system tests

Creek is designed to be extendable. See how to [extend system tests](extension).

[1]: https://github.com/creek-service/creek-kafka/tree/main/system-test
[2]: https://github.com/creek-service/creek-system-test-gradle-plugin