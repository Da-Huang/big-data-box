#/usr/bin/bash

data=/Volumes/HPT8_56T/data/U200201
index=/Volumes/HPT8_56T/index.old
java -Xms4g -Xmx4g -cp target/big-data-box.jar:target/lib/* sewm.bdbox.search.InfomallIndexer --data=$data --index=$index
sendemail -f sewm_pku@yeah.net -t dhuang.cn@gmail.com -s smtp.yeah.net -u "Index for $data has been built!" -m "Please check $index." -xu sewm_pku -xp sewm1220
