#!/usr/bin/env python3

from PyQt5.QtWidgets import QLabel
from PyQt5.QtCore import pyqtSignal

class CardLabel(QLabel):
    clickedLabel = pyqtSignal()
    
    def __init(self, parent):
        QLabel.__init__(self, parent)
    """    
    def setPosition(self, playerId, cardId):
        self._playerId = playerId
        self._cardId = cardId
    """ 
    def mousePressEvent(self, ev):
        self.clickedLabel.emit()
    """
    def getPlayerId(self):
        return self._playerId

    def getCardId(self):
        return self._cardId
    """