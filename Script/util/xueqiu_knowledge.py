# -*- coding=UTF-8 -*-
import requests
import datetime
import time
import os
from lxml import html

head = {'User-Agent':'Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2272.118 Safari/537.36'}
#
# txt = requests.get('https://xueqiu.com/edu/invest-edu/education/risk/1894299685/83623924', headers=head)
# print(txt.text)
# print(type(txt.text))
#
# f = open("test.txt", "w")
# f.write(txt.text)
# f.close()


def spider_list():

    res = requests.get('https://xueqiu.com/edu/invest-edu/education/#/law/law1', headers=head)

    # f = open("test.txt", "w")
    # f.write(res.text)
    # f.close()
    tree = html.fromstring(res.text)
    # print(tree)
    uls = tree.xpath('//*[@id="templateList"]/div/ul')

    for ul in uls:
        lis = ul.xpath('./li')

        for li in lis:
            url = li.xpath('./a/@href')[0]
            url = 'https://xueqiu.com' + url
            title = li.xpath('./a/text()')[0]

            r = requests.get(url, headers=head)
            # r.encoding = 'gb2312'
            t = html.fromstring(r.text)
            f = open('./txt/'+title+'.txt', "w")
            ps = t.xpath('//*[@id="app"]/div[2]/div[2]/div/p')
            for p in ps:
                if p.xpath('./b') != []:
                    try:
                        txts = p.xpath('./b/text()')

                        for txt in txts:
                            f.write('B'+txt + '\n')
                    except:
                        f.write('\n')
                        # print(title)
                try:
                    txts = p.xpath('./text()')
                    for txt in txts:
                        f.write(txt+'\n')
                except:
                    # print(p)
                    # print(title)
                    f.write('\n')
            f.close()
            print(datetime.datetime.now())
            print(title)
            time.sleep(1)


def txt_to_html():
    files = os.listdir('./txt')
    for file in files:
        name = file.split('.')[0]
        f = open('./txt/'+file, "r")
        h = open('./html/'+name+'.html', "w")
        h.write('<div class="work" >'+'\n')
        h.write('<p class="heat-map-title" >&nbsp;&nbsp;&nbsp;'+name+'</p>'+'\n')
        h.write('<br />'+'\n')
        line = f.readline()

        while line:
            line.strip(' ')
            if line[0] == 'B':
                h.write('<p class="foxer_text_b">'+line[1:]+'</p>'+'\n')
            else:
                h.write('<p class="foxer_text">'+line+'</p>'+'\n')
            # print(line)
            line = f.readline()
        h.write('<div class="clear"> </div>'+'\n')
        h.write('</div>')
        h.close()
        print(name)
        print('=========')


if __name__ == '__main__':
    # spider_list()
    txt_to_html()