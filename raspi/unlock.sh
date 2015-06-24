#!/bin/sh

hciconfig hci0 piscan leadv
hcitool -i hci0 cmd 0x08 0x0008 1E 02 01 1A 1A FF 4C 00 02 15 92 77 83 0A B2 EB 49 0F A1 DD 7F E3 8C 49 2E DE 00 00 00 00 C5 00

/home/pi/btserver/bin/rvi start
sleep 20
while true
do
	(cd /home/pi/unlock; python fob.py)
	sleep 5
done

