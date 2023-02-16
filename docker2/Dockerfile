FROM  centos:7

RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime

RUN echo 'Asia/Shanghai' >/etc/timezone

RUN yum -y install kde-l10n-Chinese && yum -y reinstall glibc-common

RUN localedef -c -f UTF-8 -i zh_CN zh_CN.utf8

ENV LC_ALL zh_CN.utf8

RUN  yum install java-1.8.0-openjdk* -y

RUN  mkdir -p /data/projects/

WORKDIR /data/projects/

COPY  flink-streaming-platform-web  /data/projects/flink-streaming-platform-web/

COPY  flink-1.14.6 /data/projects/flink-1.14.6/

RUN  chown -R root:root /data/projects/flink-streaming-platform-web/

RUN  chown -R root:root /data/projects/flink-1.14.6/

COPY application-docker.properties  /data/projects/flink-streaming-platform-web/conf/

RUN  cd  /data/projects/

ENTRYPOINT ["java","-jar","/data/projects/flink-streaming-platform-web/lib/flink-streaming-web-1.5.0.RELEASE.jar","--spring.config.additional-location=/data/projects/flink-streaming-platform-web/conf/application-docker.properties","&"]

EXPOSE  8180
 


