cd Plan

if [ "$TRAVIS_PULL_REQUEST" != "false" ]; then 
	mvn -T 1C test
fi

if [ "$TRAVIS_PULL_REQUEST" = "false" ]; then 
	mvn org.jacoco:jacoco-maven-plugin:prepare-agent package sonar:sonar
fi
