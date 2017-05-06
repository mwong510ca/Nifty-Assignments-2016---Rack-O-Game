"""
" Engine is the QThread of for appRacko.  It handle the game features, computer player,
" and user player decision making and update the appRacko user interface.
"
" author Meisze Wong
"        www.linkedin.com/pub/macy-wong/46/550/37b/
"        github.com/mwong510ca/RackoGame-ComputerStrategy
"""

# !/usr/bin/env python3

import time
from PyQt5.QtCore import pyqtSignal, QThread
from random import randrange

# Globals
SCORES = [0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50]
SCORES_BONUS = [0, 75, 75, 125, 175, 275, 475]


class Engine(QThread):
    scoresUpdate = pyqtSignal(int, list, bool)
    deckPile = pyqtSignal(int, int, bool)
    discardPile = pyqtSignal(int, int, bool)
    rackSlotColor = pyqtSignal(int, int, int, bool)
    rackSlotValue = pyqtSignal(int, int, str)
    gameStatus = pyqtSignal(str)
    actionLock = pyqtSignal(bool)
    humanPlay = pyqtSignal(int)
    autoRoundEnd = pyqtSignal()
    engineStop = pyqtSignal()

    def __init__(self, size, winning_score):
        super(Engine, self).__init__()
        self._isRunning = False
        self._rack_size = size
        self._winning_score = winning_score

        # initialize local variables
        self.is_active = False
        self.speed_deal = 0
        self.speed_play = 0
        self.delay_new_round = 0
        self.show_replacement = False

        self.action = -1
        self.racko_size = 0
        self.deck = []
        self.deck_discard = []
        self.game_end = False

        self.number_of_players = 0
        self.player_name = []
        self.player_list = []
        self.player_layout = []
        self.player_scores = []
        self.player_hand = []
        self.player_hand_viewable = []

        self.round_number = -1
        self.starting_player = -1
        self.active_player = -1
        self.human_discard_card = -1
        self.human_Deck_card = -1
        self.human_response_slot = -1
        self.auto_run = False
        self.setDefaultSpeed()

    def setup(self, player_list, player_name, player_layout, card_size, show_replacement):
        self._isRunning = False
        self.is_active = False
        self.auto_run = False
        self.action = 1
        self.number_of_players = len(player_list)
        self.gameStatus.emit("Score " + str(self._winning_score) + " to win the game.")
        self.gameStatus.emit(str(self.number_of_players) + " players Rack-o!  Use card 1 - "
                             + str(card_size) + ".")

        player_str = "\nGui position from left: User"
        for idx in range(1, self.number_of_players):
            if player_list[idx] is not None:
                player_list[idx].setWinningScore(self._winning_score)
                player_str = player_str + " -> " + str(player_list[idx])
            else:
                player_str = player_str + " -> User2"
        print(player_str)

        self.counter_75 = 0
        self.counter_125 = 0
        self.counter_175 = 0
        self.counter_275 = 0
        self.counter_475 = 0

        self.player_list = player_list
        self.player_name = player_name
        self.player_layout = player_layout
        for player in self.player_list:
            if player is not None:
                player.reset()
        self.racko_size = card_size
        self.show_replacement = show_replacement
        self.replacementVisible(show_replacement)
        self.round_number = 1
        self.starting_player = randrange(self.number_of_players)
        self.active_player = -1
        self.player_scores = []
        for i in range(self.number_of_players):
            self.player_scores.append(0)

    def setWinningScore(self, winning_score):
        self._winning_score = winning_score

    def setDefaultSpeed(self):
        self.speed_deal = 0.1
        self.speed_play = 0.5
        self.delay_new_round = 6

    def setFastSpeed(self):
        self.speed_deal = 0.01
        self.speed_play = 0.2
        self.delay_new_round = 4

    def setAutoRunSpeed(self):
        self.speed_deal = 0
        self.speed_play = 0.02
        self.delay_new_round = 1

    def terminate(self):
        if not self._isRunning:
            self.engineStop.emit()
        self._isRunning = False
        self.is_active = False
        self.auto_run = False

    def replacementVisible(self, show_replacement):
        self.show_replacement = show_replacement
        if not self.auto_run:
            if show_replacement:
                self.setFastSpeed()
            else:
                self.setDefaultSpeed()
        if self.is_active:
            for player_id in range(self.number_of_players):
                if player_id == self.active_player:
                    self.active_rack(player_id)
                else:
                    self.inactive_rack(player_id)

    def humanResponse(self, slot):
        self.human_response_slot = slot
        self.action = 2

    def setHumanReplacement(self, is_active_player, computer_player):
        player_id = self.active_player
        if not is_active_player:
            player_id += 2;
            if player_id == self.number_of_players:
                player_id = 0
        self.action = 3
        computer_player.reset()
        computer_player.setHand(bytearray(self.player_hand[player_id]))
        pile_count = len(self.deck_discard)
        while pile_count > 0:
            pile_count -= 1
            computer_player.discardAdd(self.deck_discard[pile_count], -1)
        self.player_list.pop(player_id)
        self.player_list.insert(player_id, computer_player)

    def setAutoNewRound(self):
        self.action = 4
        time.sleep(0.2)

    def run(self):
        self.actionLock.emit(True)
        self._isRunning = True
        if self.action == 1:
            self.is_active = True
            self.load_deck()
            self.deal_hand()
            if not self.auto_run:
                self.actionLock.emit(False)
        elif self.action == 2:
            self.human_response()
            if not self.auto_run:
                self.actionLock.emit(False)
        elif self.action == 3:
            self.auto_run = True
            for player in self.player_list:
                if player is None:
                    self.auto_run = False
            if self.human_Deck_card > -1:
                replacement_player = self.player_list[self.active_player]
                replacement_player.determineUse(self.human_Deck_card, True)
                picked = replacement_player.determineUse(self.human_Deck_card, False)
                if picked:
                    self.human_response_slot = replacement_player.choosePosition(self.human_Deck_card)
                    self.human_response()
                else:
                    self.human_response()
            else:
                self.computer_play(self.active_player)
        elif self.action == 4:
            time.sleep(self.delay_new_round)
            self.new_round()
        self._isRunning = False

    def isRunning(self):
        return self._isRunning

    def load_deck(self):
        self.deck = [1]
        self.deck_discard = []
        for val in range(2, self.racko_size + 1):
            idx2 = val - 1
            idx1 = randrange(val)
            if idx1 == idx2:
                self.deck.append(val)
            else:
                val2 = self.deck[idx1]
                self.deck[idx1] = val
                self.deck.append(val2)

    def deal_hand(self):
        self.player_hand = []
        self.player_hand_viewable = []
        for i in range(self.number_of_players):
            self.player_hand.append([])
            self.player_hand_viewable.append([])
        self.scoresUpdate.emit(self.round_number, self.player_scores, False)
        target_player = self.starting_player
        for i in range(10):
            slot = self._rack_size - i - 1
            for idx in range(self.number_of_players):
                value = self.dealCard()
                self.player_hand[target_player].insert(0, value)
                self.player_hand_viewable[target_player].insert(0, 0)
                self.rackSlotColor.emit(self.player_layout[target_player], slot, 0, False)
                if self.player_list[target_player] is None:
                    self.rackSlotValue.emit(self.player_layout[target_player], slot, str(value))
                target_player += 1
                if target_player == self.number_of_players:
                    target_player = 0
                time.sleep(self.speed_deal)

        self.addToDiscard(self.dealCard())
        for player_id in range(self.number_of_players):
            if self.player_list[player_id] is not None:
                self.player_list[player_id].setHand(bytearray(self.player_hand[player_id]))
        self.gameStatus.emit("")
        self.gameStatus.emit(self.player_name[self.starting_player] + " go first.")
        if self.player_list[self.starting_player] is not None:
            self.computer_play(self.starting_player)
        else:
            self.human_play(self.starting_player)

    def dealCard(self):
        if len(self.deck) == 0:
            print("error empty deck in dealCard")
        value = self.deck[0]
        self.deck.remove(value)
        for player in self.player_list:
            if player is not None:
                player.deckRemove(self.active_player)
        self.deckPile.emit(len(self.deck), 0, False)
        return value

    def addToDiscard(self, value):
        self.deck_discard.insert(0, value)
        self.discardPile.emit(len(self.deck_discard), value, True)
        for player in self.player_list:
            if player is not None:
                player.discardAdd(value, self.active_player)
        if len(self.deck) == 0:
            for player in self.player_list:
                if player is not None:
                    player.discard2deck()
            while len(self.deck_discard) > 0:
                value = self.deck_discard[-1]
                self.deck_discard.remove(value)
                self.deck.append(value)
            backup_player = self.active_player
            self.active_player = -1
            self.addToDiscard(self.dealCard())
            self.active_player = backup_player
        time.sleep(self.speed_play)

    def removeFromDiscard(self):
        value = self.deck_discard[0]
        self.deck_discard.remove(value)
        for player in self.player_list:
            if player is not None:
                player.discardRemove()
        if len(self.deck_discard) > 0:
            value = self.deck_discard[0]
            self.discardPile.emit(len(self.deck_discard), value, True)
        else:
            self.discardPile.emit(len(self.deck_discard), 0, True)

    def active_rack(self, player_id):
        layout_id = self.player_layout[player_id]
        is_computer = True
        if self.player_list[player_id] is None:
            is_computer = False
        for slot in range(self._rack_size):
            if self.show_replacement:
                self.rackSlotColor.emit(layout_id, slot, self.player_hand_viewable[player_id][slot], True)
                if self.player_hand_viewable[player_id][slot] == 2 and is_computer:
                    self.rackSlotValue.emit(layout_id, slot, str(self.player_hand[player_id][slot]))
            else:
                self.rackSlotColor.emit(layout_id, slot, 0, True)
                if is_computer:
                    self.rackSlotValue.emit(layout_id, slot, "")

    def inactive_rack(self, player_id):
        layout_id = self.player_layout[player_id]
        is_computer = True
        if self.player_list[player_id] is None:
            is_computer = False
        for slot in range(self._rack_size):
            if self.show_replacement:
                self.rackSlotColor.emit(layout_id, slot, self.player_hand_viewable[player_id][slot], False)
                if self.player_hand_viewable[player_id][slot] == 2 and is_computer:
                    self.rackSlotValue.emit(layout_id, slot, str(self.player_hand[player_id][slot]))
            else:
                self.rackSlotColor.emit(layout_id, slot, 0, False)
                if is_computer:
                    self.rackSlotValue.emit(layout_id, slot, "")

    def computer_play(self, player_id):
        self.active_player = player_id
        self.active_rack(player_id)
        layout_id = self.player_layout[player_id]
        computer = self.player_list[player_id]
        card_value = self.deck_discard[0]
        time.sleep(self.speed_play)
        picked = computer.determineUse(card_value, True)
        if picked:
            slot = computer.choosePosition(card_value)
            self.player_hand[player_id][slot] = card_value
            self.rackSlotColor.emit(layout_id, slot, 3, True)
            time.sleep(self.speed_play)
            self.removeFromDiscard()
            return_value = computer.replace(card_value, True)
            self.player_hand_viewable[player_id][slot] = 2
            self.rackSlotValue.emit(layout_id, slot, str(card_value))
            for other_id in range(self.number_of_players):
                if self.player_list[other_id] is not None:
                    self.player_list[other_id].playerCard(card_value,
                                                          self.active_player, slot)
            self.addToDiscard(return_value)
            time.sleep(self.speed_play)
            self.gameStatus.emit(self.player_name[player_id] + ": Replace discard card "
                                 + str(card_value) + " with " + str(return_value) + " at slot " + str(slot + 1) + ".")
        else:
            card_value = self.dealCard()
            self.deckPile.emit(len(self.deck), 0, False)
            picked = computer.determineUse(card_value, False)
            self.discardPile.emit(len(self.deck_discard), 0, False)
            time.sleep(self.speed_play)
            if picked:
                slot = computer.choosePosition(card_value)
                self.player_hand[player_id][slot] = card_value
                self.player_hand_viewable[player_id][slot] = 1
                self.rackSlotColor.emit(layout_id, slot, 3, True)
                time.sleep(self.speed_play)

                return_value = computer.replace(card_value, False)
                self.rackSlotValue.emit(layout_id, slot, str(""))
                for other_id in range(self.number_of_players):
                    if self.player_list[other_id] is not None:
                        self.player_list[other_id].playerCard(self.active_player, slot)
                self.addToDiscard(return_value)
                time.sleep(self.speed_play)
                self.gameStatus.emit(self.player_name[player_id] + ": Replace top deck card "
                                     + " with " + str(return_value) + " at slot " + str(slot + 1) + ".")
            else:
                self.addToDiscard(card_value)
                time.sleep(self.speed_play)
                self.gameStatus.emit(self.player_name[player_id] + ": Place Deck card "
                                     + str(card_value) + " in discard pile.")
        self.check_racko(player_id)

    def human_play(self, player_id):
        self.active_player = player_id
        self.active_rack(player_id)
        self.human_discard_card = self.deck_discard[0]
        self.human_Deck_card = -1
        self.humanPlay.emit(player_id)

    def human_response(self):
        player_id = self.active_player
        slot = self.human_response_slot
        if slot == -1:
            self.deckPile.emit(len(self.deck), 0, False)
            self.addToDiscard(self.human_Deck_card)
            time.sleep(self.speed_play)

            self.gameStatus.emit(self.player_name[player_id] + ": Place top deck card "
                                 + str(self.human_Deck_card) + " in discard pile.")
        else:
            return_value = self.player_hand[player_id][slot]
            layout_id = self.player_layout[player_id]
            if self.human_discard_card > -1:
                self.player_hand[player_id][slot] = self.human_discard_card
                self.player_hand_viewable[player_id][slot] = 2
                self.rackSlotColor.emit(layout_id, slot, 3, True)
                time.sleep(self.speed_play)

                self.removeFromDiscard()
                self.rackSlotValue.emit(layout_id, slot, str(self.human_discard_card))
                for other_id in range(self.number_of_players):
                    if self.player_list[other_id] is not None:
                        self.player_list[other_id].playerCard(self.human_discard_card,
                                                              self.active_player, slot)
                self.addToDiscard(return_value)
                time.sleep(self.speed_play)
                self.gameStatus.emit(self.player_name[player_id] + ": Replace discard card "
                                     + str(self.human_discard_card) + " with " + str(return_value) + " at slot "
                                     + str(slot + 1) + ".")
            else:
                self.player_hand[player_id][slot] = self.human_Deck_card
                self.player_hand_viewable[player_id][slot] = 1
                self.rackSlotColor.emit(layout_id, slot, 3, True)
                time.sleep(self.speed_play)

                self.deckPile.emit(len(self.deck), 0, False)
                self.rackSlotValue.emit(layout_id, slot, str(self.human_Deck_card))
                for other_id in range(self.number_of_players):
                    if self.player_list[other_id] is not None:
                        self.player_list[other_id].playerCard(self.active_player, slot)
                self.addToDiscard(return_value)
                time.sleep(self.speed_play)
                self.gameStatus.emit(self.player_name[player_id] + ": Replace top deck card "
                                     + " with " + str(return_value) + " at slot " + str(slot + 1) + ".")
        self.check_racko(player_id)

    def humanDeck(self):
        self.human_discard_card = -1
        self.human_Deck_card = self.dealCard()
        self.deckPile.emit(len(self.deck), self.human_Deck_card, True)

    def check_racko(self, player_id):
        round_end = True
        value = self.player_hand[player_id][0]
        for slot in range(1, self._rack_size):
            if value < self.player_hand[player_id][slot]:
                value = self.player_hand[player_id][slot]
            else:
                round_end = False
                break
        if round_end:
            print()
            for hand in self.player_hand:
                string = ""
                for card in hand:
                    string = string + str(card) + " "
                print(string)

            self.is_active = False
            self.score_update()
            if not self.game_end:
                time.sleep(self.delay_new_round)
                if self.auto_run:
                    self._isRunning = False
                    self.autoRoundEnd.emit()
                else:
                    self.new_round()
            else:
                winner = self.player_name[0]
                hi_score = self.player_scores[0]
                for player_id in range(1, self.number_of_players):
                    if self.player_scores[player_id] > hi_score:
                        winner = self.player_name[player_id]
                        hi_score = self.player_scores[player_id]
                    elif self.player_scores[player_id] == hi_score:
                        winner = winner + " and " + self.player_name[player_id]
                self.gameStatus.emit("")
                self.gameStatus.emit("    *** " + winner + " wins the game with " + str(hi_score) + " points. ***")
                self.actionLock.emit(False)
                print(str(self.player_scores))
                self.engineStop.emit()
        else:
            self.inactive_rack(player_id)
            player_id += 1
            if player_id == self.number_of_players:
                player_id = 0
            if player_id == self.starting_player:
                self.gameStatus.emit("")
            if self.player_list[player_id] is not None:
                self.computer_play(player_id)
            else:
                self.human_play(player_id)

    def new_round(self):
        if self._isRunning:
            self.counter_turns = 0
            for player_id in range(self.number_of_players):
                if self.player_list[player_id] is not None:
                    self.player_list[player_id].reset()
                    for id2 in range(self.number_of_players):
                        self.player_list[player_id].setPlayerScore(id2, self.player_scores[id2])
                layout_id = self.player_layout[player_id]
                for slot in range(self._rack_size):
                    self.rackSlotColor.emit(layout_id, slot, 3, False)
                    self.rackSlotValue.emit(layout_id, slot, "")
            self.round_number += 1
            self.starting_player += 1
            if self.starting_player == self.number_of_players:
                self.starting_player = 0
            self.active_player = -1
            self.is_active = True
            self.load_deck()
            self.deal_hand()
        else:
            self.engineStop.emit()

    def score_update(self):
        self.game_end = False
        round_scores = ""
        print_scores = ""
        for player_id in range(self.number_of_players):
            if player_id == self.active_player:
                score = str(self.score_racko(player_id))
                print_scores += score + " "
                round_scores = round_scores + self.player_name[player_id] + " - " + score + "  "
                if score == "75":
                    self.counter_75 = self.counter_75 + 1
                elif score == "125":
                    self.counter_125 = self.counter_125 + 1
                elif score == "175":
                    self.counter_175 = self.counter_175 + 1
                elif score == "275":
                    self.counter_275 = self.counter_275 + 1
                elif score == "475":
                    self.counter_475 = self.counter_475 + 1
            else:
                score = str(self.score_others(player_id))
                print_scores += score + " "
                round_scores = round_scores + self.player_name[player_id] + " - " + score + "  "
            if self.player_list[player_id] is not None:
                layout_id = self.player_layout[player_id]
                for slot in range(self._rack_size):
                    self.rackSlotValue.emit(layout_id, slot, str(self.player_hand[player_id][slot]))
        print("Round " + str(self.round_number) + " : " + print_scores)

        self.gameStatus.emit("")
        self.gameStatus.emit(round_scores)
        self.gameStatus.emit("")
        self.gameStatus.emit("Round " + str(self.round_number) + " end.")
        self.scoresUpdate.emit(self.round_number, self.player_scores, self.game_end)
        for score_id in range(self.number_of_players):
            for player_id in range(self.number_of_players):
                if self.player_list[player_id] is not None:
                    self.player_list[player_id].setPlayerScore(score_id, self.player_scores[score_id])

    def score_others(self, player_id):
        value = self.player_hand[player_id][0]
        count = 1
        for slot in range(1, self._rack_size):
            if value < self.player_hand[player_id][slot]:
                value = self.player_hand[player_id][slot]
                count += 1
            else:
                self.player_scores[player_id] += SCORES[count]
                if self.player_scores[player_id] >= self._winning_score:
                    self.game_end = True
                break
        return SCORES[count]

    def score_racko(self, player_id):
        value = self.player_hand[player_id][0]
        count = 1
        max_count = 1
        for slot in range(1, self._rack_size):
            if value + 1 == self.player_hand[player_id][slot]:
                count += 1
            else:
                if count > max_count:
                    max_count = count
                count = 1
            value = self.player_hand[player_id][slot]
        if count > max_count:
            max_count = count
        if max_count > 6:
            max_count = 6
        self.player_scores[player_id] += SCORES_BONUS[max_count]
        if self.player_scores[player_id] >= self._winning_score:
            self.game_end = True
        return SCORES_BONUS[max_count]
