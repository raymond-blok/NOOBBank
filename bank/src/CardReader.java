import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class CardReader extends HardwareElement implements InputDevice {
  BufferedReader reader;

  CardReader(String name) {
    super(name);
    this.reader = new BufferedReader(new InputStreamReader(System.in));
  }

  public String getInput() {
    try {
      System.out.println("To simulate inserting card, enter card number");
      String account = this.reader.readLine();
      return account;
    } catch(IOException e) {
      return null;
    }

  }
}
