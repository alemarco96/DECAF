#!/bin/bash

# IMPORTANT NOTE: Make sure to not put a forward slash (`/`) at the end of the path for directories.

# The directory where the repository has been cloned.
# Please set this with the appropriate value.
export DECAF_ROOT_FOLDER="/path/to/desired/location/DECAF"

# The root folder @ host where files are stored.
# When a container is not used, set it equal to the root folder.
export DECAF_HOST_ROOT_FOLDER="$DECAF_ROOT_FOLDER"

# The root folder @ container where files are stored.
# When a container is not used, set it equal to the root folder.
export DECAF_CONTAINER_ROOT_FOLDER="$DECAF_ROOT_FOLDER"

# The folder where the Python virtual environments are stored.
# If a custom location has been employed during install phase, please change this with the appropriate value.
export DECAF_VENV_FOLDER="$DECAF_CONTAINER_ROOT_FOLDER/template/venv"

# The folder where all models are stored.
export DECAF_MODELS_FOLDER="$DECAF_CONTAINER_ROOT_FOLDER/template/models"

# Change where AllenNLP looks for data.
export ALLENNLP_CACHE_DATA="$DECAF_MODELS_FOLDER/allennlp"

# Change where Transformers library looks for data.
export TRANSFORMERS_CACHE="$DECAF_MODELS_FOLDER/transformers"

# Change the filename of the log file where external Python scripts report details about the errors that occurred.
export DECAF_PYTHON_ERROR_LOG_FILENAME="$DECAF_CONTAINER_ROOT_FOLDER/template/python_errors.txt"


# Change the working directory to the root folder.
cd $DECAF_CONTAINER_ROOT_FOLDER

# Find the JAR filename.
DECAF_JAR_FILENAME=$(ls target/ | grep ".*-jar.*.jar")

# Perform the index operation.
java -jar $DECAF_CONTAINER_ROOT_FOLDER/target/"$DECAF_JAR_FILENAME" -i $DECAF_CONTAINER_ROOT_FOLDER/config/index.properties 2>&1
