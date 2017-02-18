# -*- coding: utf-8 -*-
# !/usr/bin/env python3

import sys
import time
import subprocess
import socket

from PyQt5 import QtCore
from PyQt5.QtWidgets import QApplication, QMainWindow, QMessageBox
from random import randrange

from gui.mainWindow import Ui_MainWindow as MainWindow
from utilities.rackoEngine import Engine
from utilities.rackoDeck import Cards
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
        self._default_setting()

    def _default_setting(self):
        self._layout1_lock = True
        self._layout3_lock = True
        self._draw_pile_lock = True
        self.player1Slot01.clickedLabel.connect(lambda: self._pick_card(1, 1))
        self.player1Slot02.clickedLabel.connect(lambda: self._pick_card(1, 2))
        self.player1Slot03.clickedLabel.connect(lambda: self._pick_card(1, 3))
        self.player1Slot04.clickedLabel.connect(lambda: self._pick_card(1, 4))
        self.player1Slot05.clickedLabel.connect(lambda: self._pick_card(1, 5))
        self.player1Slot06.clickedLabel.connect(lambda: self._pick_card(1, 6))
        self.player1Slot07.clickedLabel.connect(lambda: self._pick_card(1, 7))
        self.player1Slot08.clickedLabel.connect(lambda: self._pick_card(1, 8))
        self.player1Slot09.clickedLabel.connect(lambda: self._pick_card(1, 9))
        self.player1Slot10.clickedLabel.connect(lambda: self._pick_card(1, 10))
        self.player3Slot01.clickedLabel.connect(lambda: self._pick_card(3, 1))
        self.player3Slot02.clickedLabel.connect(lambda: self._pick_card(3, 2))
        self.player3Slot03.clickedLabel.connect(lambda: self._pick_card(3, 3))
        self.player3Slot04.clickedLabel.connect(lambda: self._pick_card(3, 4))
        self.player3Slot05.clickedLabel.connect(lambda: self._pick_card(3, 5))
        self.player3Slot06.clickedLabel.connect(lambda: self._pick_card(3, 6))
        self.player3Slot07.clickedLabel.connect(lambda: self._pick_card(3, 7))
        self.player3Slot08.clickedLabel.connect(lambda: self._pick_card(3, 8))
        self.player3Slot09.clickedLabel.connect(lambda: self._pick_card(3, 9))
        self.player3Slot10.clickedLabel.connect(lambda: self._pick_card(3, 10))
        self.imgDrawPile.clickedLabel.connect(self._draw_pile_action)

        self._scores_labels = [self.scorePlayer1, self.scorePlayer2, self.scorePlayer3, self.scorePlayer4]
        self._racks_labels = [
            [self.player1Slot01, self.player1Slot02, self.player1Slot03, self.player1Slot04, self.player1Slot05,
             self.player1Slot06, self.player1Slot07, self.player1Slot08, self.player1Slot09, self.player1Slot10],
            [self.player2Slot01, self.player2Slot02, self.player2Slot03, self.player2Slot04, self.player2Slot05,
             self.player2Slot06, self.player2Slot07, self.player2Slot08, self.player2Slot09, self.player2Slot10],
            [self.player3Slot01, self.player3Slot02, self.player3Slot03, self.player3Slot04, self.player3Slot05,
             self.player3Slot06, self.player3Slot07, self.player3Slot08, self.player3Slot09, self.player3Slot10],
            [self.player4Slot01, self.player4Slot02, self.player4Slot03, self.player4Slot04, self.player4Slot05,
             self.player4Slot06, self.player4Slot07, self.player4Slot08, self.player4Slot09, self.player4Slot10]]

        self._load_engine_setting()
        self._cards_files = Cards()

        self._computer_player1 = {0: self._gateway.entry_point.setPlayerEasy,
                                  1: self._gateway.entry_point.setPlayerModerate,
                                  2: self._gateway.entry_point.setPlayerHard
                                  }

        self._computer_player2 = {0: None,
                                  1: self._gateway.entry_point.setPlayerEasy,
                                  2: self._gateway.entry_point.setPlayerModerate,
                                  3: self._gateway.entry_point.setPlayerHard
                                  }

        self._computer_player3 = {0: None,
                                  1: None,
                                  2: self._gateway.entry_point.setPlayerEasy,
                                  3: self._gateway.entry_point.setPlayerModerate,
                                  4: self._gateway.entry_point.setPlayerHard
                                  }

        self.optionPlayer3.currentIndexChanged.connect(self._player3_disable)
        self.optionPlayer4.currentIndexChanged.connect(self._player4_select)
        self._game_active = False
        self.actionButton.clicked.connect(self._game_start_stop)
        self.optionView.toggled.connect(self._show_replacement)
        self._layout_reset()

        self.actionAboutRacko.triggered.connect(self._about_racko)
        self.actionAboutAuthor.triggered.connect(self._about_author)
        self.actionExit.triggered.connect(self._custom_quit)
        self.actionGameRules.triggered.connect(self._game_rules)
        self.actionHowToPlay.triggered.connect(self._how_to_play)

    def _load_engine_setting(self):
        self._game_engine = Engine(SIZE_RACK)
        self._game_engine.scoresUpdate.connect(self._round_scores_update)
        self._game_engine.drawPile.connect(self._refresh_draw_pile)
        self._game_engine.discardPile.connect(self._refresh_discard_pile)
        self._game_engine.rackSlotColor.connect(self._change_slot_color)
        self._game_engine.rackSlotValue.connect(self._change_slot_value)
        self._game_engine.gameStatus.connect(self._refresh_game_status)
        self._game_engine.actionLock.connect(self._control_in_action)
        self._game_engine.humanPlay.connect(self._human_play)

    def _layout_reset(self):
        self.headingSetup.setText("Choose 1-3 computer players: (place randomly)")
        self.statusGame.setText("")
        self.headingGame.setText("")
        self.headingDrawPile.setText("Draw Pile")
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
                self._racks_labels[rack][slot].setStyleSheet(BG_DISABLED)
                self._racks_labels[rack][slot].setText("")
                self._racks_labels[rack][slot].setLineWidth(0)
        self.imgDrawPile.setPixmap(self._cards_files.backcard())
        self.imgDiscardPile.setPixmap(self._cards_files.card_number(0))

    def _player3_disable(self):
        if self.optionPlayer3.currentIndex() == 0:
            self.optionPlayer4.setCurrentIndex(0)

    def _player4_select(self):
        if self.optionPlayer3.currentIndex() == 0:
            if self.optionPlayer4.currentIndex() > 1:
                value = self.optionPlayer4.currentIndex() - 1
                self.optionPlayer3.setCurrentIndex(value)
                self.optionPlayer4.setCurrentIndex(0)
            elif self.optionPlayer4.currentIndex() == 1:
                self.optionPlayer4.setCurrentIndex(0)

    def _game_start_stop(self):
        if self._game_active:
            reply = QMessageBox.question(None, '', 'You are requesting to stop the game,\n' + 
                                                'do you want computer take over your turn?\n' + 
                                                'Yes - finish remaining game in fast speed\n' +
                                                'No - stop the game now',
                                QMessageBox.Yes | QMessageBox.No, QMessageBox.No)
            if reply == QMessageBox.Yes:
                self._game_computer_take_over()
            else: 
                self._game_terminate()
        else:
            self._layout_reset()
            self._game_start()
            self._game_active = True
            self.headingSetup.setEnabled(False)
            self.requirePlayer2.setEnabled(False)
            self.optionPlayer3.setEnabled(False)
            self.optionPlayer4.setEnabled(False)
            self.actionButton.setText("Stop")

    def _game_terminate(self):
            self._game_active = False
            self.headingSetup.setEnabled(True)
            self.requirePlayer2.setEnabled(True)
            self.optionPlayer3.setEnabled(True)
            self.optionPlayer4.setEnabled(True)
            self.actionButton.setText("New Game")

    def _game_computer_take_over(self):
        human_id = -1;
        if not self._layout1_lock:
            human_id = 0
        elif not self._layout3_lock:
            human_id = 2
        count_computer = 0
        for player in self._players_list:
            if player is not None:
                count_computer += 1
        computer_replacement = self._gateway.setPlayerReplacement()
        self._players_list[human_id] = computer_replacement
        self._game_engine.setHumanReplacement(human_id, computer_replacement)
        change_speed = True
        if len(self._players_list) == 4:
            for player in self._players_list:
                if player is None:
                    reply = QMessageBox.question(None, '', 'You have twe set of racks.\n' + 
                                                'Do you want to replacment to computer for the other set?\n' +
                                                'If not, game will remains in same speed setting.',
                                QMessageBox.Yes | QMessageBox.No, QMessageBox.No)
                    if reply == QMessageBox.Yes:
                        if human_id == 0:
                            computer_replacement2 = self._gateway.setPlayerReplacement()
                            self._players_list[2] = computer_replacement2
                            self._game_engine.setHumanReplacement(2, computer_replacement2)
                        else:
                            computer_replacement2 = self._gateway.setPlayerReplacement()
                            self._players_list[0] = computer_replacement2
                            self._game_engine.setHumanReplacement(0, computer_replacement2)
                    else:
                        change_speed = False
        if change_speed:
            self._game_engine.setAutoRun()
        self._game_engine.start()

    def _game_start(self):
        self._status_line = ["", "", "", "", "", "", ""]
        self._setup_players()
        for rack in self._players_layout:
            for slot in range(SIZE_RACK):
                self._racks_labels[rack][slot].setText("")
                self._racks_labels[rack][slot].setStyleSheet(BG_DISABLED)
        self._game_engine.setup(self._players_list, self._players_name,
                                self._players_layout, self._racko_size, self.optionView.isChecked())
        self._game_engine.start()

    def _setup_players(self):
        self._players_list = [None]
        self.headingPlayer1.setText("Player")
        self.scorePlayer1.setText("0")
        if self.optionPlayer3.currentIndex() == 0:
            self._players_name = ["Player", "Computer"]
            self.headingPlayer3.setText("Comp")
            self.scorePlayer3.setText("0")
            self._players_layout = [0, 2]
            self._number_of_players = 2
            self._racko_size = SIZE_RACKO2
            self._gateway.entry_point.setNumberOfPlayers(self._number_of_players - 1, self._racko_size)
            self._players_list.append(self._computer_player1[self.requirePlayer2.currentIndex()](1))
        elif self.optionPlayer4.currentIndex() == 0:
            self._players_name = ["Player", "Computer 1", "Computer 2"]
            self.headingPlayer2.setText("Computer 1")
            self.scorePlayer2.setText("0")
            self.headingPlayer3.setText("Comp 2")
            self.scorePlayer3.setText("0")
            self._players_layout = [0, 1, 2]
            self._number_of_players = 3
            self._racko_size = SIZE_RACKO3
            self._gateway.entry_point.setNumberOfPlayers(self._number_of_players - 1, self._racko_size)
            self._players_list.append(self._computer_player1[self.requirePlayer2.currentIndex()](1))
            order = randrange(2)
            if order == 1:
                self._players_list.append(self._computer_player2[self.optionPlayer3.currentIndex()](2))
            else:
                swap_player = self._players_list[1]
                self._players_list[1] = self._computer_player2[self.optionPlayer3.currentIndex()](2)
                self._players_list.append(swap_player)
        else:
            self._players_layout = [0, 1, 2, 3]
            self._number_of_players = 4
            self._racko_size = SIZE_RACKO4
            if self.optionPlayer4.currentIndex() == 1:
                self._players_name = ["Player", "Computer 1", "Player 2", "Computer 2"]
                self.headingPlayer2.setText("Computer 1")
                self.scorePlayer2.setText("0")
                self.headingPlayer3.setText("Player2")
                self.scorePlayer3.setText("0")
                self.headingPlayer4.setText("Computer 2")
                self.scorePlayer4.setText("0")
                self._gateway.entry_point.setNumberOfPlayers(self._number_of_players - 2, self._racko_size)
                self._players_list.append(self._computer_player1[self.requirePlayer2.currentIndex()](1))
                self._players_list.append(None)
                order = randrange(2)
                if order == 1:
                    self._players_list.append(self._computer_player2[self.optionPlayer3.currentIndex()](2))
                else:
                    swap_player = self._players_list[1]
                    self._players_list[1] = self._computer_player2[self.optionPlayer3.currentIndex()](2)
                    self._players_list.append(swap_player)
            else:
                self._players_name = ["Player", "Computer 1", "Computer 2", "Computer 3"]
                self.headingPlayer2.setText("Computer 1")
                self.scorePlayer2.setText("0")
                self.headingPlayer3.setText("Comp 2")
                self.scorePlayer3.setText("0")
                self.headingPlayer4.setText("Computer 3")
                self.scorePlayer4.setText("0")
                self._gateway.entry_point.setNumberOfPlayers(self._number_of_players - 1, self._racko_size)
                self._players_list.append(self._computer_player1[self.requirePlayer2.currentIndex()](1))
                order = randrange(2)
                if order == 1:
                    self._players_list.append(self._computer_player2[self.optionPlayer3.currentIndex()](2))
                else:
                    swap_player = self._players_list[1]
                    self._players_list[1] = self._computer_player2[self.optionPlayer3.currentIndex()](2)
                    self._players_list.append(swap_player)
                order = randrange(3)
                if order == 2:
                    self._players_list.append(self._computer_player3[self.optionPlayer4.currentIndex()](3))
                else:
                    swap_player = self._players_list[order + 1]
                    self._players_list[order + 1] = self._computer_player3[self.optionPlayer4.currentIndex()](3)
                    self._players_list.append(swap_player)
        self._range_rack = range(1, self._racko_size + 1)
        self.headingDrawPile.setText("Draw Pile (" + str(self._racko_size) + " left)")
        self.headingDiscardPile.setText("Discard Pile")
        for rack in self._players_layout:
            for slot in range(SIZE_RACK):
                self._racks_labels[rack][slot].setLineWidth(1)

    def _round_scores_update(self, round_number, scores, is_game_end):
        self.headingGame.setText("Round " + str(round_number))
        for player in range(self._number_of_players):
            self._scores_labels[self._players_layout[player]].setText(
                str(scores[player]))
        if is_game_end and self._game_active:
            self._game_terminate()

    def _refresh_draw_pile(self, count, value, face_up):
        if count > 0:
            self.headingDrawPile.setText("Draw Pile (" + str(count) + " left)")
            self.imgDrawPile.setPixmap(self._cards_files.backcard())
        else:
            self.headingDrawPile.setText("Draw Pile (empty)")
            self.imgDrawPile.setPixmap(self._cards_files.card_number(0))
        if face_up:
            self.headingDrawPile.setText("Reviewing a draw card")
            if value in self._range_rack:
                self.imgDrawPile.setPixmap(self._cards_files.card_number(value))
            else:
                self.headingDrawPile.setText("Draw Pile (empty) ERROR : " + str(value))

    def _refresh_discard_pile(self, count, value, face_up):
        if count > 0:
            self.headingDiscardPile.setText("Discard Pile (" + str(count) + ")")
            if face_up:
                if value in self._range_rack:
                    self.imgDiscardPile.setPixmap(self._cards_files.card_number(value))
                else:
                    self.headingDiscardPile.setText("Discard Pile (empty) ERROR : " + str(value))
            else:
                self.headingDiscardPile.setText("Reviewing a draw card")
                self.imgDiscardPile.setPixmap(self._cards_files.backcard())
        else:
            self.headingDiscardPile.setText("Discard Pile")

    def _change_slot_color(self, layout_id, slot, color_code, active_state):
        if active_state:
            self._racks_labels[layout_id][slot].setStyleSheet(BG_ACTIVE[color_code])
        else:
            self._racks_labels[layout_id][slot].setStyleSheet(BG_INACTIVE[color_code])

    def _change_slot_value(self, layout_id, slot, value):
        self._racks_labels[layout_id][slot].setText("<font color="
                                                    + NUMBER_COLOR + ">" + value + "</font>")

    def _refresh_game_status(self, line):
        self._status_line.insert(0, line)
        msg = line
        for line_number in range(1, 7):
            msg += "<BR>" + self._status_line[line_number]
        self.statusGame.setText(msg)

    def _show_replacement(self):
        if self.optionView.isChecked():
            self._game_engine.replacementVisible(True)
        else:
            self._game_engine.replacementVisible(False)

    def _control_in_action(self, lock):
        if lock:
            self.actionButton.setEnabled(False)
            self.optionView.setEnabled(False)
            self._layout1_lock = lock
            self._layout3_lock = lock
            self._draw_pile_lock = lock
        else:
            self.actionButton.setEnabled(True)
            self.optionView.setEnabled(True)

    def _pick_card(self, layout_id, slot):
        if layout_id == 1 and not self._layout1_lock:
            self._game_engine.humanResponse(slot - 1)
            self._use_discard_card = False
            self._layout1_lock = True
            self._game_engine.start()
        elif layout_id == 3 and not self._layout3_lock:
            self._game_engine.humanResponse(slot - 1)
            self._use_discard_card = False
            self._layout3_lock = True
            self._game_engine.start()

    def _draw_pile_action(self):
        if not self._draw_pile_lock:
            if self._use_discard_card:
                self._game_engine.humanDraw()
                self._use_discard_card = False
            else:
                self._game_engine.humanResponse(-1)
                if not self._layout1_lock:
                    self._layout1_lock = True
                elif not self._layout3_lock:
                    self._layout3_lock = True
                self._game_engine.start()

    def _human_play(self, player_id):
        self._use_discard_card = True
        self._draw_face_up = False
        self._draw_pile_lock = False
        if player_id == 0:
            self._layout1_lock = False
        elif player_id == 2:
            self._layout3_lock = False

    def _about_racko(self):
        QMessageBox.information(None, 'About Racko Game', 'About Racko Game\n' +
                                'A 2-4 players rack-o! with computer steagery.\n' +
                                'Game setup are based on the real card game.\n' +
                                'View the following link for more information:\n' +
                                'http://www.hasbro.com/common/instruct/Racko(1987).PDF',
                                QMessageBox.Close, QMessageBox.Close)

    def _about_author(self):
        QMessageBox.information(None, 'About Author', 'Author: Meisze Wong\n' +
                                'www.linkedin.com/pub/macy-wong/46/550/37b/\n\n' +
                                'view source code:\nhttps://github.com/mwong510ca/RackoGame-ComputerStragery',
                                QMessageBox.Close, QMessageBox.Close)

    def _game_rules(self):
        QMessageBox.information(None, 'Game Rules', 'Rack-o! View the game rules from the following link:\n' +
                                'http://www.hasbro.com/common/instruct/Racko(1987).PDF',
                                QMessageBox.Close, QMessageBox.Close)

    def _how_to_play(self):
        QMessageBox.information(None, 'How To Play', 'Rack-o! How to play:\n\n'
                                '1. Choose 1 to 3 computer players and start the game.\n' +
                                '   You may play second set in a 4 playes rack-o!\n\n' +
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

    def _custom_quit(self):
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
        subprocess.Popen(['java', '-jar', 'RackoComputerPlayersGateway.jar', str(port_number)])
        time.sleep(1)
    except:
        sys.exit()

    gateway_server = JavaGateway(GatewayClient(address=host, port=port_number))
    app = QApplication(sys.argv)
    window = GameRacko(gateway_server)
    window.show()
    while app.exec_() > 0:
        time.sleep(1)
    gateway_server.shutdown()
    sys.exit()
