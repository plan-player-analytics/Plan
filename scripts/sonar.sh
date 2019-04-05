set -e
cd $TRAVIS_BUILD_DIR/Plan
if [ "$TRAVIS_PULL_REQUEST" = "false" ]; then 
	./gradlew sonarqube
fi
cd $HOME
