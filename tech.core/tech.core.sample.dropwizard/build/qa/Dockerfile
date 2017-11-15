FROM appiriodevops/ap-microservice-base:0.0.1

MAINTAINER devops@appirio.com


### Sample

WORKDIR /data

# dev|qa|prod
ENV NEWRELIC_ENV qa

# New Relic
COPY newrelic.yml /usr/local/share/newrelic/newrelic.yml

# Sumo Logic
COPY sumo.conf /etc/sumo.conf
COPY sumo-sources.json ./sumo-sources.json

# Application
COPY ap-sample.jar ./ap-sample.jar
COPY config.yml ./config.yml

# start sumocollector and application
# - $NEWRELIC_JAR: defined in the base image
CMD /bin/bash /usr/local/share/sumocollector/collector start && \
    java -jar \
    	 ./ap-sample.jar server ./config.yml

EXPOSE 8080 8081
