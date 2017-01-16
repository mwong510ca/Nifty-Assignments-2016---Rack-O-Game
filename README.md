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
* UI - No change.
* Computer strategy (Player2 object) - Rewrite:
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
                 If the discard card is 27, ignored.  Take the second chance to draw a card.
                 If the draw card is 27, replace 25 with 27 to increase the chance.  
                 If the draw card is 7, replace 15 with 7 instead of replace 10 at slot 1.
                 (7 chance) To fill 0-7 on slot 1 or fill 10-12 on slot 2 
                 (1 chance) To fill 10-12 on slot 2.
                 (4 chance) To fill 7-12 on slot 2.
  </pre>
  4. Keep the cards in sequence for bonus points if possible.
* Other - Moved images to images folder.

Phase 3 - in progress.
* Improve my computer strategy by tracing known cards on deck and other player's hand.

Phase 4.
* Modify game to support 2 to 4 players, rewrite UI in pyqt5. 

[Nifty Assignments 2016]: http://nifty.stanford.edu
[Rack-O Game]: http://nifty.stanford.edu/2016/arvind-racko/
[scoring]: http://www.hasbro.com/common/instruct/Racko(1987).PDF
