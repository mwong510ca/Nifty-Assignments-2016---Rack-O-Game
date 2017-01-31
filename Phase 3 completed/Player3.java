
public class Player3 extends Player2 {
    private int[] discardPile;
    private int discardCount;
    private byte[] cardsStatus;

    public Player3(int size, int oppoPlayers) {
        super(size);
    }

    public void setHand(Card[] hand) {
        super.setHand(hand);
        
        discardPile = new int[20];
        discardCount = 0;
        // 0 - unknown/drawPile, 1 - discardPile, 2 - self; 4 - player 1, 8 - player 2, 16 - player 3, 32 - unknown player
        cardsStatus = new byte[cardSize + 1];        
        for (int i = 0; i < hand.length; i++) {
            cardsStatus[hand[i].getValue()] = 2;
        }
    }
    
    private void reviewHand() {
        rangeMax = new int[rackSize + 2];
        gapCount = new int[rackSize];
        int possibleCount = 0;
        boolean useDiscard = discardCount > 15;
        for (int i = 1; i <= cardSize; i++) {
            if (cardsStatus[i] == 0) {
                possibleCount++;
            } else if (useDiscard && cardsStatus[i] == 1) {
                possibleCount++;
            }
        }
        int remainder = possibleCount % rackSize;
        int aveRange = (int) Math.floor(possibleCount / rackSize);
        int[] value = new int[rackSize];
        for (int i = 0; i < rackSize; i++) {
            value[i] = hand[i].getValue();
            gapCount[i] = aveRange;
            if (remainder > 0) {
                gapCount[i]++;
                remainder--;
            }    
        }
        
        discardReplacement = new int[cardSize + 1];
        
        int pos = 0;
        int count = 0;
        for (int i = 1; i <= cardSize; i++) {
            if (cardsStatus[i] < 2) {
                if ((useDiscard && cardsStatus[i] == 1) || cardsStatus[i] == 0) {
                    rangeMax[pos + 1] = i;
                    discardReplacement[i] = pos;
                    count++;
                    if (count == gapCount[pos]) {
                        count = 0;
                        pos++;
                    }
                } else {
                    discardReplacement[i] = -2;
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
        level1review(value);
        if (offRange > 0) {
            level2review(value);
        }
    } 
    
    public boolean determine_use(int value, boolean isDiscardCard) {
        if (isDiscardCard) {
            cardsStatus[value] = 0;
        }
        reviewHand();
        
        boolean result = secondCheck(value, isDiscardCard);
        if (isDiscardCard) {
            cardsStatus[value] = 1;
        } else {
            cardsStatus[value] = 0;
        }
        return result;
    }

    public Card replace(Card takeCard, boolean isDiscardCard) {
        Card card = super.replace(takeCard, isDiscardCard);
        return card;
    }
    
    protected Card replaceCard(Card newCard, int pos, boolean isDiscardCard) {
        Card returnCard = super.replaceCard(newCard, pos, isDiscardCard);
        cardsStatus[newCard.getValue()] = 2;
        return returnCard;
    }
    
    private boolean secondCheck(int value, boolean isDiscardCard) {
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
        byte[] backup = new byte[rackSize];
        System.arraycopy(viewable, 0, backup, 0, rackSize);
        replaceCard(tempCard, replacement, false);
        reviewHand();    
        int newOffRange = offRange;
        replaceCard(currCard, replacement, false);
        viewable = backup;
        reviewHand();    
        if (newOffRange <= currOffRange) {
            return true;
        }
        return false;
    }
    
    public void discardShuffle() {
        byte[] copy = cardsStatus;
        cardsStatus = new byte[cardSize + 1]; 
        for (int i = 1; i <= cardSize; i++) {
            if (copy[i] == 0 || copy[i] == 32) {
                cardsStatus[i] = 32;
            }
        }
        for (int i = 1; i <= cardSize; i++) {
            if (copy[i] == 1) {
                cardsStatus[i] = 0;
            } else if (copy[i] > 1 && copy[i] < 32) {
                cardsStatus[i] = copy[i];
            }
        }
        discardCount = 0;
    }
    
    public void discardAdd(Card card) {
        int value = card.getValue();
        cardsStatus[value] = 1;
        discardPile[discardCount++] = value;
    }
        
    public void discardRemove() {
        cardsStatus[discardPile[discardCount - 1]] = 32;
        discardCount--;
    }
    
    public void playerCard(Card card, int id) {
        cardsStatus[card.getValue()] = (byte) Math.pow(2, id + 2);
    }
}
