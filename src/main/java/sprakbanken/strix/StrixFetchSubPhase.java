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

        SpanQuery spanQuery = getSpanQuery(context);

        if(spanQuery == null) {
            return;
        }

        try {
            SpanWeight weight = spanQuery.createWeight(context.searcher(), false);
            Spans spans = weight.getSpans(hitContext.readerContext(), SpanWeight.Postings.POSITIONS);

            if(spans == null) {
                return;
            }

            int docId;
            while ((docId = spans.nextDoc()) != DocIdSetIterator.NO_MORE_DOCS) {
                if(docId == hitContext.docId()) {
                    List<Text> docHighlights = new ArrayList<>();
                    Set<Integer> seenStartPositions = new HashSet<>();
                    while (spans.nextStartPosition() != Spans.NO_MORE_POSITIONS && (numberOfFragments == -1 || docHighlights.size() < numberOfFragments)) {
                        int start = spans.startPosition();
                        if(seenStartPositions.contains(start)) {
                            continue;
                        }
                        seenStartPositions.add(start);
                        int end = spans.endPosition();
                        docHighlights.add(new Text(start + "-" + end));
                    }

                    Text[] something = docHighlights.toArray(new Text[0]);
                    Map<String, HighlightField> highlightFields = Collections.singletonMap(String.valueOf(docId), new HighlightField("positions", something));
                    hitContext.hit().highlightFields(highlightFields);
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // TODO: We can search recursively for a span query instead
    private SpanQuery getSpanQuery(SearchContext context) {
        SpanQuery spanQuery = null;

        Query query = context.query();
        if(query instanceof SpanQuery) {
            spanQuery = (SpanQuery) query;
        } else if(query instanceof BooleanQuery) {
            BooleanQuery booleanQuery = (BooleanQuery) query;
            for(BooleanClause clause : booleanQuery.clauses()) {
                if(clause.getQuery() instanceof SpanQuery) {
                    spanQuery = (SpanQuery) clause.getQuery();
                    break;
                } else if(clause.getQuery() instanceof BooleanQuery) {
                    BooleanQuery nested = (BooleanQuery) clause.getQuery();
                    for(BooleanClause nestedClause : nested.clauses()) {
                        if(nestedClause.getQuery() instanceof SpanQuery) {
                            spanQuery = (SpanQuery) nestedClause.getQuery();
                            break;
                        }
                    }
                }
            }
        }
        return spanQuery;
    }
}
