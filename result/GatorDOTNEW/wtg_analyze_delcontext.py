import os,traceback

def convert(old_path, new_path):
    with open(new_path,"a+") as fo:
        fo.write("digraph G {"+'\n')
        fo.write(" rankdir=LR;"+'\n')
        fo.write(" node[shape=box];"+'\n')
        with open(old_path,"r+") as f:
            dic1= {}
            dic2 = {}
            actname = {}
            launchnode = ''
            replacewindows = []
            dialogexceptions = []
            contextexceptions = []
            lines = f.readlines()
            for i,line in enumerate(lines):
                line = line.strip('\r\n')
                if i>2:
                    if '->' not in line:
                        if '[label="ACT[' in line:
                            fo.write(line + '\n')
                            num = line.split(' ')[1]
                            act = line.split('[')[2].split(']')[0]
                            dic1[act] = num
                            actname[num] = line.split('"')[1].split('"')[0]
                        elif '[label="DIALOG[' in line:
                            info = line.split(' ')
                            num = info[1]
                            act = info[4][1:].split('$')[0].split(':')[0]
                            dic2[num] = act
                        elif '[label="OptionsMenu[' in line:
                            info = line.split(' ')
                            num = info[1]
                            act = line.split('[')[2].split(']')[0]
                            dic2[num] = act
                        elif '[label="LAUNCHER_NODE' in line:
                            info = line.split(' ')
                            launchnode = info[1]
                        elif '[label="ContextMenu[' in line:
                            info = line.split(' ')
                            num = info[1]
                            contextexceptions.append(num)
            for key in dic2:
                try:
                    dic2[key] = dic1[dic2[key]]
                    replacewindows.append(key)
                except:
                    dialogexceptions.append(key)
            for i, line in enumerate(lines):
                line = line.strip('\r\n')
                if '->' in line:
                    info = line.split(' ')
                    src = info[1]
                    dst = info[3]
                    if src != launchnode and dst != launchnode:
                        if src in replacewindows:
                            line = line.replace(src+' ->', dic2[src]+' ->')
                            tmp0 = line[line.find('src: '):]
                            tmp1 = tmp0[:tmp0.find('\\n')]
                            tmp2 = "src: "+ actname[dic2[src]]
                            line = line.replace(tmp1, tmp2)
                        if dst in replacewindows:
                            line = line.replace('-> '+dst, '-> ' + dic2[dst])
                            tmp0 = line[line.find('tgt: '):]
                            tmp1 = tmp0[:tmp0.find('\\n')]
                            tmp2 = "tgt: "+ actname[dic2[dst]]
                            line = line.replace(tmp1, tmp2)
                        if not (src in dialogexceptions and dst in dialogexceptions):
                            if not ((src in contextexceptions or dst in contextexceptions)):
                                if src in dialogexceptions:
                                    line = line.replace(src + ' ->', dst + ' ->')
                                    tmp0 = line[line.find('src: '):]
                                    tmp1 = tmp0[:tmp0.find('\\n')]
                                    tmp2 = line[line.find('tgt: '):]
                                    tmp3 = tmp2[:tmp2.find('\\n')].replace('tgt:',"src:")
                                    line = line.replace(tmp1, tmp3)
                                if dst in dialogexceptions:
                                    line = line.replace('-> ' + dst, '-> ' + src)
                                    tmp0 = line[line.find('tgt: '):]
                                    tmp1 = tmp0[:tmp0.find('\\n')]
                                    tmp2 = line[line.find('src: '):]
                                    tmp3 = tmp2[:tmp2.find('\\n')].replace('src:',"tgt:")
                                    line = line.replace(tmp1, tmp3)
                                fo.write(line+'\n')
        fo.write("}")

if __name__ == '__main__':
    src_root = 'E:/workspace-for-mars/XposedConnection-master/result/DOT/'
    dst_root= 'E:/workspace-for-mars/XposedConnection-master/result/newGatorDOT/'
    dots = os.listdir(src_root)
    for dot in dots:
        src = src_root + dot
        dst = dst_root + dot
        try:
            convert(src, dst)
            print(dot)
        except :
            traceback.print_exc()