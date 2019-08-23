from registry.cn-beijing.aliyuncs.com/yilan_resys_tfx/beam_python:v1.0
RUN pip install -i https://pypi.tuna.tsinghua.edu.cn/simple \
                tensorflow==1.14.0 \
                numpy==1.16.4

RUN pip install tensorflow_transform==0.14.0

ADD ./hadoop-current /ml/hadoop-current
ADD ./hadoop-conf /ml/hadoop-conf
ADD ./hosts /ml/hosts
add ./localfilesystem.py /usr/local/lib/python2.7/site-packages/apache_beam/io/localfilesystem.py
add ./sdk_worker_main.py /usr/local/lib/python2.7/site-packages/apache_beam/runners/worker/sdk_worker_main.py
add ./hadoopfilesystem.py /usr/local/lib/python2.7/site-packages/apache_beam/io/hadoopfilesystem.py
add ./entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

ENV HADOOP_CONF /ml/hadoop-conf
ENV HADOOP_HOME /ml/hadoop-current
ENV HADOOP_HDFS_HOME /ml/hadoop-current
ENV HADOOP_CLASSPATH=/ml/hadoop-current/lib/*
ENV JAVA_LIBRARY_PATH /ml/hadoop-current/lib/native/
ENV JAVA_HOME /usr/lib/jvm/java-1.8.0-openjdk-amd64/
ENV PATH $PATH:/ml/hadoop-current/bin:/ml/hadoop-current/sbin
ENV LD_LIBRARY_PATH $LD_LIBRARY_PATH:${JAVA_HOME}/jre/lib/amd64/server

RUN apt-get update && \
    apt-get install -y -o Acquire::http::proxy="http://127.0.0.1:8118" \
                        openjdk-8-jre \
                        openjdk-8-jdk 

RUN cat /ml/hosts >> /etc/hosts
RUN rm /ml/hosts
ENTRYPOINT  ["/entrypoint.sh"] 
