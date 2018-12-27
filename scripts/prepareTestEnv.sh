# Install chromedriver
wget -N http://chromedriver.storage.googleapis.com/2.30/chromedriver_linux64.zip -P ~/
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
