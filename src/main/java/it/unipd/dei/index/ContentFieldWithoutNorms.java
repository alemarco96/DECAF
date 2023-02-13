package it.unipd.dei.index;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexOptions;
import it.unipd.dei.search.Searcher;

import java.io.Reader;


/**
 * The {@code ContentFieldWithoutNorms} class is a Lucene {@link Field} used to store the data representation of the
 * document used by any Lucene-based {@link Searcher} to perform its job. This field
 * does not store the norms of each document, differently from {@link ContentFieldWithNorms}.
 *
 * @author Marco Alessio
 */
@SuppressWarnings("unused")
public class ContentFieldWithoutNorms extends Field
{
    private static final FieldType FIELD_TYPE = new FieldType();
    static
    {
        FIELD_TYPE.setIndexOptions(IndexOptions.DOCS_AND_FREQS);
        FIELD_TYPE.setOmitNorms(true);
        FIELD_TYPE.setTokenized(true);
        FIELD_TYPE.setStoreTermVectors(false);
        FIELD_TYPE.setStored(false);
    }


    /**
     * Initialize this field using a {@link Reader}.
     *
     * @param value The value of this field.
     */
    public ContentFieldWithoutNorms(final Reader value)
    {
        super(ParsedDocument.CONTENT_FIELD_NAME, value, FIELD_TYPE);
    }


    /**
     * Initialize this field using a {@link String}.
     *
     * @param value The value of this field.
     */
    public ContentFieldWithoutNorms(final String value)
    {
        super(ParsedDocument.CONTENT_FIELD_NAME, value, FIELD_TYPE);
    }
}
