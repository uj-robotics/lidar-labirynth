"""
Get Data from Neato LIDAR using pyserial
========================================

Data format for firmware V2.4 and v2.6 (recent production units)

A full revolution will yield 90 packets, containing 4 consecutive readings each.
The length of a packet is 22 bytes. This amounts to a total of 360 readings (1 per degree) on 1980 
bytes.

Each packet is organized as follows:
<start> <index> <speed_L> <speed_H> [Data 0] [Data 1] [Data 2] [Data 3] <checksum_L> <checksum_H>

where:

<start> is always 0xFA

<index> is the index byte in the 90 packets, going from 0xA0 (packet 0, readings 0 to 3) to 0xF9 
(packet 89, readings 356 to 359).

<speed>> is a two-byte information, little-endian. It represents the speed, in 64th of RPM (aka 
    value in RPM represented in fixed point, with 6 bits used for the decimal part).

[Data 0] to [Data 3] are the 4 readings. Each one is 4 bytes long, and organized as follows :

`byte 0 : <distance 7:0>`
`byte 1 : <"invalid data" flag> <"strength warning" flag> <distance 13:8>`
`byte 2 : <signal strength 7:0>`
`byte 3 : <signal strength 15:8>`

The distance information is in mm, and coded on 14 bits. The minimum distance is around 15cm, and 
the maximum distance is around 6m.

When bit 7 of byte 1 is set, it indicates that the distance could not be calculated. When this 
bit is set, it seems that byte 0 contains an error code. Examples of error code are 0x02, 0x03, 0x21, 
0x25, 0x35 or 0x50... When it's `21`, then the whole block is `21 80 XX XX`, but for all the other 
values it's the data block is `YY 80 00 00`...

The bit 6 of byte 1 is a warning when the reported strength is greatly inferior to what is expected 
at this distance. This may happen when the material has a low reflectance (black material...), or 
when the dot does not have the expected size or shape (porous material, transparent fabric, grid, 
edge of an object...), or maybe when there are parasitic reflections (glass... ).

Byte 2 and 3 are the LSB and MSB of the strength indication. This value can get very high when facing 
a retroreflector.

<checksum> is a two-byte checksum of the packet. The algorithm is in checksum(), provided that 
`data` is the list of the 20 first bytes, in the same order they arrived in.

"""

import logging
import math
import serial
import time

from network import network

com_port = "/dev/ttyUSB0"
baudrate = 115200

init_level = 0
index = 0

lidarData = [[] for i in range(360)]  # A list of 360 elements Angle, Distance , quality


def checksum(data):
    """Compute and return the checksum as an int.

data -- list of 20 bytes, in the order they arrived in.
"""
    # make it into a list of 20 integers
    data = list(data)
    # group the data by word, little-endian
    data_list = []
    for t in range(10):
        data_list.append(data[2 * t] + (data[2 * t + 1] << 8))

    # compute the checksum on 32 bits
    chk32 = 0
    for d in data_list:
        chk32 = (chk32 << 1) + d

    # return a value wrapped around on 15bits, and truncated to still fit into 15 bits
    checksum = (chk32 & 0x7FFF) + (chk32 >> 15)  # wrap around to fit into 15 bits
    checksum = checksum & 0x7FFF  # truncate to 15 bits
    return int(checksum)


def readLidar():
    logging.basicConfig(filename="lidar.log", level=logging.DEBUG)

    global init_level, index

    nb_errors = 0
    while True:
        # try:
        time.sleep(0.00001)  # do not hog the processor power

        if init_level == 0:
            b_start = ser.read(1)
            logging.info("b_start = {0}".format(b_start))
            b = int.from_bytes(b_start, 'big')
            # start byte
            if b == 0xFA:
                init_level = 1
                logging.info("got start\n")
            else:
                init_level = 0
        elif init_level == 1:
            # position index
            b_index = ser.read(1)
            logging.info("b_index = {0}".format(b_index))
            b = int.from_bytes(b_index, 'big')
            if b >= 0xA0 and b <= 0xF9:
                index = b - 0xA0
                init_level = 2
                logging.info("got packet #{0:02x}\n".format(index))
            elif b != 0xFA:
                init_level = 0
        elif init_level == 2:
            # speed
            b_speed = ser.read(2)

            # data
            b_data0 = ser.read(4)
            b_data1 = ser.read(4)
            b_data2 = ser.read(4)
            b_data3 = ser.read(4)

            # print("data\n")
            # print("data0 = {0}".format(b_data0))
            # print("data1 = {0}".format(b_data1))
            # print("data2 = {0}".format(b_data2))
            # print("data3 = {0}".format(b_data3))

            # we need all the data of the packet to verify the checksum
            all_data = b_start + b_index + b_speed + b_data0 + b_data1 + b_data2 + b_data3
            logging.info("all_data = {0}".format(all_data))

            # checksum
            b_checksum = ser.read(2)
            logging.info("checksum bytes: {0}".format(b_checksum))
            # incoming_checksum = int(b_checksum[0]) + (int(b_checksum[1]) << 8)
            incoming_checksum = int.from_bytes(b_checksum, 'little')
            logging.info("incoming_checksum = {0}".format(incoming_checksum))

            # verify that the received checksum is equal to the one computed from the data
            calculated_checksum = checksum(all_data)
            logging.info("calculated_checksum = {0}".format(calculated_checksum))
            if checksum(all_data) == incoming_checksum:
                speed_rpm = float(int.from_bytes(b_speed, 'big')) / 64.0
                logging.info("speed = {0}".format(speed_rpm))
                # if visualization:
                #     gui_update_speed(speed_rpm)

                speed_data0 = update_view(index * 4 + 0, b_data0)
                speed_data1 = update_view(index * 4 + 1, b_data1)
                speed_data2 = update_view(index * 4 + 2, b_data2)
                speed_data3 = update_view(index * 4 + 3, b_data3)

                logging.info("Size Speed {0}".format(len(str(speed_rpm))))

                # send data
                data_to_send = [b_index, b_speed, b_data0, b_data1, b_data2, b_data3]

                debug_out(*data_to_send)

                network.send_data(data_to_send)

            else:
                # the checksum does not match, something went wrong...
                nb_errors += 1
                logging.error("wrong checksum nb_errors={0}\n".format(nb_errors))

            init_level = 0  # reset and wait for the next packet
            logging.info(index)
            logging.info(lidarData)
            # return # to test

        else:  # default, should never happen...
            init_level = 0
            # except :
            #     traceback.print_exc(file=sys.stdout)
            #     return


def debug_out(b_index, b_speed, b_data0, b_data1, b_data2, b_data3):
    logging.debug("DEBUG")

    logging.debug("index\n")
    logging.debug("index = {0}".format(b_index))

    logging.debug("speed\n")
    logging.debug("data0 = {0}".format(b_speed))

    logging.debug("data\n")
    logging.debug("data0 = {0}".format(b_data0))
    logging.debug("data1 = {0}".format(b_data1))
    logging.debug("data2 = {0}".format(b_data2))
    logging.debug("data3 = {0}".format(b_data3))


def update_view(angle, data):
    """Updates the view of a sample.

Takes the angle (an int, from 0 to 359) and the list of four bytes of data in the order they arrived.
"""

    angle_rad = angle * math.pi / 180.0
    c = math.cos(angle_rad)
    s = -math.sin(angle_rad)

    dist_calc_error = (data[1] & 0x80) > 0  # check bit 7 flag
    if dist_calc_error:
        logging.error("distance calculation error: {0}\n".format(data[0]))  # error code in data[0]

    inferior_signal = (data[1] & 0x40) > 0  # check bit 6 flag
    if inferior_signal:
        logging.error("inferior signal\n")

    dist_mm = data[0] | ((data[1] & 0x3f) << 8)  # remove the flags
    quality = data[2] | (data[3] << 8)  # quality is on 16 bits
    # quality = int.from_bytes(data[2:4], 'big')
    lidarData[angle] = [dist_mm, quality]
    dist_x = dist_mm * c
    dist_y = dist_mm * s

    logging.debug("X Distance {0}".format(dist_x))
    logging.debug("Y Distance {0}".format(dist_y))
    logging.debug("Distance {0}".format(dist_mm))

    return [quality, dist_mm, dist_x, dist_y]


ser = serial.Serial(com_port, baudrate)
# th = threading.Thread(target=read_Lidar)
# th.start()
readLidar()
