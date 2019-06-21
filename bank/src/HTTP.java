import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

class HTTP {

    private HttpURLConnection con;

    private String jsonString = "";
    private StringBuffer response;
    private String Iban;
    private String Name;
    private String Pin;
    private String Status;
    private String url;
    private String ibanFlag;
    private String withdrawalCheck;
    private String noocExists;
    private int Balance;

    private void getClient() throws Exception {
        URL obj = new URL(url);
        con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        String userAgent = "Mozilla/5.0";
        con.setRequestProperty("User-Agent", userAgent);
        System.out.println("\nSending 'GET' request to URL: " + url);
        System.out.println("Response code: " + con.getResponseCode());

        BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
        response = new StringBuffer();
        String line;

        while ((line = br.readLine()) != null) {
            response.append(line);
        }
        br.close();
    }

    boolean checkIban(String iban) { //check if iban exists
//        System.out.print(iban);
        iban = iban.replace("\n", "").replace("\r", "");
        url = "https://leutertrekkers.nl/api/info/index.php?IBAN=" + iban;
        try {
            getClient();
            if (con.getResponseCode() == 200) {
                setIban();
                return true;
            } else {
//                System.out.println("Failed to retrieve data iban resp");
                return false;
            }

        } catch (Exception e) {
//            System.out.println("Failed to get data iban gen");
        }
        return false;
    }

    void retrieveData(String iban, String pin) {
        iban = iban.replace("\n", "").replace("\r", "");
        url = "https://leutertrekkers.nl/api/info/index.php?IBAN=" + iban;
        url += "&PIN=" + pin;
        try {
            getClient();
            if (con.getResponseCode() == 200) {
                setData();
            } else {
//                System.out.println("Failed to retrieve data local resp");
            }

        } catch (Exception e) {
//            System.out.println("Failed to retrieve data local gen");
        }
    }

    void retrieveNooc(String iban, String pin) {
        iban = iban.replace("\n", "").replace("\r", "");
        url = "https://leutertrekkers.nl/api/info/index.php?IBAN=" + iban;
        url += "&PIN=" + pin;
//        System.out.println(url);
        try {
            setNooc();
            if (con.getResponseCode() == 200) {
                setData();
            } else {
//                System.out.println("Failed to retrieve data nooc resp");
            }

        } catch (Exception e) {
//            System.out.println("Failed to retrieve data nooc gen");
        }
    }

    boolean retrieveWithdrawal(String iban, String pin, int withdrawal) {
        iban = iban.replace("\n", "").replace("\r", "");
        url = "https://leutertrekkers.nl/api/info/index.php?IBAN=" + iban;
        url += "&PIN=" + pin + "&BALANCE=-" + withdrawal;
        try {
            setWithdrawal();
            getClient();
            if (con.getResponseCode() == 200) {
                return true;
            }
        } catch (Exception e) {
//            System.out.println("Failed to retrieve withdrawal data");
            return false;
        }
        return false;
    }

    private void setNooc() {
        jsonString = response.toString();
        JSONObject jsonObject = new JSONObject(jsonString);
        noocExists = jsonObject.getString("exists");
    }

    private void setWithdrawal() {
        jsonString = response.toString();
        JSONObject jsonObject = new JSONObject(jsonString);
        withdrawalCheck = jsonObject.getString("withdraw");
    }

    private void setData() { //Set all user data
        jsonString = response.toString();
        JSONObject jsonObject = new JSONObject(jsonString);
//        System.out.println(jsonString);
        Iban = jsonObject.getString("IBAN");
        Balance = Integer.parseInt(jsonObject.getString("balance"));
        Name = jsonObject.getString("name");
        Pin = jsonObject.getString("nuid");
        Status = jsonObject.getString("status");
    }

    private void setIban() { //Set iban
        String ibanString = response.toString();
//        System.out.println(ibanString);
        JSONObject jsonObject = new JSONObject(ibanString);
        ibanFlag = jsonObject.getString("exists");
        Status = jsonObject.getString("status");
    }

    void resetData() {
        Iban = "";
        Balance = 0;
        Name = "";
        Pin = "";
        Status = "";
        ibanFlag = "";
    }

    void block(String IBAN, String blockOption) {
        url = "https://leutertrekkers.nl/api/info/index.php?IBAN=" + IBAN + "&PIN=1234567890" + "&status=" + blockOption;
        try {
            getClient();
        } catch (Exception e) {
//            System.out.print("Failed to edit data.");
        }
    }

    void withdraw(int withdrawal, String iban, String pin) {
        Balance -= withdrawal;
//        System.out.println(Balance);
        url = "https://leutertrekkers.nl/api/info/index.php?IBAN=" + iban + "&PIN=" + pin + "&BALANCE=-" + withdrawal;

        try {
            getClient();
        } catch (Exception e) {
//            System.out.println("Failed to change data");
        }
    }

    String getIban() {
        return Iban;
    }

    int getBalance() {
        return Balance;
    }

    String getName() {
        return Name;
    }

    String getPin() {
        return Pin;
    }

    String getStatus() {
        return Status;
    }

    String exists() {
        return noocExists;
    }

    String getwithdraw() {
        return withdrawalCheck;
    }
}
