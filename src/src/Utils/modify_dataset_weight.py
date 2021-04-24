import os
import random


def main(n, mem, new_weights_lst, is_first):
    folder_root = os.path.dirname(os.path.dirname(os.path.dirname(os.getcwd())))
    folder_base = folder_root + '\\data\\pdp_' + str(n) + '_mem_' + str(mem) + '\\'
    if is_first:
        folder_in = folder_root + '\\data\\pdp_' + str(n) + '_mem_1.0\\1.0_1.0\\'
    else:
        folder_in = folder_base + '1.0_1.0\\'

    files_in = os.listdir(folder_in)

    # is_membership_calculated controls whether we need to randomly generate mem percentage of members
    # This should only be done once if is_first, and never be done if not is_first (only need copy over)
    if is_first:
        is_membership_calculated = False
    else:
        is_membership_calculated = True

    for new_weights in new_weights_lst:
        new_weight_1 = new_weights[0]
        new_weight_2 = new_weights[1]

        if not is_first and new_weight_1 == 1.0 and new_weight_2 == 1.0:
            # if not is_first, this is the base data to copy over
            continue
    
        folder_out = folder_base + str(new_weight_1)+ '_' + str(new_weight_2) + '\\'

        if not os.path.exists(folder_out):
            os.makedirs(folder_out)

        for data_file in files_in:
            p_read = folder_in + data_file
            p_write = folder_out + data_file
            f_read = open(p_read, 'r')
            f_write = open(p_write, 'w')
            lines = f_read.readlines()

            if not is_membership_calculated:
                # randomly generate membership data
                n = len(lines[-3].split(','))
                N = int((n - 2) / 2)
                lst = ['1' for _ in range(n)]
                for i in range(1, N + 1):
                    rand = random.random()
                    if rand < mem: # member
                        pass
                    else:
                        lst[i] = '0'
                        lst[i + N] = '0'
                lines[-3] = 'membership = [' + ','.join(lst) + '];\n'

            lines[-2] = 'alpha = ' + str(new_weight_1) + ';\n'
            lines[-1] = 'beta = ' + str(new_weight_2) + ';\n'
            f_write.writelines(lines)
            f_read.close()
            f_write.close()
        
        if not is_membership_calculated:
            is_membership_calculated = True

if __name__ == '__main__':
    for size in [100, 200]:
        for mem in [0.3, 0.5, 0.7]:
            main(size, mem, [[1.0, 1.0]], True)
            new_weights_lst = [[1.0, 1.0], [1.0, 3.0], [3.0, 1.0], [5.0, 1.0]] # place [1.0, 1.0] at first position
            main(size, mem, new_weights_lst, False)
