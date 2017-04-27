## [Nifty Assignments 2016]

### [Rack-O Game] -- Arvind Bhusnurmath, Kristen Gee, and Karen Her  

Phase 1 - completed on Nov 21, 2016:  
* Data structure.
  * Add Player interface.  
  * Move computer strategy for Racko.java to Player1.java.
* UI - Given.
  * Completed missing code.
  * Add [scoring] as actual card game.  The player get 500 points or over to win.
* Computer strategy - Given.

Phase 2 - completed on Jan 5, 2017.  
[Youtube Demo]
* UI - No change.
* Computer strategy - Rewrite:
  1. Review the hand with even distrubtion.  Hold the cards within the range and in ordering.
  2. Held cards may change based on the new card replacement.
  <pre>Example:  12 25 17 21 31 29 18  2 30 36 
                                         H  H
                Replace 17 with 9
                12 25  9 21 31 29 18  2 30 36
                       H           H     H  H
                Replace 2 with 27
                12 25  9 21 31 29 18 27 30 36 
                       H              H  H  H</pre>                                     
  3. Utilize the draw card to increase the chance to filled the missing card.
  <pre>Example:  10 15 12 32  1 25 30 20 39 30 
                 H     H        H  H
      If the discard card is 27, ignored.  Take the second chance to draw another card.
      If the draw card is 27, replace 25 with 27 to increase the chance to fill slot 4 and 5.  
      If the draw card is 7, replace 15 with 7 instead of replace 10 at slot 1.
          (7 chance) To fill 0-7 on slot 1 or fill 10-12 on slot 2.
          (1 chance) To fill 10-12 on slot 2.
          (4 chance) To fill 7-12 on slot 2.
  </pre>
  4. Keep the cards in sequence for bonus points if possible.
* Other - Moved images to images folder.

* How to play:  
  [Download Phase 2] and unzip the folder.  On Terminal, type: java -jar Racko.jar  
  You can watch the changes made by computer strategy.  
  <pre>Example:  computer :  7  9 15  x  o 31  x  o  x  x
            x - The initial card when the game started.
            o - The card has replaced from the draw pile.
            # - The card has replaced from the discard pile.</pre>

Phase 3 -  completed on Jan 30, 2017.  
* Computer strategy - Improve my computer strategy by tracing known cards on deck and other player's hand.
  1.  Computer will ignore the cards in the discard deck.
  2.  Once the discard deck has flipped and replaced to the draw deck.  Computer knew:
    * The 10 cards on player's hand but not the acutal order
    * The actual card in discard pile for better decision making.

* Here is the difference between Phase 2 and Phase 3:  
  * If the player kept all cards 31 - 40 (not in order) on it's hand and not to throw them back to the discard deck.  
  * Computer has 3 5 10 12 17 18 19 29 30 11 on it's hand.  Pending a card from 31 - 40 to fill the slot 10.  
    * Phase 2: The round will never finish, because the computer will never fill the 
the slot 10.  Computer estimated there are 10 chances to fill slot 10, and it won't shift the 9th card to increase chance.  
      * Phase 3:  Once the deck has shuffled and replaced.  The computer found the player hold the card 31 - 40.  It will changed the estimates to sort the rack from 1 to 30 instead of 1 to 40.  

* How to play:  
  [Download Phase 3] and unzip the folder.  On Terminal, type: java -jar Racko.jar  

Phase 4 - completed on Feb 12, 2017.
* Modify game to support 2 to 4 players (1 to 3 computer player), rewrite UI in pyqt5. 

[Nifty Assignments 2016]: http://nifty.stanford.edu
[Rack-O Game]: http://nifty.stanford.edu/2016/arvind-racko/
[scoring]: http://www.hasbro.com/common/instruct/Racko(1987).PDF
[Download Phase 2]: https://github.com/mwong510ca/RackoGame/raw/master/Phase%202%20completed/Racko2.zip
[Download Phase 3]: https://github.com/mwong510ca/RackoGame/raw/master/Phase%203%20completed/Racko3.zip
[Youtube Demo]: https://www.youtube.com/watch?v=6vSdBQDapKY&list=PLRnfrf3rzEFnVm00w-JZ-693lRKXiFRfU
