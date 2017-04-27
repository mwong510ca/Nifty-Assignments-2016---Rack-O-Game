## Rack-O! Game
[Screenshots] / [Youtube demo]
![Racko Game - start up screen](screenshots/gui1.png)  

My Rack-O! game support 2 to 4 hand of cards as the real game (40 - 60 cards).  [See rules]
A player can play with 1 to 3 computer hands.  For a 4 hands game, player may play 2 hand alternatively with 2 computer hands.

There are 3 level of computer player to choose from:
Easy - Pick the best sorted order and fill the missing slots.
       Written by Arvind Bhusnurmath, Kristen Gee, and Karen Her - copied from [Nifty assignment 2016, Racko]
Moderate - Even distribution to fill all slots.
  Try to keep the numbers in sequence for higher winning points.
  Randomly pick to prioritize lower numbers first, or higher number first.
Hard - Compare both prioritize option above and pick the best choice.
  If too few cards will fill the missing slot, expand the range if possible.
  When approaching to win, sort the slots to win instead of full rack. 
[Details - see computer stergery below]

Game option:  Single round - 75 pts to win
             Full game - 500 pts to win
            Custom setting - 75 to 1000 pts to win

### GUI design

The GUI is writtern in pyqt5, using [py4j] connected to computer player support in java.  Card image use [Nifty assignment 2016, Racko].


[Development history]

[Nifty Assignments 2016, Racko]: http://nifty.stanford.edu/2016/arvind-racko/
[See rules]: http://www.hasbro.com/common/instruct/Racko(1987).PDF
[Development history]: 