import java.util.ArrayList;

public class Menu {
  private String name;
  private Menu prev;
  private ArrayList<DisplayText> texts;
  private ArrayList<ScreenButton> buttons;
  private ArrayList<InputDevice> inputDevices;

  Menu(String name, ArrayList<DisplayText> texts) {
    this(name, texts, null);
  }
  Menu(String name, ArrayList<DisplayText> texts, ArrayList<ScreenButton> buttons) {
    this.name = name;
    this.prev = null;
    this.texts = texts;
    this.buttons = buttons;
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

  public void open(ATMScreen screen) {
    screen.clear();
    for(int i = 0; i < this.texts.size(); i++) {
      screen.add(this.texts.get(i));
    }
    for(int i = 0; i < this.buttons.size(); i++) {
      screen.add(this.buttons.get(i));
    }
  }

  public void open(ATMScreen screen, Menu prev) {
    this.prev = prev;
    this.open(screen);
    System.out.println(prev.getName());
  }

  public void addInputDevice(InputDevice device) {
    this.inputDevices.add(device);
  }

  public String listenForInput() {
    String input;
    int counter = 0;
    do {
      input = this.inputDevices.get(counter).getInput();

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
