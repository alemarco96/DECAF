# TREC CAsT 2020 Experiments

This folder contains all experiments conducted on the TREC CAsT 2020 dataset.

- For each experiment, there is a folder, dubbed as the associated experiment number, containing:

  * `search.properties`

    The configuration file employed for this experiment.

  * `run.txt`

    The run generated for this experiment.

  * `evaluation.txt`

    The evaluation measures computed using the `trec_eval` tool for the run of this experiment.


The evaluation measures reported in the following table are the same as whose reported on Table 5 of the paper.

| Number |  Topics   | Rewriter | Searcher | Reranker | Recall@100 | MRR  | NDCG@3 | NDCG@10 |
|:------:|:---------:|:--------:|:--------:|:--------:|:----------:|:----:|:------:|:-------:|
|   1    | Automatic |    T5    |  BM25_C  |    --    |    29.6    | 26.9 |  16.9  |  18.0   |
|   2    | Automatic |    T5    |  BM25_C  |   BERT   |    29.6    | 43.8 |  31.3  |  29.5   |
|   3    | Automatic |    T5    |  BERT_C  |    --    |    40.4    | 34.2 |  23.6  |  23.5   |
|   4    | Automatic |    T5    | SPLADE_C |    --    |    46.7    | 45.6 |  35.1  |  32.7   |
|   5    |  Manual   |    --    |  BM25_C  |    --    |    41.7    | 40.3 |  25.8  |  26.0   |
|   6    |  Manual   |    --    |  BM25_C  |   BERT   |    41.7    | 58.4 |  43.7  |  40.7   |
|   7    |  Manual   |    --    |  BERT_C  |    --    |    56.4    | 50.8 |  35.6  |  34.7   |
|   8    |  Manual   |    --    | SPLADE_C |    --    |    61.5    | 62.4 |  47.8  |  44.9   |