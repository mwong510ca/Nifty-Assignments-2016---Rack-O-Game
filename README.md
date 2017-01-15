## [Nifty Assignments 2016]

### [Rack-O Game] -- Arvind Bhusnurmath, Kristen Gee, and Karen Her  

Phase 1 - completed on Nov 21, 2016:  
* UI - Given.
  * Completed missing code.
  * Add [scoring] as actual card game.  The player get 500 points or over to win.
* Computer strategy - Given.

Phase 2 - completed on Jan 5, 2017.
* UI - No change.
* Data structure.
  * Add Player interface.  Move codes for computer player to Player1 object.
* Computer strategy (Player2 object) - Rewrite:
  1. Review the hand with even distrubtion.
  2. Hold the cards within the range and in ordering.
  3. Utilize the draw card to increase the chance to filled the missing card.
  4. Keep the cards in sequence for bonus points if possible.

Phase 3 - in progress.
* Improve my computer strategy by tracing known cards on deck and other player's hand.

Phase 4.
* Modify game to support 2 to 4 players, rewrite UI in pyqt5. 

[Nifty Assignments 2016]: http://nifty.stanford.edu
[Rack-O Game]: http://nifty.stanford.edu/2016/arvind-racko/
[scoring]: http://www.hasbro.com/common/instruct/Racko(1987).PDF
