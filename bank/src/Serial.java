import com.fazecast.jSerialComm.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

public class Serial {
	private String key = "";
	private String RFID = "";
	private String receivedData;

	private static SerialPort serialPort;
	private SerialPort[] portList;
	private Scanner scanner;

	Serial() {
		portList = SerialPort.getCommPorts();
		scanner = new Scanner(System.in);
	}

	void openPort() {
		int i = 1;
		int chosenPort;
		for (SerialPort port : portList) {
			System.out.println(i++ + ": " + port.getSystemPortName());
		}

		do {
			while (!scanner.hasNextInt()) {
				i = 1;
				System.out.println("This port doesn't exist.");
				for (SerialPort port : portList) {
					System.out.println(i++ + ": " + port.getSystemPortName());
				}
				scanner.next();
			}
			chosenPort = scanner.nextInt();
		} while (chosenPort <= 0);

		if (chosenPort < i) {
			serialPort = portList[chosenPort - 1];
			serialPort.setBaudRate(9600);
			if (serialPort.openPort()) {
				System.out.println("Port opened successfully.");
			} else {
				System.out.println("port opening failed.");
			}
		} else {
			System.out.println("This port doesn't exist.");
			openPort();
		}
	}

	void listenSerial() {
		//create a listener and start listening
		serialPort.addDataListener(new SerialPortDataListener() {
			@Override
			public int getListeningEvents() {
				return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
			}

			@Override
			public void serialEvent(SerialPortEvent event) {
				if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE)
					return; //wait until we receive data

				byte[] newData = new byte[serialPort.bytesAvailable()]; //receive incoming bytes
				serialPort.readBytes(newData, newData.length); //read incoming bytes
				String serialData = new String(newData); //convert bytes to string

				//print string received from the Arduino
//				System.out.println(serialData);
				receivedData = serialData;
//				System.out.println(receivedData);
				setValue(receivedData);
			}
		});
	}

	void printReceipt(String i) {
		try {
			i = "1" + i;
			System.out.println(i);
			serialPort.getOutputStream().write(i.getBytes());
			serialPort.getOutputStream().flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void sendMotorData(byte[] i) {
		try {

			serialPort.getOutputStream().write(i);
			serialPort.getOutputStream().flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setValue(String data) {
		if (data.length() > 1) {
			this.RFID = data;
//			System.out.println(this.RFID);
		} else if (data.length() == 1) {
			this.key = data;
//			System.out.println(key);
		}
	}

	String getKey() {
		return key;
	}

	String getRFID() {
		return RFID;
	}

	void resetRFID() {
	    RFID = "";
    }

    void resetKey() {
		key = "";
	}
}
