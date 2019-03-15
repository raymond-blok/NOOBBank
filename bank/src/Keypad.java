import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.lang.Character;
public class Keypad extends HardwareElement implements InputDevice {
  BufferedReader reader;

  Keypad(String name) {
    super(name);
    this.reader = new BufferedReader(new InputStreamReader(System.in));
  }
  public String getInput() {
    try {
      if(this.reader.ready()) {
         int c = this.reader.read();
         if((char)c != ' ') {
           return Character.toString((char)c);
         }
      }
      return null;
    } catch(IOException e) {
      System.err.println(e.toString());
      return null;
    }
  }
}
