# Disable all logging, used by "fastcoref" module, in order to not clutter the error stream.
import logging
logging.disable(logging.CRITICAL)

# Disable all progress bars, used by "fastcoref" module, in order to not clutter the error stream.
import functools
import tqdm
tqdm.tqdm.__init__ = functools.partialmethod(tqdm.tqdm.__init__, disable = True)

import argparse
import contextlib
import fastcoref
import fastcoref.spacy_component
import os
import spacy
import sys
import torch
import traceback


# The name of the environmental variable which stores where all Transformers models are stored on disk.
ENV_TRANSFORMERS_FOLDER = "TRANSFORMERS_CACHE"

# The name of the environmental variable which stores where the filename of the error log file.
ENV_ERROR_LOG_FILENAME = "DECAF_PYTHON_ERROR_LOG_FILENAME"


def parse_args():
    SCRIPT_DESCRIPTION = "Python script for \"FastCoref rewriter\" component of the Framework."
    HELP_MODEL = "The name of the co-reference resolution model to use. First, all files must be downloaded " + \
        "from the model page and placed inside a folder with the same name as this parameter. This folder " + \
        "must be located inside the " + ENV_TRANSFORMERS_FOLDER + " folder. Note that it is an environmental " + \
        "variable set in the launch script."
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
    model_name = os.environ[ENV_TRANSFORMERS_FOLDER] + "/" + script_args.model + "/"
    separator = script_args.separator

    # Load Spacy model, required by fastcoref. Use GPU if available.
    if torch.cuda.is_available():
        spacy.prefer_gpu()
        #spacy.require_gpu()

    nlp = spacy.load("en_core_web_sm")

    # Load the co-reference resolution predictor on GPU, if at least one has been assigned to this process.
    if torch.cuda.is_available():
        coref = fastcoref.spacy_component.FastCorefResolver(nlp, "", "FCoref", model_name, "cuda", 10000)
    else:
        coref = fastcoref.spacy_component.FastCorefResolver(nlp, "", "FCoref", model_name, "cpu", 10000)

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
        with suppress_stream(sys.__stderr__):
            doc = nlp(text)
            rewritten = coref(doc, resolve_text = True)._.resolved_text
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
        # NOTE: at some point, a '\n' is always prefixed to this print() call, for unknown reasons.
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
