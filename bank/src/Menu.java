import java.util.ArrayList;

public class Menu {
  private String name;
  private Menu prev;
  private ArrayList<DisplayText> texts;
  private ArrayList<ScreenButton> buttons;
  private ArrayList<InputDevice> inputDevices;
  private Serial arduino;

  Menu(String name, ArrayList<DisplayText> texts, Serial arduino) {
    this(name, texts, null, arduino);
  }
  Menu(String name, ArrayList<DisplayText> texts, ArrayList<ScreenButton> buttons, Serial arduino) {
    this.name = name;
    this.prev = null;
    this.texts = texts;
    this.buttons = buttons;
    this.arduino = arduino;
    if(this.buttons == null) {
      this.buttons = new ArrayList<ScreenButton>();
    }
    this.inputDevices = new ArrayList<InputDevice>();
    for(int i = 0; i < this.buttons.size(); i++) {
      this.inputDevices.add(this.buttons.get(i));
    }

  }

  public String getName() {
    return this.name;
  }
  // loads the menu on the screen
  // requires the used screen.
  public void open(ATMScreen screen) {
    screen.clear();
    for(int i = 0; i < this.texts.size(); i++) {
      screen.add(this.texts.get(i));
    }
    for(int i = 0; i < this.buttons.size(); i++) {
      screen.add(this.buttons.get(i));
    }
  }
  // loads the menu on the screen and sets the previous for backtracking.
  public void open(ATMScreen screen, Menu prev) {
    this.prev = prev;
    this.open(screen);
  }

  public void addInputDevice(InputDevice device) {
    this.inputDevices.add(device);
  }

  // listneds for input from both the software keys and hardware keys.
  // returns the pressed string.
  public String listenForInput() {
    String input;
    String arduinoInput;
    int counter = 0;
    do {
      arduino.listenSerial();
      input = this.inputDevices.get(counter).getInput();
      arduinoInput = arduino.getKey();
      if(!arduinoInput.isEmpty()) {
        input = arduinoInput;
        arduino.resetKey();
      }
      if(++counter > (this.inputDevices.size()-1)) {
        counter = 0;
      }
    } while(input == null);
    return input;
  }

  public Menu getPrev() {
    return this.prev;
  }

  public void editText(String text, int textNumber) {
    this.texts.get(textNumber).giveOutput(text);
  }


}
