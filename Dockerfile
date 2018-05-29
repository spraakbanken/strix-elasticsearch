FROM docker.elastic.co/elasticsearch/elasticsearch:6.2.4
RUN mkdir strix_plugin
COPY plugin/strix-elasticsearch-plugin-6.2.4.zip strix_plugin/strixplugin.zip
RUN elasticsearch-plugin install file:///usr/share/elasticsearch/strix_plugin/strixplugin.zip
COPY analyzers/stems.txt config/

