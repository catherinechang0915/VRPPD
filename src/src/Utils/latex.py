file_read = 'shrink_window_percent3.txt'
file_write = 'shrink_window_percent_latex.txt'
p_read = open(file_read, 'r')
p_write = open(file_write, 'w')
lines = p_read.readlines()
for i in range(len(lines)):
    args = lines[i].split(' ')
    args[0] = args[0].upper()[:-4] # strip .dat
    args.insert(1, '&')
    args.insert(3, '\\\\')
    args.insert(4, '\n')
    args[2] = '{0:.3f}'.format(float(args[2].strip()))
    lines[i] = '  '.join(args)
p_write.writelines(lines)
p_read.close()
p_write.close()

