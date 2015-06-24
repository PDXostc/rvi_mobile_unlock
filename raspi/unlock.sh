#!/bin/sh

/home/pi/btserver/bin/rvi start
sleep 20
while true
do
	(cd /home/pi/unlock; python fob.py)
	sleep 5
done

