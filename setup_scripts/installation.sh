#!/bin/bash

# Prerequisite: Launch this script from the root folder of the framework.


# ------------------------------ LOCATIONS ------------------------------
# ---------- Make sure to NOT put a forward slash (/) at the end ----------

# The path to the root folder of DECAF.
DECAF_ROOT_FOLDER="$(pwd)"

# The path to the folder where the Python virtual environments are installed.
DECAF_VENV_FOLDER="$DECAF_ROOT_FOLDER/template/venv"

# The path to the folder where the machine-learing models' data are stored.
DECAF_MODELS_FOLDER="$DECAF_ROOT_FOLDER/template/models"

# ------------------------------ STEP 1 ------------------------------

# Build the main executable JAR file using Maven.
mvn package

# Generate the JavaDoc documentation of the source code.
JAR_FILENAME=$(ls target/ | grep ".*-jar.*.jar")
javadoc -protected -d javadoc -sourcepath src/main/java -subpackages it.unipd.dei \
  -classpath target/$JAR_FILENAME

# ------------------------------ STEP 2 ------------------------------

# Make sure that both conda and pip have been updated to a recently-released version.
conda update -n base -c defaults conda
pip install pip --upgrade pip

# Create the faiss_fastcoref_spacy_transformers virtual environment.
conda env create -f faiss_fastcoref_spacy_transformers.yml -p $DECAF_VENV_FOLDER/faiss_fastcoref_spacy_transformers

conda activate $DECAF_VENV_FOLDER/faiss_fastcoref_spacy_transformers
python -m spacy download en_core_web_sm
conda deactivate

# Create the splade virtual environment.

# -- Get the .yml environment configuration file from the official SPLADE repository on GitHub
wget https://raw.githubusercontent.com/naver/splade/main/conda_splade_env.yml

# -- Use it to create the virtual environment.
conda env create -f conda_splade_env.yml -p $DECAF_VENV_FOLDER/splade

# Create the allennlp_spacy_transformers virtual environment.
conda env create -f allennlp_spacy_transformers.yml -p $DECAF_VENV_FOLDER/allennlp_spacy_transformers

conda activate $DECAF_VENV_FOLDER/allennlp_spacy_transformers
python -m spacy download en_core_web_sm
conda deactivate

# ------------------------------ STEP 3 ------------------------------

# Create the sub-directories for AllenNLP and Transformers models.
mkdir $DECAF_MODELS_FOLDER/allennlp
mkdir $DECAF_MODELS_FOLDER/transformers

# Download the AllenNLP models from the AllenNLP public models website.

# -- Change the working directory to the one containing the AllenNLP models.
cd $DECAF_MODELS_FOLDER/allennlp

# -- coref-spanbert-large
mkdir coref-spanbert-large
cd coref-spanbert-large
wget https://storage.googleapis.com/allennlp-public-models/coref-spanbert-large-2021.03.10.tar.gz
tar -xf coref-spanbert-large-2021.03.10.tar.gz
rm coref-spanbert-large-2021.03.10.tar.gz

# -- Come back to the root folder of DECAF.
cd $DECAF_ROOT_FOLDER

# Download the Transformers models from the Hugging Face website.



# -- Change the working directory to the one containing the Transformers models.
cd $DECAF_MODELS_FOLDER/transformers

# -- distilbert-dot-tas_b-b256-msmarco
mkdir distilbert-dot-tas_b-b256-msmarco
cd distilbert-dot-tas_b-b256-msmarco
wget https://huggingface.co/sebastian-hofstaetter/distilbert-dot-tas_b-b256-msmarco/blob/main/config.json
wget https://huggingface.co/sebastian-hofstaetter/distilbert-dot-tas_b-b256-msmarco/blob/main/pytorch_model.bin
wget https://huggingface.co/sebastian-hofstaetter/distilbert-dot-tas_b-b256-msmarco/blob/main/special_tokens_map.json
wget https://huggingface.co/sebastian-hofstaetter/distilbert-dot-tas_b-b256-msmarco/blob/main/tokenizer_config.json
wget https://huggingface.co/sebastian-hofstaetter/distilbert-dot-tas_b-b256-msmarco/blob/main/vocab.txt
cd ..

# -- efficient-splade-V-large-doc
mkdir efficient-splade-V-large-doc
cd efficient-splade-V-large-doc
wget https://huggingface.co/naver/efficient-splade-V-large-doc/blob/main/config.json
wget https://huggingface.co/naver/efficient-splade-V-large-doc/blob/main/pytorch_model.bin
wget https://huggingface.co/naver/efficient-splade-V-large-doc/blob/main/special_tokens_map.json
wget https://huggingface.co/naver/efficient-splade-V-large-doc/blob/main/tokenizer.json
wget https://huggingface.co/naver/efficient-splade-V-large-doc/blob/main/tokenizer_config.json
wget https://huggingface.co/naver/efficient-splade-V-large-doc/blob/main/vocab.txt
cd ..

# -- efficient-splade-V-large-query
mkdir efficient-splade-V-large-query
cd efficient-splade-V-large-query
wget https://huggingface.co/naver/efficient-splade-V-large-query/blob/main/config.json
wget https://huggingface.co/naver/efficient-splade-V-large-query/blob/main/pytorch_model.bin
wget https://huggingface.co/naver/efficient-splade-V-large-query/blob/main/special_tokens_map.json
wget https://huggingface.co/naver/efficient-splade-V-large-query/blob/main/tokenizer.json
wget https://huggingface.co/naver/efficient-splade-V-large-query/blob/main/tokenizer_config.json
wget https://huggingface.co/naver/efficient-splade-V-large-query/blob/main/vocab.txt
cd ..

# -- f-coref
mkdir f-coref
cd f-coref
wget https://huggingface.co/biu-nlp/f-coref/blob/main/config.json
wget https://huggingface.co/biu-nlp/f-coref/blob/main/merges.txt
wget https://huggingface.co/biu-nlp/f-coref/blob/main/pytorch_model.bin
wget https://huggingface.co/biu-nlp/f-coref/blob/main/special_tokens_map.json
wget https://huggingface.co/biu-nlp/f-coref/blob/main/tokenizer.json
wget https://huggingface.co/biu-nlp/f-coref/blob/main/tokenizer_config.json
wget https://huggingface.co/biu-nlp/f-coref/blob/main/vocab.json
cd ..

# -- multi-qa-mpnet-base-dot-v1
mkdir multi-qa-mpnet-base-dot-v1
cd multi-qa-mpnet-base-dot-v1
wget https://huggingface.co/sentence-transformers/multi-qa-mpnet-base-dot-v1/blob/main/config.json
wget https://huggingface.co/sentence-transformers/multi-qa-mpnet-base-dot-v1/blob/main/config_sentence_transformers.json
wget https://huggingface.co/sentence-transformers/multi-qa-mpnet-base-dot-v1/blob/main/data_config.json
wget https://huggingface.co/sentence-transformers/multi-qa-mpnet-base-dot-v1/blob/main/modules.json
wget https://huggingface.co/sentence-transformers/multi-qa-mpnet-base-dot-v1/blob/main/pytorch_model.bin
wget https://huggingface.co/sentence-transformers/multi-qa-mpnet-base-dot-v1/blob/main/sentence_bert_config.json
wget https://huggingface.co/sentence-transformers/multi-qa-mpnet-base-dot-v1/blob/main/special_tokens_map.json
wget https://huggingface.co/sentence-transformers/multi-qa-mpnet-base-dot-v1/blob/main/tokenizer.json
wget https://huggingface.co/sentence-transformers/multi-qa-mpnet-base-dot-v1/blob/main/tokenizer_config.json
wget https://huggingface.co/sentence-transformers/multi-qa-mpnet-base-dot-v1/blob/main/vocab.txt
cd ..

# -- t5-base-canard
mkdir t5-base-canard
cd t5-base-canard
wget https://huggingface.co/castorini/t5-base-canard/blob/main/config.json
wget https://huggingface.co/castorini/t5-base-canard/blob/main/pytorch_model.bin
wget https://huggingface.co/castorini/t5-base-canard/blob/main/special_tokens_map.json
wget https://huggingface.co/castorini/t5-base-canard/blob/main/spiece.model
wget https://huggingface.co/castorini/t5-base-canard/blob/main/tokenizer_config.json
cd ..

# -- Come back to the root folder of DECAF.
cd $DECAF_ROOT_FOLDER

# ------------------------------ DONE ------------------------------
clear

echo "Installation Done."
echo ""
echo "Please modify both 'template/scripts/index.sh' and 'template/scripts/search.sh':"
echo "- Line 4: change the value of 'DECAF_ROOT_FOLDER' with the absolute path"
echo "          to the root folder of DECAF."
echo ""
echo "          DECAF_ROOT_FOLDER=\"$DECAF_ROOT_FOLDER\""
echo ""
echo "To uninstall, just delete the root folder of DECAF."
echo ""
echo ""

read -p "Press Enter key to exit..."

