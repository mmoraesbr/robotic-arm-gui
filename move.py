#!/usr/bin/python

import serial

port = '/dev/ttyUSB0'

robotConn = serial.Serial(port,9600,timeout=5)

print "Ready"

value = raw_input()
while (len(value) > 0):
  robotConn.write(value)
  robotConn.read(1)
  value = raw_input()

exit()
