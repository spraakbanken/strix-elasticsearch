package sprakbanken.strix;

import org.elasticsearch.search.fetch.FetchContext;
import org.elasticsearch.search.fetch.FetchSubPhase;
import org.elasticsearch.search.fetch.FetchSubPhaseProcessor;
import java.io.IOException;

public class StrixFetchSubPhase implements FetchSubPhase {

    public StrixFetchSubPhase() {
    }

    @Override
    public FetchSubPhaseProcessor getProcessor(FetchContext fetchContext) throws IOException {
        return new StrixFetchSubPhaseProcessor(fetchContext);
    }
}
