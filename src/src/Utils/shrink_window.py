import os

dic = {}
f = open(os.getcwd() + '\\shrink_window_percent3.txt', 'r')
while True:
    line = f.readline()
    if not line:
        break
    pair = line[:-1].split(' ')
    dic[pair[0][:-4]] = float(pair[1])
f.close()

folder_root = os.path.dirname(os.path.dirname(os.path.dirname(os.getcwd())))
files = os.listdir(folder_root + '\\raw_data\\pdp_100')
for data_file in files:
    p_read = folder_root + '\\raw_data_originTW\\pdp_100\\' + data_file
    p_write = folder_root + '\\raw_data\\pdp_100\\' + data_file
    shrinkPercent = dic[data_file[:-4]]
    f_read = open(p_read, 'r')
    f_write = open(p_write, 'w')
    lines = f_read.readlines()
    lines_mod = [lines[0]]

    # deports
    line = lines[1]
    node_info = line.split()
    tw_width = float(node_info[5]) - float(node_info[4])
    # node_info[4] = '{0:.5f}'.format(float(node_info[4]) + shrinkPercent * tw_width * 0.1)
    node_info[4] = '{0:.5f}'.format(float(node_info[4]))
    node_info[5] = '{0:.5f}'.format(float(node_info[5]) - shrinkPercent * tw_width * 0.1)
    node_info.append('\n')
    lines_mod.append('\t'.join(node_info))

    for line in lines[2:]:
        node_info = line.split()
        tw_width = float(node_info[5]) - float(node_info[4])
        node_info[4] = '{0:.5f}'.format(float(node_info[4]) + shrinkPercent * tw_width)
        node_info[5] = '{0:.5f}'.format(float(node_info[5]) - shrinkPercent * tw_width)
        node_info.append('\n')
        lines_mod.append('\t'.join(node_info))
    f_write.writelines(lines_mod)
    f_read.close()
    f_write.close()