# JukeBot
The official bot for The Cats Pajamas Discord server

# Top features?
* Spotify to YouTube converter  
* LAN Controls (figure out a team based off individual skill levels for a CLOSE match)  
* Music  
* Polls

# Installation (on Raspberry PI)
1. Download JukeBot.jar and help.txt from https://www.github.com/wdavies973/JukeBot/releases.
2. Create a folder on the desktkop of your PI and name it JukeBot
3. Place the JukeBot.jar and help.txt file in the JukeBot folder on your desktop
4. Type ```crontab -u pi -e``` in terminal.
5. In the text file, place this: ```@reboot /usr/bin/java -jar /home/pi/JukeBot/JukeBot.jar```.
6. Restart your PI, and voila! (JukeBot will boot everytime your PI boots).

# Commands?
Find commands by downloading the help.txt file here:
https://www.github.com/wdavies973/JukeBot/releases
