#!/usr/bin/python

import serial
import time

port = '/dev/ttyUSB0'

robot = serial.Serial(port,9600,timeout=5)

# time.sleep(3)

def move(filename):
  with open(filename, 'r') as f:
    for line in f:
      robot.write(line)
      robot.read(1)

#print "Executing"

value = raw_input()
while (len(value) > 0):
# value = raw_input()
  # print value
  robot.write(value)
  robot.read(1)
  value = raw_input()

# move("cafe-direita.txt")
# time.sleep(3)
# move("cafe-esquerda.txt")

# time.sleep(3)
# move("cafe-direita.txt")
# time.sleep(3)
# move("cafe-esquerda.txt")

exit()
