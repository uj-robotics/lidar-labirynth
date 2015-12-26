import socket


def send_data(data):

    #['index'] = data[0]  # 1 byte
    #['speed'] = (data[1])  # 2 byte

    #['data0'] = (data[2])  # 4 byte
    #quality, dist_mm, dist_x, dist_y

    #['data1'] = (data[3])  # 4 byte
    #quality, dist_mm, dist_x, dist_y

    #['data2'] = (data[4])  # 4 byte
    #quality, dist_mm, dist_x, dist_y

    #['data3'] = (data[5])  # 4 byte
    #quality, dist_mm, dist_x, dist_y

    datagram_sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

    datagram_sock.sendto(data[0] + data[1] + data[2] + data[3] + data[4] + data[5], ("192.168.12.157", 1080))
    #datagram_sock.close()

