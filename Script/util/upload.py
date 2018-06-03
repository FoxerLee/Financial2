import pymysql
import os


sql_conn = pymysql.connect(host='101.132.166.73', port=3306, user='Hjx', passwd='Hjx_2019961226',
                           db='Finance', charset='utf8mb4')
cursor = sql_conn.cursor()
for root, dirs, files in os.walk('html'):
    for name in files:
        file = open('html/' + name, 'r', encoding='utf-8')
        name = name.split('.')[0]
        str = file.read().split('.')[0]
        str.replace(',', '\'')
        query = """INSERT INTO introduction_file(name, content) VALUES (\'%s\',\'%s\')""" % (name, str)
        try:
            cursor.execute(query)
            sql_conn.commit()
        except:
            print('failed %s' % (name))
            continue
sql_conn.close()

