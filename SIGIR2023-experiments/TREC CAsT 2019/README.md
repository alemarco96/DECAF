# TREC CAsT 2019 Experiments

This folder contains all experiments conducted on the TREC CAsT 2019 dataset.

- For each experiment, there is a folder, dubbed as the associated experiment number, containing:

  * `search.properties`

    The configuration file employed for this experiment.

  * `run.txt`

    The run generated for this experiment.

  * `evaluation.txt`

    The evaluation measures computed using the `trec_eval` tool for the run of this experiment.


The evaluation measures reported in the following table are the same as whose reported on Table 4 of the paper.

| Number |  Topics   | Rewriter |  Searcher  | Reranker | Recall@100 |   MRR    |  NDCG@3  | NDCG@10  |
|:------:|:---------:|:--------:|:----------:|:--------:|:----------:|:--------:|:--------:|:--------:|
|   1    | Automatic |    --    |   BM25_C   |    --    |    19.9    |   32.0   |   14.2   |   14.4   |
|   2    | Automatic |    CR    |   BM25_C   |    --    |    35.3    |   51.5   |   25.9   |   25.4   |
|   3    | Automatic |    T5    |   BM25_C   |    --    |    42.8    |   64.0   |   33.9   |   32.1   |
|   4    | Automatic |    --    |  BM25_FLC  |    --    |    23.9    |   41.0   |   19.1   |   18.4   |
|   5    | Automatic |    T5    |  BM25_FLC  |    --    |    45.0    |   65.6   |   34.4   |   32.5   |
|   6    | Automatic |    --    |   BM25_C   |   BERT   |    19.9    |   48.0   |   28.4   |   24.9   |
|   7    | Automatic |    T5    |   BM25_C   |   BERT   |    42.8    |   79.3   |   50.4   |   45.2   |
|   8    | Automatic |    --    |  BM25_FLC  |   BERT   |    23.9    |   51.7   |   30.9   |   27.5   |
|   9    | Automatic |    T5    |  BM25_FLC  |   BERT   |    45.0    |   79.4   |   50.3   |   46.0   |
|   10   | Automatic |    T5    |   BERT_C   |    --    |    43.2    |   52.3   |   30.4   |   33.1   |
|   11   | Automatic |    --    |  SPLADE_C  |    --    |    24.8    |   44.8   |   27.5   |   26.7   |
|   12   | Automatic |    T5    |  SPLADE_C  |    --    |  **51.5**  | **79.9** | **52.3** | **50.1** |
|   13   |  Manual   |    --    |   BM25_C   |    --    |    47.8    |   66.7   |   35.4   |   34.5   |
|   14   |  Manual   |    --    |   BM25_C   |   BERT   |    47.8    |   82.5   |   54.4   |   48.2   |
|   15   |  Manual   |    --    |   BERT_C   |    --    |    46.4    |   54.3   |   32.8   |   35.5   |
|   16   |  Manual   |    --    |  SPLADE_C  |    --    |  **54.9**  | **84.3** | **56.6** | **53.5** |