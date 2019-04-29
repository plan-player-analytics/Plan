# Start Xvfb screen
export DISPLAY=:99.0
sh -e /etc/init.d/xvfb start
/sbin/start-stop-daemon --start --quiet --pidfile /tmp/custom_xvfb_99.pid --make-pidfile --background --exec /usr/bin/Xvfb -- :99 -ac -screen 0 1280x1024x16

# Install chromedriver
wget -N https://chromedriver.storage.googleapis.com/74.0.3729.6/chromedriver_linux64.zip -P ~/
unzip ~/chromedriver_linux64.zip -d ~/
rm ~/chromedriver_linux64.zip
sudo chmod -R u=rwx,g=rwx /usr/bin/google-chrome-stable 
sudo chmod -R u=rwx,g=rwx /usr/bin/X11/google-chrome-stable

sudo mv -f ~/chromedriver /usr/local/share/
sudo chmod u=rwx,g=rwx /usr/local/share/chromedriver

sudo ln -s /usr/local/share/chromedriver /usr/local/bin/chromedriver

whereis google-chrome-stable
whereis chromedriver

mysql -e 'CREATE DATABASE Plan;'
