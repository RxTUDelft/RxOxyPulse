package nl.tudelft.rxcourse.oxypulse;

import java.util.BitSet;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import rx.Observable;
import rx.Subscriber;

public class Main {

	public static SerialPort serialPort;
	public static String PORT = "/dev/tty.SLAB_USBtoUART";

	public static boolean SEARCHING;
	public static boolean SEARCHING_TOO_LONG;
	public static boolean DROPPING_OFF_SPO2;
	public static boolean BEEP_FLAG;
	public static boolean PROBE_ERROR;

	public static void main(String[] args) {
		try {
			serialPort = openSerialPort(PORT);

			Observable<SerialPortEvent> observer = fromSerialPort(serialPort);

		} catch (SerialPortException ex) {
			System.out.println(ex);
		}
	}
	
	public static Observable<SerialPortEvent> fromSerialPort(SerialPort port) {
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

	static class SerialPortReader implements SerialPortEventListener {

		public void serialEvent(SerialPortEvent event) {
			try {
				// if this is a receive event
				if (event.isRXCHAR()) {
					int eventValue = event.getEventValue();
					// if the buffer has 5 bytes
					if (eventValue == 5) {
						// byte #1
						BitSet signal = readNextByteFrom(serialPort);
						long signalStrength = getLongValueFrom(signal, 0, 3);
						SEARCHING_TOO_LONG = signal.get(4);
						DROPPING_OFF_SPO2 = signal.get(5);
						BEEP_FLAG = signal.get(6);

						// byte #2
						BitSet waveformData = readNextByteFrom(serialPort).get(
								0, 6);
						long tick = getLongValueFrom(waveformData);

						// byte #3
						BitSet barGraph = readNextByteFrom(serialPort);
						long barValue = getLongValueFrom(barGraph, 0, 3);

						PROBE_ERROR = barGraph.get(4);
						SEARCHING = barGraph.get(5);

						boolean pulseWaveBit7 = barGraph.get(6);

						// byte #4
						BitSet pulseRate = readNextByteFrom(serialPort);
						pulseRate.set(7, pulseWaveBit7);
						long pulseValue = getLongValueFrom(pulseRate);

						// byte #5
						BitSet SpO2 = readNextByteFrom(serialPort);
						long SpO2Value = getLongValueFrom(SpO2);

						System.out.println(String.format(""
								+ "Signal strength: %d WaveForm Data: %d "
								+ "BarValue: %d Pulse: %d SPO2: %d",
								signalStrength, tick, barValue, pulseValue,
								SpO2Value));

					} else {
						// if the buffer has more than 5 bytes
						if (eventValue > 5) {
							// clear the buffer
							serialPort.readBytes(eventValue);
							System.err
									.println(String.format(
											"Clearing buffer of %d bytes.",
											eventValue));
						}
					}
				}
			} catch (SerialPortException ex) {
				System.err.println(ex);
			}
		}
	}

	private static long getLongValueFrom(BitSet bits, int from, int to) {
		long[] values = bits.get(from, to).toLongArray();
		return values.length > 0 ? values[0] : 0;
	}

	private static long getLongValueFrom(BitSet bits) {
		return getLongValueFrom(bits, 0, bits.length());
	}

	private static BitSet readNextByteFrom(SerialPort serialPort)
			throws SerialPortException {

		return BitSet.valueOf(serialPort.readBytes(1));
	}
	
	private static SerialPort openSerialPort(String port) throws SerialPortException {
		SerialPort serialPort = new SerialPort(port);		
		serialPort.openPort();		
		serialPort.setParams(SerialPort.BAUDRATE_19200,
				SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
				SerialPort.PARITY_ODD);
		// Specify the types of events that we want to track.
		int mask = SerialPort.MASK_RXCHAR;
		// Set the prepared mask
		serialPort.setEventsMask(mask);
		
		return serialPort;
	}
}