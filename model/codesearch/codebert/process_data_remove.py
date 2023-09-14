# -*- coding: utf-8 -*-
# Copyright (c) Microsoft Corporation. 
# Licensed under the MIT license.

import gzip
import os
import json
import numpy as np
from more_itertools import chunked

# todo
# DATA_DIR='../data/test/category/remove_identifier/'
DATA_DIR='../data/test/base/'

def format_str(string):
    for char in ['\r\n', '\r', '\n']:
        string = string.replace(char, ' ')
    return string


from tqdm import *

def preprocess_test_data(language, test_batch_size=1000):
    # todo
    path = os.path.join(DATA_DIR, 'test_new_0805.txt'.format(language))
    print(path)
    with open(path, 'r') as pf:
        data = pf.readlines()  

    idxs = np.arange(len(data))
    data = np.array(data, dtype=object)

    np.random.seed(100)   # set random seed so that random things are reproducible
    np.random.shuffle(idxs)
    data = data[idxs]

    # f = open("head_100.txt","w")
    # for d in data[:1000]:
    #     f.write(d)
    # f.close()


    batched_data = chunked(data, test_batch_size)
    print("start processing")
    for batch_idx, batch_data in enumerate(batched_data):
        if len(batch_data) < test_batch_size:
            break # the last batch is smaller than the others, exclude.
        examples = []
        for d_idx, d in tqdm(enumerate(batch_data)):
            # line_a = json.loads(str(d, encoding='utf-8'))
            # doc_token = ' '.join(line_a['docstring_tokens'])
            line_a_split = d.split("<CODESPLIT>")
            doc_token = line_a_split[1]
            for dd in batch_data:
                # line_b = json.loads(str(dd, encoding='utf-8'))
                # code_token = ' '.join([format_str(token) for token in line_b['code_tokens']])
                line_b_split = dd.split("<CODESPLIT>")
                code_token = line_b_split[0]
                try:
                    example = (str(1), line_a_split[2].strip(), line_b_split[2].strip(), doc_token, code_token)
                except:
                    print(line_a_split)
                    print(line_b_split)
                    # exit()
                example = '<CODESPLIT>'.join(example)
                examples.append(example)

        data_path = os.path.join(DATA_DIR)
        if not os.path.exists(data_path):
            os.makedirs(data_path)
        file_path = os.path.join(data_path, 'batch_{}.txt'.format(batch_idx))
        print(file_path)
        with open(file_path, 'w', encoding='utf-8') as f:
            f.writelines('\n'.join(examples))

if __name__ == '__main__':
    languages = ['java']
    for lang in languages:
        preprocess_test_data(lang)
