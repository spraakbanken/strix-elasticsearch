package sprakbanken.strix;

import org.apache.lucene.search.*;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanWeight;
import org.apache.lucene.search.spans.Spans;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.search.fetch.FetchSubPhase;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.fetch.subphase.highlight.SearchContextHighlight;
import org.elasticsearch.search.internal.SearchContext;

import java.io.IOException;
import java.util.*;

public class StrixFetchSubPhase implements FetchSubPhase {

    public StrixFetchSubPhase() {
    }

    @Override
    public void hitExecute(SearchContext context, HitContext hitContext) {
        int numberOfFragments = -1;

        SearchContextHighlight highlight = context.highlight();
        if(highlight == null) {
            return;
        }

        boolean found = false;
        for (SearchContextHighlight.Field field : highlight.fields()) {
            if (field.field().equals("strix")) {
                Map<String, Object> options = field.fieldOptions().options();
                if(options != null) {
                    numberOfFragments = (Integer) options.get("number_of_fragments");
                }
                found = true;
                break;
            }
        }

        if(!found) {
            return;
        }

        List<SpanQuery> spanQueries = getSpanQueriesFromContext(context);
        Map<Integer, Set<Integer>> seenStartPositions = new HashMap<>();

        List<Text> docHighlights = new ArrayList<>();
        for(SpanQuery spanQuery : spanQueries) {
            if (spanQuery == null) {
                return;
            }

            try {
                SpanWeight weight = spanQuery.createWeight(context.searcher(), false);
                Spans spans = weight.getSpans(hitContext.readerContext(), SpanWeight.Postings.POSITIONS);

                if (spans == null) {
                    return;
                }

                int docId;
                while ((docId = spans.nextDoc()) != DocIdSetIterator.NO_MORE_DOCS) {
                    if (docId == hitContext.docId()) {
                        Set<Integer> seenStartPositionsDoc = seenStartPositions.get(docId);
                        if(seenStartPositionsDoc == null) {
                            seenStartPositionsDoc = new HashSet<>();
                            seenStartPositions.put(docId, seenStartPositionsDoc);
                        }
                        while (spans.nextStartPosition() != Spans.NO_MORE_POSITIONS && (numberOfFragments == -1 || docHighlights.size() < numberOfFragments)) {
                            int start = spans.startPosition();
                            if (seenStartPositionsDoc.contains(start)) {
                                continue;
                            }
                            seenStartPositionsDoc.add(start);
                            int end = spans.endPosition();
                            docHighlights.add(new Text(start + "-" + end));
                        }
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Text[] something = docHighlights.toArray(new Text[0]);
        Map<String, HighlightField> highlightFields = Collections.singletonMap(String.valueOf(hitContext.docId()), new HighlightField("positions", something));
        hitContext.hit().highlightFields(highlightFields);
    }

    // TODO: We can search recursively for a span query instead
    private List<SpanQuery> getSpanQueriesFromContext(SearchContext context) {
        return getSpanQueries(context.query());
    }

    private List<SpanQuery> getSpanQueries(Query query) {
        List<SpanQuery> spanQueries = new ArrayList<>();
        if(query instanceof SpanQuery) {
            spanQueries.add((SpanQuery) query);
        } else if(query instanceof BooleanQuery) {
            BooleanQuery booleanQuery = (BooleanQuery) query;
            for(BooleanClause clause : booleanQuery.clauses()) {
                spanQueries.addAll(getSpanQueries(clause.getQuery()));
            }
        }
        return spanQueries;
    }

}
