Contents of this repo:
`plugin` - Contains code for a plugin for highlighting span queries, giving back word positions instead of words when highlighting.
`analyzers` - Contains `stems.txt` needed to translate words to lemgrams in analyzers

## How to set up Elasticsearch

1. Check plugin pom.xml to see current ES version needed, download and extract
2. Move `analyzers/stems.txt` to <elasticsearch_dir>/config/
3. Build plugin
    ```
    cd plugin
    mvn install
    ```
4. Unpack `plugin/target/releases/*.zip` and move containing folder to <elasticsearch_dir>/plugins/
5. Recommmended settings to att to `elasticsearch.yml` is:
    ```
    http.max_content_length: 1000mb
    indices.memory.index_buffer_size: 13%
    ```
