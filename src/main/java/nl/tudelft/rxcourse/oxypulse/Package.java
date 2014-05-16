package nl.tudelft.rxcourse.oxypulse;

import java.util.BitSet;

import jssc.SerialPort;
import jssc.SerialPortException;

public class Package {

	private final SerialPort serialPort;

	private boolean isSearching;
	private boolean isSearchingTooLong;
	private boolean isDroppingOffOxygen;
	private boolean hasBeepFlag;
	private boolean hasProbeError;

	private long signalStrength;
	private long pulseWaveform;
	private long pulseRate;
	private long pulseBar;
	private long oxygen;

	public Package(SerialPort serialPort) {
		this.serialPort = serialPort;
		try {
			processByteOne();

			processByteTwo();

			processByteFour(processByteThree());

			processByteFive();

		} catch (SerialPortException e) {
			System.err.println(e);
		}
	}

	private void processByteFive() throws SerialPortException {
		// byte #5
		BitSet oxygenBits = readNextByteFrom(serialPort);
		oxygen = getLongValueFrom(oxygenBits);
	}

	private void processByteFour(boolean pulseWaveBit7)
			throws SerialPortException {
		// byte #4
		BitSet pulseRateBits = readNextByteFrom(serialPort);
		pulseRateBits.set(7, pulseWaveBit7);
		pulseRate = getLongValueFrom(pulseRateBits);
	}

	private boolean processByteThree() throws SerialPortException {
		// byte #3
		BitSet barGraph = readNextByteFrom(serialPort);
		pulseBar = getLongValueFrom(barGraph, 0, 3);

		hasProbeError = barGraph.get(4);
		isSearching = barGraph.get(5);

		boolean pulseWaveBit7 = barGraph.get(6);
		return pulseWaveBit7;
	}

	private void processByteTwo() throws SerialPortException {
		// byte #2
		BitSet waveformData = readNextByteFrom(serialPort).get(0, 6);
		pulseWaveform = getLongValueFrom(waveformData);
	}

	private void processByteOne() throws SerialPortException {
		// byte #1
		BitSet signal = readNextByteFrom(serialPort);
		signalStrength = getLongValueFrom(signal, 0, 3);
		isSearchingTooLong = signal.get(4);
		isDroppingOffOxygen = signal.get(5);
		hasBeepFlag = signal.get(6);
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
		return String
				.format("\tOxygen:\t%d\tPulse Rate:\t%d\tPulse Waveform:\t%d Pulse Bar:\t%d\tSignal Strength:\t%d",
						oxygen, 
						pulseRate, 
						pulseWaveform, 
						pulseBar,
						signalStrength
				);
	}
}
