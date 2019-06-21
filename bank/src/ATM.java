import java.awt.Frame;
import java.awt.Color;
import java.awt.event.*;
import java.awt.Point;
import java.util.ArrayList;
import java.awt.Button;
import java.lang.Integer;
import java.util.concurrent.TimeUnit;
import java.lang.InterruptedException;
import java.util.Date;

public class ATM {
  // private Bank bank;
  private ATMScreen as;
  private String yes;   // used as the yes string so i can change it to without braking the program.
  private Serial arduino;
  Menu ibanMenu;
  Menu pinMenu;
  Menu mainMenu;
  Menu withdrawMenu;
  Menu askForReceiptMenu;
  Menu endMenu;
  Menu noteChoiceMenu;
  Menu otherAmountMenu;
  Menu prev;
  String state;
  ScreenButton balanceButton;
  String balanceButtonText;
  String otherAmountText;
  String pinCode;
  int balance;
  int amount;
  String backButton;
  String cancelButton;
  Date date;
  String iBan;
  boolean nooc;
  String withdrawButton;
  HTTP http;
  private int[] noteChoices;
  private int[] noteChoiceAmount;
  private int[] notesAvailable;

  ATM() {
    this.as = new ATMScreen();
    this.arduino = new Serial();
    this.http = new HTTP();
    this.date = new Date();
    Frame f = new Frame("My ATM");

    f.setBounds(600, 200, 1080, 720);
    f.setBackground(Color.getHSBColor(207, 215, 234));
    f.addWindowListener(new MyWindowAdapter(f));
    f.add(as);
    f.setVisible(true);
    // text set so that when i check the buttons the valeus will be the same.
    this.iBan = "";
    this.yes = "yes";
    this.state = "reset";
    this.backButton = "back";
    this.cancelButton = "cancel";
    this.withdrawButton = "withdraw";
    this.otherAmountText = "other amount";
    this.balanceButtonText = "check balance";
    this.balanceButton = new ScreenButton(this.balanceButtonText, new Point(200, 250));
    ArrayList<DisplayText> tempTexts = new ArrayList<DisplayText>();
    tempTexts.add(new DisplayText("mainText", new Point(10, 10)));
    this.ibanMenu = new Menu(
    "ibanMenu",
    tempTexts,
    arduino
    );

    tempTexts = new ArrayList<DisplayText>();
    ArrayList<ScreenButton> tempButtons = this.genButtons(40, 60, 3, 3, 40, 40, "0");
    tempButtons.add(new ScreenButton(this.backButton, new Point(10, 250)));
    tempButtons.add(new ScreenButton(this.cancelButton, new Point(110, 250)));
    tempTexts.add(new DisplayText("mainText", new Point(10, 10)));
    tempTexts.add(new DisplayText("pinText", new Point(200, 50)));
    this.pinMenu = new Menu("pinMenu", tempTexts, tempButtons, arduino);

    tempTexts = new ArrayList<DisplayText>();
    tempButtons = new ArrayList<ScreenButton>();
    tempTexts.add(new DisplayText("mainText", new Point(10, 10)));
    tempButtons.add(new ScreenButton(this.withdrawButton, new Point(10, 250)));
    tempButtons.add(new ScreenButton("20 rub", new Point(10, 350)));
    tempButtons.add(new ScreenButton(this.backButton, new Point(10, 450)));
    tempButtons.add(new ScreenButton(this.cancelButton, new Point(110, 450)));
    this.mainMenu = new Menu("mainMenu", tempTexts, tempButtons, arduino);
    this.mainMenu.addInputDevice(this.balanceButton);

    tempTexts = new ArrayList<DisplayText>();
    tempButtons = this.genButtons(40, 60, 1, 5, 80, 80, null, new String[]{"\u20AC 10", "\u20AC 20", "\u20AC 50", "\u20AC 100", this.otherAmountText});
    tempButtons.add(new ScreenButton(this.backButton, new Point(10, 250)));
    tempButtons.add(new ScreenButton(this.cancelButton, new Point(110, 250)));
    tempTexts.add(new DisplayText("mainText", new Point(10, 10)));
    this.withdrawMenu = new Menu("withdrawMenu", tempTexts, tempButtons, arduino);

    tempTexts = new ArrayList<DisplayText>();
    tempButtons = this.genButtons(40, 150, 1, 2, 200, 200, null, new String[]{this.yes,"no"});
    tempTexts.add(new DisplayText("mainText", new Point(10, 10)));
    this.askForReceiptMenu = new Menu("askForReceiptMenu", tempTexts, tempButtons, arduino);

    tempTexts = new ArrayList<DisplayText>();
    tempTexts.add(new DisplayText("mainText", new Point(10, 10)));
    this.endMenu = new Menu("endMenu", tempTexts, arduino);

    tempTexts = new ArrayList<DisplayText>();
    tempTexts.add(new DisplayText("mainText", new Point(10, 10)));
    this.noteChoiceMenu = new Menu("noteChoiceMenu", tempTexts, arduino);

    tempButtons = new ArrayList<ScreenButton>();
    tempTexts = new ArrayList<DisplayText>();
    tempButtons.add(new ScreenButton(this.backButton, new Point(10, 250)));
    tempButtons.add(new ScreenButton(this.cancelButton, new Point(110, 250)));
    tempTexts.add(new DisplayText("mainText", new Point(10, 10)));
    tempTexts.add(new DisplayText("valueText", new Point(10, 50)));
    this.otherAmountMenu = new Menu("otherAmountMenu", tempTexts, tempButtons,  arduino);

    this.arduino.openPort();


    this.noteChoices = new int[]{10, 20, 50};
    this.noteChoiceAmount = new int[this.noteChoices.length];
    this.notesAvailable = new int[] {10, 10, 10};

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
        case "mainMenu":
            this.mainMenu.open(this.as, this.prev);
            this.prev = this.loadMainMenu();
            break;
        case "withdrawMenu":
          this.withdrawMenu.open(this.as, this.prev);
          this.prev = this.withdraw();
          break;
        case "otherAmountMenu":
          this.otherAmountMenu.open(this.as, this.prev);
          this.prev = this.otherAmount();
        case "noteChoiceMenu":
            this.noteChoiceMenu.open(this.as, this.prev);
            this.prev = this.noteChoice();
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
    arduino.resetRFID();
    this.ibanMenu.editText("Insert your card.", 0);
    while(true) {
      arduino.listenSerial();
      this.iBan = this.arduino.getRFID();
      if(!this.iBan.isEmpty()) {
        if(http.checkIban(this.iBan)) {
          if(http.getStatus().equals("Active")) {
            this.nooc = false;
            break;
          } else {
            this.ibanMenu.editText("Your account is blocked.", 0);
            try{
              TimeUnit.SECONDS.sleep(2);
            }catch(InterruptedException e) {
              System.err.println(e.getMessage());
            }
            this.ibanMenu.editText("Please remove your card.", 0);
            try{
              TimeUnit.SECONDS.sleep(2);
            }catch(InterruptedException e) {
              System.err.println(e.getMessage());
            }
            throw new CancelTransactionException("blocked");
          }
        } else {
            this.nooc = true;
            break;
        }
      }
    }
    this.state = "pinMenu";
    return this.ibanMenu;
  }
  //checks pin and if no account is found will return the user to the homescreen.
  private boolean checkPin() {
    try {
      http.retrieveData(iBan, pinCode);
      if (!nooc) {
        http.retrieveData(this.iBan, this.pinCode);
        return http.getPin().equals(this.pinCode);
      }
      http.retrieveNooc(this.iBan, this.pinCode);

      return http.exists().equals("True");
    } catch (Exception e) {
      throw new CancelTransactionException("user is blocked or doesnt exist");
    }
  }

  //
  private Menu authenticate() {
    int trys = 3;
    String xes = "";
    String buttonPressed;
    this.pinCode = "";
    arduino.resetKey();
    this.pinMenu.editText("Enter your pin", 0);
    while(true) {
      while(this.pinCode.length() < 4) {

        buttonPressed = this.pinMenu.listenForInput();

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
          this.pinMenu.editText(xes, 1);
      }
      xes = "";
      this.pinMenu.editText(xes, 1);
      try {
        if(this.checkPin()&& trys > 0) {
          trys = 0;
          break;
        }
      }catch(CancelTransactionException ce) {
        this.pinMenu.editText("user is blocked or doesnt exist", 0);
        try{
          TimeUnit.SECONDS.sleep(2);
        }catch(InterruptedException e) {
          System.err.println(e.getMessage());
        }
        throw ce;
      }


      this.pinCode = "";
      if(trys <= 0){
        this.pinMenu.editText("card is is blocked.", 0);
      } else {
        this.pinMenu.editText("wrong pin " + trys + " try's left", 0);
        trys--;
      }
      try{
        TimeUnit.SECONDS.sleep(2);
      }catch(InterruptedException e) {
        System.err.println(e.getMessage());
      }
    }

    this.state = "mainMenu";
    return this.pinMenu;
  }

  private Menu loadMainMenu() {
    String name = http.getName();
    String welcomeText = "welcome ";
    if(name != null) welcomeText += name;
    this.mainMenu.editText(welcomeText  ,0);
    try{
      TimeUnit.SECONDS.sleep(2);
    }catch(InterruptedException e) {
      System.err.println(e.getMessage());
    }
    this.mainMenu.editText("What would you like to do?",0);
    if(!nooc) {
      this.as.add(this.balanceButton);
    }
    String button;
    while(true) {


      button = this.mainMenu.listenForInput();
      if(button.equals(this.backButton)) {
        Menu prevMenu = this.mainMenu.getPrev();
        this.state = prevMenu.getName();
        return prevMenu.getPrev();
      }

      if(button.equals("20 rub")) {
        http.withdraw(20, http.getIban(), http.getPin());
        this.state = "endMenu";
        return this.mainMenu;
      }

      if(button.equals(this.cancelButton)) {
        throw new CancelTransactionException("cancel");
      }
      if(button.equals(this.withdrawButton)) {
        this.state = "withdrawMenu";
        return this.mainMenu;
      }
      if(button.equals(this.balanceButtonText)) {
        this.mainMenu.editText("balance: " + http.getBalance(),0);
        try{
          TimeUnit.SECONDS.sleep(2);
        }catch(InterruptedException e) {
          System.err.println(e.getMessage());
        }
        this.mainMenu.editText("What would you like to do?",0);
      }
    }
  }

  private Menu withdraw() {
    String button;
    String amountText = "";
    while(true) {
      this.withdrawMenu.editText("Choose amount", 0);
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
        if(button.equals(this.otherAmountText)) {
          this.state = "otherAmountMenu";
          return this.withdrawMenu;
        }
        String[] splitString = button.split(" ");
        if(splitString.length >= 2) {
          this.amount = Integer.parseInt(splitString[1]);
          break;
        } else {
          amountText += button;
        }
      }
      button = "";
      amountText = "";
      if(!nooc) {
        if (this.amount <= http.getBalance()) {
          http.withdraw(this.amount, http.getIban(), http.getPin());
          break;
        } else {
          this.withdrawMenu.editText("amigo sin dinero", 0);
          this.amount = 0;
          try{
            TimeUnit.SECONDS.sleep(2);
          }catch(InterruptedException e) {
            System.err.println(e.getMessage());
          }
        }
      } else {
        if(http.retrieveWithdrawal(http.getIban(), http.getPin(), this.amount)) {
          if(http.getwithdraw().equals("True")) {

          } else {
            this.withdrawMenu.editText("amigo sin dinero", 0);
          }
          try{
            TimeUnit.SECONDS.sleep(2);
          }catch(InterruptedException e) {
            System.err.println(e.getMessage());
          }
        } else {
          throw new CancelTransactionException("connection lost");
        }
      }
    }
    this.state = "noteChoiceMenu";
    return this.withdrawMenu;
  }
  private Menu otherAmount() {
    this.otherAmountMenu.editText("What amount would you like? (# = done)", 0);
    String value = "";
    String amountText = "";
    int amount;
    while(true) {
      value = this.otherAmountMenu.listenForInput();
      if(value.equals("#")) {
        amount = Integer.parseInt(amountText);
        if((amount % 10) == 0) {
          this.amount = amount;
          if(!nooc) {
            if (this.amount <= http.getBalance()) {
              http.withdraw(this.amount, http.getIban(), http.getPin());
            } else {
              this.withdrawMenu.editText("amigo sin dinero", 0);
              this.amount = 0;
              try{
                TimeUnit.SECONDS.sleep(2);
              }catch(InterruptedException e) {
                System.err.println(e.getMessage());
              }
            }
          } else {
            if(http.retrieveWithdrawal(http.getIban(), http.getPin(), this.amount)) {
              if(http.getwithdraw().equals("True")) {

              } else {
                this.withdrawMenu.editText("amigo sin dinero", 0);
              }
              try{
                TimeUnit.SECONDS.sleep(2);
              }catch(InterruptedException e) {
                System.err.println(e.getMessage());
              }
            } else {
              throw new CancelTransactionException("connection lost");
            }
          }
          this.state = "noteChoice";
          return this.otherAmountMenu;
        } else {
          this.otherAmountMenu.editText("cant pin this amount", 1);
          try{
            TimeUnit.SECONDS.sleep(2);
          }catch(InterruptedException e) {
            System.err.println(e.getMessage());
          }
          amountText = "";
          value = "";
        }
      }
      amountText += value;
      this.otherAmountMenu.editText("Value: "+ amountText, 1);
    }
  }
  private Menu noteChoice() {
      ScreenButton tempScreenButton;
      int amount;
      int[] noteChoiceAmount = new int[this.noteChoices.length];
      int[] notesAvailable = this.notesAvailable.clone();
      String[] text = new String[this.noteChoices.length];
      ArrayList<InputDevice> inputDevices = new ArrayList<InputDevice>();
      int startI = 0;
      this.noteChoiceMenu.editText("what notes would you like?", 0);
      for(int e = 0; e < noteChoiceAmount.length; e++) {
        this.noteChoiceAmount[e] = 0;
      }
      for(int t = 0; t < this.noteChoices.length; t++) {
        notesAvailable = this.notesAvailable.clone();
        for(int g = 0; g < noteChoiceAmount.length; g++) {
          noteChoiceAmount[g] = 0;
        }



        amount = this.amount;
        for(int i = startI; i < this.noteChoices.length; i++) {
          for(int x = 0; x < 5; x++) {
            if(notesAvailable[i] <= 0) {
              break;
            }
            System.out.println(notesAvailable[i]);
            amount = amount - this.noteChoices[i];
            noteChoiceAmount[i]++;
            notesAvailable[i]--;
            if(amount == 0) {
              break;
            } else if(amount < 0) {
              noteChoiceAmount[i]--;
              notesAvailable[i]++;
              amount += this.noteChoices[i];
              break;
            }
          }
        }
        for(int y = this.noteChoices.length-1; y >= 0 ; y--) {
          while(true) {
            if(notesAvailable[y] <= 0) {
              break;
            }
            System.out.println(notesAvailable[y]);
            amount = amount - this.noteChoices[y];
            noteChoiceAmount[y]++;
            notesAvailable[y]--;
            if(amount == 0) {
              break;
            } else if(amount < 0) {
              noteChoiceAmount[y]--;
              notesAvailable[y]++;
              amount += this.noteChoices[y];
              break;
            }
          }
          System.out.println(notesAvailable[y]);
        }

        text[t] = "";
        for(int a = 0; a < noteChoiceAmount.length; a++) {

          if(noteChoiceAmount[a] > 0) text[t] += noteChoiceAmount[a] + "x " + this.noteChoices[a] + " \u20AC ";
        }

        startI++;
      }
      String prevText = "woepwoep";
      for(int i = 0; i < text.length; i++) {
        if(!text[i].contains(prevText)) {
          tempScreenButton = new ScreenButton(text[i], new Point(400, 200 + i * 200));
          inputDevices.add(tempScreenButton);
          as.add(tempScreenButton);
        }
        prevText = text[i];
      }
      String temp;
      while(true) {
        for (InputDevice inputDevice : inputDevices) {
          temp = inputDevice.getInput();
          if (temp != null) {

            temp = temp.replaceAll("\\D+"," ");
            String[] values = temp.split(" ");
            for(int i = 1; i < values.length; i+= 2) {
              for(int x = 0; x < this.noteChoices.length; x++) {
                if(this.noteChoices[x] == Integer.parseInt(values[i])) {
                  this.noteChoiceAmount[x] = Integer.parseInt(values[i-1]);
                  this.notesAvailable[x] -= this.noteChoiceAmount[x];
                }
              }
            }
            this.state = "askForReceiptMenu";
            return this.noteChoiceMenu;
          }
        }
      }

  }

  private void sendNotes() {
      byte[] sendThis = new byte[this.noteChoiceAmount.length + 1];
      for(int i = 1; i < this.noteChoiceAmount.length; i++) {
          sendThis[i] = new Integer(this.noteChoiceAmount[i - 1] + 16 * (i)).byteValue();
          System.out.println(sendThis[i]);
          System.out.println(this.noteChoiceAmount[i]);
      }
      sendThis[0] = 48;
      arduino.sendMotorData(sendThis);
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

    this.pinCode = "";
    this.balance = 0;
    this.amount = 0;
  }

  private String replaceLastFour(String s) {
      int length = s.length();
      return "**********" + s.substring(length - 6, length);
  }

  private void printReceipt(int balance, int amount) {
    String receipt = date.toString() + "\n" + replaceLastFour(http.getIban()) +  "\nNow dispensing: " + this.amount + "\n";
    for(int i = 0; i < this.noteChoiceAmount.length; i++) {
      receipt += this.noteChoiceAmount[i]+"x " + this.noteChoices[i] + "RUB\n";
    }
    arduino.printReceipt(receipt);
    System.out.println(receipt);
  }

  /*
  ** this method generates a grid of buttons in a list so that i dont have to go through them one by one.
  ** param(int) X position on the screen where the grid should start.
  ** param(int) Y position on the screen where the grid should start.
  ** param(int) amount of buttons horizontal.
  ** param(int) amount of buttons vertical.
  ** param(int) spaces between buttons horizontal.
  ** param(int) spaces between buttons vertical.
  ** param(String) the name of the last button leave null if you do not want a last button.
  */
  private ArrayList<ScreenButton> genButtons(int startX, int startY ,int gridX, int gridY, int gapX, int gapY, String lastButton) {
    int length = gridX*gridY;
    String[] names = new String[length];
    for(int i = 0; i < length; i++) {
      names[i] = Integer.toString(i+1);
    }
    return this.genButtons(startX, startY ,gridX, gridY, gapX, gapY, lastButton, names);
  }

    /*
    ** this method generates a grid of buttons in a list and gives them a name so that i dont have to go through them one by one.
    ** param(int) X position on the screen where the grid should start.
    ** param(int) Y position on the screen where the grid should start.
    ** param(int) amount of buttons horizontal.
    ** param(int) amount of buttons vertical.
    ** param(int) spaces between buttons horizontal.
    ** param(int) spaces between buttons vertical.
    ** param(String) the name of the last button leave null if you do not want a last button.
    ** param(String[]) a list of names (this needs to be equal to the grid amount otherwise it wont work).
    */
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
