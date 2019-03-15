import java.awt.Frame;
import java.awt.event.*;

class MyWindowAdapter extends WindowAdapter {
  Frame f;
  MyWindowAdapter(Frame f) {
    this.f = f;
  }
  public void windowClosing(WindowEvent e) {
    f.dispose();
    System.exit(0);
  }
}
