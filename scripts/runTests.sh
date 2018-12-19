cd Plan

gradle tasks

if [ "$TRAVIS_PULL_REQUEST" != "false" ]; then 
	gradle test
fi

if [ "$TRAVIS_PULL_REQUEST" = "false" ]; then 
	mvn org.jacoco:jacoco-maven-plugin:prepare-agent package sonar:sonar
fi
