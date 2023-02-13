import argparse
import os
import splade.models.transformer_rep
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
multiplier = None

#outFile = None


def parse_args():
    SCRIPT_DESCRIPTION = "Python script for \"Splade indexer\" component of the Framework."
    HELP_MODEL = "The name of the SPLADE model to use. First, all files must be downloaded from the model " + \
        "page on \"huggingface.co\" site and placed inside a folder with the same name as this parameter. " + \
        "This folder must be located inside the " + ENV_TRANSFORMERS_FOLDER + " folder. Note that it is " + \
        "an environmental variable set in the launch script."
    HELP_MAX_TOKENS = "The maximum number of tokens produced by the tokenizer before truncation " + \
        "is applied. The most common value is 512."
    HELP_MULTIPLIER = "The number for which to multiply the score given by SPLADE to each token. It should be " \
        "the same value used in the indexing phase. A reasonable value is 100."
    HELP_BATCH_SIZE = "The number of documents to be processed together in a single batch. " \
        "Good values should be around 10 / 32, depending on the GPU available."

    parser = argparse.ArgumentParser(description = SCRIPT_DESCRIPTION)
    parser.add_argument("--model", dest = "model", required = True, help = HELP_MODEL)
    parser.add_argument("--max_tokens", dest = "max_tokens", type = int, required = True, help = HELP_MAX_TOKENS)
    parser.add_argument("--multiplier", dest = "multiplier", type = float, required = True, help = HELP_MULTIPLIER)
    parser.add_argument("--batch_size", dest = "batch_size", type = int, required = True, help = HELP_BATCH_SIZE)

    return parser.parse_args()


def process_batch(text_batch, vocabulary):
    # Tokenize the text of the inner batch.
    if torch.cuda.is_available():
        inner_tokens = tokenizer(text_batch, truncation = True, max_length = model_max_tokens, padding = True,
                               return_tensors = "pt").to(gpu_device)
    else:
        inner_tokens = tokenizer(text_batch, truncation = True, max_length = model_max_tokens, padding = True,
                               return_tensors = "pt")

    # Use SPLADE model on the inner batch.
    with torch.no_grad():
        if torch.cuda.is_available():
            inner_repr = model(d_kwargs = inner_tokens)["d_rep"].cpu()
        else:
            inner_repr = model(d_kwargs = inner_tokens)["d_rep"]

    del inner_tokens


    for i in range(len(text_batch)):
        # Extract the relevant data from the inner batch.
        tokens = torch.nonzero(inner_repr[i], as_tuple = True)[0]
        weights = torch.round(inner_repr[i][tokens] * multiplier)
        to_print = [(vocabulary[tokens[x].item()], int(weights[x].item())) for x in range(weights.shape[0]) if weights[x] > 0]

        # Print the relevant data to the output stream.
        print(len(to_print), file = sys.stdout)
        for j in range(len(to_print)):
            print(to_print[j][0], file = sys.stdout)
            print(to_print[j][1], file = sys.stdout)

    del inner_repr

    # Flush the output stream.
    sys.stdout.flush()

    # Write an empty line to the error stream, to sync with Java code.
    # From Java, when reading from error stream two cases are possible:
    #   - Single empty line: all OK in this Python script.
    #   - Otherwise: an exception has been thrown in this Python script.
    print("", flush = True, file = sys.stderr)


def main():
    global gpu_device, tokenizer, model, model_max_tokens, multiplier

    # ********** Start of Init phase **********

    # Parse the arguments of this script.
    script_args = parse_args()
    model_name = os.environ[ENV_TRANSFORMERS_FOLDER] + "/" + script_args.model + "/"
    model_max_tokens = script_args.max_tokens
    multiplier = script_args.multiplier
    batch_size = script_args.batch_size

    if multiplier < 0.0:
        raise Exception("The provided multiplier (%f) is not a positive number." % multiplier)


    # Check if at least 1 GPU has been assigned to this process.
    if torch.cuda.is_available():
        gpu_device = torch.device("cuda:%d" % torch.cuda.current_device())
    else:
        gpu_device = None

    # Load the tokenizer.
    tokenizer = transformers.AutoTokenizer.from_pretrained(model_name, local_files_only = True)

    # Load the SPLADE model.
    if torch.cuda.is_available():
        model = splade.models.transformer_rep.Splade(model_name, agg = "max").to(gpu_device)
    else:
        model = splade.models.transformer_rep.Splade(model_name, agg = "max")
    model.eval()

    # Retrieve the vocabulary used by SPLADE.
    vocabulary = {v: k for k, v in tokenizer.vocab.items()}

    # Write an empty line to the error stream, to sync with Java code.
    # From Java, when reading from error stream two cases are possible:
    #   - Single empty line: all OK in this Python script.
    #   - Otherwise: an exception has been thrown in this Python script.
    print("", flush = True, file = sys.stderr)

    # ********** End of Init phase **********

    # ********** Start of Run phase **********

    text_batch = []
    while True:
        # Read the next document from the input stream.
        try:
            # Implementation note: it is needed to close the input stream of the process from Java,
            # in order to let this process end and not hang waiting for additional input.

            doc_text = input()

            if doc_text == "":
                process_batch(text_batch, vocabulary)

                text_batch = []

            text_batch.append(doc_text)

            if len(text_batch) >= batch_size:
                process_batch(text_batch, vocabulary)

                text_batch = []
        except EOFError:
            break

    if len(text_batch) > 0:
        process_batch(text_batch, vocabulary)

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
