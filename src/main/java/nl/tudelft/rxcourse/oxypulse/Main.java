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

			//eventsFrom(serialPort).buffer(5, 5).doOnEach(System.out::println).subscribe();
			
			eventsFrom(serialPort)	.buffer(5, 5)
									.map(event -> { 
										try {
											return new Package(serialPort); 
										} catch (SerialPortException e) {
											return null;
										}
									})
									.doOnEach(System.out::println)
									.subscribe();
			
		} catch (SerialPortException ex) {
			System.out.println(ex);
		}
	}
	
	/**
	 * An Observable for SerialPortEvents. 
	 * 
	 * An onNext is called every time an event is fired.
	 * 
	 * @param port
	 * @return
	 */
	public static Observable<SerialPortEvent> eventsFrom(SerialPort port) {
		return Observable.create(
			(Subscriber<? super SerialPortEvent> subscriber) -> {
				try {
					port.addEventListener(serialPortEvent -> subscriber.onNext(serialPortEvent));
				} catch (SerialPortException e) {
					subscriber.onError(e);
				}
			}
		);
	}

	/**
	 * An Observable
	 * @param port
	 * @param numBytes
	 * @return
	 */
	public static Observable<byte[]> bytesFrom(SerialPort port, int numBytes) {
		return Observable.create(
			(Subscriber<? super byte[]> subscriber) -> {
				try {
					port.readBytes(); // clear the buffer
					subscriber.onNext(port.readBytes(numBytes));
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
		serialPort.setParams(SerialPort.BAUDRATE_19200, SerialPort.DATABITS_8,
				SerialPort.STOPBITS_1, SerialPort.PARITY_ODD);
		// Specify the types of events that we want to track.
		int mask = SerialPort.MASK_RXCHAR + SerialPort.MASK_CTS
				+ SerialPort.MASK_DSR + SerialPort.MASK_ERR;
		// Set the prepared mask
		serialPort.setEventsMask(mask);
		// Clear buffer
		serialPort.readBytes();
		return serialPort;
	}
}