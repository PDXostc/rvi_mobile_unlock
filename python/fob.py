#!/usr/bin/python

#
# Copyright (C) 2015, Jaguar Land Rover
#
# This program is licensed under the terms and conditions of the
# Mozilla Public License, version 2.0.  The full text of the 
# Mozilla Public License is at https://www.mozilla.org/MPL/2.0/
#
#
# Register a service specified by command line with an RVI node.
# Print out a message when the service gets invoked.
#
import sys
import json
from rvilib import RVI
import getopt
import time
import RPi.GPIO as GPIO
from subprocess import call
from os import system
from datetime import datetime
import threading

GPIO_UNLOCK = 5
GPIO_LOCK   = 6
GPIO_LIGHTS = 13
GPIO_TRUNK  = 19
GPIO_PANIC  = 26
GPIO_START  = 20
GPIO_STOP   = 21

threads = []

def usage():
    print "Usage:", sys.argv[0], "[-n <rvi_url>]"
    print "  <rvi_url>                     URL of Service Edge on a local RVI node."
    print "                                Default: http://localhost:8811"
    print
    print "The RVI Service Edge URL can be found in"
    print "[backend,vehicle].config as"
    print "env -> rvi -> components -> service_edge -> url"
    print
    print "The Service Edge URL is also logged as a notice when the"
    print "RVI node is started."
    print
    print "Example: ./fob.py -n http://rvi1.nginfotpdx.net:8801"
    sys.exit(255)


#
# Our general handler, registered with rvi.register_service() below.
#
# You can also explicitly name the arguments, but then
# the sender has to match the argument names.

# For example:
# rvi_call.py http://localhost:8801 jlr.com/bt/test a=1 b=2 c=3 ->
#    def service(a,b,c)
#
def unlock_invoked(**args):
    print "UNLOCK"
    payload = args
    t1 = threading.Thread(target=gpio_output_rising_edge, args=(GPIO_UNLOCK,))
    t2 = threading.Thread(target=log_invoked_service, args=(payload, 'Door Unlock',))
    t1.start()
    t2.start()
    return ['ok']


def lock_invoked(**args):
    print "LOCK"
    payload = args
    t1 = threading.Thread(target=gpio_output_rising_edge, args=(GPIO_LOCK,))
    t2 = threading.Thread(target=log_invoked_service, args=(payload, 'Door Lock',))
    t1.start()
    t2.start()
    return ['ok']

def auto_unlock_invoked(**args):
    print "AUTO UNLOCK"
    payload = args
    t1 = threading.Thread(target=gpio_output_rising_edge, args=(GPIO_UNLOCK,))
    t2 = threading.Thread(target=log_invoked_service, args=(payload, 'Auto Door Unlock',))
    t1.start()
    t2.start()
    return ['ok']


def auto_lock_invoked(**args):
    print "AUTO LOCK"
    payload = args
    t1 = threading.Thread(target=gpio_output_rising_edge, args=(GPIO_LOCK,))
    t2 = threading.Thread(target=log_invoked_service, args=(payload, 'Auto Door Lock',))
    t1.start()
    t2.start()
    return ['ok']

def lights_invoked(**args):
    payload = args
    t1 = threading.Thread(target=gpio_output_rising_edge, args=(GPIO_LIGHTS,))
    t2 = threading.Thread(target=log_invoked_service, args=(payload, 'Flash Lights',))
    t1.start()
    t2.start()
    return ['ok']


def trunk_invoked(**args):
    payload = args
    t1 = threading.Thread(target=gpio_output_rising_edge, args=(GPIO_TRUNK,))
    t2 = threading.Thread(target=log_invoked_service, args=(payload, 'Trunk Open',))
    t1.start()
    t2.start()
    return ['ok']


def start_invoked(**args):
    print "START"
    payload = args
    t1 = threading.Thread(target=gpio_output_rising_edge, args=(GPIO_START,))
    t2 = threading.Thread(target=log_invoked_service, args=(payload, 'Remote Start',))
    t1.start()
    t2.start()
    return ['ok']


def stop_invoked(**args):
    print "STOP"
    payload = args
    t1 = threading.Thread(target=gpio_output_rising_edge, args=(GPIO_STOP,))
    t2 = threading.Thread(target=log_invoked_service, args=(payload, 'Remote Stop',))
    t1.start()
    t2.start()
    return ['ok']


def horn_invoked(**args):
    print "Horn not supported"
    return ['ok']


def panic_invoked(**args):
    print "PANIC"
    payload = args
    t1 = threading.Thread(target=gpio_output_rising_edge, args=(GPIO_PANIC,))
    t2 = threading.Thread(target=log_invoked_service, args=(payload, 'Panic',))
    t1.start()
    t2.start()
    return ['ok']


def services_available(**args):
    print
    print "Services available!"
    print "args:", args 
    print
    return ['ok']


def services_unavailable(**args):
    print "Services unavailable!"
    # Lock the door when BT connection goes away
    lock_invoked() 
    sys.exit(0)
    return ['ok']


def gpio_output_falling_edge(gpio_pin):
    GPIO.output(gpio_pin, GPIO.LOW)
    time.sleep( 1 )
    GPIO.output(gpio_pin, GPIO.HIGH)

def gpio_output_rising_edge(gpio_pin):
    GPIO.output(gpio_pin, GPIO.HIGH)
    time.sleep( 1 )
    GPIO.output(gpio_pin, GPIO.LOW)

def log_invoked_service(payload, invoked_service):
    rvi_node = "http://localhost:8801"
    service = "jlr.com/backend/logging/report/serviceinvoked"

    print "args:", payload
    # json_args = json.loads(payload)

    # print "json_args:", json_args
    rvi_args = [{
            'username': payload['username'],
            'vehicleVIN': payload['vehicleVIN'],
            'service': invoked_service,
            'latitude': payload['latitude'],
            'longitude': payload['longitude'],
            'timestamp': datetime.now().strftime('%Y-%m-%dT%H:%M:%S.000Z'),
            },
        ]
    print "rvi_args:", rvi_args
    rvi = RVI(rvi_node)
    rvi.message(service, rvi_args)

# 
# Check that we have the correct arguments
#
opts, args = getopt.getopt(sys.argv[1:], "n:")

rvi_node_url = "http://localhost:8801"
for o, a in opts:
    if o == "-n":
        rvi_node_url = a
    else:
        usage()

if len(args) != 0:
    usage()


# Setup GPIO pins
GPIO.cleanup()
GPIO.setmode(GPIO.BCM)
GPIO.setup(GPIO_UNLOCK, GPIO.OUT)
GPIO.setup(GPIO_LOCK, GPIO.OUT)
GPIO.setup(GPIO_LIGHTS, GPIO.OUT)
GPIO.setup(GPIO_TRUNK, GPIO.OUT)
GPIO.setup(GPIO_PANIC, GPIO.OUT)
GPIO.setup(GPIO_START, GPIO.OUT)
GPIO.setup(GPIO_STOP, GPIO.OUT)

#
# Setup initial state
#
GPIO.output(GPIO_UNLOCK, GPIO.LOW)
GPIO.output(GPIO_LOCK, GPIO.LOW)
GPIO.output(GPIO_LIGHTS, GPIO.LOW)
GPIO.output(GPIO_TRUNK, GPIO.LOW)
GPIO.output(GPIO_PANIC, GPIO.LOW)
GPIO.output(GPIO_START, GPIO.LOW)
GPIO.output(GPIO_STOP, GPIO.LOW)

# Setup a connection to the local RVI node
rvi = RVI(rvi_node_url)

# Starting the thread that handles incoming calls is
# not really necessary since register_service will do it for us.

rvi.start_serve_thread() 

rvi.set_services_available_callback(services_available) 
rvi.set_services_unavailable_callback(services_unavailable) 

# Register our service  and invoke 'service_invoked' if we 
# get an incoming JSON-RPC call to it from the RVI node
#
rvi.register_service("unlock", unlock_invoked)
rvi.register_service("lock", lock_invoked)
rvi.register_service("auto_unlock", auto_unlock_invoked)
rvi.register_service("auto_lock", auto_lock_invoked)
rvi.register_service("start", start_invoked)
rvi.register_service("stop", stop_invoked)
# rvi.register_service("horn", horn_invoked)
rvi.register_service("trunk", trunk_invoked)
rvi.register_service("panic", panic_invoked) 
rvi.register_service("lights", lights_invoked) 

while True:
    time.sleep(36000)

sys.exit(0)
