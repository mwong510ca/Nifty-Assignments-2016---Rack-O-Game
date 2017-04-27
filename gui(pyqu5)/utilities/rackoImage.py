"""
" Cards is the QObject for appRacko.  It contains all image paths
" for the main application appRacko.
"
" author Meisze Wong
"        www.linkedin.com/pub/macy-wong/46/550/37b/
"        github.com/mwong510ca/RackoGame-ComputerStrategy
"""

# !/usr/bin/env python3

import sys

from PyQt5.QtCore import QObject, QDir, QFile
from PyQt5.QtGui import QPixmap
from PIL import Image

IMG_FOLDER_NAME = "images"


class Cards(QObject):
    def __init__(self):
        super(Cards, self).__init__()
        load_success = False
        dir_path = QDir()
        if dir_path.exists(IMG_FOLDER_NAME):
            load_success = True
            prefix = IMG_FOLDER_NAME + QDir.separator()
        else:
            print("falied 1")

        img_extension = ".png"

        if load_success:
            self.backcard_file = prefix + "backcard" + img_extension
            if not QFile(self.backcard_file).exists():
                load_success = False
                print("falied 2")
            else:
                try:
                    Image.open(self.backcard_file)
                except IOError:
                    load_success = False
                    print("falied 3")

        if load_success:
            card_prefix = prefix + "card"
            self.card_files = (prefix + "empty" + img_extension,
                            card_prefix + "01" + img_extension, card_prefix + "02" + img_extension,
                            card_prefix + "03" + img_extension, card_prefix + "04" + img_extension,
                            card_prefix + "05" + img_extension, card_prefix + "06" + img_extension,
                            card_prefix + "07" + img_extension, card_prefix + "08" + img_extension,
                            card_prefix + "09" + img_extension, card_prefix + "10" + img_extension,
                            card_prefix + "11" + img_extension, card_prefix + "12" + img_extension,
                            card_prefix + "13" + img_extension, card_prefix + "14" + img_extension,
                            card_prefix + "15" + img_extension, card_prefix + "16" + img_extension,
                            card_prefix + "17" + img_extension, card_prefix + "18" + img_extension,
                            card_prefix + "19" + img_extension, card_prefix + "20" + img_extension,
                            card_prefix + "21" + img_extension, card_prefix + "22" + img_extension,
                            card_prefix + "23" + img_extension, card_prefix + "24" + img_extension,
                            card_prefix + "25" + img_extension, card_prefix + "26" + img_extension,
                            card_prefix + "27" + img_extension, card_prefix + "28" + img_extension,
                            card_prefix + "29" + img_extension, card_prefix + "30" + img_extension,
                            card_prefix + "31" + img_extension, card_prefix + "32" + img_extension,
                            card_prefix + "33" + img_extension, card_prefix + "34" + img_extension,
                            card_prefix + "35" + img_extension, card_prefix + "36" + img_extension,
                            card_prefix + "37" + img_extension, card_prefix + "38" + img_extension,
                            card_prefix + "39" + img_extension, card_prefix + "40" + img_extension,
                            card_prefix + "41" + img_extension, card_prefix + "42" + img_extension,
                            card_prefix + "43" + img_extension, card_prefix + "44" + img_extension,
                            card_prefix + "45" + img_extension, card_prefix + "46" + img_extension,
                            card_prefix + "47" + img_extension, card_prefix + "48" + img_extension,
                            card_prefix + "49" + img_extension, card_prefix + "50" + img_extension,
                            card_prefix + "51" + img_extension, card_prefix + "52" + img_extension,
                            card_prefix + "53" + img_extension, card_prefix + "54" + img_extension,
                            card_prefix + "55" + img_extension, card_prefix + "56" + img_extension,
                            card_prefix + "57" + img_extension, card_prefix + "58" + img_extension,
                            card_prefix + "59" + img_extension, card_prefix + "60" + img_extension)

            for idx in range(61):
                if not QFile(self.card_files[idx]).exists():
                    load_success = False
                    print("falied 4 " + str(idx))
                    break
                try:
                    Image.open(self.card_files[idx])
                except IOError:
                    load_success = False
                    print("falied 5 " + str(idx))
                    break

        if not load_success:
            print("Missing required card images, exit program.")
            #sys.exit()

    def backcard(self):
        return QPixmap(self.backcard_file)

    def card_number(self, value):
        if value in range(1, len(self.card_files)):
            return QPixmap(self.card_files[value])
        else:
            return QPixmap(self.card_files[0])
