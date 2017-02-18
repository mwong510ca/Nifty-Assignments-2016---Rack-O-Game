
public class Player2 implements Player {
    private Card[] hand;
    private int[] groupHand, groupHand1a, groupHand1b;
    private int[] gapCount, gapCount1a, gapCount1b;
    private int[] discardReplacement, discard1a, discard1b, drawReplacement;
    private int[] rangeMax, rangeMax1a, rangeMax1b;
    private byte[] viewable;
    private final int cardSize;
    private final int rackSize = 10;
    private final int cardKey = 100;
    private final int aveRange;
    private int offRange;
    private int choosePosition;

    public Player2(int size) {
        cardSize = size;
        aveRange = cardSize / rackSize;
    }

    public void setHand(Card[] hand) {
        this.viewable = new byte[hand.length];
        this.hand = new Card[hand.length];
        for (int i = 0; i < hand.length; i++) {
            this.hand[i] = hand[i];
        }
        reviewHand();
    }

    public final Card[] getHand() {
        return hand;
    }
    
    public void reviewHand() {
        boolean[] possibleHand = new boolean[cardSize + 1];
        rangeMax = new int[rackSize + 2];
        gapCount = new int[rackSize];
        for (int i = 1; i <= cardSize; i++) {
            possibleHand[i] = true;
        }
        int countMax = aveRange - 1;
        int[] value = new int[rackSize];
        for (int i = 0; i < rackSize; i++) {
            value[i] = hand[i].getValue();
            possibleHand[value[i]] = false;
            gapCount[i] = countMax;
        }
        
        discardReplacement = new int[cardSize + 1];
        
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
        level1review(value);
        if (offRange > 0)
            level2review(value);
    } 

    private void updateGroupHand() {
        for (int i = 0; i < rackSize; i++) {
            int val = hand[i].getValue();
            
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
    
    private void level1review(int[] value) {
        rangeMax1a = new int[rackSize + 2];
        gapCount1a = new int[rackSize];
        discard1a = new int[cardSize + 1];
        groupHand1a = new int[rackSize];
        System.arraycopy(rangeMax, 0, rangeMax1a, 0, rackSize + 2);
        System.arraycopy(gapCount, 0, gapCount1a, 0, rackSize);
        System.arraycopy(discardReplacement, 0, discard1a, 0, cardSize + 1);
        System.arraycopy(groupHand, 0, groupHand1a, 0, rackSize);        
        review1a(value);
        
        rangeMax1b = new int[rackSize + 2];
        gapCount1b = new int[rackSize];
        discard1b = new int[cardSize + 1];
        groupHand1b = new int[rackSize];
        System.arraycopy(rangeMax, 0, rangeMax1b, 0, rackSize + 2);
        System.arraycopy(gapCount, 0, gapCount1b, 0, rackSize);
        System.arraycopy(discardReplacement, 0, discard1b, 0, cardSize + 1);
        System.arraycopy(groupHand, 0, groupHand1b, 0, rackSize);        
        review1b(value); 
        
        int count1a = 0;
        for (int i = 0; i < rackSize; i++) {
            if (gapCount1a[i] == 0)
                count1a++;
        }
        int count1b = 0;
        for (int i = 0; i < rackSize; i++) {
            if (gapCount1b[i] == 0)
                count1b++;
        }

        if (count1a > count1b) {
            System.arraycopy(rangeMax1a, 0, rangeMax, 0, rackSize + 2);
            System.arraycopy(gapCount1a, 0, gapCount, 0, rackSize);
            System.arraycopy(discard1a, 0, discardReplacement, 0, cardSize + 1);
            System.arraycopy(groupHand1a, 0, groupHand, 0, rackSize);        
        } else {
            System.arraycopy(rangeMax1b, 0, rangeMax, 0, rackSize + 2);
            System.arraycopy(gapCount1b, 0, gapCount, 0, rackSize);
            System.arraycopy(discard1b, 0, discardReplacement, 0, cardSize + 1);
            System.arraycopy(groupHand1b, 0, groupHand, 0, rackSize);        
        }

        offRange = 0;
        for (int i = 0; i < rackSize; i++) {
            if (gapCount[i] != 0) {
                offRange++;
            }
        }        
    }
    
    private void review1a(int[] value) {    
        while (true) {
            boolean[] scan = new boolean[rackSize];
            for (int i = 0; i < rackSize; i++) {
                if (gapCount1a[i] == 0) {
                    scan[i] = true;
                }
            }
                        
            int reviewGroup = -1;
            while (true) {
                
                reviewGroup = -1;
                for (int i = 0; i < rackSize; i++) {
                    int group = groupHand1a[i];
                    if (i == group && group == discard1a[value[i]] % cardKey) {
                        if (!scan[group]) {
                            reviewGroup = group;
                            rangeMax1a[i + 1] = value[i];
                            if (i > 0 && gapCount1a[i - 1] > 0) {
                                rangeMax1a[i] = value[i] - 1;
                            }
                            break;
                        }
                    } 
                }
                
                if (reviewGroup == -1) {
                    for (int i = rackSize - 1; i >= 0; i--) {
                        if (!scan[i]) {
                            int group = i;
                            for (int j = rangeMax1a[i + 1] + 1; j < rangeMax1a[i + 2]; j++) {
                                if (discard1a[j] > rackSize) {
                                    int pos = 0;
                                    while (hand[pos].getValue() != j) {
                                        pos++;
                                    }
                                    if (pos == i) {
                                        groupHand1a[discard1a[j] % cardKey] = i;
                                        rangeMax1a[i + 1] = j;
                                        if (i > 0 && gapCount1a[i - 1] > 0) {
                                            rangeMax1a[i] = j - 1;
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
                    for (int i = 1; i < rackSize - 1; i++) {
                        if (!scan[i]) {
                            int target = cardKey + i + 1;
                            for (int j = rangeMax1a[i] + 1; j <= rangeMax1a[i + 1]; j++) {
                                if (discard1a[j] == target) {
                                    int shift = 0;
                                    for (int k = 1; k < j; k++) {
                                        if (discard1a[k] < cardKey) {
                                            if (discard1a[k] == i) {
                                                shift++;
                                            }
                                        }
                                    }
                                    if (shift < 1) {
                                        continue;
                                    }
                                    
                                    rangeMax1a[i + 1] = j - 1;
                                    rangeMax1a[i + 2] = j;                                    
                                    reviewGroup = i + 1;
                                    scan[i + 1] = true;
                                    break;
                                }
                            } 
                            
                            if (reviewGroup != -1)
                                break;
                        }
                    }
                }
                
                if (reviewGroup == -1) {
                    for (int i = rackSize - 2; i > 0; i--) {
                        if (!scan[i]) {
                            int target = cardKey + i - 1;
                            for (int j = rangeMax1a[i] + 1; j <= rangeMax1a[i + 1]; j++) {
                                if (discard1a[j] == target) {
                                    int shift = 0;
                                    for (int k = j + 1; k <= rangeMax1a[i + 1]; k++) {
                                        if (discard1a[k] < cardKey) {
                                            if (discard1a[k] == i) {
                                                shift++;
                                            }
                                        }
                                    }
                                    
                                    if (shift < 1)
                                        continue;
                                    
                                    if (i > 1 && gapCount1a[i - 2] > 0) {
                                        rangeMax1a[i - 1] = j - 1;
                                        rangeMax1a[i] = j;
                                    } else {
                                        rangeMax1a[i] = j;
                                    }
                                    reviewGroup = i - 1;
                                    scan[i - 1] = true;
                                    break;
                                }
                            } 
                            
                            if (reviewGroup != -1)
                                break;
                        }
                    }
                }

                if (reviewGroup == -1) {
                    break;
                }
                                
                int val = value[reviewGroup];
                
                int updatevalue = reviewGroup + 1;
                if (updatevalue == rackSize || gapCount1a[reviewGroup + 1] == 0) {
                    updatevalue = -1;
                }
                
                for (int i = val + 1; i <= rangeMax1a[reviewGroup + 2]; i++) {
                    if (discard1a[i] > -1 && discard1a[i] < cardKey) {
                        discard1a[i] = updatevalue;    
                        gapCount1a[reviewGroup]--;
                        if (updatevalue != -1) {
                            gapCount1a[updatevalue]++;
                        }
                    } else if (updatevalue > 0 && discard1a[i] >= cardKey) {
                        int group = discard1a[i] % cardKey;
                        if (reviewGroup < rackSize - 1) {
                            groupHand1a[group] = reviewGroup + 1;
                        } else {
                            groupHand1a[group] = reviewGroup;
                        }
                        scan[updatevalue] = false; 
                    } 
                }
                
                updatevalue = reviewGroup - 1;
                if (reviewGroup == 0 || gapCount1a[reviewGroup - 1] == 0) {
                    updatevalue = -1;
                }
                
                int begin2 = 0;
                if (reviewGroup > 0) {
                    begin2 = rangeMax1a[reviewGroup - 1];
                }
                for (int i = val - 1; i > begin2; i--) {
                    if (discard1a[i] > -1 && discard1a[i] < cardKey) {
                        discard1a[i] = updatevalue;
                        gapCount1a[reviewGroup]--;
                        if (updatevalue != -1) {
                            gapCount1a[updatevalue]++;
                        }
                    } else if (updatevalue >= 0 && discard1a[i] >= cardKey) {
                        int group = discard1a[i] % cardKey;
                        if (reviewGroup > 0) {
                            groupHand1a[group] = reviewGroup - 1;
                        } else {
                            groupHand1a[group] = reviewGroup;
                        }
                        scan[updatevalue] = false;
                    } 
                }
                
                scan[reviewGroup] = true;
                
                gapCount1a = new int[rackSize];
                for (int i = 1; i <= cardSize; i++) {
                    if (discard1a[i] > -1 && discard1a[i] < rackSize) {
                        gapCount1a[discard1a[i]]++;
                    }
                }
            }
            
            boolean completed = true;
            int i = 0;
            while (i < rackSize - 1) {
                
                if (gapCount1a[i] == 0) {
                    i++;
                    continue;
                }
                
                int min = gapCount1a[i];
                int max = gapCount1a[i];
                int total = gapCount1a[i];
                int begin = i;
                for (int j = begin + 1; j < rackSize; j++) {
                    int val = gapCount1a[j];
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
                            int k = rangeMax1a[group] + 1;
                            while (total > 0) {
                                int count = aveMin;
                                if (numAveMax > 0) {
                                    count = aveMax;
                                }
                                total -= count;
                                gapCount1a[group] = count;
                                while (count > 0) {
                                    if (discard1a[k] < rackSize) {
                                        discard1a[k] = group;
                                        count--;
                                    } else {
                                        for (int pos = 0; pos < rackSize; pos++) {
                                            if (hand[pos].getValue() == k) {
                                                groupHand1a[pos] = group;
                                                break;
                                            }
                                        }
                                    }
                                    k++;
                                }
                                rangeMax1a[group + 1] = k - 1;
                                group++;
                                numAveMax--;
                            }
                            while (discard1a[k] > rackSize 
                                    && discard1a[k] != cardKey + j) {
                                rangeMax1a[group] = k;
                                k++;
                            }                            
                        }
                        i = j + 1;
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
                            int k = cardSize;
                            while (total > 0) {
                                rangeMax1a[group + 1] = k;
                                int count = aveMin;
                                if (numAveMax > 0) {
                                    count = aveMax;
                                }
                                total -= count;
                                gapCount1a[group] = count;
                                while (count > 0) {
                                    if (discard1a[k] < rackSize) {
                                        discard1a[k] = group;
                                        count--;
                                    } else {
                                        for (int pos = 0; pos < rackSize; pos++) {
                                            if (hand[pos].getValue() == k) {
                                                groupHand1a[pos] = group;
                                                break;
                                            }
                                        }
                                    }
                                    k--;
                                }
                                
                                while (discard1a[k] > rackSize && k > rangeMax1a[begin - 1] + 1) {
                                    for (int pos = 0; pos < rackSize; pos++) {
                                        if (hand[pos].getValue() == k) {
                                            if (group > 0 && gapCount1a[group - 1] == 0) {
                                                groupHand1a[pos] = group - 1;
                                            } else {
                                                groupHand1a[pos] = group;
                                            }
                                            
                                            k--; 
                                            break;
                                        }
                                    }
                                }
                                group--;
                                numAveMax--;
                            }
                        }
                        i = j;
                        break;
                    }
                }                
            }
            
            if (completed) {
                break;
            }
        }
    }
        
    private void review1b(int[] value) {    
        while (true) {
            boolean[] scan = new boolean[rackSize];
            for (int i = 0; i < rackSize; i++) {
                if (gapCount1b[i] == 0) {
                    scan[i] = true;
                }
            }
                        
            int reviewGroup = -1;
            while (true) {
                
                reviewGroup = -1;
                for (int i = 0; i < rackSize; i++) {
                    int group = groupHand1b[i];
                    if (i == group && group == discard1b[value[i]] % cardKey) {
                        if (!scan[group]) {
                            reviewGroup = group;
                            rangeMax1b[i + 1] = value[i];
                            if (i > 0 && gapCount1b[i - 1] > 0) {
                                rangeMax1b[i] = value[i] - 1;
                            }
                            break;
                        }
                    } 
                }
                
                if (reviewGroup == -1) {
                    for (int i = rackSize - 1; i >= 0; i--) {
                        if (!scan[i]) {
                            int group = i;
                            for (int j = rangeMax1b[i + 1] + 1; j < rangeMax1b[i + 2]; j++) {
                                if (discard1b[j] > rackSize) {
                                    int pos = 0;
                                    while (hand[pos].getValue() != j) {
                                        pos++;
                                    }
                                    if (pos == i) {
                                        groupHand1b[discard1b[j] % cardKey] = i;
                                        rangeMax1b[i + 1] = j;
                                        if (i > 0 && gapCount1b[i - 1] > 0) {
                                            rangeMax1b[i] = j - 1;
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
                        if (!scan[i]) {
                            int target = cardKey + i - 1;
                            for (int j = rangeMax1b[i] + 1; j <= rangeMax1b[i + 1]; j++) {
                                if (discard1b[j] == target) {
                                    int shift = 0;
                                    for (int k = j + 1; k <= rangeMax1b[i + 1]; k++) {
                                        if (discard1b[k] < cardKey) {
                                            if (discard1b[k] == i) {
                                                shift++;
                                            }
                                        }
                                    }
                                    
                                    if (shift < 1)
                                        continue;

                                    if (i > 1 && gapCount1b[i - 2] > 0) {
                                        rangeMax1b[i - 1] = j - 1;
                                        rangeMax1b[i] = j;
                                    } else {
                                        rangeMax1b[i] = j;
                                    }
                                    reviewGroup = i - 1;
                                    scan[i - 1] = true;
                                    break;
                                }
                            } 
                            
                            if (reviewGroup != -1)
                                break;
                        }
                    }
                }


                if (reviewGroup == -1) {
                    for (int i = 1; i < rackSize - 1; i++) {
                        if (!scan[i]) {
                            int target = cardKey + i + 1;
                            for (int j = rangeMax1b[i] + 1; j <= rangeMax1b[i + 1]; j++) {
                                if (discard1b[j] == target) {
                                    int shift = 0;
                                    for (int k = 1; k < j; k++) {
                                        if (discard1b[k] < cardKey) {
                                            if (discard1b[k] == i) {
                                                shift++;
                                            }
                                        }
                                    }
                                    if (shift < 1) {
                                        continue;
                                    }
                                    
                                    rangeMax1b[i + 1] = j - 1;
                                    rangeMax1b[i + 2] = j;                                    
                                    reviewGroup = i + 1;
                                    scan[i + 1] = true;
                                    break;
                                }
                            } 
                            
                            if (reviewGroup != -1)
                                break;
                        }
                    }
                }
                
                if (reviewGroup == -1) {
                    break;
                }
                                
                int val = value[reviewGroup];
                
                int updatevalue = reviewGroup + 1;
                if (updatevalue == rackSize || gapCount1b[reviewGroup + 1] == 0) {
                    updatevalue = -1;
                }
                
                for (int i = val + 1; i <= rangeMax1b[reviewGroup + 2]; i++) {
                    if (discard1b[i] > -1 && discard1b[i] < cardKey) {
                        discard1b[i] = updatevalue;    
                        gapCount1b[reviewGroup]--;
                        if (updatevalue != -1) {
                            gapCount1b[updatevalue]++;
                        }
                    } else if (updatevalue > 0 && discard1b[i] >= cardKey) {
                        int group = discard1b[i] % cardKey;
                        if (reviewGroup < rackSize - 1) {
                            groupHand1b[group] = reviewGroup + 1;
                        } else {
                            groupHand1b[group] = reviewGroup;
                        }
                        scan[updatevalue] = false; 
                    } 
                }
                
                updatevalue = reviewGroup - 1;
                if (reviewGroup == 0 || gapCount1b[reviewGroup - 1] == 0) {
                    updatevalue = -1;
                }
                
                int begin2 = 0;
                if (reviewGroup > 0) {
                    begin2 = rangeMax1b[reviewGroup - 1];
                }
                for (int i = val - 1; i > begin2; i--) {
                    if (discard1b[i] > -1 && discard1b[i] < cardKey) {
                        discard1b[i] = updatevalue;
                        gapCount1b[reviewGroup]--;
                        if (updatevalue != -1) {
                            gapCount1b[updatevalue]++;
                        }
                    } else if (updatevalue >= 0 && discard1b[i] >= cardKey) {
                        int group = discard1b[i] % cardKey;
                        if (reviewGroup > 0) {
                            groupHand1b[group] = reviewGroup - 1;
                        } else {
                            groupHand1b[group] = reviewGroup;
                        }
                        scan[updatevalue] = false;
                    } 
                }
                
                scan[reviewGroup] = true;
                
                gapCount1b = new int[rackSize];
                for (int i = 1; i <= cardSize; i++) {
                    if (discard1b[i] > -1 && discard1b[i] < rackSize) {
                        gapCount1b[discard1b[i]]++;
                    }
                }
            }
            
            boolean completed = true;
            int i = 0;
            while (i < rackSize - 1) {
                
                if (gapCount1b[i] == 0) {
                    i++;
                    continue;
                }
                
                int min = gapCount1b[i];
                int max = gapCount1b[i];
                int total = gapCount1b[i];
                int begin = i;
                for (int j = begin + 1; j < rackSize; j++) {
                    int val = gapCount1b[j];
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
                            int k = rangeMax1b[group] + 1;
                            while (total > 0) {
                                int count = aveMin;
                                if (numAveMax > 0) {
                                    count = aveMax;
                                }
                                total -= count;
                                gapCount1b[group] = count;
                                while (count > 0) {
                                    if (discard1b[k] < rackSize) {
                                        discard1b[k] = group;
                                        count--;
                                    } else {
                                        for (int pos = 0; pos < rackSize; pos++) {
                                            if (hand[pos].getValue() == k) {
                                                groupHand1b[pos] = group;
                                                break;
                                            }
                                        }
                                    }
                                    k++;
                                }
                                rangeMax1b[group + 1] = k - 1;
                                group++;
                                numAveMax--;
                            }
                            while (discard1b[k] > rackSize 
                                    && discard1b[k] != cardKey + j) {
                                rangeMax1b[group] = k;
                                k++;
                            }                            
                        }
                        i = j + 1;
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
                            int k = cardSize;
                            while (total > 0) {
                                rangeMax1b[group + 1] = k;
                                int count = aveMin;
                                if (numAveMax > 0) {
                                    count = aveMax;
                                }
                                total -= count;
                                gapCount1b[group] = count;
                                while (count > 0) {
                                    if (discard1b[k] < rackSize) {
                                        discard1b[k] = group;
                                        count--;
                                    } else {
                                        for (int pos = 0; pos < rackSize; pos++) {
                                            if (hand[pos].getValue() == k) {
                                                groupHand1b[pos] = group;
                                                break;
                                            }
                                        }
                                    }
                                    k--;
                                }
                                
                                while (discard1b[k] > rackSize && k > rangeMax1b[begin - 1] + 1) {
                                    for (int pos = 0; pos < rackSize; pos++) {
                                        if (hand[pos].getValue() == k) {
                                            if (group > 0 && gapCount1b[group - 1] == 0) {
                                                groupHand1b[pos] = group - 1;
                                            } else {
                                                groupHand1b[pos] = group;
                                            }
                                            
                                            k--; 
                                            break;
                                        }
                                    }
                                }
                                group--;
                                numAveMax--;
                            }
                        }
                        i = j;
                        break;
                    }
                }                
            }
            
            if (completed) {
                break;
            }
        }
    }
    
    private void level2review(int[] value) {
        drawReplacement = new int[cardSize + 1];
        System.arraycopy(discardReplacement, 1, drawReplacement, 1, cardSize);
        int maxSeq = 1;
        for (int i = 0; i < 9;) {
            int max = 1; 
            for (int j = i + 1; j < 10; j++) {
                i++;
                if (value[j] == value[j - 1] + 1) {
                    max++;
                } else {
                    break;
                }
            }
            if (max > maxSeq)
                maxSeq = max;
        }
        if (maxSeq <= 2) {
            maxSeq = 0;
        }
        
        for (int i = 0; i < 9; i++) {
            if (gapCount[i] == 0 && gapCount[i + 1] == 0) {
            // ... i0 0 ....
                if (i == 0 && gapCount[2] > 0) {
                // i0 0 n ...
                    if (value[2] < value[1] && value[2] > value[0]) {
                        for (int j = 1; j < value[2]; j++) {
                            if (drawReplacement[j] == -1) {
                                discardReplacement[j] = 1;
                                drawReplacement[j] = 1;
                                gapCount[2]++;
                            }
                        }
                    }
                    
                    int count = 0;
                    for (int j = value[0] + 1; j < value[1]; j++) {
                        if (drawReplacement[j] == -1) {
                            count++;
                        }
                    }
                    int cutoff = (int) Math.floor(count / 2);
                    
                    for (int j = value[0] + 1; j < value[1]; j++) {
                        if (drawReplacement[j] == -1) {
                            if (count-- > cutoff) {
                                drawReplacement[j] = 1;
                            } else {
                                drawReplacement[j] = 2;
                            }
                        }
                    }
                } else if  (i == 8 && gapCount[7] > 0) {
                // .... n i0 0
                    if (value[7] > value[8] && value[7] < value[9]) {
                        for (int j = value[7] + 1; j < value[9]; j++) {
                            if (discardReplacement[j] == -1) {
                                discardReplacement[j] = 8;
                                drawReplacement[j] = 8;
                                gapCount[7]++;
                            }
                        }
                    }
                    
                    int count = 0;
                    for (int j = value[8] + 1; j < value[9]; j++) {
                        if (discardReplacement[j] == -1) {
                            count++;
                        }
                    }
                    int cutoff = (int) Math.floor(count / 2);
                    
                    for (int j = value[9] - 1; j > value[8]; j--) {
                        if (discardReplacement[j] == -1) {
                            if (count-- > cutoff) {
                                drawReplacement[j] = 8;
                            } else {
                                drawReplacement[j] = 7;
                            }
                        }
                    }
                } else if (i > 0 && i < 8) {
                    if (gapCount[i - 1] > 0 && gapCount[i + 2] == 0) {
                    // .... n i0 0 _0 ....
                        if (value[i - 1] > value[i] && value[i - 1] < value[i + 1]) {
                            for (int j = value[i] + 1; j < value[i + 1]; j++) {
                                if (discardReplacement[j] == -1) {
                                    if (j > value[i - 1]) {
                                        discardReplacement[j] = i;
                                        gapCount[i - 1]++;
                                    }
                                    drawReplacement[j] = i;
                                }
                            }
                        } else { 
                            int count = 0;
                            for (int j = value[i] + 1; j < value[i + 1]; j++) {
                                if (discardReplacement[j] == -1) {
                                    count++;
                                }
                            }
                            int cutoff = (int) Math.floor(count / 2);
                            
                            for (int j = value[i] + 1; j < value[i + 1]; j++) {
                                if (discardReplacement[j] == -1) {
                                    if (--count > cutoff) {
                                        drawReplacement[j] = i - 1;
                                    } else {
                                        drawReplacement[j] = i;
                                    }
                                }
                            }
                        }
                    } else if (gapCount[i - 1] == 0 && gapCount[i + 2] > 0) {
                    // .... 0_ i0 0 n ....
                        if (value[i + 2] > value[i] && value[i + 2] < value[i + 1]) {
                            for (int j = value[i] + 1; j < value[i + 2]; j++) {
                                if (discardReplacement[j] == -1) {
                                    gapCount[i + 2]++;
                                    discardReplacement[j] = i + 1;
                                    drawReplacement[j] = i + 1;
                                }
                            }
                            for (int j = value[i + 2] + 1; j < value[i + 1]; j++) {
                                if (discardReplacement[j] == -1) {
                                    drawReplacement[j] = i + 1;
                                }
                            }
                        } else { 
                            int count = 0;
                            for (int j = value[i] + 1; j < value[i + 1]; j++) {
                                if (discardReplacement[j] == -1) {
                                    count++;
                                }
                            }
                            int cutoff = (int) Math.floor(count / 2);
                            
                            for (int j = value[i] + 1; j < value[i + 1]; j++) {
                                if (discardReplacement[j] == -1) {
                                    if (count-- > cutoff) {
                                        drawReplacement[j] = i + 1;
                                    } else {
                                        drawReplacement[j] = i + 2;
                                    }
                                }
                            }
                        }
                    } else if (gapCount[i - 1] > 0 && gapCount[i + 2] > 0) {
                    // .... n i0 0 n ....
                        int gapPlusL = 0;
                        if (value[i - 1] > value[i] && value[i - 1] < value[i + 1]) {
                            for (int j = value[i - 1] + 1; j < value[i + 1]; j++) {
                                if (discardReplacement[j] == -1) {
                                    discardReplacement[j] = i;
                                    drawReplacement[j] = i;
                                    gapPlusL++;
                                }
                            }
                        }
                        int gapPlusR = 0;
                        if (value[i + 2] > value[i] && value[i + 2] < value[i + 1]) {
                            for (int j = value[i] + 1; j < value[i + 2]; j++) {
                                if (discardReplacement[j] == -1) {
                                    discardReplacement[j] = i + 1;
                                    drawReplacement[j] = i + 1;
                                    gapPlusR++;
                                }
                            }
                        }
                                                
                        int shift = 0;
                        for (int j = rangeMax[i - 1] + 1; j < rangeMax[i + 2]; j++) {
                            if (drawReplacement[j] == -1) {
                                shift++;
                            }
                        }
                        int expandL = gapCount[i - 1];
                        int expandR = gapCount[i + 2];
                        int j = value[i] + 1;
                        while (expandR > expandL && shift > 0 && j < value[i + 1]) {
                            if (drawReplacement[j] == -1) {
                                drawReplacement[j] = i;
                                expandL++;
                                shift--;
                            }
                            j++;
                        }
                        j = value[i + 1] - 1;
                        while (expandR < expandL && shift > 0 && j > value[i]) {
                            if (drawReplacement[j] == -1) {
                                drawReplacement[j] = i + 1;
                                expandR++;
                                shift--;
                            }
                            j--;
                        }
                        
                        if (shift > 0) {
                            shift /= 2;
                            for (j = value[i + 1] - 1; j > value[i]; j--) {
                                if (drawReplacement[j] == -1) {
                                    if (shift-- > 0) {
                                        drawReplacement[j] = i + 1;
                                    } else 
                                        drawReplacement[j] = i;
                                }
                            }
                        }
                        
                        gapCount[i - 1] += gapPlusL;
                        gapCount[i + 2] += gapPlusR;
                    }
                } 
            } else if (i == 0 && gapCount[0] == 0 && gapCount[1] > 0) {
            // 0 n 0 .....
                if (value[1] < value[0]) {
                    for (int j = 1; j < value[0]; j++) {
                        if (discardReplacement[j] == -1) {
                            discardReplacement[j] = 0;
                            drawReplacement[j] = 0;
                            gapCount[1]++;
                        }
                    }
                } 
                
                if (value[1] < value[0] || gapCount[2] == 0) {
                    int count = 0;
                    for (int j = 1; j < value[0]; j++) {
                        if (discardReplacement[j] == -1) {
                            count++;
                        }
                    }
                    int cutoff = (int) Math.floor(count / 2);
                    
                    for (int j = 1; j < value[0]; j++) {
                        if (discardReplacement[j] == -1) {
                            if (--count >= cutoff) {
                                drawReplacement[j] = 0;
                            } else {
                                drawReplacement[j] = 1;
                            }
                        }
                    }
                }
            } else if (i == 8 && gapCount[8] > 0 && gapCount[9] == 0
                    && gapCount[7] == 0) {
            // .... 0 n 0
                int count = 0;
                for (int j = value[9] + 1; j <= cardSize; j++) {
                    if (discardReplacement[j] == -1) {
                        count++;
                    }
                }
                int cutoff = (int) Math.floor(count / 2);
                
                for (int j = cardSize; j > value[9]; j--) {
                    if (discardReplacement[j] == -1) {
                        if (count-- > cutoff) {
                            drawReplacement[j] = 9;
                        } else {
                            drawReplacement[j] = 8;
                        }
                    }
                }
            } 
        }
        if (gapCount[9] == 0 && value[8] > value [9]) {
            for (int i = value[8] + 1; i <= cardSize; i++) {
                if (discardReplacement[i] == -1) {
                    discardReplacement[i] = 9;
                    drawReplacement[i] = 9;
                    gapCount[8]++;
                }
            }
        }

        for (int i = 1; i <= 8; i++) {
            if (gapCount[i] > 0 && gapCount[i] < 3 && gapCount[i - 1] == 0
                    && gapCount[i + 1] == 0) {
                int expand = gapCount[i];
                for (int j = rangeMax[i - 1] + 1; j < rangeMax[i]; j++) {
                    discardReplacement[j] = drawReplacement[j];
                    if (discardReplacement[j] == -1) {
                        discardReplacement[j] = i - 1;
                        drawReplacement[j] = i - 1;
                        expand++;
                    }
                }
                for (int j = rangeMax[i + 1] + 1; j < rangeMax[i + 2]; j++) {
                    discardReplacement[j] = drawReplacement[j];
                    if (discardReplacement[j] == -1) {
                        discardReplacement[j] = i + 1;
                        drawReplacement[j] = i + 1;
                        expand++;
                    }
                }
                
                if (offRange < 3) {
                    if (i - 2 < 0) {
                        for (int j = 1; j < rangeMax[i - 1]; j++) {
                            discardReplacement[j] = drawReplacement[j];
                            if (discardReplacement[j] == -1 && expand < 6) {
                                discardReplacement[j] = 0;
                                drawReplacement[j] = 0;
                            }
                        }
                    } else {
                        for (int j = rangeMax[i - 2] + 1; j < rangeMax[i - 1]; j++) {
                            discardReplacement[j] = drawReplacement[j];
                            if (discardReplacement[j] == -1 && expand < 6) {
                                discardReplacement[j] = i - 2;
                                drawReplacement[j] = i - 2;
                            }
                        }
                    }
                    
                    for (int j = rangeMax[i + 2] + 1; j < rangeMax[i + 3]; j++) {
                        discardReplacement[j] = drawReplacement[j];
                        if (discardReplacement[j] == -1 && expand < 6) {
                            discardReplacement[j] = i + 2;
                            drawReplacement[j] = i + 2;
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
            } else
                pos = i;
        }
        if (pos > -1) {
            int expand = gapCount[pos + 1];
            for (int i = pos; i >= 0 && expand < 6; i--) {
                if (i > 0 && value[i] - 1 == value[i - 1])
                    break;
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
                            if (drawReplacement[j] == -1) {
                                if (count < cutoff) {
                                    drawReplacement[j] = i + 1;
                                } else {
                                    drawReplacement[j] = i;
                                }
                                if (offRange < 3) {
                                    discardReplacement[j] = drawReplacement[j];
                                }
                            } else {
                                if (offRange < 3) {
                                    discardReplacement[j] = drawReplacement[j];
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
                            if (drawReplacement[j] == -1) {
                                drawReplacement[j] = i;
                                if (offRange < 3) {
                                    discardReplacement[j] = i;
                                }
                            } else {
                                if (offRange < 3) {
                                    discardReplacement[j] = drawReplacement[j];
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
            } else
                pos = i;
        }
        if (pos > -1) {
            int expand = gapCount[pos - 1];
            for (int i = pos; i <= 9 && expand < 6; i++) {
                if (i < 9 && value[i] + 1 == value[i + 1])
                    break;
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
                            if (drawReplacement[j] == -1) {
                                if (count <= cutoff) {
                                    drawReplacement[j] = i;
                                } else {
                                    drawReplacement[j] = i - 1;
                                }
                                
                                if (offRange < 3) {
                                    discardReplacement[j] = drawReplacement[j];
                                }
                            } else {
                                if (offRange < 3) {
                                    discardReplacement[j] = drawReplacement[j];
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
                            if (drawReplacement[j] == -1) {
                                drawReplacement[j] = i;
                                if (offRange < 3) {
                                    discardReplacement[j] = i;
                                }
                            } else {
                                if (offRange < 3) {
                                    discardReplacement[j] = drawReplacement[j];
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // add sequence for higher scores ----------------------------------------*/
        if (maxSeq < 6) {
            for (int i = 0; i < rackSize; i++) {
                if (gapCount[i] == 0) {
                    //  x x x x x x n x x x x x x 
                    //               n+1
                    if (i < rackSize - 1 && discardReplacement[value[i] + 1] < rackSize) {
                        if (drawReplacement[value[i] + 1] == -1) {
                            if (gapCount[i + 1] > aveRange) {
                                drawReplacement[value[i] + 1] = i + 1;
                            } else if (gapCount[i + 1] == 0) { 
                                int seqL = 1;
                                for (int j = i - 1; j >= 0; j--) {
                                    if (gapCount[j] == 0 && value[j] + 1 == value[j + 1]) {
                                        seqL++;
                                    } else {
                                        break;
                                    }
                                } 
                                
                                if (seqL >= maxSeq) {
                                    if (i == 8)  {
                                        if (seqL > 2) 
                                            discardReplacement[value[i] + 1] = i + 1;
                                        drawReplacement[value[i] + 1] = i + 1;
                                    } else {
                                        if (value[i + 1] + 1 == value[i + 2]) {
                                            int seqR = 1;
                                            for (int j = i + 2; j < rackSize; j++) {
                                                if (gapCount[j] == 0 && value[j] - 1 == value[j - 1]) {
                                                    seqR++;
                                                }
                                            }
                                            if (seqL >= seqR) {
                                                if (seqL > 2) 
                                                    discardReplacement[value[i] + 1] = i + 1;
                                                drawReplacement[value[i] + 1] = i + 1;
                                            }                                
                                        } else {
                                            if (seqL > 2) 
                                                discardReplacement[value[i] + 1] = i + 1;
                                            drawReplacement[value[i] + 1] = i + 1;
                                            
                                        }
                                    }
                                }
                            }
                        }
                    }

                    //  x x x x x x n x x x x x x 
                    //           n-1
                    if (i > 0 && discardReplacement[value[i] - 1] < rackSize) {
                        if (drawReplacement[value[i] - 1] == -1) {
                            if (gapCount[i - 1] > aveRange) {
                                drawReplacement[value[i] - 1] = i - 1;
                            } else if (gapCount[i - 1] == 0) {
                                int seqR = 1;
                                for (int j = i + 1; j < rackSize; j++) {
                                    if (gapCount[j] == 0 && value[j] - 1 == value[j - 1]) {
                                        seqR++;
                                    } else {
                                        break;
                                    }
                                }
                                if (seqR >= maxSeq) {
                                    if (i == 1) {
                                        if (seqR > 2)
                                            discardReplacement[value[i] - 1] = i - 1;
                                        drawReplacement[value[i] - 1] = i - 1;
                                    } else {
                                        // 4 5 7 8 .... i = 1
                                        if (value[i - 2] + 1 == value[i - 1]) {
                                            int seqL = 1;
                                            for (int j = i - 2; j >= 0; j--) {
                                                if (gapCount[j] == 0 && value[j] + 1 == value[j + 1]) {
                                                    seqL++;
                                                }
                                            }
                                            if (seqR >= seqL) {
                                                if (seqR > 2)
                                                    discardReplacement[value[i] - 1] = i - 1;
                                                drawReplacement[value[i] - 1] = i - 1;
                                            }            
                                        } else {
                                            if (seqR > 2)
                                                discardReplacement[value[i] - 1] = i - 1;
                                            drawReplacement[value[i] - 1] = i - 1;
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

    public boolean determine_use(int value, boolean isDiscardCard) {
        boolean secondCheck = goodChoice(value, isDiscardCard);
        if (!secondCheck) {
            return false;
        }
        
        if (isDiscardCard) {
            if (value > 1 && discardReplacement[value - 1] == cardKey + choosePosition) {
                if (value < cardSize && discardReplacement[value + 1] != cardKey + choosePosition + 1) {
                    return false;
                }
            }
            if (value < cardSize && discardReplacement[value + 1] == cardKey + choosePosition) {
                if (value > 1 && discardReplacement[value - 1] != cardKey + choosePosition - 1) {
                    return false;
                }
            }
        }
        
        int currOffRange = offRange;
        int replacement = choosePosition;
        Card currCard = hand[replacement];
        Card tempCard = new Card(value, 0, 0);
        replaceCard(tempCard, replacement, false);
        int newOffRange = offRange;
        replaceCard(currCard, replacement, false);
        if (newOffRange <= currOffRange) {
            return true;
        }
        return false;
    }
    
    private boolean goodChoice(int value, boolean isDiscardCard) {
        //printAll();
        //System.out.println("determine_use : " + value);
        int replacement = discardReplacement[value];
          
        if (replacement > -1 && replacement < rackSize) {
            choosePosition = replacement;
            return true;
        }        
        if (!isDiscardCard) {
            //System.out.println("\t\tadditional determine_use draw: " + value);
            replacement = drawReplacement[value];
            if (replacement > -1 && replacement < rackSize) {
                choosePosition = replacement;
                return true;
            }
        }          
         return false;
    }

    private int choose_position(int card) {
        return choosePosition;
    }
    
    public Card replace(Card takeCard, boolean isDiscardCard) {
        int newPos = choose_position(takeCard.getValue());
        if (isDiscardCard) {
            viewable[newPos] = 3;
        } else {
            viewable[newPos] = 1;
        }
        return replaceCard(takeCard, newPos, false);
    }
    
    private Card replaceCard(Card newCard, int newPos, boolean print) {
        Card returnCard = hand[newPos];
        hand[newPos] = newCard;        
        reviewHand();
        if (print) {
            printAll();
            System.out.println();
            System.out.print("gap count\t");
            for (int i = 0; i < rackSize; i++) {
                System.out.print(gapCount[i] + " ");
            }
            System.out.println();
            
            for (int i = 1; i <= rackSize; i++) {
                int temp = rangeMax[i] - rangeMax[i - 1];
                System.out.print(rangeMax[i - 1] + " to " + rangeMax[i] + " : " + temp + " | ");
            }
            System.out.println();
            
            for (int i = 1; i <= cardSize; i++) {
                System.out.print(discardReplacement[i] + " ");
                if (i % 10 == 0) {
                    if (i == cardSize)
                        System.out.println();
                    else
                        System.out.print("| ");
                }
            }
            
            for (int i = 1; i <= cardSize; i++) {
                System.out.print(drawReplacement[i] + " ");
                if (i % 10 == 0) {
                    if (i == cardSize)
                        System.out.println();
                    else
                        System.out.print("| ");
                }
            }
        }
        return returnCard;
    }
    
    public void print() {
        for (int i = 0; i < rackSize; i++) {
            if (viewable[i] == 0) {
                System.out.print(" x ");
            } else if (viewable[i] == 1) {
                System.out.print(" o ");
            } else {
                int val = hand[i].getValue();
                if (val < 10)
                    System.out.print(" " + val + " ");
                else 
                    System.out.print(val + " ");
            }
        }
        System.out.println();
    }
    
    public void printAll() {
        for (int i = 0; i < rackSize; i++) {
            int val = hand[i].getValue();
            if (val < 10)
                System.out.print(" " + val + " ");
            else 
                System.out.print(val + " ");
        }
        System.out.println();
    }
}
