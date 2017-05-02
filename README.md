### Rack-O! Game
[Screenshots] / [Youtube demo]

![Racko Game - start up screen](screenshots/startup.png)  

My Rack-O! game support 2 to 4 hand of cards as the real game (40 - 60 cards).  [See rules]  
A user can play with 1 to 3 computer hands.  For a 4 hands game, user may play 2 hands alternatively with 2 computer hands.

There are 3 level of computer player to choose from:
* Easy - Pick the best sorted order and fill the missing slots.  
       Written by Arvind Bhusnurmath, Kristen Gee, and Karen Her - copied from [Nifty assignments 2016, Racko]
* Moderate - Even distribution to fill all slots.  
  Randomly pick to prioritize lower numbers first, or higher number first. 
  <pre>
  Example:  2  4  8 18  3 24  7 13 33 39
               H     H        H  H
    If the new card is 16, it will place in slot 5.  
    To sort the slots 3 to 6, either fill slot 4 from 8 - 16 or fill slot 5 from 18 - 24.
  </pre>
* Hard - Compare both prioritize option above and pick the best choice.  
    <pre>
    Example:  12 25 17 21 31 29 18  2 30 36 
                                       H  H
              Replace 17 with 9
              12 25  9 21 31 29 18  2 30 36
                     H           H     H  H
              Replace 2 with 27
              12 25  9 21 31 29 18 27 30 36 
                     H              H  H  H
    </pre>
  Try to keep the numbers in sequence for higher winning points.  
  If too few cards will fill the missing slot, expand the range if possible.  
  When approaching to win such as 475 points, sort minimun 5 slots (+25 points) to win instead of full rack.  

[System requirements and installation]  

### GUI design

The GUI is writtern in pyqt5, using [py4j] connected to computer player support in java.  Card image use [Nifty assignments 2016, Racko].

Change the game setting from the menu bar:  
Single round - 75 points  
Full game - 500 points  
Custom setting - 75 to 1000 points

Choose your choice of computer players, click "NEW" button to start the game.  
An option to show card replacement by the players.  
On player's turn, the rack will highlight in lighter color.  
<pre>
Blue   - initial card when start the game.
Green  - card has been replaced.  If card from discard pile, also display card number.
Yellow - player pick the card to place in discard pile.
</pre>

### How to play
The left rack of cards is the user's rack.  
* To keep the top card from discard pile, simply click the card you want to replace.  
* Otherwise, click the deck pile to turn over the top card from the deck pile.  
* Then click your card to replace it or click the deck pile again to move it to discard pile.  
For 4 hands and play with 2 sets.  The second set on the right side.

The user may terminate the game at any time.  
An option to have a computer player (Moderate level only) take over and finish the remaining game in fast speed.

[Development history]

[Nifty Assignments 2016, Racko]: http://nifty.stanford.edu/2016/arvind-racko/
[See rules]: http://www.hasbro.com/common/instruct/Racko(1987).PDF
[Development history]: https://github.com/mwong510ca/Racko_ComputerStrategy/tree/master/older%20versions
[py4j]: https://www.py4j.org
[Screenshots]: https://github.com/mwong510ca/Racko_ComputerStrategy/tree/master/screenshots
[System requirements and installation]: https://github.com/mwong510ca/Racko_ComputerStrategy/tree/master/gui(pyqu5)
