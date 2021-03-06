package com.diozero.internal.provider.test;

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

import java.nio.ByteBuffer;
import java.util.Random;

import org.junit.Assert;

import com.diozero.McpAdc;
import com.diozero.api.SpiClockMode;
import com.diozero.internal.spi.DeviceFactoryInterface;
import com.diozero.util.RuntimeIOException;

public class TestMcpAdcSpiDevice extends TestSpiDevice {
	private static final Random random = new Random();
	
	private static McpAdc.Type type;
	static {
		// Default to MCP3208
		setType(McpAdc.Type.MCP3208);
	}
	
	public static void setType(McpAdc.Type type) {
		TestMcpAdcSpiDevice.type = type;
	}

	public TestMcpAdcSpiDevice(String key, DeviceFactoryInterface deviceFactory, int controller, int chipSelect, int frequency,
			SpiClockMode spiClockMode) {
		super(key, deviceFactory, controller, chipSelect, frequency, spiClockMode);
	}

	@Override
	public ByteBuffer writeAndRead(ByteBuffer out) throws RuntimeIOException {
		byte b = out.get();
		//out.put((byte) (0x10 | (differentialRead ? 0 : 0x08 ) | adcPin));
		//int pin = b & 0x07;
		//Logger.debug("Received read request for pin {}", Integer.valueOf(pin));
		b = out.get();
		Assert.assertEquals(0, b);
		b = out.get();
		Assert.assertEquals(0, b);
		
		int temp = random.nextInt(type.getRange());
		// FIXME Support MCP3301
		ByteBuffer dst = ByteBuffer.allocateDirect(3);
		dst.put((byte)0);
		switch (type.name().substring(0, 5)) {
		case "MCP30":
			// Rx x0RRRRRR RRRRxxxx for the 30xx (10-bit unsigned)
			dst.put((byte)((temp >> 4) & 0x3f));
			dst.put((byte)((temp << 4) & 0xf0));
			break;
		case "MCP32":
			// Rx x0RRRRRR RRRRRRxx for the 32xx (12-bit unsigned)
			dst.put((byte)((temp >> 6) & 0x3f));
			dst.put((byte)((temp << 2) & 0xfc));
			break;
		case "MCP33":
			// Signed
			temp *= random.nextBoolean() ? 1 : -1;
			// Rx x0SRRRRR RRRRRRRx for the 33xx (13-bit signed)
			dst.put((byte)((temp >> 7) & 0x3f));
			dst.put((byte)((temp << 1) & 0xfe));
			break;
		}
		dst.flip();
		
		return dst;
	}
}
