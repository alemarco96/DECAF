import argparse
import faiss
import numpy
import os
import sys
import time
import torch
import traceback
import transformers


# The name of the environmental variable which stores where all Transformers models are stored on disk.
ENV_TRANSFORMERS_FOLDER = "TRANSFORMERS_CACHE"

# The name of the environmental variable which stores where the filename of the error log file.
ENV_ERROR_LOG_FILENAME = "DECAF_PYTHON_ERROR_LOG_FILENAME"

model_max_tokens = None
similarity = None


def parse_args():
    SCRIPT_DESCRIPTION = "Python script for \"Faiss indexer\" component of the Framework."
    HELP_INDEX_FILENAME = "The regex for the filename of the shards of the Faiss index. The general structure " \
        "of this field should be something similar to <prefix>_%d.<extension>. Note that it is required " \
        "to have '%d' inside, so it can be replaced with the shard number."
    HELP_MODEL = "The name of the Transformers model to use. First, all files must be downloaded " \
        "from the model page on \"huggingface.co\" site and placed inside a folder with the same " \
        "name as this parameter. This folder must be located inside the " + ENV_TRANSFORMERS_FOLDER + \
        " folder. Note that " + ENV_TRANSFORMERS_FOLDER + " is an environmental variable set in the launch script."
    HELP_VECTOR_SIZE = "The size of the vectors produced by the model. The most common value is 768."
    HELP_MAX_TOKENS = "The maximum number of tokens produced by the tokenizer before truncation " \
        "is applied. The most common value is 512."
    HELP_SIMILARITY = "The similarity function used to compare the vectors. Available options are: " \
        "\"cos\" for cosine similarity, \"dot\" for dot product, \"l2\" for euclidean distance and " \
        "\"l2sq\" for squared euclidean distance. Usually \"dot\" gives the best results."
    HELP_BATCH_SIZE = "The number of documents to be processed together in a single batch. " \
        "Good values should be around 16 / 32, depending on the GPU available."
    HELP_CHUNKS_SIZE = "The (minimal) number of documents to be put inside a shard of the whole Faiss index. " \
        "A reasonable value is 1 million."

    parser = argparse.ArgumentParser(description = SCRIPT_DESCRIPTION)
    parser.add_argument("--index_filename", dest = "index_filename", required = True, help = HELP_INDEX_FILENAME)
    parser.add_argument("--model", dest = "model", required = True, help = HELP_MODEL)
    parser.add_argument("--vector_size", dest = "vector_size", type = int, required = True, help = HELP_VECTOR_SIZE)
    parser.add_argument("--max_tokens", dest = "max_tokens", type = int, required = True, help = HELP_MAX_TOKENS)
    parser.add_argument("--similarity", dest = "similarity", choices = ["cos", "dot", "l2", "l2sq"], required = True,
                        help = HELP_SIMILARITY)
    parser.add_argument("--batch_size", dest = "batch_size", type = int, required = True, help = HELP_BATCH_SIZE)
    parser.add_argument("--chunks_size", dest = "chunks_size", type = int, required = True, help = HELP_CHUNKS_SIZE)

    return parser.parse_args()


def process_batch(batch, gpu_device, tokenizer, model, faiss_index):
    if len(batch) == 0:
        return (0.0, 0.0)

    ext_time = -time.perf_counter()

    # Tokenize the documents text.
    if gpu_device is not None:
        tokens = tokenizer(batch, padding = True, truncation = True, max_length = model_max_tokens,
                           return_tensors = "pt").to(gpu_device)
    else:
        tokens = tokenizer(batch, padding = True, truncation = True, max_length = model_max_tokens,
                           return_tensors = "pt")

    # Compute the documents vectors.
    if gpu_device is not None:
        vectors = model(**tokens)[0][:, 0, :].contiguous().cpu().detach().numpy()
    else:
        vectors = model(**tokens)[0][:, 0, :].contiguous().detach().numpy()

    ext_time += time.perf_counter()
    index_time = -time.perf_counter()

    # Add the documents vectors to the Faiss index.
    if similarity == "cos":
        vectors_norm = numpy.linalg.norm(vectors, axis = 1)
        faiss_index.add(vectors / vectors_norm)
    else:
        faiss_index.add(vectors)

    index_time += time.perf_counter()

    # Delete the tokens and vectors from memory.
    del tokens, vectors

    return (ext_time, index_time)


def main():
    global model_max_tokens, similarity

    # ********** Start of Init phase **********

    # Parse the arguments of this script.
    script_args = parse_args()
    index_filename = script_args.index_filename
    model_name = os.environ[ENV_TRANSFORMERS_FOLDER] + "/" + script_args.model + "/"
    model_vector_size = script_args.vector_size
    model_max_tokens = script_args.max_tokens
    similarity = script_args.similarity
    batch_size = script_args.batch_size
    chunks_size = script_args.chunks_size

    if similarity not in ["cos", "dot", "l2", "l2sq"]:
        raise Exception("The provided similarity function \"%s\" is unknown." % similarity)
    if batch_size < 1:
        raise Exception("The provided batch size (%d) is not a positive integer number." % batch_size)
    if chunks_size < 1:
        raise Exception("The provided chunks size (%d) is not a positive integer number." % chunks_size)


    # Check if at least 1 GPU has been assigned to this process.
    if torch.cuda.is_available():
        gpu_device = torch.device("cuda:%d" % torch.cuda.current_device())
    else:
        gpu_device = None

    # Load the tokenizer.
    tokenizer = transformers.AutoTokenizer.from_pretrained(model_name, local_files_only = True)

    # Load the model.
    if torch.cuda.is_available():
        model = transformers.AutoModel.from_pretrained(model_name, local_files_only = True).to(gpu_device)
    else:
        model = transformers.AutoModel.from_pretrained(model_name, local_files_only = True)

    # Create the Faiss index.
    if (similarity == "cos") or (similarity == "dot"):
        faiss_index = faiss.IndexFlatIP(model_vector_size)
    elif (similarity == "l2") or (similarity == "l2sq"):
        faiss_index = faiss.IndexFlatL2(model_vector_size)

    # Write an empty line to the error stream, to sync with Java code.
    # From Java, when reading from error stream two cases are possible:
    #   - Single empty line: all OK in this Python script.
    #   - Otherwise: an exception has been thrown in this Python script.
    print("", flush = True, file = sys.stderr)

    #******************** End of Initialization phase ********************

    #******************** Start of Indexing phase ********************

    # Store the number of indexes processed.
    index_counter = 0

    # Store the number of documents stored in the current index.
    doc_index_counter = 0

    # Store the batch data.
    text_batch = []

    # Store timing information.
    ext_time = 0.0
    index_time = 0.0

    while True:
        # Read the next document from the input stream.
        try:
            # Implementation note: it is needed to close the input stream of the process from Java,
            # in order to let this process end and not hang waiting for additional input.

            doc_text = input()

            # Append the just-read string to the batch, but only if is not an empty line.
            if doc_text != "":
                text_batch.append(doc_text)

            # Process the batch of documents if either it reached its maximum size, or an empty line
            # has been written to the input stream by Java code.
            if (doc_text == "") or (len(text_batch) >= batch_size):
                # Invoke process_batch() only it the batch has some element(s).
                if len(text_batch) > 0:
                    ext_time, index_time = process_batch(text_batch, gpu_device, tokenizer, model, faiss_index)
                else:
                    ext_time, index_time = (0.0, 0.0)

                # Add the number of documents of the current batch to the number of documents the current index.
                doc_index_counter += len(text_batch)

                # Reset the batch to the empty state.
                text_batch = []

                # Check if the current index has reached its maximum size.
                if doc_index_counter >= chunks_size:
                    index_time -= time.perf_counter()

                    # Write the current index to storage.
                    index_counter += 1
                    faiss.write_index(faiss_index, index_filename % index_counter)

                    # Reset the index to the empty state.
                    del faiss_index
                    if (similarity == "cos") or (similarity == "dot"):
                        faiss_index = faiss.IndexFlatIP(model_vector_size)
                    elif (similarity == "l2") or (similarity == "l2sq"):
                        faiss_index = faiss.IndexFlatL2(model_vector_size)

                    # Reset the counter of documents contained in the current index.
                    doc_index_counter = 0

                    index_time += time.perf_counter()


                # Write an empty line to the error stream, to sync with Java code.
                # From Java, when reading from error stream two cases are possible:
                #   - Single empty line: all OK in this Python script.
                #   - Otherwise: an exception has been thrown in this Python script.
                print("", flush = True, file = sys.stderr)

                print(ext_time, flush = True, file = sys.stdout)
                print(index_time, flush = True, file = sys.stdout)

        except EOFError:
            break

    # Check if there are documents left unprocessed inside the batch.
    if len(text_batch) > 0:
        # Process the partially-filled last batch.
        ext_time, index_time = process_batch(text_batch, gpu_device, tokenizer, model, faiss_index)

        doc_index_counter += len(text_batch)
    else:
        ext_time, index_time = (0.0, 0.0)

    # Check if there are documents inside the current index.
    if doc_index_counter > 0:
        index_time -= time.perf_counter()

        # Write the partially-filled current index to storage.
        index_counter += 1
        faiss.write_index(faiss_index, index_filename % index_counter)

        index_time += time.perf_counter()


    # Write an empty line to the error stream, to sync with Java code.
    # From Java, when reading from error stream two cases are possible:
    #   - Single empty line: all OK in this Python script.
    #   - Otherwise: an exception has been thrown in this Python script.
    print("", flush = True, file = sys.stderr)

    print(ext_time, flush = True, file = sys.stdout)
    print(index_time, flush = True, file = sys.stdout)

    #******************** End of Indexing phase ********************

    #******************** Start of Closing phase ********************

    # Delete the resources used.
    del tokenizer, model, faiss_index

    #******************** End of Closing phase ********************


if __name__ == "__main__":
    try:
        main()
    except SystemExit:
        # Do not print anything for all exceptions raised by "argparse" module. They are already self-explicative.
        sys.stderr.flush()
    except:
        # Log the full stack trace of the exception to a file stored on disk.
        with open(os.environ[ENV_ERROR_LOG_FILENAME], "wt", encoding = "utf8") as f:
            print(traceback.format_exc(), flush = True, file = f)

        # Print the captured exception and flush the error stream, so from Java code
        # it is possible to retrieve the entire content of the exception.
        print(traceback.format_exc(), flush = True, file = sys.stderr)
