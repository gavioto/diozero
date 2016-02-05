package com.diozero.sampleapps;

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

import com.diozero.MCP3008;
import com.diozero.util.SleepUtil;

/**
 * MCP3008 test application
 * To run:
 * (Pi4j):				sudo java -classpath dio-zero.jar:pi4j-core.jar com.diozero.sampleapps.MCP3008Test 0 0
 * (JDK Device I/O):	sudo java -classpath dio-zero.jar -Djava.security.policy=config/gpio.policy com.diozero.sampleapps.MCP3008Test 0 0
 */
public class MCP3008Test {
	private static final Logger logger = LogManager.getLogger(MCP3008Test.class);
	
	public static void main(String[] args) {
		if (args.length < 2) {
			logger.error("Usage: MCP3008 <spi-chip-select> <adc_pin>");
			System.exit(2);
		}
		int spi_chip_select = Integer.parseInt(args[0]);
		int adc_pin = Integer.parseInt(args[1]);

		try (MCP3008 mcp3008 = new MCP3008(spi_chip_select)) {
			while (true) {
				float v = mcp3008.getVoltage(adc_pin);
				logger.info("Voltage: %.2f", Float.valueOf(v));
				SleepUtil.sleepMillis(1000);
			}
		} catch (IOException ioe) {
			logger.error("Error: " + ioe, ioe);
		}
	}
}