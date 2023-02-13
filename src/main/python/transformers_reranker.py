import argparse
import functools
import numpy
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
    SCRIPT_DESCRIPTION = "Python script for \"Transformers reranker\" component of the Framework."
    HELP_MODEL = "The name of the Transformers model to use. First, all files must be downloaded " + \
        "from the model page on \"huggingface.co\" site and placed inside a folder with the same " + \
        "name as this parameter. This folder must be located inside the " + ENV_TRANSFORMERS_FOLDER + \
        " folder. Note that " + ENV_TRANSFORMERS_FOLDER + " is an environmental variable set in the launch script."
    HELP_VECTOR_SIZE = "The size of the vectors produced by the model. The most common value is 768."
    HELP_MAX_TOKENS = "The maximum number of tokens produced by the tokenizer before truncation " + \
        "is applied. The most common value is 512."
    HELP_SIMILARITY = "The similarity function used to compare the vectors. Available options are: " + \
        "\"cos\" for cosine similarity, \"dot\" for dot product, \"l2\" for euclidean distance and " + \
        "\"l2sq\" for squared euclidean distance. Usually \"dot\" gives the best results."

    parser = argparse.ArgumentParser(description = SCRIPT_DESCRIPTION)
    parser.add_argument("--model", dest = "model", required = True, help = HELP_MODEL)
    parser.add_argument("--vector_size", dest = "vector_size", type = int, required = True, help = HELP_VECTOR_SIZE)
    parser.add_argument("--max_tokens", dest = "max_tokens", type = int, required = True, help = HELP_MAX_TOKENS)
    parser.add_argument("--similarity", dest = "similarity", choices = ["cos", "dot", "l2", "l2sq"], required = True,
                        help = HELP_SIMILARITY)

    return parser.parse_args()


@functools.lru_cache(maxsize = 4096)
def compute_vector(text):
    if gpu_device is not None:
        text_tokens = tokenizer(text, truncation = True, max_length = model_max_tokens, return_tensors = "pt").to(gpu_device)
        text_vector = model(**text_tokens)[0][0, 0, :].contiguous().cpu().detach().numpy()
    else:
        text_tokens = tokenizer(text, truncation = True, max_length = model_max_tokens, return_tensors = "pt")
        text_vector = model(**text_tokens)[0][0, 0, :].contiguous().detach().numpy()

    del text_tokens
    return text_vector


def main():
    global gpu_device, tokenizer, model, model_max_tokens

    # ********** Start of Init phase **********

    # Parse the arguments of this script.
    script_args = parse_args()
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

    # Write an empty line to the error stream, to sync with Java code.
    # From Java, when reading from error stream two cases are possible:
    #   - Single empty line: all OK in this Python script.
    #   - Otherwise: an exception has been thrown in this Python script.
    print("", flush = True, file = sys.stderr)

    # ********** End of Init phase **********

    # ********** Start of Run phase **********

    while True:
        queries_vector = []
        queries_weight = []

        try:
            # Implementation note: it is needed to close the input stream of the process from Java,
            # in order to let this process end and not hang waiting for additional input.

            # Read the number of queries used to rerank from input stream.
            num_queries = int(input())
            for i in range(num_queries):
                # Read the text of the i-th query from input stream, compute its vector and store it.
                queries_vector.append(compute_vector(input()))
                # Read the weight used for the i-th query from input stream, and store it.
                queries_weight.append(float(input()))

            # Read the number of documents to rerank. If the provided number is negative or zero, exit.
            num_documents = int(input())
            if num_documents < 1:
                break
        except:
            break

        # Loop for each document to rerank.
        for _ in range(num_documents):
            # Read the next document from input stream and compute its vector.
            doc_vector = compute_vector(input())

            # Compute the similarity score between the documents and the queries.
            score = 0.0
            if similarity == "cos":
                doc_inv_norm = 1.0 / numpy.linalg.norm(doc_vector).item()

                for i in range(num_queries):
                    query_inv_norm = 1.0 / numpy.linalg.norm(queries_vector[i]).item()

                    score += queries_weight[i] * (numpy.dot(doc_vector, queries_vector[i]) *
                                                  (doc_inv_norm * query_inv_norm))
            elif similarity == "dot":
                for i in range(num_queries):
                    score += queries_weight[i] * numpy.dot(doc_vector, queries_vector[i])
            elif similarity == "l2":
                for i in range(num_queries):
                    score -= queries_weight[i] * (numpy.sqrt(numpy.sum(numpy.square(doc_vector - queries_vector[i]))))
            elif similarity == "l2sq":
                for i in range(num_queries):
                    score -= queries_weight[i] * (numpy.sum(numpy.square(doc_vector - queries_vector[i])))
            else:
                pass

            # Write the new score to the output stream.
            print(score, file = sys.stdout)

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
