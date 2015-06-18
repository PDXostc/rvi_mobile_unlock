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
from rvilib import RVI
import getopt
import time
import RPi.GPIO as GPIO
from subprocess import call

GPIO_UNLOCK = 5
GPIO_LOCK   = 6
GPIO_LIGHTS = 13
GPIO_TRUNK  = 19
GPIO_PANIC  = 26


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
    GPIO.output(GPIO_UNLOCK, GPIO.HIGH)
    time.sleep( 0.3 ) 
    GPIO.output(GPIO_UNLOCK, GPIO.LOW)
    return ['ok']

def lock_invoked(**args):
    GPIO.output(GPIO_LOCK, GPIO.HIGH)
    time.sleep( 0.3 ) 
    GPIO.output(GPIO_LOCK, GPIO.LOW)
    return ['ok']

def lights_invoked(**args):
    GPIO.output(GPIO_LIGHTS, GPIO.HIGH)
    time.sleep( 0.3 ) 
    GPIO.output(GPIO_LIGHTS, GPIO.LOW)
    return ['ok']

def trunk_invoked(**args):
    GPIO.output(GPIO_TRUNK, GPIO.HIGH)
    time.sleep( 0.3 ) 
    GPIO.output(GPIO_TRUNK, GPIO.LOW)
    return ['ok']

def start_invoked(**args):
    print "Start not supported"
    return ['ok']

def stop_invoked(**args):
    print "Stop not supported"
    return ['ok']

def horn_invoked(**args):
    print "Horn not supported"
    return ['ok']


def panic_invoked(**args):
    GPIO.output(GPIO_PANIC, GPIO.HIGH)
    time.sleep( 0.3 )  # Longer?
    GPIO.output(GPIO_PANIC, GPIO.LOW)
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
    return ['ok']


# 
# Check that we have the correct arguments
#
opts, args= getopt.getopt(sys.argv[1:], "n:")

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
GPIO.setup(GPIO_UNLOCK, GPIO.OUT)  # unlock
GPIO.setup(GPIO_LOCK, GPIO.OUT)  # lock
GPIO.setup(GPIO_LIGHTS, GPIO.OUT)  # lights
GPIO.setup(GPIO_TRUNK, GPIO.OUT)  # trunk (weird)
GPIO.setup(GPIO_PANIC, GPIO.OUT)  # panic

#
# Setup initial state
#
GPIO.output(GPIO_UNLOCK, GPIO.LOW)
GPIO.output(GPIO_LOCK, GPIO.LOW)
GPIO.output(GPIO_LIGHTS, GPIO.LOW)
GPIO.output(GPIO_TRUNK, GPIO.LOW)
GPIO.output(GPIO_PANIC, GPIO.LOW)

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
rvi.register_service("start", start_invoked) 
rvi.register_service("stop", stop_invoked) 
rvi.register_service("horn", horn_invoked) 
rvi.register_service("trunk", trunk_invoked) 
rvi.register_service("panic", panic_invoked) 
rvi.register_service("lights", lights_invoked) 

while True:
    time.sleep(36000)

sys.exit(0)
