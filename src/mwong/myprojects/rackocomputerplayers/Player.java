package mwong.myprojects.rackocomputerplayers;


/**
 * Player is the interface class that has the basic methods of any
 * computer player for Racko game.
 *
 * @author Meisze Wong
 *         www.linkedin.com/pub/macy-wong/46/550/37b/
 */
interface Player {
    /**
     * Set the player position of Racko game.
     * 
     * @param id the integer of player position of Racko game
     */
    public void setPositionId(int id);
    
    /**
     * Set the winning score of Racko game.
     * 
     * @param score the integer represent the minimum score to win Racko game
     */
    public void setWinningScore(int score);
    
    /**
     * Set the latest score of the given player
     * 
     * @param id the integer represent the player of the game
     * @param score the integer represent the score of the player
     */
    public void setPlayerScore(int id, int score);
    
    /**
     * Set the initial hand of cards.
     * 
     * @param hand the byte array of rack of card values
     */
    public void setHand(byte[] hand);
    
    /**
     * @return byte array of the rack of card values
     */
    public byte[] getHand();
    
    /**
     * Clear and reset the initial setting.
     */
    public void reset();

    /**
     * Determine the use of the card by determining whether the current card's value
     * can go between any two cards in the hand that have cards to be replaced between
     * them.
     * 
     * @param value the byte of card value to be review
     * @param isDiscardCard the boolean represent the given card from discard pile
     *        or deck pile
     * @return boolean the card value to keep or ignored
     */
    public boolean determineUse(byte value, boolean isDiscardCard);

    /**
     * Determine the slot of rack to be replace by the given card.
     * 
     * @param card the byte of card value to be keep
     * @return integer the slot of rack to be replaced, -1 if ignored
     */
    public int choosePosition(byte card);

    /**
     * Search through the player's hand and replace the new card with the
     * selected card. Throw the selected card to the discard pile.
     * 
     * @param takeCard the byte of card value to keep
     * @param isDiscardCard the boolean represent the given card from discard pile
     *        or deck pile
     * @return byte the original card value to be replaced
     */
    public byte replace(byte takeCard, boolean isDiscardCard);

    /**
     * When deck pile is empty, notify the discard pile has flipped over to deck pile.
     */
    public void discard2deck();    
    
    /**
     * Notify the card has add to discard pile by the given player.
     * 
     * @param card the byte of card value add to discard pile
     * @param id represent the given player
     */
    public void discardAdd(byte card, int id);
    
    /**
     * Notify the top card has removed from discard pile.
     */
    public void discardRemove();
    
    /**
     * Notify the top card has removed from deck pile.
     */
    public void deckRemove(int id);
    
    /**
     * Monitor the given player has replaced a known card at given slot.
     * 
     * @param card the byte of card value to keep
     * @param id represent the given player
     * @param slot represent the slot of rack to be replaced
     */
    public void playerCard(byte card, int id, int slot);
    
    /**
     * Monitor the given player has replaced a unknown card at given slot.
     * 
     * @param id represent the given player
     * @param slot represent the slot of rack to be replaced
     */
    public void playerCard(int id, int slot);
    
    /**
     * Print the current hand of cards.
     */
    public void printAll();
}
