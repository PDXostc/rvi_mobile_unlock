# rvi_mobile_unlock
RVI Mobile Unlock demo android app.

App that will discover and iBeacon and try to establish a BT RFCOMM socket to the car.
On the car side right now there is a Raspberry PI executing the Erlang RVI stack.
The PI has to have BT scanning on and also BT LE announcment of the Beacon

```
sudo hciconfig hci0 piscan leadv
sudo hcitool -i hci0 cmd 0x08 0x0008 1E 02 01 1A 1A FF 4C 00 02 15 92 77 83 0A B2 EB 49 0F A1 DD 7F E3 8C 49 2E DE 00 00 00 00 C5 00
```

Note that the Beacon will change. Right now is a proper iBeacon and can be detected with any apps.
One good in particular is the Locate App
[iOS App](https://itunes.apple.com/us/app/locate-beacon/id738709014?mt=8) or 
[Android App](https://play.google.com/store/apps/details?id=com.radiusnetworks.locate&hl=en)


# INSTALL
Raspbian 
rvi_0.4.0
fob.py
jsonrpclib + rvilib.py
apt-get install  bluez
/etc/rc.local
/etc/bluetooth/main.conf Add
  DisablePlugins = pnat



# TO BUILD
sudo apt-get update
apt-get install libbluetooth-dev
apt-get install git
apt-get install libssl-dev
apt-get install libncurses-dev
Unpack OTP R16B03
./configure --prefix=/usr
make
make install
