package nl.tudelft.rxcourse.oxypulse;

import java.util.BitSet;

import jssc.SerialPort;
import jssc.SerialPortException;

/**
 * Simple program to test reading values from the device. 
 * Nothing Reactive Yet...
 * 
 */
public class App {

	public static SerialPort serialPort;
	public static String PORT = "/dev/tty.SLAB_USBtoUART";

	public static final int PACKET_SYNCED_FLAG = 7;

	public static long WAIT_TIME_SECONDS = 30 * 1000;
	public static long WAIT_FOR_PACKAGE = 1 / 60 * 1000;

	public static boolean SEARCHING_TOO_LONG = false;
	public static boolean DROPPING_OFF_SPO2 = false;
	public static boolean BEEP_FLAG = false;

	public static boolean PROBE_ERROR = false;
	public static boolean SEARCHING = false;

	public static void main(String[] args) {

		try {
			serialPort = openSerialPort(PORT);

			// keep track of time
			long start = System.currentTimeMillis();

			while (System.currentTimeMillis() < (start + WAIT_TIME_SECONDS)) {

				// Read a byte from the Serial Port
				BitSet signal = readNextByteFrom(serialPort);
				// If the packet we received has the sync flag
				if (signal.get(PACKET_SYNCED_FLAG)) {
					long signalStrength = getLongValueFrom(signal, 0, 3);
					SEARCHING_TOO_LONG = signal.get(4);
					DROPPING_OFF_SPO2 = signal.get(5);
					BEEP_FLAG = signal.get(6);

					// Read the next data packets
					BitSet waveformData = readNextByteFrom(serialPort)
							.get(0, 6);
					long tick = getLongValueFrom(waveformData);

					BitSet barGraph = readNextByteFrom(serialPort);
					long barValue = getLongValueFrom(barGraph, 0, 3);

					PROBE_ERROR = barGraph.get(4);
					SEARCHING = barGraph.get(5);

					boolean pulseWaveBit7 = barGraph.get(6);

					BitSet pulseRate = readNextByteFrom(serialPort);
					pulseRate.set(7, pulseWaveBit7);
					long pulseValue = getLongValueFrom(pulseRate);

					BitSet SpO2 = readNextByteFrom(serialPort);
					long SpO2Value = getLongValueFrom(SpO2);

					System.out.println(String.format(""
							+ "Signal strength: %d WaveForm Data: %d "
							+ "BarValue: %d Pulse: %d SPO2: %d",
							signalStrength, tick, barValue, pulseValue,
							SpO2Value));
				}
			}
			serialPort.closePort();

		} catch (SerialPortException e) {
			System.out.println(e);
		} catch (InterruptedException e) {
			System.out.println(e);
		}
	}

	private static long getLongValueFrom(BitSet bits, int from, int to) {
		long[] values = bits.get(from, to).toLongArray();
		return values.length > 0 ? values[0] : 0;
	}

	private static long getLongValueFrom(BitSet bits) {
		return getLongValueFrom(bits, 0, bits.length() - 1);
	}

	private static BitSet readNextByteFrom(SerialPort serialPort)
			throws SerialPortException, InterruptedException {
		// The Device sends 60 packages/second.
		// 1 package contains 5 bytes.
		// Yup, just block
		Thread.sleep(WAIT_FOR_PACKAGE / 5);

		return BitSet.valueOf(serialPort.readBytes(1));
	}

	private static SerialPort openSerialPort(String port)
			throws SerialPortException {

		// Port to open
		SerialPort serialPort = new SerialPort(port);
		// Open serial port
		System.out.println("Opening Port: " + port);
		serialPort.openPort();
		// Communication settings for port
		System.out.println("Set communication settings.");
		serialPort.setParams(SerialPort.BAUDRATE_19200, SerialPort.DATABITS_8,
				SerialPort.STOPBITS_1, SerialPort.PARITY_ODD);

		return serialPort;
	}
}
