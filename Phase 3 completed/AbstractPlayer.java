abstract class AbstractPlayer implements Player {
    protected Card[] hand;
    protected byte[] viewable;
    protected int cardSize;
    protected final int rackSize = 10;
    protected final int cardKey = 100;

    public AbstractPlayer(int size) {
        cardSize = size;
    }

    public void setHand(Card[] hand) {
        this.viewable = new byte[rackSize];
        this.hand = new Card[hand.length];
        for (int i = 0; i < hand.length; i++) {
            this.hand[i] = hand[i];
        }
    }

    protected Card replaceCard(Card newCard, int pos, boolean isDiscardCard) {
        Card returnCard = hand[pos];
        hand[pos] = newCard;
        if (isDiscardCard) {
            viewable[pos] = 3;
        } else {
            viewable[pos] = 1;
        }
        return returnCard;
    }

    public final Card[] getHand() {
        return hand;
    }
    
    public final void print() {
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
    
    public final void printAll() {
        for (int i = 0; i < rackSize; i++) {
            int val = hand[i].getValue();
            if (val < 10)
                System.out.print(" " + val + " ");
            else 
                System.out.print(val + " ");
        }
        System.out.println();
    }
    
    public void discardShuffle() {
        // do nothing 
    }
    
    public void discardAdd(Card card) {
        // do nothing 
    }
    
    public void discardRemove() {
        // do nothing 
    }
    
    public void playerCard(Card card, int id) {
        // do nothing
    }
}
