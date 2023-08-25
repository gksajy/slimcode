package code2nl;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import combination.SpanContent;
import functionstructure.RemoveFunctionStructure;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


class RemoveStructureVisiter extends VoidVisitorAdapter<Void> {

    public List<String> removeList = new ArrayList<String>();
    public String methodCode = "";
    public String code = "";
    public RemoveStructureVisiter(String code){
        this.methodCode = code;
        this.code = "public class HelloWorld { " + this.methodCode.trim() + " }";
    }

//    private int getEndIndex(int startIndex,String nString){
//        int leftBracket = 1;
//        int startMark = 0;
//        for(int i=startIndex+1; i<nString.length();i++){
//            if(nString.charAt(i) == '(' && startMark % 2 == 0){
//                leftBracket ++;
//            }else if (nString.charAt(i) == ')' && startMark % 2 == 0){
//                leftBracket--;
//            }else if (nString.charAt(i) == '\"'){
//                startMark ++;
//            }
//            if (leftBracket==0){
//                return i;
//            }
//        }
//        return -1;
//    }
    /**
     * 去掉catch中的关键词
     * @param n
     * @param arg
     */
    @Override
    public void visit(TryStmt n, Void arg) {
        super.visit(n, arg);
        NodeList<CatchClause> catchClauses = n.getCatchClauses();
        for(CatchClause catchClause : catchClauses){
//            int start = catchClause.getBegin().get().column;
//            int end = catchClause.getEnd().get().column;
//            System.out.println(catchClause.toString());
//            String oldStr = MyClassVisitor.code.substring(start,end);
            String oldStr = catchClause.toString();
            oldStr = oldStr.substring(oldStr.indexOf("(")+1,oldStr.indexOf(")"));
            removeList.add(oldStr);
//            System.out.println(oldStr);
        }

    }

//    private String getRemoveString(int startIndex,int endIndex){
//        String nString = this.methodCode.substring(startIndex - 26 -1,endIndex-26);
//        int subStartIndex = nString.indexOf('(');
//        int subEndIndex = getEndIndex(subStartIndex,nString);
//        String removeString = "";
//        try {
//            removeString = nString.substring(subStartIndex+1,subEndIndex);
//        } catch (Exception e){
//            System.out.println(nString);
//        }
//        return removeString;
//    }

    private String getRemoveString(int startIndex,int endIndex){
        String nString = this.methodCode.substring(startIndex - 26 -1,endIndex-26);
        int subStartIndex = nString.indexOf('(');
//        int subStartIndex = 0;
        int subEndIndex = getEndIndex(subStartIndex,nString)-2;
        String removeString = "";
        try {
            removeString = this.methodCode.substring(startIndex + subStartIndex + 2 - 26 - 1,startIndex + subEndIndex - 26);
        } catch (Exception e){
            System.out.println(nString);
        }

        return removeString;
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

    /**
     * 去掉if节点的括号内的判断条件
     * @param n
     * @param arg
     */
    @Override
    public void visit(IfStmt n, Void arg) {
        super.visit(n, arg);
//        String nString = n.toString().split("\r\n")[0];
        int startIndex = n.getBegin().get().column;
        int endIndex = n.getEnd().get().column;
        String oldString = getRemoveString(startIndex,endIndex);
//        String oldStr = nString.substring(0,nString.indexOf(")")+2);
        removeList.add(oldString);
    }
    @Override
    public void visit(SwitchStmt n, Void arg) {
        super.visit(n, arg);
        String selector = n.getSelector().toString();
        removeList.add(selector);
        NodeList<SwitchEntryStmt> entries = n.getEntries();
        for(SwitchEntryStmt entryStmt : entries){
            String entryStr = entryStmt.toString();
            entryStr = entryStr.substring(0,entryStr.indexOf(":"));
            removeList.add(entryStr);
        }
    }
    @Override
    public void visit(WhileStmt n, Void arg) {
        super.visit(n, arg);
//        String nString = n.getCondition().toString();
        int startIndex = n.getBegin().get().column;
        int endIndex = n.getEnd().get().column;
        String oldString = getRemoveString(startIndex,endIndex);
        removeList.add(oldString);
    }
    @Override
    public void visit(DoStmt n, Void arg) {
        super.visit(n, arg);
//        String nString = n.getCondition().toString();
        int startIndex = n.getBegin().get().column;
        int endIndex = n.getEnd().get().column;
        String nString = this.methodCode.substring(startIndex - 26 - 1,endIndex - 26);
        int whileIndex = nString.lastIndexOf("while");

        nString = nString.substring(whileIndex);
        int subStartIndex = nString.indexOf('(');
        int subEndIndex = getEndIndex(subStartIndex,nString);
        String removeString = nString.substring(subStartIndex+1,subEndIndex);
        removeList.add(removeString);
    }
    @Override
    public void visit(ForStmt n, Void arg) {
        super.visit(n, arg);
//        NodeList<Expression> initialization = n.getInitialization();
//        Optional<Expression> compare = n.getCompare();
//        NodeList<Expression> update = n.getUpdate();
//        String oldStr = n.toString().split("\n")[0];
//        System.out.println(oldStr);
//        if(update.size()!=0){
//            String updateLastStr = update.get(update.size()-1).toString();
//            oldStr = n.toString();
//            oldStr = oldStr.substring(oldStr.indexOf("(")+1,oldStr.indexOf(updateLastStr)+updateLastStr.length());
//        }else {
//            oldStr = oldStr.substring(oldStr.indexOf("(")+1,Math.max(oldStr.indexOf(")"),oldStr.indexOf("{")));
//        }
        int startIndex = n.getBegin().get().column;
        int endIndex = n.getEnd().get().column;
        String oldString = getRemoveString(startIndex,endIndex);
        removeList.add(oldString);
//        String nString = n.toString();
//        String initialStr = "";
//        for (Expression e : initialization){
//            initialStr = e.toString();
//        }
//        int startIndex = nString.indexOf(initialStr);
//        String updateStr = "";
//        for(Expression e : update){
//            updateStr = e.toString();
//        }
//        int endIndex = nString.indexOf(updateStr) + updateStr.length();
//        String oldStr = nString.substring(startIndex,endIndex);
//        removeList.add(initialization+" ;");
//        removeList.add(initialization+" ;");
//        for(Expression e : update){
//            String updateStr = e.toString();
//            removeList.add(updateStr);
//        }
    }

    @Override
    public void visit(ForeachStmt n, Void arg) {
        super.visit(n, arg);
//        NodeList<Expression> initialization = n.getInitialization();
//        Optional<Expression> compare = n.getCompare();
//        NodeList<Expression> update = n.getUpdate();
//        String oldStr = n.toString().split("\n")[0];
//        System.out.println(oldStr);
//        if(update.size()!=0){
//            String updateLastStr = update.get(update.size()-1).toString();
//            oldStr = n.toString();
//            oldStr = oldStr.substring(oldStr.indexOf("(")+1,oldStr.indexOf(updateLastStr)+updateLastStr.length());
//        }else {
//            oldStr = oldStr.substring(oldStr.indexOf("(")+1,Math.max(oldStr.indexOf(")"),oldStr.indexOf("{")));
//        }
        int startIndex = n.getBegin().get().column;
        int endIndex = n.getEnd().get().column;
        String oldString = getRemoveString(startIndex,endIndex);
        removeList.add(oldString);
//        String nString = n.toString();
//        String initialStr = "";
//        for (Expression e : initialization){
//            initialStr = e.toString();
//        }
//        int startIndex = nString.indexOf(initialStr);
//        String updateStr = "";
//        for(Expression e : update){
//            updateStr = e.toString();
//        }
//        int endIndex = nString.indexOf(updateStr) + updateStr.length();
//        String oldStr = nString.substring(startIndex,endIndex);
//        removeList.add(initialization+" ;");
//        removeList.add(initialization+" ;");
//        for(Expression e : update){
//            String updateStr = e.toString();
//            removeList.add(updateStr);
//        }
    }

//    @Override
//    public void visit(MethodCallExpr n, Void arg) {
//        super.visit(n, arg);
//        System.out.println(n);
//    }
    private void removeStructKey(){
        String[] removeList = new String[]{"if","else","try","try","catch","finally","for","while","do","switch","case"};
        Collections.addAll(this.removeList,removeList);
    }

    public void remove(){
        removeStructKey();
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
                this.methodCode = this.methodCode.replace(" "+ s.trim() + " "," ");
            }

        }
    }



}


public class RemoveStructure {


    public String remove(String code){
//        System.out.print(code.split(" +").length+"-");
        // 解析Java源代码生成AST
        RemoveStructureVisiter visitor = new RemoveStructureVisiter(code);
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



//    private List<String> getStaticTokens(){
//        List<String> strings = null;
//        try {
//            strings = readFile("static_tokens.txt");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return strings;
//    }
//    private int getCommenIndex(String[] strings){
//        for(int i=0;i<strings.length;i++){
//            if("//".equals(strings[i]) || strings[i].startsWith("//")){
//                return i;
//            }
//        }
//        return strings.length-1;
//    }

//    public String removeCommon(String code){
//        String[] strings = code.split(" +");
//        List<String> staticTokens = getStaticTokens();
//        int commenIndex = getCommenIndex(strings);
//        int staticIndex = strings.length - 1;
//        for(int i=commenIndex+1;i<strings.length;i++){
//            if (staticTokens.contains(strings[i])){
//                staticIndex = i;
//                break;
//            }
//        }
//        String removeCommons = "";
//        for(int i=0;i<commenIndex;i++){
//            removeCommons = removeCommons + strings[i] + " ";
//        }
//        for(int i=staticIndex;i<strings.length;i++){
//            removeCommons = removeCommons +  strings[i] + " ";
//        }
//        return removeCommons;
//    }
    public static void main(String[] args) throws Exception {
//        String code = "public ViewDocumentRequestBuilder addHighlightExpressions ( final String highlightExpression , final String ... highlightExpressions ) { this . highlightExpressions . add ( highlightExpression ) ; this . highlightExpressions . addAll ( Arrays . asList ( highlightExpressions ) ) ; return this ; } ";
//        String code = "public String toXmlString ( ) { final StringBuilder buffer = new StringBuilder ( ) ; buffer . append ( \"<\" ) ; buffer . append ( getName ( ) ) ; Optional < String > attr = TagExtensions . attributesToString ( getAttributes ( ) ) ; if ( attr . isPresent ( ) ) { buffer . append ( attr . get ( ) ) ; } if ( isEndTag ( ) ) { buffer . append ( \">\" ) ; if ( getChildren ( ) != null && ! getChildren ( ) . isEmpty ( ) ) { String processingContent = getContent ( ) ; Integer lastPosition = 0 ; for ( final ChildTagPosition child : getChildren ( ) ) { final String subContent = getContent ( ) . substring ( lastPosition , child . getPosition ( ) ) ; lastPosition = child . getPosition ( ) ; processingContent = processingContent . substring ( lastPosition , processingContent . length ( ) ) ; buffer . append ( subContent ) ; buffer . append ( child . getChild ( ) . toXmlString ( ) ) ; } buffer . append ( processingContent ) ; } else { buffer . append ( getContent ( ) ) ; } buffer . append ( \"</\" ) ; buffer . append ( getName ( ) ) ; buffer . append ( \">\" ) ; } else { buffer . append ( \"/>\" ) ; } return buffer . toString ( ) ; }\n ";
        int count = 0;
        List<String> urlList = new ArrayList<>();
        RemoveStructure rn = new RemoveStructure();
        List<String> stringList = rn.readFile("./0822/codesearch/code_search_data_base.txt");
        FileOutputStream fileOutputStream = new FileOutputStream("./0822/codesearch/code_search_remove_structure.txt");
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
        BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
        double allRemovePercent = 0;
        for(int i=0; i < stringList.size();i++){
            String lineStr = stringList.get(i);
            String[] codeList = lineStr.split("<CODESPLIT>");
            String code = codeList[4];
//            String code = codeList[0];
//            String funName = codeList[2];

//            while (code.contains("//") && !code.contains("http://") && !code.contains("//%s") &&!code.contains("://") && !code.contains("//+") && !code.contains("manifest//") && !code.contains("\"//\"") && !code.contains("\"// \"") &&!code.contains("\"// ") &&!code.contains("//manifest") &&!code.contains("mongodb+srv//")){
//                code = rn.removeCommon(code);
////                System.out.println(code);
//            }
//            if (code.startsWith("@")){
//                code = code.substring(code.indexOf(")")+1);
//            }

//                code = code.replace(code.substring(code.indexOf("//"),code.indexOf(";")),"");
//            System.out.println("origin:"+code.split(" +").length);
            int originLength = code.split(" +").length;
            try {

                code = rn.remove(code);
                codeList[4] = code;
//                codeList[0] = code;
                int removeLength =code.split(" +").length;
                double removePercent = (originLength - removeLength) * 1.0 / originLength;
                allRemovePercent += removePercent;
                System.out.println("remove percent:"+removePercent);
                String newLine = String.join("<CODESPLIT>", codeList).replace("\n","");
                bufferedWriter.write(newLine + "\n");
                count++;


            }catch (ParseProblemException e){

                System.out.println(code);
                System.out.println(e);
//                System.out.println(code);
//                System.out.println(e);
            }
//            System.out.println("cut:"+code.split(" +").length);
        }
        bufferedWriter.close();
        outputStreamWriter.close();
        System.out.println(count+"/"+stringList.size());
        System.out.println("共有"+count+"条数据");
        System.out.println("all remove percent:"+allRemovePercent/count);
//        String code = rn.removeCommon("public void addServiceIfNotPresent ( WiFiP2pService service ) { WfdLog . d ( TAG , \"addServiceIfNotPresent BEGIN, with size = \" + serviceList . size ( ) ) ; if ( service == null ) { WfdLog . e ( TAG , \"Service is null, returning...\" ) ; return ; } boolean add = true ; for ( WiFiP2pService element : serviceList ) { if ( element != null && element . getDevice ( ) . equals ( service . getDevice ( ) ) && element . getInstanceName ( ) . equals ( service . getInstanceName ( ) ) ) { add = false ; //already in the list } } if ( add ) { serviceList . add ( service ) ; } WfdLog . d ( TAG , \"addServiceIfNotPresent END, with size = \" + serviceList . size ( ) ) ; } \n");
//        RemoveName rn = new RemoveName();
//        String code = rn.remove(" public void validate ( Object response ) throws RedditParseException {  if ( response == null ) { throw new RedditParseException ( ) ; }  if ( ! ( response instanceof JSONObject ) ) { throw new RedditParseException ( \"not a JSON response\" ) ; } jsonResponse = ( ( JSONObject ) response ) ;  if ( jsonResponse . get ( \"error\" ) != null ) { throw new RedditParseException ( JsonUtils . safeJsonToInteger ( jsonResponse . get ( \"error\" ) ) ) ; }  if ( jsonResponse . get ( \"data\" ) == null && jsonResponse . get ( \"json\" ) == null ) { throw new RedditParseException ( \"data is missing from listing\" ) ; } }\n ");
    }
//    public static void main(String[] args) {
//        String code = "private Map < String , Set < String > > getDependencies ( Pattern pattern ) { Map < String , Set < String > > result = new HashMap < String , Set < String > > ( ) ; for ( PatternNode node : pattern . aliasToNode . values ( ) ) { Set < String > currentDependencies = new HashSet < String > ( ) ; OWhereClause filter = aliasFilters . get ( node . alias ) ; if ( filter != null && filter . getBaseExpression ( ) != null ) { List < String > involvedAliases = filter . getBaseExpression ( ) . getMatchPatternInvolvedAliases ( ) ; if ( involvedAliases != null ) { currentDependencies . addAll ( involvedAliases ) ; } } result . put ( node . alias , currentDependencies ) ; } return result ; }\n";
//        RemoveStructure rn = new RemoveStructure();
//        String remove = rn.remove(code);
//        System.out.println(remove);
//    }

}
