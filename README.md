RxOxyPulse
=========

Goal: 11. *Hack O~2 / heartrate sensor, sensors as observable streams.*

Hardware
-------------

Name: Pulse Oximeter

Drivers: [CP210x USB to UART Bridge VCP Drivers](http://www.silabs.com/products/mcu/pages/usbtouartbridgevcpdrivers.aspx)

Communication Protocol
---------------------------------
1. Communication settings (for Comm port)
	- Data format:
		- 1 Start bit + 8 data bits + 1 stopbit, odd
	- Baudrate:
		- 4800 baud / 19200 baud	 	 
2. Real time data sent to PC from Pulse Oximeter:
	- Data: 5 bytes in 1 package
	- 60 packages/second
	- bit 7 stands for synchronization.

	|byte#	|bit   	|content				|
	|:-----:|:-----:|---------------------------------------|
	|	|0 ~ 3 	|Signal strength for pulsate (0 ~ 8)	|
	|	|4	|1= searching too long, 0=OK		|
	|1	|5	|1= dropping of SpO~2, 0=OK		|
	|	|6	|1= beep flag				|
	|	|7	|Synchronization, always be 1		|

	|byte#	|bit	|content				|
	|:-----:|:-----:|---------------------------------------|
	|2	|0 ~ 6	|pulse waveform data			|
	|	|7	|Synchronization, always be 0		|

	|byte#	|bit	|content				|
	|:-----:|:-----:|---------------------------------------|
	|	|0 ~ 3	|bar graph (stand for pulsate case)	|
	|	|4	|1= probe error, 0=OK			|
	|3	|5	|1= searching, 0=OK			|
	|	|6	|**bit 7 for Pulse Rate**		|
	|	|7	|Synchronization, always be 0		|

	|byte#	|bit	|content				|
	|:-----:|:-----:|---------------------------------------|
	|4	|0 ~ 6	|bit 0 ~ 6 for Pulse Rate		|
	|	|	|*bit 7 of Pulse Rate is in byte #3 *	|
	|	|7	|Synchronization, always be 0		|

	|byte#	|bit	|content				|
	|:-----:|:-----:|---------------------------------------|
	|5	|0 ~ 6	|bit 0 ~ 6 for SpO~2			|
	|	|7	|Synchronization, always be 0		|



