for i in /usr/lib/spark-current/jars/*.jar
do
    echo "Finding:" $i
    jar -tf $i | grep $1
done
