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

//    public List<String> removeList = new ArrayList<String>();
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
//                    System.out.println();
                    String removeStr = this.methodCode.substring(index + 1,index+1+s.length());
                    SpanContent spanContent = new SpanContent(index+1,index+1+s.length(),methodCode);
                    functionStructureList.add(spanContent);
                    index += s.length();
                }else {
                    break;
                }
            }
//            functionStructureList.add(spanContent);
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
//        this.removeList.add(removeStr);
        SpanContent spanContent = new SpanContent(startIndex - STARTINDEX -1, endIndex - STARTINDEX,methodCode);
        ArrayList<SpanContent> invocationList = (ArrayList<SpanContent>) this.map.get("function_invocation");
        invocationList.add(spanContent);
        this.map.put("function_invocation",invocationList);
    }

    // identifiers
    @Override
    public void visit(NameExpr n, Void arg) {
        super.visit(n, arg);
//        System.out.println(n);
//        removeList.add(n.toString());
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
////        System.out.println(n);
////        removeList.add(n.toString());
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

//
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
//            int innerStart = 0;
            int innerEnd = removeStr.indexOf(')');
            removeStr = this.methodCode.substring(startIndex + innerStart - STARTINDEX - 1, startIndex + innerEnd - STARTINDEX);
            SpanContent spanContent = new SpanContent(startIndex + innerStart - STARTINDEX - 1, startIndex + innerEnd - STARTINDEX,methodCode);
//            System.out.println(spanContent);
            ArrayList<SpanContent> functionStructureList = (ArrayList<SpanContent>) this.map.get("function_structure");
            functionStructureList.add(spanContent);
            this.map.put("function_structure",functionStructureList);
//            String oldStr = MyClassVisitor.code.substring(start,end);
//            String oldStr = catchClause.toString();
//            oldStr = oldStr.substring(oldStr.indexOf("(")+1,oldStr.indexOf(")"));
//            removeList.add(oldStr);
//            System.out.println(oldStr);
        }

    }

    private void getRemoveString(int startIndex,int endIndex){
        String nString = this.methodCode.substring(startIndex - 26 -1,endIndex-26);
        int subStartIndex = nString.indexOf('(');
//        int subStartIndex = 0;
        int subEndIndex = getEndIndex(subStartIndex,nString)-2;
        String removeString = "";
        try {
            removeString = this.methodCode.substring(startIndex + subStartIndex + 2 - STARTINDEX - 1,startIndex + subEndIndex - STARTINDEX);
            SpanContent spanContent = new SpanContent(startIndex + subStartIndex + 2 - STARTINDEX - 1,startIndex + subEndIndex - STARTINDEX,methodCode);
//            System.out.println(spanContent);
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
//        String nString = n.toString().split("\r\n")[0];
        int startIndex = n.getBegin().get().column;
        int endIndex = n.getEnd().get().column;
        getRemoveString(startIndex,endIndex);
//        String oldStr = nString.substring(0,nString.indexOf(")")+2);
//        removeList.add(oldString);
    }
    //function-structure - switch
    @Override
    public void visit(SwitchStmt n, Void arg) {
        super.visit(n, arg);
        String selector = n.getSelector().toString();
//        removeList.add(selector);
        int startIndex1 = n.getBegin().get().column;
        int endIndex1 = n.getEnd().get().column;

        getRemoveString(startIndex1,endIndex1);

        NodeList<SwitchEntryStmt> entries = n.getEntries();
        for(SwitchEntryStmt entryStmt : entries){
//            String entryStr = entryStmt.toString();
            int startIndex2 = entryStmt.getBegin().get().column;
            int endIndex2 = entryStmt.getEnd().get().column;
            String innerStr = this.methodCode.substring(startIndex2 - STARTINDEX - 1, endIndex2 - STARTINDEX);
            int innerIndex = innerStr.indexOf(":");
            String removeStr = this.methodCode.substring(startIndex2 - STARTINDEX - 1, startIndex2 + innerIndex - STARTINDEX - 1);
            SpanContent spanContent = new SpanContent(startIndex2 - STARTINDEX - 1, startIndex2 + innerIndex - STARTINDEX - 1,methodCode);
            ArrayList<SpanContent> functionStructureList = (ArrayList<SpanContent>) this.map.get("function_structure");
            functionStructureList.add(spanContent);
            this.map.put("function_structure",functionStructureList);
//            entryStr = entryStr.substring(0,entryStr.indexOf(":"));
//            removeList.add(entryStr);
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
//        String nString = n.getCondition().toString();
        int startIndex = n.getBegin().get().column;
        int endIndex = n.getEnd().get().column;
        String nString = this.methodCode.substring(startIndex - 26 - 1,endIndex - 26);
        int whileIndex = nString.lastIndexOf("while");
        getRemoveString(startIndex + whileIndex,endIndex);
//        String removeStr = this.methodCode.substring(startIndex + whileIndex - 26 - 1,endIndex - 26);
//        SpanContent spanContent = new SpanContent(startIndex + whileIndex - 26 - 1,endIndex - 26,removeStr);
//        ArrayList<SpanContent> functionStructureList = (ArrayList<SpanContent>) this.map.get("function_structure");
//        functionStructureList.add(spanContent);
//        this.map.put("function_structure",functionStructureList);
//        System.out.println(removeStr);
//        nString = nString.substring(whileIndex);
//        int subStartIndex = nString.indexOf('(');
//        int subEndIndex = getEndIndex(subStartIndex,nString);
//        String removeString = nString.substring(subStartIndex+1,subEndIndex);
//        removeList.add(removeString);
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
//        System.out.print(code.split(" +").length+"-");
        // 解析Java源代码生成AST
        MyVisitor visitor = new MyVisitor(code);
//        System.out.println(visitor.code);
        CompilationUnit cu = JavaParser.parse(visitor.code);
        visitor.visit(cu, null);
//        System.out.println(visitor.map);
//        visitor.remove();
        return visitor.methodCode;
//        System.out.println(visitor.methodCode.split(" +").length);
    }
//
    public static void main(String[] args) {
        String code = "public String toXmlString ( ) { final StringBuilder buffer = new StringBuilder ( ) ; buffer . append ( \"<\" ) ; buffer . append ( getName ( ) ) ; Optional < String > attr = TagExtensions . attributesToString ( getAttributes ( ) ) ; if ( attr . isPresent ( ) ) { buffer . append ( attr . get ( ) ) ; } if ( isEndTag ( ) ) { buffer . append ( \">\" ) ; if ( getChildren ( ) != null && ! getChildren ( ) . isEmpty ( ) ) { String processingContent = getContent ( ) ; Integer lastPosition = 0 ; for ( final ChildTagPosition child : getChildren ( ) ) { final String subContent = getContent ( ) . substring ( lastPosition , child . getPosition ( ) ) ; lastPosition = child . getPosition ( ) ; processingContent = processingContent . substring ( lastPosition , processingContent . length ( ) ) ; buffer . append ( subContent ) ; buffer . append ( child . getChild ( ) . toXmlString ( ) ) ; } buffer . append ( processingContent ) ; } else { buffer . append ( getContent ( ) ) ; } buffer . append ( \"</\" ) ; buffer . append ( getName ( ) ) ; buffer . append ( \">\" ) ; } else { buffer . append ( \"/>\" ) ; } return buffer . toString ( ) ; }\n ";
        code = "private void createServletApiFilter ( BeanReference authenticationManager ) { final String ATT_SERVLET_API_PROVISION = \"servlet-api-provision\" ; final String DEF_SERVLET_API_PROVISION = \"true\" ; String provideServletApi = httpElt . getAttribute ( ATT_SERVLET_API_PROVISION ) ; if ( ! StringUtils . hasText ( provideServletApi ) ) { provideServletApi = DEF_SERVLET_API_PROVISION ; } if ( \"true\" . equals ( provideServletApi ) ) { servApiFilter = GrantedAuthorityDefaultsParserUtils . registerWithDefaultRolePrefix ( pc , SecurityContextHolderAwareRequestFilterBeanFactory . class ) ; servApiFilter . getPropertyValues ( ) . add ( \"authenticationManager\" , authenticationManager ) ; } } ";
        code = "private void createJaasApiFilter ( ) { final String ATT_JAAS_API_PROVISION = \"jaas-api-provision\" ; final String DEF_JAAS_API_PROVISION = \"false\" ; String provideJaasApi = httpElt . getAttribute ( ATT_JAAS_API_PROVISION ) ; if ( ! StringUtils . hasText ( provideJaasApi ) ) { provideJaasApi = DEF_JAAS_API_PROVISION ; } if ( \"true\" . equals ( provideJaasApi ) ) { jaasApiFilter = new RootBeanDefinition ( JaasApiIntegrationFilter . class ) ; } } ";
        code = "public Object execute ( final Object iThis , final OIdentifiable iCurrentRecord , final Object iCurrentResult , final OCommandContext iContext ) { for ( int i = 0 ; i < configuredParameters . length ; ++ i ) { runtimeParameters [ i ] = configuredParameters [ i ] ; if ( configuredParameters [ i ] instanceof OSQLFilterItemField ) { runtimeParameters [ i ] = ( ( OSQLFilterItemField ) configuredParameters [ i ] ) . getValue ( iCurrentRecord , iCurrentResult , iContext ) ; } else if ( configuredParameters [ i ] instanceof OSQLFunctionRuntime ) runtimeParameters [ i ] = ( ( OSQLFunctionRuntime ) configuredParameters [ i ] ) . execute ( iThis , iCurrentRecord , iCurrentResult , iContext ) ; else if ( configuredParameters [ i ] instanceof OSQLFilterItemVariable ) { runtimeParameters [ i ] = ( ( OSQLFilterItemVariable ) configuredParameters [ i ] ) . getValue ( iCurrentRecord , iCurrentResult , iContext ) ; } else if ( configuredParameters [ i ] instanceof OCommandSQL ) { try { runtimeParameters [ i ] = ( ( OCommandSQL ) configuredParameters [ i ] ) . setContext ( iContext ) . execute ( ) ; } catch ( OCommandExecutorNotFoundException ignore ) { final String text = ( ( OCommandSQL ) configuredParameters [ i ] ) . getText ( ) ; final OSQLPredicate pred = new OSQLPredicate ( text ) ; runtimeParameters [ i ] = pred . evaluate ( iCurrentRecord instanceof ORecord ? ( ORecord ) iCurrentRecord : null , ( ODocument ) iCurrentResult , iContext ) ; configuredParameters [ i ] = pred ; } } else if ( configuredParameters [ i ] instanceof OSQLPredicate ) runtimeParameters [ i ] = ( ( OSQLPredicate ) configuredParameters [ i ] ) . evaluate ( iCurrentRecord . getRecord ( ) , ( iCurrentRecord instanceof ODocument ? ( ODocument ) iCurrentResult : null ) , iContext ) ; else if ( configuredParameters [ i ] instanceof String ) { if ( configuredParameters [ i ] . toString ( ) . startsWith ( \"\\\"\" ) || configuredParameters [ i ] . toString ( ) . startsWith ( \"'\" ) ) runtimeParameters [ i ] = OIOUtils . getStringContent ( configuredParameters [ i ] ) ; } } if ( function . getMaxParams ( ) == - 1 || function . getMaxParams ( ) > 0 ) { if ( runtimeParameters . length < function . getMinParams ( ) || ( function . getMaxParams ( ) > - 1 && runtimeParameters . length > function . getMaxParams ( ) ) ) throw new OCommandExecutionException ( \"Syntax error: function '\" + function . getName ( ) + \"' needs \" + ( function . getMinParams ( ) == function . getMaxParams ( ) ? function . getMinParams ( ) : function . getMinParams ( ) + \"-\" + function . getMaxParams ( ) ) + \" argument(s) while has been received \" + runtimeParameters . length ) ; } final Object functionResult = function . execute ( iThis , iCurrentRecord , iCurrentResult , runtimeParameters , iContext ) ; if ( functionResult instanceof OAutoConvertToRecord ) ( ( OAutoConvertToRecord ) functionResult ) . setAutoConvertToRecord ( false ) ; return transformValue ( iCurrentRecord , iContext , functionResult ) ; }";
//        code = "\n" +
//                "protected void parseServerURLs ( ) { String lastHost = null ; int dbPos = url . indexOf ( ' ' ) ; if ( dbPos == - 1 ) { addHost ( url ) ; lastHost = url ; name = url ; } else { name = url . substring ( url . lastIndexOf ( \"/\" ) + 1 ) ; for ( String host : url . substring ( 0 , dbPos ) . split ( ADDRESS_SEPARATOR ) ) { lastHost = host ; addHost ( host ) ; } } synchronized ( serverURLs ) { if ( serverURLs . size ( ) == 1 && getClientConfiguration ( ) . getValueAsBoolean ( OGlobalConfiguration . NETWORK_BINARY_DNS_LOADBALANCING_ENABLED ) ) { final String primaryServer = lastHost ; OLogManager . instance ( ) . debug ( this , \"Retrieving URLs from DNS '%s' (timeout=%d)...\" , primaryServer , getClientConfiguration ( ) . getValueAsInteger ( OGlobalConfiguration . NETWORK_BINARY_DNS_LOADBALANCING_TIMEOUT ) ) ; try { final Hashtable < String , String > env = new Hashtable < String , String > ( ) ; env . put ( \"java.naming.factory.initial\" , \"com.sun.jndi.dns.DnsContextFactory\" ) ; env . put ( \"com.sun.jndi.ldap.connect.timeout\" , getClientConfiguration ( ) . getValueAsString ( OGlobalConfiguration . NETWORK_BINARY_DNS_LOADBALANCING_TIMEOUT ) ) ; final DirContext ictx = new InitialDirContext ( env ) ; final String hostName = ! primaryServer . contains ( \":\" ) ? primaryServer : primaryServer . substring ( 0 , primaryServer . indexOf ( \":\" ) ) ; final Attributes attrs = ictx . getAttributes ( hostName , new String [ ] { \"TXT\" } ) ; final Attribute attr = attrs . get ( \"TXT\" ) ; if ( attr != null ) { for ( int i = 0 ; i < attr . size ( ) ; ++ i ) { String configuration = ( String ) attr . get ( i ) ; if ( configuration . startsWith ( \"\\\"\" ) ) configuration = configuration . substring ( 1 , configuration . length ( ) - 1 ) ; if ( configuration != null ) { final String [ ] parts = configuration . split ( \" \" ) ; List < String > toAdd = new ArrayList <> ( ) ; for ( String part : parts ) { if ( part . startsWith ( \"s=\" ) ) { toAdd . add ( part . substring ( \"s=\" . length ( ) ) ) ; } } if ( toAdd . size ( ) > 0 ) { serverURLs . clear ( ) ; for ( String host : toAdd ) addHost ( host ) ; } } } } } catch ( NamingException ignore ) { } } } } ";
        code = "public Object execute ( final Object iThis , final OIdentifiable iCurrentRecord , final Object iCurrentResult , final OCommandContext iContext ) { if ( iThis == null ) return null ; if ( configuredParameters != null ) { for ( int i = 0 ; i < configuredParameters . length ; ++ i ) { runtimeParameters [ i ] = configuredParameters [ i ] ; if ( method . evaluateParameters ( ) ) { if ( configuredParameters [ i ] instanceof OSQLFilterItemField ) { runtimeParameters [ i ] = ( ( OSQLFilterItemField ) configuredParameters [ i ] ) . getValue ( iCurrentRecord , iCurrentResult , iContext ) ; if ( runtimeParameters [ i ] == null && iCurrentResult instanceof OIdentifiable ) runtimeParameters [ i ] = ( ( OSQLFilterItemField ) configuredParameters [ i ] ) . getValue ( ( OIdentifiable ) iCurrentResult , iCurrentResult , iContext ) ; } else if ( configuredParameters [ i ] instanceof OSQLMethodRuntime ) runtimeParameters [ i ] = ( ( OSQLMethodRuntime ) configuredParameters [ i ] ) . execute ( iThis , iCurrentRecord , iCurrentResult , iContext ) ; else if ( configuredParameters [ i ] instanceof OSQLFunctionRuntime ) runtimeParameters [ i ] = ( ( OSQLFunctionRuntime ) configuredParameters [ i ] ) . execute ( iCurrentRecord , iCurrentRecord , iCurrentResult , iContext ) ; else if ( configuredParameters [ i ] instanceof OSQLFilterItemVariable ) { runtimeParameters [ i ] = ( ( OSQLFilterItemVariable ) configuredParameters [ i ] ) . getValue ( iCurrentRecord , iCurrentResult , iContext ) ; if ( runtimeParameters [ i ] == null && iCurrentResult instanceof OIdentifiable ) runtimeParameters [ i ] = ( ( OSQLFilterItemVariable ) configuredParameters [ i ] ) . getValue ( ( OIdentifiable ) iCurrentResult , iCurrentResult , iContext ) ; } else if ( configuredParameters [ i ] instanceof OCommandSQL ) { try { runtimeParameters [ i ] = ( ( OCommandSQL ) configuredParameters [ i ] ) . setContext ( iContext ) . execute ( ) ; } catch ( OCommandExecutorNotFoundException ignore ) { final String text = ( ( OCommandSQL ) configuredParameters [ i ] ) . getText ( ) ; final OSQLPredicate pred = new OSQLPredicate ( text ) ; runtimeParameters [ i ] = pred . evaluate ( iCurrentRecord instanceof ORecord ? ( ORecord ) iCurrentRecord : null , ( ODocument ) iCurrentResult , iContext ) ; configuredParameters [ i ] = pred ; } } else if ( configuredParameters [ i ] instanceof OSQLPredicate ) runtimeParameters [ i ] = ( ( OSQLPredicate ) configuredParameters [ i ] ) . evaluate ( iCurrentRecord . getRecord ( ) , ( iCurrentRecord instanceof ODocument ? ( ODocument ) iCurrentResult : null ) , iContext ) ; else if ( configuredParameters [ i ] instanceof String ) { if ( configuredParameters [ i ] . toString ( ) . startsWith ( \"\\\"\" ) || configuredParameters [ i ] . toString ( ) . startsWith ( \"'\" ) ) runtimeParameters [ i ] = OIOUtils . getStringContent ( configuredParameters [ i ] ) ; } } } if ( method . getMaxParams ( ) == - 1 || method . getMaxParams ( ) > 0 ) { if ( runtimeParameters . length < method . getMinParams ( ) || ( method . getMaxParams ( ) > - 1 && runtimeParameters . length > method . getMaxParams ( ) ) ) throw new OCommandExecutionException ( \"Syntax error: function '\" + method . getName ( ) + \"' needs \" + ( method . getMinParams ( ) == method . getMaxParams ( ) ? method . getMinParams ( ) : method . getMinParams ( ) + \"-\" + method . getMaxParams ( ) ) + \" argument(s) while has been received \" + runtimeParameters . length ) ; } } final Object functionResult = method . execute ( iThis , iCurrentRecord , iContext , iCurrentResult , runtimeParameters ) ; return transformValue ( iCurrentRecord , iContext , functionResult ) ; } ";
        code = "private static int find ( char [ ] data , int off , int out , Term term ) { if ( off >= out ) return - 1 ; switch ( term . type ) { case Term . CHAR : { char c = term . c ; int i = off ; while ( i < out ) { if ( data [ i ] == c ) break ; i ++ ; } return i - off ; } case Term . BITSET : { IntBitSet arr = term . bitset ; int i = off ; char c ; if ( ! term . inverse ) while ( i < out ) { if ( ( c = data [ i ] ) <= 255 && arr . get ( c ) ) break ; else i ++ ; } else while ( i < out ) { if ( ( c = data [ i ] ) <= 255 && arr . get ( c ) ) i ++ ; else break ; } return i - off ; } case Term . BITSET2 : { int i = off ; IntBitSet [ ] bitset2 = term . bitset2 ; char c ; if ( ! term . inverse ) while ( i < out ) { IntBitSet arr = bitset2 [ ( c = data [ i ] ) >> 8 ] ; if ( arr != null && arr . get ( c & 0xff ) ) break ; else i ++ ; } else while ( i < out ) { IntBitSet arr = bitset2 [ ( c = data [ i ] ) >> 8 ] ; if ( arr != null && arr . get ( c & 0xff ) ) i ++ ; else break ; } return i - off ; } } throw new IllegalArgumentException ( \"can't seek this kind of term:\" + term . type ) ; }";
        RemoveAll ra = new RemoveAll();
        ra.remove(code);
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

//    public static void main(String[] args) throws IOException {
//        int count = 0;
//        List<String> urlList = new ArrayList<>();
//        RemoveAll rn = new RemoveAll();
//        List<String> stringList = rn.readFile("./code2nl/test_new.txt");
//        FileOutputStream fileOutputStream = new FileOutputStream("./new_test_remove_function_invocation.txt");
//        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
//        BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
//        double allRemovePercent = 0, allCount = 0, allRemovedCount = 0;
//        for(int i=0; i < stringList.size();i++){
//            String lineStr = stringList.get(i);
//            String[] codeList = lineStr.split("<CODESPLIT>");
////            String code = codeList[4];
//            String code = codeList[0];
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
////                codeList[4] = code;
//                codeList[0] = code;
//                int removeLength =code.split(" +").length;
//                allCount += originLength;
//                allRemovedCount += removeLength;
//                double removePercent = (originLength - removeLength) * 1.0 / originLength;
//                allRemovePercent += removePercent;
//                System.out.println("remove percent:"+removePercent);
//                String newLine = String.join("<CODESPLIT>", codeList).replace("\n","");
//                bufferedWriter.write(newLine + "\n");
//                count++;
//
//
//            }catch (ParseProblemException e){
//                System.out.println(e);
////                System.out.println(code);
////                System.out.println(e);
////                System.out.println(code);
////                System.out.println(e);
//            }
////            System.out.println("cut:"+code.split(" +").length);
//        }
//        outputStreamWriter.close();
//        System.out.println(count+"/"+stringList.size());
//        System.out.println("共有"+count+"条数据");
//        System.out.println("all remove percent:"+allRemovePercent/count);
//        System.out.println("all remove percent:"+ (1 - allRemovedCount/allCount));
////        String code = rn.removeCommon("public void addServiceIfNotPresent ( WiFiP2pService service ) { WfdLog . d ( TAG , \"addServiceIfNotPresent BEGIN, with size = \" + serviceList . size ( ) ) ; if ( service == null ) { WfdLog . e ( TAG , \"Service is null, returning...\" ) ; return ; } boolean add = true ; for ( WiFiP2pService element : serviceList ) { if ( element != null && element . getDevice ( ) . equals ( service . getDevice ( ) ) && element . getInstanceName ( ) . equals ( service . getInstanceName ( ) ) ) { add = false ; //already in the list } } if ( add ) { serviceList . add ( service ) ; } WfdLog . d ( TAG , \"addServiceIfNotPresent END, with size = \" + serviceList . size ( ) ) ; } \n");
////        MyVisitor visitor = new MyVisitor(code);
////        RemoveName rn = new RemoveName();
////        String code = rn.remove(" public void validate ( Object response ) throws RedditParseException {  if ( response == null ) { throw new RedditParseException ( ) ; }  if ( ! ( response instanceof JSONObject ) ) { throw new RedditParseException ( \"not a JSON response\" ) ; } jsonResponse = ( ( JSONObject ) response ) ;  if ( jsonResponse . get ( \"error\" ) != null ) { throw new RedditParseException ( JsonUtils . safeJsonToInteger ( jsonResponse . get ( \"error\" ) ) ) ; }  if ( jsonResponse . get ( \"data\" ) == null && jsonResponse . get ( \"json\" ) == null ) { throw new RedditParseException ( \"data is missing from listing\" ) ; } }\n ");
//
//    }

}


