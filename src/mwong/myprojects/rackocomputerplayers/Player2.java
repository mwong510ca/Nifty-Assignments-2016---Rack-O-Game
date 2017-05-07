package mwong.myprojects.rackocomputerplayers;


/**
 * Player2 extends AbstractPlayer with computer strategy start from even distribution.
 * If fill the card in assigned slot up to +/- 1 neighbor.
 *
 * <p>Dependencies : AbstractPlayer.java, HandAnalysis1.java
 *
 * @author Meisze Wong
 *         www.linkedin.com/pub/macy-wong/46/550/37b/
 */
public class Player2 extends AbstractPlayer {
    protected int[] groupHand;
    protected int[] gapCount;
    protected int[] discardReplacement;
    protected int[] rangeMax;
    protected int[] drawReplacement;
    protected int aveRange;
    protected int offRange;
    protected int choosePosition;
    protected final AbstractHandAnalyzer analyzer1;
    protected final AbstractHandAnalyzer analyzer2;
    private AbstractHandAnalyzer inUseAnalyzer;
    private byte[] backupHand;
    private byte[] deadlockReference;
    private boolean deadlock;
    private boolean[] possibleHand;
    private boolean analyzerFlag;

    /**
     * Initializes Player2 object.
     */
    public Player2(int size) {
        super(size);
        aveRange = cardSize / rackSize;
        analyzer1 = new HandAnalyzer1(cardSize, rackSize, cardKey);
        analyzer2 = new HandAnalyzer2(cardSize, rackSize, cardKey);
        backupHand = new byte[rackSize];
        deadlockReference = new byte[rackSize];
        rangeMax = new int[rackSize + 2];
        gapCount = new int[rackSize];
        discardReplacement = new int[cardSize + 1];
        drawReplacement = new int[cardSize + 1];
        groupHand = new int[rackSize];
        possibleHand = new boolean[cardSize + 1];
        analyzerFlag = true;
    }

    /**
     * Returns a string represents the difficulty level of Player2.
     *
     * @return a string represents the difficulty level of Player2
     */
    public String toString() {
        return "Moderate";
    }

    /**
     * Set the initial hand of cards.
     *
     * @param hand the byte array of rack of card values
     */
    public void setHand(byte[] hand) {
        super.setHand(hand);
        System.arraycopy(hand, 0, backupHand, 0, rackSize);
        deadlock = false;
        if (analyzerFlag) {
            inUseAnalyzer = analyzer1;
        } else {
            inUseAnalyzer = analyzer2;
        }
        analyzerFlag = !analyzerFlag;
    }

    private void evenDistribution() {
    	for (int i = 0; i < possibleHand.length; i++) {
    		possibleHand[i] = false;
    	}
    	for (int i = 0; i < rangeMax.length; i++) {
    		rangeMax[i] = 0;
    	}
    	for (int i = 0; i < gapCount.length; i++) {
    		gapCount[i] = 0;
    	}
        for (int i = 1; i <= cardSize; i++) {
            possibleHand[i] = true;
        }
        int countMax = aveRange - 1;
        for (int i = 0; i < rackSize; i++) {
            possibleHand[hand[i]] = false;
            gapCount[i] = countMax;
        }

        for (int i = 0; i < discardReplacement.length; i++) {
        	discardReplacement[i] = 0;
    	}
    	for (int i = 0; i < drawReplacement.length; i++) {
    		drawReplacement[i] = 0;
    	}

        int pos = 0;
        int count = 0;
        for (int i = 1; i <= cardSize; i++) {
            if (possibleHand[i]) {
                rangeMax[pos + 1] = i;
                discardReplacement[i] = pos;
                count++;
                if (count == countMax) {
                    count = 0;
                    pos++;
                }
            } else {
                discardReplacement[i] = -1;
                continue;
            }
        }
        rangeMax[rackSize] = cardSize;
        rangeMax[rackSize + 1] = cardSize;

        for (int i = 0; i < groupHand.length; i++) {
    		groupHand[i] = 0;
    	}
        for (int i = 0; i < rackSize; i++) {
            discardReplacement[hand[i]] = cardKey + i;
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
    }
    
    // review the hand of cards and determine the values to keep or discard.
    protected void reviewHand() {
    	evenDistribution();
        handAnalyze();
    }

    // review each card belongs to designated range
    protected void updateGroupHand() {
        for (int i = 0; i < rackSize; i++) {
            int val = hand[i];

            if (val > rangeMax[rackSize]) {
                groupHand[i] = rackSize - 1;
                continue;
            }
            for (int j = 0; j < rackSize; j++) {
                if (val > rangeMax[j] && val <= rangeMax[j + 1]) {
                    groupHand[i] = j;
                    break;
                }
            }
        }
    }

    // using HandAnalyzer1 to determine the values to keep or discard.
    protected void handAnalyze() {
    	inUseAnalyzer.analysisNow(hand, groupHand, gapCount, discardReplacement, rangeMax);
        System.arraycopy(inUseAnalyzer.getRangeMax(), 0, rangeMax, 0, rackSize + 2);
        System.arraycopy(inUseAnalyzer.getGapCount(), 0, gapCount, 0, rackSize);
        System.arraycopy(inUseAnalyzer.getDiscard(), 0, discardReplacement, 0, cardSize + 1);
        System.arraycopy(inUseAnalyzer.getGroupHand(), 0, groupHand, 0, rackSize);

        offRange = 0;
        for (int i = 0; i < rackSize; i++) {
            if (gapCount[i] != 0) {
                offRange++;
            }
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
        if (deadlock) {
            return referenceSort(value);
        }
        reviewHand();
        secondCheck(value, isDiscardCard);
        return (choosePosition >= 0 && choosePosition < rackSize);
    }

    // Determine the use of the card to be replaced.
    protected boolean hasReplacement(byte value, boolean isDiscardCard) {
        int replacement = discardReplacement[value];

        if (replacement > -1 && replacement < rackSize) {
            choosePosition = replacement;
            return true;
        }
        if (!isDiscardCard) {
            replacement = drawReplacement[value];
            if (replacement > -1 && replacement < rackSize) {
                choosePosition = replacement;
                return true;
            }
        }
        return false;
    }

    // Demonstrate the replacement and reivew it, make sure sorted order will not reduce.
    private void secondCheck(byte value, boolean isDiscardCard) {
        boolean secondCheck = hasReplacement(value, isDiscardCard);
        if (!secondCheck) {
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
        final byte currCard = hand[replacement];
        byte tempCard = value;
        byte[] backup = new byte[rackSize];
        System.arraycopy(viewable, 0, backup, 0, rackSize);
        replaceCard(tempCard, replacement, false);
        reviewHand();
        int newOffRange = offRange;
        replaceCard(currCard, replacement, false);
        System.arraycopy(backup, 0, viewable, 0, rackSize);

        if (newOffRange <= currOffRange) {
            return;
        }
    }

    private boolean referenceSort(byte value) {
        for (int i = 0; i < rackSize; i++) {
            if (value == deadlockReference[i]) {
                choosePosition = i;
                return true;
            }
        }

        for (int i = 0; i < rackSize; i++) {
            int refValue = deadlockReference[i];
            if (hand[i] != refValue) {
                for (int j = 0; j < rackSize; j++) {
                    if (hand[j] == refValue) {
                        choosePosition = j;
                        return true;
                    }
                }
            }
        }

        for (int i = 0; i < rackSize; i++) {
            if (value < deadlockReference[i]) {
                choosePosition = i;
                return true;
            }
        }
        choosePosition = rackSize - 1;
        return true;
    }

    /**
     * Determine the slot of rack to be replace by the given card.
     *
     * @param card the byte of card value to be keep
     * @return integer the slot of rack to be replaced, -1 if ignored
     */
    public int choosePosition(byte card) {
        return choosePosition;
    }

    /**
     * Search through the player's hand and replace the new card with the
     * selected card. Throw the selected card to the discard pile.
     *
     * @param takeCard the byte of card value to keep
     * @param isDiscardCard the boolean represent the given card from discard pile
     *        or deck pile
     * @return byte the original card value to be replaced
     */
    public byte replace(byte takeCard, boolean isDiscardCard) {
        int newPos = choosePosition;
        if (choosePosition == -1) {
            return takeCard;
        }
        return replaceCard(takeCard, newPos, isDiscardCard);
    }

    /**
     * When deck pile is empty, notify the discard pile has flipped over to deck pile.
     * Compare the hand of cards with backup record.  If none of changed, activated
     * trigger to shift the card to prevent deadlock.
     */
    public void discard2deck() {
        int count = 0;
        for (int i = 0; i < rackSize; i++) {
            if (hand[i] == backupHand[i]) {
                count++;
            }
        }
        if (count > rackSize - 2 && ! deadlock) {
            deadlock = true;
            System.out.println("deadlock");
        }

        if (deadlock && count > rackSize - 3) {
            deadlockReference[0] = hand[0];
            for (int i = 1; i < rackSize; i++) {
                byte val = hand[i];
                deadlockReference[i] = val;
                for (int j = i - 1; j >= 0; j--) {
                    if (val > deadlockReference[j]) {
                        break;
                    } else {
                        deadlockReference[j + 1] = deadlockReference[j];
                        deadlockReference[j] = val;
                    }
                }
            }
        }
        System.arraycopy(hand, 0, backupHand, 0, rackSize);
    }

    protected void printDiscard() {
        for (int i = 1; i <= cardSize; i++) {
            System.out.print(discardReplacement[i] + " ");
            if (i % 10 == 0) {
                System.out.print("| ");
            }
        }
        System.out.println();
    }
}
