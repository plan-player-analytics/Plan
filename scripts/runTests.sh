set -e
cd $TRAVIS_BUILD_DIR/Plan
./gradlew test --info
cd $HOME
