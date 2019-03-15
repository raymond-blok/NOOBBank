public class ReceiptPrinter extends HardwareElement implements OutputDevice {
  ReceiptPrinter(String name) {
    super(name);
  }
  public void giveOutput(String output) {
    System.out.println(output);
  }
}
