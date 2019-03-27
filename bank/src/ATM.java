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
  ReceiptPrinter receiptPrinter;
  Menu ibanMenu;
  Menu pinMenu;
  Menu mainMenu;
  Menu withdrawMenu;
  Menu askForReceiptMenu;
  Menu endMenu;
  Menu prev;
  String state;
  Client client;
  String pinCode;
  int balance;
  int amount;
  String backButton;
  String cancelButton;
  Keypad keypad;

  ATM(Bank bank) {
    this.bank = bank;
    this.as = new ATMScreen();
    Frame f = new Frame("My ATM");

    f.setBounds(200, 200, 400, 300);
    f.setBackground(Color.BLUE);
    f.addWindowListener(new MyWindowAdapter(f));
    f.add(as);
    f.setVisible(true);
    // text
    this.yes = "yes";
    this.state = "reset";
    this.backButton = "back";
    this.cancelButton = "cancel";
    this.accountReader = new CardReader("cardReader");
    this.receiptPrinter = new ReceiptPrinter("receiptPrinter");
    ArrayList<DisplayText> tempTexts = new ArrayList<DisplayText>();
    tempTexts.add(new DisplayText("mainText", new Point(10, 10)));
    this.ibanMenu = new Menu(
    "ibanMenu",
    tempTexts
    );

    tempTexts = new ArrayList<DisplayText>();
    ArrayList<ScreenButton> tempButtons = this.genButtons(40, 60, 3, 3, 40, 40, "0");
    tempButtons.add(new ScreenButton(this.backButton, new Point(10, 250)));
    tempButtons.add(new ScreenButton(this.cancelButton, new Point(110, 250)));
    tempTexts.add(new DisplayText("mainText", new Point(10, 10)));
    tempTexts.add(new DisplayText("pinText", new Point(200, 50)));
    this.pinMenu = new Menu("pinMenu", tempTexts, tempButtons);
    this.pinMenu.addInputDevice(new Keypad("keypad"));

    tempTexts = new ArrayList<DisplayText>();
    tempButtons = this.genButtons(40, 60, 1, 4, 80, 80, null, new String[]{"\u20AC 10", "\u20AC 20", "\u20AC 50", "\u20AC 100"});
    tempButtons.add(new ScreenButton(this.backButton, new Point(10, 250)));
    tempButtons.add(new ScreenButton(this.cancelButton, new Point(110, 250)));
    tempTexts.add(new DisplayText("mainText", new Point(10, 10)));
    this.withdrawMenu = new Menu("withdrawMenu", tempTexts, tempButtons);
    this.withdrawMenu.addInputDevice(new Keypad("keypad"));

    tempTexts = new ArrayList<DisplayText>();
    tempButtons = this.genButtons(40, 150, 1, 2, 200, 200, null, new String[]{this.yes,"no"});
    tempTexts.add(new DisplayText("mainText", new Point(10, 10)));
    this.askForReceiptMenu = new Menu("askForReceiptMenu", tempTexts, tempButtons);

    tempTexts = new ArrayList<DisplayText>();
    tempTexts.add(new DisplayText("mainText", new Point(10, 10)));
    this.endMenu = new Menu("endMenu", tempTexts);

    while(true) {
      this.doTransaction();
    }
  }

  private void doTransaction() {
    try {
      switch(this.state) {
        case "ibanMenu":
          this.ibanMenu.open(this.as);
          this.prev = this.getClient();
          break;
        case "pinMenu":
          this.pinMenu.open(this.as, this.prev);
          this.prev = this.authenticate();
          break;
        case "withdrawMenu":
          this.withdrawMenu.open(this.as, this.prev);
          this.prev = this.withdraw();
          break;
        case "askForReceiptMenu":
          this.askForReceiptMenu.open(this.as, this.prev);
          this.prev = this.askForReceipt();
          break;
        case "endMenu":
          this.endMenu.open(this.as, this.prev);
          this.end();
        default:
          this.reset();
          break;
      }
    } catch(CancelTransactionException e) {
      this.state = "reset";
    }
  }

  private Menu getClient() {
    String IBan;
    this.ibanMenu.editText("Insert your card.", 0);
    do {
      IBan = this.accountReader.getInput();
      this.client = this.bank.get(IBan);
      if(IBan != null && this.client == null) {
        this.ibanMenu.editText("no card found, please try again.", 0);
      }
    } while(this.client == null);
    this.state = "pinMenu";
    return this.ibanMenu;
  }

  private Menu authenticate() {
    String xes = "";
    String buttonPressed;
    this.pinMenu.editText("Enter your pin", 0);
    while(true) {
      while(true) {
        this.pinMenu.editText(xes, 1);
        buttonPressed = this.pinMenu.listenForInput();
        if(buttonPressed.equals("\n")) {
          break;
        }
        if(buttonPressed.equals(this.backButton)) {
          Menu prevMenu = this.pinMenu.getPrev();
          this.state = prevMenu.getName();
          return prevMenu.getPrev();
        }

        if(buttonPressed.equals(this.cancelButton)) {
          throw new CancelTransactionException("cancel");
        }
        this.pinCode += buttonPressed;
        xes += "x";
      }
      xes = "";
      if(this.client.checkPin(this.pinCode)) {
        break;
      }
      this.pinCode = "";
      if(this.client.isBlocked()){
        this.pinMenu.editText("card is is blocked.", 0);
      } else {
        this.pinMenu.editText("wrong pin " + this.client.getTrys() + " try's left", 0);
      }
      try{
        TimeUnit.SECONDS.sleep(2);
      }catch(InterruptedException e) {
        System.err.println(e.getMessage());
      }
    }
    this.balance = this.client.getBalance(this.pinCode);
    this.state = "withdrawMenu";
    return this.pinMenu;
  }

  private Menu withdraw() {
    String button;
    String amountText = "";
    while(true) {
      this.withdrawMenu.editText("Choose amount \u20AC" + this.balance, 0);
      while(true) {


        button = this.withdrawMenu.listenForInput();
        if(button.equals(this.backButton)) {
          Menu prevMenu = this.withdrawMenu.getPrev();
          this.state = prevMenu.getName();
          return prevMenu.getPrev();
        }

        if(button.equals(this.cancelButton)) {
          throw new CancelTransactionException("cancel");
        }
        if(button.equals("\n")) {
          this.amount = Integer.parseInt(amountText);
          break;
        }
        String[] splitString = button.split(" ");
        if(splitString.length >= 2) {
          this.amount = Integer.parseInt(splitString[1]);
          break;
        } else {
          amountText += button;
        }

        button = "";
      }
      if (this.client.withdraw(this.amount, pinCode)) {
        break;
      } else {
        this.withdrawMenu.editText("amigo sin dinero", 0);
        try{
          TimeUnit.SECONDS.sleep(2);
        }catch(InterruptedException e) {
          System.err.println(e.getMessage());
        }
      }
    }
    this.state = "askForReceiptMenu";
    return this.withdrawMenu;
  }

  private Menu askForReceipt() {
    String answer;
    this.askForReceiptMenu.editText("do you want a receipt?", 0);
    answer = this.askForReceiptMenu.listenForInput();
    if(answer.equals(this.yes)) {
      this.printReceipt(this.balance, this.amount);
    }
    this.state = "endMenu";
    return this.askForReceiptMenu;
  }

  private void end() {
    this.endMenu.editText("withdrawing money please wait...", 0);
    try{
      TimeUnit.SECONDS.sleep(2);
    } catch(InterruptedException e) {
      System.err.println(e.getMessage());
    }
    this.state = "reset";
  }

  private void reset() {
    this.state = "ibanMenu";
    this.client = null;
    this.pinCode = "";
    this.balance = 0;
    this.amount = 0;
  }


  private void printReceipt(int balance, int amount) {
    this.receiptPrinter.giveOutput("balance: "+ (balance) + "\n" + "withdrawn: " + amount + "\n" + "new balance: " + (balance-amount));
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
