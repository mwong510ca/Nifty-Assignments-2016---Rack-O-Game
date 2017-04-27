package mwong.myprojects.rackocomputerplayers;


public class Player2 extends AbstractPlayer {
	protected int[] groupHand;
	protected int[] gapCount;
	protected int[] discardReplacement;
	protected int[] rangeMax;
	protected int[] drawReplacement;
	protected int aveRange;
	protected int offRange;
	protected int choosePosition;
	protected boolean aggressivePlayer;
	protected AbstractHandAnalyzer analyzer1;
	private byte[] backupHand;
	private boolean deadlock;

	public Player2(int size) {
		super(size);
		aveRange = cardSize / rackSize;
		analyzer1 = new HandAnalyzer1(cardSize, rackSize, cardKey);
		backupHand = new byte[rackSize];
		aggressivePlayer = false;
	}

    public String toString() {
		return "Moderate";
	}
    
    public void setWinningScore(int score) {
    	/*
    	if (score == 75)
    		aggressivePlayer = false;
    	else 
    		aggressivePlayer = true;
    	aggressivePlayer = true;
    	*/
    }
    
    public void setAggressive(boolean flag) {
    	aggressivePlayer = flag;
    }
    
    public void setHand(byte[] hand) {
    	super.setHand(hand);    	
    	System.arraycopy(hand, 0, backupHand, 0, rackSize);
    	deadlock = false;
    }
    
	protected void reviewHand() {
		boolean[] possibleHand = new boolean[cardSize + 1];
		rangeMax = new int[rackSize + 2];
		gapCount = new int[rackSize];
		for (int i = 1; i <= cardSize; i++) {
			possibleHand[i] = true;
		}
		int countMax = aveRange - 1;
		for (int i = 0; i < rackSize; i++) {
			possibleHand[hand[i]] = false;
			gapCount[i] = countMax;
		}
		
		discardReplacement = new int[cardSize + 1];
		drawReplacement = new int[cardSize + 1];
		
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
		handAnalyze();
	} 

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
	
	protected void handAnalyze() {
		analyzer1.analysisNow(hand, groupHand, gapCount, discardReplacement, rangeMax);
		System.arraycopy(analyzer1.getRangeMax(), 0, rangeMax, 0, rackSize + 2);
		System.arraycopy(analyzer1.getGapCount(), 0, gapCount, 0, rackSize);
		System.arraycopy(analyzer1.getDiscard(), 0, discardReplacement, 0, cardSize + 1);
		System.arraycopy(analyzer1.getGroupHand(), 0, groupHand, 0, rackSize);	
		
		offRange = 0;
        for (int i = 0; i < rackSize; i++) {
            if (gapCount[i] != 0) {
                offRange++;
            }
        }        
	}
	
	public boolean determineUse(byte value, boolean isDiscardCard) {
		reviewHand();
		if (offRange > 0) {
			//optimizer.optimizeNow(offRange, aggressivePlayer, hand, gapCount, rangeMax, 
			//		discardReplacement, drawReplacement);
		}
    	boolean hasReplacement = secondCheck(value, isDiscardCard);
    	if (deadlock && !isDiscardCard && !hasReplacement) {
			return switchCard(value); 
		}
    	return hasReplacement;
    }
	
	private boolean switchCard(byte value) {
		if (value < hand[0]) {
			choosePosition = 0;
			return true;
		}
		for (int i = 1; i < rackSize; i++) { 
			if (value > hand[i - 1] && value < hand[i]) {
				choosePosition = i;
				return true;
			}
		}
		if (value > hand[rackSize - 1]) {
			choosePosition = rackSize - 1;
			return true;
		}
		return false;
	}
  
    private boolean secondCheck(byte value, boolean isDiscardCard) {
    	boolean secondCheck = hasReplacement(value, isDiscardCard);
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
        byte currCard = hand[replacement];
        byte tempCard = value;
        byte[] backup = new byte[rackSize];
        System.arraycopy(viewable, 0, backup, 0, rackSize);
        replaceCard(tempCard, replacement, false);
        reviewHand();	
		int newOffRange = offRange;
        replaceCard(currCard, replacement, false);
        viewable = backup;

		if (newOffRange <= currOffRange) {
            return true;
        }
        return false;
    }
    
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

    public int choosePosition(byte card) {
        return choosePosition;
    }

    public byte replace(byte takeCard, boolean isDiscardCard) {
        int newPos = choosePosition;
        if (choosePosition == -1) {
            return takeCard;
        }
        return replaceCard(takeCard, newPos, isDiscardCard);
    }
    
    public void discard2deck() {
    	deadlock = true;
    	for (int i = 0; i < rackSize; i++) {
    		if (hand[i] != backupHand[i]) {
    			deadlock = false;
    			break;
    		}
    	}
    	System.arraycopy(hand, 0, backupHand, 0, rackSize);    	
    }
}
