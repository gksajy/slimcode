# slimcode
This repo provides the code for reproducing the experiments in SlimCode. SlimCode is a program simplification method that consider more on the nature of the code.
# Requirments
- [python3](https://www.python.org/)
- [PyTorch](https://pytorch.org/)
- [JDK1.8](https://www.oracle.com/java/technologies/downloads/)
# Quick Start
## Prepare the dataset
We use the same dataset as CodeBERT and Dietcode. But we remove all the comments in the code to make more code can be converted to AST and remove the code that can't be converted to AST after removing the comments.
The original data can be downloaded from [CodeBERT](https://github.com/microsoft/CodeBERT/tree/master/CodeBERT) and our preprocessed data can be download from [SlimCode](https://drive.google.com/drive/folders/1IV9a9Dc9aZRXYUHRXjBN2fNr6wIdmrLT?usp=drive_link).
## Process the dataset
### random process
We remove tokens from the code randomly and we reference the code from [DietCode](https://github.com/zhangzwwww/DietCode).Our modified code is can be found [here](https://github.com/cufelxn/slimcode/tree/main/random).It's easy to use the code to process our preprocessed data. 
### category removal
We divide the tokens in the code into 3 levels: lexical level, Syntactic level and semantic level. The lexical level includes symbol tokens and identifiers. Syntactic level includes structure tokens, signature tokens and invocation tokens. The semantic level includes the tokens in PDG. For the first two levels,we recognize them from the code by AST.And we use [JavaParser](https://mvnrepository.com/artifact/com.github.javaparser/javaparser-core) to convert the code into AST and then we remove the tokens from the code by AST independently. For the last level,we moditified the [javaDependencyGraph](https://github.com/hpnog/javaDependenceGraph) to generate PDGS for a large number of functions in our dataset.Our modiified code can be found [here](url).
### dietcode removal

### slimcode removal

