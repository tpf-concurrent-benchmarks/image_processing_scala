FROM adoptopenjdk:11-jre-hotspot

ENV SCALA_VERSION=3.3.1
ENV SBT_VERSION=1.9.6

RUN curl -LO https://github.com/lampepfl/dotty/releases/download/$SCALA_VERSION/scala3-$SCALA_VERSION.tar.gz && \
    tar -xf scala3-$SCALA_VERSION.tar.gz -C /opt/ && \
    rm scala3-$SCALA_VERSION.tar.gz && \
    ln -s /opt/scala3-$SCALA_VERSION /opt/scala3

RUN curl -L "https://github.com/sbt/sbt/releases/download/v$SBT_VERSION/sbt-$SBT_VERSION.tgz" | tar -xz -C /opt/ && \
    ln -s /opt/sbt/bin/sbt /usr/local/bin/sbt

ENV PATH=/opt/scala3/bin:/opt/sbt/bin:$PATH

WORKDIR /app

CMD ["sbt", "assembly"]