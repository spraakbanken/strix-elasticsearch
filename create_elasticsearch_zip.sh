#!/bin/bash

#set -x

if [ $# -ne 1 ]; then
    echo "Usage: $0 VERSION"
    exit 1
fi

version=$1
elasticzipurl="https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-$version.zip"
elasticshaurl="https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-$version.zip.sha1"
elasticzip="elasticsearch-$version.zip"
elasticsha="elasticsearch-$version.zip.sha1"
elasticdir="elasticsearch-$version"

wget $elasticzipurl --output-document $elasticzip
wget $elasticshaurl --output-document $elasticsha

result=`for f in elasticsearch-$version.zip.sha1; do echo "$(cat $f) ${f/.sha1/}"; done | sha1sum -c`
if [[ $result != *OK* ]]; then
    echo "wrong sha1 sum for zip"
    exit 1
fi

rm $elasticsha

unzip -qq $elasticzip

# move stems.txt
cp analyzers/stems.txt $elasticdir/config

# move and unpack the strix plugin
unzip -qq plugin/strix-elasticsearch-plugin-$version.zip -d $elasticdir/plugins/

# update the config
echo "http.max_content_length: 1000mb" >> $elasticdir/config/elasticsearch.yml
# 13% is the value that gives each shard 512mb each when each index has 4 shards and nodes have 16GB heap
echo "indices.memory.index_buffer_size: 13%" >> $elasticdir/config/elasticsearch.yml
zip -qq -r $elasticzip $elasticdir
rm -r $elasticdir

