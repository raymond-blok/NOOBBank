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
  ArrayList<ScreenButton> buttonList;

  Keypad keypad;
  ATM(Bank bank) {
    this.bank = bank;
    this.as = new ATMScreen();
    Frame f = new Frame("My ATM");
    this.buttonList = new ArrayList<ScreenButton>();
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
    this.buttonList = this.genButtons(20, 20, 3, 3, 40, 40, "0");


    while(true) {
      this.doTransaction();
    }
  }
  public void doTransaction() {
    String account_text;
    while(true) {
      do{
        account_text = this.accountReader.getInput();
      } while(account_text == null);
      Client client = this.bank.get(account_text);
      if(client != null){
        break;
      }
      this.text.giveOutput("wacht een paar en begin opnieuw...");
    }
    for(int i = 0; i < this.buttonList.size(); i++) {
      this.as.add(this.buttonList.get(i));
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
    while(true) {
      buttonText = this.buttonList.get(counter).getInput();
      if(buttonText != null) {

        pinList += buttonText;
      }

      counter++;
      if(counter > 9) {
        counter = 0;
      }
    }
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
