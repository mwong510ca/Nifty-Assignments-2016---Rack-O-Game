package mwong.myprojects.rackocomputerplayers;

public abstract class AbstractHandAnalyzer {
	int[] groupHand;
	int[] gapCount;
	int[] discard;
	int[] rangeMax;
	int rating;
	int cardSize, rackSize, cardKey;

	public AbstractHandAnalyzer(int cardSize, int rackSize, int cardKey) {
		this.cardSize = cardSize;
		this.rackSize = rackSize;
		this.cardKey = cardKey;
		rangeMax = new int[rackSize + 2];
		gapCount = new int[rackSize];
		discard = new int[cardSize + 1];
		groupHand = new int[rackSize];
	}
	
	public void analysisNow(byte[] hand, int[] groupHand, int[] gapCount, int[]discardReplacement, int[] rangeMax) {
		System.arraycopy(rangeMax, 0, this.rangeMax, 0, rackSize + 2);
		System.arraycopy(gapCount, 0, this.gapCount, 0, rackSize);
		System.arraycopy(discardReplacement, 0, this.discard, 0, cardSize + 1);
		System.arraycopy(groupHand, 0, this.groupHand, 0, rackSize);		
		review(hand);
		
		rating = 10;
		for (int i = 0; i < rackSize; i++) {
			int val = this.gapCount[i];
			if (val == 0) {
				rating++;
			}
		}
	}
	
	abstract void review(byte[] hand);

	public int[] getGroupHand() {
		return groupHand;
	}

	public int[] getGapCount() {
		return gapCount;
	}

	public int[] getDiscard() {
		return discard;
	}

	public int[] getRangeMax() {
		return rangeMax;
	}

	public int getRating() {
		return rating;
	}
}
