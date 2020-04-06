all:
	mvn clean
	mvn compile
	mvn package

server:
	java -jar target/IdServer.jar

debug:
	java -jar target/IdServer.jar --verbose --numport ${ARGS}

client:
	java -jar target/IdClient.jar ${ARGS}

clean:
	mvn clean
	rm -rf target