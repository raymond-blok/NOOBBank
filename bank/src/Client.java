public class Client {
  private String name;
  private String pin;
  private int balance;

  Client(String name, String pin, int balance) {
    this.name = name;
    this.pin = pin;
    this.balance = balance;
  }
  public String getName() {
    return this.name;
  }
  public boolean checkPin(String pin) {
    //returns true if they are the same.
    return this.pin.equals(pin);
  }
  public int getBalance(String pin) {
      if (this.checkPin(pin)) {
        return this.balance;
      }
      return Integer.MIN_VALUE;
  }

  public void deposit(int balance) {
    // check if the balance is less than 0 so no negative money can be stored.
    if(balance > 0) {
      this.balance = this.balance + balance;
    }
  }

  public boolean withdraw(int balance, String pin) {
    // checks if the number is not negative so you cant add money.
    // also checks if the balance withrawn is not more than the balance in the account (so you cant have negative balance).
    // also checks if the pin is the same.
    if(balance > 0 && this.balance >= balance && this.checkPin(pin)) {
      this.balance = this.balance - balance;
      return true;
    }
    return false;
  }

}
