package mwong.myprojects.rackocomputerplayers;

/**
 * AbstractPlayer is the abstract class extends Player Interface of Racko game.
 *
 * <p>Dependencies : Player.java
 *
 * @author Meisze Wong
 *         www.linkedin.com/pub/macy-wong/46/550/37b/
 */
abstract class AbstractPlayer implements Player {
    // constants
    protected final int rackSize = 10;
    protected final int cardKey = 100;
    protected final int singleRound = 75;
    // game setting
    protected int cardSize;
    // rack of cards related
    protected byte[] hand;
    protected byte[] viewable;

    public AbstractPlayer(int size) {
        cardSize = size;
    }

    /**
     * Set the player position of Racko game.
     *
     * @param id the integer of player position of Racko game
     */
    public void setPositionId(int id) {
        // do nothing
    }

    /**
     * Set the winning score of Racko game.
     *
     * @param score the integer represent the minimum score to win Racko game
     */
    public void setWinningScore(int score) {
        // do nothing
    }

    /**
     * Set the latest score of the given player.
     *
     * @param id the integer represent the player of the game
     * @param score the integer represent the score of the player
     */
    public void setPlayerScore(int id, int score) {
        // do nothing
    }

    /**
     * Set the initial hand of cards.
     *
     * @param hand the byte array of rack of card values
     */
    public void setHand(byte[] hand) {
        this.viewable = new byte[rackSize];
        this.hand = new byte[hand.length];
        for (int i = 0; i < hand.length; i++) {
            this.hand[i] = hand[i];
        }
    }

    /**
     * @return byte array of the rack of card values.
     */
    public final byte[] getHand() {
        return hand.clone();
    }

    /**
     * Clear and reset the initial setting.
     */
    public void reset() {
        // do nothing
    }

    // Replace the new card value at given slot and return the original card value.
    protected byte replaceCard(byte newCard, int pos, boolean isDiscardCard) {
        byte returnCard = hand[pos];
        hand[pos] = newCard;
        if (isDiscardCard) {
            viewable[pos] = 3;
        } else {
            viewable[pos] = 1;
        }
        return returnCard;
    }

    /**
     * When deck pile is empty, notify the discard pile has flipped over to deck pile.
     */
    public void discard2deck() {
        // do nothing
    }

    /**
     * Notify the card has add to discard pile by the given player.
     *
     * @param card the byte of card value add to discard pile
     * @param id represent the given player
     */
    public void discardAdd(byte card, int id) {
        // do nothing
    }

    /**
     * Notify the top card has removed from discard pile.
     */
    public void discardRemove() {
        // do nothing
    }

    /**
     * Notify the top card has removed from deck pile.
     */
    public void deckRemove(int id) {
        // do nothing
    }

    /**
     * Monitor the given player has replaced a known card at given slot.
     *
     * @param card the byte of card value to keep
     * @param id represent the given player
     * @param slot represent the slot of rack to be replaced
     */
    public void playerCard(byte card, int id, int slot) {
        // do nothing
    }

    /**
     * Monitor the given player has replaced a unknown card at given slot.
     *
     * @param id represent the given player
     * @param slot represent the slot of rack to be replaced
     */
    public void playerCard(int id, int slot) {
        // do nothing
    }

    /**
     * Print the current hand of cards.
     */
    public final void printAll() {
        for (int i = 0; i < rackSize; i++) {
            int val = hand[i];
            if (val < 10) {
                System.out.print(" " + val + " ");
            } else {
                System.out.print(val + " ");
            }
        }
        System.out.println();
    }
}