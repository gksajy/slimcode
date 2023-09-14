# coding=utf-8
# Copyright 2018 The Google AI Language Team Authors and The HuggingFace Inc. team.
# Copyright (c) 2018, NVIDIA CORPORATION.  All rights reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
""" BERT classification fine-tuning: utilities to work with GLUE tasks """

from __future__ import absolute_import, division, print_function

import csv
import logging
import os
import sys
from io import open
from sklearn.metrics import f1_score, recall_score

csv.field_size_limit(sys.maxsize)
logger = logging.getLogger(__name__)


class InputExample(object):
    """A single training/test example for simple sequence classification."""

    def __init__(self, guid, text_a, text_b=None, label=None):
        """Constructs a InputExample.

        Args:
            guid: Unique id for the example.
            text_a: string. The untokenized text of the first sequence. For single
            sequence tasks, only this sequence must be specified.
            text_b: (Optional) string. The untokenized text of the second sequence.
            Only must be specified for sequence pair tasks.
            label: (Optional) string. The label of the example. This should be
            specified for train and dev examples, but not for test examples.
        """
        self.guid = guid
        self.text_a = text_a
        self.text_b = text_b
        self.label = label


class InputFeatures(object):
    """A single set of features of data."""

    def __init__(self, input_ids, input_mask, segment_ids, label_id):
        self.input_ids = input_ids
        self.input_mask = input_mask
        self.segment_ids = segment_ids
        self.label_id = label_id


class DataProcessor(object):
    """Base class for data converters for sequence classification data sets."""

    def get_train_examples(self, data_dir):
        """Gets a collection of `InputExample`s for the train set."""
        raise NotImplementedError()

    def get_dev_examples(self, data_dir):
        """Gets a collection of `InputExample`s for the dev set."""
        raise NotImplementedError()

    def get_labels(self):
        """Gets the list of labels for this data set."""
        raise NotImplementedError()

    @classmethod
    def _read_tsv(cls, input_file, quotechar=None):
        """Reads a tab separated value file."""
        with open(input_file, "r", encoding='utf-8') as f:
            lines = []
            for line in f.readlines():
                line = line.strip().split('<CODESPLIT>')
                if len(line) != 5:
                    continue
                lines.append(line)
            return lines


class CodesearchProcessor(DataProcessor):
    """Processor for the MRPC data set (GLUE version)."""

    def get_train_examples(self, data_dir, train_file):
        """See base class."""
        logger.info("LOOKING AT {}".format(os.path.join(data_dir, train_file)))
        return self._create_examples(
            self._read_tsv(os.path.join(data_dir, train_file)), "train")

    def get_dev_examples(self, data_dir, dev_file):
        """See base class."""
        logger.info("LOOKING AT {}".format(os.path.join(data_dir, dev_file)))
        return self._create_examples(
            self._read_tsv(os.path.join(data_dir, dev_file)), "dev")

    def get_test_examples(self, data_dir, test_file):
        """See base class."""
        logger.info("LOOKING AT {}".format(os.path.join(data_dir, test_file)))
        return self._create_examples(
            self._read_tsv(os.path.join(data_dir, test_file)), "test")

    def get_labels(self):
        """See base class."""
        return ["0", "1"]

    def _create_examples(self, lines, set_type):
        """Creates examples for the training and dev sets."""
        examples = []
        for (i, line) in enumerate(lines):
            # guid = "%s-%s" % (set_type, i)
            guid = i
            text_a = line[3]
            text_b = line[4]

            if (set_type == 'test'):
                label = self.get_labels()[0]
            else:
                label = line[0]
            examples.append(
                InputExample(guid=guid, text_a=text_a, text_b=text_b, label=label))
        if (set_type == 'test'):
            return examples, lines
        else:
            return examples


import random
from tqdm import *

# 删除代码的token到120
def remove_random_no_w2v(method_code,target_len):
    method_list = method_code.split()
    if len(method_list) <= target_len:
        return method_code
    # nsp_count = int(len(method_list) * 0.4)
    random.seed(5201314)
    while len(method_list) > target_len:
        rand_removed_token = random.choice(method_list)
        method_list.remove(rand_removed_token)
    return " ".join(method_list)


def random_prune_code_with_ratio(code, rate=0.4):
    random.seed(5201314)
    def random_select_tokens(tokens, ratio):
        pruned_index = sorted(random.sample(range(0, len(tokens)), int(len(tokens) * rate)), reverse=True)
        for index in pruned_index:
            del tokens[index:index+1]
        return tokens
    tokens = code.split(' ')
    result = random_select_tokens(tokens, rate)
    return ' '.join(result)

# 删除一个方法中的静态词
def remove_static_less(static_tokens,example_text):
    for static_token in static_tokens:
        example_text = example_text.replace(static_token,"")
    return example_text

def remove_function_structure(code):
    bracket_index = 0
    try:
        flag = True
        bracket_index = code.index("{")
    except:
        flag = False
    return flag,code[bracket_index:]

def get_static_tokens():
    with open('static_tokens.txt', 'r') as file_obj:
        tokens = file_obj.readlines()

    tokens = [token.strip() for token in tokens]
    return tokens

def convert_examples_to_features(examples, ttype, label_list, max_seq_length,
                                 tokenizer, output_mode,
                                 cls_token_at_end=False, pad_on_left=False,
                                 cls_token='[CLS]', sep_token='[SEP]', pad_token=0,
                                 sequence_a_segment_id=0, sequence_b_segment_id=1,
                                 cls_token_segment_id=1, pad_token_segment_id=0,
                                 mask_padding_with_zero=True):
    """ Loads a data file into a list of `InputBatch`s
        `cls_token_at_end` define the location of the CLS token:
            - False (Default, BERT/XLM pattern): [CLS] + A + [SEP] + B + [SEP]
            - True (XLNet/GPT pattern): A + [SEP] + B + [SEP] + [CLS]
        `cls_token_segment_id` define the segment id associated to the CLS token (0 for BERT, 2 for XLNet)
    """

    label_map = {label: i for i, label in enumerate(label_list)}

    features = []

    # static_tokens = get_static_tokens()
    # print(str(len(examples)).center(100, "="))
    # for id,example in enumerate(tqdm(examples)):
    #     if example.text_b:
    #         tmp_list = example.text_b.split()
    #         example.text_b = " ".join(tmp_list)
    #         # id = example.guid
    #         # method_list, method_code,delete_flag = remove_random(model_w2v, example.text_b)
    #         method_code = remove_static_less(static_tokens,example.text_b)
    #         if id < 10:
    #             print(f"origin code length:{len(example.text_b.split())}".center(100,"*"))
    #         # if delete_flag:
    #         #     examples.remove(example)
    #         # all_removed_static_list.append(method_list)
    #         example.text_b = method_code
    #         if id < 10:
    #             print(f"cut code length:{len(example.text_b.split())}".center(100,"*"))
    #             print(example.text_b)
            # if len(example.text_b.split()) > max_text_b:
            #     # if len(tokenizer.tokenize(example.text_b)) > (max_seq_length - 50) * 0.85:
            #     #     examples.remove(example)
            #     #     continue
            #     max_text_b = len(example.text_b.split())
    # print(str(max_text_b).center(100, "="))
    # print(f"length:120".center(100, "="))

    # static_tokens = get_static_tokens()
    # print(str(len(examples)).center(100, "="))
    # for id,example in enumerate(tqdm(examples)):
    #     if example.text_b:
    #         tmp_list = example.text_b.split()
    #         example.text_b = " ".join(tmp_list)
    #         # id = example.guid
    #         # method_list, method_code,delete_flag = remove_random(model_w2v, example.text_b)
    #         remove_flag,method_code = remove_function_structure(example.text_b)
    #         if not remove_flag:
    #             continue
    #         if id < 10:
    #             print(f"origin code length:{len(example.text_b.split())}".center(100,"*"))
    #         # if delete_flag:
    #         #     examples.remove(example)
    #         # all_removed_static_list.append(method_list)
    #         example.text_b = method_code
    #         if id < 10:
    #             print(f"cut code length:{len(example.text_b.split())}".center(100,"*"))
    #             print(example.text_b)
    #         # if len(example.text_b.split()) > max_text_b:
    #         #     # if len(tokenizer.tokenize(example.text_b)) > (max_seq_length - 50) * 0.85:
    #         #     #     examples.remove(example)
    #         #     #     continue
    #         #     max_text_b = len(example.text_b.split())
    # # print(str(max_text_b).center(100, "="))
    # # print(f"length:120".center(100, "="))



    for (ex_index, example) in enumerate(examples):
        if ex_index % 10000 == 0:
            logger.info("Writing example %d of %d" % (ex_index, len(examples)))

        # tokens_a是文本
        tokens_a = tokenizer.tokenize(example.text_a)[:50]
        # tokens_b是java代码
        tokens_b = None

        if example.text_b:


            tokens_b = tokenizer.tokenize(example.text_b)
            # Modifies `tokens_a` and `tokens_b` in place so that the total
            # length is less than the specified length.
            # Account for [CLS], [SEP], [SEP] with "- 3"
            _truncate_seq_pair(tokens_a, tokens_b, max_seq_length - 3)
        else:
            # Account for [CLS] and [SEP] with "- 2"
            if len(tokens_a) > max_seq_length - 2:
                tokens_a = tokens_a[:(max_seq_length - 2)]

        # The convention in BERT is:
        # (a) For sequence pairs:
        #  tokens:   [CLS] is this jack ##son ##ville ? [SEP] no it is not . [SEP]
        #  type_ids:   0   0  0    0    0     0       0   0   1  1  1  1   1   1
        # (b) For single sequences:
        #  tokens:   [CLS] the dog is hairy . [SEP]
        #  type_ids:   0   0   0   0  0     0   0
        #
        # Where "type_ids" are used to indicate whether this is the first
        # sequence or the second sequence. The embedding vectors for `type=0` and
        # `type=1` were learned during pre-training and are added to the wordpiece
        # embedding vector (and position vector). This is not *strictly* necessary
        # since the [SEP] token unambiguously separates the sequences, but it makes
        # it easier for the model to learn the concept of sequences.
        #
        # For classification tasks, the first vector (corresponding to [CLS]) is
        # used as as the "sentence vector". Note that this only makes sense because
        # the entire model is fine-tuned.
        tokens = tokens_a + [sep_token]
        segment_ids = [sequence_a_segment_id] * len(tokens)

        if tokens_b:
            tokens += tokens_b + [sep_token]
            segment_ids += [sequence_b_segment_id] * (len(tokens_b) + 1)

        if cls_token_at_end:
            tokens = tokens + [cls_token]
            segment_ids = segment_ids + [cls_token_segment_id]
        else:
            tokens = [cls_token] + tokens
            segment_ids = [cls_token_segment_id] + segment_ids

        input_ids = tokenizer.convert_tokens_to_ids(tokens)

        # The mask has 1 for real tokens and 0 for padding tokens. Only real
        # tokens are attended to.
        input_mask = [1 if mask_padding_with_zero else 0] * len(input_ids)

        # Zero-pad up to the sequence length.
        padding_length = max_seq_length - len(input_ids)
        if pad_on_left:
            input_ids = ([pad_token] * padding_length) + input_ids
            input_mask = ([0 if mask_padding_with_zero else 1] * padding_length) + input_mask
            segment_ids = ([pad_token_segment_id] * padding_length) + segment_ids
        else:
            input_ids = input_ids + ([pad_token] * padding_length)
            input_mask = input_mask + ([0 if mask_padding_with_zero else 1] * padding_length)
            segment_ids = segment_ids + ([pad_token_segment_id] * padding_length)

        assert len(input_ids) == max_seq_length
        assert len(input_mask) == max_seq_length
        assert len(segment_ids) == max_seq_length

        if output_mode == "classification":
            label_id = label_map[example.label]
        elif output_mode == "regression":
            label_id = float(example.label)
        else:
            raise KeyError(output_mode)

        if ex_index < 5:
            logger.info("*** Example ***")
            logger.info("guid: %s" % (example.guid))
            logger.info("tokens: %s" % " ".join(
                [str(x) for x in tokens]))
            logger.info("input_ids: %s" % " ".join([str(x) for x in input_ids]))
            logger.info("input_mask: %s" % " ".join([str(x) for x in input_mask]))
            logger.info("segment_ids: %s" % " ".join([str(x) for x in segment_ids]))
            logger.info("label: %s (id = %d)" % (example.label, label_id))
            logger.info(f"target_length:{len(example.text_b.split())}".center(100,"*"))

        features.append(
            InputFeatures(input_ids=input_ids,
                          input_mask=input_mask,
                          segment_ids=segment_ids,
                          label_id=label_id
                          ))


    return features


def _truncate_seq_pair(tokens_a, tokens_b, max_length):
    """Truncates a sequence pair in place to the maximum length."""

    # This is a simple heuristic which will always truncate the longer sequence
    # one token at a time. This makes more sense than truncating an equal percent
    # of tokens from each, since if one sequence is very short then each token
    # that's truncated likely contains more information than a longer sequence.
    while True:
        total_length = len(tokens_a) + len(tokens_b)
        if total_length <= max_length:
            break
        if len(tokens_a) > len(tokens_b):
            tokens_a.pop()
        else:
            tokens_b.pop()


def simple_accuracy(preds, labels):
    return (preds == labels).mean()

def acc_and_f1(preds, labels):
    acc = simple_accuracy(preds, labels)
    f1 = f1_score(y_true=labels, y_pred=preds)
    recall = recall_score(y_true=labels, y_pred=preds)
    return {
        "acc": acc,
        "f1": f1,
        "recall": recall,
        "acc_and_f1": (acc + f1) / 2,
    }


def compute_metrics(task_name, preds, labels):
    assert len(preds) == len(labels)
    if task_name == "codesearch":
        return acc_and_f1(preds, labels)
    else:
        raise KeyError(task_name)


processors = {
    "codesearch": CodesearchProcessor,
}

output_modes = {
    "codesearch": "classification",
}

GLUE_TASKS_NUM_LABELS = {
    "codesearch": 2,
}
