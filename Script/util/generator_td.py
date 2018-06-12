import sys
import os


def listdir(miao):
    path = '/Users/liyuan/Documents/Financial2/Script/util/advanced/'+miao
    res = []
    for file in os.listdir(path):
        file_path = os.path.join(path, file)
        if os.path.isdir(file_path):
            continue
        else:
            res.append(file.split('.')[0])
    return res


if __name__ == '__main__':
    # miao = ['投资者权利与义务', '投资者风险偏好测试', '投资风险', '明规则识风险']
    #  miao = ['债券', '基金', '期货', '股票', '其他法律']
    # miao = ['债券入门', '基金入门', '期货入门', '股票入门', '金融词典']
    miao = ['B股', '分级基金', '新三板', '期权|期货', '沪港通|深港通', '融资融券']
    first = '<td style="vertical-align:middle; text-align:center;">'
    later = '</td>'
    cc = open('advanced.txt', 'w')
    for m in miao:
        res = listdir(m)
        for r in res:
            cc.write('<tr>'+'\n')
            cc.write(first+m+later+'\n')
            cc.write(first+r+later+'\n')
            cc.write(first+'<a href="knowledge.html?name='+r+'" target="_blank">'+'🔎</a>'+later+'\n')
            cc.write('</tr>'+'\n')

    cc.close()


