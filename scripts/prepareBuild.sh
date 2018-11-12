mvn clean
cd PlanPluginBridge
mvn install:install-file -Dfile=./PlanPluginBridge-4.5.0.jar -DpomFile=./pom.xml
cd ..