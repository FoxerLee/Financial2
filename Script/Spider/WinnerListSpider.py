# -*- coding: utf-8 -*-
import sys
import mysql.connector
import datetime
import tushare as ts
import csv
import re

reload(sys)
sys.setdefaultencoding('utf-8')
config = {'host': '101.132.166.73', 'user': 'Hjx', 'password': 'Hjx_2019961226', 'port': 3306, 'database': 'Finance',
          'charset': 'utf8'}
def run():
    start = datetime.datetime.now()
    # config = {'host': '10.60.42.201', 'user': 'root', 'password': '123456', 'port': 13142, 'database': 'javaEE',
    #           'charset': 'utf8'}
    conn = mysql.connector.connect(**config)
    cursor = conn.cursor()
    cursor.execute("DELETE FROM winnerlist;")

    today = datetime.date.today()
    yesterday = today + datetime.timedelta(days = -1)
    # today = str(today)
    print yesterday
    td = ts.top_list(str(yesterday))

    params = []
    for index, row in td.iterrows():
        params.append((row['code'], row['name'], row['pchange'], row['amount'], row['buy'], row['bratio'], row['sell'], row['sratio'], row['reason']))

    sql = "INSERT INTO winnerlist VALUES (NULL, %s, %s, %s, %s, %s, %s, %s, %s, %s)"
    cursor.executemany(sql, params)
    cursor.execute("Commit;")

    cursor.close()
    conn.close()

    print datetime.datetime.now() - start

if __name__ == '__main__':
    run()

