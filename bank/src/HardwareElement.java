public abstract class HardwareElement extends ATMElement {
  boolean power;
  HardwareElement(String name) {
    super(name);
    this.power = false;
  }
  public void powerOn() {
    this.power = true;
  }
  public void powerOff() {
    this.power = false;
  }
}
