<a href="https://opensource.org/licenses/Apache-2.0"><img src="https://img.shields.io/badge/license-apache2-blue.svg"></a>

# yad2xx extension for Adafruit 24 segment [bargraph] using I2C

This is Java API extension to [yad2xx]. The colors of the bars can be controlled with a simple API. 

```
// configure an I2C instance
I2C device;
// create a Bargraph instance
Bargraph bargraph = new Bargraph(device);
// clear the display
bargraph.clear();
// change the color of a bar
bargraph.setBar(1, Color.RED);
bargraph.setBar(1, Color.GREEN);
bargraph.setBar(1, Color.YELLOW);
bargraph.setBar(1, Color.OFF);
// change a number of bars using a percentage setting, e.g. setting bar 1-12 to green
bargraph.setPercentage(0.5, Color.GREEN);
```
## yad2xx linking

This project is using the yad2xx library released under LGPL3. The dynamic linking is done using Maven dependency mechanism. To change the linking to
a different version of the library just change the dependency. A copy of the library binary and license is available in the lib folder. A source artifact
has not been provided by the publisher of the library. You can find the source code on the SourceForge project page of [yad2xx].


## Chip details

The Adafruit 24 segment [bargraph] is using a HT16K33 chip of [holtek]. The chip documentation can be found in the doc folder.

[bargraph]: https://www.adafruit.com/product/1721
[yad2xx]: https://sourceforge.net/projects/yad2xx
[holtek]: https://holtek.com
