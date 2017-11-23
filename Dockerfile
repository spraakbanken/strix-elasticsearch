FROM docker.elastic.co/elasticsearch/elasticsearch:5.5.1

# Add your elasticsearch plugins setup here

RUN mkdir strix_plugin

COPY plugin/strix-elasticsearch-plugin-5.5.1.zip strix_plugin/strixplugin.zip

RUN elasticsearch-plugin install file:///usr/share/elasticsearch/strix_plugin/strixplugin.zip

COPY analyzers/stems.txt config/

