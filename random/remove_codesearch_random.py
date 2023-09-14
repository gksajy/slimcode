
STAGE = "train"
f = open(f"../data/codesearch/train_valid/random/base/{STAGE}_new_0805.txt","r")
lines = f.readlines()
import json
f.close()


f = open(f"random/remove_random_50/{STAGE}_remove_50.txt","w")

from tqdm import *

# 按照一定的概率删除方法中的词
def random_prune_code_with_ratio(code, rate=0.4):
    random.seed(100)
    def random_select_tokens(tokens, ratio):
        pruned_index = sorted(random.sample(range(0, len(tokens)), int(len(tokens) * rate)), reverse=True)
        for index in pruned_index:
            del tokens[index:index+1]
        return tokens
    tokens = code.split(' ')
    result = random_select_tokens(tokens, rate)
    return ' '.join(result)

for idx,line in tqdm(enumerate(lines)):
    line = line.strip()
    line_split = line.split("<CODESPLIT>")
    code = line_split[4]
    # code = line_split[0]
    if idx < 100:
        print(f"origin length:{len(code.split())}")
    code = random_prune_code_with_ratio(code,0.5)
    # line_split[0] = code
    line_split[4] = code
    if idx < 100:
        print(f"now length:{len(code.split())}")

    line_str = "<CODESPLIT>".join(line_split)
    f.write(line_str + "\n")
f.close()
