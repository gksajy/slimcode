package functionstructure;

import code2nl.RemoveStructure;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


class RemoveFunctionStructureVisiter extends VoidVisitorAdapter<Void> {

    public List<String> removeList = new ArrayList<String>();
    public String methodCode = "";
    public String code = "";
    public RemoveFunctionStructureVisiter(String code){
        this.methodCode = code;
        this.code = "public class HelloWorld { " + this.methodCode.trim() + " }";
    }


//    @Override
//    public void visit(ReturnStmt n, Void arg) {
//        super.visit(n, arg);
//        int startIndex = n.getBegin().get().column;
//        int endIndex = n.getEnd().get().column;
//        String removeStr = this.methodCode.substring(startIndex - 26 - 1, endIndex - 26);
//        this.removeList.add(removeStr);
//    }




    public void remove(){
//        removeStructKey();

        int endIndex = this.methodCode.indexOf('{');
        String removeStr = this.methodCode.substring(0,endIndex);
        this.removeList.add(removeStr);

        this.removeList.sort(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                if (o1.length() > o2.length()){
                    return -1;
                }else if (o1.length() == o2.length()){
                    return 0;
                }else {
                    return 1;
                }
            }
        });
        for(String s : this.removeList){
            if(this.methodCode.contains(s)){
                this.methodCode = this.methodCode.replace(s.trim() + " ","");
            }

        }
    }



}


public class RemoveFunctionStructure {


    public String remove(String code){
//        System.out.print(code.split(" +").length+"-");
        // 解析Java源代码生成AST
        RemoveFunctionStructureVisiter visitor = new RemoveFunctionStructureVisiter(code);
//        System.out.println(visitor.code);
        CompilationUnit cu = JavaParser.parse(visitor.code);
        visitor.visit(cu, null);
        visitor.remove();
        return visitor.methodCode;
//        System.out.println(visitor.methodCode.split(" +").length);
    }

    public List<String> readFile(String path) throws IOException {
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


//    public static void main(String[] args) throws Exception {
////        String code = "public ViewDocumentRequestBuilder addHighlightExpressions ( final String highlightExpression , final String ... highlightExpressions ) { this . highlightExpressions . add ( highlightExpression ) ; this . highlightExpressions . addAll ( Arrays . asList ( highlightExpressions ) ) ; return this ; } ";
////        String code = "public String toXmlString ( ) { final StringBuilder buffer = new StringBuilder ( ) ; buffer . append ( \"<\" ) ; buffer . append ( getName ( ) ) ; Optional < String > attr = TagExtensions . attributesToString ( getAttributes ( ) ) ; if ( attr . isPresent ( ) ) { buffer . append ( attr . get ( ) ) ; } if ( isEndTag ( ) ) { buffer . append ( \">\" ) ; if ( getChildren ( ) != null && ! getChildren ( ) . isEmpty ( ) ) { String processingContent = getContent ( ) ; Integer lastPosition = 0 ; for ( final ChildTagPosition child : getChildren ( ) ) { final String subContent = getContent ( ) . substring ( lastPosition , child . getPosition ( ) ) ; lastPosition = child . getPosition ( ) ; processingContent = processingContent . substring ( lastPosition , processingContent . length ( ) ) ; buffer . append ( subContent ) ; buffer . append ( child . getChild ( ) . toXmlString ( ) ) ; } buffer . append ( processingContent ) ; } else { buffer . append ( getContent ( ) ) ; } buffer . append ( \"</\" ) ; buffer . append ( getName ( ) ) ; buffer . append ( \">\" ) ; } else { buffer . append ( \"/>\" ) ; } return buffer . toString ( ) ; }\n ";
//        int count = 0;
//        List<String> urlList = new ArrayList<>();
//        RemoveFunctionStructure rn = new RemoveFunctionStructure();
//        List<String> stringList = rn.readFile("./0822/codesearch/code_search_data_base.txt");
//        FileOutputStream fileOutputStream = new FileOutputStream("./0822/codesearch/codesearch_remove_signature.txt");
//        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
//        BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
//        double allRemovePercent = 0, allCount = 0, allRemovedCount = 0;
//        for(int i=0; i < stringList.size();i++){
//            String lineStr = stringList.get(i);
//            String[] codeList = lineStr.split("<CODESPLIT>");
//            String code = codeList[4];
////            String code = codeList[0];
////            String funName = codeList[2];
//
////            while (code.contains("//") && !code.contains("http://") && !code.contains("//%s") &&!code.contains("://") && !code.contains("//+") && !code.contains("manifest//") && !code.contains("\"//\"") && !code.contains("\"// \"") &&!code.contains("\"// ") &&!code.contains("//manifest") &&!code.contains("mongodb+srv//")){
////                code = rn.removeCommon(code);
//////                System.out.println(code);
////            }
////            if (code.startsWith("@")){
////                code = code.substring(code.indexOf(")")+1);
////            }
//
////                code = code.replace(code.substring(code.indexOf("//"),code.indexOf(";")),"");
////            System.out.println("origin:"+code.split(" +").length);
//            int originLength = code.split(" +").length;
//            try {
//
//                code = rn.remove(code);
//                codeList[4] = code;
////                codeList[0] = code;
//                int removeLength =code.split(" +").length;
//                allCount += originLength;
//                allRemovedCount += removeLength;
////                double removePercent = (originLength - removeLength) * 1.0 / originLength;
////                allRemovePercent += removePercent;
////                System.out.println("remove percent:"+removePercent);
//                String newLine = String.join("<CODESPLIT>", codeList).replace("\n","");
//                bufferedWriter.write(newLine + "\n");
//                count++;
//
//
//            }catch (ParseProblemException e){
//
////                System.out.println(code);
////                System.out.println(e);
////                System.out.println(code);
////                System.out.println(e);
//            }
////            System.out.println("cut:"+code.split(" +").length);
//        }
//        bufferedWriter.flush();
//        bufferedWriter.close();
//        outputStreamWriter.close();
//        System.out.println(count+"/"+stringList.size());
//        System.out.println("共有"+count+"条数据");
////        System.out.println("all remove percent:"+allRemovePercent/count);
//        System.out.println("all remove percent:"+ (1 - allRemovedCount/allCount));
////        String code = rn.removeCommon("public void addServiceIfNotPresent ( WiFiP2pService service ) { WfdLog . d ( TAG , \"addServiceIfNotPresent BEGIN, with size = \" + serviceList . size ( ) ) ; if ( service == null ) { WfdLog . e ( TAG , \"Service is null, returning...\" ) ; return ; } boolean add = true ; for ( WiFiP2pService element : serviceList ) { if ( element != null && element . getDevice ( ) . equals ( service . getDevice ( ) ) && element . getInstanceName ( ) . equals ( service . getInstanceName ( ) ) ) { add = false ; //already in the list } } if ( add ) { serviceList . add ( service ) ; } WfdLog . d ( TAG , \"addServiceIfNotPresent END, with size = \" + serviceList . size ( ) ) ; } \n");
////        RemoveName rn = new RemoveName();
////        String code = rn.remove(" public void validate ( Object response ) throws RedditParseException {  if ( response == null ) { throw new RedditParseException ( ) ; }  if ( ! ( response instanceof JSONObject ) ) { throw new RedditParseException ( \"not a JSON response\" ) ; } jsonResponse = ( ( JSONObject ) response ) ;  if ( jsonResponse . get ( \"error\" ) != null ) { throw new RedditParseException ( JsonUtils . safeJsonToInteger ( jsonResponse . get ( \"error\" ) ) ) ; }  if ( jsonResponse . get ( \"data\" ) == null && jsonResponse . get ( \"json\" ) == null ) { throw new RedditParseException ( \"data is missing from listing\" ) ; } }\n ");
//    }


}
