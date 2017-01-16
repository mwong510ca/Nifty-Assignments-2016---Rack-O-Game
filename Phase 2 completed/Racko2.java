import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Racko2 extends JPanel implements MouseListener, ActionListener{
    private static final long serialVersionUID = -4876351837154789884L;
	private LinkedList<Card> deck;
    private LinkedList<Card> discard;
    private Card[] humanHand;
    private Player2 compHand;
     
    private int selectedSlot = -1;
    JButton b1, b2, b3, b4, b5, b6, b7;
    Card takeCard = null;
    private BufferedImage img;
    public final String img_file = "images/backcard.png";
    private boolean deckPopped = false;
    private JLabel status;
    private final int cardSize = 40;
    private int playerScore, computerScore, roundCounter, playerWins, computerWins;
    private boolean userDealer;

    public Racko2() {
        compHand = new Player2(cardSize);
        status = new JLabel();
        status.setBorder(BorderFactory.createLineBorder(Color.RED));
        newGame();
        makeUI();
    }

    private void newGame() {
        userDealer = does_user_begin();
        playerScore = 0;
        computerScore = 0;
        roundCounter = 0;
        playerWins = 0;
        computerWins = 0;
        newRound();
    }
    
    private void newRound() {
        roundCounter++;
        deck = new LinkedList<Card>();
        discard = new LinkedList<Card>();
        humanHand = new Card[10];
        deckPopped = false;
        // Load deck and distribute cards
        load_deck(deck);
        shuffle();
        //printAll();
        Card[][] initialHands = deal_initial_hands();
        
        status.setForeground(Color.BLACK);
        status.setFont(new Font("Arial", Font.PLAIN, 13));
        // Determine who makes the first move
        if (userDealer) {
            status.setText("The computer went first.");
            System.out.println("The computer went first.");
            humanHand = initialHands[0];
            compHand.setHand(initialHands[1]);            
        } else {
            humanHand = initialHands[1];
            compHand.setHand(initialHands[0]);
            status.setText("You go first.");
            System.out.println("You go first.");
        }
        
        System.out.print("computer : ");
        compHand.print();
        
        add_card_to_discard(deck.pop());
        for (int i = 0; i < 10; i++) {
            humanHand[i] = new Card(humanHand[i].getValue(), 350, 350 - (30 * i));
        }
        
        if (userDealer) {
            computer_play();
        } else {
        	System.out.println();
        }
        userDealer = !userDealer;
    }
    
    /*
     * Shuffle the deck. If the deck size is 0, shuffle the discard pile
     * and put it back in the deck.
     */
    public void shuffle() { 
        Random rand = new Random();
        if (deck.size() == 0) {
            deck = discard;
            discard = new LinkedList<Card>();
        }
        
        for (int idx1 = 0; idx1 < deck.size(); idx1++) {
            int idx2 = rand.nextInt(deck.size());
            Card card1 = deck.get(idx1);
            Card card2 = deck.get(idx2);
            deck.set(idx1, card2);
            deck.set(idx2, card1);
        }
    }

    /*
     * Create the deck of cards with values 1 through cardSize
     */
    public void load_deck(LinkedList<Card> deck) {
        for (int i = 1; i <= cardSize; i++) {
            Card card = new Card(i, 0, 0);
            deck.add(card);
        }
    }

    /*
     * Check whether the player has racko, meaning that the cards are in
     * ascending order from index 0 to 9.
     */
    public boolean check_racko(Card[] rack) {
        int val = rack[0].getValue();
        for (int i = 1; i < 10; i++) {
            if (val < rack[i].getValue()) {
                val = rack[i].getValue();
            } else {
                return false;
            }
        }
        return true;
    }

    private int scoreHand(Card[] rack) {
        int score = 5;
        int val = rack[0].getValue();
        for (int i = 1; i < 10; i++) {
            if (val < rack[i].getValue()) {
                val = rack[i].getValue();
                score += 5;
            } else {
                break;
            }
        }
        return score;
    }

    private int scoreHandBonus(Card[] rack) {
        int val = rack[0].getValue();
        int counter = 1;
        int maxCounter = 0;
        for (int i = 1; i < 10; i++) {
            if (rack[i].getValue() == val + 1) {
                counter++;
            } else {
                if (counter > maxCounter) {
                    maxCounter = counter;
                }
                counter = 1;
            }
            val = rack[i].getValue();
        }
        if (counter > maxCounter) {
            maxCounter = counter;
        }
        
        if (maxCounter < 3) 
            return 75;
        
        switch (maxCounter) {
            case 3: 
                return 125;
            case 4: 
                return 175;
            case 5: 
                return 275;
            default: 
                return 475;
        }
    }

    /*
     * Deal the card by popping the top card in the deck.
     */
    public Card deal_card() {
        if (deck.isEmpty()) {
            return null;
        }
        return deck.pop();
    }

    /*
     * Deal the initial hands by alternating between the two hands 
     * (the computer's and user's) and distributing cards from the deck.
     */
    public Card[][] deal_initial_hands() {
        Card[][] cards = new Card[2][10];
        for (int i = 9; i >= 0; i--) {
            for (int players = 0; players < 2; players++) {
                cards[players][i] = deck.removeFirst();
            }
        }
        return cards;
    }

    /*
     * Decide whether the user or computer should begin by generating a
     * random number and letting the user begin if the value is greater
     * than 0.5.
     */
    public boolean does_user_begin() {
        Random rand = new Random();
        if (rand.nextInt(2) == 0) {
            return false;
        }
        return true;
    }

    /*
     * Print card values in the player's hand.
     */
    public void print_human_hand() {
        for (Card card : humanHand) {
            int val = card.getValue();
            if (val < 10)
                System.out.print(" " + val + " ");
            else 
                System.out.print(val + " ");
        }
        System.out.println();
    }

    /*
     * Search through the player's hand and replace the new card with the
     * selected card. Throw the selected card to the discard pile.
     */
    public boolean find_and_replace(Card newCard, int numberOfCard, Card[] rack) {
        int index = -1;
        for (int idx = 0; idx < 10; idx++) {
            if (rack[idx].getValue() == numberOfCard) {
                index = idx;
                break;
            }
        }
        discard.addFirst(rack[index]);
        rack[index] = newCard;
        return true;
    }

    /*
     * Put the card in the discard pile.
     */
    public void add_card_to_discard(Card card) {
        //compHand.discardAdd(card.getValue());
        discard.addFirst(card);
    }
    
    
    /*
     * ------------------------------ COMPUTER STRATEGY ----------------------
     */
    

    /*
     * Determine the computer's strategy. Return the computer's hand after it
     * makes its move.
     */
    public void computer_play() {
        if (deck.size() == 0) {
            shuffle();
            add_card_to_discard(deck.pop());
        }

        System.out.println("\nThe computer review " + discard.peek().getValue() + " in discard pile.");
        
        // Determine whether card on top of the discard pile can be placed into
        // ascending hand; otherwise, draw a card from the deck and determine
        // whether to discard it or use it
        if (compHand.determine_use(discard.peek().getValue(), true)) {
            System.out.println("The computer chose to keep the card " + discard.peek().getValue() + ".");
            Card discardCard = discard.pop();
            Card returnCard = compHand.replace(discardCard, true);
            add_card_to_discard(returnCard);
        } else {
            // Take a card from the deck and determine its use
            Card drawCard = deck.pop();
            System.out.println("The computer review a card from the deck pile.");
            if (compHand.determine_use(drawCard.getValue(), false)) {
            	System.out.println("The computer chose to keep the draw card.");
                Card returnCard = compHand.replace(drawCard, false);
                add_card_to_discard(returnCard);    
            } else {
                add_card_to_discard(drawCard);
            }
        }
        /*
        if (deck.size() == 0) {
            System.out.println("deck is empty");
        } else {
            System.out.print("deck : ");
            for (Card card : deck) {
                System.out.print(card.getValue() + " ");
            }
            System.out.println();
        }
        
        if (discard.size() == 0) {
            System.out.println("discard is empty");
        } else {
            System.out.print("discard : ");
            for (Card card : discard) {
                System.out.print(card.getValue() + " ");
            }
            System.out.println();
        }
        */
        System.out.println("The computer pleaced " + discard.peek().getValue() + " in discard pile.");
        System.out.print("computer : ");
        compHand.print();
        System.out.println();
    }
    
    /**
     *  USER INTERFACE METHODS
     */
    
    public void makeUI() {
        addMouseListener(this);
        
        b1 = new JButton("Choose Card from Discard Pile");
        b2 = new JButton("Choose Card from Deck");
        b3 = new JButton("Replace with your Selected Card");
        b4 = new JButton("Keep");
        b5 = new JButton("Discard");
        b6 = new JButton("Next Round");
        b7 = new JButton("New Game");
        
        //Listen for actions on buttons 1, 2, 3, 4, and 5.
        b1.addActionListener(this);
        b2.addActionListener(this);
        b3.addActionListener(this);
        b4.addActionListener(this);
        b5.addActionListener(this);
        b6.addActionListener(this);
        b7.addActionListener(this);
        
        add(b1);
        add(status);
        add(b2);
        add(b3);
        add(b4);
        add(b5);
        add(b6);
        add(b7);
        
        b3.setVisible(false);
        b4.setVisible(false);
        b5.setVisible(false);
        b6.setVisible(false);
        b7.setVisible(false);
        b1.setActionCommand("selectFromDis");
        b2.setActionCommand("selectFromDec");
        b3.setActionCommand("switch");
        b4.setActionCommand("keep");
        b5.setActionCommand("discard");
        b6.setActionCommand("newround");
        b7.setActionCommand("newgame");
        
        setFocusable(true);
        
        try {
            if (img == null) {
                img = ImageIO.read(new File(img_file));
            }
        } catch (IOException e) {
            System.out.println("Internal Error:" + e.getMessage());
        }
    
    }

    /*
     * Give buttons actions
     */
    public void actionPerformed(ActionEvent e) {
        // Select button to choose card from discard pile
        if ("selectFromDis".equals(e.getActionCommand())) {
            b1.setVisible(false);
            b2.setVisible(false);
            b3.setVisible(true);
            takeCard = discard.pop();
            System.out.println("The player chose " + takeCard.getValue() + " from the discard pile.");
            status.setText("You chose " + takeCard.getValue() + " from discard pile. Select a card to replace.");
        // Select button to choose card from deck
        } else if ("selectFromDec".equals(e.getActionCommand())) {
            b1.setVisible(false);
            b2.setVisible(false);
            b4.setVisible(true);
            b5.setVisible(true);
            deckPopped = true;
            status.setText("You are selecting from the deck.");
            deck.peek();            
        // Select button to replace a card in the hand with the selected card
        } else if ("switch".equals(e.getActionCommand()) && takeCard != null && selectedSlot != -1) {
            b1.setVisible(true);
            b2.setVisible(true);
            b3.setVisible(false);
            status.setText("Your card was replaced.");
                
            int orgValue = humanHand[selectedSlot].getValue();
                
            int orgX = humanHand[selectedSlot].getX();
            int orgY = humanHand[selectedSlot].getY();
            find_and_replace(takeCard, orgValue, humanHand);
            System.out.println("The player placed " + discard.peek().getValue() + " in discard pile.");
            
            humanHand[selectedSlot].setX(orgX);
            humanHand[selectedSlot].setY(orgY + 30);
            repaint();
            selectedSlot = -1;
            takeCard = null;
            
            if (check_racko(humanHand)) {
                b1.setVisible(false);
                b2.setVisible(false);
                playerScore += scoreHandBonus(humanHand);
                computerScore += scoreHand(compHand.getHand());
                playerWins++;
                if (playerScore >= 500 || computerScore >= 500) {
                    if (computerScore > playerScore) {
                        status.setText("Game ended - Computer win!");
                        status.setForeground(Color.RED);
                        status.setFont(new Font("Arial", Font.BOLD, 20));                        
                    } else if (computerScore < playerScore) {
                        status.setText("Game ended - You win!");
                        status.setForeground(Color.RED);
                        status.setFont(new Font("Arial", Font.BOLD, 20));
                    } else {
                        status.setText("Game ended - Oops! Tie.");
                        status.setForeground(Color.RED);
                        status.setFont(new Font("Arial", Font.BOLD, 20));
                    }
                    b7.setVisible(true);
                    
                    System.out.printf("human    - %3d : ", scoreHandBonus(humanHand));
                    print_human_hand();
                    System.out.printf("computer - %3d : ", scoreHand(compHand.getHand()));
                    compHand.printAll();
                    System.out.println("\nPlayer " + playerScore + "\tvs\tComputer " + computerScore);
                    System.out.println("--------------- Game Ended ---------------\n");
                } else {
                    status.setText("Round ended - You win!");
                    status.setForeground(Color.RED);
                    status.setFont(new Font("Arial", Font.BOLD, 20));
                    b6.setVisible(true);

                    System.out.printf("human    - %3d : ", scoreHandBonus(humanHand));
                    print_human_hand();
                    System.out.printf("computer - %3d : ", scoreHand(compHand.getHand()));
                    compHand.printAll();
                    System.out.println("Player " + playerScore + "\tvs\tComputer " + computerScore);
                    System.out.println("\n--------------- Round Ended ---------------\n");
                }
            } else {                    
                computer_play();
                if (check_racko(compHand.getHand())) {
                    b1.setVisible(false);
                    b2.setVisible(false);
                    playerScore += scoreHand(humanHand);
                    computerScore += scoreHandBonus(compHand.getHand());
                    computerWins++;
                    if (playerScore >= 500 || computerScore >= 500) {
                        if (computerScore > playerScore) {
                            status.setText("Game ended - Computer win!");
                            status.setForeground(Color.RED);
                            status.setFont(new Font("Arial", Font.BOLD, 20));                        
                        } else if (computerScore < playerScore) {
                            status.setText("Game ended - You win!");
                            status.setForeground(Color.RED);
                            status.setFont(new Font("Arial", Font.BOLD, 20));
                        } else {
                            status.setText("Game ended - Oops! Tie.");
                            status.setForeground(Color.RED);
                            status.setFont(new Font("Arial", Font.BOLD, 20));
                        }
                        b7.setVisible(true);
                        
                        System.out.printf("human    - %3d : ", scoreHand(humanHand));
                        print_human_hand();
                        System.out.printf("computer - %3d : ", scoreHandBonus(compHand.getHand()));
                        compHand.printAll();
                        System.out.println("Player " + playerScore + "\tvs\tComputer " + computerScore);
                        System.out.println("\n--------------- Game Ended ---------------\n");
                    } else {
                        status.setText("Round ended - Computer win!");
                        status.setForeground(Color.RED);
                        status.setFont(new Font("Arial", Font.BOLD, 20));
                        b6.setVisible(true);
                        
                        System.out.printf("human    - %3d : ", scoreHand(humanHand));
                        print_human_hand();
                        System.out.printf("computer - %3d : ", scoreHandBonus(compHand.getHand()));
                        compHand.printAll();
                        System.out.println("Player " + playerScore + "\tvs\tComputer " + computerScore);
                        System.out.println("\n--------------- Round Ended ---------------\n");
                    }
                } else if (deck.size() == 0) {
                    shuffle();
                    add_card_to_discard(deck.pop());
                }            
            }
        // Select button to keep card drawn from deck
        } else if ("keep".equals(e.getActionCommand())) {
            System.out.println("The player chose from the deck pile.");
            b4.setVisible(false);
            b5.setVisible(false);
            b3.setVisible(true);
            deckPopped = false;
            takeCard = deck.pop();
            status.setText("You chose " + takeCard.getValue() + " from deck pile. Select a card to replace.");
        // Select button to discard card drawn from deck
        } else if ("discard".equals(e.getActionCommand())) {
            b1.setVisible(true);
            b2.setVisible(true);
            b4.setVisible(false);
            b5.setVisible(false);
            deckPopped = false;
            takeCard = deck.pop();
            System.out.println("The player placed " + takeCard.getValue() + " in discard pile.");
            
            status.setText("Card discarded: " + takeCard.getValue());
            add_card_to_discard(takeCard);
            computer_play();
            if (check_racko(compHand.getHand())) {
                b1.setVisible(false);
                b2.setVisible(false);
                playerScore += scoreHand(humanHand);
                computerScore += scoreHandBonus(compHand.getHand());
                computerWins++;
                if (playerScore >= 500 || computerScore >= 500) {
                    if (computerScore > playerScore) {
                        status.setText("Game ended - Computer win!");
                        status.setForeground(Color.RED);
                        status.setFont(new Font("Arial", Font.BOLD, 20));                        
                    } else if (computerScore < playerScore) {
                        status.setText("Game ended - You win!");
                        status.setForeground(Color.RED);
                        status.setFont(new Font("Arial", Font.BOLD, 20));
                    } else {
                        status.setText("Game ended - Oops! Tie.");
                        status.setForeground(Color.RED);
                        status.setFont(new Font("Arial", Font.BOLD, 20));
                    }
                    b7.setVisible(true);
                    
                    System.out.printf("human    - %3d : ", scoreHand(humanHand));
                    print_human_hand();
                    System.out.printf("computer - %3d : ", scoreHandBonus(compHand.getHand()));
                    compHand.printAll();
                    System.out.println("Player " + playerScore + "\tvs\tComputer " + computerScore);
                    System.out.println("\n--------------- Game Ended ---------------");
                } else {
                    status.setText("Round ended - Computer win!");
                    status.setForeground(Color.RED);
                    status.setFont(new Font("Arial", Font.BOLD, 20));
                    b6.setVisible(true);
                    
                    System.out.printf("human    - %3d : ", scoreHand(humanHand));
                    print_human_hand();
                    System.out.printf("computer - %3d : ", scoreHandBonus(compHand.getHand()));
                    compHand.printAll();
                    System.out.println("Player " + playerScore + "\tvs\tComputer " + computerScore);
                    System.out.println("\n--------------- Round Ended ---------------\n");                    
                }                
            } else if (deck.size() == 0) {
                shuffle();
                add_card_to_discard(deck.pop());
            }
        } else if ("newround".equals(e.getActionCommand())) {
            newRound();
            
            b1.setVisible(true);
            b2.setVisible(true);
            b3.setVisible(false);
            b4.setVisible(false);
            b5.setVisible(false);
            b6.setVisible(false);
        } else if ("newgame".equals(e.getActionCommand())) {
            newGame();
            
            b1.setVisible(true);
            b2.setVisible(true);
            b3.setVisible(false);
            b4.setVisible(false);
            b5.setVisible(false);
            b7.setVisible(false);
        }
        
        repaint();
    }

    /*
     * Draw images.
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        g.setFont(new Font("Arial", Font.BOLD, 20));  
        g.drawString("Round " + roundCounter, 300, 60);
        g.setFont(new Font("Arial", Font.BOLD, 16));  
        g.drawString("Player  " + playerScore + " (" + playerWins + ")", 150, 60);
        g.drawString("Computer  " + computerScore + " (" + computerWins + ")", 450, 60);
        // Draw cards descending down from last slot to first
        g.setFont(new Font("Arial", Font.PLAIN, 16));  
        
        for (int i = 9; i >= 0; i--) {
            humanHand[i].draw(g);
            g.drawString(String.valueOf("          " + humanHand[i].getValue()), 
                    humanHand[i].getX() + 20 + (4 * humanHand[i].getValue()), 
                    humanHand[i].getY() + 25);
        }
        // Draw discard pile with top card showing
        g.drawString("Discard Pile", 30, 320);
        if (discard.size() > 0) {
            for (int i = discard.size() - 1; i >= 0; i--) {
                discard.get(i).setX(20);
                discard.get(i).setY(330 + (i * 2));
                discard.get(i).draw(g);
                g.drawString(String.valueOf("          " + discard.get(i).getValue()), 
                        discard.get(i).getX() + 20 + (4 * discard.get(i).getValue()), 
                        discard.get(i).getY() + 25);
            }
        }
        // Draw deck with top card showing when player has chosen to draw a card
        g.drawString("Deck Pile", 30, 90);
        if (deck.size() > 0) {
            g.drawImage(img, 20, 100, 300, 201, null);
        }
        if (deckPopped == true) {
            Card popped = deck.peek();
            popped.setX(20);
            popped.setY(100);
            popped.draw(g);
            g.drawString(String.valueOf("          " + popped.getValue()), popped.getX() + 
                    20 + (4 * popped.getValue()),popped.getY() + 25);
        }
    }

    /*
     * Allow user to click on a card to select it. Selected card will move up to indicate it
     * has been selected and move back down to indicate it has been unselected.
     */
    @Override
    public void mouseClicked(MouseEvent e) {
        // TODO Auto-generated method stub
        if (takeCard != null) {
            if (selectedSlot == -1) {
                for (int i = 9; i >= 0; i--) {
                    if (e.getX() > humanHand[i].getX() && e.getX() < humanHand[i].getX() + 300
                            && e.getY() > humanHand[i].getY() && e.getY() < humanHand[i].getY() + 30) {
                        
                        humanHand[i].setY(humanHand[i].getY() - 30);
                        selectedSlot = i;
                        repaint();
                    }
                } 
            } else if (e.getX() > humanHand[selectedSlot].getX() && e.getX() < humanHand[selectedSlot].getX() + 300
                    && e.getY() > humanHand[selectedSlot].getY() && e.getY() < humanHand[selectedSlot].getY() + 60) {    
                humanHand[selectedSlot].setY(humanHand[selectedSlot].getY() + 30);
                selectedSlot = -1;
                repaint();
            }
        }
    }
    
    @Override
    public void mouseEntered(MouseEvent e) {
        // TODO Auto-generated method stub

    }
    @Override
    public void mouseExited(MouseEvent e) {
        // TODO Auto-generated method stub

    }
    @Override
    public void mousePressed(MouseEvent e) {
        // TODO Auto-generated method stub

    }
    @Override
    public void mouseReleased(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    public void callTimer() {
        
    }
}
