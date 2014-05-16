package nl.tudelft.rxcourse.oxypulse;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortException;
import rx.Observable;
import rx.Subscriber;

public class Main {

	public static SerialPort serialPort;
	public static String PORT = "/dev/tty.SLAB_USBtoUART";

	public static void main(String[] args) {
		try {
			serialPort = openSerialPort(PORT);

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
						serialPortEvent -> subscriber.onNext(serialPortEvent));
				} catch (SerialPortException e) {
						subscriber.onError(e);
				}
			}
		);
	}
	
	private static SerialPort openSerialPort(String port)
			throws SerialPortException {
		SerialPort serialPort = new SerialPort(port);
		serialPort.openPort();
		// Communication settings for SerialPort
		serialPort.setParams(SerialPort.BAUDRATE_19200, SerialPort.DATABITS_8,
				SerialPort.STOPBITS_1, SerialPort.PARITY_ODD);
		// Specify the types of events that we want to track.
		serialPort.setEventsMask(SerialPort.MASK_RXCHAR);
		// Clear buffer
		serialPort.readBytes();
		
		return serialPort;
	}
}