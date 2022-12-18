FROM amazoncorretto@sha256:792e94e61407ef28d981a0b9a9aeb309690375a61a61f17808499ff16864d0a3
ARG APP_NAME
ARG APP_VERSION
ENV VERSION=$APP_VERSION

RUN yum update -y
RUN yum install -y tar lsof

RUN mkdir -p /opt/creek

COPY bin /bin
COPY log4j /log
COPY agent /opt/creek/agent

COPY ${APP_NAME}-${APP_VERSION}.tar /opt/creek
WORKDIR /opt/creek
RUN tar xf ${APP_NAME}-${APP_VERSION}.tar
RUN ln -s ${APP_NAME}-${APP_VERSION} service

ENTRYPOINT ["/bin/run.sh"]