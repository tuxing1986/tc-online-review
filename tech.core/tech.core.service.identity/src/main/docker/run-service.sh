# starts sumo logic collector and dropwizard from a Docker container
/usr/local/share/sumocollector/collector start

if [ "$AP_ENV" = "prod" ]
then
	java -Dnewrelic.environment=$AP_ENV -javaagent:$NEWRELIC_JAR -Djavax.net.ssl.trustStore=/data/TC.prod.ldap.keystore -DZOOKEEPER_HOSTS_LIST=$ZOOKEEPER_HOSTS_LIST -jar /data/tech.core.service.identity.jar server /data/config.yml
else
	java -Djavax.net.ssl.trustStore=/data/TC.prod.ldap.keystore -DZOOKEEPER_HOSTS_LIST=$ZOOKEEPER_HOSTS_LIST -jar /data/tech.core.service.identity.jar server /data/config.yml
fi


