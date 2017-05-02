package mwong.myprojects.rackocomputerplayers;

/**
 * Player1 extends AbstractPlayer with computer strategy from Nifty assignment 2016.
 * It review the initial hand of card, find the best sorted order then fill the missing
 * slots.
 *
 * <p>Dependencies : AbstractPlayer.java
 *
 * @author Arvind Bhusnurmath, Kristen Gee, and Karen Her
 *         copied from Nifty assignment 2016, Racko
 *         http://nifty.stanford.edu/2016/arvind-racko/
 * @modified by Meisze Wong to matched the data structure
 */
public class Player1 extends AbstractPlayer {
    private int[] bestAscendingHand;
    private int choosePosition;

    /**
     * Initializes Player1 object.
     */
    public Player1(int size) {
        super(size);
    }

    /**
     * Returns a string representation the difficulty level of Player1.
     *
     * @return a string representation the difficulty level of Player1
     */
    public String toString() {
        return "Easy";
    }

    /*
     * ------------------------------ COMPUTER STRATEGY ----------------------
     */

    /**
     * Determine if there are enough cards that can go between the two cards.
     *
     * @param hi - index of the second card
     * @param lo - index of the first card
     * @param higherVal - value of the higher card
     * @param lowerVal - value of the lower card
     * @return true if there are enough cards that can go between the two cards
     */
    private boolean check_if_possible(int hi, int lo, int higherVal, int lowerVal) {
        // Checks if there are enough possible cards to put between the
        // lower valued card and higher valued card
        if (hi - lo > higherVal - lowerVal) {
            return false;
        }
        return true;
    }

    /*
     * Determine which cards in the current hand should be kept. Mark the cards
     * to be replaced with -1 and return the hand. The cards are compared by which
     * card has the greater range of values.
     */
    private void find_ascending_hand() {
        for (int i = 0; i < 10; i++) {
            if ((i >= 0 && i < (9 - (cardSize - hand[i])))
                    || (i <= 9 && hand[i] < i + 1)) {
                bestAscendingHand[i] = -1;
            } else {
                bestAscendingHand[i] = hand[i];
            }
        }

        int[] ascendingHand = new int[10];
        // Index of the lowest valued card kept at a time
        int loIndex = 0;
        // loIndex + 1 to remain noninclusive of that index
        int lower = 0;
        // Value of the card at the loIndex but incremented by one to remain noninclusive
        int lowerVal = 0;

        // Make the array take in all the values
        for (int a = 0; a < bestAscendingHand.length; a++) {
            ascendingHand[a] = bestAscendingHand[a];
        }

        // Go through every card except the last card and compare it with all other cards
        // after it that are lower
        for (int i = 0; i < 9; i++) {
            // If the card is already marked for removal, don't check it
            if (ascendingHand[i] == -1) {
                continue;
            } else {
                // Determine whether the card can be kept based on what the previous
                // card's value and position
                if (!check_if_possible(i, lower, bestAscendingHand[i], lowerVal)) {
                    ascendingHand[i] = -1;
                    continue;
                }

                for (int j = i + 1; j < 10; j++) {
                    // Check cards that have lower values than the current card
                    if (bestAscendingHand[j] < bestAscendingHand[i] && ascendingHand[j] != -1) {
                        // Check if there are more spaces between the card and the end
                        // or more spaces between the card and the last card that was kept
                        // This comparison is done with both the current card and the card
                        // it is being compared with
                        if (j - lower >= 9 - j) {
                            // More cards between the card and the last KEPT card
                            if (i - lower >= 9 - i) {
                                // More cards between the card and the last KEPT card

                                // Check if the current card or the other card has a
                                // higher range; if the other card is better, make the
                                // current card -1, otherwise keep the current card
                                // and mark the other card -1
                                if (bestAscendingHand[j] - bestAscendingHand[loIndex]
                                        > bestAscendingHand[i] - bestAscendingHand[loIndex]) {
                                    //Check whether other card has a possible place in the hand
                                    if (check_if_possible(j, lower, bestAscendingHand[j],
                                            lowerVal)) {
                                        ascendingHand[i] = -1;
                                        break;
                                    } else {
                                        ascendingHand[i] = bestAscendingHand[i];
                                        ascendingHand[j] = -1;
                                    }
                                } else {
                                    ascendingHand[i] = bestAscendingHand[i];
                                    ascendingHand[j] = -1;
                                }
                            } else if (i - lower < 9 - i) {
                                // More cards between the card and the last card
                                if (bestAscendingHand[j] - bestAscendingHand[loIndex]
                                        > cardSize - bestAscendingHand[i]) {
                                    //Check whether other card has a possible place in the hand
                                    if (check_if_possible(j, lower, bestAscendingHand[j],
                                            lowerVal)) {
                                        ascendingHand[i] = -1;
                                        break;
                                    } else {
                                        ascendingHand[i] = bestAscendingHand[i];
                                        ascendingHand[j] = -1;
                                    }
                                } else {
                                    ascendingHand[i] = bestAscendingHand[i];
                                    ascendingHand[j] = -1;
                                }
                            }
                        } else if (j - lower < 9 - j) {
                            // More cards between the card and the last card
                            if (i - lower >= 9 - i) {
                                // More cards between the card and the last KEPT card
                                if (cardSize - bestAscendingHand[j]
                                        > bestAscendingHand[i] - bestAscendingHand[loIndex]) {
                                    //Check whether other card has a possible place in the hand
                                    if (check_if_possible(j, lower, bestAscendingHand[j],
                                            lowerVal)) {
                                        ascendingHand[i] = -1;
                                        break;
                                    } else {
                                        ascendingHand[i] = bestAscendingHand[i];
                                        ascendingHand[j] = -1;
                                    }
                                } else {
                                    ascendingHand[i] = bestAscendingHand[i];
                                    ascendingHand[j] = -1;
                                }
                            } else if (i - lower < 9 - i) {
                                // More cards between the card and the last card
                                if (cardSize - bestAscendingHand[j]
                                        > cardSize - bestAscendingHand[i]) {
                                    //Check whether other card has a possible place in the hand
                                    if (check_if_possible(j, lower, bestAscendingHand[j],
                                            lowerVal)) {
                                        ascendingHand[i] = -1;
                                        break;
                                    } else {
                                        ascendingHand[i] = bestAscendingHand[i];
                                        ascendingHand[j] = -1;
                                    }
                                } else {
                                    ascendingHand[i] = bestAscendingHand[i];
                                    ascendingHand[j] = -1;
                                }
                            }
                        }
                    }
                }

                // If the current card was not marked for replacement, increment the lower
                // index variables to account for the current card being the latest addition
                // to the best ascending hand
                if (ascendingHand[i] != -1) {
                    loIndex = i;
                    lower = i + 1;
                    lowerVal = ascendingHand[i] + 1;
                }
            }
        }
        // Make sure that the last card in the hand can be used
        if (!check_if_possible(9, lower, bestAscendingHand[9], lowerVal)) {
            ascendingHand[9] = -1;
        }

        for (int i = 0; i < 10; i++) {
            bestAscendingHand[i] = ascendingHand[i];
        }
    }

    /*
     * Check that the card is within range.
     */
    private boolean within_range(int drawCard, int hi, int lo) {
        if (drawCard < hi && drawCard > lo) {
            return true;
        }
        return false;
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
        bestAscendingHand = new int[10];
        find_ascending_hand();

        int slot = 0;
        int lo = 0;
        int lowerIndex = 0;
        int hi = 0;
        while (slot < 10) {
            // Look for cards that are to be kept and determine whether there are
            // cards that need to be replaced between the two
            int numOfCards = 0;
            if (bestAscendingHand[slot] != -1) {
                hi = slot;
                numOfCards = hi - lowerIndex;
                lowerIndex = slot + 1;
            } else {
                if (slot == 9) {
                    hi = 9;
                    numOfCards = hi + 1 - lowerIndex;
                }
            }
            // Determine whether there are cards that need to be replaced between the
            // two cards and if the current card's value falls between the two cards'
            if (numOfCards != 0) {
                if (bestAscendingHand[lo] != -1 && bestAscendingHand[hi] != -1) {
                    if (within_range(value, bestAscendingHand[hi], bestAscendingHand[lo])) {
                        choosePosition = load_choose_position(value);
                        return true;
                    }
                } else if ((bestAscendingHand[lo] == -1
                        && within_range(value, bestAscendingHand[hi], 0))
                        || (bestAscendingHand[hi] == -1
                        && within_range(value, cardSize + 1, bestAscendingHand[lo]))) {
                    // Check looking at the cards from 0 to the current card's value or the
                    // current card's value to cardSize
                    choosePosition = load_choose_position(value);
                    return true;
                }
            }

            // Update to check for the next range
            if (bestAscendingHand[slot] != -1) {
                lo = slot;
            }
            slot++;
        }
        return false;
    }

    /*
     * Choose a position for the card to be placed. This is assuming that it has
     * already been determined that the card can be used. Look for the range it falls
     * in and place the card next to the card with the closer value.
     */
    private int load_choose_position(int card) {
        int slot = 0;
        int lo = 0;
        int hi = 0;
        while (slot < 10) {
            if (bestAscendingHand[slot] != -1) {
                hi = slot;
                if (lo == 0 && bestAscendingHand[lo] == -1) {
                    if (within_range(card, bestAscendingHand[hi], 0)) {
                        // If the card is closer in value to the higher card,
                        // return the index of the card behind the higher card
                        if (bestAscendingHand[hi] - card < card) {
                            bestAscendingHand[hi - 1] = card;
                            return hi - 1;
                        } else {
                            bestAscendingHand[0] = card;
                            return 0;
                        }
                    }
                } else {
                    if (within_range(card, bestAscendingHand[hi], bestAscendingHand[lo])) {
                        // If the card is closer in value to the lower card,
                        // return the index of the card in front the lower card
                        if (bestAscendingHand[hi] - card
                                < card - bestAscendingHand[lo]) {
                            bestAscendingHand[hi - 1] = card;
                            return hi - 1;
                        } else {
                            bestAscendingHand[lo + 1] = card;
                            return lo + 1;
                        }
                    }
                }
            // Determine whether the card should be in the last slot or closer to the
            // lower card
            } else if (slot == 9 && bestAscendingHand[9] == -1) {
                if (within_range(card, cardSize + 1, bestAscendingHand[lo])) {

                    if (cardSize - card < card - bestAscendingHand[lo]) {
                        bestAscendingHand[9] = card;
                        return 9;
                    } else {
                        bestAscendingHand[lo + 1] = card;
                        return lo + 1;
                    }
                }
            }

            // Update to check for the next range
            if (bestAscendingHand[slot] != -1) {
                lo = slot;
            }
            slot++;
        }
        return -1;
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
        //int newPos = choose_position(takeCard);
        return replaceCard(takeCard, choosePosition, isDiscardCard);
    }
}
