package mwong.myprojects.rackocomputerplayers;


/**
 * Player2 extends Playerv2 with take the best choice between HandAnalysis1 and HandAnalysis2.
 * It may expand the range to fill the missing slot and try to keep the cards in sequence
 * for higher score.
 *
 * <p>Dependencies : Player2.java, RangeOptimizer.java
 *
 * @author Meisze Wong
 *         www.linkedin.com/pub/macy-wong/46/550/37b/
 */
public class Player3 extends Player2 {
    private int[] discardPile;
    private int[] deckPile;
    private int discardCount;
    private int deckIdx;
    private byte[] cardsStatus;
    protected boolean aggressivePlayer;
    private boolean predictNextCard;
    private boolean expandRange;
    private boolean almostWin;
    private int idShift;
    private int numPlayer;
    private int winScore;
    private int myScore;
    private int[] otherScores;
    private RangeOptimizer optimizer;

    /**
     * Initializes Player3 object.
     */
    public Player3(int size) {
        super(size);
        optimizer = new RangeOptimizer(cardSize, rackSize, cardKey, aveRange);
        numPlayer = 2 + (cardSize - 40) / 10;
        idShift = 0;
        deckPile = new int[20];
        discardPile = new int[20];
        deckIdx = -1;
        discardCount = 0;
        predictNextCard = false;
        cardsStatus = new byte[cardSize + 1];
        otherScores = new int[numPlayer - 1];
        aggressivePlayer = true;
    }

    /**
     * Returns a string represents the difficulty level of Player3.
     *
     * @return a string represents the difficulty level of Player3
     */
    public String toString() {
        return "Hard";
    }

    /**
     * Set the player position of Racko game.
     *
     * @param id the integer of player position of Racko game
     */
    public void setPositionId(int id) {
        idShift = numPlayer - id;
    }

    /**
     * Set the winning score of Racko game.
     *
     * @param score the integer represent the minimum score to win Racko game
     */
    public void setWinningScore(int score) {
        super.setWinningScore(score);
        winScore = score;
    }

    /**
     * Set the latest score of the given player.
     *
     * @param id the integer represent the player of the game
     * @param score the integer represent the score of the player
     */
    public void setPlayerScore(int id, int score) {
        int playerId = transId(id);
        if (playerId == -1) {
            myScore = score;
        } else {
            otherScores[playerId] = score;
        }
    }

    /**
     * Set the initial hand of cards.
     *
     * @param hand the byte array of rack of card values
     */
    public void setHand(byte[] hand) {
        super.setHand(hand);
        int otherScore2win = 0;
        for (int i = 0; i < numPlayer - 1; i++) {
            if (otherScores[i] > otherScore2win) {
                otherScore2win = otherScores[i];
            }
        }

        aggressivePlayer = !aggressivePlayer;
        almostWin = false;
        if (myScore + 45 >= winScore && otherScore2win + 75 < winScore) {
            almostWin = true;
        } else if (myScore + 75 >= winScore) {
            if (otherScore2win + 150 < myScore) {
                almostWin = true;
            } else if (otherScore2win + 25 < winScore + 75) {
                aggressivePlayer = false;
            }
        } else if (otherScore2win + 25 >= winScore) {
            if (myScore + 75 < winScore) {
                aggressivePlayer = true;
            }
        }
		
        // 0 - unknown/drawPile, 1 - discardPile, 2 - self; 4 - player 1, 8 - player 2,
        // 16 - player 3, 32 - unknown player
        for (int i = 0; i < hand.length; i++) {
            cardsStatus[hand[i]] = 2;
        }
    }

    /**
     * Clear and reset the initial setting.
     */
    public void reset() {
        for (int i = 0; i < deckPile.length; i++) {
            deckPile[i] = 0;
        }
        for (int i = 0; i < discardPile.length; i++) {
            discardPile[i] = 0;
        }
        deckIdx = -1;
        discardCount = 0;
        predictNextCard = false;
        for (int i = 0; i < cardsStatus.length; i++) {
            cardsStatus[i] = 0;
        }
        for (int i = 0; i < otherScores.length; i++) {
            otherScores[i] = 0;
        }
    }

    // review the hand of cards and determine the values to keep or discard.
    protected void reviewHand() {
        rangeMax = new int[rackSize + 2];
        gapCount = new int[rackSize];
        int possibleCount = 0;

        for (int i = 1; i <= cardSize; i++) {
            if (cardsStatus[i] == 0 || cardsStatus[i] == 1) {
                possibleCount++;
            }
        }

        int remainder = possibleCount % rackSize;
        int aveRange = (int) Math.floor(possibleCount / rackSize);
        int[] value = new int[rackSize];
        for (int i = 0; i < rackSize; i++) {
            value[i] = hand[i];
            gapCount[i] = aveRange;
            if (remainder > 0) {
                gapCount[i]++;
                remainder--;
            }
        }

        discardReplacement = new int[cardSize + 1];
        drawReplacement = new int[cardSize + 1];

        int pos = 0;
        int count = 0;
        for (int i = 1; i <= cardSize; i++) {
            if (cardsStatus[i] < 2) {
                rangeMax[pos + 1] = i;
                discardReplacement[i] = pos;
                count++;
                if (count == gapCount[pos]) {
                    count = 0;
                    pos++;
                }
            } else if (cardsStatus[i] > 2) {
                discardReplacement[i] = -2;
            }
        }
        rangeMax[rackSize] = cardSize;
        rangeMax[rackSize + 1] = cardSize;

        groupHand = new int[rackSize];
        for (int i = 0; i < rackSize; i++) {
            discardReplacement[value[i]] = cardKey + i;
        }

        int order = -1;
        for (int i = 1; i <= cardSize; i++) {
            if (discardReplacement[i] > rackSize) {
                int val = discardReplacement[i] % cardKey;
                if (order == -1 && (val == i - 1 || val == i)) {
                    order = val;
                } else if (val == order + 1 || val == order + 2) {
                    order = val;
                }
            }
        }

        updateGroupHand();
        handAnalyze();
    }

    // pick the best range between HandAnalyzer1 and HandAnalyzer2 to determine
    // the values to keep or discard.
    protected void handAnalyze() {
        analyzer1.analysisNow(hand, groupHand, gapCount, discardReplacement, rangeMax);
        analyzer2.analysisNow(hand, groupHand, gapCount, discardReplacement, rangeMax);

        if (analyzer1.getRating() > analyzer2.getRating()) {
            System.arraycopy(analyzer1.getRangeMax(), 0, rangeMax, 0, rackSize + 2);
            System.arraycopy(analyzer1.getGapCount(), 0, gapCount, 0, rackSize);
            System.arraycopy(analyzer1.getDiscard(), 0, discardReplacement, 0, cardSize + 1);
            System.arraycopy(analyzer1.getGroupHand(), 0, groupHand, 0, rackSize);
        } else {
            System.arraycopy(analyzer2.getRangeMax(), 0, rangeMax, 0, rackSize + 2);
            System.arraycopy(analyzer2.getGapCount(), 0, gapCount, 0, rackSize);
            System.arraycopy(analyzer2.getDiscard(), 0, discardReplacement, 0, cardSize + 1);
            System.arraycopy(analyzer2.getGroupHand(), 0, groupHand, 0, rackSize);
        }

        offRange = 0;
        for (int i = 0; i < rackSize; i++) {
            if (gapCount[i] != 0) {
                offRange++;
            }
        }
    }

    // expand the range to fill the missing slots and keep the cards in sequence if possible
    private void optimizeRange(boolean expandRange) {
        if (offRange > 0) {
            optimizer.optimizeNow(offRange, aggressivePlayer, hand, gapCount, rangeMax,
                    discardReplacement, drawReplacement);
        }
        if (expandRange) {
            optimizer.optimizeMore(aveRange, hand, gapCount, discardReplacement, drawReplacement);
        }
    }

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
    public boolean determineUse(byte value, boolean isDiscardCard) {
        choosePosition = -1;
        if (almostWin) {
            return prioritySorting(value, isDiscardCard);
        }

        expandRange = false;
        reviewHand();
        if (offRange > 0) {
            optimizer.optimizeNow(offRange, aggressivePlayer, hand, gapCount, rangeMax,
                    discardReplacement, drawReplacement);
        }

        if (offRange == 1) {
            int sum = 0;
            for (int i : gapCount) {
                sum += i;
            }
            if (sum < aveRange * 2) {
                expandRange = true;
                optimizer.optimizeMore(aveRange, hand, gapCount, discardReplacement,
                        drawReplacement);
            }
        }

        secondCheck(value, isDiscardCard);
        if (isDiscardCard) {
            cardsStatus[value] = 1;
        } else {
            cardsStatus[value] = 0;
        }
        return (choosePosition >= 0 && choosePosition < rackSize);
    }

    private void secondCheck(byte value, boolean isDiscardCard) {
        boolean secondCheck = hasReplacement(value, isDiscardCard);
        if (!secondCheck) {
            if (value <= aveRange) {
                if (gapCount[0] == 0 && value < hand[0] && hand[0] > aveRange) {
                    if (gapCount[1] > 0) {
                        choosePosition = 0;
                        return;
                    }
                }
            }
            return;
        }

        if (isDiscardCard) {
            if (value > 1 && discardReplacement[value - 1] == cardKey + choosePosition) {
                if (value < cardSize && discardReplacement[value + 1]
                        != cardKey + choosePosition + 1) {
                    return;
                }
            }
            if (value < cardSize && discardReplacement[value + 1] == cardKey + choosePosition) {
                if (value > 1 && discardReplacement[value - 1] != cardKey + choosePosition - 1) {
                    return;
                }
            }
        }

        final int currOffRange = offRange;
        int replacement = choosePosition;
        byte currCard = hand[replacement];
        byte tempCard = value;
        byte[] backup = new byte[rackSize];
        System.arraycopy(viewable, 0, backup, 0, rackSize);
        replaceCard(tempCard, replacement, false);
        cardsStatus[currCard] = 0;
        reviewHand();
        final int newOffRange = offRange;
        replaceCard(currCard, replacement, false);
        cardsStatus[tempCard] = 0;
        System.arraycopy(backup, 0, viewable, 0, rackSize);
        reviewHand();

        if (newOffRange <= currOffRange) {
            if (newOffRange == 0 || !isDiscardCard || !predictNextCard) {
                choosePosition = replacement;
                return;
            }

            byte nextValue = (byte) deckPile[deckIdx];
            boolean nextCheck = hasReplacement(nextValue, false);
            optimizeRange(expandRange);
            if (!nextCheck) {
                choosePosition = replacement;
                return;
            }

            int replacement2 = choosePosition;
            byte currCard2 = hand[replacement2];
            byte tempCard2 = nextValue;
            byte[] backup2 = new byte[rackSize];
            System.arraycopy(viewable, 0, backup2, 0, rackSize);
            replaceCard(tempCard2, replacement2, false);
            cardsStatus[currCard2] = 0;
            reviewHand();
            final int newOffRange2 = offRange;
            replaceCard(currCard2, replacement2, false);
            cardsStatus[tempCard2] = 0;
            System.arraycopy(backup2, 0, viewable, 0, rackSize);
            reviewHand();
            if (newOffRange2 < newOffRange) {
                choosePosition = -1;
            } else {
                optimizeRange(expandRange);
                choosePosition = replacement;
            }
        }
    }

    // if approach to win, fill the minimum slots to reach the winning score
    // instead of full rack of cards
    private boolean prioritySorting(byte value, boolean isDiscardCard) {
        int count = 1;
        while (count < rackSize) {
            if (hand[count - 1] < hand[count]) {
                count++;
            } else {
                break;
            }
        }

        int fillSlot = (int) Math.floor(value / Math.ceil(cardSize / rackSize));
        if (fillSlot == rackSize) {
            fillSlot--;
        }
        boolean offRangeValue = false;
        if (fillSlot > count + 3) {
            offRangeValue = true;
        }

        int lastSortedSlot = (int) Math.floor(hand[count - 1] / Math.ceil(cardSize / rackSize));
        if (lastSortedSlot == rackSize) {
            lastSortedSlot--;
        }
        boolean offRangeLastSorted = false;
        if (lastSortedSlot > count + 3) {
            offRangeLastSorted = true;
        }

        if (isDiscardCard && offRangeValue) {
            return false;
        }

        if (offRangeLastSorted && offRangeValue) {
            for (int i = fillSlot; i > count; i--) {
                if (value > hand[i - 1]) {
                    if (hand[i] > value) {
                        return false;
                    }
                    choosePosition = i;
                    return true;
                }
            }
        }

        if (value > hand[count - 1]) {
            choosePosition = count;
            return true;
        }
        int breakpoint = 0;
        for (int i = count - 1; i >= 0; i--) {
            int checkSlot = (int) Math.floor(hand[i] / Math.ceil(cardSize / rackSize));
            if (checkSlot == i) {
                breakpoint = i;
                break;
            }
        }

        if (fillSlot < count + 1 && lastSortedSlot >= count) {
            for (int i = 0; i < count; i++) {
                if (value < hand[i]) {
                    if (isDiscardCard && i < breakpoint) {
                        return false;
                    }

                    int checkSlot = (int) Math.floor(hand[i] / Math.ceil(cardSize / rackSize));
                    if (checkSlot == rackSize) {
                        checkSlot--;
                    }
                    if (checkSlot > fillSlot) {
                        if (i == count - 1) {
                            if (i > 0 && hand[i] - value < value - hand[i - 1]) {
                                if (hand[i + 1] > hand[i - 1] && hand[i + 1] > value) {
                                    choosePosition = i;
                                    return true;
                                }
                                choosePosition = i + 1;
                                return true;
                            }
                            choosePosition = i;
                            return true;
                        }
                        choosePosition = i;
                        return true;
                    } else {
                        break;
                    }
                }
            }
        }

        if (isDiscardCard) {
            return false;
        }

        if (fillSlot == lastSortedSlot && value < hand[count - 1]) {
            choosePosition = count;
            return true;
        } else if (fillSlot < lastSortedSlot) {
            for (int i = 0; i < count; i++) {
                if (value < hand[i]) {
                    choosePosition = i;
                    return true;
                }
            }
            if (value > hand[count - 1]) {
                choosePosition = count;
                return true;
            }
            return false;
        } else {
            if (value > hand[fillSlot - 1]) {
                if (value > hand[fillSlot] && hand[fillSlot] > hand[fillSlot - 1]
                        && fillSlot < rackSize - 1) {
                    choosePosition = fillSlot + 1;
                    return true;
                }
                choosePosition = fillSlot;
                return true;
            }
        }
        return false;
    }

    /**
     * When deck pile is empty, notify the discard pile has flipped over to deck pile.
     */
    public void discard2deck() {
        for (int i = 1; i <= cardSize; i++) {
            if (cardsStatus[i] == 0) {
                cardsStatus[i] = 32;
            }
            if (cardsStatus[i] == 1) {
                cardsStatus[i] = 0;
            }
        }
        System.arraycopy(discardPile, 0, deckPile, 0, 20);
        deckIdx = 0;
        discardCount = 0;
        predictNextCard = true;
    }

    /**
     * Notify the card has add to discard pile by the given player.
     *
     * @param card the byte of card value add to discard pile
     * @param id represent the given player
     */
    public void discardAdd(byte card, int id) {
        int value = card;
        cardsStatus[value] = 1;
        discardPile[discardCount++] = value;
    }

    /**
     * Notify the top card has removed from discard pile.
     */
    public void discardRemove() {
        cardsStatus[discardPile[discardCount - 1]] = 32;
        discardCount--;
    }

    /**
     * Notify the top card has removed from deck pile.
     */
    public void deckRemove(int id) {
        if (deckIdx > -1) {
            deckIdx++;
        }
    }

    /**
     * Monitor the given player has replaced a known card at given slot.
     *
     * @param card the byte of card value to keep
     * @param id represent the given player
     * @param slot represent the slot of rack to be replaced
     */
    public void playerCard(byte card, int id, int slot) {
        int playerId = transId(id);
        if (playerId < 0) {
            return;
        }
        cardsStatus[card] = (byte) Math.pow(2, playerId + 2);
    }

    /**
     * Monitor the given player has replaced a unknown card at given slot.
     *
     * @param id represent the given player
     * @param slot represent the slot of rack to be replaced
     */
    public void playerCard(int id, int slot) {
        int playerId = transId(id);
        if (playerId < 0) {
            return;
        }
        if (deckIdx > 0) {
            byte card = (byte) deckPile[deckIdx - 1];
            playerCard(card, id, slot);
        }
    }

    /**
     * Print the current hand of cards.
     */
    private int transId(int id) {
        if (id == -1) {
            return -1;
        }
        return ((id + idShift) % numPlayer) - 1;
    }
}
