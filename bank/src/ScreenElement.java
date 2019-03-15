import java.awt.Point;
import java.awt.Container;

public abstract class ScreenElement extends ATMElement {
  Point POS;
  ScreenElement(String name, Point POS) {
    super(name);
    this.POS = POS;
  }
  public abstract void setContainer(Container container);
}
