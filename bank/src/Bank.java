import java.util.Map;
import java.util.HashMap;

public class Bank {
  Map<String, Client> accounts;

  Bank() {
    this.accounts = new HashMap<String, Client>();
    this.accounts.put("NL01BANK0123456789", new Client("raymond", "0123", 0));
    this.accounts.put("NL01BANK0123456790", new Client("saymond", "0124", 0));
  }
  public Client get(String account) {
    Client response = this.accounts.getOrDefault(account, null);
    return response;
  }
}
