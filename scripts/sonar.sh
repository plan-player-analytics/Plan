cd $HOME/Plan
if [ "$TRAVIS_PULL_REQUEST" = "false" ]; then 
	gradle sonarqube
fi
cd $HOME
