cd $TRAVIS_BUILD_DIR/Plan
gradle checkstyleMain
gradle checkstyleTest
cd $HOME
