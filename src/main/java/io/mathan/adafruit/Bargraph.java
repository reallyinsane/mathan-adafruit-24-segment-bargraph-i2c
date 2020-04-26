package io.mathan.adafruit;

import java.util.Arrays;
import java.util.List;
import net.sf.yad2xx.FTDIException;
import net.sf.yad2xx.mpsse.I2C;

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

  public Bargraph(I2C device) throws FTDIException {
    this(device, ADDRESS_DEFAULT);
  }

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

  public void clear() throws FTDIException {
    displaybuffer[0] = 0;
    displaybuffer[1] = 0;
    displaybuffer[2] = 0;
    displaybuffer[3] = 0;
    displaybuffer[4] = 0;
    displaybuffer[5] = 0;
    update();
  }

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

  public void setBar(int bar, Color color) {
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
