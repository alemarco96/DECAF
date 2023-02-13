import allennlp.predictors.predictor
import argparse
import contextlib
import os
import sys
import torch
import traceback


# The name of the environmental variable which stores where all NLTK data is stored on disk.
ENV_NLTK_FOLDER = "NLTK_DATA"

# The name of the environmental variable which stores where all AllenNLP models are stored on disk.
ENV_ALLENNLP_FOLDER = "ALLENNLP_CACHE_DATA"

# The name of the environmental variable which stores where all Transformers models are stored on disk.
ENV_TRANSFORMERS_FOLDER = "TRANSFORMERS_CACHE"

# The name of the environmental variable which stores where the filename of the error log file.
ENV_ERROR_LOG_FILENAME = "DECAF_PYTHON_ERROR_LOG_FILENAME"


def parse_args():
    SCRIPT_DESCRIPTION = "Python script for \"AllenNLP rewriter\" component of the Framework."
    HELP_MODEL = "The name of the co-reference resolution model to use. First, all files must be downloaded " \
        "from the model page and placed inside a folder with the same name as this parameter. This folder " \
        "must be located inside the " + ENV_ALLENNLP_FOLDER + " folder. Note that it is an environmental " \
        "variable set in the launch script. NOTE: AllenNLP will clutter also the " + ENV_NLTK_FOLDER + " and " + \
        ENV_TRANSFORMERS_FOLDER + " folders with some files. It also requires internet connection to work. " \
        "Since its performance are almost the same as \"FastCoref\", consider using it instead."
    HELP_SEPARATOR = "The string used to split questions. It should be a symbol that does not appear in the " \
        "original text. Default value is \"#\"."

    parser = argparse.ArgumentParser(description = SCRIPT_DESCRIPTION)
    parser.add_argument("--model", dest = "model", required = True, help = HELP_MODEL)
    parser.add_argument("--separator", dest = "separator", default = "#", help = HELP_SEPARATOR)

    return parser.parse_args()


# Suppress any output to the stream, except exception traceback.
@contextlib.contextmanager
def suppress_stream(stream):
    try:
        old_fd = os.dup(stream.fileno())
        fnull = open(os.devnull, 'w')
        os.dup2(fnull.fileno(), stream.fileno())

        yield
    finally:
        if old_fd is not None:
            os.dup2(old_fd, stream.fileno())

        if fnull is not None:
            fnull.close()


def main():
    # ********** Start of Init phase **********

    # Parse the arguments of this script.
    script_args = parse_args()
    model_name = os.environ[ENV_ALLENNLP_FOLDER] + "/" + script_args.model + "/"
    separator = script_args.separator

    # Suppress any output to the error stream, to avoid cluttering it with allennlp logging stuff.
    with suppress_stream(sys.stderr):
        # Load the co-reference resolution predictor.
        if torch.cuda.is_available():
            # Load the predictor on GPU if at least one has been assigned to this process.
            predictor = allennlp.predictors.predictor.Predictor.from_path(model_name,
                    cuda_device = torch.cuda.current_device())
        else:
            predictor = allennlp.predictors.predictor.Predictor.from_path(model_name)

    # Write an empty line to the error stream, to sync with Java code.
    # From Java, when reading from error stream two cases are possible:
    #   - Single empty line: all OK in this Python script.
    #   - Otherwise: an exception has been thrown in this Python script.
    print("", flush = True, file = sys.stderr)

    # ********** End of Init phase **********

    # ********** Start of Run phase **********

    conversation = None
    while True:
        try:
            # Implementation note: it is needed to close the input stream of the process from Java,
            # in order to let this process end and not hang waiting for additional input.
            original = input()

            # Check if an empty line has been supplied to signal the end of the conversation.
            if original == "":
                conversation = []
                continue
        except EOFError:
            break

        # Prepare the text to submit to the co-reference predictor.
        text = (" %s " % separator).join(conversation) + " " + separator + " " + original

        # Rewrite the original text using the predictor loaded before.
        rewritten = predictor.coref_resolved(text)
        rewritten = rewritten[rewritten.rindex(separator) + (len(separator) + 1) :]

        # Append the current utterance to the conversation. The original text is used as
        # it gives better rewriting performance w.r.t rewritten text.
        conversation.append(original)

        # Write the rewritten text to the output stream.
        print(rewritten, flush = True, file = sys.stdout)

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