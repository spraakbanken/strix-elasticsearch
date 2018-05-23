package sprakbanken.strix;

import org.elasticsearch.index.analysis.TokenFilterFactory;
import org.elasticsearch.indices.analysis.AnalysisModule.AnalysisProvider;
import org.elasticsearch.plugins.AnalysisPlugin;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.plugins.SearchPlugin;
import org.elasticsearch.search.fetch.FetchSubPhase;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class StrixPlugin extends Plugin implements AnalysisPlugin, SearchPlugin {

    @Override
    public Map<String, AnalysisProvider<TokenFilterFactory>> getTokenFilters() {
        return Collections.singletonMap("set_delimiter_token_filter", SetDelimiterTokenFilterFactory::new);
    }

    @Override
    public List<FetchSubPhase> getFetchSubPhases(SearchPlugin.FetchPhaseConstructionContext context) {
        return Collections.singletonList(new StrixFetchSubPhase());
    }

    @Override
    public List<QuerySpec<?>> getQueries() {
        return Collections.singletonList(new QuerySpec<>(SpanQueryAnyTokenBuilder.NAME, SpanQueryAnyTokenBuilder::new, SpanQueryAnyTokenBuilder::fromXContent));
    }

}