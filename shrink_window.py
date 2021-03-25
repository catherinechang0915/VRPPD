import os

dic = {}
f = open(os.getcwd() + '\\temp.txt', 'r')
while True:
    line = f.readline()
    if not line:
        break
    pair = line[:-1].split(' ')
    dic[pair[0][:-4]] = float(pair[1])
f.close()


files = os.listdir(os.getcwd() + '\\raw_data\\pdp_100')
for data_file in files:
    p_read = os.getcwd() + '\\raw_data_originTW\\pdp_100\\' + data_file
    p_write = os.getcwd() + '\\raw_data\\pdp_100\\' + data_file
    shrinkPercent = dic[data_file[:-4]]
    f_read = open(p_read, 'r')
    f_write = open(p_write, 'w')
    lines = f_read.readlines()
    lines_mod = [lines[0][:-1]]
    for line in lines[1:]:
        node_info = line.split()
        tw_width = float(node_info[5]) - float(node_info[4])
        node_info[4] = str(float(node_info[4]) + shrinkPercent * tw_width)
        node_info[5] = str(float(node_info[5]) - shrinkPercent * tw_width)
        lines_mod.append('\t'.join(node_info))
    for line in lines_mod:
        f_write.write(line)
        f_write.write('\n')
    f_read.close()
    f_write.close()