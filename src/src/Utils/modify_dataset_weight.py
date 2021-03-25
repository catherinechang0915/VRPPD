import os

mem = '0.3'
new_weight = '5.0'
folder_base = os.getcwd() + '\\data\\pdp_100_mem_' + mem + '\\'
folder_in = folder_base + '1.0_1.0\\'
folder_out = folder_base + new_weight + '_1.0\\'

files_in = os.listdir(folder_in)

for data_file in files_in:
    p_read = folder_in + data_file
    p_write = folder_out + data_file
    f_read = open(p_read, 'r')
    f_write = open(p_write, 'w')
    lines = f_read.readlines()
    lines[-2] = 'alpha = ' + new_weight + ';\n'
    f_write.writelines(lines)
    f_read.close()
    f_write.close()

