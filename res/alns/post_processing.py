import os
import sys

def generate_file():
    pass

def main():
    cwd = os.getcwd()
    dir_to = cwd + os.sep + 'aggregation' + os.sep

    aggregation_lines = []

    for dirname in os.listdir(cwd):
        if dirname == 'aggregation' or '.py' in dirname:
            continue
        
        outer_dir_from = cwd + os.sep + dirname

        args = outer_dir_from.split('_')
        size = int(args[1])
        mem = float(args[3])

        for inner_dirname in os.listdir(outer_dir_from):
            for filename_in in os.listdir(outer_dir_from + os.sep + inner_dirname):

                file_in = open(outer_dir_from + os.sep + inner_dirname + os.sep + filename_in)

                if size == 200:
                    group_num = 3
                else:
                    group_num = int(filename_in.split('_')[-1][:-4])

                filename_out = str(group_num) + '_mem_' + str(mem) + '_coeff_' + inner_dirname + '.txt'
                file_out = open(dir_to + filename_out, 'w+')

                in_lines = file_in.readlines()
                case_num = len(in_lines) // 6
                out_lines = [in_lines[1 + 6*i][:15] + in_lines[1 + 6*i + 5][15:] for i in range(case_num)]
                file_out.writelines(out_lines)
                
                func = lambda pos, lst : '{0:<15f}'.format(sum([float(line[15*pos:15*(pos+1)]) for line in out_lines]) / len(out_lines))
                aggregation_line = '{0:25s}'.format(filename_out[:-4]) + ''.join([func(i, out_lines) for i in range(1, 5)]) + '\n'
                aggregation_lines.append(aggregation_line)

                file_in.close()
                file_out.close()

    file_aggregation_out = open(dir_to + 'aggregation.txt', 'w+')
    aggregation_lines = sorted(aggregation_lines)
    file_aggregation_out.writelines(aggregation_lines)
    file_aggregation_out.close()

if __name__ == '__main__':
    main()
