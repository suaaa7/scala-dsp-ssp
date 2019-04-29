FROM centos:7

# JVM
RUN yum -y install java-1.8.0-openjdk java-1.8.0-openjdk-devel

ENV JAVA_HOME /usr/lib/jvm/java-1.8.0-openjdk-1.8.0.144-0.b01.el7_4.x86_64
ENV PATH $PATH:$JAVA_HOME/bin
ENV CLASSPATH .:$JAVA_HOME/jre/lib:$JAVA_HOME/lib:$JAVA_HOME/lib/tools.jar

# Scala
WORKDIR /usr/local/lib
RUN yum -y install wget
RUN wget http://downloads.typesafe.com/scala/2.12.6/scala-2.12.6.tgz
RUN tar zxvf scala-2.12.6.tgz
RUN ln -s scala-2.12.6 scala

ENV SCALA_HOME=/usr/local/lib/scala
ENV PATH $PATH:$SCALA_HOME/bin

# sbt
RUN curl https://bintray.com/sbt/rpm/rpm | tee /etc/yum.repos.d/bintray-sbt-rpm.repo
RUN yum -y install sbt

RUN mkdir -p /opt/SSP
WORKDIR /opt/SSP

CMD ["/bin/bash"]
