cd $HOME/Rsl1122/Plan-PlayerAnalytics/Plan
if [ "$TRAVIS_PULL_REQUEST" = "false" ]; then 
	gradle sonarqube
fi
cd $HOME
