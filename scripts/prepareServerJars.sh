mkdir $HOME/servers
mkdir $HOME/servers/build

wget -O BuildTools.jar https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar
git config --global --unset core.autocrlf

cd $HOME/servers
if [ ! -e spigot.jar ]; then
	cd $HOME/servers/build
	echo "Building spigot 1.12.2"
	java -jar BuildTools.jar --rev 1.12.2
	cp spigot-1.12.2.jar ../spigot.jar
else
	echo "Found spigot 1.12.2 jar"
fi

cd $HOME/servers
if [ ! -e craftbukkit.jar ]; then
	cd $HOME/servers/build
	echo "Building craftbukkit 1.8"
	java -jar BuildTools.jar --rev 1.8
	cp craftbukkit-1.8.jar ../craftbukkit.jar
else
	echo "Found craftbukkit 1.8 jar"
fi

cd $HOME/servers
if [ ! -e sponge.jar ]; then
	cd $HOME/servers/build
	echo "Downloading sponge"
	wget -O sponge.jar https://repo.spongepowered.org/maven/org/spongepowered/spongevanilla/1.12.2-7.1.4/spongevanilla-1.12.2-7.1.4.jar
	cp sponge.jar ../sponge.jar
else
	echo "Found sponge jar"
fi	


cd $HOME/servers
rm -rf build
cd $HOME
