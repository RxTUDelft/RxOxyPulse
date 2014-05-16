package nl.tudelft.rxcourse.oxypulse;

import java.util.BitSet;

import jssc.SerialPort;
import jssc.SerialPortException;

public class Package {

	private boolean SEARCHING;
	private boolean SEARCHING_TOO_LONG;
	private boolean DROPPING_OFF_SPO2;
	private boolean BEEP_FLAG;
	private boolean PROBE_ERROR;

	private long signalStrength;
	private long pulseWaveform;
	private long pulseRate;
	private long pulseBar;
	private long oxygen;

	public Package(SerialPort serialPort) {
		try {
			processByteOne(serialPort);

			processByteTwo(serialPort);

			boolean pulseWaveBit7 = processByteThree(serialPort);

			processByteFour(serialPort, pulseWaveBit7);

			processByteFive(serialPort);
		} catch (SerialPortException e) {
			System.err.println(e);
		}
	}

	private void processByteFive(SerialPort serialPort)
			throws SerialPortException {
		// byte #5
		BitSet oxygenBits = readNextByteFrom(serialPort);
		oxygen = getLongValueFrom(oxygenBits);
	}

	private void processByteFour(SerialPort serialPort, boolean pulseWaveBit7)
			throws SerialPortException {
		// byte #4
		BitSet pulseRateBits = readNextByteFrom(serialPort);
		pulseRateBits.set(7, pulseWaveBit7);
		pulseRate = getLongValueFrom(pulseRateBits);
	}

	private boolean processByteThree(SerialPort serialPort)
			throws SerialPortException {
		// byte #3
		BitSet barGraph = readNextByteFrom(serialPort);
		pulseBar = getLongValueFrom(barGraph, 0, 3);

		PROBE_ERROR = barGraph.get(4);
		SEARCHING = barGraph.get(5);

		boolean pulseWaveBit7 = barGraph.get(6);
		return pulseWaveBit7;
	}

	private void processByteTwo(SerialPort serialPort)
			throws SerialPortException {
		// byte #2
		BitSet waveformData = readNextByteFrom(serialPort).get(0, 6);
		pulseWaveform = getLongValueFrom(waveformData);
	}

	private void processByteOne(SerialPort serialPort)
			throws SerialPortException {
		// byte #1
		BitSet signal = readNextByteFrom(serialPort);
		signalStrength = getLongValueFrom(signal, 0, 3);
		SEARCHING_TOO_LONG = signal.get(4);
		DROPPING_OFF_SPO2 = signal.get(5);
		BEEP_FLAG = signal.get(6);
	}

	private long getLongValueFrom(BitSet bits, int from, int to) {
		long[] values = bits.get(from, to).toLongArray();
		return values.length > 0 ? values[0] : 0;
	}

	private long getLongValueFrom(BitSet bits) {
		return getLongValueFrom(bits, 0, bits.length());
	}

	private BitSet readNextByteFrom(SerialPort serialPort)
			throws SerialPortException {

		return BitSet.valueOf(serialPort.readBytes(1));
	}
	
	public String toString() {
		return String.format("Oxygen: %d Pulse Rate: %d Pulse Waveform: %d Pulse Bar: %d Signal Strength: %d", oxygen, pulseRate, pulseWaveform, pulseBar, signalStrength);
	}
}
