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
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.diozero.internal.spi.GpioDeviceFactoryInterface;
import com.diozero.internal.spi.GpioDigitalInputDeviceInterface;
import com.diozero.internal.spi.InternalPinListener;

/**
 * Represents a generic input device.
 * 
 */
public class DigitalInputDevice extends GpioDevice implements InternalPinListener {
	private static final Logger logger = LogManager.getLogger(DigitalInputDevice.class);
	
	private Consumer<DigitalPinEvent> consumer;
	protected boolean activeHigh;
	protected GpioDigitalInputDeviceInterface device;

	public DigitalInputDevice(int pinNumber, GpioPullUpDown pud, GpioEventTrigger trigger) throws IOException {
		this(DeviceFactoryHelper.getNativeDeviceFactory(), pinNumber, pud, trigger);
	}

	public DigitalInputDevice(GpioDeviceFactoryInterface deviceFactory, int pinNumber, GpioPullUpDown pud, GpioEventTrigger trigger) throws IOException {
		this(deviceFactory.provisionDigitalInputPin(pinNumber, pud, trigger), pud != GpioPullUpDown.PULL_DOWN);
	}

	public DigitalInputDevice(GpioDigitalInputDeviceInterface device, boolean activeHigh) {
		super(device.getPin());
		
		this.device = device;
		this.activeHigh = activeHigh;
	}

	@Override
	public void close() {
		logger.debug("close()");
		device.close();
	}

	public boolean getValue() throws IOException {
		return device.getValue();
	}
	
	public boolean isActive() throws IOException {
		return device.getValue() == activeHigh;
	}

	public void setConsumer(Consumer<DigitalPinEvent> consumer) {
		device.setListener(this);
		this.consumer = consumer;
	}

	@Override
	public void valueChanged(DigitalPinEvent event) {
		if (consumer != null) {
			consumer.accept(event);
		}
	}
}