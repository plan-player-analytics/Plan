cd $TRAVIS_BUILD_DIR/Plan
if [ "$TRAVIS_PULL_REQUEST" = "false" ]; then 
	gradle sonarqube
fi
cd $HOME
