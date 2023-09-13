package githubcode.slimcode;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.ast.CompilationUnit;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class RemoveSignature {

    public static void markFlag(int[] codeFlag, SpanContent spanContent, int flag, String code, boolean[] otherFlag){
        int startWord = spanContent.startWord;
        int endWord = spanContent.endWord;
        for (int i=startWord;i<=endWord-1;i++){
            codeFlag[i] = flag;
            if (otherFlag!=null){
                otherFlag[i] = true;
            }

        }
    }


    public static int targetLength = 0;


    public static ArrayList<Integer> getRemovedIndex(String[] codeSplits,int[] codeFlag){
        ArrayList<Integer> removeIndex = new ArrayList<>();
            for (int j = codeSplits.length-1;j>=0;j--){
                if (codeFlag[j] == 1){
                    removeIndex.add(j);
                }
            }
        return removeIndex;

    }


    public static int id = 0;

    public static FileOutputStream fileOutputStream_log = null;
    public static OutputStreamWriter outputStreamWriter_log = null;
    public static BufferedWriter bufferedWriter_log = null;


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
        for (int i = 0; i< codeSplits.length;i++){
            if (!simpleSymbolFlag[i] && structureFlag[i] && !identifierFlag[i]){
                codeFlag[i] = 6; // structure中的非identifier
            }else if (!simpleSymbolFlag[i] && invocationFlag[i] && !identifierFlag[i]){
                codeFlag[i] = 5; // invocation中的非identifier
            }else if (!simpleSymbolFlag[i] && structureFlag[i] && identifierFlag[i]){
                codeFlag[i] = 2; // structure中的identifier
            }else if (!simpleSymbolFlag[i] && invocationFlag[i] && identifierFlag[i]){
                codeFlag[i] = 3; // invocation中的identifier
            }else if (!simpleSymbolFlag[i] && !invocationFlag[i] && !structureFlag[i] && identifierFlag[i]){
                codeFlag[i] = 4; // 其他identifier
            }
        }

        //删除优先级：simple symbols > structure中非identifier >  invocation中非identifier > 非structre和invocation中的identifier >
        // invocation中identifier > structure中的identifier > signature
        String removedCode = "";
        ArrayList<Integer> removedIndex = getRemovedIndex(codeSplits, codeFlag);
        for (int index : removedIndex){
            removedCode += codeSplits[index] + " ";
            codeSplits[index] = "";
        }

        String new_code = String.join(" ",codeSplits);

//        try {
//            if (id < 1000){
//                bufferedWriter_log.write(id + "：" + removedCode + "\n");
//                bufferedWriter_log.write(id + "：" + code+"\n");
//                bufferedWriter_log.write(id + "：" + new_code + "\n\n");
//            }
//
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        return new_code;
//        String new_code =  removeCodeByMark(allLists, code);
//        try {
//            bufferedWriter_log.write("\n" + id + ":" + code+"\n");
//            bufferedWriter_log.write(id + ":" + new_code + "\n\n");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return new_code;
    }

    public static String remove(String code){

        if (code.split(" +").length <= targetLength){
            return code;
        }
        MyVisitor myVisitor = new MyVisitor(code);
        CompilationUnit cu = JavaParser.parse(myVisitor.code);
        myVisitor.visit(cu, null);
//        System.out.println(myVisitor.map);

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
            FileOutputStream fileOutputStream = new FileOutputStream("remove_results/" + stage + "_remove_identifier.txt");



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
//		        String code = codeList[4];
    		    String code = codeList[0];
                int originLength = code.split(" +").length;
                try {
                    code = remove(code).trim();
//                  codeList[4] = code;
                    codeList[0] = code;
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
            System.out.println("totalTime:" + (endTime - startTime));
//	    //关闭
//            if (bufferedWriter_log != null){
//                bufferedWriter_log.close();
//                outputStreamWriter_log.close();
//                fileOutputStream_log.close();
//            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
