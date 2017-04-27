package mwong.myprojects.rackocomputerplayers;

abstract class AbstractPlayer implements Player {
	protected byte[] hand;
	protected byte[] viewable;
	protected int cardSize;
	protected final int rackSize = 10;
	protected final int cardKey = 100;

	public AbstractPlayer(int size) {
		cardSize = size;
	}

	public void setHand(byte[] hand) {
		this.viewable = new byte[rackSize];
		this.hand = new byte[hand.length];
		for (int i = 0; i < hand.length; i++) {
			this.hand[i] = hand[i];
		}
	}

	public void setPositionId(int id) {
		// do nothing
	}
	
	public void setWinningScore(int score) {
		// do nothing
	}

	public void setAggressive(boolean flag) {
		// do nothing
	}

	public void setPlayerScore(int id, int score) {
		// do nothing
	}

	public void reset() {
		// do nothing 
	}

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

	public final byte[] getHand() {
		return hand.clone();
	}

	public final void printAll() {
		for (int i = 0; i < rackSize; i++) {
    		int val = hand[i];
    		if (val < 10)
    			System.out.print(" " + val + " ");
    		else 
    			System.out.print(val + " ");
		}
		System.out.println();
	}
	
	public void discard2deck() {
		// do nothing 
	}
	
	public void discardAdd(byte card, int id) {
		// do nothing 
	}
	
	public void discardRemove() {
		// do nothing 
	}
	
	public void deckRemove(int id) {
		// do nothing 
	}

	public void playerCard(byte card, int id, int slot) {
		// do nothing
	}
	
	public void playerCard(int id, int slot) {
		// do nothing
	}
}