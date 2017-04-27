"""
" CardLabel is the custom QLabel object of Racko Card for appRacko.
" It supported click function for the card image.
"
" author Meisze Wong
"        www.linkedin.com/pub/macy-wong/46/550/37b/
"        github.com/mwong510ca/RackoGame-ComputerStrategy
"""

#!/usr/bin/env python3

from PyQt5.QtWidgets import QLabel
from PyQt5.QtCore import pyqtSignal


class CardLabel(QLabel):
    clickedLabel = pyqtSignal()

    def __init(self, parent):
        QLabel.__init__(self, parent)

    def mousePressEvent(self, ev):
        self.clickedLabel.emit()
