# DECAF

![DECAF Logo](logos/decaf.png "DECAF Logo")

DECAF is a framework for performing conversational search. Its modular architecture enables it to easily extend and
adapt to suit most applications' needs while reusing most of the pre-defined components. The core of DECAF is written
in Java, while some components are implemented in Python. All machine-learning code supports acceleration through
CUDA-enabled GPU. It is developed at the
[Intelligent Interactive Information Access Hub](https://iiia.dei.unipd.it/),
[Department of Information Engineering (DEI)](https://www.dei.unipd.it/home-page),
[University of Padua](https://www.unipd.it/).

![IIIA Logo](logos/iiia.png "IIIA Logo") ![DEI Logo](logos/dei.png "DEI Logo") ![UniPD Logo](logos/unipd.png "UniPD Logo")

## License ##

All the contents of this repository are shared using the
[Creative Commons Attribution-ShareAlike 4.0 International License](http://creativecommons.org/licenses/by-sa/4.0/).

![CC logo](https://i.creativecommons.org/l/by-sa/4.0/88x31.png)

## Repository Structure ##

The repository is organized following this structure:

- `SIGIR-experiments`

  This folder contains the data about the experiments shown in Section 5 'Experimental Results' of the paper.

  * `TREC CAsT 2019`

    This folder contains all experiments conducted on the TREC CAsT 2019 dataset. The evaluation measures are reported
    on Table 4 of the paper.

  * `TREC CAsT 2020`

    This folder contains all experiments conducted on the TREC CAsT 2020 dataset. The evaluation measures are reported
    on Table 5 of the paper.

  For each experiment, inside a subdirectory with the number of the experiment reported in the paper, we include the
  configuration file employed as well as the run generated and the evaluation measures produced by `trec_eval` tool.


- `src`

  This folder contains the source code of DECAF.


- `template`

  This folder contains the structure of directories created to ease the framework instantiation.

  * `config`

    This folder contains the properties files defining which components are instantiated along with their parameters.

  * `corpora`

    This folder must be populated with the corpora data that will be processed by the index pipeline.

  * `indexes`

    This folder contains the indexes data that will be produced by the index pipeline.

  * `models`

    This folder contains the machine-learning models employed. It must be populated according to the procedure shown in
    [Section 3](INSTALLATION_GUIDE.md#step-3--download-all-models) of the Installation Guide.

  * `runs`

    This folder contains the runs that will be generated using the search pipeline.

  * `scripts`

    This folder contains the scripts used to execute either the index or the search pipeline.

  * `topics`

    This folder must be populated with the evaluation topics files that will be used during the search phase.

  * `venv`

    This folder contains the Python virtual environments employed by the components. It must be populated according to
    the procedure shown in [Section 2](INSTALLATION_GUIDE.md#step-2--setup-python-virtual-environments) of the
    Installation Guide.


- [`CONFIGURATION_GUIDE.md`](CONFIGURATION_GUIDE.md)

  The guide describing how to configure the properties files and execute experiments using DECAF.


- [`INSTALLATION_GUIDE.md`](INSTALLATION_GUIDE.md)

  The guide describing how to install and setup DECAF.


- [`README.md`](README.md)

  This readme.


- [`REPRODUCIBILITY.md`](REPRODUCIBILITY_GUIDE.md)

  The guide describing how to reproduce the results shown in the paper.


- `install_scripts`

  This folder contains the scripts used to install the framework as well as the data needed to reproduce the
  experiments shown in Section 5 'Experimental Results' of the paper.

  Note that all scripts **MUST** be launched from the root folder of DECAF.

  * `install.sh`

    The bash script used to execute the installation procedure for DECAF. It is equivalent to the entire procedure
    shown in the [`Installation Guide`](INSTALLATION_GUIDE.md).

  * `reproducibility.sh`

    The bash script used to download all data needed to replicate our experiments. It is equivalent to Step 1 of the
    [`Reproducibility Guide`](REPRODUCIBILITY_GUIDE.md). To replicate our experiments, first execute this script
    successfully, then follow the guide starting from Step 2.


- `allennlp_spacy_transformers.yml`

  The conda environmental file needed to create the `allennlp_spacy_transformers` Python virtual environment.


- `faiss_fastcoref_spacy_transformers.yml`

  The conda environmental file needed to create the `faiss_fastcoref_spacy_transformers` Python virtual environment.


- `logos`

  This folder contains the logos used in this readme.


  * `decaf.png`

    The logo of DECAF.


  * `iiia.png`

    The logo of [Intelligent Interactive Information Access Hub](https://iiia.dei.unipd.it/).


  * `dei.png`

    The logo of [Department of Information Engineering (DEI)](https://www.dei.unipd.it/home-page).


  * `unipd.png`

    The logo of [University of Padua](https://www.unipd.it/).


- `pom.xml`

  The Maven pom file.


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

  It processes each document provided by the CorpusParser and stores relevant information in a data structure called
  index, which is stored on disk for later performing document retrieval through the Searcher component.


Instead, the Search Pipeline is composed of five main components:

- `TopicsParser`

  It provides new conversations and utterances to be processed by the remainder of the framework.

- `Rewriter`

  It modifies the text of the question by adding contextual and implied information extracted from previous utterances
  in the conversation. Its goal is to enhance the retrieval performance of the third component, the Searcher.

- `Searcher`

  It generates the query from the rewritten text of the utterance(s), then retrieves a small set of candidate documents
  to answer the provided question. The ranked list of documents generated as output is then consumed by the Reranker.

- `Reranker`

  It is designed to provide a better ranking, therefore boosting the performance of the whole system. This component
  usually carries out expensive computations, which are however focused only on the candidate documents.

- `RunWriter`

  Finally, the RunWriter delivers the result computed from the system to the user.

## Components Implemented

DECAF comes with several components already available out of the box.

### CorpusParser

- `MSMARCOv1`

  It allows for parsing passages contained within MS-MARCO version 1 dataset. There is also support for duplicate
  removal, by providing an additional textual file. Each line must use the
  `<KEEP ID>:<DUPLICATE 1 ID>,<DUPLICATE 2 ID>,...\n` format, so all documents whose ID appears as a duplicate
  are discarded.

- `TRECCARv2`

  It addresses the paragraph corpus of TREC CAR v2.0 dataset.

- `Tsv`

  It processes any corpus based on tab-separated files using the `<DOC ID>\t<DOC TEXT>\n` format.

- `Multi`

  It fuses the stream of documents extracted by some other CorpusParsers. It is well suited for indexing data coming
  from multiple corpora, where each of them is parsed using one of the other implemented CorpusParser.

### Indexer

- `BoW`

  It is a wrapper around Lucene indexing operations. Its efficient inverted index implementation makes it suitable
  for bag-of-words sparse retrieval models.

- `Splade`

  This BoW indexer implementation is specific for the homonyms neural retrieval model. It replaces the standard
  tokenization and analysis pipeline performed by Lucene with the SPLADE model inference.

- `Dense`

  It is specific for dense retrieval models. It is built on top of [Transformers (Hugging Face)](https://huggingface.co/)
  library, which handles the models, and of [FAISS](https://faiss.ai/) library, which handles the storage of the
  document embeddings.

### TopicsParser

All available components are designed to parse [TREC CAsT](https://www.treccast.ai/) evaluation topics for Years 1 and
2, held respectively in 2019 and 2020. The prefix in their names represents the different kinds of utterances returned.

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
  suited for implementing bag-of-words retrieval models. It provides good results when using the BM25 scoring function,
  which is often used as an evaluation baseline. It should be paired with a reranker to improve performance measures.

- `Splade`

  It performs first stage retrieval using [SPLADE](https://github.com/naver/splade), a neural retrieval model which
  learns sparse representation for both queries and documents via the BERT MLM head and sparse regularization. It
  produces a list of term-weight pairs, that is indexed using [Lucene](https://lucene.apache.org/). Please note that,
  during index phase, each weight given by SPLADE is multiplied by a constant factor (such as 100 or 1000) and then
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

- `Sequence`

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

    It produces the query embedding, which will be compared against each document to produce their new score. The
    available options are the same as above.

  * Similarity Function
  
    The similarity function is used to evaluate the similarity degree between the query and each document. The available
    options are:
  
    + `cos` Cosine Similarity
    + `dot` Dot Product
    + `l2` Euclidean Distance
    + `l2sq` Squared Euclidean Distance (no square root computation)

  * Run Fusion

    The ranked list produced by the Transformers model can be fused with the original one given by the Searcher using a
    run fusion technique. The available options are:
  
    + `No`
    
      No fusion is applied.

    + `Linear`
    
      For each document, the new score is computed by applying a linear combination between the score of the Searcher,
      weighted `1.0 - alpha`, and the one given by the Transformers model, weighted `alpha`. Note that both ranked
      lists are min-max normalized before applying the linear combination.
    
    + `ReciprocalRank`
    
      For each document, the formula `1.0 / (k + rank[doc])` is used to obtain a score from both ranked lists, which
      are then combined using a linear combination of parameter `alpha`. To obtain the same results as the standard
      reciprocal rank run fusion technique, use `k = 60` and `alpha = 0.5`.

### Run Writer

- `TrecEval`

  It produces a run using the standard TREC eval format.

- `Debug`

  It produces a directory containing multiple files providing information about system performance and behavior. It
  contains various runs in TREC eval format:

  * Full run generated from the rankings given by the Searcher and Reranker.
  * One run for each conversation processed.
  * One run for all utterance with the same turn in their conversation.

  as well as the list of the top 10 responses produced by the system for each utterance.
