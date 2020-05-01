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

import io.mathan.adafruit.Bargraph.Color;
import net.sf.yad2xx.Device;
import net.sf.yad2xx.FTDIException;
import net.sf.yad2xx.FTDIInterface;
import net.sf.yad2xx.mpsse.I2C;

public class Sample {

  public static void main(String[] args) throws FTDIException, InterruptedException {
    System.loadLibrary("FTDIInterface");
    // Get all available FTDI Devices
    Device[] devices = FTDIInterface.getDevices();
    Device device = devices[0];
    I2C d = new I2C(device);
    d.open();
    Bargraph b = new Bargraph(d);
    b.clear();
    // setting all bars to a color
    Color[] colors = {Color.GREEN, Color.RED, Color.YELLOW};
    for(int i=0;i<24;i++) {
      b.setBar((i+1), colors[i%3]);
      b.update();
      d.delay(200);
    }
    // demonstrating percentage usage
    for(int i=0;i<=100;i++) {
      Color color = i>80?Color.RED:i>50?Color.YELLOW:Color.GREEN;
      b.setPercentage((float)i/100, color);
      d.delay(200);
    }
    b.clear();
    d.close();
  }
}
