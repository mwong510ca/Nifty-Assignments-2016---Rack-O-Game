package mwong.myprojects.rackocomputerplayers;


/**
 * HandAnalyzer3 extends AbstractHandAnalyzer with main analysis function.
 *
 * <p>Dependencies : AbstractHandAnalyzer.java
 *
 * @author Meisze Wong
 *         www.linkedin.com/pub/macy-wong/46/550/37b/
 */
public class HandAnalyzer2 extends AbstractHandAnalyzer {
    /**
     * Initializes HandAnalyzer2 object.
     */
    public HandAnalyzer2(int cardSize, int rackSize, int cardKey) {
        super(cardSize, rackSize, cardKey);
        rangeMax = new int[rackSize + 2];
        gapCount = new int[rackSize];
        discard = new int[cardSize + 1];
        orgDiscard = new int[cardSize + 1];
        groupHand = new int[rackSize];
    }

    /**
     * Loop the following until no change.
     * 1. If the card fall in assigned range, change the range and even out.
     * 2. If the card fall in next assigned range, change the range and even out.
     * 3. If the card fall in previous assigned range, change the range and even out.
     */
    void analysis(byte[] hand) {
        int loopCount = 0;
        while (true) {
            if (loopCount++ == rackSize) {
                break;
            }

            boolean[] scan = new boolean[rackSize];
            for (int i = 0; i < rackSize; i++) {
                if (gapCount[i] == 0) {
                    scan[i] = true;
                }
            }

            int reviewGroup = -1;
            while (true) {
                reviewGroup = -1;
                for (int i = 0; i < rackSize; i++) {
                    int group = groupHand[i];
                    if (i == group && group == discard[hand[i]] % cardKey) {
                        if (!scan[group]) {
                            reviewGroup = group;
                            rangeMax[i + 1] = hand[i];
                            if (i > 0 && gapCount[i - 1] > 0) {
                                rangeMax[i] = hand[i] - 1;
                            }
                            break;
                        }
                    }
                }

                if (reviewGroup == -1) {
                    for (int i = rackSize - 1; i >= 0; i--) {
                        if (!scan[i]) {
                            int group = i;
                            for (int j = rangeMax[i + 1] + 1; j < rangeMax[i + 2]; j++) {
                                if (discard[j] > rackSize) {
                                    int pos = 0;
                                    while (hand[pos] != j) {
                                        pos++;
                                    }
                                    if (pos == i) {
                                        groupHand[discard[j] % cardKey] = i;
                                        rangeMax[i + 1] = j;
                                        if (i > 0 && gapCount[i - 1] > 0) {
                                            rangeMax[i] = j - 1;
                                        }
                                        reviewGroup = group;
                                        break;
                                    }
                                } else {
                                    break;
                                }
                            }
                        }
                    }
                }

                if (reviewGroup == -1) {
                    for (int i = rackSize - 2; i > 0; i--) {
                        if (!scan[i] && gapCount[i - 1] > 0) {
                            int target = cardKey + i - 1;
                            for (int j = rangeMax[i] + 1; j <= rangeMax[i + 1]; j++) {
                                if (discard[j] == target) {
                                    int shift = 0;
                                    for (int k = j + 1; k <= rangeMax[i + 1]; k++) {
                                        if (discard[k] < cardKey) {
                                            if (discard[k] == i) {
                                                shift++;
                                            }
                                        }
                                    }

                                    if (shift < 1) {
                                        continue;
                                    }

                                    if (i > 1 && gapCount[i - 2] > 0) {
                                        rangeMax[i - 1] = j - 1;
                                        rangeMax[i] = j;
                                    } else {
                                        rangeMax[i] = j;
                                    }
                                    reviewGroup = i - 1;
                                    scan[i - 1] = true;
                                    break;
                                }
                            }

                            if (reviewGroup != -1) {
                                break;
                            }
                        }
                    }
                }

                if (reviewGroup == -1) {
                    for (int i = 1; i < rackSize - 1; i++) {
                        if (!scan[i] && gapCount[i + 1] > 0) {
                            int target = cardKey + i + 1;
                            for (int j = rangeMax[i] + 1; j <= rangeMax[i + 1]; j++) {
                                if (discard[j] == target) {
                                    int shift = 0;
                                    for (int k = 1; k < j; k++) {
                                        if (discard[k] < cardKey) {
                                            if (discard[k] == i) {
                                                shift++;
                                            }
                                        }
                                    }
                                    if (shift < 1) {
                                        continue;
                                    }

                                    rangeMax[i + 1] = j - 1;
                                    rangeMax[i + 2] = j;

                                    reviewGroup = i + 1;
                                    scan[i + 1] = true;
                                    break;
                                }
                            }

                            if (reviewGroup != -1) {
                                break;
                            }
                        }
                    }
                }

                if (reviewGroup == -1) {
                    break;
                }

                int val = hand[reviewGroup];

                int updatevalue = reviewGroup + 1;
                if (updatevalue == rackSize || gapCount[reviewGroup + 1] == 0) {
                    updatevalue = -1;
                }

                for (int i = val + 1; i <= rangeMax[reviewGroup + 2]; i++) {
                    if (discard[i] > -1 && discard[i] < cardKey) {
                        discard[i] = updatevalue;
                        gapCount[reviewGroup]--;
                        if (updatevalue != -1) {
                            gapCount[updatevalue]++;
                        }
                    } else if (updatevalue > 0 && discard[i] >= cardKey) {
                        int group = discard[i] % cardKey;
                        if (reviewGroup < rackSize - 1) {
                            groupHand[group] = reviewGroup + 1;
                        } else {
                            groupHand[group] = reviewGroup;
                        }
                        scan[updatevalue] = false;
                    }
                }

                updatevalue = reviewGroup - 1;
                if (reviewGroup == 0 || gapCount[reviewGroup - 1] == 0) {
                    updatevalue = -1;
                }

                int begin2 = 0;
                if (reviewGroup > 0) {
                    begin2 = rangeMax[reviewGroup - 1];
                }
                for (int i = val - 1; i > begin2; i--) {
                    if (discard[i] > -1 && discard[i] < cardKey) {
                        discard[i] = updatevalue;
                        gapCount[reviewGroup]--;
                        if (updatevalue != -1) {
                            gapCount[updatevalue]++;
                        }
                    } else if (updatevalue >= 0 && discard[i] >= cardKey) {
                        int group = discard[i] % cardKey;
                        if (reviewGroup > 0) {
                            groupHand[group] = reviewGroup - 1;
                        } else {
                            groupHand[group] = reviewGroup;
                        }
                        scan[updatevalue] = false;
                    }
                }

                scan[reviewGroup] = true;

                gapCount = new int[rackSize];
                for (int i = 1; i <= cardSize; i++) {
                    if (discard[i] > -1 && discard[i] < rackSize) {
                        gapCount[discard[i]]++;
                    }
                }
            }

            boolean completed = true;
            int slot = 0;

            while (slot < rackSize - 1) {
                if (gapCount[slot] == 0) {
                    slot++;
                    continue;
                }

                int min = gapCount[slot];
                int max = gapCount[slot];
                int total = gapCount[slot];
                int begin = slot;
                for (int j = begin + 1; j < rackSize; j++) {
                    int val = gapCount[j];
                    if (val > 0) {
                        total += val;
                        if (val < min) {
                            min = val;
                        }
                        if (val > max) {
                            max = val;
                        }
                    }

                    if (val == 0) {
                        if (max > min + 1) {
                            completed = false;
                            int aveMin = total / (j - begin);
                            int aveMax = aveMin + 1;
                            int numAveMax = total - aveMin * (j - begin);
                            int group = begin;
                            int neighbor = rangeMax[group] + 1;
                            while (total > 0) {
                                int count = aveMin;
                                if (numAveMax > 0) {
                                    count = aveMax;
                                }
                                total -= count;
                                gapCount[group] = count;
                                while (count > 0) {
                                    if (discard[neighbor] < rackSize && discard[neighbor] > -2) {
                                        discard[neighbor] = group;
                                        count--;
                                    } else {
                                        for (int pos = 0; pos < rackSize; pos++) {
                                            if (hand[pos] == neighbor) {
                                                groupHand[pos] = group;
                                                break;
                                            }
                                        }
                                    }
                                    neighbor++;
                                }
                                rangeMax[group + 1] = neighbor - 1;
                                group++;
                                numAveMax--;
                            }

                            while (discard[neighbor] > rackSize
                                    && discard[neighbor] != cardKey + j) {
                                rangeMax[group] = neighbor;
                                neighbor++;
                            }
                        }
                        slot = j + 1;
                        break;
                    }

                    // reach the last one...
                    if (j == rackSize - 1) {
                        if (max > min + 1) {
                            completed = false;
                            int aveMin = total / (j - begin + 1);
                            int aveMax = aveMin + 1;
                            int numAveMax = total - aveMin * (j - begin + 1);
                            int group = rackSize - 1;
                            int neighbor = cardSize;
                            while (total > 0) {
                                rangeMax[group + 1] = neighbor;
                                int count = aveMin;
                                if (numAveMax > 0) {
                                    count = aveMax;
                                }
                                total -= count;
                                gapCount[group] = count;
                                while (count > 0) {
                                    if (discard[neighbor] < rackSize && discard[neighbor] > -2) {
                                        discard[neighbor] = group;
                                        count--;
                                    } else {
                                        for (int pos = 0; pos < rackSize; pos++) {
                                            if (hand[pos] == neighbor) {
                                                groupHand[pos] = group;
                                                break;
                                            }
                                        }
                                    }
                                    neighbor--;
                                }

                                while (discard[neighbor] > rackSize
                                        && neighbor > rangeMax[begin - 1] + 1) {
                                    for (int pos = 0; pos < rackSize; pos++) {
                                        if (hand[pos] == neighbor) {
                                            if (group > 0 && gapCount[group - 1] == 0) {
                                                groupHand[pos] = group - 1;
                                            } else {
                                                groupHand[pos] = group;
                                            }

                                            neighbor--;
                                            break;
                                        }
                                    }
                                }
                                group--;
                                numAveMax--;
                            }
                        }
                        slot = j;
                        break;
                    }
                }
            }

            if (completed) {
                break;
            }
        }
    }
}
