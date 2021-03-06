package com.diozero.internal.board.raspberrypi;

/*
 * #%L
 * Device I/O Zero - Core
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
import java.util.*;

import com.diozero.internal.spi.GpioDeviceInterface.Mode;
import com.diozero.util.BoardInfo;
import com.diozero.util.BoardInfoProvider;

/**
 * See <a href="https://github.com/AndrewFromMelbourne/raspberry_pi_revision">this c library</a>.
 */
public class RaspberryPiBoardInfoProvider implements BoardInfoProvider {
	public static final String MAKE = "RaspberryPi";
	
	public static enum Model {
		A(0), B(1), A_PLUS(2), B_PLUS(3), PI_2_B(4), ALPHA(5), COMPUTE_MODEL(6), UNKNWON(7), PI_3_B(8), ZERO(9);
		
		private int id;
		private Model(int id) {
			this.id = id;
		}
		
		public int getId() {
			return id;
		}
		
		public static Model forId(int id) {
			Model[] values = Model.values();
			if (id < 0 || id >= values.length) {
				throw new IllegalArgumentException("Illegal Model id " + id + ", must be 0.." + (values.length-1));
			}
			return values[id];
		}
	}
	public static enum Revision {
		REV_1(0), REV_1_1(1), REV_2(2), REV_1_2(3);
		
		private int id;
		private Revision(int id) {
			this.id = id;
		}
		
		public int getId() {
			return id;
		}
		
		public static Revision forId(int id) {
			Revision[] values = Revision.values();
			if (id < 0 || id >= values.length) {
				throw new IllegalArgumentException("Illegal Revision id " + id + ", must be 0.." + (values.length-1));
			}
			return values[id];
		}
	}
	public static enum Memory {
		MEM_256(0, 256), MEM_512(1, 512), MEM_1024(2, 1024);

		private int id;
		private int ram;
		private Memory(int id, int ram) {
			this.id = id;
			this.ram = ram;
		}
		
		public int getId() {
			return id;
		}
		
		public int getRam() {
			return ram;
		}
		
		public static Memory forId(int id) {
			Memory[] values = Memory.values();
			if (id < 0 || id >= values.length) {
				throw new IllegalArgumentException("Illegal Memory id " + id + ", must be 0.." + (values.length-1));
			}
			return values[id];
		}
	}
	public static enum Manufacturer {
		SONY(0), EGOMAN(1), EMBEST(2), QISDA(3), EMBEST2(4);
		
		private int id;
		private Manufacturer(int id) {
			this.id = id;
		}
		
		public int getId() {
			return id;
		}
		
		public static Manufacturer forId(int id) {
			Manufacturer[] values = Manufacturer.values();
			if (id < 0 || id >= values.length) {
				throw new IllegalArgumentException("Illegal Manufacturer id " + id + ", must be 0.." + (values.length-1));
			}
			return values[id];
		}
	}
	public static enum Processor {
		BCM_2835(0), BCM_2836(1), BCM_2837(2);
		
		private int id;
		private Processor(int id) {
			this.id = id;
		}
		
		public int getId() {
			return id;
		}
		
		public static Processor forId(int id) {
			Processor[] values = Processor.values();
			if (id < 0 || id >= values.length) {
				throw new IllegalArgumentException("Illegal Processor id " + id + ", must be 0.." + (values.length-1));
			}
			return values[id];
		}
	}
	
	private static Map<String, BoardInfo> PI_BOARDS;
	static {
		PI_BOARDS = new HashMap<>();
		PI_BOARDS.put("0002", new PiBRev1BoardInfo(Revision.REV_1,
				Memory.MEM_256, Manufacturer.EGOMAN));
		PI_BOARDS.put("0003", new PiBRev1BoardInfo(Revision.REV_1_1,
				Memory.MEM_256, Manufacturer.EGOMAN));
		PI_BOARDS.put("0004", new PiABRev2BoardInfo(Model.B,
				Memory.MEM_256, Manufacturer.SONY));
		PI_BOARDS.put("0005", new PiABRev2BoardInfo(Model.B,
				Memory.MEM_256, Manufacturer.QISDA));
		PI_BOARDS.put("0006", new PiABRev2BoardInfo(Model.B,
				Memory.MEM_256, Manufacturer.EGOMAN));
		PI_BOARDS.put("0007", new PiABRev2BoardInfo(Model.A,
				Memory.MEM_256, Manufacturer.EGOMAN));
		PI_BOARDS.put("0008", new PiABRev2BoardInfo(Model.A,
				Memory.MEM_256, Manufacturer.SONY));
		PI_BOARDS.put("0009", new PiABRev2BoardInfo(Model.A,
				Memory.MEM_256, Manufacturer.QISDA));
		PI_BOARDS.put("000d", new PiABRev2BoardInfo(Model.B,
				Memory.MEM_512, Manufacturer.EGOMAN));
		PI_BOARDS.put("000e", new PiABRev2BoardInfo(Model.B,
				Memory.MEM_512, Manufacturer.SONY));
		PI_BOARDS.put("000f", new PiABRev2BoardInfo(Model.B,
				Memory.MEM_512, Manufacturer.EGOMAN));
		PI_BOARDS.put("0010", new PiABPlusBoardInfo(Model.B_PLUS, Revision.REV_1_2,
				Memory.MEM_512, Manufacturer.SONY, Processor.BCM_2835));
		PI_BOARDS.put("0011", new PiComputeModuleBoardInfo(
				Memory.MEM_512, Manufacturer.SONY, Processor.BCM_2835));
		PI_BOARDS.put("0012", new PiABPlusBoardInfo(Model.A_PLUS, Revision.REV_1_2,
				Memory.MEM_256, Manufacturer.SONY, Processor.BCM_2835));
		PI_BOARDS.put("0013", new PiABPlusBoardInfo(Model.B_PLUS, Revision.REV_1_2,
				Memory.MEM_512, Manufacturer.EGOMAN, Processor.BCM_2835));
		PI_BOARDS.put("0014", new PiComputeModuleBoardInfo(
				Memory.MEM_512, Manufacturer.SONY, Processor.BCM_2835));
		PI_BOARDS.put("0015", new PiABPlusBoardInfo(Model.A_PLUS, Revision.REV_1_1,
				Memory.MEM_256, Manufacturer.SONY, Processor.BCM_2835));
	}
	
	@Override
	public BoardInfo lookup(String revisionString) {
		try {
			int revision = Integer.parseInt(revisionString, 16);
			// With the release of the Raspberry Pi 2, there is a new encoding of the
			// Revision field in /proc/cpuinfo
			if ((revision & (1 << 23)) != 0) {
				int pcb_rev = (revision & (0x0F << 0)) >> 0;
				int model = (revision & (0xFF << 4)) >> 4;
				int proc = (revision & (0x0F << 12)) >> 12;
				int mfr = (revision & (0x0F << 16)) >> 16;
				int mem = (revision & (0x07 << 20)) >> 20;
				//boolean warranty_void = (revision & (0x03 << 24)) != 0;
				
				return new PiABPlusBoardInfo(Model.forId(model), Revision.forId(pcb_rev),
						Memory.forId(mem), Manufacturer.forId(mfr), Processor.forId(proc));
			}
		} catch (NumberFormatException nfe) {
			// Ignore
		}
		if (revisionString.length() < 4) {
			return null;
		}
		
		return PI_BOARDS.get(revisionString.substring(revisionString.length()-4));
	}
	
	static abstract class PiBoardInfo extends BoardInfo {
		private Model model;
		private Revision revision;
		private Memory memory;
		private Manufacturer manufacturer;
		private Processor processor;
		
		public PiBoardInfo(Model model, Revision revision, Memory memory,
				Manufacturer manufacturer, Processor processor, Map<Integer, List<Mode>> pins) {
			super(MAKE, model.toString(), memory.getRam(), pins, MAKE.toLowerCase());
			
			this.model = model;
			this.revision = revision;
			this.memory = memory;
			this.manufacturer = manufacturer;
			this.processor = processor;
		}
	
		public Model getPiModel() {
			return model;
		}
	
		public Revision getRevision() {
			return revision;
		}
	
		public Memory getPiMemory() {
			return memory;
		}
	
		public Manufacturer getManufacturer() {
			return manufacturer;
		}
	
		public Processor getProcessor() {
			return processor;
		}
	
		@Override
		public String toString() {
			return "PiBoardInfo [" + super.toString() + ", revision=" + revision + ", memory=" + memory + ", manufacturer="
					+ manufacturer + ", processor=" + processor + "]";
		}
	}
	
	public static class PiBRev1BoardInfo extends PiBoardInfo {
		private static Map<Integer, List<Mode>> B_REV_1_PINS;
		static {
			List<Mode> digital_in_out = Arrays.asList(Mode.DIGITAL_INPUT, Mode.DIGITAL_OUTPUT, Mode.SOFTWARE_PWM_OUTPUT);
			B_REV_1_PINS = new HashMap<>();
			B_REV_1_PINS.put(Integer.valueOf(0), digital_in_out);  // I2C SDA
			B_REV_1_PINS.put(Integer.valueOf(1), digital_in_out);  // I2C SCL
			B_REV_1_PINS.put(Integer.valueOf(4), digital_in_out);
			B_REV_1_PINS.put(Integer.valueOf(7), digital_in_out);  // SPI CE1
			B_REV_1_PINS.put(Integer.valueOf(8), digital_in_out);  // SPI CE0
			B_REV_1_PINS.put(Integer.valueOf(9), digital_in_out);  // SPI MISO
			B_REV_1_PINS.put(Integer.valueOf(10), digital_in_out); // SPI MOSI
			B_REV_1_PINS.put(Integer.valueOf(11), digital_in_out); // SPI CLK
			B_REV_1_PINS.put(Integer.valueOf(14), digital_in_out); // UART TXD
			B_REV_1_PINS.put(Integer.valueOf(15), digital_in_out); // UART RXD
			B_REV_1_PINS.put(Integer.valueOf(17), digital_in_out);
			B_REV_1_PINS.put(Integer.valueOf(21), digital_in_out);
			B_REV_1_PINS.put(Integer.valueOf(22), digital_in_out);
			B_REV_1_PINS.put(Integer.valueOf(23), digital_in_out);
			B_REV_1_PINS.put(Integer.valueOf(24), digital_in_out);
			B_REV_1_PINS.put(Integer.valueOf(25), digital_in_out);
			// GPIO 18 also has Hardware PWM output
			B_REV_1_PINS.put(Integer.valueOf(18), Arrays.asList(Mode.DIGITAL_INPUT, Mode.DIGITAL_OUTPUT, Mode.SOFTWARE_PWM_OUTPUT, Mode.PWM_OUTPUT));
		}
		
		public PiBRev1BoardInfo(Revision revision, Memory memory, Manufacturer manufacturer) {
			super(Model.B, revision, memory, manufacturer, Processor.BCM_2835, B_REV_1_PINS);
		}
	}
	
	public static class PiABRev2BoardInfo extends PiBoardInfo {
		private static Map<Integer, List<Mode>> AB_REV_2_PINS;
		static {
			List<Mode> digital_in_out = Arrays.asList(Mode.DIGITAL_INPUT, Mode.DIGITAL_OUTPUT, Mode.SOFTWARE_PWM_OUTPUT);
			// GPIO 18 also has Hardware PWM output
			List<Mode> digital_in_out_pwm = Arrays.asList(Mode.DIGITAL_INPUT, Mode.DIGITAL_OUTPUT, Mode.SOFTWARE_PWM_OUTPUT, Mode.PWM_OUTPUT);
			AB_REV_2_PINS = new HashMap<>();
			AB_REV_2_PINS.put(Integer.valueOf(2), digital_in_out);  // I2C SDA
			AB_REV_2_PINS.put(Integer.valueOf(3), digital_in_out);  // I2C SCL
			AB_REV_2_PINS.put(Integer.valueOf(4), digital_in_out);
			AB_REV_2_PINS.put(Integer.valueOf(7), digital_in_out);  // SPI CE1
			AB_REV_2_PINS.put(Integer.valueOf(8), digital_in_out);  // SPI CE0
			AB_REV_2_PINS.put(Integer.valueOf(9), digital_in_out);  // SPI MISO
			AB_REV_2_PINS.put(Integer.valueOf(10), digital_in_out); // SPI MOSI
			AB_REV_2_PINS.put(Integer.valueOf(11), digital_in_out); // SPI CLK
			AB_REV_2_PINS.put(Integer.valueOf(14), digital_in_out); // UART TXD
			AB_REV_2_PINS.put(Integer.valueOf(15), digital_in_out); // UART RXD
			AB_REV_2_PINS.put(Integer.valueOf(17), digital_in_out);
			AB_REV_2_PINS.put(Integer.valueOf(18), digital_in_out_pwm);
			AB_REV_2_PINS.put(Integer.valueOf(22), digital_in_out);
			AB_REV_2_PINS.put(Integer.valueOf(23), digital_in_out);
			AB_REV_2_PINS.put(Integer.valueOf(24), digital_in_out);
			AB_REV_2_PINS.put(Integer.valueOf(25), digital_in_out);
			AB_REV_2_PINS.put(Integer.valueOf(27), digital_in_out);
		}
		
		public PiABRev2BoardInfo(Model model, Memory memory, Manufacturer manufacturer) {
			super(model, Revision.REV_2, memory, manufacturer, Processor.BCM_2835, AB_REV_2_PINS);
		}
	}
	
	public static class PiABPlusBoardInfo extends PiBoardInfo {
		private static Map<Integer, List<Mode>> AB_PLUS_PINS;
		static {
			List<Mode> digital_in_out = Arrays.asList(Mode.DIGITAL_INPUT, Mode.DIGITAL_OUTPUT, Mode.SOFTWARE_PWM_OUTPUT);
			// GPIO 12, 13, 18 and 19 also have Hardware PWM output
			List<Mode> digital_in_out_pwm = Arrays.asList(Mode.DIGITAL_INPUT, Mode.DIGITAL_OUTPUT, Mode.SOFTWARE_PWM_OUTPUT, Mode.PWM_OUTPUT);
			AB_PLUS_PINS = new HashMap<>();
			AB_PLUS_PINS.put(Integer.valueOf(2), digital_in_out);  // I2C SDA
			AB_PLUS_PINS.put(Integer.valueOf(3), digital_in_out);  // I2C SCL
			AB_PLUS_PINS.put(Integer.valueOf(4), digital_in_out);
			AB_PLUS_PINS.put(Integer.valueOf(5), digital_in_out);
			AB_PLUS_PINS.put(Integer.valueOf(6), digital_in_out);
			AB_PLUS_PINS.put(Integer.valueOf(7), digital_in_out);  // SPI-0 CE1
			AB_PLUS_PINS.put(Integer.valueOf(8), digital_in_out);  // SPI-0 CE0
			AB_PLUS_PINS.put(Integer.valueOf(9), digital_in_out);  // SPI-0 MISO
			AB_PLUS_PINS.put(Integer.valueOf(10), digital_in_out); // SPI-0 MOSI
			AB_PLUS_PINS.put(Integer.valueOf(11), digital_in_out); // SPI-0 CLK
			AB_PLUS_PINS.put(Integer.valueOf(12), digital_in_out_pwm);
			AB_PLUS_PINS.put(Integer.valueOf(13), digital_in_out_pwm);
			AB_PLUS_PINS.put(Integer.valueOf(14), digital_in_out); // UART TXD
			AB_PLUS_PINS.put(Integer.valueOf(15), digital_in_out); // UART RXD
			AB_PLUS_PINS.put(Integer.valueOf(16), digital_in_out); // SPI-1 CE2
			AB_PLUS_PINS.put(Integer.valueOf(17), digital_in_out); // SPI-1 CE1
			AB_PLUS_PINS.put(Integer.valueOf(18), digital_in_out_pwm); // SPI-1 CE0
			AB_PLUS_PINS.put(Integer.valueOf(19), digital_in_out_pwm); // SPI-1 MISO
			AB_PLUS_PINS.put(Integer.valueOf(20), digital_in_out); // SPI-1 MOSI
			AB_PLUS_PINS.put(Integer.valueOf(21), digital_in_out); // SPI-1 SCLK
			AB_PLUS_PINS.put(Integer.valueOf(22), digital_in_out);
			AB_PLUS_PINS.put(Integer.valueOf(23), digital_in_out);
			AB_PLUS_PINS.put(Integer.valueOf(24), digital_in_out);
			AB_PLUS_PINS.put(Integer.valueOf(25), digital_in_out);
			AB_PLUS_PINS.put(Integer.valueOf(26), digital_in_out);
			AB_PLUS_PINS.put(Integer.valueOf(27), digital_in_out);
			// P5 Header
			AB_PLUS_PINS.put(Integer.valueOf(28), digital_in_out);
			AB_PLUS_PINS.put(Integer.valueOf(29), digital_in_out);
			AB_PLUS_PINS.put(Integer.valueOf(30), digital_in_out);
			AB_PLUS_PINS.put(Integer.valueOf(31), digital_in_out);
		}
		
		public PiABPlusBoardInfo(Model model, Revision revision, Memory memory, Manufacturer manufacturer, Processor processor) {
			super(model, revision, memory, manufacturer, processor, AB_PLUS_PINS);
		}
	}
	
	public static class PiComputeModuleBoardInfo extends PiBoardInfo {
		private static Map<Integer, List<Mode>> AB_PLUS_PINS;
		static {
			List<Mode> digital_in_out = Arrays.asList(Mode.DIGITAL_INPUT, Mode.DIGITAL_OUTPUT, Mode.SOFTWARE_PWM_OUTPUT);
			// GPIO 12, 13, 18 and 19 also have Hardware PWM output
			List<Mode> digital_in_out_pwm = Arrays.asList(Mode.DIGITAL_INPUT, Mode.DIGITAL_OUTPUT, Mode.SOFTWARE_PWM_OUTPUT, Mode.PWM_OUTPUT);
			AB_PLUS_PINS = new HashMap<>();
			AB_PLUS_PINS.put(Integer.valueOf(2), digital_in_out);  // I2C SDA
			AB_PLUS_PINS.put(Integer.valueOf(3), digital_in_out);  // I2C SCL
			AB_PLUS_PINS.put(Integer.valueOf(4), digital_in_out);
			AB_PLUS_PINS.put(Integer.valueOf(5), digital_in_out);
			AB_PLUS_PINS.put(Integer.valueOf(6), digital_in_out);
			AB_PLUS_PINS.put(Integer.valueOf(7), digital_in_out);  // SPI-0 CE1
			AB_PLUS_PINS.put(Integer.valueOf(8), digital_in_out);  // SPI-0 CE0
			AB_PLUS_PINS.put(Integer.valueOf(9), digital_in_out);  // SPI-0 MISO
			AB_PLUS_PINS.put(Integer.valueOf(10), digital_in_out); // SPI-0 MOSI
			AB_PLUS_PINS.put(Integer.valueOf(11), digital_in_out); // SPI-0 CLK
			AB_PLUS_PINS.put(Integer.valueOf(12), digital_in_out_pwm);
			AB_PLUS_PINS.put(Integer.valueOf(13), digital_in_out_pwm);
			AB_PLUS_PINS.put(Integer.valueOf(14), digital_in_out); // UART TXD
			AB_PLUS_PINS.put(Integer.valueOf(15), digital_in_out); // UART RXD
			AB_PLUS_PINS.put(Integer.valueOf(16), digital_in_out); // SPI-1 CE2
			AB_PLUS_PINS.put(Integer.valueOf(17), digital_in_out); // SPI-1 CE1
			AB_PLUS_PINS.put(Integer.valueOf(18), digital_in_out_pwm); // SPI-1 CE0
			AB_PLUS_PINS.put(Integer.valueOf(19), digital_in_out_pwm); // SPI-1 MISO
			AB_PLUS_PINS.put(Integer.valueOf(20), digital_in_out); // SPI-1 MOSI
			AB_PLUS_PINS.put(Integer.valueOf(21), digital_in_out); // SPI-1 SCLK
			AB_PLUS_PINS.put(Integer.valueOf(22), digital_in_out);
			AB_PLUS_PINS.put(Integer.valueOf(23), digital_in_out);
			AB_PLUS_PINS.put(Integer.valueOf(24), digital_in_out);
			AB_PLUS_PINS.put(Integer.valueOf(25), digital_in_out);
			AB_PLUS_PINS.put(Integer.valueOf(26), digital_in_out);
			AB_PLUS_PINS.put(Integer.valueOf(27), digital_in_out);
			// P5 Header
			AB_PLUS_PINS.put(Integer.valueOf(28), digital_in_out);
			AB_PLUS_PINS.put(Integer.valueOf(29), digital_in_out);
			AB_PLUS_PINS.put(Integer.valueOf(30), digital_in_out);
			AB_PLUS_PINS.put(Integer.valueOf(31), digital_in_out);
		}
		
		public PiComputeModuleBoardInfo(Memory memory, Manufacturer manufacturer, Processor processor) {
			super(Model.COMPUTE_MODEL, Revision.REV_1_2, memory, manufacturer, processor, AB_PLUS_PINS);
		}
	}
}
