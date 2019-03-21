import java.awt.Frame;
import java.awt.Color;
import java.awt.event.*;
import java.awt.Point;
import java.util.ArrayList;
import java.awt.Button;
import java.lang.Integer;
import java.util.concurrent.TimeUnit;
import java.lang.InterruptedException;

public class ATM {
  private Bank bank;
  private ATMScreen as;
  private String yes;
  CardReader accountReader;
  DisplayText mainText;
  DisplayText mainText2;
  DisplayText pinText;
  ArrayList<ScreenButton> keypadButtonList;
  ArrayList<ScreenButton> amountButtonList;
  ArrayList<ScreenButton> confirmButtonList;
  ArrayList<InputDevice> inputDevices;
  ReceiptPrinter receiptPrinter;

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
    this.yes = "yes";
    this.mainText = new DisplayText("mainText", new Point(10, 10));
    this.mainText2 = new DisplayText("mainText", new Point(10, 40));

    this.pinText = new DisplayText("pinText", new Point(200, 50));
    this.accountReader = new CardReader("account");
    this.keypad = new Keypad("keypad");
    this.receiptPrinter = new ReceiptPrinter("receiptPrinter");
    this.keypadButtonList = this.genButtons(40, 60, 3, 3, 40, 40, "0");
    this.amountButtonList = this.genButtons(40, 60, 1, 4, 80, 80, null, new String[]{"\u20AC 10", "\u20AC 20", "\u20AC 50", "\u20AC 100"});
    this.confirmButtonList = this.genButtons(40, 150, 1, 2, 200, 200, null, new String[]{this.yes,"no"});
    while(true) {
      this.doTransaction();
    }
  }

  private void doTransaction() {
    Client client;
    String pinCode;
    int amount;
    client = this.getClient();
    pinCode = this.authenticate(client);
    this.withdrawMenu(client, pinCode);
  }

  private Client getClient() {
    Client client;
    this.as.clear();
    this.mainText.giveOutput("Insert your card");
    this.as.add(this.mainText);
    do {
      String IBan = this.accountReader.getInput();
      client = this.bank.get(IBan);
      if(IBan != null && client == null) {
        this.mainText.giveOutput("no card found, please try again.");
      }
    } while(client == null);
    return client;
  }

  private String authenticate(Client client) {
    String pinCode = "";
    String xes = "";
    String buttonPressed;
    ArrayList<InputDevice> inputDevices = new ArrayList<InputDevice>();
    this.as.clear();
    this.mainText.giveOutput("Enter your pin");
    this.as.add(this.mainText);
    this.as.add(this.pinText);
    for(int i = 0; i < this.keypadButtonList.size(); i++) {
      ScreenButton keypadButton = this.keypadButtonList.get(i);
      inputDevices.add(keypadButton);
      this.as.add(keypadButton);
    }
    inputDevices.add(this.keypad);
    while(true) {
      while(true) {
        this.pinText.giveOutput(xes);
        buttonPressed = this.listenForInput(inputDevices);
        if(buttonPressed.equals("\n")) {
          break;
        }
        pinCode += buttonPressed;
        xes += "x";
      }
      if(client.checkPin(pinCode)) {
        break;
      }
      xes = "";
      pinCode = "";
      this.mainText.giveOutput("wrong pin please try again.");
      try{
        TimeUnit.SECONDS.sleep(2);
      }catch(InterruptedException e) {
        System.err.println(e.toString());
      }
    }
    return pinCode;
  }

  private int withdrawMenu(Client client, String pinCode) {
    ScreenButton amountButton;
    String amountText = "";
    int amount = 0;
    int balance;
    ArrayList<InputDevice> inputDevices = new ArrayList<InputDevice>();
    balance = client.getBalance(pinCode);
    this.as.clear();
    this.as.add(this.mainText);
    for(int i = 0; i < this.amountButtonList.size(); i++) {
      amountButton = this.amountButtonList.get(i);
      inputDevices.add(amountButton);
      this.as.add(amountButton);
    }
    while(true){
      this.mainText.giveOutput("Choose amount \u20AC" + balance);
      amountText = this.listenForInput(inputDevices);
      String[] splitString = amountText.split(" ");
      amount = Integer.parseInt(splitString[1]);
      amountText = "";
      if (client.withdraw(amount, pinCode)) {
        break;
      } else {
        this.mainText.giveOutput("amigo sin dinero");
        try{
          TimeUnit.SECONDS.sleep(2);
        }catch(InterruptedException e) {
          System.err.println("oops");
        }

      }
    }
    if(this.askForReceipt()) {
      this.printReceipt(balance, amount);
    }

    this.as.clear();
    this.mainText.giveOutput("withdrawing \u20AC " + amount );
    this.mainText2.giveOutput(" please wait...");
    this.as.add(this.mainText);
    this.as.add(this.mainText2);
    try{
      TimeUnit.SECONDS.sleep(2);
    }catch(InterruptedException e) {
      System.err.println("oops");
    }
    return amount;
  }

  private void printReceipt(int balance, int amount) {
    this.receiptPrinter.giveOutput("balance: "+ (balance+amount) + "\n" + "withdrawn: " + amount + "\n" + "new balance: " + balance);
  }

  private boolean askForReceipt() {
    ScreenButton confirmButton;
    String answer;
    ArrayList<InputDevice> inputDevices = new ArrayList<InputDevice>();
    this.as.clear();
    this.mainText.giveOutput("do you want a receipt?");
    this.as.add(this.mainText);
    for(int i = 0; i < this.confirmButtonList.size(); i++) {
      confirmButton = this.confirmButtonList.get(i);
      inputDevices.add(confirmButton);
      this.as.add(confirmButton);
    }
    answer = this.listenForInput(inputDevices);
    return answer.equals(this.yes);
  }

  private String listenForInput(ArrayList<InputDevice> inputDevices) {
    String input;
    int counter = 0;
    do {
      input = inputDevices.get(counter).getInput();

      if(++counter > (inputDevices.size()-1)) {
        counter = 0;
      }
    } while(input == null);
    return input;
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
