package sprakbanken.strix;

import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermContext;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.spans.*;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class SpanQueryAnyToken extends SpanQuery {

    private final String field;
    private final int width;

    public SpanQueryAnyToken(String field, int width) {
        this.field = field;
        this.width = width;
    }

    @Override
    public String getField() {
        return field;
    }

    @Override
    public String toString(String field) {
        return "SpanGap(" + field + ":" + width + ")";
    }

    @Override
    public SpanWeight createWeight(IndexSearcher searcher, boolean needsScores, float boost) throws IOException {
        return new SpanQueryAnyToken.SpanGapWeight(searcher, boost);
    }


    private class SpanGapWeight extends SpanWeight {

        SpanGapWeight(IndexSearcher searcher, float boost) throws IOException {
            super(SpanQueryAnyToken.this, searcher, null, boost);
        }

        @Override
        public void extractTermContexts(Map<Term, TermContext> contexts) {

        }

        @Override
        public Spans getSpans(LeafReaderContext ctx, Postings requiredPostings) throws IOException {
            return new GapSpans(width);
        }

        @Override
        public void extractTerms(Set<Term> terms) {

        }

        @Override
        public boolean isCacheable(LeafReaderContext ctx) {
            return true;
        }

    }

    @Override
    public boolean equals(Object other) {
        return sameClassAs(other) && equalsTo(getClass().cast(other));
    }

    private boolean equalsTo(SpanQueryAnyToken other) {
        return width == other.width && field.equals(other.field);
    }

    @Override
    public int hashCode() {
        int result = classHash();
        result -= 7 * width;
        return result * 15 - field.hashCode();
    }

    static class GapSpans extends Spans {

        int doc = -1;
        int pos = -1;
        final int width;

        GapSpans(int width) {
            this.width = width;
        }

        @Override
        public int nextStartPosition() throws IOException {
            return ++pos;
        }

        public int skipToPosition(int position) throws IOException {
            return pos = position;
        }

        @Override
        public int startPosition() {
            return pos;
        }

        @Override
        public int endPosition() {
            return pos + width;
        }

        @Override
        public int width() {
            return width;
        }

        @Override
        public void collect(SpanCollector collector) throws IOException {

        }

        @Override
        public int docID() {
            return doc;
        }

        @Override
        public int nextDoc() throws IOException {
            pos = -1;
            return ++doc;
        }

        @Override
        public int advance(int target) throws IOException {
            pos = -1;
            return doc = target;
        }

        @Override
        public long cost() {
            return 0;
        }

        @Override
        public float positionsCost() {
            return 0;
        }
    }
}