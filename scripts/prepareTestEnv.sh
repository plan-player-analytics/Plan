# Install chromedriver
wget -N http://chromedriver.storage.googleapis.com/2.30/chromedriver_linux64.zip -P ~/
unzip ~/chromedriver_linux64.zip -d ~/
rm ~/chromedriver_linux64.zip
sudo mv -f ~/chromedriver /usr/local/share/
sudo chmod +x /usr/local/share/chromedriver
sudo ln -s /usr/local/share/chromedriver /usr/local/bin/chromedriver

whereis google-chrome-stable
whereis chromedriver
