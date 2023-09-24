package githubcode.slimcode;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.ast.CompilationUnit;
import combination.SpanContent;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class RemoveIdentifier {

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



    public static String removeCode(String code, Map map,int targetLength){
        ArrayList<SpanContent> identifierList = (ArrayList<SpanContent>) map.get("identifiers");

        String[] codeSplits = code.split(" +");
        boolean[] identifierFlag = new boolean[codeSplits.length];

        int[] codeFlag = new int[codeSplits.length];

        for (SpanContent spanContent : identifierList){
            markFlag(codeFlag,spanContent,-1,code,identifierFlag);
        }

        String removedCode = "";
        ArrayList<Integer> removedIndex = getRemovedIndex(codeSplits, identifierFlag);
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
//            String stage = "test";
//            List<String> stringList = readFile("data/" + stage + "_no_common.txt");
//            FileOutputStream fileOutputStream = new FileOutputStream("remove_results/" + stage + "_remove_identifier.txt");

            String stage = "valid";
            List<String> stringList = readFile("code2nl_data/" + stage + "_new_0916.txt");
            FileOutputStream fileOutputStream = new FileOutputStream("removed_results/" + stage + "_remove_identifier.txt");



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

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
