cd Plan

if [ "$TRAVIS_PULL_REQUEST" != "false" ]; then 
	gradle test --info
fi

if [ "$TRAVIS_PULL_REQUEST" = "false" ]; then 
        gradle test --info
	gradle sonarqube
fi
