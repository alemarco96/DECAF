# Installation Guide

## Hardware and Software Requirements

In order to execute this framework, these requirements must be fulfilled:
- Git version control system
- Java 11 JDK
- [Maven](https://maven.apache.org/) build tool for Java
- Python 3.8
- [Conda](https://conda.io/projects/conda/en/latest/index.html) and [Pip](https://pypi.org/project/pip/) package
  managers for Python
- (Highly recommended) At least 1 Nvidia GPU with support for CUDA 11.3.

For the remainder of this guide, we assume that the OS used is Linux.

## Step 1: Build the framework from source

The Framework must be built according to the following procedure:

- Select a location on disk where to install the framework. Then create a new folder and clone this repository inside:

  ```
  cd /path/to/desired/location
  git clone https://github.com/alemarco96/DECAF.git
  cd DECAF
  DECAF_ROOT_FOLDER="$(pwd)"
  ```

- Build the main executable JAR file using Maven:

  ```
  mvn package
  ```

- (Optional) Generate the JavaDoc documentation of the source code:

  ```
  JAR_FILENAME=$(ls target/ | grep ".*-jar.*.jar")
  javadoc -protected -d javadoc -sourcepath src/main/java -subpackages it.unipd.dei \
    -classpath target/$JAR_FILENAME
  ```

  It will be placed inside the `javadoc` subdirectory inside the root folder.

## Step 2: Setup Python virtual environments

The configuration files provided with the framework assume that all Python package dependencies have been installed
according to the following procedure:

- Make sure that both `conda` and `pip` have been updated to a recently-released version. In order to do so, use the
  following commands:
  ```
  conda update -n base -c defaults conda
  pip install pip --upgrade pip
  ```

- Choose a folder where all Python virtual environments will be stored. For the remainder of this guide, the `venv`
  subdirectory inside the root folder will be used. In case you choose differently, just change it with the appropriate
  value. Make sure to not put a forward slash (`/`) at the end of the command.

  ```
  DECAF_VENV_FOLDER="$DECAF_ROOT_FOLDER/venv"
  ```

- Create the `faiss_fastcoref_spacy_transformers` virtual environment:
  ```
  conda env create -f faiss_fastcoref_spacy_transformers.yml -p $DECAF_VENV_FOLDER/faiss_fastcoref_spacy_transformers
  
  conda activate $DECAF_VENV_FOLDER/faiss_fastcoref_spacy_transformers
  python -m spacy download en_core_web_sm
  conda deactivate
  ```

- Create the `splade` virtual environment:

  * Get the `.yml` environment configuration file from the official SPLADE [repository](https://github.com/naver/splade)
    on GitHub:
    ```
    wget https://raw.githubusercontent.com/naver/splade/main/conda_splade_env.yml
    ```

  * Use it to create the virtual environment:
    ```
    conda env create -f conda_splade_env.yml -p $DECAF_VENV_FOLDER/splade
    ```

- Create the `allennlp_spacy_transformers` virtual environment:
  ```
  conda env create -f allennlp_spacy_transformers.yml -p $DECAF_VENV_FOLDER/allennlp_spacy_transformers
  
  conda activate $DECAF_VENV_FOLDER/allennlp_spacy_transformers
  python -m spacy download en_core_web_sm
  conda deactivate
  ```
  
  This virtual environment is required by the `AllenNLP` rewriter only. If you don't plan of using this component,
  this step can be safely skipped. Note that the `FastCoref` rewriter performs the same job using a different library,
  the results are (mostly) identical, and it is faster.

## Step 3: Download all models

This framework relies on some machine-learning models to perform its job. Before being able to use the framework,
users are required to download them and save them on disk. Please follow this procedure:

- Choose a folder where all machine-learning models will be stored. For the remainder of this guide, the `models`
  subdirectory inside the root folder will be used. In case you choose differently, just change it with the appropriate
  value. Make sure to not put a forward slash (`/`) at the end of the command.
  ```
  DECAF_MODELS_FOLDER="$DECAF_ROOT_FOLDER/models"
  ```

- Create the folder where to place all AllenNLP models:

  ```
  mkdir $DECAF_MODELS_FOLDER/allennlp
  ```

- Create the folder where to place all Transformers models:

  ```
  mkdir $DECAF_MODELS_FOLDER/transformers
  ```

### Download all Transformers models

For each model, repeat these steps:

  * From the [HuggingFace](https://huggingface.co/) website main page, search for the desired model's page using the
    text box placed in the top-left corner.

  * Create a new folder inside the `models/transformers` subdirectory with the same name as the desired one.
  
  * Download all files from the `Files and versions` tab of the model card page on HuggingFace and place them inside
    the folder created in the previous step. Note that, in case there are multiple files with the `LFS` symbol
    and the same size, it is generally possible to only pick `pytorch_model.bin` and skip all the other(s).

  * In case these steps are required to be performed in a command line-only environment, please use the following
    commands:
    ```
    cd "$DECAF_MODELS_FOLDER/transformers"
    MODEL_NAME=name-of-desired-model
    git lfs clone https://huggingface.co/$MODEL_NAME
    ```
    Unfortunately, it requires that `git-lfs` is also installed.


- The built-in components of the framework rely on these models:

  * [distilbert-dot-tas_b-b256-msmarco](https://huggingface.co/sebastian-hofstaetter/distilbert-dot-tas_b-b256-msmarco)
  * [efficient-splade-V-large-doc](https://huggingface.co/naver/efficient-splade-V-large-doc)
  * [efficient-splade-V-large-query](https://huggingface.co/naver/efficient-splade-V-large-query)
  * [f-coref](https://huggingface.co/biu-nlp/f-coref)
  * [multi-qa-mpnet-base-dot-v1](https://huggingface.co/sentence-transformers/multi-qa-mpnet-base-dot-v1)
  * [t5-base-canard](https://huggingface.co/castorini/t5-base-canard)


- In case a model is not needed anymore, its folder inside the `models/transformers` directory can be deleted.

### Download all AllenNLP models

This step is required only if the `AllenNLP` rewriter is employed, otherwise it can be safely skipped. In affirmative
cases, make sure to create the `allennlp_spacy_transformers` virtual environment as well.

- For each model, repeat these steps:

  * From the [AllenNLP GitHub repository](https://github.com/allenai/allennlp-models/tree/main/allennlp_models/modelcards),
    search for the desired model card.

  * Read the model card's JSON file, looking for the `model_usage/archive_file` property. It gives the filename of the
    archive that must be downloaded to use the model.

  * Create a new folder inside the `models/allennlp` subdirectory with the same name as the desired model.

  * Download the model archive and decompress it:

  ```
  MODEL_NAME=name-of-desired-model
  MODEL_ARCHIVE=archive-filename-of-desired-model
  
  cd "$DECAF_MODELS_FOLDER/allennlp/$MODEL_NAME"
  wget https://storage.googleapis.com/allennlp-public-models/$MODEL_ARCHIVE
  tar -xf $MODEL_ARCHIVE
  rm $MODEL_ARCHIVE
  ```

- The built-in components of the framework rely on these models:

  * [coref-spanbert-large](https://storage.googleapis.com/allennlp-public-models/coref-spanbert-large-2021.03.10.tar.gz)

## Step 4: Edit the launch scripts

The provided launch scripts must be edited to point to the correct locations on disk. Please follow this procedure:

- Edit both `index.sh` and `search.sh` launch scripts, located inside the `scripts/` subfolder. In line 7, change the
  value of the environmental variable `DECAF_ROOT_FOLDER` with the location of the root folder of the framework. The
  value required is the same as the homonym environmental variable created before in step 1.

# Uninstall

For uninstalling the framework, it is sufficient to delete the root folder of DECAF. This will delete all source code,
documentation, data and models, using the following commands:
```
cd /path/to/desired/location
rm -r DECAF
```
