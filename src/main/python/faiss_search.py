import argparse
import bisect
import functools
import faiss
import numpy
import operator
import os
import sys
import torch
import traceback
import transformers


# The name of the environmental variable which stores where all Transformers models are stored on disk.
ENV_TRANSFORMERS_FOLDER = "TRANSFORMERS_CACHE"

# The name of the environmental variable which stores where the filename of the error log file.
ENV_ERROR_LOG_FILENAME = "DECAF_PYTHON_ERROR_LOG_FILENAME"

gpu_device = None
tokenizer = None
model = None
model_max_tokens = None


def parse_args():
    SCRIPT_DESCRIPTION = "Python script for \"Faiss searcher\" component of the Framework."
    HELP_INDEX_FILENAME = "The regex for the filename of the shards of the Faiss index. The general structure " + \
        "of this field should be something similar to <prefix>_%d.<extension>. Note that it is required " + \
        "to have '%d' inside, so it can be replaced with the shard number."
    HELP_DOCS_FILENAME = "The filename of the text file containing \"<id>\t<text>\n\" lines, " \
        "one for each documents indexed."
    HELP_REFS_FILENAME = "The filename of the text file containing, for the i-th line, the index of the first byte " \
        "that encodes the data line inside the \"docs\" file for the i-th document indexed. It enables faster " \
        "retrieval of the ID and text for all indexed documents."
    HELP_MODEL = "The name of the Transformers model to use. First, all files must be downloaded " + \
        "from the model page on \"huggingface.co\" site and placed inside a folder with the same " + \
        "name as this parameter. This folder must be located inside the " + ENV_TRANSFORMERS_FOLDER + \
        " folder. Note that " + ENV_TRANSFORMERS_FOLDER + " is an environmental variable set in the launch script."
    HELP_VECTOR_SIZE = "The size of the vectors produced by the model. The most common value is 768."
    HELP_MAX_TOKENS = "The maximum number of tokens produced by the tokenizer before truncation " + \
        "is applied. The most common value is 512."
    HELP_SIMILARITY = "The similarity function used to compare the vectors. Available options are: " \
        "\"cos\" for cosine similarity, \"dot\" for dot product, \"l2\" for euclidean distance and " \
        "\"l2sq\" for squared euclidean distance. Usually \"dot\" gives the best results."

    parser = argparse.ArgumentParser(description = SCRIPT_DESCRIPTION)
    parser.add_argument("--index_filename", dest = "index_filename", required = True, help = HELP_INDEX_FILENAME)
    parser.add_argument("--docs_filename", dest = "docs_filename", required = True, help = HELP_DOCS_FILENAME)
    parser.add_argument("--refs_filename", dest = "refs_filename", required = True, help = HELP_REFS_FILENAME)
    parser.add_argument("--model", dest = "model", required = True, help = HELP_MODEL)
    parser.add_argument("--vector_size", dest = "vector_size", type = int, required = True, help = HELP_VECTOR_SIZE)
    parser.add_argument("--max_tokens", dest = "max_tokens", type = int, required = True, help = HELP_MAX_TOKENS)
    parser.add_argument("--similarity", dest = "similarity", choices = ["cos", "dot", "l2", "l2sq"], required = True,
                        help = HELP_SIMILARITY)

    return parser.parse_args()


@functools.lru_cache(maxsize=4096)
def compute_vector(text):
    if torch.cuda.is_available():
        text_tokens = tokenizer(text, truncation = True, max_length = model_max_tokens, return_tensors = "pt").to(gpu_device)
        text_vector = model(**text_tokens)[0][0, 0, :].contiguous().cpu().detach().numpy()
    else:
        text_tokens = tokenizer(text, truncation = True, max_length = model_max_tokens, return_tensors = "pt")
        text_vector = model(**text_tokens)[0][0, 0, :].contiguous().detach().numpy()

    del text_tokens
    return text_vector


def compute_similarity(x, y, similarity):
    if (similarity == "cos") or (similarity == "dot"):
        return float(numpy.dot(x, y).item())
    elif similarity == "l2":
        return float(numpy.sqrt(numpy.sum(numpy.square(x - y))).item())
    elif similarity == "l2sq":
        return float(numpy.sum(numpy.square(x - y)).item())
    else:
        pass


def retrieve_vector_from_index(x, shards, starts):
    i = bisect.bisect(starts, x) - 1
    j = x - starts[i]

    return shards.at(i).reconstruct(j)


def main():
    global gpu_device, tokenizer, model, model_max_tokens
    # ********** Start of Init phase **********

    # Parse the arguments of this script.
    script_args = parse_args()
    index_filename = script_args.index_filename
    docs_filename = script_args.docs_filename
    refs_filename = script_args.refs_filename
    model_name = os.environ[ENV_TRANSFORMERS_FOLDER] + "/" + script_args.model + "/"
    model_vector_size = script_args.vector_size
    model_max_tokens = script_args.max_tokens
    similarity = script_args.similarity

    if similarity not in ["cos", "dot", "l2", "l2sq"]:
        raise Exception("The provided similarity function \"%s\" is unknown." % similarity)

    # Check if at least 1 GPU has been assigned to this process.
    if torch.cuda.is_available():
        gpu_device = torch.device("cuda:%d" % torch.cuda.current_device())
    else:
        gpu_device = None

    # Load the tokenizer used by the model from disk.
    tokenizer = transformers.AutoTokenizer.from_pretrained(model_name, local_files_only = True)

    # Load the model from disk and store it on GPU if available, otherwise on CPU.
    if gpu_device is not None:
        model = transformers.AutoModel.from_pretrained(model_name, local_files_only = True).to(gpu_device)
    else:
        model = transformers.AutoModel.from_pretrained(model_name, local_files_only = True)

    # Load the references for each indexed document. It enables faster retrieval of their ID and text.
    with open(refs_filename, "rt", encoding = "utf8") as f:
        docs_ref = [line.strip(' \t\r\n') for line in f.readlines()]
        docs_ref = [int(x) for x in docs_ref]

    # Load the whole index from disk.
    shards = faiss.IndexShards(model_vector_size, True, True)
    shards_start = []
    curr_start = 0

    i = 1
    while True:
        try:
            curr_index = faiss.read_index(index_filename % i)
            shards.add_shard(curr_index)

            shards_start.append(curr_start)
            curr_start += int(curr_index.ntotal)

            i += 1
        except:
            break

    # Write an empty line to the error stream, to sync with Java code.
    # From Java, when reading from error stream two cases are possible:
    #   - Single empty line: all OK in this Python script.
    #   - Otherwise: an exception has been thrown in this Python script.
    print("", flush = True, file = sys.stderr)

    # ********** End of Init phase **********

    # ********** Start of Run phase **********

    while True:
        try:
            # Implementation note: it is needed to close the input stream of the process from Java,
            # in order to let this process end and not hang waiting for additional input.

            # Read the number of queries used to build the search query from input stream.
            num_queries = int(input())
            queries_vector = numpy.zeros((num_queries, model_vector_size), dtype = numpy.float32)
            queries_weight = []

            for i in range(num_queries):
                # Read the text of the i-th query from input stream, compute its vector and store it.
                queries_vector[i] = compute_vector(input())

                # Normalize the query vector, if the similarity function chosen is the cosine similarity.
                if similarity == "cos":
                    queries_vector[i] = queries_vector[i] / numpy.linalg.norm(queries_vector[i]).item()

                # Read the weight used for the i-th query from input stream, and store it.
                queries_weight.append(float(input()))

            # Read the number of documents to search. If the provided number is negative or zero, exit.
            num_documents = int(input())
            if num_documents < 1:
                break
        except EOFError:
            break


        # Perform the search using Faiss.
        distances, indexes = shards.search(queries_vector, num_documents)

        # Compute the set of documents that are retrieved by at least 1 of the queries.
        indexes_retrieved = {int(x.item()) for x in indexes[i] for i in range(num_queries) if x >= 0}

        # Retrieve from the Faiss index the vector for each retrieved document.
        vectors = {x : retrieve_vector_from_index(x, shards, shards_start) for x in indexes_retrieved}

        # Compute the weighted sum of the scores of each document for all queries, then sort the pairs (index, score)
        # by score in descending order and keep at most num_documents pairs.
        queries_result = {x : sum([queries_weight[i] * compute_similarity(vectors[x], queries_vector[i], similarity)])
                          for i in range(num_queries) for x in indexes_retrieved}
        result = sorted(queries_result.items(), key = operator.itemgetter(1), reverse = True)
        result = [result[i] for i in range(min(len(result), num_documents))]


        # Print the results of the search to the standard output.
        with open(docs_filename, "rt", encoding = "utf8") as f:
            # Print the number of documents actually retrieved by Faiss.
            print(len(result), flush = True, file = sys.stdout)

            for doc_index, doc_score in result:
                # Retrieve the ID and text of each document.
                f.seek(docs_ref[doc_index])
                temp = f.readline().strip(' \t\r\n').split('\t')
                doc_id = temp[0]
                doc_text = temp[1]

                # Print the results of the search to the standard output.
                print(doc_id, file = sys.stdout)
                print(doc_score, file = sys.stdout)
                print(doc_index, file = sys.stdout)
                print(doc_text, file = sys.stdout)

                # Flush the output stream.
                sys.stdout.flush()

        # Write an empty line to the error stream, to sync with Java code.
        # From Java, when reading from error stream two cases are possible:
        #   - Single empty line: all OK in this Python script.
        #   - Otherwise: an exception has been thrown in this Python script.
        print("", flush = True, file = sys.stderr)

    # ********** End of Run phase **********


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
