package nl.tudelft.rxcourse.oxypulse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortException;
import rx.Observable;
import rx.Subscriber;

public class Main {

	public static SerialPort serialPort;
	
	private static final String SETTINGS = "src/main/resources/serialport.properties";

	public static void main(String[] args) {
		try {
			serialPort = openSerialPort();
			
			/**
			 * Every time an event is received, buffer 5 items and then skip 5 items.
			 * Then fill a package with 5 bytes.
			 */
			eventsFrom(serialPort).buffer(5, 5).map(event -> {
				return new Package(serialPort);
			}).doOnEach(System.out::println).subscribe();

		} catch (SerialPortException e) {
			System.err.println(e);
		}
	}

	/**
	 * An Observable for SerialPortEvents.
	 * 
	 * An onNext is called every time an SerialPortEvent is fired.
	 * 
	 * @param port
	 * @return
	 */
	public static Observable<SerialPortEvent> eventsFrom(SerialPort port) {
		return Observable.create(
			(Subscriber<? super SerialPortEvent> subscriber) -> {
				try {
					port.addEventListener(
						serialPortEvent -> subscriber.onNext(serialPortEvent)
					);
				} catch (SerialPortException e) {
						subscriber.onError(e);
				}
			}
		);
	}
	
	private static SerialPort openSerialPort()
			throws SerialPortException {
		
		Properties p = openProperties();
		
		String portName = p.getProperty("port.name");
		int baudRate = Integer.valueOf(p.getProperty("port.baudrate"));
		int dataBits = Integer.valueOf(p.getProperty("port.databits"));
		int stopBits = Integer.valueOf(p.getProperty("port.stopbits"));
		int parity = Integer.valueOf(p.getProperty("port.parity"));
		
		SerialPort serialPort = new SerialPort(portName);
		serialPort.openPort();
		
		// Communication settings for SerialPort
		serialPort.setParams(baudRate, dataBits, stopBits, parity);
		// Specify the types of events that we want to track.
		serialPort.setEventsMask(SerialPort.MASK_RXCHAR);
		// Clear buffer
		serialPort.readBytes();
		
		return serialPort;
	}
	
	private static Properties openProperties() {
		Properties props = new Properties();
		try {
			props.load(new FileInputStream(new File(SETTINGS)));
		} catch (IOException e) {
			System.err.println("Error reading properties from file: "+SETTINGS);
		}
		return props;
	}
}