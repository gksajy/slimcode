# SlimCode
This repo provides the code for reproducing the experiments in SlimCode. SlimCode is a program simplification method that consider more on the nature of the code.
# Requirments
- [python3](https://www.python.org/)
- [PyTorch](https://pytorch.org/)
- [JDK1.8](https://www.oracle.com/java/technologies/downloads/)
# Quick Start
## Prepare the dataset
We use the same dataset as [CodeBERT](https://github.com/microsoft/CodeBERT/tree/master/CodeBERT) and [Dietcode](https://github.com/zhangzwwww/DietCode). But we remove all the comments in the code to make more code can be converted to AST and remove the code that can't be converted to AST after removing the comments.
The original data can be downloaded from [CodeBERT](https://github.com/microsoft/CodeBERT/tree/master/CodeBERT) and our preprocessed data can be download from [SlimCode](https://drive.google.com/drive/folders/1TlhFHyHODQbu7wu30Tg4TQCEmSsKiLJo?usp=drive_link).The final specific data volume is summarized as follows.
<table align="center">  
	<tr>
    <td rowspan="2" style="text-align: center;" align="center">Dataset Volume</td> 
		<td colspan="3" style="text-align: center;" align="center">Code Search</td> 
    <td colspan="3" style="text-align: center;" align="center">Code2nl</td> 
	</tr> 
  <tr>    
    <td align="center">train</td>
    <td align="center">valid</td>
    <td align="center">test</td>
    <td align="center">train</td>
    <td align="center">valid</td>
    <td align="center">test</td>
	</tr>
	<tr>    
		<td align="center">CodeBERT/DietCode</td>    
    <td align="center">908886</td>
    <td align="center">30655</td>
    <td align="center">26909</td>
    <td align="center">164923</td>
    <td align="center">5183</td>
    <td align="center">10955</td>
	</tr>
 <tr>
   <td align="center">SlimCode</td>
    <td align="center">904817</td>
    <td align="center">30437</td>
    <td align="center">26780</td>
    <td align="center">164813</td>
    <td align="center">5183</td>
    <td align="center">10948</td>
 </tr>
</table>

## Process the dataset
### Random process
We remove tokens from the code randomly and we reference the code from [DietCode](https://github.com/zhangzwwww/DietCode).Our modified code can be found [here](https://github.com/cufelxn/slimcode/tree/main/random).We use the code to remove 10%-50% tokens from the given code snippet.
### Category process
We divide the tokens in the code into 3 levels: lexical level, syntactic level and semantic level. The lexical level includes symbol tokens and identifiers. Syntactic level includes structure tokens, signature tokens and invocation tokens. The semantic level includes the tokens in PDG. In the first two levels,we recognize identifiers,structure tokens,signature tokens and invocation tokens from the code by AST.And we use [JavaParser](https://mvnrepository.com/artifact/com.github.javaparser/javaparser-core) to convert the code into AST and then we remove the tokens from the code by AST independently.The relationship of our category and the node type can be seen in the table.
<table align="center" bgcolor="#E0E0E0" style="background-color:white;">
	<tr>
		<th>level</th>
		<th>category</th>
		<th>node type</th>
	</tr>
	<tr>
		<th rowspan="2" style="background-color: transparent;">lexical</th>
		<td>symbol tokens</td>
		<td>None</td>
	</tr>
 	<tr>
		<td>identifier</td>
		<td>NameExpr node <br> VariableDeclarationExpr</td>
	</tr>
	<tr>
		<th rowspan="3">syntactic</th>
		<td>structure tokens</td>
		<td>
			TryStmt node <br>
			IfStmt node <br>
			SwitchStmt node <br>
			WhileStmt node <br>
			DoStmt node <br>
			ForStmt node <br>
			ForeachStmt node <br>
		</td>
	</tr>
	<tr>
		<td>signature tokens</td>
		<td>MethodDeclaration node</td>
	</tr>
	<tr>
		<td>invocation tokens</td>
		<td>MethodCallExpr node</td>
	</tr>
	<tr>
		<th>semantic</th>
		<td>PDG tokens</td>
		<td>javaDependencyGraph</td>
	</tr>
</table>

For the last level,we modfied the [javaDependencyGraph](https://github.com/hpnog/javaDependenceGraph) to generate PDG for a large number of functions in our dataset.Our modified code can be found [here](https://github.com/cufelxn/slimcode/tree/main/PDG).Because we remove the tokens in the code in line,the function of the code in the data should include "\n" in the end of the line so that we can remove the code by PDG in line.So we provide our preprocessed dataset for PDG.Our preprocessed dataset can be found [here](https://drive.google.com/drive/folders/1rkF0ggK4pJt2IcjB-EW4CuvROqlpbCAX?usp=drive_link).
### DietCode process
We modified the code of DietCode to process the dataset in diffient removal percent.Our modified code can be found [here](https://github.com/cufelxn/slimcode/tree/main/dietcode).After the dataset is processed by DietCode,then we feed them to CodeBERT and CodeT5 for codesearch and code2nl.
### SlimCode process
Based on the result of category removal,we proposed SlimCode.Its core idea is to prioritize removing words that have less impact on downstream tasks according to our results.Our removal order is symbol tokens > the tokens beyond our category > not identifier tokens in structure > not identifier tokens in invocation > identifiers not in structure and invocation > identifiers in invocation > identifiers in structure > signature tokens.Similarly,we get removal order by AST and then remove them in the code in different removal percent.Our code can be found [here](https://github.com/cufelxn/slimcode/tree/main/slimcode).It's necessary to have a JDK8 in your computer and then you can use the follow command to compile the code. <br>
```./jdk1.8.0_341/bin/javac -classpath ./javaparser-core-3.6.5.jar -d bin SlimCode.java RemoveAll.java SpanContent.java -Xlint:unchecked``` <br>
And then you can use the follow command to run the code. <br>
```./jdk1.8.0_341/bin/java -classpath ./javaparser-core-3.6.5.jar:bin/ SlimCode```
## Finetune
After processing the dataset, you can feed the data into codebert,codet5 for codesearch and code summarization.
### Code Search
#### CodeBERT
The code for code search of CodeBERT can be found [here](https://github.com/cufelxn/slimcode/tree/main/model/codesearch/codebert).It is from [CodeBERT](https://github.com/microsoft/CodeBERT/tree/master/CodeBERT) and we did't modify it. <br>
training:
```
python run_classifier.py --model_type roberta --task_name codesearch --do_train --do_eval --train_file train_no_comment.txt --dev_file valid_no_comment.txt --max_seq_length 200 --per_gpu_train_batch_size 320 --per_gpu_eval_batch_size 320 --learning_rate 1e-5 --num_train_epochs 4 --gradient_accumulation_steps 1 --overwrite_output_dir --data_dir ../data/train_valid/base/ --output_dir ./codebert/base/  --model_name_or_path microsoft/codebert-base
```
evaluating:
```
python run_classifier.py --model_type roberta --model_name_or_path microsoft/codebert-base --task_name codesearch --do_predict --output_dir ./codebert/base/ --data_dir ../data/test/base/ --max_seq_length 200 --per_gpu_train_batch_size 320 --per_gpu_eval_batch_size 320 --learning_rate 1e-5 --num_train_epochs 4 --test_file batch_0.txt --pred_model_dir ./codebert/base/ --test_result_dir ./results/codebert/base/0_batch_result.txt
```
### CodeT5
The code for code search of CodeT5 can be found [here](https://github.com/cufelxn/slimcode/tree/main/model/codesearch/codet5).It is originally from [DietCode](https://github.com/zhangzwwww/DietCode).And we modified it for code search and not remove tokens from the code. <br>
training:
```
python run_classifier.py --model_type codet5 --task_name codesearch --do_train --do_eval --train_file train_no_comment.txt --dev_file valid_no_comment.txt --max_seq_length 200 --per_gpu_train_batch_size 320 --per_gpu_eval_batch_size 320 --learning_rate 1e-5 --num_train_epochs 4 --gradient_accumulation_steps 1 --overwrite_output_dir --data_dir ../data/train_valid/base/ --output_dir ./codet5/base/ --model_name_or_path Salesforce/codet5-base --tokenizer_name Salesforce/codet5-base
```
evaluating:
```
python run_classifier.py --model_type codet5 --model_name_or_path Salesforce/codet5-base --task_name codesearch --do_predict --output_dir ./codet5/base/ --data_dir ../data/test/base_t5/ --max_seq_length 200 --per_gpu_train_batch_size 320 --per_gpu_eval_batch_size 320 --learning_rate 1e-5 --num_train_epochs 4 --test_file batch_0.txt --pred_model_dir ./codet5/base/checkpoint-best/ --test_result_dir ./results/codet5/base/0_batch_result.txt --tokenizer_name Salesforce/codet5-base
```
## Code2nl
### CodeBERT
The code for code2nl of CodeBERT can be found [here](https://github.com/cufelxn/slimcode/tree/main/model/code2nl/codebert).It is originally from [CodeBert](https://github.com/microsoft/CodeBERT/tree/master/CodeBERT).And we modified the code for fixed epochs and evaluate only in the end of every epoch for time comparation.<br>
training:
```
python run_codebert.py --do_train --do_eval --model_type roberta --model_name_or_path microsoft/codebert-base --train_filename ./data/base/train.txt --dev_filename ../data/base/valid.txt --output_dir ./codebert/base --max_source_length 256 --max_target_length 128 --beam_size 10 --train_batch_size 64 --eval_batch_size 64 -learning_rate 5e-5
```
evaluating:
```
python run_codebert_three.py --do_test --model_type roberta --model_name_or_path microsoft/codebert-base --load_model_path codebert/base/checkpoint-best-bleu/pytorch_model.bin  --test_filename ./data/base/test.txt --output_dir codebert/base --max_source_length 256 --max_target_length 128 --beam_size 10 --eval_batch_size 64
```
### CodeT5
The code for code2nl of CodeT5 can be found [here](https://github.com/cufelxn/slimcode/tree/main/model/code2nl/codet5).It is originally from [CodeT5](https://github.com/salesforce/CodeT5/tree/main/CodeT5). We modfied the code for fixed epochs and not stop early. <br>
training and evaluating:
```
python run_exp.py --model_tag codet5_base --task summarize --sub_task java
```
