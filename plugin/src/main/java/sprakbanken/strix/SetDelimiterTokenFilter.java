package sprakbanken.strix;


import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;

import java.io.IOException;
import java.util.Stack;

public class SetDelimiterTokenFilter extends TokenFilter {

    private Stack<String> synonymStack = new Stack<>();
    private String delimiter;
    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final PositionIncrementAttribute posIncrAtt = addAttribute(PositionIncrementAttribute.class);
    private int positionIncrement;

    SetDelimiterTokenFilter(TokenStream input, String delimiter) {
        super(input);
        this.delimiter = delimiter;
    }

    @Override
    public boolean incrementToken() throws IOException {
        positionIncrement = 0;

        if (synonymStack.size() > 0) {
            popAliasFromStack();
            return true;
        }

        while(true) {
            if (!input.incrementToken()) {
                return false;
            }

            if (addAliasesToStack()) {
                popAliasFromStack();
                return true;
            }
        }

    }

    private void popAliasFromStack() {
        String syn = synonymStack.pop();
        termAtt.copyBuffer(syn.toCharArray(), 0, syn.length());
        posIncrAtt.setPositionIncrement(positionIncrement);
    }


    /**
     * Checks whether current token has synonyms appended. If it has, then they
     * are pushed on the synonymStack.
     *
     * @return true if synonyms were found, otherwise false
     */
    private boolean addAliasesToStack() {
        positionIncrement++;
        String buffer = termAtt.toString();
        String[] synonyms = buffer.split(delimiter);

        // No synonyms have been found
        if (synonyms.length == 0) {
            return false;
        }

        for(String synonym : synonyms) {
            if(!synonym.equals("")) {
                synonymStack.push(synonym);
            }
        }

        return true;
    }

}
