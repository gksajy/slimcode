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
