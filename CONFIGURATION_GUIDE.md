# Configuration Guide

## Structure of the Configuration Files

Both the index and search phase behavior can be customised by doing small edits to the properties configuration files
provided with the framework. The general structure is:

```
Configuration lines, that should be edited by DECAF users to customize its behavior.

#****************************************************************************************************
#****************************************************************************************************
#****************************************************************************************************
#****************************************************************************************************
#****************************************************************************************************

Reflection lines, needed to correctly instantiate the selected components at runtime.
```

Each line represent a `key = value` pair. Differently from standard
[properties file](https://en.wikipedia.org/wiki/.properties), with `$(referenced_key)` in the value it is possible to
replace it with the value stored for that referenced key. For example, this snippet

```
first = foo
second = $(first)_bar
```

is evaluated as `first = foo` and `second = foo_bar`.

A common technique used in the provided properties files is to have multiple lines with the same key but different
values. Their purpose is to provide for the same key multiple options to choose from. All lines must be commented by
adding a `#` character at the start of the line, except the one containing the desired value. As an example, with this
snippet

```
#color = yellow
#color = red
#color = blue
color = green
```

the selected color is green.


## Index Phase Configuration

The index phase is configured using the provided `index.properties` file. In the top part of the file, the user of
DECAF must set:

- `[int] launch.num_threads` The number of CPU threads to use to speed-up code execution.
- `launch.corpus` Which [CorpusParser](README.md#corpusparser) to instantiate.
- `launch.indexer` Which [Indexer](README.md#indexer) to instantiate.

### Detailed Components Configuration

Most components also requires to set some additional parameters.

#### CorpusParser

- `CAsT1920`

    <details>
    <summary>Additional Parameters</summary>

    * `[string] launch.corpus.CAsT1920.msmarco_corpus_filename`

      The path to the corpus file of MS-MARCO passage dataset v1.

    * `[string] launch.corpus.CAsT1920.msmarco_duplicates_filename`

      The path to the duplicates file for MS-MARCO passage dataset v1, provided by the organizers of TREC CAsT.

    * `[string] launch.corpus.CAsT1920.treccar_corpus_filename`

      The path to the corpus file of TREC-CAR dataset v2.

    </details>

#### Indexer

- `BoW`

    <details>
    <summary>Additional Parameters</summary>

    * `[string] launch.indexer.BoW.index_directory`

      The folder where the index will be stored.

    * `[string] launch.indexer.BoW.analyzer`

      Which Lucene analyzer is applied to the documents' text. The only pre-defined option available is `English`.

    * `[string] launch.indexer.BoW.similarity`

      Which Lucene similarity function will be later used to score each document. The available options are:

        + `BM25`
            - `[int] launch.indexer.BoW.similarity.BM25.k1` The k1 parameter of BM25 (Lucene default value: `1.2`).
            - `[int] launch.indexer.BoW.similarity.BM25.b` The b parameter of BM25 (Lucene default value: `0.75`)
        + `Dirichlet`
            - `[int] launch.indexer.BoW.similarity.Dirichlet.mu`
              The mu parameter of Dirichlet (Lucene default value: `2000`)

    * `[int] launch.indexer.BoW.chunks_size`

      The maximum number of documents that are indexed before flushing the data to disk.
      
    </details>

- `Splade`

    <details>
    <summary>Additional Parameters</summary>

    * `[string] launch.indexer.Splade.index_directory`

      The folder where the index will be stored.

    * `[string] launch.indexer.Splade.model`

      The name of the Transformers-based SPLADE model used by this component. All model files must be downloaded and
      placed inside the Transformers cache folder (set by the `TRANSFORMERS_CACHE` environment variable).

    * `[int] launch.indexer.Splade.max_tokens`

      The maximum number of tokens handled by the model.

    * `[double] launch.indexer.Splade.multiplier`

      The SPLADE model returns a list of token-score pairs. Each score is multiplied by the number specified in this
      parameter, then it is rounded to the nearest integer. All pairs with a score less or equal to 0 are discarded,
      while the remaining ones are included in the indexed data of the currently-processed document.

    * `[int] launch.indexer.Splade.batch_size`

      The number of documents processed in parallel by the SPLADE model. It is suggested to set this parameter to 1,
      otherwise it is possible to experience a slowdown in terms of execution time (as we found during our testings).

    * `[int] launch.indexer.Splade.chunks_size`
  
      The maximum number of documents that are indexed before flushing the data to disk.

    </details>

- `Dense`

    <details>
    <summary>Additional Parameters</summary>

    * `[string] launch.indexer.Dense.index_directory`

      The folder where the index will be stored.

    * `[string] launch.indexer.Dense.model`

      The name of the Transformers model used by this component. All model files must be downloaded and placed inside the
      Transformers cache folder (set by the `TRANSFORMERS_CACHE` environment variable).

    * `[int] launch.indexer.Dense.vector_size`

      The number of dimensions of the vector produced by the model.

    * `[int] launch.indexer.Dense.max_tokens`

      The maximum number of tokens handled by the model.

    * `[string] launch.indexer.Dense.similarity`

      Which similarity function to use for evaluating the similarity degree between the query and each document. The
      available options are:

        + `cos` Cosine Similarity
        + `dot` Dot Product
        + `l2` Euclidean Distance
        + `l2sq` Squared Euclidean Distance (no square root computation)

    * `[int] launch.indexer.Dense.batch_size`

      The number of documents processed in parallel by the model. This parameter should be set according to the
      capability of the GPU(s) hardware employed, especially in terms of VRAM usage. A reasonable value can be 16.

    * `[int] launch.indexer.Dense.chunks_size`

      The maximum number of documents that are indexed before flushing the data to disk.

    </details>

## Search Phase Configuration

The search phase is configured using the provided `search.properties` file. In the top part of the file, the user of
DECAF must set:

- `[int] launch.num_documents` The (maximum) number of documents to retrieve for each utterance.
- `[int] launch.num_threads` The number of CPU threads to use to speed-up code execution.
- `launch.topics` Which [TopicsParser](README.md#topicsparser) to instantiate.
- `launch.rewriter` Which [Rewriter](README.md#rewriter) to instantiate.
- `launch.searcher` Which [Searcher](README.md#searcher) to instantiate.
- `launch.reranker` Which [Reranker](README.md#reranker) to instantiate.
- `launch.run_writer` Which [Run Writer](README.md#run-writer) to instantiate.

### Detailed Components Configuration

Most components also requires to set some additional parameters.

#### TopicsParser

- [`AutCAsT19`](#topicsparser)
- [`ManCAsT19`](#topicsparser)
- [`AutCAsT20`](#topicsparser)
- [`RewCAsT20`](#topicsparser)
- [`ManCAsT20`](#topicsparser)

#### Rewriter

- [`No`](#rewriter)

- [`AllenNLP`](#rewriter)

    <details>
    <summary>Additional Parameters</summary>

    * `[string] launch.rewriter.AllenNLP.model`

      The name of the co-reference resolution model used by this component. All model files must be downloaded and placed
      inside the AllenNLP cache folder (set by the `ALLENNLP_CACHE_DATA` environment variable).

    </details>

- [`FastCoref`](#rewriter)

    <details>
    <summary>Additional Parameters</summary>

    * `[string] launch.rewriter.FastCoref.model`

      The name of the co-reference resolution model used by this component. All model files must be downloaded and placed
      inside the Transformers cache folder (set by the `TRANSFORMERS_CACHE` environment variable).

    </details>

- [`T5`](#rewriter)

    <details>
    <summary>Additional Parameters</summary>

    * `[string] launch.rewriter.T5.model`

      The name of the T5 model used by this component. All model files must be downloaded and placed inside the
      Transformers cache folder (set by the `TRANSFORMERS_CACHE` environment variable).

    * `[int] launch.rewriter.T5.max_tokens`

      The maximum number of tokens handled by the T5 model used.
      
    </details>

#### Query Generator

- [`Current`](#query-generator)

- [`All`](#query-generator)

- [`FLC`](#query-generator)

    <details>
    <summary>Additional Parameters</summary>

    * `[double] <base_key>.FLC.qC1` The weight given to the Current query utterance, when the conversation has size 1.
    * `[double] <base_key>.FLC.qF2` The weight given to the First query utterance, when the conversation has size 2.
    * `[double] <base_key>.FLC.qC2` The weight given to the Current query utterance, when the conversation has size 2.
    * `[double] <base_key>.FLC.qF3` The weight given to the First query utterance, when the conversation has size >= 3.
    * `[double] <base_key>.FLC.qL3` The weight given to the Last query utterance, when the conversation has size >= 3.
    * `[double] <base_key>.FLC.qC3` The weight given to the Current query utterance, when the conversation has size >= 3.

    </details>

#### Searcher

- [`BoW`](#searcher)

    <details>
    <summary>Additional Parameters</summary>

    * `[string] launch.searcher.BoW.index_directory`

      The folder where the index has been stored.

    * `[string] launch.searcher.BoW.analyzer`

      Which Lucene analyzer is applied to the utterances' rewritten text. The only pre-defined option available is
      `English`.

    * `[string] launch.searcher.BoW.similarity`

      Which Lucene similarity function is used to score each document. The available options are:

        + `BM25`
            - `[int] launch.searcher.BoW.similarity.BM25.k1` The k1 parameter of BM25 (Lucene default value: `1.2`).
            - `[int] launch.searcher.BoW.similarity.BM25.b` The b parameter of BM25 (Lucene default value: `0.75`)
        + `Dirichlet`
            - `[int] launch.searcher.BoW.similarity.Dirichlet.mu`
              The mu parameter of Dirichlet (Lucene default value: `2000`)

    * `[string] launch.searcher.BoW.query`

      Which [Query Generator](README.md#query-generator) is used to produce the query. See
      [this chapter](#query-generator) for more details about it.

    </details>

- [`Splade`](#searcher)

    <details>
    <summary>Additional Parameters</summary>

    * `[string] launch.searcher.Splade.index_directory`

      The folder where the index has been stored.

    * `[string] launch.searcher.Splade.model`

      The name of the Transformers-based SPLADE model used by this component. All model files must be downloaded and
      placed inside the Transformers cache folder (set by the `TRANSFORMERS_CACHE` environment variable).

    * `[int] launch.searcher.Splade.max_tokens`

      The maximum number of tokens handled by the model.

    * `[string] launch.searcher.Splade.query`

      Which [Query Generator](README.md#query-generator) is used to produce the query. See
      [this chapter](#query-generator) for more details about it.

    </details>

- [`Dense`](#searcher)

    <details>
    <summary>Additional Parameters</summary>

    * `[string] launch.searcher.Dense.index_directory`

      The folder where the index has been stored.

    * `[string] launch.searcher.Dense.model`

      The name of the Transformers model used by this component. All model files must be downloaded and placed inside the
      Transformers cache folder (set by the `TRANSFORMERS_CACHE` environment variable).

    * `[int] launch.searcher.Dense.vector_size`

      The number of dimensions of the vector produced by the model.

    * `[int] launch.searcher.Dense.max_tokens`

      The maximum number of tokens handled by the model.

    * `[string] launch.searcher.Dense.similarity`

      Which similarity function to use for evaluating the similarity degree between the query and each document. The
      available options are:

        + `cos` Cosine Similarity
        + `dot` Dot Product
        + `l2` Euclidean Distance
        + `l2sq` Squared Euclidean Distance (no square root computation)

    * `[string] launch.searcher.Dense.query`

      Which [Query Generator](README.md#query-generator) is used to produce the query. See
      [this chapter](#query-generator) for more details about it.

    </details>

#### Reranker

- [`No`](#reranker)

- [`Transformers`](#reranker)

    <details>
    <summary>Additional Parameters</summary>

    * `[string] launch.reranker.Transformers.model`

      The name of the Transformers model used by this component. All model files must be downloaded and placed inside the
      Transformers cache folder (set by the `TRANSFORMERS_CACHE` environment variable).

    * `[int] launch.reranker.Transformers.vector_size`

      The number of dimensions of the vector produced by the model.

    * `[int] launch.reranker.Transformers.max_tokens`

      The maximum number of tokens handled by the model.

    * `[string] launch.reranker.Transformers.similarity`

      Which similarity function to use for evaluating the similarity degree between the query and each document. The
      available options are:

        + `cos` Cosine Similarity
        + `dot` Dot Product
        + `l2` Euclidean Distance
        + `l2sq` Squared Euclidean Distance (no square root computation)

    * `[string] launch.reranker.Transformers.query`

      Which [Query Generator](README.md#query-generator) is used to produce the query. See
      [this chapter](#query-generator) for more details about it.

    * `[string] launch.reranker.Transformers.fusion`

      Which [Run Fusion](#reranker) is used to merge the rankings produced by the Searcher and by the Transformers model.
      The available options are:

        + `Null`
        + `Linear`

            - `[double] launch.reranker.Transformers.fusion.Linear.alpha` The alpha parameter for Linear run fusion.

        + `ReciprocalRank`

            - `[double] launch.reranker.Transformers.fusion.ReciprocalRank.k` The k parameter for ReciprocalRank run fusion.
            - `[double] launch.reranker.Transformers.fusion.ReciprocalRank.alpha`
              The alpha parameter for ReciprocalRank run fusion.

    </details>

#### Run Writer

- [`TrecEval`](#run-writer)
- [`Debug`](#run-writer)