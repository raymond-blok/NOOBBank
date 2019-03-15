import java.awt.Frame;
import java.awt.Color;
import java.awt.event.*;
import java.awt.Point;
import java.util.ArrayList;
import java.awt.Button;
import java.lang.Integer;

public class ATM {
  private Bank bank;
  private ATMScreen as;
  CardReader accountReader;
  DisplayText text;
  ArrayList<ScreenButton> keypadButtonList;
  ArrayList<InputDevice> inputDevices;

  Keypad keypad;
  ATM(Bank bank) {
    this.bank = bank;
    this.as = new ATMScreen();
    Frame f = new Frame("My ATM");
    this.keypadButtonList = new ArrayList<ScreenButton>();
    f.setBounds(200, 200, 400, 300);
    f.setBackground(Color.BLUE);
    f.addWindowListener(new MyWindowAdapter(f));
    f.add(as);
    f.setVisible(true);
    // text
    this.text = new DisplayText("welkomstext", new Point(200, 200));
    this.as.add(text);
    text.giveOutput("Insert your card");
    this.accountReader = new CardReader("account");
    this.keypad = new Keypad("keypad");
    this.keypadButtonList = this.genButtons(20, 20, 3, 3, 40, 40, "0");
    this.inputDevices = new ArrayList<InputDevice>();
    for(int i = 0; i < this.keypadButtonList.size(); i++) {
      this.inputDevices.add(this.keypadButtonList.get(i));
    };
    this.inputDevices.add(this.keypad);
    while(true) {
      this.doTransaction();
    }
  }
  public void doTransaction() {
    String account_text;
    Client client;
    while(true) {
      do{
        account_text = this.accountReader.getInput();
      } while(account_text == null);
      client = this.bank.get(account_text);
      if(client != null){
        break;
      }
      this.text.giveOutput("wacht een paar en begin opnieuw...");
    }
    for(int i = 0; i < this.keypadButtonList.size(); i++) {
      this.as.add(this.keypadButtonList.get(i));
    }
    this.text.giveOutput("enter your PIN");

    // String keyValue;
    // keyValue = this.keypad.getInput();
    // if(keyValue != null) {
    //   this.text.giveOutput(keyValue);
    // }
    int counter = 0;
    String buttonText;
    String pinList = "";
    String xText = "";
    DisplayText pinText = new DisplayText("pinText", new Point(200, 250));
    this.as.add(pinText);
    while(true) {
      while(true) {
        buttonText = this.inputDevices.get(counter).getInput();
        if(buttonText != null) {
          if( buttonText.equals("\n")){
            break;
          }
          pinList += buttonText;
          xText += "X";
          System.out.println(buttonText);
          pinText.giveOutput(xText);
        }
        counter++;
        if(counter > (this.inputDevices.size()-1)) {
          counter = 0;
        }
      }
      if(client.checkPin(pinList)) {
        break;
      }
      xText = "";
      pinList = "";
      pinText.giveOutput(xText);
    }
    this.as.clear();
    this.as.add(this.text);
    this.text.giveOutput("youre in!!!");
  }
  private ArrayList<ScreenButton> genButtons(int startX, int startY ,int gridX, int gridY, int gapX, int gapY, String lastButton) {
    ArrayList<ScreenButton> temp = new ArrayList<ScreenButton>();
    int counter = 1;
    int positionButtonX = 0;
    int positionButtonY = 0;
    ScreenButton screenButton;
    for (int y = 0; y < gridX; y++) {
      for(int x = 0; x < gridY; x++) {
        positionButtonX = startX+x*gapX;
        positionButtonY = startY+y*gapY;
        screenButton = new ScreenButton(Integer.toString(counter++), new Point(positionButtonX, positionButtonY));
        temp.add(screenButton);
        screenButton.button.setBackground(Color.WHITE);
      }
    }
    if(lastButton != null) {
      screenButton = new ScreenButton(lastButton, new Point(positionButtonX-gapX, positionButtonY+gapY));
      temp.add(screenButton);
      screenButton.button.setBackground(Color.WHITE);
    }
    return temp;
  }

  private ArrayList<ScreenButton> genButtons(int startX, int startY ,int gridX, int gridY, int gapX, int gapY, String lastButton, String[] names) {
    ArrayList<ScreenButton> temp = new ArrayList<ScreenButton>();
    int counter = 0;
    int positionButtonX = 0;
    int positionButtonY = 0;
    ScreenButton screenButton;
    for (int y = 0; y < gridX; y++) {
      for(int x = 0; x < gridY; x++) {
        positionButtonX = startX+x*gapX;
        positionButtonY = startY+y*gapY;
        screenButton = new ScreenButton(names[counter++], new Point(positionButtonX, positionButtonY));
        temp.add(screenButton);
        screenButton.button.setBackground(Color.WHITE);
      }
    }
    if(lastButton != null) {
      screenButton = new ScreenButton(lastButton, new Point(positionButtonX-gapX, positionButtonY+gapY));
      temp.add(screenButton);
      screenButton.button.setBackground(Color.WHITE);
    }
    return temp;
  }
}
