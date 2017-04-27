package mwong.myprojects.rackocomputerplayers;

public interface Player {
	public void setHand(byte[] hand);
	public void setPositionId(int id);
	public void setWinningScore(int score);
	public void setPlayerScore(int id, int score);
    public byte[] getHand();
    
    public void reset();
    /*
     * Search through the player's hand and replace the new card with the
     * selected card. Throw the selected card to the discard pile.
     */
    public byte replace(byte takeCard, boolean isDiscardCard);


    /*
     * Determine the use of the card by determining whether the current card's value
     * can go between any two cards in the hand that have cards to be replaced between
     * them.
     */
    public boolean determineUse(byte value, boolean isDiscardCard);
    public int choosePosition(byte card);
    
	public void printAll();
	
	public void discard2deck();	
	public void discardAdd(byte card, int id);
	public void discardRemove();
	public void deckRemove(int id);
	public void playerCard(byte card, int id, int slot);
	public void playerCard(int id, int slot);
	public void setAggressive(boolean b);
}
