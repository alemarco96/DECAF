# How to reproduce our work

In this page, we detailed the step-by-step procedure on how to replicate the experiments conducted for the <br>
**DECAF: a Modular and Extensible Conversational Search Framework** <br>
paper. For the remainder of this guide, we assume that the OS used is Linux.

## Prerequisite: Initial setup of the framework

It is required to perform the initial setup of the framework. Please follow the instructions detailed on the
[Installation](INSTALLATION_GUIDE.md) guide.

## Prerequisite: Install trec-eval tool for evaluation

We perform evaluation using the official [trec-eval](https://github.com/usnistgov/trec_eval) open-source tool available
on GitHub. If it is not available on your system, please follow this procedure:

- Open the terminal on the main folder of the framework, then clone the `trec-eval` repository and compile the source
  code using Make:

  ```
  mkdir trec_eval_tool
  cd trec_eval_tool
  git clone https://github.com/usnistgov/trec_eval.git
  make -C trec_eval/
  ```

- Copy the `trec-eval` executable on the main folder, then delete the source code:

  ```
  cp trec_eval/trec_eval ../
  cd ..
  rm -rf trec_eval_tool/
  ```

## Step 1: Download all necessary data

The first step is to download all data necessary to conduct our experiments, following this procedure:

- Download the MS-MARCO and TREC-CAR corpora data from their official websites into the `corpora` sub-folder:

  ```
  cd corpora/
  mkdir MS-MARCO-passages
  cd MS-MARCO-passages/
  wget https://msmarco.blob.core.windows.net/msmarcoranking/collection.tar.gz
  tar -xf collection.tar.gz
  rm collection.tar.gz
  cd ../TREC-CAR/
  wget https://trec-car.cs.unh.edu/datareleases/v2.0/paragraphCorpus.v2.0.tar.xz
  tar -xf paragraphCorpus.v2.0.tar.xz
  mv paragraphCorpus/dedup.articles-paragraphs.cbor dedup.articles-paragraphs.cbor
  rm -r paragraphCorpus/
  rm paragraphCorpus.v2.0.tar.xz
  cd ..
  ```

- Download the list of duplicates for MS-MARCO passages dataset, provided by TREC CAsT organizers:

  ```
  cd corpora/MS-MARCO-passages/
  wget https://boston.lti.cs.cmu.edu/Services/treccast19/duplicate_list_v1.0.txt
  cd ../..
  ```

- Download the topics for TREC CAsT 2019 and 2020 into the `topics` sub-folder:

  ```
  cd topics/
  wget https://raw.githubusercontent.com/daltonj/treccastweb/master/2019/data/evaluation/evaluation_topics_v1.0.json
  mv evaluation_topics_v1.0.json cast2019_automatic_evaluation.json
  wget https://raw.githubusercontent.com/daltonj/treccastweb/master/2019/data/evaluation/evaluation_topics_annotated_resolved_v1.0.tsv
  mv evaluation_topics_annotated_resolved_v1.0.tsv cast2019_manual_evaluation.txt
  wget https://raw.githubusercontent.com/daltonj/treccastweb/master/2020/2020_automatic_evaluation_topics_v1.0.json
  mv 2020_automatic_evaluation_topics_v1.0.json cast2020_automatic_evaluation.json
  wget https://github.com/daltonj/treccastweb/blob/master/2020/2020_manual_evaluation_topics_v1.0.json
  mv 2020_manual_evaluation_topics_v1.0.json cast2020_manual_evaluation.json
  cd ..
  ```

- Download the qrels for TREC CAsT 2019 and 2020 into the `qrels` sub-folder:

  ```
  cd qrels/
  wget https://raw.githubusercontent.com/daltonj/treccastweb/master/2019/data/2019qrels.txt
  mv 2019qrels.txt cast2019_evaluation.qrels
  wget https://raw.githubusercontent.com/daltonj/treccastweb/master/2020/2020qrels.txt
  mv 2020qrels.txt cast2020_evaluation.qrels
  cd ..
  ```

## Step 2: Index the corpora

The next step is to index the corpora, using all three different approaches detailed in the paper (`BM25` and
`SPLADE` bag-of-words and `BERT` dense retrieval models). In practice, you need to execute this phase once for every
approach, following this two-steps procedure:

- Adjust the index parameters by editing the `index.properties` configuration file inside the `config` sub-folder. More
  details are provided below in the remainder of this chapter.


- Execute the index phase, using the provided script:

  ```
  ./scripts/index.sh
  ```


The index configuration file must be adjusted according to this procedure:

- Set the `launch.corpus` to `CAsT1920`, and comment out with `#` all other options for the same component:

  ```
  launch.corpus = CAsT1920
  ```

- Use these parameters based on which approach you wish to replicate:

    * `BM25` approach:

        + Select the `launch.indexer` to `BoW`, and comment out with `#` all other options for the same component:

          ```
          launch.indexer = BoW
          #launch.indexer = Splade
          #launch.indexer = Dense
          ```

        + Set the similarity to `BM25` with parameters `k1 = 0.82` and `b = 0.4`, and comment out with `#` all other
          options:

          ```
          launch.indexer.BoW.similarity = BM25
          launch.indexer.BoW.similarity.BM25.k1 = 0.82
          launch.indexer.BoW.similarity.BM25.b = 0.4
          #launch.indexer.BoW.similarity = Dirichlet
          ```

        + All other parameters could be left with their default parameters.

    * `BERT` approach:

        + Note that this approach occupies a lot of disk space: it takes more than `2 KB` for every document,
          therefore the index for TREC CAsT 2019 and 2020 requires approximately `100 GB` of free disk space.

        + Select the `launch.indexer` indexer to `Dense`, and comment out with `#` all other options for the same
          component:

          ```
          #launch.indexer = BoW
          #launch.indexer = Splade
          launch.indexer = Dense
          ```

        + Set the model used to generate the embeddings to `distilbert-dot-tas_b-b256-msmarco`, together with its
          vector size (`768`) and maximum number of tokens (`512`) and with the dot-product similarity function used
          (`dot`):

          ```
          launch.indexer.Dense.model = distilbert-dot-tas_b-b256-msmarco
          launch.indexer.Dense.vector_size = 768
          launch.indexer.Dense.max_tokens = 512
          launch.indexer.Dense.similarity = dot
          #launch.indexer.Dense.similarity = cos
          #launch.indexer.Dense.similarity = l2
          #launch.indexer.Dense.similarity = l2sq
          ```

        + All other parameters could be left with their default parameters.

    * `SPLADE` approach:

        + Select the `launch.indexer` indexer to `Splade`, and comment out with `#` all other options for the same
          component:

          ```
          #launch.indexer = BoW
          launch.indexer = Splade
          #launch.indexer = Dense
          ```

        + Set the SPLADE model used for the documents to `efficient-splade-V-large-doc`, together with its maximum
          number of tokens (`512`) and with the multiplier used (`1000`):

          ```
          launch.indexer.Splade.model = efficient-splade-V-large-doc
          launch.indexer.Splade.max_tokens = 512
          launch.indexer.Splade.multiplier = 1000
          ```

        + All other parameters could be left with their default parameters.

## Step 3: Perform the experiments

After the index phase has been completed, it is possible to replicate the experiments we conducted. Similarly to the
previous phase, it is required to adjust the `search.properties` configuration file, then execute the desired operation
using the `./scripts/search.sh` command.

We tested different configurations for each of the four main components of the framework (Topics Parser, Rewriter,
Searcher and Reranker), which are referred to by unique IDs in the remainder of this chapter. The configuration
parameters that must be set inside the `search.properties` file are given below. The list of experiments that we carried
out is detailed in the following tables: the first one refers to TREC CAsT 2019, while the second one is for the 2020
edition. We also reported the four evaluation metrics used in the paper (Recall with cutoff 100, Mean Reciprocal Rank
and Normalized Discounted Cumulative Gain with cutoffs 3 and 10).

Note that, for being able to replicate our experiments using the dense retrieval Searcher based on the `BERT`model,
it is required to load the whole index to RAM. This means that at least 100 GB of RAM is required for TREC CAsT 2019
and 2020 datasets.

- Table of experiments on TREC CAsT 2019:

| Number |  Topics   | Rewriter |  Searcher  | Reranker | Recall@100 | MRR  | NDCG@3 | NDCG@10 |
|:------:|:---------:|:--------:|:----------:|:--------:|:----------:|:----:|:------:|:-------:|
|   1    | Automatic |    --    |   BM25_C   |    --    |    19.9    | 31.8 |  13.7  |  14.2   |
|   2    | Automatic |    CR    |   BM25_C   |    --    |    35.2    | 52.0 |  25.5  |  25.3   |
|   3    | Automatic |    T5    |   BM25_C   |    --    |    42.6    | 64.5 |  33.7  |  32.0   |
|   4    | Automatic |    --    |  BM25_FLC  |    --    |    23.9    | 40.6 |  18.3  |  17.9   |
|   5    | Automatic |    CR    |  BM25_FLC  |    --    |    39.8    | 57.6 |  28.9  |  27.6   |
|   6    | Automatic |    T5    |  BM25_FLC  |    --    |    44.8    | 65.6 |  34.1  |  32.0   |
|   7    | Automatic |    --    |   BM25_C   |   BERT   |    19.9    | 48.1 |  27.8  |  24.7   |
|   8    | Automatic |    CR    |   BM25_C   |   BERT   |    35.2    | 68.5 |  42.3  |  38.0   |
|   9    | Automatic |    T5    |   BM25_C   |   BERT   |    42.6    | 79.4 |  49.8  |  44.4   |
|   10   | Automatic |    --    |  BM25_FLC  |   BERT   |    23.9    | 51.5 |  30.5  |  27.1   |
|   11   | Automatic |    CR    |  BM25_FLC  |   BERT   |    39.8    | 70.6 |  44.6  |  40.4   |
|   12   | Automatic |    T5    |  BM25_FLC  |   BERT   |    44.8    | 80.3 |  49.8  |  45.3   |
|   13   | Automatic |    --    |   BERT_C   |    --    |    19.0    | 22.7 |  11.9  |  14.0   |
|   14   | Automatic |    CR    |   BERT_C   |    --    |    33.5    | 40.1 |  23.1  |  25.1   |
|   15   | Automatic |    T5    |   BERT_C   |    --    |    43.0    | 52.3 |  30.1  |  32.6   |
|   16   | Automatic |    T5    |  BERT_FLC  |    --    |    43.0    | 52.3 |  30.1  |  32.6   |
|   17   | Automatic |    --    |  SPLADE_C  |    --    |    24.7    | 44.2 |  27.0  |  26.1   |
|   18   | Automatic |    CR    |  SPLADE_C  |    --    |    41.8    | 67.5 |  44.1  |  41.6   |
|   19   | Automatic |    T5    |  SPLADE_C  |    --    |    51.4    | 79.6 |  51.9  |  49.5   |
|   20   | Automatic |    T5    | SPLADE_FLC |    --    |    48.1    | 75.5 |  48.4  |  44.0   |
|   21   |  Manual   |    --    |   BM25_C   |    --    |    47.5    | 67.2 |  34.7  |  34.4   |
|   22   |  Manual   |    --    |   BM25_C   |   BERT   |    47.5    | 82.3 |  53.9  |  47.5   |
|   23   |  Manual   |    --    |   BERT_C   |    --    |    46.2    | 54.0 |  32.5  |  34.8   |
|   24   |  Manual   |    --    |  SPLADE_C  |    --    |    54.8    | 83.7 |  56.0  |  52.7   |

- Table of experiments on TREC CAsT 2020:

| Number |  Topics   | Rewriter | Searcher | Reranker | Recall@100 | MRR  | NDCG@3 | NDCG@10 |
|:------:|:---------:|:--------:|:--------:|:--------:|:----------:|:----:|:------:|:-------:|
|   1    | Automatic |    T5    |  BM25_C  |    --    |    29.1    | 26.8 |  16.7  |  17.6   |
|   2    | Automatic |    T5    |  BM25_C  |   BERT   |    29.1    | 43.0 |  31.3  |  29.2   |
|   3    | Automatic |    T5    |  BERT_C  |    --    |    40.2    | 34.2 |  23.6  |  23.2   |
|   4    | Automatic |    T5    | SPLADE_C |    --    |    46.5    | 45.6 |  35.0  |  32.3   |
|   5    |  Manual   |    --    |  BM25_C  |    --    |    41.4    | 40.2 |  25.3  |  25.7   |
|   6    |  Manual   |    --    |  BM25_C  |   BERT   |    41.4    | 57.1 |  42.7  |  40.2   |
|   7    |  Manual   |    --    |  BERT_C  |    --    |    56.2    | 50.6 |  35.4  |  34.3   |
|   8    |  Manual   |    --    | SPLADE_C |    --    |    61.3    | 62.4 |  47.4  |  44.5   |

- Configuration parameters to set in the `search.properties` file:

    * `Topics`:

        + TREC CAsT 2019 experiments:

            - `Automatic`:

            ```
            launch.topics = AutCAsT19
            #launch.topics = ManCAsT19
            #launch.topics = AutCAsT20
            #launch.topics = RewCAsT20
            #launch.topics = ManCAsT20
            ```

            - `Manual`:

            ```
            #launch.topics = AutCAsT19
            launch.topics = ManCAsT19
            #launch.topics = AutCAsT20
            #launch.topics = RewCAsT20
            #launch.topics = ManCAsT20
            ```

        + TREC CAsT 2020 experiments:

            - `Automatic`:

            ```
            #launch.topics = AutCAsT19
            #launch.topics = ManCAsT19
            launch.topics = AutCAsT20
            #launch.topics = RewCAsT20
            #launch.topics = ManCAsT20
            ```

            - `Manual`:

            ```
            #launch.topics = AutCAsT19
            #launch.topics = ManCAsT19
            #launch.topics = AutCAsT20
            #launch.topics = RewCAsT20
            launch.topics = ManCAsT20
            ```

    * `Rewriter`:

        + `--`:

          ```
          launch.rewriter = No
          #launch.rewriter = AllenNLP
          #launch.rewriter = FastCoref
          #launch.rewriter = T5
          ```

        + `CR`:

          ```
          #launch.rewriter = No
          #launch.rewriter = AllenNLP
          launch.rewriter = FastCoref
          #launch.rewriter = T5

          launch.rewriter.FastCoref.model = f-coref
          ```

        + `T5`:

          ```
          #launch.rewriter = No
          #launch.rewriter = AllenNLP
          #launch.rewriter = FastCoref
          launch.rewriter = T5

          launch.rewriter.T5.model = t5-base-canard
          launch.rewriter.T5.max_tokens = 512
          ```

    * `Searcher`:

        + Approach `BM25_xxx`:

          ```
          launch.searcher = BoW
          #launch.searcher = Splade
          #launch.searcher = Dense
              
          launch.searcher.BoW.index_directory = $(ROOT_FOLDER)/indexes/CAsT1920__BoW_$(launch.searcher.BoW.analyzer)_$(launch.searcher.BoW.similarity)
          launch.searcher.BoW.analyzer = English
          launch.searcher.BoW.similarity = BM25
          launch.searcher.BoW.similarity.BM25.k1 = 0.82
          launch.searcher.BoW.similarity.BM25.b = 0.4
          #launch.searcher.BoW.similarity = Dirichlet
          launch.searcher.BoW.similarity.Dirichlet.mu = 2000
          ```

            - Query Generator `BM25_C`:

              ```
              launch.searcher.BoW.query = Current
              #launch.searcher.BoW.query = All
              #launch.searcher.BoW.query = FLC
              ```

            - Query Generator `BM25_FLC`:

              ```
              #launch.searcher.BoW.query = Current
              #launch.searcher.BoW.query = All
              launch.searcher.Lucene.query = FLC

              launch.searcher.Lucene.query.FLC.qF1 = 1.0
              launch.searcher.Lucene.query.FLC.qF2 = 0.25
              launch.searcher.Lucene.query.FLC.qC2 = 0.75
              launch.searcher.Lucene.query.FLC.qF3 = 0.10
              launch.searcher.Lucene.query.FLC.qL3 = 0.15
              launch.searcher.Lucene.query.FLC.qC3 = 0.75
              ```

        + Approach `BERT_xxx`:

          ```
          #launch.searcher = BoW
          #launch.searcher = Splade
          launch.searcher = Dense

          launch.searcher.Dense.index_directory = $(ROOT_FOLDER)/indexes/CAsT1920__Dense_$(launch.searcher.Dense.model)_$(launch.searcher.Dense.similarity)
          launch.searcher.Dense.model = distilbert-dot-tas_b-b256-msmarco
          #launch.searcher.Dense.model = multi-qa-mpnet-base-dot-v1
          launch.searcher.Dense.vector_size = 768
          launch.searcher.Dense.max_tokens = 512
          launch.searcher.Dense.similarity = dot
          #launch.searcher.Dense.similarity = cos
          #launch.searcher.Dense.similarity = l2
          #launch.searcher.Dense.similarity = l2sq
          ```

            - Query Generator `BERT_C`:

              ```
              launch.searcher.Faiss.query = Current
              #launch.searcher.Faiss.query = All
              #launch.searcher.Faiss.query = FLC
              ```

            - Query Generator `BERT_FLC`:

              ```
              #launch.searcher.Faiss.query = Current
              #launch.searcher.Faiss.query = All
              launch.searcher.Faiss.query = FLC

              launch.searcher.Faiss.query.FLC.qF1 = 1.0
              launch.searcher.Faiss.query.FLC.qF2 = 0.25
              launch.searcher.Faiss.query.FLC.qC2 = 0.75
              launch.searcher.Faiss.query.FLC.qF3 = 0.10
              launch.searcher.Faiss.query.FLC.qL3 = 0.15
              launch.searcher.Faiss.query.FLC.qC3 = 0.75
              ```

        + Approach `SPLADE_xxx`:

          ```
          #launch.searcher = BoW
          launch.searcher = Splade
          #launch.searcher = Dense

          launch.searcher.Splade.index_directory = $(ROOT_FOLDER)/indexes/CAsT1920__Splade_efficient-splade-V-large-doc
          launch.searcher.Splade.model = efficient-splade-V-large-query
          launch.searcher.Splade.max_tokens = 512
          ```

            - Query Generator `SPLADE_C`:

              ```
              launch.searcher.Splade.query = Current
              #launch.searcher.Splade.query = All
              #launch.searcher.Splade.query = FLC
              ```

            - Query Generator `SPLADE_FLC`:

              ```
              #launch.searcher.Splade.query = Current
              #launch.searcher.Splade.query = All
              launch.searcher.Splade.query = FLC

              launch.searcher.Splade.query.FLC.qF1 = 1.0
              launch.searcher.Splade.query.FLC.qF2 = 0.25
              launch.searcher.Splade.query.FLC.qC2 = 0.75
              launch.searcher.Splade.query.FLC.qF3 = 0.10
              launch.searcher.Splade.query.FLC.qL3 = 0.15
              launch.searcher.Splade.query.FLC.qC3 = 0.75
              ```

    * `Reranker`:

        + `--`:

          ```
          launch.reranker = No
          #launch.reranker = Transformers
          ```

        + `BERT`:

          ```
          #launch.reranker = No
          launch.reranker = Transformers

          launch.reranker.Transformers.model = distilbert-dot-tas_b-b256-msmarco
          #launch.reranker.Transformers.model = multi-qa-mpnet-base-dot-v1
          launch.reranker.Transformers.vector_size = 768
          launch.reranker.Transformers.max_tokens = 512
          launch.reranker.Transformers.similarity = dot

          launch.reranker.Transformers.query = Current
          #launch.reranker.Transformers.query = All
          #launch.reranker.Transformers.query = FLC

          launch.reranker.Transformers.fusion = No
          #launch.reranker.Transformers.fusion = Linear
          #launch.reranker.Transformers.fusion = ReciprocalRank
          ```

- It is also required to set the `RunWriter` component, which generates the output run. It is possible to select
  between two options:

  * `TrecEval`:

    Generate the run using TREC-eval format.

    ```
    launch.run_writer = TrecEval
    #launch.run_writer = Debug
    ```

  * `Debug`:

    Generate a directory containing multiple output files. Refer to the documentation for further details. Note that
    the run in TREC-eval format can be found in the `full.txt` file inside the `reranked` sub-folder.

    ```
    #launch.run_writer = TrecEval
    launch.run_writer = Debug
    ```

## Step 4: Perform evaluation of the generated runs

After step 3, it is possible to perform evaluation of the run generated for each experiment. To perform this phase, we
use the `trec-eval` tool from command-line, according to this procedure:

- Open the terminal from the main folder of the framework.


- Identify the path relative to the `runs` sub-folder to the file containing the desired run:

  ```
  RUN_FILENAME=path/to/run/file
  ```

- Perform evaluation:

  * TREC CAsT 2019 experiments:

    ```
    ./trec_eval -m recall.100 -m recip_rank -m ndcg_cut.3,10 qrels/cast2019_evaluation.qrels runs/$RUN_FILENAME
    ```

  * TREC CAsT 2020 experiments:

    ```
    ./trec_eval -l 2 -m recall.100 -m recip_rank -m ndcg_cut.3,10 qrels/cast2020_evaluation.qrels runs/$RUN_FILENAME
    ```

    Note that we deem relevance scale >= 2 as positive for both MRR and Recall metrics, as required by the official
    evaluation setting for this dataset.