package my;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import my.graphStructures.GraphNode;
import my.graphStructures.RelationshipEdge;
import org.jgraph.graph.Edge;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import my.pdg.PDGCore;

import java.io.*;
import java.util.*;

public class Test {
    /** The href graph. */
    @SuppressWarnings("rawtypes")
    private DirectedGraph<GraphNode, RelationshipEdge> hrefGraph;
    /** The ast printer. */
    private PDGCore astPrinter = new PDGCore();

    /**
     * Creates the graph.
     */
    private void createGraph() {
        hrefGraph = new DefaultDirectedGraph<>(RelationshipEdge.class);
    }
    public String removeByPDG(String filePath){
        createGraph();
        GraphNode gn = new GraphNode(0, "Entry");
        hrefGraph.addVertex(gn);
        try {
            boolean result = astPrinter.addFile(new FileInputStream(filePath), hrefGraph, gn);
            if (!result){
                return "error";
            }
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }

        ArrayList<String> codeList= new ArrayList<String>();
        try {
            BufferedReader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
            String line;

            while ((line = fileReader.readLine()) != null){
                codeList.add(line);
            }
//            System.out.println(codeList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Set<RelationshipEdge> edges = hrefGraph.edgeSet();
//        System.out.println(edges.size() + "===========");
        Object[] codeLines = codeList.toArray();
        boolean[] codeFlag = new boolean[codeLines.length];

        for(RelationshipEdge edge : edges){
            GraphNode source = (GraphNode) edge.getSource();
            GraphNode target = (GraphNode) edge.getTarget();
            String infoSource = source.getInfo().trim();
            String infoTarget = target.getInfo().trim();
            int idSource = source.getId();
            int idTarget = target.getId();
            if("Entry".equals(infoSource) || infoSource.startsWith("public class") || infoTarget.startsWith("public class")){
                continue;
            }
            if("CD".equals(edge.toString())){
                int indexSource = infoSource.indexOf("\n");
                if (indexSource!=-1){
                    infoSource = infoSource.substring(0,indexSource);
                }

                int indexTarget = infoTarget.indexOf("\n");
                if (indexTarget != -1){
                    infoTarget = infoTarget.substring(0,indexTarget);
                }
//                System.out.println(infoSource + "\n");
//                System.out.println(infoTarget + "\n");
                if(!infoSource.contains(infoTarget) && !infoTarget.contains(infoSource)){
                    codeFlag[idSource-1] = true;
                    codeFlag[idTarget-1] = true;
                }
            }else {
                codeFlag[idSource-1] = true;
                codeFlag[idTarget-1] = true;
            }
        }

        String removedCode = "";
        for (int i=0;i<codeLines.length;i++){
            if (codeFlag[i]){
                removedCode = removedCode + codeLines[i] + "\n";
            }else{
                System.out.println(codeLines[i] + "\n");
            }
        }
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

    public static void writeFile(String code){
        try {
            FileOutputStream fileOutputStream = new FileOutputStream("tmp.txt");
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
            bufferedWriter.write(code);
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void appendStringToFile(String filePath, String text) {
        try (FileWriter fileWriter = new FileWriter(filePath, true)) {
            fileWriter.write(text);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        Test test = new Test();
//        String code = "class HelloWorld{\n" +
//                "private void setupCheckboxBindings() {\n" +
//                "    for (CheckBox checkbox : checkboxes) {\n" +
//                "          checkbox.disableProperty().bind(field.editableProperty().not());\n" +
//                "    }\n" +
//                "}\n" +
//                "}";

        try {
            FileOutputStream fileOutputStream = new FileOutputStream("0917/codesearch/test_remove_PDG.txt");
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);

            List<String> lineList = readFile("0917/codesearch/test_PDG.txt");
            int originCount = lineList.size();
            double avgRemoveRate = 0;
            int count = 0;
            for(String line : lineList){
                String[] splitLines = line.split("<CODESPLIT>");
                if (splitLines.length!=3){
                    System.out.println("!=5");
                    continue;
                }
//                String lineCode = splitLines[4];
                String lineCode = splitLines[0];

                lineCode = lineCode.replace("<CODESEP>","\n");
                String lineCode1 = lineCode.replace("\n","");

                String[] simpleStr = new String[]{"=", "+", "-", "*", "/", "%", "!", ">",  "<", "|", "?", ":", "~", "&", "^", "(",
                        "{", ")", "}", "[", ".", "]", ";", "\"", ",","==","++","--","!=",">=","<=","&&","||","<<",">>",">>>"
                };
                List<String> simpleList = Arrays.asList(simpleStr);
                String tarCode = "";
                for (int i=0;i<lineCode1.length();i++){
                    if (simpleList.contains(String.valueOf(lineCode1.charAt(i)))){
                        tarCode += " " + lineCode1.charAt(i) + " ";
                    }else {
                        tarCode += lineCode1.charAt(i);
                    }
                }
//                lineCode = tarCode;
//                writeFile("public class HelloWorld { \n" + lineCode + " \n }");
//                CompilationUnit cu = null;
//                try {
//                    cu = JavaParser.parse(new FileInputStream("tmp.txt"));
//                    lineCode = cu.toString();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    continue;
//                }



                int originLength = tarCode.replace("\n"," ").split(" +").length;

//                if (line.contains("static <T> void subscribe")){
//                    System.out.println(lineCode);
//                }
                String[] splits = lineCode.split("\n");
                for (int i= 0; i<splits.length;i++){
                    if (splits[i].trim().replace("\t","").startsWith("@")){
                        splits[i] = "";
                    }
                }
                lineCode = String.join("\n",splits);

                writeFile("public class HelloWorld { \n" + lineCode + " \n }");
//                writeFile(lineCode);
                String removeCode = test.removeByPDG("tmp.txt");
                if ("error".equals(removeCode)){
                    continue;
                }
                removeCode = removeCode.replace("\n", " ");

                String tarRemoveCode = "";
                for (int i=0;i<removeCode.length();i++){
                    if (simpleList.contains(String.valueOf(removeCode.charAt(i)))){
                        tarRemoveCode += " " + removeCode.charAt(i) + " ";
                    }else {
                        tarRemoveCode += removeCode.charAt(i);
                    }
                }

                int removedLength = tarRemoveCode.split(" +").length;
                double tmpRate = (originLength - removedLength) * 1.0 / originLength;
                System.out.println(tmpRate);
//                if (tmpRate > 0.5){
//                    System.out.println();
//                }
                avgRemoveRate += tmpRate;
                if ("".equals(removeCode)){
                    appendStringToFile("error.txt",lineCode + "\n");
//                    System.out.println("error");
                    System.out.println(lineCode);
//                    System.exit(0);
//                    continue;
                    removeCode = lineCode.replace("\n","").replace("\r\n","");
                }
//                System.out.println(111111);
//                splitLines[4] = removeCode;
                splitLines[0] = removeCode;
                count++;
//                System.out.println(removeCode);
                String newLine = String.join("<CODESPLIT>", splitLines);
                bufferedWriter.write(newLine + "\n");
            }
            bufferedWriter.close();
            System.out.println(count + "/" + originCount);
            System.out.println(avgRemoveRate / count);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e);
        }

//        System.out.println(removeCode);
    }
}
