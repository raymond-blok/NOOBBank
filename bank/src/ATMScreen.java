import java.awt.Container;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Font;

public class ATMScreen extends Container{
  ATMScreen() {
    super();
    this.setLayout(null);
  }
  public void add(ScreenElement screenElement) {
    screenElement.setContainer(this);
  }
  public void clear() {
    removeAll();
  }
  public void paint(Graphics g) {
    super.paint(g);
    g.fillRoundRect(347, 230, 35, 35, 10, 10);
    g.fillRect(377, 260, 5, 5);
    g.setColor(Color.WHITE);
    g.setFont(new Font("SansSerif", Font.BOLD, 20));
    g.drawString("HR", 350, 250);
    g.setFont(new Font("SansSerif", Font.PLAIN, 12));
    g.drawString("bank", 351, 260);
  }
}
