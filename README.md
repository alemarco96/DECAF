# DECAF

DECAF is a framework for performing conversational search. Its modular architecture enables to easily
extend and adapt it to suit most applications' needs, while reusing most of the pre-defined components. The core of
DECAF is written in Java, while some components are implemented in Python. All machine-learning code
supports acceleration through CUDA-enabled GPU. It is developed at [University of Padua](https://www.unipd.it/).

## How to setup the Framework

Refer to the [Installation](INSTALLATION_GUIDE.md) guide for further details.

## How to perform experiments using the Framework

Refer to the [Reproducibility](REPRODUCIBILITY_GUIDE.md) and [Configuration](CONFIGURATION_GUIDE.md) guides for further
details.

## DECAF Architecture

DECAF contains several components responsible for performing all main operations required by a
conversational search application. Both the indexing of the corpus' documents and the search phase execution
can be easily customized using the provided properties configuration files. For more details, see the
[Configuration Guide](CONFIGURATION_GUIDE.md).

The framework is built around two configurable pipelines, dubbed respectively index and search pipeline. The Index
Pipeline is composed of two main components:

- `CorpusParser`

  It processes a corpus of documents and provides a stream of documents to be processed by the Indexer.

- `Indexer`

  It processes each document provided by the CorpusParser and stores relevant information of a data structure called
  index, which is stored on disk for later performing documents retrieval through the Searcher component.


Instead, the Search Pipeline is composed of five main components:

- `TopicsParser`

  It provides new conversations and utterances to be processed by the remaining of the framework.

- `Rewriter`

  It modifies the text of the question by adding contextual and implied information extracted from previous utterances
  in the conversation. Its goal is to enhance the retrieval performance of the third component, the Searcher.

- `Searcher`

  It takes the rewritten text, generate a query and retrieves a small set of candidate documents to answer the
  provided question. The ranked list of documents generated as output is then consumed by the Reranker.

- `Reranker`

  It is designed to provide a better ranking, therefore boosting the performance of the whole system. This component
  usually carries out expensive computations, which are however focused only on the candidate documents.

- `RunWriter`

  Finally, the RunWriter delivers the result computed from the system to the user.

## Components Implemented

DECAF comes with several components already available out-of-the-box.

### CorpusParser

- `MSMARCOv1`

  It allows for parsing passages contained within MS-MARCO version 1. There is also support for duplicates removal, by
  providing an additional textual file. Each line must use the `<KEEP ID>:<DUPLICATE 1 ID>,<DUPLICATE 2 ID>,...\n`
  format, so all documents whose ID appears as a duplicate are discarded.

- `TRECCARv2`

  It addresses the paragraph corpus of TREC CAR v2.0 dataset.

- `Tsv`

  It processes any corpus based on tab-separated files using the `<DOC_ID>\t<DOC TEXT>\n` format.

- `Multi`

  It fuses together the stream of documents extracted by some other CorpusParsers. It is well suited for indexing
  data coming from multiple corpora, where each of them is parsed using one of the other implemented CorpusParser.

### Indexer

- `BoW`

  It is a wrapper around Lucene indexing operations. Its efficient inverted index implementation makes it suitable
  for bag-of-words sparse retrieval models.

- `Splade`

  This `BoW` indexer implementation is specific for the homonyms neural retrieval model. It replaces the standard
  tokenization and analysis pipeline performed by Lucene with the SPLADE model inference.

- `Dense`

  It is specific for dense retrieval models. It is built on top of [Transformers (Hugging Face)](https://huggingface.co/)
  library, which handles the models, and of [FAISS](https://faiss.ai/) library, which handles the storage of the
  document embeddings.

### TopicsParser

All available components are designed to parse [TREC CAsT](https://www.treccast.ai/) evaluation topics for Year 1 and 2,
held respectively in 2019 and 2020. The prefix in their names represent the different kind of utterances returned.

- `AutCAsT19`

  It provides *raw utterances* used by the so-called *automatic runs* for TREC CAsT 2019.

- `ManCAsT19`

  It provides utterances *manually rewritten* by TREC CAsT organizers, used by the so-called *manual runs* for TREC
  CAsT 2019.

- `AutCAsT20`

  It provides *raw utterances* used by the so-called *automatic runs* for TREC CAsT 2020.

- `RewCAsT20`

  It provides *automatically rewritten utterances* obtained from the raw ones by applying a T5 model, available only for
  TREC CAsT 2020.

- `ManCAsT20`

  It provides utterances *manually rewritten* by TREC CAsT organizers, used by the so-called *manual runs* for TREC
  CAsT 2020.

### Rewriter

- `No`

  It performs no operations and simply returns the original text unchanged. It should be used in all cases where the
  utterances have been rewritten externally from this framework.

- `AllenNLP` and `FastCoref`

  They perform co-reference resolution, which replaces all expressions (mostly pronouns) that refers to the same entity
  in the text. The two components differ for the library used to perform the operation, which are
  [AllenNLP](https://demo.allennlp.org/coreference-resolution/) and
  [fastcoref](https://github.com/shon-otmazgin/fastcoref) respectively. Their performance is similar, but the second one
  is faster to execute.

- `T5`

  A T5 model trained for conversational search question rewriting. It is the rewriter with the best performance for
  automatic utterances in both TREC CAsT 2019 and 2020.

### Searcher

All the components require that the documents must have already been indexed to perform their operation.

- `BoW`

  It performs first stage retrieval using [Lucene](https://lucene.apache.org/) text search engine library. It is well
  suited for implementing bag-of-words retrieval models. It provides good results when using BM25 scoring function,
  which is often used as an evaluation baseline. It should be paired with a reranker to improve performance measures.

- `Splade`

  It performs first stage retrieval using [SPLADE](https://github.com/naver/splade), a neural retrieval model which
  learns sparse representation for both queries and documents via the BERT MLM head and sparse regularization. It
  produces a list of term-weight pairs, that is indexed using [Lucene](https://lucene.apache.org/). Please note that,
  during indexing phase, each weight given by SPLADE is multiplied by a constant factor (such as 100 or 1000) and then
  rounded to the nearest integer, therefore the results may slightly differ from the one reported in the literature.
  This component does not require any additional reranking for optimal performance.

- `Dense`

  It performs first stage dense retrieval, using [Tranformers (Hugging Face)](https://huggingface.co/) library
  to generate the embeddings and [Faiss](https://faiss.ai/) library to store them. The amount of disk space required to
  store the index is very large (usually 3 KB for each document), and it is the worst performing Searcher for TREC CAsT.

#### Query Generator

This sub-component generates the query representation used by a Searcher for retrieval as well as by a Reranker to
evaluate the query similarity with the documents. The available options are:

- `Current`

  It considers only the rewritten text of the current utterance.

- `All`

  It considers the concatenation of the rewritten text for each utterance of the current conversation.

- `FLC`

  It does a weighted sum of the rewritten text of the first (F), last (L) and current (C) query utterances. The
  rationale behind it is that the first one gives the general topic of the conversation, while the last one is very
  likely to be referenced by the current utterance. It aids the Rewriter effort to bring contextual information into
  the current query, especially useful when the quality of its output is less than ideal.

### Reranker

- `No`

  It performs no operations and simply returns the ranked list of documents generated by the Searcher.

- `Transformers`

  It performs reranking by applying a machine-learning model, such as BERT, using
  [Tranformers (Hugging Face)](https://huggingface.co/) library. This component has three configurable sub-components:

  * Query Generator

    It produces the query embedding, that will be compared against each document to produce their new score. The
    available options are the same as above.

  * Similarity Function
  
    The similarity function is used to evaluate the similarity degree between the query and each document. The available
    options are:
  
    + `cos` Cosine Similarity
    + `dot` Dot Product
    + `l2` Euclidean Distance
    + `l2sq` Squared Euclidean Distance (no square root computation)

  * Run Fusion

    The ranked list produced by the Transformers model can be fused together with the original one given by the Searcher
    using a run fusion technique. The available options are:
  
    + `Null`
    
      No fusion is applied.

    + `Linear`
    
      For each document, the new score is computed by applying a linear combination between the score of the Searcher,
      weighted `1.0 - alpha`, and the one given by the Transformers model, weighted `alpha`. Note that both ranked
      lists are min-max normalized before applying the linear combination.
    
    + `ReciprocalRank`
    
      For each document, the formula `1.0 / (k + rank[doc])` is used to obtain a score from both ranked list, that are
      then combined using a linear combination of parameter `alpha`. To obtain the same results as the standard
      reciprocal rank run fusion technique, use `k = 60` and `alpha = 0.5`.

### Run Writer

- `TrecEval`

  It produces a run using the standard TREC eval format.

- `Debug`

  It produces a directory containing multiple files providing information about system performance and behavior. It
  contains various runs in TREC eval format:

  * Full run generated from the rankings given by the Searcher and Reranker.
  * One run for each conversation processed.
  * One run for all utterances with the same turn in their conversation.

  as well as the list of top 10 responses produced by the system for each utterance.
