import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;


public class Card {
    private int value;
    private BufferedImage img;
    public final String img_front = "card.png";
    public final String img_back = "backcard.png";
    private int pos_x = 30;
    private int pos_y = 30;
    
    public Card(int value, int x, int y) {
        this.value = value;
        this.pos_x = x;
        this.pos_y = y;
        
        try {
            if (img == null) {
                img = ImageIO.read(new File(img_front));
            }
        } catch (IOException e) {
            System.out.println("Internal Error:" + e.getMessage());
        }
    }
    
    public int getValue() {
        return value;
    }
    
    public void setValue(int newVal) {
        value = newVal;
    }
    
    public int getX() {
        return pos_x;
    }
    
    public int getY() {
        return pos_y;
    }

    public void setX(int newX) {
        pos_x = newX;
    }
    
    public void setY(int newY) {
        pos_y = newY;
    }
    
    public void setImgFront() {
        try {
            img = ImageIO.read(new File(img_front));
        } catch (IOException e) {
            System.out.println("Internal Error:" + e.getMessage());
        }
    }
    
    public void setImgBack() {
        try {
            img = ImageIO.read(new File(img_back));
        } catch (IOException e) {
            System.out.println("Internal Error:" + e.getMessage());
        }
    }
    
    public void draw(Graphics g) {
        g.drawImage(img, pos_x, pos_y, 300, 201, null);
    }
}
