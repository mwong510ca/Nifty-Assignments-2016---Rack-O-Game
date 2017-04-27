"""
" appRacko is the GUI application of Racko game.  It support 2-4 players
" version for 40-60 cards.  User can play with 1-3 computer players.
" The first player reached 500 points to win the game.
"
" author Meisze Wong
"        www.linkedin.com/pub/macy-wong/46/550/37b/
"        github.com/mwong510ca/RackoGame-ComputerStrategy
"""

# !/usr/bin/env python3

import sys
import time
import subprocess
import socket

from PyQt5 import QtCore
from PyQt5.QtWidgets import QApplication, QMainWindow, QMessageBox, QInputDialog
from random import randrange

from gui.mainWindow import Ui_MainWindow as MainWindow
from utilities.rackoEngine import Engine
from utilities.rackoImage import Cards
from py4j.java_gateway import JavaGateway
from py4j.java_gateway import GatewayClient

# Globals
RACK_SIZE = 10
BG_DISABLED = "background: None"
BG_ACTIVE = {0: 'background: DeepSkyBlue',
             1: 'background: GreenYellow',
             2: 'background: GreenYellow',
             3: 'background: Yellow'}
BG_INACTIVE = {0: 'background: SteelBlue',
               1: 'background: OliveDrab',
               2: 'background: OliveDrab',
               3: BG_DISABLED}
NUMBER_COLOR = "#941e0b"
SIZE_RACKO2 = 40
SIZE_RACKO3 = 50
SIZE_RACKO4 = 60
SIZE_MAX_PLAYERS = 4
SIZE_RACK = 10


class GameRacko(QMainWindow, MainWindow):
    closing = QtCore.pyqtSignal()

    def __init__(self, in_gateway):
        super().__init__()
        self._gateway = in_gateway
        self.setupUi(self)

        # mainWindow connection settings
        #    menu bar
        self.actionAboutRacko.triggered.connect(self.about_racko)
        self.actionAboutAuthor.triggered.connect(self.about_author)
        self.actionOneRound.triggered.connect(self.one_round)
        self.actionFullGame.triggered.connect(self.full_game)
        self.actionCustomLimit.triggered.connect(self.custom_limit)
        self.actionExit.triggered.connect(self.custom_quit)
        self.actionGameRules.triggered.connect(self.game_rules)
        self.actionHowToPlay.triggered.connect(self.how_to_play)

        #    game setup
        self.requirePlayer2.setItemText(0, "Easy")
        self.requirePlayer2.setItemText(1, "Moderate")
        self.requirePlayer2.setItemText(2, "Hard")
        self.requirePlayer2.setCurrentIndex(0)
        self.optionPlayer3.setItemText(0, "Easy")
        self.optionPlayer3.setItemText(1, "Moderate")
        self.optionPlayer3.setItemText(2, "Hard")
        self.optionPlayer3.setItemText(3, "Disabled")
        self.optionPlayer3.setCurrentIndex(3)
        self.optionPlayer4.setItemText(0, "Easy")
        self.optionPlayer4.setItemText(1, "Moderate")
        self.optionPlayer4.setItemText(2, "Hard")
        self.optionPlayer4.setItemText(3, "Play 2 sets")
        self.optionPlayer4.setItemText(4, "Disabled")
        self.optionPlayer4.setCurrentIndex(4)
        self.optionPlayer4.setEnabled(False)

        self.optionPlayer3.currentIndexChanged.connect(self.player3_disable)
        self.actionButton.clicked.connect(self.game_start_stop)
        self.optionView.toggled.connect(self.show_replacement)

        #    player cards
        self.player1Slot01.clickedLabel.connect(lambda: self.pick_card(1, 1))
        self.player1Slot02.clickedLabel.connect(lambda: self.pick_card(1, 2))
        self.player1Slot03.clickedLabel.connect(lambda: self.pick_card(1, 3))
        self.player1Slot04.clickedLabel.connect(lambda: self.pick_card(1, 4))
        self.player1Slot05.clickedLabel.connect(lambda: self.pick_card(1, 5))
        self.player1Slot06.clickedLabel.connect(lambda: self.pick_card(1, 6))
        self.player1Slot07.clickedLabel.connect(lambda: self.pick_card(1, 7))
        self.player1Slot08.clickedLabel.connect(lambda: self.pick_card(1, 8))
        self.player1Slot09.clickedLabel.connect(lambda: self.pick_card(1, 9))
        self.player1Slot10.clickedLabel.connect(lambda: self.pick_card(1, 10))
        self.player3Slot01.clickedLabel.connect(lambda: self.pick_card(3, 1))
        self.player3Slot02.clickedLabel.connect(lambda: self.pick_card(3, 2))
        self.player3Slot03.clickedLabel.connect(lambda: self.pick_card(3, 3))
        self.player3Slot04.clickedLabel.connect(lambda: self.pick_card(3, 4))
        self.player3Slot05.clickedLabel.connect(lambda: self.pick_card(3, 5))
        self.player3Slot06.clickedLabel.connect(lambda: self.pick_card(3, 6))
        self.player3Slot07.clickedLabel.connect(lambda: self.pick_card(3, 7))
        self.player3Slot08.clickedLabel.connect(lambda: self.pick_card(3, 8))
        self.player3Slot09.clickedLabel.connect(lambda: self.pick_card(3, 9))
        self.player3Slot10.clickedLabel.connect(lambda: self.pick_card(3, 10))
        self.imgDeckPile.clickedLabel.connect(self.deck_pile_action)

        # initial lists
        self.scores_labels = [self.scorePlayer1, self.scorePlayer2, self.scorePlayer3, self.scorePlayer4]
        self.racks_labels = [
            [self.player1Slot01, self.player1Slot02, self.player1Slot03, self.player1Slot04, self.player1Slot05,
             self.player1Slot06, self.player1Slot07, self.player1Slot08, self.player1Slot09, self.player1Slot10],
            [self.player2Slot01, self.player2Slot02, self.player2Slot03, self.player2Slot04, self.player2Slot05,
             self.player2Slot06, self.player2Slot07, self.player2Slot08, self.player2Slot09, self.player2Slot10],
            [self.player3Slot01, self.player3Slot02, self.player3Slot03, self.player3Slot04, self.player3Slot05,
             self.player3Slot06, self.player3Slot07, self.player3Slot08, self.player3Slot09, self.player3Slot10],
            [self.player4Slot01, self.player4Slot02, self.player4Slot03, self.player4Slot04, self.player4Slot05,
             self.player4Slot06, self.player4Slot07, self.player4Slot08, self.player4Slot09, self.player4Slot10]]
        self.winning_score = 500
        # initial maps with final values
        self.computer_player = {0: self._gateway.entry_point.setPlayerEasy,
                                1: self._gateway.entry_point.setPlayerModerate,
                                2: self._gateway.entry_point.setPlayerHard,
                                3: None,
                                4: None}

        # load game engine
        self._game_engine = Engine(SIZE_RACK, self.winning_score)
        self._game_engine.scoresUpdate.connect(self.round_scores_update)
        self._game_engine.deckPile.connect(self.refresh_deck_pile)
        self._game_engine.discardPile.connect(self.refresh_discard_pile)
        self._game_engine.rackSlotColor.connect(self.change_slot_color)
        self._game_engine.rackSlotValue.connect(self.change_slot_value)
        self._game_engine.gameStatus.connect(self.refresh_game_status)
        self._game_engine.actionLock.connect(self.control_in_action)
        self._game_engine.humanPlay.connect(self.human_play)
        self._game_engine.autoRoundEnd.connect(self.auto_round_end)
        # Notes: statsEnd not in use. For statistic run only
        self._game_engine.statsEnd.connect(self.game_terminate)

        # link card images
        self._cards_files = Cards()

        # initialize local variables
        self.layout1_lock = True
        self.layout3_lock = True
        self.Deck_pile_lock = True
        self.game_active = False
        self.players_name = []
        self.players_list = []
        self.players_layout = []
        self.number_of_players = 0
        self.racko_size = 0
        self.range_rack = 0
        self.use_discard_card = False
        self.status_line = []

        # default settings
        self.layout_reset()

    def layout_reset(self):
        self.headingSetup.setText("Choose 1-3 computer players: (play in random order)")
        self.statusGame.setText("")
        self.headingGame.setText("")
        self.headingDeckPile.setText("Deck Pile")
        self.headingDiscardPile.setText("Discard Pile")
        self.headingPlayer1.setText("")
        self.headingPlayer2.setText("")
        self.headingPlayer3.setText("")
        self.headingPlayer4.setText("")
        self.scorePlayer1.setText("")
        self.scorePlayer2.setText("")
        self.scorePlayer3.setText("")
        self.scorePlayer4.setText("")
        for rack in range(SIZE_MAX_PLAYERS):
            for slot in range(SIZE_RACK):
                self.racks_labels[rack][slot].setStyleSheet(BG_DISABLED)
                self.racks_labels[rack][slot].setText("")
                self.racks_labels[rack][slot].setLineWidth(0)
        self.imgDeckPile.setPixmap(self._cards_files.backcard())
        self.imgDiscardPile.setPixmap(self._cards_files.card_number(0))

    def player3_disable(self):
        if self.optionPlayer3.currentIndex() == 3:
            self.optionPlayer4.setCurrentIndex(4)
            self.optionPlayer4.setEnabled(False)
        else:
            self.optionPlayer4.setEnabled(True)

    def game_start_stop(self):
        if self.game_active:
            reply = QMessageBox.question(None, '', 'You are requesting to stop the game.\n' +
                                         'Do you want computer take over your turn?\n' +
                                         'Cancel - return to game\n' +
                                         'No - stop the game now\n' +
                                         'Yes - finish remaining game in fast speed',
                                         QMessageBox.Cancel | QMessageBox.Yes | QMessageBox.No, QMessageBox.Cancel)
            if reply == QMessageBox.Yes:
                self.game_computer_take_over()
            elif reply == QMessageBox.No:
                self.game_terminate()
        else:
            self.layout_reset()
            self.game_start()
            self.game_active = True
            self.headingSetup.setEnabled(False)
            self.requirePlayer2.setEnabled(False)
            self.optionPlayer3.setEnabled(False)
            self.optionPlayer4.setEnabled(False)
            self.actionOneRound.setEnabled(False)
            self.actionFullGame.setEnabled(False)
            self.actionCustomLimit.setEnabled(False)
            self.actionButton.setText("Stop")

    def game_terminate(self):
        self.game_active = False
        self.headingSetup.setEnabled(True)
        self.requirePlayer2.setEnabled(True)
        self.optionPlayer3.setEnabled(True)
        self.optionPlayer4.setEnabled(True)
        self.actionOneRound.setEnabled(True)
        self.actionFullGame.setEnabled(True)
        self.actionCustomLimit.setEnabled(True)
        self.actionButton.setText("New Game")

    def setup_players(self):
        while len(self.players_list) > 0:
            self.players_list.remove(self.players_list[-1])
        self.players_list.append(None)
        self.headingPlayer1.setText("Player")
        self.scorePlayer1.setText("0")
        if self.optionPlayer3.currentIndex() == 3:
            self.players_name = ["Player", "Computer"]
            self.headingPlayer3.setText("Comp")
            self.scorePlayer3.setText("0")
            self.players_layout = [0, 2]
            self.number_of_players = 2
            self.racko_size = SIZE_RACKO2
            self._gateway.entry_point.setNumberOfPlayers(self.number_of_players - 1, self.racko_size)
            self.players_list.append(self.computer_player[self.requirePlayer2.currentIndex()](1))
            self.players_list[1].setPositionId(1)
        elif self.optionPlayer4.currentIndex() == 4:
            self.players_name = ["Player", "Computer 1", "Computer 2"]
            self.headingPlayer2.setText("Computer 1")
            self.scorePlayer2.setText("0")
            self.headingPlayer3.setText("Comp 2")
            self.scorePlayer3.setText("0")
            self.players_layout = [0, 1, 2]
            self.number_of_players = 3
            self.racko_size = SIZE_RACKO3
            self._gateway.entry_point.setNumberOfPlayers(self.number_of_players - 1, self.racko_size)
            order = randrange(2)
            if order == 1:
                self.players_list.append(self.computer_player[self.requirePlayer2.currentIndex()](1))
                self.players_list.append(self.computer_player[self.optionPlayer3.currentIndex()](2))
            else:
                self.players_list.append(self.computer_player[self.optionPlayer3.currentIndex()](2))
                self.players_list.append(self.computer_player[self.requirePlayer2.currentIndex()](1))
            self.players_list[1].setPositionId(1)
            self.players_list[2].setPositionId(2)
        else:
            self.players_layout = [0, 1, 2, 3]
            self.number_of_players = 4
            self.racko_size = SIZE_RACKO4
            if self.optionPlayer4.currentIndex() == 3:
                self.players_name = ["Player", "Computer 1", "Player 2", "Computer 2"]
                self.headingPlayer2.setText("Computer 1")
                self.scorePlayer2.setText("0")
                self.headingPlayer3.setText("Player2")
                self.scorePlayer3.setText("0")
                self.headingPlayer4.setText("Computer 2")
                self.scorePlayer4.setText("0")
                self._gateway.entry_point.setNumberOfPlayers(self.number_of_players - 2, self.racko_size)
                order = randrange(2)
                if order == 1:
                    self.players_list.append(self.computer_player[self.requirePlayer2.currentIndex()](1))
                    self.players_list.append(None)
                    self.players_list.append(self.computer_player[self.optionPlayer3.currentIndex()](2))
                else:
                    self.players_list.append(self.computer_player[self.optionPlayer3.currentIndex()](2))
                    self.players_list.append(None)
                    self.players_list.append(self.computer_player[self.requirePlayer2.currentIndex()](1))
                self.players_list[1].setPositionId(1)
                self.players_list[3].setPositionId(3)
            else:
                self.players_name = ["Player", "Computer 1", "Computer 2", "Computer 3"]
                self.headingPlayer2.setText("Computer 1")
                self.scorePlayer2.setText("0")
                self.headingPlayer3.setText("Comp 2")
                self.scorePlayer3.setText("0")
                self.headingPlayer4.setText("Computer 3")
                self.scorePlayer4.setText("0")
                self._gateway.entry_point.setNumberOfPlayers(self.number_of_players - 1, self.racko_size)
                order = randrange(2)
                if order == 1:
                    self.players_list.append(self.computer_player[self.requirePlayer2.currentIndex()](1))
                    self.players_list.append(self.computer_player[self.optionPlayer3.currentIndex()](2))
                else:
                    self.players_list.append(self.computer_player[self.optionPlayer3.currentIndex()](2))
                    self.players_list.append(self.computer_player[self.requirePlayer2.currentIndex()](1))
                order = randrange(3)
                if order == 2:
                    self.players_list.append(self.computer_player[self.optionPlayer4.currentIndex()](3))
                else:
                    swap_player = self.players_list[order + 1]
                    self.players_list.remove(swap_player)
                    self.players_list.insert(order + 1, self.computer_player[self.optionPlayer4.currentIndex()](3))
                    self.players_list.append(swap_player)
                self.players_list[1].setPositionId(1)
                self.players_list[2].setPositionId(2)
                self.players_list[3].setPositionId(3)
        self.range_rack = range(1, self.racko_size + 1)
        self.headingDeckPile.setText("Deck Pile (" + str(self.racko_size) + " left)")
        self.headingDiscardPile.setText("Discard Pile")
        for rack in self.players_layout:
            for slot in range(SIZE_RACK):
                self.racks_labels[rack][slot].setLineWidth(1)

    def game_start(self):
        while len(self.status_line) > 0:
            self.status_line.remove(self.status_line[0])
        for count in range(7):
            self.status_line.append("")
        self.setup_players()
        for rack in self.players_layout:
            for slot in range(SIZE_RACK):
                self.racks_labels[rack][slot].setText("")
                self.racks_labels[rack][slot].setStyleSheet(BG_DISABLED)
        self._game_engine.setup(self.players_list, self.players_name,
                                self.players_layout, self.racko_size, self.optionView.isChecked())
        self._game_engine.start()

    def refresh_deck_pile(self, count, value, face_up):
        if count > 0:
            self.headingDeckPile.setText("Deck Pile (" + str(count) + " left)")
            self.imgDeckPile.setPixmap(self._cards_files.backcard())
        else:
            self.headingDeckPile.setText("Deck Pile (empty)")
            self.imgDeckPile.setPixmap(self._cards_files.card_number(0))
        if face_up:
            self.headingDeckPile.setText("Reviewing top deck card")
            if value in self.range_rack:
                self.imgDeckPile.setPixmap(self._cards_files.card_number(value))
            else:
                self.headingDeckPile.setText("Deck Pile (empty) ERROR : " + str(value))

    def refresh_discard_pile(self, count, value, face_up):
        if count > 0:
            self.headingDiscardPile.setText("Discard Pile (" + str(count) + ")")
            if face_up:
                if value in self.range_rack:
                    self.imgDiscardPile.setPixmap(self._cards_files.card_number(value))
                else:
                    self.headingDiscardPile.setText("Discard Pile (empty) ERROR : " + str(value))
            else:
                self.headingDiscardPile.setText("Reviewing top deck card")
                self.imgDiscardPile.setPixmap(self._cards_files.backcard())
        else:
            self.headingDiscardPile.setText("Discard Pile")

    def change_slot_color(self, layout_id, slot, color_code, active_state):
        if active_state:
            self.racks_labels[layout_id][slot].setStyleSheet(BG_ACTIVE[color_code])
        else:
            self.racks_labels[layout_id][slot].setStyleSheet(BG_INACTIVE[color_code])

    def change_slot_value(self, layout_id, slot, value):
        self.racks_labels[layout_id][slot].setText("<font color="
                                                   + NUMBER_COLOR + ">" + value + "</font>")

    def refresh_game_status(self, line):
        msg = ""
        for line_number in range(5, -1, -1):
            msg = "<BR>" + self.status_line[line_number] + msg
            self.status_line[line_number + 1] = self.status_line[line_number]
        self.status_line[0] = line
        msg = line + msg
        self.statusGame.setText(msg)

    def show_replacement(self):
        if self.optionView.isChecked():
            self._game_engine.replacementVisible(True)
        else:
            self._game_engine.replacementVisible(False)

    def control_in_action(self, lock):
        if lock:
            self.actionButton.setEnabled(False)
            self.optionView.setEnabled(False)
            self.layout1_lock = lock
            self.layout3_lock = lock
            self.Deck_pile_lock = lock
        else:
            self.actionButton.setEnabled(True)
            self.optionView.setEnabled(True)

    def pick_card(self, layout_id, slot):
        if layout_id == 1 and not self.layout1_lock:
            self._game_engine.humanResponse(slot - 1)
            self.use_discard_card = False
            self.layout1_lock = True
            self._game_engine.start()
        elif layout_id == 3 and not self.layout3_lock:
            self._game_engine.humanResponse(slot - 1)
            self.use_discard_card = False
            self.layout3_lock = True
            self._game_engine.start()

    def deck_pile_action(self):
        if not self.Deck_pile_lock:
            if self.use_discard_card:
                self._game_engine.humanDeck()
                self.use_discard_card = False
            else:
                self._game_engine.humanResponse(-1)
                if not self.layout1_lock:
                    self.layout1_lock = True
                elif not self.layout3_lock:
                    self.layout3_lock = True
                self._game_engine.start()

    def round_scores_update(self, round_number, scores, is_game_end):
        self.headingGame.setText("Round " + str(round_number))
        for player in range(self.number_of_players):
            self.scores_labels[self.players_layout[player]].setText(
                str(scores[player]))
        if is_game_end and self.game_active:
            self.game_terminate()

    def human_play(self, player_id):
        self.use_discard_card = True
        self.Deck_pile_lock = False
        if player_id == 0:
            self.layout1_lock = False
        elif player_id == 2:
            self.layout3_lock = False

    def game_computer_take_over(self):
        computer_replacement = self._gateway.setPlayerReplacement()
        self._game_engine.setHumanReplacement(True, computer_replacement)
        change_speed = True

        if len(self.players_list) == 4:
            for player in self.players_list:
                if player is None:
                    change_speed = False
                    reply = QMessageBox.question(None, '', 'You have two set of racks.\n' +
                                                 'Do you want to replacement to computer for the other set?\n' +
                                                 'If not, game will remains in same speed setting.',
                                                 QMessageBox.Yes | QMessageBox.No, QMessageBox.No)
                    if reply == QMessageBox.Yes:
                        change_speed = True
                        computer_replacement2 = self._gateway.setPlayerReplacement()
                        self._game_engine.setHumanReplacement(False, computer_replacement2)

        if change_speed:
            self._game_engine.setAutoRunSpeed()
        self._game_engine.start()

    def auto_round_end(self):
        if self.game_active:
            self._game_engine.setAutoNewRound()
            self._game_engine.start()

    def about_racko(self):
        QMessageBox.information(None, 'About Racko Game', 'About Racko Game\n' +
                                'A 2-4 players rack-o! with computer strategy.\n' +
                                'Game setup are based on the real card game.\n' +
                                'View the following link for more information:\n' +
                                'http://www.hasbro.com/common/instruct/Racko(1987).PDF',
                                QMessageBox.Close, QMessageBox.Close)

    def about_author(self):
        QMessageBox.information(None, 'About Author', 'Author: Meisze Wong\n' +
                                'www.linkedin.com/pub/macy-wong/46/550/37b/\n\n' +
                                'view source code:\nhttps://github.com/mwong510ca/RackoGame-ComputerStrategy',
                                QMessageBox.Close, QMessageBox.Close)

    def one_round(self):
        self.winning_score = 75
        self._game_engine.setWinningScore(self.winning_score)

    def full_game(self):
        self.winning_score = 500
        self._game_engine.setWinningScore(self.winning_score)

    def custom_limit(self):
        custom_score, ok_pressed = QInputDialog.getInt(None, "Change winning score",
                                                       "Please enter score from 75 to 1000 (increment by 5):",
                                                       500, 75, 1000, 5)
        if ok_pressed:
            self.winning_score = (custom_score // 5) * 5
            self._game_engine.setWinningScore(self.winning_score)

    def game_rules(self):
        QMessageBox.information(None, 'Game Rules', 'Rack-o! View the game rules from the following link:\n' +
                                'http://www.hasbro.com/common/instruct/Racko(1987).PDF',
                                QMessageBox.Close, QMessageBox.Close)

    def how_to_play(self):
        QMessageBox.information(None, 'How To Play', 'Rack-o! How to play:\n\n'
                                                     '1. Choose 1 to 3 computer players and start the game.\n' +
                                '   You may play second set in a 4 players rack-o!\n\n' +
                                '2. On you turn, your rack will be highlight.  Sort the lowest to highest ' +
                                ' numbers from bottom up.\n\n' +
                                '3. Replace with a discard card, click on the number of your deck\n' +
                                '   Pick a card from deck, click the deck pile. Then,\n' +
                                '   Click your number to replace the deck card, \n' +
                                '   or click the deck pile again to throw the card in discard pile.\n\n' +
                                'You have an option to show the replacement from computer players.\n' +
                                'Show replacement will play in fast speed.\n\n' +
                                'Have fun!!!',
                                QMessageBox.Close, QMessageBox.Close)

    def custom_quit(self):
        if QMessageBox.question(None, '', 'Are you sure to quit?',
                                QMessageBox.Yes | QMessageBox.No, QMessageBox.No) == QMessageBox.Yes:
            QApplication.quit()

    def closeEvent(self, event):
        self.closing.emit()
        super(GameRacko, self).closeEvent(event)


if __name__ == "__main__":
    host = '127.0.0.1'
    port_number = 25333
    while port_number < 25335:
        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        s.bind(('', 0))
        port_number = s.getsockname()[1]
        s.close()
    try:
        p = subprocess.Popen(['java', '-jar', 'RackoComputerPlayersGateway.jar', str(port_number)])
        time.sleep(1)
    except:
        p.kill()
        sys.exit()

    gateway_server = JavaGateway(GatewayClient(address=host, port=port_number))
    app = QApplication(sys.argv)
    window = GameRacko(gateway_server)
    window.show()
    while app.exec_() > 0:
        time.sleep(1)
    gateway_server.shutdown()
    sys.exit()
