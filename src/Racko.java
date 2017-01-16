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

@SuppressWarnings("serial")
public class Racko extends JPanel implements MouseListener, ActionListener{
	private LinkedList<Card> deck;
	private LinkedList<Card> discard;
	public boolean playing = false;
	private Card[] humanHand;
	private Card[] compHand;
	private int selectedSlot = -1;
	JButton b1, b2, b3, b4, b5, b6, b7;
	Card takeCard = null;
	private BufferedImage img;
	public final String img_file = "backcard.png";
	private boolean deckPopped = false;
	private JLabel status;
	private final int cardSize = 40;
	private int playerScore, computerScore, roundCounter;
	private boolean userDealer;

	public Racko() {
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
		newRound();
	}
	
	private void newRound() {
		roundCounter++;
		deck = new LinkedList<Card>();
		discard = new LinkedList<Card>();
		humanHand = new Card[10];
		compHand = new Card[10];
		playing = true;
		deckPopped = false;
		// Load deck and distribute cards
		load_deck(deck);
		shuffle();
		printAll();
		Card[][] initialHands = deal_initial_hands();
		add_card_to_discard(deck.pop());
		humanHand = initialHands[1];
		compHand = initialHands[0];
		printAll();
		
		for (int i = 0; i < 10; i++) {
			humanHand[i] = new Card(humanHand[i].getValue(), 350, 350 - (30 * i));
		}
		
		status.setForeground(Color.BLACK);
		status.setFont(new Font("Arial", Font.PLAIN, 13));
		// Determine who makes the first move
		if (userDealer) {
			status.setText("The computer went first.");
			System.out.println("The computer went first.");
			// Check that the deck size isn't 0 before the computer makes a move.
			compHand = computer_play(compHand);
		} else {
			status.setText("You go first.");
			System.out.println("You go first.");
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
		
		System.out.println("max counter " + maxCounter);
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
	public void print_top_to_bottom(Card[] rack) {
		for (Card card : rack) {
			if (card != null) {
				System.out.print(card.getValue() + " ");
			}
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
		//System.out.println("Replace Discard Card: " + discard.peek().getValue());
		rack[index] = newCard;
		return true;
	}

	/*
	 * Put the card in the discard pile.
	 */
	public void add_card_to_discard(Card card) {
		discard.addFirst(card);
		//System.out.println("Immediate Discard Card: " + discard.peek().getValue());
	}
	
	public void printAll() {
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
		
		System.out.print("computer : ");
		print_top_to_bottom(compHand);
		System.out.print("human    : ");
		print_top_to_bottom(humanHand);
		System.out.println();
		
	}
	
	/*
	 * ------------------------------ COMPUTER STRATEGY ----------------------
	 */
	

	/*
	 * Determine the computer's strategy. Return the computer's hand after it
	 * makes its move.
	 */
	public Card[] computer_play(Card[] hand) {
		printAll();
		if (deck.size() == 0) {
	    	shuffle();
	    	add_card_to_discard(deck.pop());
	    	printAll();
	    }
		
		// Check that the deck size isn't 0 before the computer makes a move.
		int[] bestAscendingHand = new int[10]; // Used to keep track of cards to keep

	    // Marks the cards that make it impossible to win for replacement; otherwise,
	    // keep the card value the same in the array
	    for (int i = 0; i < 10; i++) {
	    	if ((i >= 0 && i < (9 - (cardSize - hand[i].getValue()))) || 
	    			(i <= 9 && hand[i].getValue() < i + 1)) {
	    		bestAscendingHand[i] = -1;
	    	} else {
	    		bestAscendingHand[i] = hand[i].getValue();
	    	}
	    }

	    // An array with cards to be kept and cards to be replaced
	    bestAscendingHand = find_ascending_hand(bestAscendingHand);

	    // Determine whether card on top of the discard pile can be placed into
	    // ascending hand; otherwise, draw a card from the deck and determine
	    // whether to discard it or use it
	    if (determine_use(discard.peek().getValue(), bestAscendingHand)) {
	    	System.out.println("The computer chose from the discard pile.");
	    	Card discardedCard = discard.pop();
	    	int newPos = choose_position(discardedCard.getValue(), bestAscendingHand);
	    	bestAscendingHand[newPos] = discardedCard.getValue();
	    	find_and_replace(discardedCard, hand[newPos].getValue(), hand);
	    } else {
	    	// Take a card from the deck and determine its use
	    	Card drawCard = deck.pop();
	    	System.out.println("The computer chose from the deck.");
	    	if (determine_use(drawCard.getValue(), bestAscendingHand)) {
	    		int newPos = choose_position(drawCard.getValue(), bestAscendingHand);
	    		bestAscendingHand[newPos] = drawCard.getValue();
	    		find_and_replace(drawCard, hand[newPos].getValue(), hand);	
	    	} else {
	    		add_card_to_discard(drawCard);
	    	}
	    }
	    printAll();
	    return hand;
	}

	/**
	 * @param hi - index of the second card
	 * @param lo - index of the first card
	 * @param higherVal - value of the higher card
	 * @param lowerVal - value of the lower card
	 * @return true if there are enough cards that can go between the two cards 
	 */
	private boolean check_if_possible(int hi, int lo, int higherVal, int lowerVal) {
		// Checks if there are enough possible cards to put between the
		// lower valued card and higher valued card
		if (hi - lo > higherVal - lowerVal) {
			return false;
		}

		return true;
	}

    /*
     * Determine which cards in the current hand should be kept. Mark the cards
     * to be replaced with -1 and return the hand. The cards are compared by which
     * card has the greater range of values.
     */
    private int[] find_ascending_hand(int[] hand) {
        int[] ascendingHand = new int[10];
        int loIndex = 0;    // Index of the lowest valued card kept at a time
        int lower = 0;      // loIndex + 1 to remain noninclusive of that index
        int lowerVal = 0;   // Value of the card at the loIndex but incremented by one to remain noninclusive

        // Make the array take in all the values
        for (int a = 0; a < hand.length; a++) {
            ascendingHand[a] = hand[a];
        }

        // Go through every card except the last card and compare it with all other cards
        // after it that are lower
        for (int i = 0; i < 9; i++) {
            // If the card is already marked for removal, don't check it
            if (ascendingHand[i] == -1) {
                continue;
            } else {
                // Determine whether the card can be kept based on what the previous
                // card's value and position
                if (!check_if_possible(i, lower, hand[i], lowerVal)) {
                    ascendingHand[i] = -1;
                    continue;
                }
                
                for (int j = i + 1; j < 10; j++) {
                    // Check cards that have lower values than the current card
                    if (hand[j] < hand[i] && ascendingHand[j] != -1) {
                        // Check if there are more spaces between the card and the end
                        // or more spaces between the card and the last card that was kept
                        // This comparison is done with both the current card and the card
                        // it is being compared with
                        if (j - lower >= 9 - j) { // More cards between the card and the last KEPT card
                            if (i - lower >= 9 - i) { // More cards between the card and the last KEPT card
                                // Check if the current card or the other card has a
                                // higher range; if the other card is better, make the
                                // current card -1, otherwise keep the current card
                                // and mark the other card -1
                                if (hand[j] - hand[loIndex] > hand[i] - hand[loIndex]) {
                                    //Check whether other card has a possible place in the hand
                                    if (check_if_possible(j, lower, hand[j], lowerVal)) {
                                        ascendingHand[i] = -1;
                                        break;
                                    } else {
                                        ascendingHand[i] = hand[i];
                                        ascendingHand[j] = -1;
                                    }
                                } else {
                                    ascendingHand[i] = hand[i];
                                    ascendingHand[j] = -1;
                                }
                            } else if (i - lower < 9 - i) { // More cards between the card and the last card
                                if (hand[j] - hand[loIndex] > cardSize - hand[i]) {
                                    //Check whether other card has a possible place in the hand
                                    if (check_if_possible(j, lower, hand[j], lowerVal)) {
                                        ascendingHand[i] = -1;
                                        break;
                                    } else {
                                        ascendingHand[i] = hand[i];
                                        ascendingHand[j] = -1;
                                    }
                                } else {
                                    ascendingHand[i] = hand[i];
                                    ascendingHand[j] = -1;
                                }
                            }
                        } else if (j - lower < 9 - j) { // More cards between the card and the last card
                            if (i - lower >= 9 - i) { // More cards between the card and the last KEPT card
                                if (cardSize - hand[j] > hand[i] - hand[loIndex]) {
                                    //Check whether other card has a possible place in the hand
                                    if (check_if_possible(j, lower, hand[j], lowerVal)) {
                                        ascendingHand[i] = -1;
                                        break;
                                    } else {
                                        ascendingHand[i] = hand[i];
                                        ascendingHand[j] = -1;
                                    }
                                } else {
                                    ascendingHand[i] = hand[i];
                                    ascendingHand[j] = -1;
                                }
                            } else if (i - lower < 9 - i) { // More cards between the card and the last card
                                if (cardSize - hand[j] > cardSize - hand[i]) {
                                    //Check whether other card has a possible place in the hand
                                    if (check_if_possible(j, lower, hand[j], lowerVal)) {
                                        ascendingHand[i] = -1;
                                        break;
                                    } else {
                                        ascendingHand[i] = hand[i];
                                        ascendingHand[j] = -1;
                                    }
                                } else {
                                    ascendingHand[i] = hand[i];
                                    ascendingHand[j] = -1;
                                }
                            }
                        }
                    }
                }

                // If the current card was not marked for replacement, increment the lower
                // index variables to account for the current card being the latest addition
                // to the best ascending hand
                if (ascendingHand[i] != -1) {
                    loIndex = i;
                    lower = i + 1;
                    lowerVal = ascendingHand[i] + 1;
                }
            }
        }
        // Make sure that the last card in the hand can be used
        if (!check_if_possible(9, lower, hand[9], lowerVal)) {
            ascendingHand[9] = -1;
        }
        return ascendingHand;
    }

    /*
     * Check that the card is within range.
     */
    private boolean within_range(int drawCard, int hi, int lo) {
        if (drawCard < hi && drawCard > lo) {
            return true;
        }
        return false;
    }

    /*
     * Determine the use of the card by determining whether the current card's value
     * can go between any two cards in the hand that have cards to be replaced between
     * them.
     */
    private boolean determine_use(int value, int[] ascHand) {
        int i = 0;
        int lo = 0;
        int lowerIndex = 0;
        int hi = 0;
        while (i < 10) {
            // Look for cards that are to be kept and determine whether there are
            // cards that need to be replaced between the two
            int numOfCards = 0;
            if (ascHand[i] != -1) {
                hi = i;
                numOfCards = hi - lowerIndex;
                lowerIndex = i + 1;
            } else {
                if (i == 9) {
                    hi = 9;
                    numOfCards = hi + 1 - lowerIndex;
                }
            }
            // Determine whether there are cards that need to be replaced between the
            // two cards and if the current card's value falls between the two cards'
            if (numOfCards != 0) {
                if (ascHand[lo] != -1 && ascHand[hi] != -1) {
                    if (within_range(value, ascHand[hi], ascHand[lo])) {
                        return true;
                    }
                } else if ((ascHand[lo] == -1 && within_range(value, ascHand[hi], 0)) || 
                        (ascHand[hi] == -1 && within_range(value, cardSize + 1, ascHand[lo]))) {
                	// Check looking at the cards from 0 to the current card's value or the
	                // current card's value to cardSize
	                
                	return true;
                }
            }

            // Update to check for the next range
            if (ascHand[i] != -1) lo = i;
            i++;
        }
        return false;
    }

    /*
     * Choose a position for the card to be placed. This is assuming that it has
     * already been determined that the card can be used. Look for the range it falls
     * in and place the card next to the card with the closer value.
     */
    private int choose_position(int card, int[] bestAscendingHand) {
        int i = 0;
        int lo = 0;
        int hi = 0;
        while (i < 10) {
            if (bestAscendingHand[i] != -1) {
                hi = i;
                if (lo == 0 && bestAscendingHand[lo] == -1) {
                    if (within_range(card, bestAscendingHand[hi], 0)) {
                        // If the card is closer in value to the higher card,
                        // return the index of the card behind the higher card
                        if (bestAscendingHand[hi] - card < card) {
                            bestAscendingHand[hi - 1] = card;
                            return hi - 1;
                        }
                        else {
                            bestAscendingHand[0] = card;
                            return 0;
                        }
                    }
                } else {
                    if (within_range(card, bestAscendingHand[hi], bestAscendingHand[lo])) {
                        // If the card is closer in value to the lower card,
                        // return the index of the card in front the lower card
                        if (bestAscendingHand[hi] - card < 
                                card - bestAscendingHand[lo]) {
                            bestAscendingHand[hi - 1] = card;
                            return hi - 1;
                        } else {
                            bestAscendingHand[lo + 1] = card;
                            return lo + 1;
                        }
                    }
                }
            // Determine whether the card should be in the last slot or closer to the
	        // lower card
	        } else if (i == 9 && bestAscendingHand[9] == -1) {
            	if (within_range(card, cardSize + 1, bestAscendingHand[lo])) {

                    if (cardSize - card < card - bestAscendingHand[lo]) {
                        bestAscendingHand[9] = card;
                        return 9;
                    } else {
                        bestAscendingHand[lo + 1] = card;
                        return lo + 1;
                    }
                }
            }

            // Update to check for the next range
            if (bestAscendingHand[i] != -1) lo = i;

            i++;
        }
        return -1;
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
			status.setText("You chose from the discard pile. Select a card to replace.");
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
			humanHand[selectedSlot].setX(orgX);
			humanHand[selectedSlot].setY(orgY + 30);
			repaint();
			selectedSlot = -1;
			takeCard = null;
			
			if (check_racko(humanHand)) {
				b1.setVisible(false);
				b2.setVisible(false);
				status.setText("You win!");
				status.setForeground(Color.RED);
				status.setFont(new Font("Arial", Font.BOLD, 20));
				System.out.println("Round " + roundCounter + "\t" + playerScore + " vs " + computerScore);
				playerScore += scoreHandBonus(humanHand);
				computerScore += scoreHand(compHand);
				System.out.println("Round " + roundCounter + "\t" + playerScore + " vs " + computerScore);
				if (playerScore >= 500 || computerScore >= 500) {
					b7.setVisible(true);
				} else {
					b6.setVisible(true);
				}
			} else {					
				compHand = computer_play(compHand);
				if (check_racko(compHand)) {
					b1.setVisible(false);
					b2.setVisible(false);
					status.setText("Computer wins!");
					status.setForeground(Color.RED);
					status.setFont(new Font("Arial", Font.BOLD, 20));
					System.out.println("Round " + roundCounter + "\t" + playerScore + " vs " + computerScore);
					playerScore += scoreHand(humanHand);
					computerScore += scoreHandBonus(compHand);
					System.out.println("Round " + roundCounter + "\t" + playerScore + " vs " + computerScore);
					if (playerScore >= 500 || computerScore >= 500) {
						b7.setVisible(true);
					} else {
						b6.setVisible(true);
					}
				} else if (deck.size() == 0) {
					shuffle();
					add_card_to_discard(deck.pop());
					printAll();
				}			
			}
		// Select button to keep card drawn from deck
		} else if ("keep".equals(e.getActionCommand())) {
			b4.setVisible(false);
			b5.setVisible(false);
			b3.setVisible(true);
			deckPopped = false;
			takeCard = deck.pop();
			status.setText("Card kept. Select a card to replace with your new card.");
			System.out.println("Card kept");
		// Select button to discard card drawn from deck
		} else if ("discard".equals(e.getActionCommand())) {
			b1.setVisible(true);
			b2.setVisible(true);
			b4.setVisible(false);
			b5.setVisible(false);
			deckPopped = false;
			takeCard = deck.pop();
			status.setText("Card discarded: " + takeCard.getValue());
			add_card_to_discard(takeCard);
			compHand = computer_play(compHand);
			if (check_racko(compHand)) {
				b1.setVisible(false);
				b2.setVisible(false);
				status.setText("Computer wins!");
				status.setForeground(Color.RED);
				status.setFont(new Font("Arial", Font.BOLD, 20));
				System.out.println("Round " + roundCounter + "\t" + playerScore + " vs " + computerScore);
				playerScore += scoreHand(humanHand);
				computerScore += scoreHandBonus(compHand);
				System.out.println("Round " + roundCounter + "\t" + playerScore + " vs " + computerScore);
				if (playerScore >= 500 || computerScore >= 500) {
					b7.setVisible(true);
				} else {
					b6.setVisible(true);
				}
			} else if (deck.size() == 0) {
			    shuffle();
			    add_card_to_discard(deck.pop());
			    printAll();
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
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		// Draw cards descending down from last slot to first
		for (int i = 9; i >= 0; i--) {
			humanHand[i].draw(g);
			g.drawString(String.valueOf(humanHand[i].getValue()), 
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
				g.drawString(String.valueOf(discard.get(i).getValue()), 
						discard.get(i).getX() + 20 + (4 * discard.get(i).getValue()), 
						discard.get(i).getY() + 25);
			}
		}
		// Draw deck with top card showing when player has chosen to draw a card
		g.drawString("Deck Pile", 30, 90);
		if (deck.size() > 0) {
			g.drawImage(img, 5, 70, 340, 250, null);
		}
		if (deckPopped == true) {
			Card popped = deck.peek();
			popped.setX(20);
			popped.setY(100);
			popped.draw(g);
			g.drawString(String.valueOf(popped.getValue()), popped.getX() + 
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
			for (int i = 9; i >= 0; i--) {
				if (selectedSlot == -1) {
					if (e.getX() > humanHand[i].getX() && e.getX() < humanHand[i].getX() + 300
							&& e.getY() > humanHand[i].getY() && e.getY() < humanHand[i].getY() + 30) {
						
						humanHand[i].setY(humanHand[i].getY() - 30);
						selectedSlot = i;
						repaint();
					}
				} else if (selectedSlot == i) {
					if (e.getX() > humanHand[i].getX() && e.getX() < humanHand[i].getX() + 300
							&& e.getY() > humanHand[i].getY() && e.getY() < humanHand[i].getY() + 60) {
						
						humanHand[i].setY(humanHand[i].getY() + 30);
						selectedSlot = -1;
						repaint();
					}
				}
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
