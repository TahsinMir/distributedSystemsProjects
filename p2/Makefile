all:
	mvn clean
	mvn compile
	mvn package

idserver:
	java -jar target/IdServer.jar ${ARGS}

idclient:
	java -jar target/IdClient.jar ${ARGS}

clean:
	mvn clean
	rm -rf target
	rm -rf userinfo.db
