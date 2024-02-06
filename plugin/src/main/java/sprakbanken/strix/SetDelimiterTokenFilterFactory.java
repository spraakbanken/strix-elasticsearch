package sprakbanken.strix;

import org.apache.lucene.analysis.TokenStream;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractTokenFilterFactory;

public class SetDelimiterTokenFilterFactory extends AbstractTokenFilterFactory {

    private String delimiter;

    public SetDelimiterTokenFilterFactory(IndexSettings indexSettings, Environment environment, String name, Settings settings) {
        super(name, settings);
        this.delimiter = settings.get("delimiter", "\u241F");
    }

    @Override
    public TokenStream create(TokenStream tokenStream) {
        return new SetDelimiterTokenFilter(tokenStream, this.delimiter);
    }
}
