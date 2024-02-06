package sprakbanken.strix;

import org.apache.lucene.search.Query;
import org.elasticsearch.TransportVersion;
import org.elasticsearch.TransportVersions;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.index.query.AbstractQueryBuilder;
import org.elasticsearch.index.query.SearchExecutionContext;
import org.elasticsearch.index.query.SpanQueryBuilder;
import org.elasticsearch.xcontent.ParseField;
import org.elasticsearch.xcontent.XContentBuilder;
import org.elasticsearch.xcontent.XContentParser;
import org.elasticsearch.common.ParsingException;

import java.io.IOException;

public class SpanQueryAnyTokenBuilder extends AbstractQueryBuilder<SpanQueryAnyTokenBuilder> implements SpanQueryBuilder {
    public static final String NAME = "span_any";
    public static final ParseField WIDTH_NAME = new ParseField("width");
    public static final ParseField FIELD_NAME = new ParseField("field");

    private final int width;
    private final String field;

    public SpanQueryAnyTokenBuilder(StreamInput in) throws IOException {
        super(in);
        this.width = in.readVInt();
        this.field = in.readString();
    }

    public SpanQueryAnyTokenBuilder(String field, int width) {
        this.field = field;
        this.width = width;
    }

    @Override
    protected void doWriteTo(StreamOutput out) throws IOException {
        out.writeInt(this.width);
        out.writeString(this.field);
    }

    @Override
    protected void doXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject(NAME).field("width", this.width).field("field", this.field).endObject();
    }

    public static SpanQueryAnyTokenBuilder fromXContent(XContentParser parser) throws IOException {
        int width = 0;
        String field = "";

        String currentFieldName = null;
        XContentParser.Token token;
        while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
            if (token == XContentParser.Token.FIELD_NAME) {
                currentFieldName = parser.currentName();
            } else if (token.isValue()) {
                if (WIDTH_NAME.match(currentFieldName, null)) {
                    width = parser.intValue();
                } else if (FIELD_NAME.match(currentFieldName, null)) {
                    field = parser.text();
                } else {
                    throw new ParsingException(parser.getTokenLocation(), "[span_any] query does not support [" + currentFieldName + "]");
                }
            } else {
                throw new ParsingException(parser.getTokenLocation(), "[span_any] query does not support [" + currentFieldName + "]");
            }
        }
        return new SpanQueryAnyTokenBuilder(field, width);
    }

    @Override
    protected Query doToQuery(SearchExecutionContext context) {
        return new SpanQueryAnyToken(this.field, this.width);
    }

    @Override
    protected int doHashCode() {
        return 0;
    }

    @Override
    protected boolean doEquals(SpanQueryAnyTokenBuilder other) {
        return true;
    }

    @Override
    public String getWriteableName() {
        return NAME;
    }

    @Override
    public TransportVersion getMinimalSupportedVersion() {
        return TransportVersions.ZERO;
    }
}