
from PyQt5.QtCore import QObject
from PyQt5.QtGui import QPixmap

class Cards(QObject):
    def __init__(self):
        super(Cards, self).__init__()
        self._backcard_file = "images/backcard.png"
        img_prefix = "images/card"
        img_extension = ".png"
        self._cards_files = ["images/empty.png",
            img_prefix + "01" + img_extension, img_prefix + "02" + img_extension,
            img_prefix + "03" + img_extension, img_prefix + "04" + img_extension,
            img_prefix + "05" + img_extension, img_prefix + "06" + img_extension,
            img_prefix + "07" + img_extension, img_prefix + "08" + img_extension,
            img_prefix + "09" + img_extension, img_prefix + "10" + img_extension,
            img_prefix + "11" + img_extension, img_prefix + "12" + img_extension,
            img_prefix + "13" + img_extension, img_prefix + "14" + img_extension,
            img_prefix + "15" + img_extension, img_prefix + "16" + img_extension,
            img_prefix + "17" + img_extension, img_prefix + "18" + img_extension,
            img_prefix + "19" + img_extension, img_prefix + "20" + img_extension,
            img_prefix + "21" + img_extension, img_prefix + "22" + img_extension,
            img_prefix + "23" + img_extension, img_prefix + "24" + img_extension,
            img_prefix + "25" + img_extension, img_prefix + "26" + img_extension,
            img_prefix + "27" + img_extension, img_prefix + "28" + img_extension,
            img_prefix + "29" + img_extension, img_prefix + "30" + img_extension,
            img_prefix + "31" + img_extension, img_prefix + "32" + img_extension,
            img_prefix + "33" + img_extension, img_prefix + "34" + img_extension,
            img_prefix + "35" + img_extension, img_prefix + "36" + img_extension,
            img_prefix + "37" + img_extension, img_prefix + "38" + img_extension,
            img_prefix + "39" + img_extension, img_prefix + "40" + img_extension,
            img_prefix + "41" + img_extension, img_prefix + "42" + img_extension,
            img_prefix + "43" + img_extension, img_prefix + "44" + img_extension,
            img_prefix + "45" + img_extension, img_prefix + "46" + img_extension,
            img_prefix + "47" + img_extension, img_prefix + "48" + img_extension,
            img_prefix + "49" + img_extension, img_prefix + "50" + img_extension,
            img_prefix + "51" + img_extension, img_prefix + "52" + img_extension,
            img_prefix + "53" + img_extension, img_prefix + "54" + img_extension,
            img_prefix + "55" + img_extension, img_prefix + "56" + img_extension,
            img_prefix + "57" + img_extension, img_prefix + "58" + img_extension,
            img_prefix + "59" + img_extension, img_prefix + "60" + img_extension]

    def backcard(self):
        return QPixmap(self._backcard_file)

    def card_number(self, value):
        if value > 0 and value < len(self._cards_files):
            return QPixmap(self._cards_files[value])
        else:
            return QPixmap(self._cards_files[0])
