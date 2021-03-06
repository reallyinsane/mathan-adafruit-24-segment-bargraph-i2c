/*
 * mathan-adafruit-24-segment-bargraph-i2c
 * Copyright (c) 2020 Matthias Hanisch
 * matthias@mathan.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.mathan.adafruit;

import java.util.Arrays;
import java.util.List;
import net.sf.yad2xx.FTDIException;
import net.sf.yad2xx.mpsse.I2C;

/**
 * Using the Bargraph class you can control the 24 bars of the Adafruit 24-segment bargraph. Each bar can have one one of the following colors: red, green, yellow (red+green) or off. You need to
 * initialize a {@link I2C I2C-Device}. Then you can create an instance of Bargraph either with <code>new Bargraph(device)</code> or <code>new Bargraph(device, address)</code>. Use the latter if you
 * need to specify a non-default address for the device. The default address is <code>0x70</code>. Once initializes you can change the color of a bar using {@link #setBar(int, Color)}.
 *
 */
public class Bargraph {

  private static final byte ADDRESS_DEFAULT = 0x70;

  private static final byte CMD_SYSTEM_OFF = (byte) 0x20;
  private static final byte CMD_SYSTEM_ON = (byte) 0x21;
  private static final byte CMD_ROW_OUTPUT = (byte) 0xA0;
  private static final byte CMD_DATA = (byte) 0x00;
  private static final byte CMD_DISPLAY_OFF = (byte) 0x80;
  private static final byte CMD_DISPLAY_ON = (byte) 0x81;
  private static final byte CMD_DISPLAY_BLINK_2HZ = (byte) 0x83;
  private static final byte CMD_DISPLAY_BLINK_1HZ = (byte) 0x85;
  private static final byte CMD_DISPLAY_BLINK_HALF_HZ = (byte) 0x87;
  private static final byte CMD_NO_DIMMING = (byte) 0xEF;
  private static final byte CMD_INT_FLAG_DEFAULT = (byte) 0x60;

  /**
   * The data for the two colors RED and GREEN are encoded in 6 bytes. Combination of RED and GREEN become YELLOW.
   * The encoding is like this: (please note that the encoding for GREEN follows the encoding for RED in the next byte)
   *
   * G1 - Green for Bar 1
   * R24- Red for Bar 24
   *
   * +-------------------------------------------------+
   * |bit      |   8|   7|   6|   5|   4|   3|   2|   1|
   * +---------+----+----+----+----+----+----+----+----+
   * |byte 1   | R16| R15| R14| R13|  R4|  R3|  R2|  R1|
   * +---------+----+----+----+----+----+----+----+----+
   * |byte 2   | G16| G15| G14| G13|  G4|  G3|  G2|  G1|
   * +---------+----+----+----+----+----+----+----+----+
   * |byte 3   | R20| R19| R18| R17|  R8|  R7|  R6|  R5|
   * +---------+----+----+----+----+----+----+----+----+
   * |byte 4   | R20| R19| R18| R17|  R8|  R7|  R6|  R5|
   * +---------+----+----+----+----+----+----+----+----+
   * |byte 5   | R24| R23| R22| R21| R12| R11| R10|  R9|
   * +---------+----+----+----+----+----+----+----+----+
   * |byte 6   | R24| R23| R22| R21| R12| R11| R10|  R9|
   * +---------+----+----+----+----+----+----+----+----+
   */
  private static final List<Bar> BARS = Arrays.asList(
      Bar.create(0, (byte) 0x01),
      Bar.create(0, (byte) 0x02),
      Bar.create(0, (byte) 0x04),
      Bar.create(0, (byte) 0x08),
      Bar.create(2, (byte) 0x01),
      Bar.create(2, (byte) 0x02),
      Bar.create(2, (byte) 0x04),
      Bar.create(2, (byte) 0x08),
      Bar.create(4, (byte) 0x01),
      Bar.create(4, (byte) 0x02),
      Bar.create(4, (byte) 0x04),
      Bar.create(4, (byte) 0x08),
      Bar.create(0, (byte) 0x10),
      Bar.create(0, (byte) 0x20),
      Bar.create(0, (byte) 0x40),
      Bar.create(0, (byte) 0x80),
      Bar.create(2, (byte) 0x10),
      Bar.create(2, (byte) 0x20),
      Bar.create(2, (byte) 0x40),
      Bar.create(2, (byte) 0x80),
      Bar.create(4, (byte) 0x10),
      Bar.create(4, (byte) 0x20),
      Bar.create(4, (byte) 0x40),
      Bar.create(4, (byte) 0x80)
  );

  private final I2C device;
  private final byte address;
  private final byte[] displaybuffer = new byte[6];

  /**
   * Creates an instance of Bargraph using the specified I2C device and the default address.
   */
  public Bargraph(I2C device) throws FTDIException {
    this(device, ADDRESS_DEFAULT);
  }

  /**
   * Creates an instance of Bargraph using the specified I2C device and a custom address.
   */
  public Bargraph(I2C device, byte address) throws FTDIException {
    this.address = address;
    this.device = device;
    this.device.transactWrite(this.address, CMD_SYSTEM_ON);
    this.device.delay(200);
    this.device.transactWrite(this.address, CMD_ROW_OUTPUT);
    this.device.delay(200);
    this.device.transactWrite(this.address, CMD_INT_FLAG_DEFAULT);
    this.device.delay(200);
    this.device.transactWrite(this.address, CMD_DISPLAY_ON);
    this.device.delay(200);
    this.device.transactWrite(this.address, CMD_NO_DIMMING);
    this.device.delay(200);
    clear();
  }

  /**
   * Clears all bars, setting them to off and updates the display.
   */
  public void clear() throws FTDIException {
    displaybuffer[0] = 0;
    displaybuffer[1] = 0;
    displaybuffer[2] = 0;
    displaybuffer[3] = 0;
    displaybuffer[4] = 0;
    displaybuffer[5] = 0;
    update();
  }

  /**
   * Updates the display with all pending changes.
   */
  public void update() throws FTDIException {
    this.device.transactWrite(this.address,
        CMD_DATA,
        displaybuffer[0],
        displaybuffer[1],
        displaybuffer[2],
        displaybuffer[3],
        displaybuffer[4],
        displaybuffer[5]
    );
  }

  /**
   * Changes the color of a certain bar. Please note that the display is not updated immediately, you have to call {@link #update()} so that the changes will be displayed.
   * @param bar The number of the bar, 1-24.
   * @param color The new color of the bar.
   */
  public void setBar(int bar, Color color) {
    if (bar<1 || bar>24) {
      return;
    }
    if(color==null) {
      color = Color.OFF;
    }
    Bar b = BARS.get(bar-1);
    switch (color) {
      case RED:
        on(b.index, b.mask);
        off(b.index + 1, b.mask);
        break;
      case GREEN:
        on(b.index + 1, b.mask);
        off(b.index, b.mask);
        break;
      case YELLOW:
        on(b.index, b.mask);
        on(b.index + 1, b.mask);
        break;
      case OFF:
        off(b.index, b.mask);
        off(b.index + 1, b.mask);
        break;
    }
  }

  /**
   * Changes the color of all bars. The percentage given will make up the bars to illuminate with the given color - starting with bar 1. All other bars will be set to {@link Color#OFF}. E.g. calling
   * <code>setPercentage(0.5, Color.RED)</code> will set bar 1 to 12 to red and bar 13 to 24 to off.
   * @param percentage The percentage of bars to set to the given color.
   * @param color The color to set.
   */
  public void setPercentage(float percentage, Color color) throws FTDIException {
    if(percentage<0) {
      percentage=0;
    }
    if(percentage>1) {
      percentage = 1;
    }
    if(color==null) {
      color = Color.OFF;
    }
    int bars = (int) (24*percentage);
    for(int i=0;i<bars;i++) {
      setBar((i+1), color);
    }
    for(int i=bars;i<24;i++) {
      setBar((i+1), Color.OFF);
    }
    update();
  }

  private void on(int index, byte mask) {
    displaybuffer[index] |= mask;
  }

  private void off(int index, byte mask) {
    displaybuffer[index] &= ~mask;
  }

  public enum Color {
    RED,
    YELLOW,
    GREEN,
    OFF;
  }

  static class Bar {
    int index;
    byte mask;
    static Bar create(int index, byte mask) {
      Bar b = new Bar();
      b.index = index;
      b.mask = mask;
      return b;
    }
  }
}
