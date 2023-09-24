package githubcode.slimcode;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.ast.CompilationUnit;
import combination.SpanContent;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class RemoveInvocation {

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


    public static ArrayList<Integer> getRemovedIndex(String[] codeSplits,boolean[] codeFlag){
        ArrayList<Integer> removeIndex = new ArrayList<>();
            for (int j = codeSplits.length-1;j>=0;j--){
                if (codeFlag[j]){
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

        ArrayList<SpanContent> invocationList = (ArrayList<SpanContent>) map.get("function_invocation");

        String[] codeSplits = code.split(" +");
        boolean[] invocationFlag = new boolean[codeSplits.length];

        int[] codeFlag = new int[codeSplits.length];
        for (SpanContent spanContent : invocationList){
            markFlag(codeFlag,spanContent,-1,code,invocationFlag);
        }

        String removedCode = "";
        ArrayList<Integer> removedIndex = getRemovedIndex(codeSplits, invocationFlag);
        for (int index : removedIndex){
            removedCode += codeSplits[index] + " ";
            codeSplits[index] = "";
        }

        String new_code = String.join(" ",codeSplits);

        return new_code;
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
            String stage = "train";
            List<String> stringList = readFile("code2nl_data/" + stage + "_new_0916.txt");
            FileOutputStream fileOutputStream = new FileOutputStream("removed_results/code2nl_remove_invocation/" + stage + "_remove_invocation.txt");
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
                    code = remove(code);
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
