package mwong.myprojects.rackocomputerplayers;


public class Player3 extends Player2v2 {
    private int[] discardPile;
    private int[] deckPile;
    private int discardCount;
    private int deckIdx;
    private byte[] cardsStatus;
    private boolean predictNextCard, expandRange, almostWin;
	private int idShift, numPlayer;
	private int winScore, myScore;// numTurn;//, sort2win;
	private int[] otherScores;
	private RangeOptimizer optimizer;
	
	public Player3(int size) {
		super(size);
		optimizer = new RangeOptimizer(cardSize, rackSize, cardKey, aveRange);
		numPlayer = 2 + (cardSize - 40) / 10;
        idShift = 0;
		reset();
	}

    public String toString() {
		return "Hard";
	}

    public void reset() {
     	deckPile = new int[20];
		discardPile = new int[20];
    	deckIdx = -1;
		discardCount = 0;
		predictNextCard = false;    	
		cardsStatus = new byte[cardSize + 1];	
		otherScores = new int[numPlayer - 1];
	}
    
    public void setPositionId(int id) {
    	idShift = numPlayer - id;
    }
    
    public void setWinningScore(int score) {
    	super.setWinningScore(score);
    	winScore = score;
    }
    
	public void setPlayerScore(int id, int score) {
		int playerId = transId(id);
		if (playerId == -1) {
			myScore = score;
		} else {
			otherScores[playerId] = score;
		}
	}
    
    public void setHand(byte[] hand) {
    	super.setHand(hand);    	
    	int otherScore2win = 0;
    	for (int i = 0; i < numPlayer - 1; i++) {
			if (otherScores[i] > otherScore2win) {
				otherScore2win = otherScores[i];
			}
		}
    	
    	aggressivePlayer = false;
    	almostWin = false;
    	
    	if (myScore + 45 >= winScore) {
    		if (otherScore2win + 75 < winScore) {
    			almostWin = true;
    		}
    	} else if (myScore + 75 >= winScore) {
    		if (otherScore2win + 150 < myScore) {
    			almostWin = true;
    		} else if (otherScore2win + 25 > winScore + 75) {
    			aggressivePlayer = true;
    		}
    	} else if (otherScore2win  + 30 >= winScore) {
    		if (myScore + 75 < winScore) {
    			aggressivePlayer = true;
    		}
    	} else if (otherScore2win  + 75 >= winScore) {
    		if (myScore + 150 < winScore) {
    			aggressivePlayer = true;
    		}
    	} else if (myScore - otherScore2win > 200) {
    		aggressivePlayer = true;
    	}
    	//System.out.println(almostWin + "\t" + aggressivePlayer + "\t" + winScore + " " + myScore + " " + otherScore2win);
    	// 0 - unknown/drawPile, 1 - discardPile, 2 - self; 4 - player 1, 8 - player 2, 16 - player 3, 32 - unknown player
		for (int i = 0; i < hand.length; i++) {
			cardsStatus[hand[i]] = 2;
		}
	}

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
		if (fillSlot == rackSize) 
			fillSlot--;
		boolean offRangeValue = false;
		if (fillSlot > count + 3) {
			offRangeValue = true;
		}
		
		int lastSortedSlot = (int) Math.floor(hand[count - 1] / Math.ceil(cardSize / rackSize));
		if (lastSortedSlot == rackSize) 
			lastSortedSlot--;
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
					if (checkSlot == rackSize) 
						checkSlot--;
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
	
    public boolean determineUse(byte value, boolean isDiscardCard) {
    	if (almostWin) {
    		return prioritySorting(value, isDiscardCard);
    	}
    	//System.out.println("Hard : " + isDiscardCard);
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
        		optimizer.optimizeMore(aveRange, hand, gapCount, discardReplacement, drawReplacement);
        	}
        }
        
    	boolean result = secondCheck(value, isDiscardCard);
    	if (isDiscardCard) {
            cardsStatus[value] = 1;
    	} else {
    		cardsStatus[value] = 0;
    	}
    	if (result || !isDiscardCard) {
    		//System.out.println("======= end =======\n");
    	}
     	return result;
    }

    private void optimizeRange(boolean expandRange) {
    	if (offRange > 0) {
        	optimizer.optimizeNow(offRange, aggressivePlayer, hand, gapCount, rangeMax, 
					discardReplacement, drawReplacement);
        }
        if (expandRange) {
    		optimizer.optimizeMore(aveRange, hand, gapCount, discardReplacement, drawReplacement);
    	}
    	
    }
    
    public byte replace(byte takeCard, boolean isDiscardCard) {
        byte card = super.replace(takeCard, isDiscardCard);
        return card;
    }
    
    protected byte replaceCard(byte newCard, int pos, boolean isDiscardCard) {
    	byte returnCard = super.replaceCard(newCard, pos, isDiscardCard);
		return returnCard;
	}
    
    private boolean secondCheck(byte value, boolean isDiscardCard) {
    	boolean secondCheck = hasReplacement(value, isDiscardCard);
        if (!secondCheck) {
        	if (value <= aveRange) { 
        		if (gapCount[0] == 0 && value < hand[0] && hand[0] > aveRange) {
        			if (gapCount[1] > 0) {
        				choosePosition = 0;
        				return true;
        			}
        		}
        	}
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
        cardsStatus[currCard] = 0;
        reviewHand();	
		int newOffRange = offRange;
		replaceCard(currCard, replacement, false);
		cardsStatus[tempCard] = 0;
        viewable = backup;
        reviewHand();	
    	
		if (newOffRange <= currOffRange) {
			if (newOffRange == 0 || !isDiscardCard || !predictNextCard) {
				choosePosition = replacement;
				return true;
			}
        	
			byte nextValue = (byte) deckPile[deckIdx];
			boolean nextCheck = hasReplacement(nextValue, false);
			optimizeRange(expandRange);
        	if (!nextCheck) {
	        	choosePosition = replacement;
				return true;
	        }
        	
	        int replacement2 = choosePosition;
	        byte currCard2 = hand[replacement2];
	        byte tempCard2 = nextValue;
	        byte[] backup2 = new byte[rackSize];
	        System.arraycopy(viewable, 0, backup2, 0, rackSize);
	        replaceCard(tempCard2, replacement2, false);
	        cardsStatus[currCard2] = 0;
	        reviewHand();	
			int newOffRange2 = offRange;
			replaceCard(currCard2, replacement2, false);
			cardsStatus[tempCard2] = 0;
	        viewable = backup2;
	        reviewHand();
        	if (newOffRange2 < newOffRange) {
				return false;
	        } else {
	        	optimizeRange(expandRange);
	        	choosePosition = replacement;
				return true;
	        }
        }
		return false;
    }

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
	
	public void discardAdd(byte card, int id) {
		int value = card;
		cardsStatus[value] = 1;
		discardPile[discardCount++] = value;
	}
		
	public void discardRemove() {
		cardsStatus[discardPile[discardCount - 1]] = 32;
		discardCount--;
	}
	
	public void deckRemove(int id) {
		if (deckIdx > -1) {
			deckIdx++;
		}
	}
	
	public void playerCard(byte card, int id, int slot) {
		int playerId = transId(id);
		if (playerId < 0) {
			return;		
		}
		cardsStatus[card] = (byte) Math.pow(2, playerId + 2);
	}
	
	public void playerCard(int id, int slot) {
		int playerId = transId(id);
		if (playerId < 0) {
			return;
		}
		if (deckIdx > -1 && deckIdx < 20) {
			byte card = (byte) deckPile[deckIdx];
			playerCard(card, id, slot);
		} else if (deckIdx >= 20) {
			System.err.println("\t\tERROR: " + deckIdx);
			System.exit(0);
		}
	}

	private int transId(int id) {
		if (id == -1) {
			return -1;
		}
		return ((id + idShift) % numPlayer) - 1;
	}	
}
