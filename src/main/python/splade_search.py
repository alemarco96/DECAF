import argparse
import os
import splade.models.transformer_rep
import sys
import torch
import traceback
import transformers
import warnings


# The name of the environmental variable which stores where all Transformers models are stored on disk.
ENV_TRANSFORMERS_FOLDER = "TRANSFORMERS_CACHE"

# The name of the environmental variable which stores where the filename of the error log file.
ENV_ERROR_LOG_FILENAME = "DECAF_PYTHON_ERROR_LOG_FILENAME"


def parse_args():
    SCRIPT_DESCRIPTION = "Python script for \"Splade searcher\" component of the Framework."
    HELP_MODEL = "The name of the SPLADE model to use. First, all files must be downloaded from the model " \
        "page on \"huggingface.co\" site and placed inside a folder with the same name as this parameter. " \
        "This folder must be located inside the " + ENV_TRANSFORMERS_FOLDER + " folder. Note that it is " \
        "an environmental variable set in the launch script."
    HELP_MAX_TOKENS = "The maximum number of tokens produced by the tokenizer before truncation " \
        "is applied. The most common value is 512."

    parser = argparse.ArgumentParser(description = SCRIPT_DESCRIPTION)
    parser.add_argument("--model", dest = "model", required = True, help = HELP_MODEL)
    parser.add_argument("--max_tokens", dest = "max_tokens", type = int, required = True, help = HELP_MAX_TOKENS)

    return parser.parse_args()


def main():
    # ********** Start of Init phase **********

    # Parse the arguments of this script.
    script_args = parse_args()
    model_name = os.environ[ENV_TRANSFORMERS_FOLDER] + "/" + script_args.model + "/"
    model_max_tokens = script_args.max_tokens

    # Check if at least 1 GPU has been assigned to this process.
    if torch.cuda.is_available():
        gpu_device = torch.device("cuda:%d" % torch.cuda.current_device())
    else:
        gpu_device = None


    with warnings.catch_warnings():
        warnings.simplefilter("ignore")

        # Load the tokenizer used by the model from disk.
        tokenizer = transformers.AutoTokenizer.from_pretrained(model_name, local_files_only = True)

        # Load the model from disk and store it on GPU if available, otherwise on CPU.
        if gpu_device is not None:
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

    while True:
        try:
            # Implementation note: it is needed to close the input stream of the process from Java,
            # in order to let this process end and not hang waiting for additional input.

            query_text = input()
        except EOFError:
            break

        with warnings.catch_warnings():
            warnings.simplefilter("ignore")

            # Tokenize the text of the inner batch.
            if gpu_device is not None:
                query_tokens = tokenizer(query_text, truncation = True, max_length = model_max_tokens, padding = True,
                                         return_tensors = "pt").to(gpu_device)
            else:
                query_tokens = tokenizer(query_text, truncation = True, max_length = model_max_tokens, padding = True,
                                         return_tensors = "pt")

            # Use SPLADE model on the query.
            with torch.no_grad():
                if gpu_device is not None:
                    query_repr = model(d_kwargs = query_tokens)["d_rep"][0].cpu()
                else:
                    query_repr = model(d_kwargs = query_tokens)["d_rep"][0]

            del query_tokens

        # Extract the relevant data from the SPLADE data.
        tokens = torch.nonzero(query_repr, as_tuple = True)[0]
        weights = query_repr[tokens]
        to_print = [(vocabulary[tokens[x].item()], float(weights[x].item())) for x in range(weights.shape[0]) if weights[x] > 0]

        del query_repr

        # Print the relevant data to the output stream.
        print(len(to_print), file = sys.stdout)
        for i in range(len(to_print)):
            print(to_print[i][0], file = sys.stdout)
            print(to_print[i][1], file = sys.stdout)

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
