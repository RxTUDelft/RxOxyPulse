package nl.tudelft.rxcourse.oxipulse;

import java.util.BitSet;

import jssc.SerialPort;
import jssc.SerialPortException;

/**
 * Test the input.
 * 
 */
public class App {

	public static SerialPort serialPort;
	public static String PORT = "/dev/tty.SLAB_USBtoUART";
	
	public static final int PACKET_SYNCED_FLAG = 7;
	
	public static long WAIT_TIME_SECONDS = 30 * 1000;
	public static long WAIT_FOR_PACKAGE = 1/60 * 1000;
	
	public static void main(String[] args) {

		try {
			serialPort = openSerialPort(PORT);

			long start = System.currentTimeMillis();
			while (System.currentTimeMillis() < (start + WAIT_TIME_SECONDS)) {
				// Read a byte from the Serial Port
				BitSet signal = readNextPackageFrom(serialPort);
				// If the packet we received has the sync flag
				if(signal.get(PACKET_SYNCED_FLAG)) {
					// Read the next data packets
					BitSet waveformData = readNextPackageFrom(serialPort);
					BitSet barGraph = readNextPackageFrom(serialPort);
					BitSet pulseRate = readNextPackageFrom(serialPort);
					BitSet SpO2 = readNextPackageFrom(serialPort);
					
					System.out.print(signal.toLongArray()[0]+ "  ");
					System.out.print(waveformData + " ");
					System.out.print(barGraph + " ");
					System.out.print(pulseRate + " ");
					System.out.println(SpO2.toLongArray()[0]);

					// build data from two packages
					BitSet graph = new BitSet(8);
					
				}
			}
			serialPort.closePort();

		} catch (SerialPortException e) {
			System.out.println(e);
		} catch (InterruptedException e) {
			System.out.println(e);
		}
	}
	
	private static BitSet readNextPackageFrom(SerialPort serialPort) throws SerialPortException, InterruptedException {
		// The Device sends 60 packages/second.
		Thread.sleep(WAIT_TIME_SECONDS);
		
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
