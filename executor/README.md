# Creek system test runner

Provides functionality for executing system tests.

Simply put, system tests provide black-box testing of micro-services. The system tests start your service or services, 
and any additional 3rd-party services they need e.g. Kafka, in local docker containers, so that it can run through
the system test cases you've defined.

System test cases are written YAML and define any initial seed data; the inputs to feed into the system; and the
expected output or final state of the system.
