package com.diozero.sampleapps;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.diozero.TSL2561;
import com.diozero.util.SleepUtil;

/**
 * To run:
 * JDK Device I/O 1.0:
 *  sudo java -cp log4j-api-2.5.jar:log4j-core-2.5.jar:diozero-core-0.2-SNAPSHOT.jar:diozero-provider-jdkdio10-0.2-SNAPSHOT.jar:dio-1.0.1-dev-linux-armv6hf.jar -Djava.library.path=. com.diozero.sampleapps.TSL2561Test
 * JDK Device I/O 1.1:
 *  sudo java -cp log4j-api-2.5.jar:log4j-core-2.5.jar:diozero-core-0.2-SNAPSHOT.jar:diozero-provider-jdkdio11-0.2-SNAPSHOT.jar:dio-1.1-dev-linux-armv6hf.jar -Djava.library.path=. com.diozero.sampleapps.TSL2561Test
 * Pi4j:
 *  sudo java -cp log4j-api-2.5.jar:log4j-core-2.5.jar:diozero-core-0.2-SNAPSHOT.jar:diozero-provider-pi4j-0.2-SNAPSHOT.jar:pi4j-core-1.1-SNAPSHOT.jar com.diozero.sampleapps.TSL2561Test
 * wiringPi:
 *  sudo java -cp log4j-api-2.5.jar:log4j-core-2.5.jar:diozero-core-0.2-SNAPSHOT.jar:diozero-provider-wiringpi-0.2-SNAPSHOT.jar:pi4j-core-1.1-SNAPSHOT.jar com.diozero.sampleapps.TSL2561Test
 * pigpgioJ:
 *  sudo java -cp log4j-api-2.5.jar:log4j-core-2.5.jar:diozero-core-0.2-SNAPSHOT.jar:diozero-provider-pigpio-0.2-SNAPSHOT.jar:pigpioj-java-0.0.1-SNAPSHOT.jar -Djava.library.path=. com.diozero.sampleapps.TSL2561Test
 */
public class TSL2561Test {
	private static final Logger logger = LogManager.getLogger(TSL2561Test.class);

	public static void main(String[] args) {
		try (TSL2561 tsl2561 = new TSL2561(TSL2561.TSL2561_PACKAGE_T_FN_CL)) {
			tsl2561.enableAutoGain(true);

			while (true) {
				double lux = tsl2561.getLuminosity();
				System.out.format("Luminosity=%f Lux%n", Double.valueOf(lux));

				SleepUtil.sleepMillis(1);
			}
		} catch (IOException ioe) {
			logger.error("Error: " + ioe, ioe);
		}
	}
}