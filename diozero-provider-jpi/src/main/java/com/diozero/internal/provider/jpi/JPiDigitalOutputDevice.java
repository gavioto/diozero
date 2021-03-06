package com.diozero.internal.provider.jpi;

/*
 * #%L
 * Device I/O Zero - Java Native provider for the Raspberry Pi
 * %%
 * Copyright (C) 2016 mattjlewis
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */


import org.pmw.tinylog.Logger;

import com.diozero.internal.spi.AbstractDevice;
import com.diozero.internal.spi.GpioDeviceInterface;
import com.diozero.internal.spi.GpioDigitalOutputDeviceInterface;
import com.diozero.util.RuntimeIOException;

public class JPiDigitalOutputDevice extends AbstractDevice implements GpioDigitalOutputDeviceInterface {

	private int pinNumber;
	private MmapGpioInterface mmapGpio;

	public JPiDigitalOutputDevice(JPiDeviceFactory deviceFactory, MmapGpioInterface mmapGpio,
			String key, int pinNumber, boolean initialValue) {
		super(key, deviceFactory);
		
		this.pinNumber = pinNumber;
		this.mmapGpio = mmapGpio;
		
		mmapGpio.setMode(pinNumber, GpioDeviceInterface.Mode.DIGITAL_OUTPUT);
		mmapGpio.gpioWrite(pinNumber, initialValue);
	}

	@Override
	public int getPin() {
		return pinNumber;
	}

	@Override
	public boolean getValue() throws RuntimeIOException {
		return mmapGpio.gpioRead(pinNumber);
	}

	@Override
	public void setValue(boolean value) throws RuntimeIOException {
		mmapGpio.gpioWrite(pinNumber, value);
	}

	@Override
	public void closeDevice() {
		Logger.debug("closeDevice()");
		// No GPIO close method
		// TODO Revert to default input mode?
	}
}
