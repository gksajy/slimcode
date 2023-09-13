package githubcode.slimcode;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.ast.CompilationUnit;
import combination.SpanContent;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


public class SlimCode {

    public static void markFlag(int[] codeFlag,SpanContent spanContent,int flag,String code,boolean[] otherFlag){
        int startWord = spanContent.startWord;
        int endWord = spanContent.endWord;
        for (int i=startWord;i<=endWord-1;i++){
            codeFlag[i] = flag;
            if (otherFlag!=null){
                otherFlag[i] = true;
            }

        }
    }


    public static int targetLength = 100;


    public static ArrayList<Integer> getRemovedIndex(String[] codeSplits,int[] codeFlag){
        ArrayList<Integer> removeIndex = new ArrayList<>();
        int removeTargetLength = codeSplits.length - targetLength;
        for (int i=8;i>0;i--){
            for (int j = codeSplits.length-1;j>=0;j--){
                if (codeFlag[j] == i){
                    if (removeIndex.size() >= removeTargetLength){
                        return removeIndex;
                    }
                    removeIndex.add(j);
                }
            }
        }
        return removeIndex;

    }


    public static int id = 0;

    public static FileOutputStream fileOutputStream_log = null;
    public static OutputStreamWriter outputStreamWriter_log = null;
    public static BufferedWriter bufferedWriter_log = null;

    public static long astTime = 0;
    public static long labelTime = 0;
    public static long removeTime = 0;

    public static String removeCode(String code, Map map,int targetLength){
        if (code.split(" +").length <= targetLength){
            return code;
        }
        if(fileOutputStream_log == null){
            try {
                fileOutputStream_log = new FileOutputStream("log.txt");
                outputStreamWriter_log = new OutputStreamWriter(fileOutputStream_log);
                bufferedWriter_log = new BufferedWriter(outputStreamWriter_log);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }


        long startTime = System.currentTimeMillis();
        ArrayList<SpanContent> identifierList = (ArrayList<SpanContent>) map.get("identifiers");
        ArrayList<SpanContent> invocationList = (ArrayList<SpanContent>) map.get("function_invocation");
        ArrayList<SpanContent> structureList = (ArrayList<SpanContent>) map.get("function_structure");
        ArrayList<SpanContent> signatureList = (ArrayList<SpanContent>) map.get("method_signature");
        ArrayList<SpanContent> simpleSymbolList = new ArrayList<SpanContent>();
        ArrayList<SpanContent> otherList = new ArrayList<SpanContent>();

        String[] codeSplits = code.split(" +");
        boolean[] structureFlag = new boolean[codeSplits.length];
        boolean[] invocationFlag = new boolean[codeSplits.length];
        boolean[] identifierFlag = new boolean[codeSplits.length];
        boolean[] simpleSymbolFlag = new boolean[codeSplits.length];

        int[] codeFlag = new int[codeSplits.length];
        // signature > identifier > structure > invocation > simple symbols
        for (SpanContent spanContent : signatureList){
            markFlag(codeFlag,spanContent,1,code,null);
        }
        for (SpanContent spanContent : identifierList){
            markFlag(codeFlag,spanContent,-1,code,identifierFlag);
        }
        for (SpanContent spanContent : structureList){
            markFlag(codeFlag,spanContent,-1,code,structureFlag);
        }
        for (SpanContent spanContent : invocationList){
            markFlag(codeFlag,spanContent,-1,code,invocationFlag);
        }

        // simple
        String[] simpleStr = new String[]{"=", "+", "-", "*", "/", "%", "!", ">",  "<", "|", "?", ":", "~", "&", "^", "(",
                "{", ")", "}", "[", ".", "]", ";", "\"", ",","==","++","--","!=",">=","<=","&&","||","<<",">>",">>>","\'"
        };
        List<String> simpleList = Arrays.asList(simpleStr);
        for(int i = 0;i< codeSplits.length;i++){
            if (simpleList.contains(codeSplits[i])){
                codeFlag[i] = 8;
                simpleSymbolFlag[i] =true;
            }
        }

        //other
        for (int i = 0; i<codeSplits.length;i++){
            if (codeFlag[i] == 0){
                int start = i;
                while (start < codeFlag.length && codeFlag[start] == 0){
                    start ++;
                }
                int end = start;
                for (int k=i;k<end;k++){
                    codeFlag[k] = 7;
                }
            }
        }

        for (int i = 0; i< codeSplits.length;i++){
            if (!simpleSymbolFlag[i] && structureFlag[i] && !identifierFlag[i]){
                codeFlag[i] = 6;
            }else if (!simpleSymbolFlag[i] && invocationFlag[i] && !identifierFlag[i]){
                codeFlag[i] = 5;
            }else if (!simpleSymbolFlag[i] && structureFlag[i] && identifierFlag[i]){
                codeFlag[i] = 2;
            }else if (!simpleSymbolFlag[i] && invocationFlag[i] && identifierFlag[i]){
                codeFlag[i] = 3;
            }else if (!simpleSymbolFlag[i] && !invocationFlag[i] && !structureFlag[i] && identifierFlag[i]){
                codeFlag[i] = 4;
            }
        }

        long endTime = System.currentTimeMillis();
	    long time = endTime - startTime;
	    labelTime += time;
        startTime = System.currentTimeMillis();
        //删除优先级：simple symbols > structure中非identifier >  invocation中非identifier > 非structre和invocation中的identifier >
        // invocation中identifier > structure中的identifier > signature
        String removedCode = "";
        ArrayList<Integer> removedIndex = getRemovedIndex(codeSplits, codeFlag);
        for (int index : removedIndex){
            removedCode += codeSplits[index] + " ";
            codeSplits[index] = "";
        }
//        System.out.println(removedCode);

        String new_code = String.join(" ",codeSplits);

       endTime = System.currentTimeMillis();
       time = endTime - startTime;
       removeTime += time;

        try {
            if (id < 1000){
                bufferedWriter_log.write(id + "：" + removedCode + "\n");
                bufferedWriter_log.write(id + "：" + code+"\n");
                bufferedWriter_log.write(id + "：" + new_code + "\n\n");
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
        return new_code;
    }

    public static String remove(String code){

        if (code.split(" +").length <= targetLength){
            return code;
        }
	    long startTime = System.currentTimeMillis();
        MyVisitor myVisitor = new MyVisitor(code);
        CompilationUnit cu = JavaParser.parse(myVisitor.code);
        myVisitor.visit(cu, null);
//        System.out.println(myVisitor.map);
        long endTime = System.currentTimeMillis();
       	long time = endTime - startTime;
	    astTime += time;

	    String removedCode = removeCode(code, myVisitor.map,targetLength);
//        System.out.println(removedCode);
        return removedCode;
    }

    public static List<String> readFile(String path) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(path);
        InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        List<String> stringsList = new ArrayList<String>();
        String stringTmp = "";
        while ((stringTmp = bufferedReader.readLine())!=null){
            stringsList.add(stringTmp);
        }
        inputStreamReader.close();
        return stringsList;

    }
    public static void main(String[] args) {

        try {
            long startTime = System.currentTimeMillis();
            String stage = "test";
            List<String> stringList = readFile("data/" + stage + "_no_common.txt");
            FileOutputStream fileOutputStream = new FileOutputStream("remove_results/" + stage + "_remove_slimcode_50.txt");
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
            double allRemovePercent = 0;
            int count = 0;
            for(int i=0; i < stringList.size();i++){
                id = i;
                if (i % 10000 == 0){
                    System.out.println("writing " + i + "examples");
                }
                String lineStr = stringList.get(i);
                String[] codeList = lineStr.split("<CODESPLIT>");
		        String code = codeList[4];
//    		String code = codeList[0];
                int originLength = code.split(" +").length;
                try {

                    code = remove(code);
                  codeList[4] = code;
//                  codeList[0] = code;
                    int removeLength = code.split(" +").length;
                    if (i < 100){
                        System.out.println("removed length:"+removeLength);
                    }
                    double removePercent = (originLength - removeLength) * 1.0 / originLength;
                    allRemovePercent += removePercent;
                    String newLine = String.join("<CODESPLIT>", codeList);
                    bufferedWriter.write(newLine + "\n");
                    count++;
                }catch (ParseProblemException e){
                    continue;
                }
//            System.out.println("cut:"+code.split(" +").length);
            }
            bufferedWriter.close();
            outputStreamWriter.close();
            System.out.println(count+"/"+stringList.size());
            System.out.println("共有"+count+"条数据");
            double avgRemovePercent = allRemovePercent / count;
            System.out.println("avg remove percent:"+avgRemovePercent);

            long endTime = System.currentTimeMillis();

	        System.out.println("astTime:"+ astTime);
	        System.out.println("labelTime:"+labelTime);
            System.out.println("removeTime:" + removeTime);
	        System.out.println("totalTime:" + (endTime - startTime));
	    //关闭
            if (bufferedWriter_log != null){
                bufferedWriter_log.close();
                outputStreamWriter_log.close();
                fileOutputStream_log.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
