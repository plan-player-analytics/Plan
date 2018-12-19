cd Plan

gradle tasks

if [ "$TRAVIS_PULL_REQUEST" != "false" ]; then 
	gradle test
fi

if [ "$TRAVIS_PULL_REQUEST" = "false" ]; then 
	gradle sonarqube
fi
