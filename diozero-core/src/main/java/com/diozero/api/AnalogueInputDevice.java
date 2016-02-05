package com.diozero.api;

/*
 * #%L
 * Device I/O Zero - Core
 * %%
 * Copyright (C) 2016 diozero
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


import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.diozero.internal.spi.AnalogueInputDeviceFactoryInterface;
import com.diozero.internal.spi.GpioAnalogueInputDeviceInterface;

public class AnalogueInputDevice extends GpioDevice {
	private static final Logger logger = LogManager.getLogger(AnalogueInputDevice.class);
	
	private GpioAnalogueInputDeviceInterface device;

	public AnalogueInputDevice(int pinNumber) throws IOException {
		this(DeviceFactoryHelper.getNativeDeviceFactory().provisionAnalogueInputPin(pinNumber));
	}

	public AnalogueInputDevice(AnalogueInputDeviceFactoryInterface deviceFactory, int pinNumber) throws IOException {
		this(deviceFactory.provisionAnalogueInputPin(pinNumber));
	}

	public AnalogueInputDevice(GpioAnalogueInputDeviceInterface device) {
		super(device.getPin());
		this.device = device;
	}

	@Override
	public void close() throws IOException {
		logger.debug("close()");
		if (device != null) { device.close(); }
	}
	
	public float getValue() throws IOException {
		return device.getValue();
	}
}