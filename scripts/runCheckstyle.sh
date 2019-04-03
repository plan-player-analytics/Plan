set -e
cd $TRAVIS_BUILD_DIR/Plan
./gradlew checkstyleMain
./gradlew checkstyleTest
cd $HOME
