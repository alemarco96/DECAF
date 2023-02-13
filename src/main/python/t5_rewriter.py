import argparse
import os
import sys
import torch
import traceback
import transformers


# The name of the environmental variable which stores where all Transformers models are stored on disk.
ENV_TRANSFORMERS_FOLDER = "TRANSFORMERS_CACHE"

# The name of the environmental variable which stores where the filename of the error log file.
ENV_ERROR_LOG_FILENAME = "DECAF_PYTHON_ERROR_LOG_FILENAME"


def parse_args():
    SCRIPT_DESCRIPTION = "Python script for \"FastCoref rewriter\" component of the Framework."
    HELP_MODEL = "The name of the Transformers model to use. First, all files must be downloaded " \
        "from the model page on \"huggingface.co\" site and placed inside a folder with the same " \
        "name as this parameter. This folder must be located inside the " + ENV_TRANSFORMERS_FOLDER + \
        " folder. Note that " + ENV_TRANSFORMERS_FOLDER + " is an environmental variable set in the launch script."
    HELP_MAX_TOKENS = "The maximum number of tokens produced by the tokenizer before truncation " \
        "is applied. The most common value is 512."
    HELP_SEPARATOR = "The string used to split questions. It should be a symbol that does not appear in the " \
        "original text. Default value is \"|||\"."

    parser = argparse.ArgumentParser(description = SCRIPT_DESCRIPTION)
    parser.add_argument("--model", dest = "model", required = True, help = HELP_MODEL)
    parser.add_argument("--max_tokens", dest = "max_tokens", type = int, required = True, help = HELP_MAX_TOKENS)
    parser.add_argument("--separator", dest = "separator", default = "|||", help = HELP_SEPARATOR)

    return parser.parse_args()


def main():
    # ********** Start of Init phase **********

    # Parse the arguments of this script.
    script_args = parse_args()
    model_name = os.environ[ENV_TRANSFORMERS_FOLDER] + "/" + script_args.model + "/"
    model_max_tokens = script_args.max_tokens
    separator = script_args.separator

    # Check if at least 1 GPU has been assigned to this process.
    if torch.cuda.is_available():
        gpu_device = torch.device("cuda:%d" % torch.cuda.current_device())
    else:
        gpu_device = None

    # Load the tokenizer.
    tokenizer = transformers.T5Tokenizer.from_pretrained(model_name, local_files_only = True)

    # Load the T5 model from disk and store it on GPU if available, otherwise on CPU.
    if gpu_device is not None:
        model = transformers.T5ForConditionalGeneration.from_pretrained(model_name, local_files_only = True).to(gpu_device)
    else:
        model = transformers.T5ForConditionalGeneration.from_pretrained(model_name, local_files_only = True)

    # Write an empty line to the error stream, to sync with Java code.
    # From Java, when reading from error stream two cases are possible:
    #   - Single empty line: all OK in this Python script.
    #   - Otherwise: an exception has been thrown in this Python script.
    print("", flush = True, file = sys.stderr)

    # ********** End of Init phase **********

    # ********** Start of Run phase **********

    conversation = []
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

        # Prepare the text to submit to the t5 model.
        if len(conversation) == 0:
            text = original
        else:
            text = (" %s " % separator).join(conversation) + " " + separator + " " + original

        # Rewrite the original text using the predictor loaded before.
        if gpu_device is not None:
            text_tokens = tokenizer(text, return_tensors = "pt")
            text_ids = text_tokens.input_ids.to(gpu_device)
            text_attention = text_tokens.attention_mask.to(gpu_device)
            text_rewritten = model.generate(input_ids = text_ids, attention_mask = text_attention,
                                            max_length = model_max_tokens, num_beams = 10).cpu().detach()

            # Delete resources from GPU memory.
            del text_ids
            del text_attention
        else:
            text_tokens = tokenizer(text, return_tensors = "pt").input_ids
            text_rewritten = model.generate(input_ids = text_tokens, max_length = model_max_tokens).detach()

        rewritten = tokenizer.batch_decode(text_rewritten, skip_special_tokens = True)[0]


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
