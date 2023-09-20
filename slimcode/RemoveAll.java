package githubcode.slimcode;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class MyVisitor extends VoidVisitorAdapter<Void> {

    public Map map = new HashMap();
    public String methodCode = "";
    public String code = "";
    final private int STARTINDEX = 26;

    public MyVisitor(String code) {
        this.methodCode = code;
        this.code = "public class HelloWorld { " + this.methodCode.trim() + " }";
        this.map.put("function_invocation",new ArrayList<SpanContent>());
        this.map.put("identifiers",new ArrayList<SpanContent>());
        this.map.put("function_structure",new ArrayList<SpanContent>());
        this.map.put("method_signature",new ArrayList<SpanContent>());

        addStructKeyWords();
    }

    private void addStructKeyWords(){
        String[] removeList = new String[]{"if","else","try","catch","finally","for","while","do","switch","case"};
        ArrayList<SpanContent> functionStructureList = (ArrayList<SpanContent>) this.map.get("function_structure");
        for (String s : removeList){
            int index = 0;
            while (index != -1){
                index = this.methodCode.indexOf(" " + s + " ",index);
                if(index != -1){
                    String removeStr = this.methodCode.substring(index + 1,index+1+s.length());
                    SpanContent spanContent = new SpanContent(index+1,index+1+s.length(),methodCode);
                    functionStructureList.add(spanContent);
                    index += s.length();
                }else {
                    break;
                }
            }
        }
        this.map.put("function_structure",functionStructureList);
    }

    //function invocation
    @Override
    public void visit(MethodCallExpr n, Void arg) {
        super.visit(n, arg);
        int startIndex = n.getBegin().get().column;
        int endIndex = n.getEnd().get().column;
        String removeStr = this.methodCode.substring(startIndex - STARTINDEX - 1, endIndex - STARTINDEX);
        SpanContent spanContent = new SpanContent(startIndex - STARTINDEX -1, endIndex - STARTINDEX,methodCode);
        ArrayList<SpanContent> invocationList = (ArrayList<SpanContent>) this.map.get("function_invocation");
        invocationList.add(spanContent);
        this.map.put("function_invocation",invocationList);
    }

    // identifiers
    @Override
    public void visit(NameExpr n, Void arg) {
        super.visit(n, arg);
        int startIndex = n.getBegin().get().column;
        int endIndex = n.getEnd().get().column;
        String removeStr = this.methodCode.substring(startIndex - STARTINDEX - 1, endIndex - STARTINDEX);
        SpanContent spanContent = new SpanContent(startIndex - STARTINDEX -1, endIndex - STARTINDEX,methodCode);
        ArrayList<SpanContent> identifiersList = (ArrayList<SpanContent>) this.map.get("identifiers");
        identifiersList.add(spanContent);
        this.map.put("identifiers",identifiersList);
    }

    @Override
    public void visit(VariableDeclarationExpr n, Void arg) {
        super.visit(n, arg);
        ArrayList<SpanContent> identifiersList = (ArrayList<SpanContent>) this.map.get("identifiers");
        NodeList<VariableDeclarator> variables = n.getVariables();
        for (VariableDeclarator variableDeclarator : variables){
            int startIndex = variableDeclarator.getName().getBegin().get().column;
            int endIndex = variableDeclarator.getName().getEnd().get().column;
            String removeStr = this.methodCode.substring(startIndex - STARTINDEX - 1, endIndex - STARTINDEX);
            SpanContent spanContent = new SpanContent(startIndex - STARTINDEX -1, endIndex - STARTINDEX,methodCode);
            identifiersList.add(spanContent);
        }
        this.map.put("identifiers",identifiersList);

    }

    // function structure - catch
    @Override
    public void visit(TryStmt n, Void arg) {
        super.visit(n, arg);
        NodeList<CatchClause> catchClauses = n.getCatchClauses();
        for(CatchClause catchClause : catchClauses){
            int startIndex = catchClause.getBegin().get().column;
            int endIndex = catchClause.getEnd().get().column;
            String removeStr = this.methodCode.substring(startIndex - STARTINDEX - 1, endIndex - STARTINDEX);
            int innerStart = removeStr.indexOf('(');
            int innerEnd = removeStr.indexOf(')');
            removeStr = this.methodCode.substring(startIndex + innerStart - STARTINDEX - 1, startIndex + innerEnd - STARTINDEX);
            SpanContent spanContent = new SpanContent(startIndex + innerStart - STARTINDEX - 1, startIndex + innerEnd - STARTINDEX,methodCode);
            ArrayList<SpanContent> functionStructureList = (ArrayList<SpanContent>) this.map.get("function_structure");
            functionStructureList.add(spanContent);
            this.map.put("function_structure",functionStructureList);
        }

    }

    private void getRemoveString(int startIndex,int endIndex){
        String nString = this.methodCode.substring(startIndex - 26 -1,endIndex-26);
        int subStartIndex = nString.indexOf('(');
        int subEndIndex = getEndIndex(subStartIndex,nString)-2;
        String removeString = "";
        try {
            removeString = this.methodCode.substring(startIndex + subStartIndex + 2 - STARTINDEX - 1,startIndex + subEndIndex - STARTINDEX);
            SpanContent spanContent = new SpanContent(startIndex + subStartIndex + 2 - STARTINDEX - 1,startIndex + subEndIndex - STARTINDEX,methodCode);
            ArrayList<SpanContent> functionStructureList = (ArrayList<SpanContent>) this.map.get("function_structure");
            functionStructureList.add(spanContent);
            this.map.put("function_structure",functionStructureList);
        } catch (Exception e){
            System.out.println(nString);
        }

    }
    private int getEndIndex(int startIndex,String nString){
        int leftBracket = 1;
        int startMark = 0;
        for(int i=startIndex+1; i<nString.length();i++){

            if(nString.charAt(i) == '(' && (nString.charAt(i-1) != '\'' || nString.charAt(i+1)!='\'') && startMark % 2 == 0 ){
                leftBracket ++;
            }else if (nString.charAt(i) == ')' && startMark % 2 == 0 ){
                leftBracket--;
            }else if (nString.charAt(i) == '\"' && (nString.charAt(i-1) !='\'' || nString.charAt(i+1)!='\'')){
                int count = 0, index = i - 1;
                while (nString.charAt(index) == '\\'){
                    index--;
                    count++;
                }
                if (count % 2 == 0)
                    startMark ++;
            }
            if (leftBracket==0){
                return i;
            }
        }
        return -1;
    }
    //function structure - if
    @Override
    public void visit(IfStmt n, Void arg) {
        super.visit(n, arg);
        int startIndex = n.getBegin().get().column;
        int endIndex = n.getEnd().get().column;
        getRemoveString(startIndex,endIndex);
    }
    //function-structure - switch
    @Override
    public void visit(SwitchStmt n, Void arg) {
        super.visit(n, arg);
        String selector = n.getSelector().toString();
        int startIndex1 = n.getBegin().get().column;
        int endIndex1 = n.getEnd().get().column;

        getRemoveString(startIndex1,endIndex1);

        NodeList<SwitchEntryStmt> entries = n.getEntries();
        for(SwitchEntryStmt entryStmt : entries){
            int startIndex2 = entryStmt.getBegin().get().column;
            int endIndex2 = entryStmt.getEnd().get().column;
            String innerStr = this.methodCode.substring(startIndex2 - STARTINDEX - 1, endIndex2 - STARTINDEX);
            int innerIndex = innerStr.indexOf(":");
            String removeStr = this.methodCode.substring(startIndex2 - STARTINDEX - 1, startIndex2 + innerIndex - STARTINDEX - 1);
            SpanContent spanContent = new SpanContent(startIndex2 - STARTINDEX - 1, startIndex2 + innerIndex - STARTINDEX - 1,methodCode);
            ArrayList<SpanContent> functionStructureList = (ArrayList<SpanContent>) this.map.get("function_structure");
            functionStructureList.add(spanContent);
            this.map.put("function_structure",functionStructureList);
        }
    }
    // function structure - while
    @Override
    public void visit(WhileStmt n, Void arg) {
        super.visit(n, arg);
//        String nString = n.getCondition().toString();
        int startIndex = n.getBegin().get().column;
        int endIndex = n.getEnd().get().column;
        getRemoveString(startIndex,endIndex);

    }
    // function structure - do while
    @Override
    public void visit(DoStmt n, Void arg) {
        super.visit(n, arg);
        int startIndex = n.getBegin().get().column;
        int endIndex = n.getEnd().get().column;
        String nString = this.methodCode.substring(startIndex - 26 - 1,endIndex - 26);
        int whileIndex = nString.lastIndexOf("while");
        getRemoveString(startIndex + whileIndex,endIndex);
    }
    // function structure - for
    @Override
    public void visit(ForStmt n, Void arg) {
        super.visit(n, arg);
        int startIndex = n.getBegin().get().column;
        int endIndex = n.getEnd().get().column;
        getRemoveString(startIndex,endIndex);
    }

    // function structure - for
    @Override
    public void visit(ForeachStmt n, Void arg) {
        super.visit(n, arg);
        int startIndex = n.getBegin().get().column;
        int endIndex = n.getEnd().get().column;
        getRemoveString(startIndex,endIndex);
    }
    // method - signature
    @Override
    public void visit(MethodDeclaration n, Void arg) {
        super.visit(n, arg);
        int startIndex = n.getBegin().get().column;
        int endIndex = n.getEnd().get().column;
        String removeStr = this.methodCode.substring(startIndex - STARTINDEX - 1, endIndex - STARTINDEX);
        int leftBracket = removeStr.indexOf("{");
        removeStr = this.methodCode.substring(0,startIndex+leftBracket-STARTINDEX);
        SpanContent spanContent = new SpanContent(0,startIndex+leftBracket-STARTINDEX,methodCode);
        ArrayList<SpanContent> methodSignatureList = (ArrayList<SpanContent>) this.map.get("method_signature");
        methodSignatureList.add(spanContent);
        this.map.put("method_signature",methodSignatureList);
//        System.out.println(removeStr);
    }

}


public class RemoveAll {
    public String remove(String code) {
        // 解析Java源代码生成AST
        MyVisitor visitor = new MyVisitor(code);
        CompilationUnit cu = JavaParser.parse(visitor.code);
        visitor.visit(cu, null);
//        System.out.println(visitor.map);
        return visitor.methodCode;
    }
    public List<String> readFile(String path) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(path);
        InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        List<String> stringsList = new ArrayList<String>();
        String stringTmp = "";
        while ((stringTmp = bufferedReader.readLine()) != null) {
            stringsList.add(stringTmp);
        }
        inputStreamReader.close();
        return stringsList;

    }


}


