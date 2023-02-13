package it.unipd.dei.corpus;

import it.unipd.dei.index.ParsedDocument;

import java.util.Iterator;


/**
 * The {@code CollectionParser} interface is derived by all classes that parse a corpus of documents for
 * indexing purposes.
 *
 * @author Marco Alessio
 */
@SuppressWarnings("unused")
public interface CorpusParser extends Iterator<ParsedDocument>, Iterable<ParsedDocument>, AutoCloseable
{
}
