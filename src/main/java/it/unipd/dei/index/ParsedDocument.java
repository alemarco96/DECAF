package it.unipd.dei.index;


/**
 * The {@code ParsedDocument} class is a container for the ID and textual content of a document for indexing purposes.
 *
 * @author Marco Alessio
 */
@SuppressWarnings("unused")
public class ParsedDocument
{
    /**
     * The name of the Lucene field storing the ID of the document.
     */
    public static final String ID_FIELD_NAME = "id";

    /**
     * The name of the Lucene field storing the text of the document.
     */
    public static final String TEXT_FIELD_NAME = "text";

    /**
     * The name of the Lucene field storing the textual content of the document.
     */
    public static final String CONTENT_FIELD_NAME = "content";

    /**
     * The ID of this document.
     */
    public final String id;

    /**
     * The text of the document.
     */
    public final String text;

    /**
     * The content of the document.
     */
    public final String content;


    /**
     * Create the document.
     *
     * @param id The ID of the document.
     * @param content The textual content of the document.
     * @throws NullPointerException If any of the provided parameters is null.
     */
    public ParsedDocument(String id, String content)
    {
        if (id == null)
            throw new NullPointerException("The provided ID is null.");

        if (content == null)
            throw new NullPointerException("The provided content is null.");

        this.id = "" + id;
        this.text = "" + content;
        this.content = "" + content;
    }
}
