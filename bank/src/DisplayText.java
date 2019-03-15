import java.awt.Point;
import java.awt.Container;
import java.awt.Label;
import java.awt.Color;
import java.awt.Font;

public class DisplayText extends ScreenElement implements OutputDevice {
  Label label;

  DisplayText(String name, Point POS) {
    super(name, POS);
    this.label = new Label();
    this.label.setForeground(Color.WHITE);
    this.label.setFont(new Font("SansSerif", Font.BOLD, 30));
    this.label.setBounds(POS.x, POS.y, 400, 35);
  }
  public void setContainer(Container container) {
    container.add(this.label);
  }
  public void giveOutput(String output) {
    this.label.setText(output);
  }
}
