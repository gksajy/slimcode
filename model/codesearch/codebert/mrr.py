# -*- coding: utf-8 -*-
# Copyright (c) Microsoft Corporation. 
# Licensed under the MIT license.

import os
import numpy as np
from more_itertools import chunked
import argparse


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--test_batch_size', type=int, default=1000)
    args = parser.parse_args()
    languages = ['java']
    MRR_dict = {}
    for language in languages:
        # file_dir = './results/codebert/codesearch/remove_combination_10/'
        # file_dir = './results/remove_pdg_0728/'
        # file_dir = './results/codebert/remove_combination_180/'
        # file_dir = './results/codet5/random/remove_random_10'
        # file_dir = './results/codet5/category/remove_identifier'
        # file_dir = './results/codebert/category/remove_identifier/'
        file_dir = './results/codet5/base'
        # file_dir = './results/combination/codet5/base_0826'
        # print(f"file_dir:{file_dir}".center(100,"*"))
        ranks = []
        num_batch = 0
        for file in sorted(os.listdir(file_dir)):
            print(os.path.join(file_dir, file))
            with open(os.path.join(file_dir, file), encoding='utf-8') as f:
                batched_data = chunked(f.readlines(), args.test_batch_size)
                for batch_idx, batch_data in enumerate(batched_data):
                    num_batch += 1
                    correct_score = float(batch_data[batch_idx].strip().split('<CODESPLIT>')[-1])
                    scores = np.array([float(data.strip().split('<CODESPLIT>')[-1]) for data in batch_data])
                    rank = np.sum(scores >= correct_score)

                    ranks.append(rank)

        mean_mrr = np.mean(1.0 / np.array(ranks))
        print("{} mrr: {}".format(language, mean_mrr))
        MRR_dict[language] = mean_mrr
    for key, val in MRR_dict.items():
        print("{} mrr: {}".format(key, val))


if __name__ == "__main__":
    main()
