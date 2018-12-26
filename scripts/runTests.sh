cd Plan

if [ "$TRAVIS_PULL_REQUEST" != "false" ]; then 
	gradle test
fi

if [ "$TRAVIS_PULL_REQUEST" = "false" ]; then 
        gradle test
	gradle sonarqube
fi
