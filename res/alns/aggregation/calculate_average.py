import os
import numpy as np

cwd = os.getcwd()

val = 3
if val == 1: # vehicle
    start_pos = 25
    end_pos = 40
elif val == 2: # objective
    start_pos = 40
    end_pos = 55
elif val == 3: # distance
    start_pos = 55
    end_pos = 70
elif val == 4: # delay
    start_pos = 70
    end_pos = 85


mem_map = {'0.3': 0, '0.5': 1, '0.7': 2}
coeff_map = {'1.0_3.0': 0, '1.0_1.0': 1, '3.0_1.0': 2}

file_in = open('aggregation.txt')
lines = file_in.readlines()

file_out_mem = open('group_mem.txt', 'w+')
group_mem_mat = np.empty((3, 3))
'''
    0.3 0.5 0.7
1
2
3
'''
file_out_coeff = open('group_coeff.txt', 'w+')
group_coeff_mat = np.empty((3, 3))

for line in lines:
    identifier = line[:25]
    row = int(identifier[0]) - 1
    for mem_key in mem_map.keys():
        if mem_key in identifier:
            col_mem = mem_map[mem_key]
    if col_mem != 2:
        continue
    for coeff_key in coeff_map.keys():
        if coeff_key in identifier:
            col_coeff = coeff_map[coeff_key]
    group_mem_mat[row][col_mem] += np.float64(line[start_pos:end_pos])
    group_coeff_mat[row][col_coeff] += np.float64(line[start_pos:end_pos])

file_in.close()

# print(group_mem_mat / 3)
for i in range(3):
    for j in range(3):
        print(group_coeff_mat[i][j], end=' ')
    print()
