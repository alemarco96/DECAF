#!/bin/bash

# Prerequisite: Launch this script from the root folder of DECAF.

# Prerequisite: Installation procedure already executed and completed successfully.


# ------------------------------ LOCATIONS ------------------------------
# ---------- Make sure to NOT put a forward slash (/) at the end ----------

# The path to the root folder of DECAF.
DECAF_ROOT_FOLDER="$(pwd)"

# The path to the folder where the Python virtual environments are installed.
DECAF_VENV_FOLDER="$DECAF_ROOT_FOLDER/template/venv"

# The path to the folder where the machine-learing models' data are stored.
DECAF_MODELS_FOLDER="$DECAF_ROOT_FOLDER/template/models"

# ------------------------------ TREC-eval ------------------------------

# Clone the 'trec-eval' repository and compile the source code using Make.
mkdir trec_eval_tool
cd trec_eval_tool
git clone https://github.com/usnistgov/trec_eval.git
make -C trec_eval/

# Copy the 'trec-eval' executable on the main folder, then delete the source code.
cp trec_eval/trec_eval ../
cd ..
rm -rf trec_eval_tool/

# ------------------------------ STEP 1 ------------------------------

# Download all necessary 'corpora' data to replicate our experiments.

# -- Change the working directory to the one containing the 'corpora' data.
cd $DECAF_ROOT_FOLDER/template/corpora

# -- Download the MS-MARCO passages dataset from the official website.
mkdir MS-MARCO-passage
cd MS-MARCO-passage/
wget https://msmarco.blob.core.windows.net/msmarcoranking/collection.tar.gz
tar -xf collection.tar.gz
rm collection.tar.gz
cd ..

# -- Download the list of duplicates for MS-MARCO passages dataset, provided by TREC CAsT organizers.
cd MS-MARCO-passage/
wget https://boston.lti.cs.cmu.edu/Services/treccast19/duplicate_list_v1.0.txt
cd ..

# -- Download the TREC-CAR dataset from the official website.
mkdir TREC-CAR
cd TREC-CAR/
wget https://trec-car.cs.unh.edu/datareleases/v2.0/paragraphCorpus.v2.0.tar.xz
tar -xf paragraphCorpus.v2.0.tar.xz
mv paragraphCorpus/dedup.articles-paragraphs.cbor dedup.articles-paragraphs.cbor
rm -r paragraphCorpus/
rm paragraphCorpus.v2.0.tar.xz
cd ..

# -- Come back to the root folder of DECAF.
cd $DECAF_ROOT_FOLDER


# Download all necessary 'topics' data to replicate our experiments.

# -- Change the working directory to the one containing the 'topics' data.
cd $DECAF_ROOT_FOLDER/template/topics

# -- Download the topics for TREC CAsT 2019.
wget https://raw.githubusercontent.com/daltonj/treccastweb/master/2019/data/evaluation/evaluation_topics_v1.0.json
mv evaluation_topics_v1.0.json cast2019_automatic_evaluation.json
wget https://raw.githubusercontent.com/daltonj/treccastweb/master/2019/data/evaluation/evaluation_topics_annotated_resolved_v1.0.tsv
mv evaluation_topics_annotated_resolved_v1.0.tsv cast2019_manual_evaluation.txt

# -- Download the topics for TREC CAsT 2020.
wget https://raw.githubusercontent.com/daltonj/treccastweb/master/2020/2020_automatic_evaluation_topics_v1.0.json
mv 2020_automatic_evaluation_topics_v1.0.json cast2020_automatic_evaluation.json
wget https://github.com/daltonj/treccastweb/blob/master/2020/2020_manual_evaluation_topics_v1.0.json
mv 2020_manual_evaluation_topics_v1.0.json cast2020_manual_evaluation.json

# -- Come back to the root folder of DECAF.
cd $DECAF_ROOT_FOLDER


# Download all necessary 'qrels' data to replicate our experiments.

# -- Change the working directory to the one containing the 'qrels' data.
mkdir $DECAF_ROOT_FOLDER/template/qrels
cd $DECAF_ROOT_FOLDER/template/qrels

# -- Download the qrels for TREC CAsT 2019.

wget https://raw.githubusercontent.com/daltonj/treccastweb/master/2019/data/2019qrels.txt
mv 2019qrels.txt cast2019_evaluation.qrels

# -- Download the qrels for TREC CAsT 2020.

wget https://raw.githubusercontent.com/daltonj/treccastweb/master/2020/2020qrels.txt
mv 2020qrels.txt cast2020_evaluation.qrels

# -- Come back to the root folder of DECAF.
cd $DECAF_ROOT_FOLDER

# ------------------------------ DONE ------------------------------
clear

echo "Reproducibility setup Done."
echo ""
echo "All data necessary to reproduce our experiments shown in the paper has"
echo "been successfully downloaded and stored in the expected locations."
echo "Please, now follow REPRODUCIBILITY_GUIDE.md starting from 'Step 2'."
echo ""
echo ""


read -p "Press Enter key to exit..."
