import java.awt.Point;
import java.awt.Container;
import java.awt.Button;
import java.awt.event.*;

public class ScreenButton extends ScreenElement implements InputDevice, ActionListener {
  Button button;
  boolean inputAvailable;
  ScreenButton(String name, Point POS) {
    super(name, POS);
    this.button = new Button(name);
    this.button.setBounds(POS.x, POS.y, 10 + 15 * name.length(), 25);
    this.button.addActionListener(this);
  }
  public void setContainer(Container container) {
    container.add(this.button);
  }
  public String getInput() {
    if(this.inputAvailable) {
      this.inputAvailable = false;
      return this.name;
    }
    return null;
  }
  public void actionPerformed(ActionEvent event) {
    this.inputAvailable = true;
  }

}
