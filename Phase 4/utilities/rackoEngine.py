import time
from PyQt5.QtCore import pyqtSignal, QThread
from PyQt5.QtGui import QPixmap
from random import randrange

# Globals
SCORES = [0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50]
SCORES_BONUS = [0, 75, 75, 125, 175, 275, 475]
GAME_END = 500

class Engine(QThread):
    scoresUpdate = pyqtSignal(int, list, bool)
    drawPile = pyqtSignal(int, int, bool)
    discardPile = pyqtSignal(int, int, bool)
    rackSlotColor = pyqtSignal(int, int, int, bool)
    rackSlotValue = pyqtSignal(int, int, str)
    gameStatus = pyqtSignal(str)
    actionLock = pyqtSignal(bool)
    humanPlay = pyqtSignal(int)

    def __init__(self, size):
        super(Engine, self).__init__()
        self._isRunning = False
        self.setDefaultSpeed()
        self._rack_size = size
        self._is_active = False
        
    def setup(self, player_list, player_name, player_layout, card_size, show_replacement):
        self._isRunning = False
        self._is_active = False
        self._action = 1
        self._number_of_players = len(player_list)
        for player in player_list:
            if player == None:
                print("human")
            else:
                print(player)
        self._player_list = player_list
        self._player_name = player_name
        self._player_layout = player_layout
        self._racko_size = card_size
        self._show_replacement = show_replacement
        self._round_number = 1
        self._dealer_id = randrange(self._number_of_players)
        self._active_player = -1
        self._player_scores = []
        for i in range(self._number_of_players):
            self._player_scores.append(0)
        
    def setDefaultSpeed(self):
        self._speed_deal = 0.1
        self._speed_play = 0.5
        self._delay_new_round = 10

    def setFastSpeed(self):
        self._speed_deal = 0
        self._speed_play = 0.1
        self._delay_new_round = 3

    def setSlowSpeed(self):
        self._speed_deal = 0.2
        self._speed_play = 1
        self._delay_new_round = 30

    def replacementVisible(self, show_replacement):
        self._show_replacement = show_replacement
        if self._is_active:
            for player_id in range(self._number_of_players):
                if player_id == self._active_player:
                    self._active_rack(player_id)
                else:
                    self._inactive_rack(player_id)

    def humanResponse(self, slot):
        self._human_response_slot = slot
        self._action = 2

    def run(self):
        self.actionLock.emit(True)
        self._isRunning = True
        if self._action == 1:
            self._is_active = True
            self._load_deck()
            self._deal_hand()
        elif self._action == 2:
            self._human_response(self._human_response_slot)
        self._isRunning = False 
        self.actionLock.emit(False)
    
    def isRunning(self):
        return self._isRunning

    def _load_deck(self):
        self._deck = [1]
        self._deck_discard = []
        for val in range(2, self._racko_size + 1):
            idx2 = val - 1;
            idx1 = randrange(val)
            if (idx1 == idx2):
                self._deck.append(val)
            else:
                val2 = self._deck[idx1]
                self._deck[idx1] = val
                self._deck.append(val2)

    def _deal_hand(self):
        self._player_hand = []
        self._player_hand_viewable = []
        for i in range(self._number_of_players):
            self._player_hand.append([])
            self._player_hand_viewable.append([])
        self.scoresUpdate.emit(self._round_number, self._player_scores, False)
        target_player = self._dealer_id
        for i in range(10):
            slot = self._rack_size - i - 1
            for idx in range(self._number_of_players):
                value = self.dealCard()
                self._player_hand[target_player].insert(0, value)
                self._player_hand_viewable[target_player].insert(0, 0)
                self.rackSlotColor.emit(self._player_layout[target_player], slot, 0, False)
                if self._player_list[target_player] == None:
                    self.rackSlotValue.emit(self._player_layout[target_player], slot, str(value))
                target_player = target_player + 1
                if (target_player == self._number_of_players):
                    target_player = 0
                time.sleep(self._speed_deal)
        self.addToDiscard(self.dealCard())
        for id in range(self._number_of_players):
            if self._player_list[id] != None:
                self._player_list[id].setHand(bytearray(self._player_hand[id]))
        
        self.gameStatus.emit(self._player_name[self._dealer_id] + " go first.")
        if self._player_list[self._dealer_id] != None:
            self._computer_play(self._dealer_id)
        else:
            self._human_play(self._dealer_id)
    
    def dealCard(self):
        if len(self._deck) == 0:
            print("error empty deck in dealCard")
        value = self._deck[0]
        self._deck.remove(value)
        self.drawPile.emit(len(self._deck), 0, False)
        return value;

    def addToDiscard(self, value):
        self._deck_discard.insert(0, value)
        self.discardPile.emit(len(self._deck_discard), value, True)
        if len(self._deck) == 0:
            while len(self._deck_discard) > 0:
                value = self._deck_discard[-1]
                self._deck_discard.remove(value)
                self._deck.append(value)
            self.addToDiscard(self.dealCard())
        time.sleep(self._speed_play)

    def removeFromDiscard(self):
        value = self._deck_discard[0]
        self._deck_discard.remove(value)
        if len(self._deck_discard) > 0:
            value = self._deck_discard[0]
            self.discardPile.emit(len(self._deck_discard), value, True)
        else:
            self.discardPile.emit(len(self._deck_discard), 0, True)
        
    def _active_rack(self, id):
        layout_id = self._player_layout[id]
        isComputer = True
        if self._player_list[id] == None:
            isComputer = False
        for slot in range(self._rack_size):
            if self._show_replacement:
                self.rackSlotColor.emit(layout_id, slot, self._player_hand_viewable[id][slot], True)
                if self._player_hand_viewable[id][slot] == 2 and isComputer:
                    self.rackSlotValue.emit(layout_id, slot, str(self._player_hand[id][slot]))
            else:
                self.rackSlotColor.emit(layout_id, slot, 0, True)
                if isComputer:
                    self.rackSlotValue.emit(layout_id, slot, "")

    def _inactive_rack(self, id):
        layout_id = self._player_layout[id]
        isComputer = True
        if self._player_list[id] == None:
            isComputer = False
        for slot in range(self._rack_size):
            if self._show_replacement:
                self.rackSlotColor.emit(layout_id, slot, self._player_hand_viewable[id][slot], False)
                if self._player_hand_viewable[id][slot] == 2 and isComputer:
                    self.rackSlotValue.emit(layout_id, slot, str(self._player_hand[id][slot]))
            else:
                self.rackSlotColor.emit(layout_id, slot, 0, False)
                if isComputer:
                    self.rackSlotValue.emit(layout_id, slot, "")

    def _computer_play(self, player_id):
        self._active_player = player_id
        self._active_rack(player_id)
        layout_id = self._player_layout[player_id]
        computer = self._player_list[player_id]
        card_value = self._deck_discard[0]
        picked = computer.determine_use(card_value, True)
        if picked:
            slot = computer.choose_position(card_value)
            self._player_hand[player_id][slot] = card_value
            self.rackSlotColor.emit(layout_id, slot, 3, True)
            time.sleep(self._speed_play)
            
            self.removeFromDiscard()
            return_value = computer.replace(card_value, True)
            self._player_hand_viewable[player_id][slot] = 2
            self.addToDiscard(return_value)
            self.rackSlotValue.emit(layout_id, slot, str(card_value))
            time.sleep(self._speed_play)
            
            self.gameStatus.emit(self._player_name[player_id] + ": Replace discard card "
                + str(card_value) + " with " + str(return_value) + " at slot " + str(slot + 1) + ".")
        else:
            card_value = self.dealCard()
            self.discardPile.emit(len(self._deck), 0, False) 
            picked = computer.determine_use(card_value, True)
            self.discardPile.emit(len(self._deck_discard), 0, False)
            time.sleep(self._speed_play)
            if picked:
                slot = computer.choose_position(card_value)
                self._player_hand[player_id][slot] = card_value
                self._player_hand_viewable[player_id][slot] = 1
                self.rackSlotColor.emit(layout_id, slot, 3, True)
                time.sleep(self._speed_play)
                
                return_value = computer.replace(card_value, False)
                self.addToDiscard(return_value)
                
                time.sleep(self._speed_play)
                self.gameStatus.emit(self._player_name[player_id] + ": Replace a draw card "
                    + " with " + str(return_value) + " at slot " + str(slot + 1) + ".")
            else:
                self.addToDiscard(card_value)
                time.sleep(self._speed_play)
                
                self.gameStatus.emit(self._player_name[player_id] + ": Place draw card "
                    + str(card_value) + " in discard pile.")
        self._check_racko(player_id)

    def _human_play(self, player_id):
        self._active_player = player_id
        self._active_rack(player_id)
        self._human_discard_card = self._deck_discard[0]
        self._human_draw_card = -1
        self.humanPlay.emit(player_id)

    def _human_response(self, slot):
        player_id = self._active_player
        if slot == -1:
            self.drawPile.emit(len(self._deck), 0, False)  
            self.addToDiscard(self._human_draw_card)
            time.sleep(self._speed_play)
            
            self.gameStatus.emit(self._player_name[player_id] + ": Place draw card "
                + str(self._human_draw_card) + " in discard pile.")          
        else:
            return_value = self._player_hand[player_id][slot]
            layout_id = self._player_layout[player_id]
            if self._human_discard_card > -1:
                self._player_hand[player_id][slot] = self._human_discard_card
                self._player_hand_viewable[player_id][slot] = 2
                self.rackSlotColor.emit(layout_id, slot, 3, True)
                time.sleep(self._speed_play)

                self.removeFromDiscard()
                self.rackSlotValue.emit(layout_id, slot, str(self._human_discard_card))
                self.addToDiscard(return_value)
                time.sleep(self._speed_play)
                
                self.gameStatus.emit(self._player_name[player_id] + ": Replace discard card "
                    + str(self._human_discard_card) + " with " + str(return_value) + " at slot " + str(slot + 1) + ".")
            else:
                self._player_hand[player_id][slot] = self._human_draw_card
                self._player_hand_viewable[player_id][slot] = 1
                self.rackSlotColor.emit(layout_id, slot, 3, True)
                time.sleep(self._speed_play)
                
                self.drawPile.emit(len(self._deck), 0, False)  
                self.rackSlotValue.emit(layout_id, slot, str(self._human_draw_card))
                self.addToDiscard(return_value)
                time.sleep(self._speed_play)
                
                self.gameStatus.emit(self._player_name[player_id] + ": Replace a draw card "
                    + " with " + str(return_value) + " at slot " + str(slot + 1) + ".")
        self._check_racko(player_id)            

    def humanDraw(self):
        self._human_discard_card = -1
        self._human_draw_card = self.dealCard()
        self.drawPile.emit(len(self._deck), self._human_draw_card, True)

    def _check_racko(self, player_id):
        round_end = True;
        value = self._player_hand[player_id][0]
        for slot in range(1, self._rack_size):
            if value < self._player_hand[player_id][slot]:
                value = self._player_hand[player_id][slot]
            else:
                round_end = False
                break
        if round_end:
            self._is_active = False
            self._score_update()
            if not self._game_end:
                time.sleep(self._delay_new_round) 
                self._new_round()  
        else:
            self._inactive_rack(player_id)
            player_id += 1
            if player_id == self._number_of_players:
                player_id = 0
            if self._player_list[player_id] != None:
                self._computer_play(player_id)
            else:
                self._human_play(player_id)

    def _new_round(self):
        for layout_id in self._player_layout:
            for slot in range(self._rack_size):
                self.rackSlotColor.emit(layout_id, slot, 3, False)
                self.rackSlotValue.emit(layout_id, slot, "")
        self._round_number += 1
        self._dealer_id += 1
        if self._dealer_id == self._number_of_players:
            self._dealer_id = 0
        self._active_player = -1
        self._is_active = True
        self._load_deck()
        self._deal_hand()

    def _score_update(self):
        self._game_end = False
        for player_id in range(self._number_of_players):
            if player_id == self._active_player:
                self._score_racko(player_id)
            else:
                self._score_others(player_id)
            if self._player_list[player_id] != None:
                layout_id = self._player_layout[player_id]
                for slot in range(self._rack_size):
                    self.rackSlotValue.emit(layout_id, slot, str(self._player_hand[player_id][slot]))
        self.scoresUpdate.emit(self._round_number, self._player_scores, self._game_end)

    def _score_others(self, player_id):
        value = self._player_hand[player_id][0]
        count = 1
        for slot in range(1, self._rack_size):
            if value < self._player_hand[player_id][slot]:
                value = self._player_hand[player_id][slot]
                count += 1
            else:
                self._player_scores[player_id] += SCORES[count]
                if self._player_scores[player_id] >= GAME_END:
                    self._game_end = True
                break

    def _score_racko(self, player_id):
        value = self._player_hand[player_id][0]
        count = 1
        max_count = 1
        for slot in range(1, self._rack_size):
            if value + 1== self._player_hand[player_id][slot]:
                value = self._player_hand[player_id][slot]
                count += 1
            else:
                if count > max_count:
                    max_count = count
                    count = 1
            value = self._player_hand[player_id][slot]
        if count > max_count:
            max_count = count
            count = 1
        if max_count > 6:
            max_count = 6
        self._player_scores[player_id] += SCORES_BONUS[max_count]
        if self._player_scores[player_id] >= GAME_END:
            self._game_end = True
                




                









