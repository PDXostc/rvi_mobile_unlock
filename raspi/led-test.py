#!/usr/bin/python
import time
import RPi.GPIO as GPIO

GPIO.setmode(GPIO.BCM)
#GPIO.setmode(GPIO.BOARD)
GPIO.setup(13, GPIO.OUT)
GPIO.setup(11, GPIO.OUT)
print "Start : %s" % time.ctime()
GPIO.output(13, GPIO.HIGH)
GPIO.output(11, GPIO.HIGH)
time.sleep( 5 )
GPIO.output(13, GPIO.LOW)
GPIO.output(11, GPIO.LOW)
print "End : %s" % time.ctime()
GPIO.cleanup()
