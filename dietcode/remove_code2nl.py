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

from weights import (WeightOutputer, Statement)
from prune import Code_Reduction

import numpy as np
import random
import re
import csv
import logging
import os
import sys
from io import open
from sklearn.metrics import f1_score

csv.field_size_limit(sys.maxsize)
logger = logging.getLogger(__name__)
outputFileIndex = 1
low_rated_tokens = []

def is_number(s):
    try:
        float(s)
        return True
    except ValueError:
        pass
    return False

def assimilate_code_string_and_integer(code, string_mask=" string ", number_mask="10"):
    quotation_index_list = []
    for i in range(0, len(code)):
        if code[i] == "\"":
            quotation_index_list.append(i)
    for i in range(len(quotation_index_list) - 1, 0, -2):
        code = code[:quotation_index_list[i-1] + 1] + string_mask + code[quotation_index_list[i]:]

    tokens = code.split(" ")
    for i in range(0, len(tokens)):
        if is_number(tokens[i]):
            tokens[i] = number_mask
    code = " ".join(tokens)

    return code

def delete_code_pattern(code, strategy='None', lang='java', **kwargs):
    result = ''
    if strategy == 'method-return':
        if lang == 'java':
            main_body_start = code.find('{') + 1
            result = code[:main_body_start]
            index = main_body_start
            while index < len(code):
                i = code.find(' return ', index)
                index = i + 1
                if i == -1:
                    result = result + " }"
                    break
                result += code[i:code.find(';', i) + 1]
        elif lang == 'python':
            statements = merge_python_statements(code)
            result = ' '
            for statement in statements:
                if len(statement) == 0:
                    continue
                if statement[0] == 'def' or statement[0] == 'return':
                    result = result + ' '.join(statement) + ' '
    elif strategy == 'trim':
        rate = 0.5
        # result = code[:int(len(code)*rate)]
        result = prune_tokens(code, 0.5)
        return code[:int(len(code)*rate)]
    elif strategy == 'slim':
        if lang == 'java':
            reduction = Code_Reduction(code, lang=lang)
            result = reduction.prune()
        elif lang == 'python':
            reduction = Code_Reduction(code, lang=lang)
            result = reduction.prune()
    elif strategy == 'variable':
        if lang == 'java':
            result = delete_code_with_variable_declaration(code)
        elif lang == 'python':
            pass
    elif strategy == 'loop':
        if lang == 'java':
            pass
        elif lang == 'python':
            statements = merge_python_statements(code)
            result = ' '
            for statement in statements:
                if len(statement) == 0:
                    continue
                if statement[0] != 'if' and statement[0] != 'while' and statement[0] != 'for':
                    result = result + ' '.join(statement) + ' '
    elif strategy == 'token':
        global low_rated_tokens
        if len(low_rated_tokens) == 0:
            logger.info("generate low rated tokens...")
            low_rated_tokens = generate_low_rated_tokens('./low_rated_word')
        if lang == 'java':
            result = ' '.join(prune_tokens(code, low_rated_tokens))
        elif lang == 'python':
            result = ' '.join(prune_tokens(code, low_rated_tokens))
    elif strategy == 'random':
        rate = 0.7
        if 'rate' in kwargs.keys():
            rate = kwargs['rate']
        result = random_prune_code_with_ratio(code, rate)
    elif strategy == 'None':
        return code
    else:
        return code
    return result


STAGE = "valid"
f = open(f"{STAGE}.txt","r")
lines = f.readlines()
import json
f.close()

f = open(f"{STAGE}_remove_40.txt","w")

line_list = []
max_s_len = 0
from tqdm import *
for idx,line in tqdm(enumerate(lines)):
    line = line.strip()
    line_split = line.split("<CODESPLIT>")
    code = line_split[0]
    if idx < 100:
        print(f"origin length:{len(code.split())}")
    code = assimilate_code_string_and_integer(code)
    code = delete_code_pattern(code, "slim", "java")
    line_split[0] = code
    if idx < 100:
        print(f"now length:{len(code.split())}")

    line_str = "<CODESPLIT>".join(line_split)
    f.write(line_str + "\n")
f.close()




