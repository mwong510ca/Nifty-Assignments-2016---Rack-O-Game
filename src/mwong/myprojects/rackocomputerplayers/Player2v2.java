package mwong.myprojects.rackocomputerplayers;


public class Player2v2 extends Player2 {
	protected AbstractHandAnalyzer analyzer2;

	public Player2v2(int size) {
		super(size);
		analyzer2 = new HandAnalyzer2(cardSize, rackSize, cardKey);
	}

	protected void handAnalyze() {
		analyzer2.analysisNow(hand, groupHand, gapCount, discardReplacement, rangeMax);
		System.arraycopy(analyzer2.getRangeMax(), 0, rangeMax, 0, rackSize + 2);
		System.arraycopy(analyzer2.getGapCount(), 0, gapCount, 0, rackSize);
		System.arraycopy(analyzer2.getDiscard(), 0, discardReplacement, 0, cardSize + 1);
		System.arraycopy(analyzer2.getGroupHand(), 0, groupHand, 0, rackSize);	
		
		offRange = 0;
        for (int i = 0; i < rackSize; i++) {
            if (gapCount[i] != 0) {
                offRange++;
            }
        }        
	}
}
