package com.diozero.internal.provider.mcpadc;

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

import org.pmw.tinylog.Logger;

import com.diozero.McpAdc;
import com.diozero.api.AnalogInputEvent;
import com.diozero.internal.spi.AbstractInputDevice;
import com.diozero.internal.spi.GpioAnalogInputDeviceInterface;
import com.diozero.util.RuntimeIOException;

public class McpAdcAnalogInputPin extends AbstractInputDevice<AnalogInputEvent> implements GpioAnalogInputDeviceInterface {
	private McpAdc mcp3xxx;
	private int pinNumber;

	public McpAdcAnalogInputPin(McpAdc mcp3xxx, String key, int pinNumber) {
		super(key, mcp3xxx);
		
		this.mcp3xxx = mcp3xxx;
		this.pinNumber = pinNumber;
	}

	@Override
	public void closeDevice() {
		Logger.debug("closeDevice()");
		// TODO Nothing to do?
	}

	@Override
	public float getValue() throws RuntimeIOException {
		return mcp3xxx.getValue(pinNumber);
	}

	@Override
	public int getPin() {
		return pinNumber;
	}
}
