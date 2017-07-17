#!/usr/bin/python

import serial
import time

port = '/dev/ttyUSB0'

robot = serial.Serial(port,9600,timeout=5)

def move(filename):
  with open(filename, 'r') as f:
    for line in f:
      robot.write(line)
      robot.read(1)

value = raw_input()
while (len(value) > 0):
  robot.write(value)
  robot.read(1)

exit()
