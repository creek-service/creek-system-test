#!/bin/bash

java \
 -Xms64m -Xmx256m \
 -Dlog4j.configurationFile=/log/log4j2.xml \
 --module-path "/opt/creek/service/lib" \
 --module creek.system.test.test.service/org.creekservice.internal.system.test.test.service.ServiceMain