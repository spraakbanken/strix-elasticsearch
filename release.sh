#!/bin/bash

set -x

cd plugin
mvn install
cd ..

es_version="6.2.4"
strix_es_version="1.0.dev"

elasticzipurl="https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-$es_version.zip"
elasticshaurl="https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-$es_version.zip.sha512"
elasticzip="elasticsearch-$es_version.zip"
elasticsha="elasticsearch-$es_version.zip.sha512"
elasticdir="elasticsearch-$es_version"

wget $elasticzipurl --output-document $elasticzip
wget $elasticshaurl --output-document $elasticsha

result=`for f in elasticsearch-$es_version.zip.sha512; do echo "$(cat $f)"; done | sha512sum -c`
if [[ $result != *OK* ]]; then
    echo "wrong sha1 sum for zip"
    exit 1
fi

rm $elasticsha

unzip -qq $elasticzip

# move stems.txt
cp analyzers/stems.txt $elasticdir/config

# move and unpack the strix plugin
unzip -qq plugin/target/releases/*.zip -d $elasticdir/plugins/

# update the config
echo "http.max_content_length: 1000mb" >> $elasticdir/config/elasticsearch.yml
# 13% is the value that gives each shard 512mb each when each index has 4 shards and nodes have 16GB heap
echo "indices.memory.index_buffer_size: 13%" >> $elasticdir/config/elasticsearch.yml
zip -qq -r $elasticzip $elasticdir
rm -r $elasticdir




scp $elasticzip demo:/export/htdocs/mariao/strix/elasticsearch/strix-elasticsearch_$strix_es_version.zip

rm $elasticzip

