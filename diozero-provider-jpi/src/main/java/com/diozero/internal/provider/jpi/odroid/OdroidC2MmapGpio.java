package com.diozero.internal.provider.jpi.odroid;

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


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Random;

import com.diozero.api.GpioPullUpDown;
import com.diozero.internal.provider.jpi.MmapBufferNative;
import com.diozero.internal.provider.jpi.MmapByteBuffer;
import com.diozero.internal.provider.jpi.MmapGpioInterface;
import com.diozero.internal.provider.jpi.rpi.RPiMmapGpio;
import com.diozero.internal.spi.GpioDeviceInterface;
import com.diozero.util.LibraryLoader;
import com.diozero.util.MemoryInspector;
import com.diozero.util.SleepUtil;

/**
 * See <a href="https://github.com/hardkernel/wiringPi/blob/master/wiringPi/wiringPi.c">Odroid wiringPi</a> fork.
 */
public class OdroidC2MmapGpio implements MmapGpioInterface {
	private static final String MEM_DEVICE = "/dev/mem";
	private static final int GPIO_BASE_OFFSET = 0xC8834000;
	private static final int BLOCK_SIZE = 4*1024;
	private static final int C2_GPIO_PIN_BASE = 0x88;
	private static final int C2_GPIOY_PIN_START = C2_GPIO_PIN_BASE + 0x4b;
	private static final int C2_GPIOY_PIN_END = C2_GPIO_PIN_BASE + 0x5b;
	private static final int C2_GPIOX_PIN_START = C2_GPIO_PIN_BASE + 0x5c;
	private static final int C2_GPIOX_PIN_END = C2_GPIO_PIN_BASE + 0x72;

	private static final int C2_GPIOX_FSEL_REG_OFFSET = 0x118;
	private static final int C2_GPIOX_OUTP_REG_OFFSET = 0x119;
	private static final int C2_GPIOX_INP_REG_OFFSET = 0x11A;
	private static final int C2_GPIOX_PUPD_REG_OFFSET = 0x13E;
	private static final int C2_GPIOX_PUEN_REG_OFFSET = 0x14C;

	private static final int C2_GPIOY_FSEL_REG_OFFSET = 0x10F;
	private static final int C2_GPIOY_OUTP_REG_OFFSET = 0x110;
	private static final int C2_GPIOY_INP_REG_OFFSET = 0x111;
	private static final int C2_GPIOY_PUPD_REG_OFFSET = 0x13B;
	private static final int C2_GPIOY_PUEN_REG_OFFSET = 0x149;
	
	private static final int[] C2_GP_TO_SHIFT_REG = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16,
			0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22 };
	
	private boolean loaded;
	private MmapByteBuffer mmap;
	private volatile IntBuffer gpioReg;
	
	@Override
	public synchronized void initialise() {
		if (! loaded) {
			LibraryLoader.loadLibrary(RPiMmapGpio.class, "jpi");
			
			mmap = MmapBufferNative.createMmapBuffer(MEM_DEVICE, GPIO_BASE_OFFSET, BLOCK_SIZE);
			gpioReg = mmap.getBuffer().order(ByteOrder.LITTLE_ENDIAN).asIntBuffer();
			
			loaded = true;
		}
	}
	
	@Override
	public synchronized void terminate() {
		if (loaded) {
			MmapBufferNative.closeMmapBuffer(mmap.getFd(), mmap.getAddress(), mmap.getLength());
		}
	}
	
	@Override
	public int getMode(int gpio) {
		int reg = gpio < C2_GPIOX_PIN_START ? C2_GPIOY_FSEL_REG_OFFSET : C2_GPIOX_FSEL_REG_OFFSET;
		int shift = C2_GP_TO_SHIFT_REG[gpio - C2_GPIOY_PIN_START];
		
		// TODO FIXME Mapping to GpioDeviceInterface.Mode enum
		//return (gpioReg.get(reg) & (1 << shift)) == 0 ? GpioDeviceInterface.Mode.DIGITAL_OUTPUT : GpioDeviceInterface.Mode.DIGITAL_INPUT;
		return (gpioReg.get(reg) & (1 << shift));
	}
	
	@Override
	public void setMode(int gpio, GpioDeviceInterface.Mode mode) {
		int reg = gpio < C2_GPIOX_PIN_START ? C2_GPIOY_FSEL_REG_OFFSET : C2_GPIOX_FSEL_REG_OFFSET;
		int shift = C2_GP_TO_SHIFT_REG[gpio - C2_GPIOY_PIN_START];
		switch (mode) {
		case DIGITAL_INPUT:
			gpioReg.put(reg, gpioReg.get(reg) | (1 << shift));
			break;
		case DIGITAL_OUTPUT:
			gpioReg.put(reg, gpioReg.get(reg) & ~(1 << shift));
			break;
		default:
			throw new IllegalArgumentException("Invalid GPIO mode " + mode + " for pin " + gpio);
		}
	}
	
	@Override
	public void setPullUpDown(int gpio, GpioPullUpDown pud) {
		int shift = C2_GP_TO_SHIFT_REG[gpio - C2_GPIOY_PIN_START];
		int pud_en_reg = gpio < C2_GPIOX_PIN_START ? C2_GPIOY_PUEN_REG_OFFSET : C2_GPIOX_PUEN_REG_OFFSET;
		if (pud == GpioPullUpDown.NONE) {
			// Disable Pull/Pull-down resister
			gpioReg.put(pud_en_reg, gpioReg.get(pud_en_reg) & ~(1 << shift));
		} else {
			// Enable Pull/Pull-down resister
			gpioReg.put(pud_en_reg, gpioReg.get(pud_en_reg) | (1 << shift));
			int pud_reg = gpio < C2_GPIOX_PIN_START ? C2_GPIOY_PUPD_REG_OFFSET : C2_GPIOX_PUPD_REG_OFFSET;
			if (pud == GpioPullUpDown.PULL_UP) {
				gpioReg.put(pud_reg, gpioReg.get(pud_reg) |  (1 << shift));
			} else {
				gpioReg.put(pud_reg, gpioReg.get(pud_reg) & ~(1 << shift));
			}
		}
	}
	
	@Override
	public boolean gpioRead(int gpio) {
		int shift = C2_GP_TO_SHIFT_REG[gpio - C2_GPIOY_PIN_START];
		int gp_lev_reg = gpio < C2_GPIOX_PIN_START ? C2_GPIOY_INP_REG_OFFSET : C2_GPIOX_INP_REG_OFFSET;
		return (gpioReg.get(gp_lev_reg) & (1 << shift)) != 0;
	}
	
	@Override
	public void gpioWrite(int gpio, boolean value) {
		int shift = C2_GP_TO_SHIFT_REG[gpio - C2_GPIOY_PIN_START];
		int gp_set_reg = gpio < C2_GPIOX_PIN_START ? C2_GPIOY_OUTP_REG_OFFSET : C2_GPIOX_OUTP_REG_OFFSET;
		if (value) {
			gpioReg.put(gp_set_reg, gpioReg.get(gp_set_reg) | (1 << shift));
		} else {
			gpioReg.put(gp_set_reg, gpioReg.get(gp_set_reg) & ~(1 << shift));
		}
	}
	
	private static int gpioToGPSETReg(int gpio) {
		if (gpio >= C2_GPIOX_PIN_START && gpio <= C2_GPIOX_PIN_END) {
			return  C2_GPIOX_OUTP_REG_OFFSET;
		}
		if (gpio >= C2_GPIOY_PIN_START && gpio <= C2_GPIOY_PIN_END) {
			return C2_GPIOY_OUTP_REG_OFFSET;
		}
		return -1;
	}
	
	private static int gpioToShiftReg(int gpio) {
		if (gpio >= C2_GPIOX_PIN_START && gpio <= C2_GPIOX_PIN_END) {
			return gpio - C2_GPIOX_PIN_START;
		}
		if (gpio >= C2_GPIOY_PIN_START && gpio <= C2_GPIOY_PIN_END) {
			return gpio - C2_GPIOY_PIN_START;
		}
		return -1;
	}
	
	private static final int gpioToGPLEVReg(int gpio) {
		if (gpio >= C2_GPIOX_PIN_START && gpio <= C2_GPIOX_PIN_END) {
			return C2_GPIOX_INP_REG_OFFSET;
		}
		if (gpio >= C2_GPIOY_PIN_START && gpio <= C2_GPIOY_PIN_END) {
			return C2_GPIOY_INP_REG_OFFSET;
		}
		return -1;
	}
	
	private static final int gpioToGPFSELReg(int gpio) {
		if (gpio >= C2_GPIOX_PIN_START && gpio <= C2_GPIOX_PIN_END) {
			return C2_GPIOX_FSEL_REG_OFFSET;
		}
		if (gpio >= C2_GPIOY_PIN_START && gpio <= C2_GPIOY_PIN_END) {
			return C2_GPIOY_FSEL_REG_OFFSET;
		}
		return -1;
	}
	
	public static void main(String[] args) throws InterruptedException {
		System.out.println(ByteOrder.nativeOrder());
		if (args.length != 2) {
			System.out.println("Usage: " + OdroidC2MmapGpio.class.getName() + " <gpio> <iterations>");
			System.exit(1);
		}
		int gpio = Integer.parseInt(args[0]);
		int iterations = Integer.parseInt(args[1]);
		
		int gp_set_reg = gpio < C2_GPIOX_PIN_START ? C2_GPIOY_OUTP_REG_OFFSET : C2_GPIOX_OUTP_REG_OFFSET;
		int gp_set_reg2 = gpioToGPSETReg(gpio);
		System.out.println("gp_set_reg=" + gp_set_reg + ", gp_set_reg2=" + gp_set_reg2);
		int shift = C2_GP_TO_SHIFT_REG[gpio - C2_GPIOY_PIN_START];
		int shift2 = gpioToShiftReg(gpio);
		System.out.println("shift=" + shift + ", shift2=" + shift2);
		int gp_lev_reg = gpio < C2_GPIOX_PIN_START ? C2_GPIOY_INP_REG_OFFSET : C2_GPIOX_INP_REG_OFFSET;
		int gp_lev_reg2 = gpioToGPLEVReg(gpio);
		System.out.println("gp_lev_reg=" + gp_lev_reg + ", gp_lev_reg2=" + gp_lev_reg2);
		int gp_fsel_reg = gpio < C2_GPIOX_PIN_START ? C2_GPIOY_FSEL_REG_OFFSET : C2_GPIOX_FSEL_REG_OFFSET;
		int gp_fsel_reg2 = gpioToGPFSELReg(gpio);
		System.out.println("gp_fsel_reg=" + gp_fsel_reg + ", gp_fsel_reg2=" + gp_fsel_reg2);
		
		System.out.format("gpioToGPSETReg(%d)=0x%04x%n", 214, gpioToGPSETReg(214));
		System.out.format("gpioToGPSETReg(%d)=0x%04x%n", 219, gpioToGPSETReg(219));
		System.out.format("gpioToGPFSELReg(%d)=0x%04x%n", 214, gpioToGPFSELReg(214));
		System.out.format("gpioToGPFSELReg(%d)=0x%04x%n", 219, gpioToGPFSELReg(219));
		
		OdroidC2MmapGpio mmap_gpio = new OdroidC2MmapGpio();
		
		mmap_gpio.initialise();

		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		try {
			while (true) {
				MemoryInspector.dumpMemory(mmap_gpio.gpioReg, C2_GPIO_PIN_BASE, 200);
				String line = reader.readLine();
				if (line == null || line.equals("q")) {
					break;
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("getMode(" + gpio + ")=" + mmap_gpio.getMode(gpio));
		mmap_gpio.setMode(gpio, GpioDeviceInterface.Mode.DIGITAL_OUTPUT);
		System.out.println("getMode(" + gpio + ")=" + mmap_gpio.getMode(gpio));

		System.out.println("Current val=" + mmap_gpio.gpioRead(gpio));
		for (int i=0; i<5; i++) {
			System.out.println("on");
			mmap_gpio.gpioWrite(gpio, true);
			System.out.println("Current val=" + mmap_gpio.gpioRead(gpio));
			SleepUtil.sleepSeconds(1);
			System.out.println("off");
			mmap_gpio.gpioWrite(gpio, false);
			System.out.println("Current val=" + mmap_gpio.gpioRead(gpio));
			SleepUtil.sleepSeconds(1);
		}
		
		if (false) {
			System.exit(1);
		}
		
		if (true) {
			long start = System.currentTimeMillis();
			for (int i=0; i<iterations; i++) {
				mmap_gpio.gpioWrite(gpio, true);
				mmap_gpio.gpioWrite(gpio, false);
			}
			long duration = System.currentTimeMillis() - start;
			System.out.format("Took %d ms for %d iterations, frequency=%.2fkHz%n",
					Long.valueOf(duration), Integer.valueOf(iterations), Double.valueOf(iterations/(double) duration));
		}

		for (int i=0; i<5; i++) {
			System.out.println("on");
			mmap_gpio.gpioWrite(gpio, true);
			SleepUtil.sleepSeconds(1);
			System.out.println("off");
			mmap_gpio.gpioWrite(gpio, false);
			SleepUtil.sleepSeconds(1);
		}
		
		mmap_gpio.terminate();
	}
	
	public static void test() {
		Random rand = new Random();
		IntBuffer buffer = ByteBuffer.allocateDirect(500).asIntBuffer();
		for (int i=0; i<buffer.capacity(); i++) {
			buffer.put(rand.nextInt());
		}
		buffer.flip();
		MemoryInspector.dumpMemory(buffer, 0, 2);
	}
}
