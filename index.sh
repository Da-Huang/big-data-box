#/usr/bin/bash

data=/mnt/HPT8_56T/infomall-data/data2
index=/mnt/HPT8_56T/infomall-index/index2
java -Xms40g -Xmx40g -cp target/big-data-box.jar:target/lib/* sewm.bdbox.search.ThreadedInfomallIndexer --data=$data --index=$index --buffer_mb=30720 --threads=16
#sendemail -f sewm_pku@yeah.net -t dhuang.cn@gmail.com -s smtp.yeah.net -u "Index for $data has been built!" -m "Please check $index." -xu sewm_pku -xp sewm1220
