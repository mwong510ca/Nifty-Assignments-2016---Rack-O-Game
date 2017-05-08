package mwong.myprojects.rackocomputerplayers;


/**
 * RangeOptimizer is the tool to determine the replacement slot for cards from discard pile
 * and deck pile of Racko game.  It expand the range to sort the missing slot and keep the
 * cards in sequence for higher score.
 *
 * @author Meisze Wong
 *         www.linkedin.com/pub/macy-wong/46/550/37b/
 */
public class RangeOptimizer {
    private int cardSize;
    private int rackSize;
    private int cardKey;
    private int aveRange;

    /**
     * Initializes RangeOptimizer object.
     */
    public RangeOptimizer(int cardSize, int rackSize, int cardKey, int aveRange) {
        this.cardSize = cardSize;
        this.rackSize = rackSize;
        this.cardKey = cardKey;
        this.aveRange = aveRange;
    }

    //expand the range to sort the missing slot and keep the cards in sequence for higher score
    void optimizeNow(int offRange, boolean aggressivePlayer, byte[] hand, int[] gapCount,
            int[] rangeMax, int[] discardReplacement, int[] deckReplacement) {
        System.arraycopy(discardReplacement, 1, deckReplacement, 1, cardSize);

        for (int i = 0; i < 9; i++) {
            if (gapCount[i] == 0 && gapCount[i + 1] == 0) {
                /// ... i0 0 ....
                if (i == 0 && gapCount[2] > 0) {
                    // i0 0 n ...
                    if (hand[2] < hand[1] && hand[2] > hand[0]) {
                        for (int j = 1; j < hand[2]; j++) {
                            if (deckReplacement[j] == -1) {
                                discardReplacement[j] = 1;
                                deckReplacement[j] = 1;
                                gapCount[2]++;
                            }
                        }
                    }

                    int count = 0;
                    for (int j = hand[0] + 1; j < hand[1]; j++) {
                        if (deckReplacement[j] == -1) {
                            count++;
                        }
                    }
                    int cutoff = (int) Math.floor(count / 2);

                    for (int j = hand[0] + 1; j < hand[1]; j++) {
                        if (deckReplacement[j] == -1) {
                            if (count-- > cutoff) {
                                deckReplacement[j] = 1;
                            } else {
                                deckReplacement[j] = 2;
                            }
                        }
                    }
                } else if  (i == 8 && gapCount[7] > 0) {
                    // .... n i0 0
                    if (hand[7] > hand[8] && hand[7] < hand[9]) {
                        for (int j = hand[7] + 1; j < hand[9]; j++) {
                            if (discardReplacement[j] == -1) {
                                discardReplacement[j] = 8;
                                deckReplacement[j] = 8;
                                gapCount[7]++;
                            }
                        }
                    }

                    int count = 0;
                    for (int j = hand[8] + 1; j < hand[9]; j++) {
                        if (discardReplacement[j] == -1) {
                            count++;
                        }
                    }
                    int cutoff = (int) Math.floor(count / 2);

                    for (int j = hand[9] - 1; j > hand[8]; j--) {
                        if (discardReplacement[j] == -1) {
                            if (count-- > cutoff) {
                                deckReplacement[j] = 8;
                            } else {
                                deckReplacement[j] = 7;
                            }
                        }
                    }
                } else if (i > 0 && i < 8) {
                    if (gapCount[i - 1] > 0 && gapCount[i + 2] == 0) {
                        // .... n i0 0 _0 ....
                        if (hand[i - 1] > hand[i] && hand[i - 1] < hand[i + 1]) {
                            for (int j = hand[i] + 1; j < hand[i + 1]; j++) {
                                if (discardReplacement[j] == -1) {
                                    if (j > hand[i - 1]) {
                                        discardReplacement[j] = i;
                                        gapCount[i - 1]++;
                                    }
                                    deckReplacement[j] = i;
                                }
                            }
                        } else {
                            int count = 0;
                            for (int j = hand[i] + 1; j < hand[i + 1]; j++) {
                                if (discardReplacement[j] == -1) {
                                    count++;
                                }
                            }
                            if (count % 2 == 1) {
                                count++;
                            }
                            int cutoff = count / 2;

                            for (int j = hand[i] + 1; j < hand[i + 1]; j++) {
                                if (discardReplacement[j] == -1) {
                                    if (--count >= cutoff) {
                                        deckReplacement[j] = i - 1;
                                    } else {
                                        deckReplacement[j] = i;
                                    }
                                }
                            }
                        }
                    } else if (gapCount[i - 1] == 0 && gapCount[i + 2] > 0) {
                        // .... 0_ i0 0 n ....
                        if (hand[i + 2] > hand[i] && hand[i + 2] < hand[i + 1]) {
                            for (int j = hand[i] + 1; j < hand[i + 2]; j++) {
                                if (discardReplacement[j] == -1) {
                                    gapCount[i + 2]++;
                                    discardReplacement[j] = i + 1;
                                    deckReplacement[j] = i + 1;
                                }
                            }
                            for (int j = hand[i + 2] + 1; j < hand[i + 1]; j++) {
                                if (discardReplacement[j] == -1) {
                                    deckReplacement[j] = i + 1;
                                }
                            }
                        } else {
                            int count = 0;
                            for (int j = hand[i] + 1; j < hand[i + 1]; j++) {
                                if (discardReplacement[j] == -1) {
                                    count++;
                                }
                            }
                            int cutoff = (int) Math.floor(count / 2);

                            for (int j = hand[i] + 1; j < hand[i + 1]; j++) {
                                if (discardReplacement[j] == -1) {
                                    if (count-- > cutoff) {
                                        deckReplacement[j] = i + 1;
                                    } else {
                                        deckReplacement[j] = i + 2;
                                    }
                                }
                            }
                        }
                    } else if (gapCount[i - 1] > 0 && gapCount[i + 2] > 0) {
                        // .... n i0 0 n ....
                        int gapPlusL = 0;
                        if (hand[i - 1] > hand[i] && hand[i - 1] < hand[i + 1]) {
                            for (int j = hand[i - 1] + 1; j < hand[i + 1]; j++) {
                                if (discardReplacement[j] == -1) {
                                    discardReplacement[j] = i;
                                    deckReplacement[j] = i;
                                    gapPlusL++;
                                }
                            }
                        }
                        int gapPlusR = 0;
                        if (hand[i + 2] > hand[i] && hand[i + 2] < hand[i + 1]) {
                            for (int j = hand[i] + 1; j < hand[i + 2]; j++) {
                                if (discardReplacement[j] == -1) {
                                    discardReplacement[j] = i + 1;
                                    deckReplacement[j] = i + 1;
                                    gapPlusR++;
                                }
                            }
                        }

                        int shift = 0;
                        for (int j = rangeMax[i - 1] + 1; j < rangeMax[i + 2]; j++) {
                            if (deckReplacement[j] == -1) {
                                shift++;
                            }
                        }

                        int expandL = gapCount[i - 1];
                        int expandR = gapCount[i + 2];
                        int neighbor = hand[i] + 1;
                        while (expandR > expandL && shift > 0 && neighbor < hand[i + 1]) {
                            if (deckReplacement[neighbor] == -1) {
                                deckReplacement[neighbor] = i;
                                expandL++;
                                shift--;
                            }
                            neighbor++;
                        }
                        neighbor = hand[i + 1] - 1;
                        while (expandR < expandL && shift > 0 && neighbor > hand[i]) {
                            if (deckReplacement[neighbor] == -1) {
                                deckReplacement[neighbor] = i + 1;
                                expandR++;
                                shift--;
                            }
                            neighbor--;
                        }

                        if (shift > 0) {
                            shift /= 2;
                            for (neighbor = hand[i + 1] - 1; neighbor > hand[i]; neighbor--) {
                                if (deckReplacement[neighbor] == -1) {
                                    if (shift-- > 0) {
                                        deckReplacement[neighbor] = i + 1;
                                    } else {
                                        deckReplacement[neighbor] = i;
                                    }
                                }
                            }
                        }

                        gapCount[i - 1] += gapPlusL;
                        gapCount[i + 2] += gapPlusR;
                    }
                }
            } else if (i == 0 && gapCount[0] == 0 && gapCount[1] > 0) {
                // 0 n 0 .....
                if (hand[1] < hand[0]) {
                    for (int j = 1; j < hand[0]; j++) {
                        if (discardReplacement[j] == -1) {
                            discardReplacement[j] = 0;
                            deckReplacement[j] = 0;
                            gapCount[1]++;
                        }
                    }
                }

                if (hand[1] < hand[0] || gapCount[2] == 0) {
                    int count = 0;
                    for (int j = 1; j < hand[0]; j++) {
                        if (discardReplacement[j] == -1) {
                            count++;
                        }
                    }
                    int cutoff = (int) Math.floor(count / 2);

                    for (int j = 1; j < hand[0]; j++) {
                        if (discardReplacement[j] == -1) {
                            if (--count >= cutoff) {
                                deckReplacement[j] = 0;
                            } else {
                                deckReplacement[j] = 1;
                            }
                        }
                    }
                }
            } else if (i == 8 && gapCount[8] > 0 && gapCount[9] == 0
                    && gapCount[7] == 0) {
                // .... 0 n 0
                int count = 0;
                for (int j = hand[9] + 1; j <= cardSize; j++) {
                    if (discardReplacement[j] == -1) {
                        count++;
                    }
                }
                int cutoff = (int) Math.floor(count / 2);

                for (int j = cardSize; j > hand[9]; j--) {
                    if (discardReplacement[j] == -1) {
                        if (count-- > cutoff) {
                            deckReplacement[j] = 9;
                        } else {
                            deckReplacement[j] = 8;
                        }
                    }
                }
            }
        }
        if (gapCount[9] == 0 && hand[8] > hand[9]) {
            for (int i = hand[8] + 1; i <= cardSize; i++) {
                if (discardReplacement[i] == -1) {
                    discardReplacement[i] = 9;
                    deckReplacement[i] = 9;
                    gapCount[8]++;
                }
            }
        }

        for (int i = 1; i <= 8; i++) {
            if (gapCount[i] > 0 && gapCount[i] < 3 && gapCount[i - 1] == 0
                    && gapCount[i + 1] == 0) {
                int expand = gapCount[i];
                for (int j = rangeMax[i - 1] + 1; j < rangeMax[i]; j++) {
                    discardReplacement[j] = deckReplacement[j];
                    if (discardReplacement[j] == -1) {
                        discardReplacement[j] = i - 1;
                        deckReplacement[j] = i - 1;
                        expand++;
                    }
                }
                for (int j = rangeMax[i + 1] + 1; j < rangeMax[i + 2]; j++) {
                    discardReplacement[j] = deckReplacement[j];
                    if (discardReplacement[j] == -1) {
                        discardReplacement[j] = i + 1;
                        deckReplacement[j] = i + 1;
                        expand++;
                    }
                }

                if (offRange < 3) {
                    if (i - 2 < 0) {
                        for (int j = 1; j < rangeMax[i - 1]; j++) {
                            discardReplacement[j] = deckReplacement[j];
                            if (discardReplacement[j] == -1 && expand < 6) {
                                discardReplacement[j] = 0;
                                deckReplacement[j] = 0;
                            }
                        }
                    } else {
                        for (int j = rangeMax[i - 2] + 1; j < rangeMax[i - 1]; j++) {
                            discardReplacement[j] = deckReplacement[j];
                            if (discardReplacement[j] == -1 && expand < 6) {
                                discardReplacement[j] = i - 2;
                                deckReplacement[j] = i - 2;
                            }
                        }
                    }

                    for (int j = rangeMax[i + 2] + 1; j < rangeMax[i + 3]; j++) {
                        discardReplacement[j] = deckReplacement[j];
                        if (discardReplacement[j] == -1 && expand < 6) {
                            discardReplacement[j] = i + 2;
                            deckReplacement[j] = i + 2;
                        }
                    }
                }
            }
        }

        // shift first and last to increase chance.
        int pos = -1;
        for (int i = 0; i < rackSize; i++) {
            if (gapCount[i] > 0) {
                break;
            } else {
                pos = i;
            }
        }
        if (pos > -1) {
            int expand = gapCount[pos + 1];
            for (int i = pos; i >= 0 && expand < 6; i--) {
                if (i > 0 && hand[i] - 1 == hand[i - 1]) {
                    break;
                }
                if (expand < 6) {
                    if (pos == i) {
                        int count = 0;
                        for (int j = rangeMax[i] + 1; j < rangeMax[i + 1]; j++) {
                            if (discardReplacement[j] < cardKey) {
                                expand++;
                                count++;
                            }
                        }

                        int cutoff = (int) Math.floor(count / 2);
                        for (int j = rangeMax[i] + 1; j < rangeMax[i + 1]; j++) {
                            if (deckReplacement[j] == -1) {
                                if (count < cutoff) {
                                    deckReplacement[j] = i + 1;
                                } else {
                                    deckReplacement[j] = i;
                                }
                                if (offRange < 3) {
                                    discardReplacement[j] = deckReplacement[j];
                                }
                            } else {
                                if (offRange < 3) {
                                    discardReplacement[j] = deckReplacement[j];
                                }
                            }

                            if (discardReplacement[j] < cardKey) {
                                count--;
                            }
                        }
                    } else {
                        for (int j = rangeMax[i] + 1; j < rangeMax[i + 1]; j++) {
                            if (discardReplacement[j] < cardKey) {
                                expand++;
                            }
                            if (deckReplacement[j] == -1) {
                                deckReplacement[j] = i;
                                if (offRange < 3) {
                                    discardReplacement[j] = i;
                                }
                            } else {
                                if (offRange < 3) {
                                    discardReplacement[j] = deckReplacement[j];
                                }
                            }
                        }
                    }
                }
            }
        }

        pos = -1;
        for (int i = 9; i < rackSize; i--) {
            if (gapCount[i] > 0) {
                break;
            } else {
                pos = i;
            }
        }
        if (pos > -1) {
            int expand = gapCount[pos - 1];
            for (int i = pos; i <= 9 && expand < 6; i++) {
                if (i < 9 && hand[i] + 1 == hand[i + 1]) {
                    break;
                }
                if (expand < 6) {
                    if (i == pos) {
                        int count = 0;
                        for (int j = rangeMax[i + 1] + 1; j <= rangeMax[i + 2]; j++) {
                            if (discardReplacement[j] < cardKey) {
                                expand++;
                                count++;
                            }
                        }

                        int cutoff = (int) Math.floor(count / 2);
                        for (int j = rangeMax[i + 1] + 1; j <= rangeMax[i + 2]; j++) {
                            if (deckReplacement[j] == -1) {
                                if (count <= cutoff) {
                                    deckReplacement[j] = i;
                                } else {
                                    deckReplacement[j] = i - 1;
                                }

                                if (offRange < 3) {
                                    discardReplacement[j] = deckReplacement[j];
                                }
                            } else {
                                if (offRange < 3) {
                                    discardReplacement[j] = deckReplacement[j];
                                }
                            }

                            if (discardReplacement[j] < cardKey) {
                                count--;
                            }
                        }
                    } else {
                        for (int j = rangeMax[i + 1] + 1; j <= rangeMax[i + 2]; j++) {
                            if (discardReplacement[j] < cardKey) {
                                expand++;
                            }
                            if (deckReplacement[j] == -1) {
                                deckReplacement[j] = i;
                                if (offRange < 3) {
                                    discardReplacement[j] = i;
                                }
                            } else {
                                if (offRange < 3) {
                                    discardReplacement[j] = deckReplacement[j];
                                }
                            }
                        }
                    }
                }
            }
        }

        // add sequence for higher scores ----------------------------------------*/
        int maxSeq = 1;
        for (int i = 0; i < 9;) {
            int max = 1;
            for (int j = i + 1; j < 10; j++) {
                i++;
                if (hand[j] == hand[j - 1] + 1) {
                    max++;
                } else {
                    break;
                }
            }
            if (max > maxSeq) {
                maxSeq = max;
            }
        }
        if (maxSeq <= 2) {
            maxSeq = 0;
        }

        if (maxSeq < 6) {
            for (int i = 0; i < rackSize; i++) {
                if (gapCount[i] == 0) {
                    //  x x x x x x n x x x x x x
                    //               n+1
                    if (i < rackSize - 1 && discardReplacement[hand[i] + 1] < rackSize) {
                        if (deckReplacement[hand[i] + 1] == -1) {
                            if (gapCount[i + 1] > aveRange) {
                                deckReplacement[hand[i] + 1] = i + 1;
                            } else if (gapCount[i + 1] == 0) {
                                int seqL = 1;
                                for (int j = i - 1; j >= 0; j--) {
                                    if (gapCount[j] == 0 && hand[j] + 1 == hand[j + 1]) {
                                        seqL++;
                                    } else {
                                        break;
                                    }
                                }

                                if (seqL >= maxSeq) {
                                    if (i == 8)  {
                                        if (seqL > 2 && aggressivePlayer) {
                                            discardReplacement[hand[i] + 1] = i + 1;
                                        }
                                        deckReplacement[hand[i] + 1] = i + 1;
                                    } else {
                                        if (hand[i + 1] + 1 == hand[i + 2]) {
                                            int seqR = 1;
                                            for (int j = i + 2; j < rackSize; j++) {
                                                if (gapCount[j] == 0
                                                        && hand[j] - 1 == hand[j - 1]) {
                                                    seqR++;
                                                }
                                            }
                                            if (seqL >= seqR) {
                                                if (seqL > 2 && aggressivePlayer) {
                                                    discardReplacement[hand[i] + 1] = i + 1;
                                                }
                                                deckReplacement[hand[i] + 1] = i + 1;
                                            }
                                        } else {
                                            if (seqL > 2 && aggressivePlayer) {
                                                discardReplacement[hand[i] + 1] = i + 1;
                                            }
                                            deckReplacement[hand[i] + 1] = i + 1;
                                        }
                                    }
                                }
                            }
                        }
                    }

                    //  x x x x x x n x x x x x x
                    //           n-1
                    if (i > 0 && discardReplacement[hand[i] - 1] < rackSize) {
                        if (deckReplacement[hand[i] - 1] == -1) {
                            if (gapCount[i - 1] > aveRange) {
                                //discardReplacement[value[i] - 1] = i - 1;
                                deckReplacement[hand[i] - 1] = i - 1;
                            } else if (gapCount[i - 1] == 0) {
                                int seqR = 1;
                                for (int j = i + 1; j < rackSize; j++) {
                                    if (gapCount[j] == 0 && hand[j] - 1 == hand[j - 1]) {
                                        seqR++;
                                    } else {
                                        break;
                                    }
                                }
                                if (seqR >= maxSeq) {
                                    if (i == 1) {
                                        if (seqR > 2 && aggressivePlayer) {
                                            discardReplacement[hand[i] - 1] = i - 1;
                                        }
                                        deckReplacement[hand[i] - 1] = i - 1;
                                    } else {
                                        // 4 5 7 8 .... i = 1
                                        if (hand[i - 2] + 1 == hand[i - 1]) {
                                            int seqL = 1;
                                            for (int j = i - 2; j >= 0; j--) {
                                                if (gapCount[j] == 0
                                                        && hand[j] + 1 == hand[j + 1]) {
                                                    seqL++;
                                                }
                                            }
                                            if (seqR >= seqL) {
                                                if (seqR > 2 && aggressivePlayer) {
                                                    discardReplacement[hand[i] - 1] = i - 1;
                                                }
                                                deckReplacement[hand[i] - 1] = i - 1;
                                            }
                                        } else {
                                            if (seqR > 2 && aggressivePlayer) {
                                                discardReplacement[hand[i] - 1] = i - 1;
                                            }
                                            deckReplacement[hand[i] - 1] = i - 1;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // expand the range to sort the missing slot exclude the sequence of cards,
    // until enough chances to fill the solt.
    void optimizeMore(int aveRange, byte[] hand, int[] gapCount, int[] discardReplacement,
            int[] deckReplacement) {
        int origin = 0;
        for (int i = 0; i < rackSize; i++) {
            if (gapCount[i] > 0) {
                origin = i;
                break;
            }
        }
        int[] expansion = new int[rackSize];
        int lo = 1;
        for (int i = 0; i < rackSize; i++) {
            if (i == origin) {
                break;
            }
            for (int j = lo; j < hand[i]; j++) {
                if (deckReplacement[j] == -1 || (deckReplacement[j] >= 0
                        && deckReplacement[j] < cardKey)) {
                    expansion[i]++;
                }
            }
            lo = hand[i] + 1;
        }
        int hi = cardSize;
        for (int i = rackSize - 1; i > origin; i--) {
            if (i == origin) {
                break;
            }

            for (int j = hi; j > hand[i]; j--) {
                if (deckReplacement[j] == -1 || (deckReplacement[j] > 0
                        && deckReplacement[j] < cardKey)) {
                    expansion[i]++;
                }
            }
            hi = hand[i] - 1;
        }

        int count = gapCount[origin];
        int shift = 0;
        while (count < aveRange * 2 && shift < rackSize) {
            shift++;
            int base = origin - shift;
            if (base >= 0 && expansion[base] > 0) {
                count += expansion[base];
                int start = 1;
                if (base > 0) {
                    start = hand[base - 1] + 1;
                }
                for (int i = start; i < hand[base]; i++) {
                    if (deckReplacement[i] == -1 || deckReplacement[i] > 0) {
                        deckReplacement[i] = base;
                    }
                    if (shift > 1 && deckReplacement[i] >= 0 && deckReplacement[i] < cardKey) {
                        deckReplacement[i] = base;
                    }
                }
            }
            base = origin + shift;
            if (base < rackSize && expansion[base] > 0) {
                count += expansion[base];
                int start = cardSize;
                if (base >= 0 && base < rackSize - 1) {
                    start = hand[base + 1] - 1;
                }
                for (int i = start; i > hand[base]; i--) {
                    if (deckReplacement[i] == -1) {
                        deckReplacement[i] = base;
                    }
                    if (shift > 1 && deckReplacement[i] > 0 && deckReplacement[i] < cardKey) {
                        deckReplacement[i] = base;
                    }
                }
            }
        }
    }
}
