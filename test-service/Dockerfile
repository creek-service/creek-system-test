FROM amazoncorretto:21@sha256:e268835d2ba1559ef39f9c7d9fc69501ff45143e6fbf89ada118017ad14f52c1
ARG APP_NAME
ARG APP_VERSION
ENV VERSION=$APP_VERSION


RUN mkdir -p /opt/creek

COPY bin /bin
COPY log4j /log

COPY ${APP_NAME}-${APP_VERSION} /opt/creek/${APP_NAME}-${APP_VERSION}
WORKDIR /opt/creek
RUN ln -s ${APP_NAME}-${APP_VERSION} service

ENTRYPOINT ["/bin/run.sh"]