
public interface Player {
    public void setHand(Card[] hand);

    public Card[] getHand();
    

    /*
     * Search through the player's hand and replace the new card with the
     * selected card. Throw the selected card to the discard pile.
     */
    public Card replace(Card takeCard, boolean isDiscardCard);


    /*
     * Determine the use of the card by determining whether the current card's value
     * can go between any two cards in the hand that have cards to be replaced between
     * them.
     */
    public boolean determine_use(int value, boolean isDiscardCard);

    public void print();
    public void printAll();
    
    public void discardShuffle();
    
    public void discardAdd(Card card);
    
    public void discardRemove();
    public void playerCard(Card card, int id);
}
