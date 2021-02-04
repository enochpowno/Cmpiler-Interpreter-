import com.udojava.evalex.Expression;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.*;

public class Visitor2 extends sklLanguageBaseVisitor<Object> {

    List<Token> tokenList = new ArrayList<>();
    public String IDENTIFIER = "[a-zA-Z][a-zA-Z0-9_]*";
    public static ArrayList<String> data_types = new ArrayList<>(Arrays.asList("int", "float", "flag", "String", "char"));

    public static ArrayList<String> keywords = new ArrayList<>(Arrays.asList("START", "INT", "CHAR", "BOOLEAN", "BREAK", "CATCH", "DO", "DOUBLE", "ELSES",
            "FINAL", "FINALLY", "FOR", "IF", "NEW", "RETURN", "THROWS", "TRY", "VOID", "WHILE", "OUTPUT", "INPUT"));
    public static ArrayList<String> identifiers = new ArrayList<>(Arrays.asList("IDENTIFIER"));
    public static ArrayList<String> operators = new ArrayList<>(Arrays.asList("ASSIGN", "GT", "LT", "BANG", "COLON", "EQUAL", "LE", "GE",
            "NOTEQUAL", "AND", "OR", "INC", "DEC", "ADD", "SUB", "MUL", "DIV", "CARET", "MOD", "ADD_ASSIGN", "SUB_ASSIGN", "MUL_ASSIGN", "DIV_ASSIGN", "MOD_ASSIGN"));
    public static ArrayList<String> literals = new ArrayList<>(Arrays.asList("DECIMAL_LITERAL", "FLOAT_LITERAL", "BOOL_LITERAL", "CHAR_LITERAL", "STRING_LITERAL", "NULL_LITERAL"));
    public static ArrayList<String> separators = new ArrayList<>(Arrays.asList("LPAREN", "RPAREN", "LBRACE", "RBRACE", "LBRACK", "RBRACK", "SEMI", "COMMA", "DOT"));
    public static ArrayList<String> comments = new ArrayList<>(Arrays.asList("COMMENT", "LINE_COMMENT"));
    Stack<Scope> scopeList; // list of functions
    sklLanguageLexer lexerList;
    Stack<Scope> stack; // monitors where it's being executed
    mainIDE mainController;
    Stack<Symbol> globalVar; // list of global variables
    String str;
    String methodHold;
    ArrayList<Symbol> symbolHolder = new ArrayList<Symbol>();
    boolean arithmeticError = false;
    boolean nullError = false;
    boolean indexError = false;
    boolean parseError = false;
    int lineError = 0;
    int dataTypeLevel =0;
    String booleanExpr = "";
    HashMap<String, Symbol> temphashmap;


    public Visitor2(List<Token> tokens, sklLanguageLexer lexer, Stack<Scope> stack1, Stack<Scope> scopeList, mainIDE cont) {
        stack = stack1;
        lexerList = lexer;
        this.scopeList = scopeList;
        mainController = cont;
        globalVar = new Stack<>();

        for (int i = 0; i < tokens.size(); i++)
            if (!tokens.get(i).getText().contains(" ") && !tokens.get(i).getText().contains("\n") && !tokens.get(i).getText().contains("\t"))
                tokenList.add(tokens.get(i));
    }

    @Override
    public Object visitCompilationUnit(sklLanguageParser.CompilationUnitContext ctx) {

        if (ctx.EOF() != null) {
            System.out.println(ctx.EOF());
        }
        return visitChildren(ctx);
    }

    @Override
    public Object visitVariableModifier(sklLanguageParser.VariableModifierContext ctx) {
        return visitChildren(ctx);
    }
//
//    @Override
//    public Object visitClassDeclaration(sklLanguageParser.ClassDeclarationContext ctx) {
//        return visitChildren(ctx);
//    }

    @Override
    public Object visitTypeParameters(sklLanguageParser.TypeParametersContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Object visitTypeParameter(sklLanguageParser.TypeParameterContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Object visitTypeBound(sklLanguageParser.TypeBoundContext ctx) {
        return visitChildren(ctx);
    }

//    @Override
//    public Object visitClassBody(sklLanguageParser.ClassBodyContext ctx) {
//        return visitChildren(ctx);
//    }

    @Override
    public Object visitClassBodyDeclaration(sklLanguageParser.ClassBodyDeclarationContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Object visitMemberDeclaration(sklLanguageParser.MemberDeclarationContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Object visitMethodDeclaration_1(sklLanguageParser.MethodDeclaration_1Context ctx) {
        System.out.println("[IN VISIT METHOD DECLARATION 1");
        System.out.println("[CTX.IDENTIFIER] " + ctx.IDENTIFIER());

        List<Token> temp = getFlatTokenList(ctx);
        boolean pass = false;

        // semantic checking if return statement has a function
        if (data_types.contains(temp.get(0).getText())) { // if first token is a datatype of number, flag etc
            for (int i = 1; i < temp.size(); i++) { // loop that there should be a pass statement
                if (temp.get(i).getText().equals("return")) {
                    pass = true;
                    break;
                }
            }
            if (!pass) {
                mainController.appendError("Error: Missing return statement in function '" + ctx.IDENTIFIER() + "' at line " + ctx.getStart().getLine());
            }
        } else {
            for (int i = 1; i < temp.size(); i++) { // loop that there should be a pass statement
                if (temp.get(i).getText().equals("return")) {
                    pass = true;
                    break;
                }
            }
            if (pass) {
                mainController.appendError("Error: Unexpected return statement in function '" + ctx.IDENTIFIER() + "' at line " + ctx.getStart().getLine());
            }
        }

        boolean duplicate = false;
        for (Scope item : scopeList) {
            if (ctx.IDENTIFIER().getText().equals(item.getName())) {
                duplicate = true;

            }
        }

        if (!duplicate) {
            Scope add = new Scope(ctx.IDENTIFIER().getText(), null, ctx.methodBody());
            if (ctx.IDENTIFIER().getText().equals("main")) // add method to STACK only if its the main method
                stack.push(add);


            scopeList.add(add); // add all methods to ARRAYLIST
        } else {
            mainController.appendError("Error: Duplicate function name '" + ctx.IDENTIFIER() + "' at line " + ctx.getStart().getLine());

        }


        Iterator value = stack.iterator();

        System.out.print("[STACK contains] ");
        for (Scope item : stack)
            System.out.print(item.getName() + " " + item.hashCode());
        System.out.println();

        System.out.print("[SCOPELIST contains] ");
        for (Scope item : scopeList)
            System.out.print(item.getName() + " ");
        System.out.println();


        return visitChildren(ctx);
    }

    @Override
    public Object visitMethodDeclaration_2(sklLanguageParser.MethodDeclaration_2Context ctx) {

        mainController.appendError("Error: Missing return type at line " + ctx.getStart().getLine());
        return visitChildren(ctx);
    }

    @Override
    public Object visitMethodBody(sklLanguageParser.MethodBodyContext ctx) {
        System.out.println("VISIT METHOD BODY PLEASE");
        Object val = 0;


        if (!stack.isEmpty()) {
            if(symbolHolder.size() > 0){
                for (int i=0; i<symbolHolder.size(); i++){
                    stack.peek().pushToSymbolTable(symbolHolder.get(i).getData_type(), symbolHolder.get(i).getGlobal_var_name(), symbolHolder.get(i).getValue());
                }
                symbolHolder.clear();
            }
            System.out.println(stack.peek().getName() + " " + stack.peek().hashCode());
            printSymbolTable(stack.peek(), stack.peek().getSymbolTable());

            System.out.println(ctx.getParent().getChild(0).getText());
            Object returnableValue = null;
            for (int i=0; i<ctx.getChildCount(); i++){
                if(ctx.getChild(i) instanceof sklLanguageParser.Statement_2Context){
                    returnableValue = visit(ctx.getChild(i));
                }
                else if(ctx.getChild(i) instanceof sklLanguageParser.Statement_8Context){
                    returnableValue = visit(ctx.getChild(i));
                    if(ctx.getChild(i+2) != null){
                        mainController.appendError("Semantic Error: Unreachable Code");
                    }
                    break;
                }
                else{
                    visit(ctx.getChild(i));
                }

                //handling run time errors :(((((
                if(indexError) {
                    mainController.appendError("Runtime Error: Index out of bounds at line " + lineError);
                    indexError = false;
                }
                else if(nullError) {
                    mainController.appendError("Runtime Error: Null Pointer exception at line " + lineError);
                    nullError = false;
                }
                else if(arithmeticError) {
                    mainController.appendError("Runtime Error: Arithmetic Exception at line " + lineError);
                    arithmeticError = false;
                }
            }

            return returnableValue;
        }
        return null;
    }

    @Override
    public Object visitTypeTypeOrVoid(sklLanguageParser.TypeTypeOrVoidContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Object visitGenericMethodDeclaration(sklLanguageParser.GenericMethodDeclarationContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Object visitFieldDeclaration(sklLanguageParser.FieldDeclarationContext ctx) {
        if (ctx.getText().contains("<missing ';'>"))
            mainController.appendError("Error: Missing ';' at line " + ctx.getStart().getLine());

        if (!ctx.getText().contains("=")) { // if variable declaration has no initialization. Ex. number x;
            mainController.appendError("Error: Variable not initialized at line " + ctx.getStart().getLine());

        }


        System.out.println("FIELDDECLARATION");
        String var_name = "";
        String datatype = "";
        for (int i = 0; i < ctx.getChildCount(); i++) {
            // System.out.println(getFlatTokenList(ctx.getChild(i)));
            List<Token> temp = getFlatTokenList(ctx.getChild(i));

            for (int j = 0; j < temp.size(); j++) {
                System.out.println("TEMP " + temp.get(j).getText());

                if (temp.get(j).getText().equals("constant")) {
                    datatype = temp.get(j).getText() + " ";

                } else if (data_types.contains(temp.get(j).getText())) {
                    datatype += temp.get(j).getText();
                } else if (temp.get(j).getText().equals("[")) {
                    datatype += "[]";
                } else if (checkTokenType(temp.get(j).getText()).equals("IDENTIFIER")) {
                    var_name = temp.get(j).getText();
                } else if (temp.get(j).getText().equals("=")) {
                    Symbol add = new Symbol(datatype, temp.get(j + 1).getText(), var_name);
                    System.out.println("PUSH DATATYPE NOW - " + datatype);
                    if (checkVariableDuplicateinGlobalVar(var_name, ctx.getStart().getLine())) {
                        globalVar.add(add);
                    }
                }
            }
        }
        if (!stack.isEmpty())
            printSymbolTable(stack.peek(), stack.peek().getSymbolTable());


        return visitChildren(ctx);
    }

    @Override
    public Object visitConstDeclaration(sklLanguageParser.ConstDeclarationContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Object visitConstantDeclarator(sklLanguageParser.ConstantDeclaratorContext ctx) {
        if (ctx.getText().contains("<missing ';'>"))
            mainController.appendError("Error: Missing ';' at line " + ctx.getStart().getLine());

        return visitChildren(ctx);
    }

    @Override
    public Object visitVariableDeclarators(sklLanguageParser.VariableDeclaratorsContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Object visitVariableDeclarator(sklLanguageParser.VariableDeclaratorContext ctx) {


        System.out.println("VARIABLEDECLARTAOR1 " + ctx.getText());
        if (ctx.parent.parent.parent.getRuleContext().getClass().toString().contains("ForInitContext")) { 
            if (ctx.getChildCount() == 1) { 
                mainController.appendError("Error: Missing assignment operator at line " + ctx.getStart().getLine());
            }
        }

        Object tempObject;
        for (int i = 0; i < ctx.getChildCount(); i++) {
            System.out.println(i + " = " + ctx.getChild(i).getText());
            if (ctx.getChild(i) instanceof sklLanguageParser.VariableDeclaratorIdContext) {

                if (ctx.getChildCount() >= 3 && ctx.getChild(i + 2).getChild(0) instanceof sklLanguageParser.InputCorrectContext) { // input now wtf do i do????
                    List<Token> temp = getFlatTokenList(ctx.getChild(i + 2));
                    if (temp.size() > 3) {
                        tempObject = processInput(temp.get(0).getText(), temp.get(2).getText());
                    } else {
                        tempObject = processInput(temp.get(0).getText(), "");
                    }
                    System.out.println(ctx.getChild(i).getText() + " = " + tempObject);

                    if (!stack.isEmpty()) {

                        System.out.println(stack.peek().getName());
                        System.out.println(ctx.getChild(i).getText());
                        Symbol sym = stack.peek().getSymbolTable().get(ctx.getChild(i).getText());
                        System.out.println("im here " + sym.getData_type() + " ");
                        sym.setValue(tempObject);
                        printSymbolTable(stack.peek(), stack.peek().getSymbolTable());
                    } else {
                        mainController.appendError("Error: Cannot ask for input globally");
                    }

                }
            }

        }
        System.out.println("===============================");

        if (ctx.parent.parent.parent.getRuleContext().getClass().toString().contains("ForInitContext")) { // to check if child ng for loop yung variable declarator
            //  System.out.println("im in FORINITCONTEXT TREE");
            if (ctx.getChildCount() == 1) { // isa lang anak ni variable declarator meaning wala yung variable initializer na child
                mainController.appendError("Error: Missing assignment operator at line " + ctx.getStart().getLine());
            }
        }

        return null;
    }

    public Object processInput(String value, String print) {

        String tempVal = mainController.askInput(print);
        System.out.println("NASA PROCESS INPUT BA AKO?? " + tempVal);


        switch (value) {
            case "input":
                return tempVal;
            case "inputInt":
                try {
                    return Integer.parseInt(tempVal);
                } catch (Exception e) {
                    mainController.appendError("Error: Input data type mismatch for keyword 'inputInt'");
                    return null;
                }
            case "inputFloat":
                try {
                    return Double.parseDouble(tempVal);
                } catch (Exception e) {
                    mainController.appendError("Error: Input data type mismatch for keyword 'inputFloat'");
                    return null;
                }
            case "inputChar":
                return tempVal.charAt(0);
            case "inputString":
                    return tempVal;
        }


        return tempVal;
    }

    @Override
    public Object visitVariableDeclaratorId(sklLanguageParser.VariableDeclaratorIdContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Object visitVariableInitializer(sklLanguageParser.VariableInitializerContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Object visitArrayInitializer_1(sklLanguageParser.ArrayInitializer_1Context ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Object visitArrayInitializer_2(sklLanguageParser.ArrayInitializer_2Context ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Object visitQualifiedNameList(sklLanguageParser.QualifiedNameListContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Object visitFormalParameters(sklLanguageParser.FormalParametersContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Object visitFormalParameterList(sklLanguageParser.FormalParameterListContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Object visitFormalParameter(sklLanguageParser.FormalParameterContext ctx) {

        System.out.println(ctx.typeType().primitiveType().getText());
        System.out.println(ctx.variableDeclaratorId().getText());
        String data_type = ctx.typeType().primitiveType().getText();
        String var_name = ctx.variableDeclaratorId().getText();
        Symbol symbol = new Symbol(data_type, null);
        scopeList.get(scopeList.size() - 1).getSymbolTable().put(var_name, symbol);

        return visitChildren(ctx);
    }

    @Override
    public Object visitLastFormalParameter(sklLanguageParser.LastFormalParameterContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Object visitQualifiedName(sklLanguageParser.QualifiedNameContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Object visitLiteral(sklLanguageParser.LiteralContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Object visitIntegerLiteral(sklLanguageParser.IntegerLiteralContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Object visitFloatLiteral(sklLanguageParser.FloatLiteralContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Object visitElementValuePairs(sklLanguageParser.ElementValuePairsContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Object visitElementValuePair(sklLanguageParser.ElementValuePairContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Object visitElementValue(sklLanguageParser.ElementValueContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Object visitElementValueArrayInitializer(sklLanguageParser.ElementValueArrayInitializerContext ctx) {
        if (ctx.getText().contains("<missing '}'>")) {
            // System.out.println("VISIT ELEMENT VALUE ARRAY " + ctx.getText());

            mainController.appendError("Error: Missing '}' at line " + ctx.getStart().getLine());
        }

        return visitChildren(ctx);
    }

    @Override
    public Object visitBlock(sklLanguageParser.BlockContext ctx) {

        Object returnableValue;
        System.out.println("VISIT BLOCK " + ctx.getText());
        for (int i = 0; i < ctx.getChildCount(); i++) {
            if (ctx.getChild(i).getText().equals("<missing '}'>")) {
                mainController.appendError("Error: Missing '}' at line " + ctx.getStart().getLine());
            }
            else{
                System.out.println("VISIT CHILD " + ctx.getChild(i).getText() + ctx.getChild(i).getClass());
                if(ctx.getChild(i) instanceof sklLanguageParser.Statement_8Context){
//                    Object returnedValue = visit(ctx.getChild(i).getChild(1));
//                    System.out.println("RINETURN SA VISIT BLOCK =" + returnedValue);
//                    return returnedValue;
                    try{
                        if(ctx.getChild(i+1) != null){
                            mainController.appendError("Semantic Error: Unreachable Code");
                        }
                    }catch (Exception e){

                    }
                    returnableValue = visit(ctx.getChild(i));;
                    System.out.println("ANDITO AKO SA RETURN TREE AFTER CALLING IT = " + str);
                    System.out.println("ANDITO AKO SA RETURN TREE AFTER CALLING IT = " + returnableValue);
                    return returnableValue;

                }
                else{
                    visit(ctx.getChild(i));
                }
            }
        }
        return null;
    }

    public String processBooleanExpressions(ParseTree newCtx){

        str = "";
        for (int i=0; i<newCtx.getChildCount(); i++){
            if (newCtx.getChild(i) instanceof sklLanguageParser.Expression_4Context || newCtx.getChild(i) instanceof sklLanguageParser.Expression_1Context || newCtx.getChild(i) instanceof sklLanguageParser.Expression_10Context || newCtx.getChild(i) instanceof sklLanguageParser.Expression_3Context){
                String fmlAgain = str;
                String object = processExpressionPlease(newCtx.getChild(i));
                str = fmlAgain + object;
            }
            else if(newCtx.getChild(i) instanceof sklLanguageParser.AdditionalComparisonContext){
                str += processParExpression(newCtx.getChild(i).getChild(0).getText());
                newCtx = newCtx.getChild(i).getChild(1);
                i =-1;
            }
            else{
                str += processParExpression(newCtx.getChild(i).getText());
            }

        }
        System.out.println("Code Line 591 boolean str = " + str);
        return str;
    }

    // you can fix the errors based on the data type
    public String processExpressionPlease(ParseTree newCtx){
        System.out.println("PLSSS PROCESSS" + newCtx.getText() + newCtx.getClass());
        str = "";

        if(newCtx instanceof sklLanguageParser.Expression_3Context){
            String temp = str;
            String arrIndex = processExpressionPlease(newCtx.getChild(2));

            str = temp;

            System.out.println("I AM PRINTING AN ARRAY = " + arrIndex);
            System.out.println();
            Object valueInArrIndex = "";

            try {
                Object[] tempArr = (Object[]) stack.peek().getSymbolTable().get(newCtx.getChild(0).getText()).getValue();
                int newIndex = checkDecimal(Double.parseDouble(arrIndex));
                System.out.println(newIndex);
                System.out.println("I JUST GOT AN ARRAY " + tempArr[newIndex]);
                Object tempVal = tempArr[newIndex];

                if (tempVal == null){
                    nullError = true;
                    str ="\" waaat\"";
                    System.out.println("im in null error");
                }
                else{
                    System.out.println("not a null = " +tempVal);
                    str += tempVal;
                }

            }catch(IndexOutOfBoundsException ex){
                indexError = true;
                str ="\" waaat\"";
            }

            lineError = ((sklLanguageParser.Expression_3Context) newCtx).getStart().getLine();
        }
        else if(newCtx instanceof sklLanguageParser.Expression_4Context){
            System.out.println("METHOD CALL");
            Object getVal = null;
            String fuak = str;
            System.out.println("oh no = " + fuak);
            getVal = visit(newCtx);
            System.out.println("returned val from method call is = " + getVal);
            while (getVal == null){
                System.out.println("WAITING EXPRESSION 4 CONTEXT SA LOCAL VAR");
            }

            str = fuak;
            System.out.println("str after method call = " + str);
            str += getVal;
            lineError = ((sklLanguageParser.Expression_4Context) newCtx).getStart().getLine();
        }
        else if(newCtx instanceof sklLanguageParser.Expression_1Context){
            System.out.println("WHY WONT U WORK???");
            if (newCtx.getChild(0) instanceof sklLanguageParser.Primary_1Context){
                str += "(";
                Object getVal = null;
                getVal = visit(newCtx.getChild(0).getChild(1));
                while(getVal == null){
                    System.out.println("WAITING EXPRESSION 1 CONTEXT SA LOCAL VAR");
                }
                str += ")";
                System.out.println("PARENTHESES CALL");
            }
            else if(newCtx.getChild(0) instanceof sklLanguageParser.Primary_3Context){
                try {
                    if(stack.peek().getSymbolTable().get(newCtx.getChild(0).getText()).getData_type().equals("String") || stack.peek().getSymbolTable().get(newCtx.getChild(0).getText()).getData_type().equals("constant String")){
                        str += "\"" + stack.peek().getSymbolTable().get(newCtx.getChild(0).getText()).getValue() + "\"";
                    }
                    else if(stack.peek().getSymbolTable().get(newCtx.getChild(0).getText()).getData_type().equals("char") || stack.peek().getSymbolTable().get(newCtx.getChild(0).getText()).getData_type().equals("constant char")){
                        str += "\'" + stack.peek().getSymbolTable().get(newCtx.getChild(0).getText()).getValue() + "\'";
                    }
                    else if(stack.peek().getSymbolTable().get(newCtx.getChild(0).getText()).getData_type().equals("float") || stack.peek().getSymbolTable().get(newCtx.getChild(0).getText()).getData_type().equals("constant float")){
                        str += makeDouble(stack.peek().getSymbolTable().get(newCtx.getChild(0).getText()).getValue());
                    }
                    else if(stack.peek().getSymbolTable().get(newCtx.getChild(0).getText()).getData_type().equals("int") || stack.peek().getSymbolTable().get(newCtx.getChild(0).getText()).getData_type().equals("constant int")){
                        str += makeInt(stack.peek().getSymbolTable().get(newCtx.getChild(0).getText()).getValue());
                    }
                     // IF VARIABLE
                } catch (Exception ex) {
                    mainController.appendError("Semantic Error: Undeclared Variable at line " + ((sklLanguageParser.Expression_1Context) newCtx).getStart().getLine());
                }
            }
            else {
                visit(newCtx.getChild(0));
                str += newCtx.getChild(0).getText();
                System.out.println("CODE LINE 908 str is = " + str) ;

            }
            lineError = ((sklLanguageParser.Expression_1Context) newCtx).getStart().getLine();
        }
        else if(newCtx instanceof sklLanguageParser.Expression_10Context) {

            Object getVal1 = null;
            printSymbolTable(stack.peek(), stack.peek().getSymbolTable());
            getVal1 = visit(newCtx);
            printSymbolTable(stack.peek(), stack.peek().getSymbolTable());
            while(getVal1 == null){

            }
            System.out.println("DONE OH ");
            System.out.println("Done with Operation Call");
            lineError = ((sklLanguageParser.Expression_10Context) newCtx).getStart().getLine();
        }
        else{
            visit(newCtx);
            return null;
        }

        // to be processed after getting value from var and functions

        Object toReturn = null;
        String value;

        System.out.println("CODE LINE 927 str is " + str);
        if (str.contains("\"") || str.contains("\'")) {

            str = str.replaceAll("\"", "");
            str = str.replaceAll("\'", "");
            str = str.replaceAll("\\+", "");
            toReturn = str;

        }
        else {

            System.out.println("Problem here?? =>" + str);
            try {
                Expression expression = new Expression(str);
                BigDecimal val = expression.eval();
                if(str.contains(".")){
                    toReturn= val.setScale(2, BigDecimal.ROUND_HALF_DOWN).toString();
                }
                else{
                    toReturn= val.setScale(0, BigDecimal.ROUND_FLOOR).toString();
                }
            } catch (Exception e) {
                toReturn = str;
                arithmeticError = true;
            }
        }
        return toReturn.toString();
    }

    public void processChild2(ParseTree newCtx){

        if(newCtx instanceof sklLanguageParser.Expression_3Context){
            String temp = str;
            String arrIndex = processExpressionPlease(newCtx.getChild(2));

            str = temp;

            Object[] tempArr = (Object[])stack.peek().getSymbolTable().get(newCtx.getChild(0).getText()).getValue();
            try {
                int newIndex = checkDecimal(Double.parseDouble(arrIndex));
                System.out.println(newIndex);
                System.out.println("I JUST GOT AN ARRAY " + tempArr[newIndex]);
                str += tempArr[newIndex];
            }catch (Exception e){
                System.out.println(e);
            }

        }
        else if(newCtx instanceof sklLanguageParser.Expression_4Context){
            Object getVal4 = null;
            String temp4 = str;
            getVal4 = visit(newCtx);
            while(getVal4 == null){
            }
            str = temp4;
            str += getVal4;
        }
        else if(newCtx instanceof sklLanguageParser.Expression_1Context){
            System.out.println(newCtx.getText());
            if(newCtx.getChild(0) instanceof  sklLanguageParser.Primary_1Context){
                str += "(";
                Object getVal11 = null;
                getVal11 = visit(newCtx.getChild(0).getChild(1));
                while (getVal11 == null){
                    System.out.println("2ND WAITING PRIMARY 1 CONTEXT SA EXPRESSION 10 VISIT");
                }
                str += ")";
            }
            else if(newCtx.getChild(0) instanceof sklLanguageParser.Primary_3Context){
                try {

                    if(stack.peek().getSymbolTable().get(newCtx.getChild(0).getText()).getData_type().equals("String") || stack.peek().getSymbolTable().get(newCtx.getChild(0).getText()).getData_type().equals("constant String")){
                        str += "\"" + stack.peek().getSymbolTable().get(newCtx.getChild(0).getText()).getValue() + "\"";
                    }
                    else if(stack.peek().getSymbolTable().get(newCtx.getChild(0).getText()).getData_type().equals("char") || stack.peek().getSymbolTable().get(newCtx.getChild(0).getText()).getData_type().equals("constant char")){
                        str += "\'" + stack.peek().getSymbolTable().get(newCtx.getChild(0).getText()).getValue() + "\'";
                    }
                    else if(stack.peek().getSymbolTable().get(newCtx.getChild(0).getText()).getData_type().equals("float") || stack.peek().getSymbolTable().get(newCtx.getChild(0).getText()).getData_type().equals("constant float")){
                        str += makeDouble(stack.peek().getSymbolTable().get(newCtx.getChild(0).getText()).getValue());
                    }
                    else if(stack.peek().getSymbolTable().get(newCtx.getChild(0).getText()).getData_type().equals("int") || stack.peek().getSymbolTable().get(newCtx.getChild(0).getText()).getData_type().equals("constant int")){
                        str += makeInt(stack.peek().getSymbolTable().get(newCtx.getChild(0).getText()).getValue());
                    }

                    // IF VARIABLE
                } catch (Exception ex) {
//                    str += newCtx.getChild(0).getText();
                    mainController.appendError("Semantic Error: Undeclared Variable at line " + ((sklLanguageParser.Expression_1Context) newCtx).getStart().getLine());
                }
            }
            else {
                str += newCtx.getChild(0).getText();
            }
        }
        else if(newCtx instanceof sklLanguageParser.Expression_10Context){
            visit(newCtx);
        }
        else{
            System.out.println("HERE DIBA");
            visit(newCtx);
        }
    }

    @Override
    public Object visitLocalVariableDeclaration(sklLanguageParser.LocalVariableDeclarationContext ctx) {
        System.out.println("LOCALVARIABLEDECLARATION");
        System.out.println(stack.peek().getName() + " " + stack.peek().hashCode());
        printSymbolTable(stack.peek(), stack.peek().getSymbolTable());


        String var_name = "";
        String datatype = "";
        int functionHolder =0;
        boolean found = false;

        if (!stack.isEmpty()) {
            if (!ctx.getText().contains("=")) { // if variable declaration has no initialization. Ex. number x;
                mainController.appendError("Error: Variable not initialized at line " + ctx.getStart().getLine());

            }
        }
        for (int i = 0; i < ctx.getChildCount(); i++) {
            List<Token> temp = getFlatTokenList(ctx.getChild(i));

            for (int j = 0; j < temp.size(); j++) {
                System.out.println("J = " + j +" " + temp.get(j).getText());

                if (temp.get(j).getText().equals("constant")) { // if its a constant, add constant to datatype of var
                    datatype += temp.get(j).getText() + " ";

                } else if (data_types.contains(temp.get(j).getText())) {
                    datatype += temp.get(j).getText();
                } else if (temp.get(j).getText().equals("[")) {
                    datatype += "[]";
                } else if (checkTokenType(temp.get(j).getText()).equals("IDENTIFIER")) {
                    var_name = temp.get(j).getText();
                } else if (temp.get(j).getText().equals("=")) {
                    if (!stack.isEmpty()) {
                        if (checkVariableDuplicateinScope(stack.peek(), var_name, ctx.getStart().getLine())) { // check first if var name is valid
                            str = "";

                            //newEDIT
                            ParseTree newCtx = ctx.getChild(i).getChild(0).getChild(2);
                            System.out.println("==========================");
                            System.out.println(newCtx.getText());
                            System.out.println(newCtx.getClass());
                            System.out.println(newCtx.getChild(0).getClass());

                            Object tempObject;

                            if (newCtx.getChild(0) instanceof sklLanguageParser.InputCorrectContext) {

                                tempObject = processInput(newCtx.getChild(0).getChild(0).getText(), processExpressionPlease(newCtx.getChild(0).getChild(2)));

                                System.out.println("BLEHHH" + tempObject + newCtx.getParent().getChild(0).getText());

                                stack.peek().pushToSymbolTable(datatype, var_name, tempObject);

                                printSymbolTable(stack.peek(), stack.peek().getSymbolTable());

                            }
                            else if(newCtx.getChild(0) instanceof sklLanguageParser.ArrayInitializer_2Context) {
                                System.out.println("local var array initializer2");
                                if(newCtx.getChild(0).getChild(0).getText().equals("[") && newCtx.getChild(0).getChild(2).getText().equals("]")){
                                    String arrSize = "";
                                    int fml;
                                    try {
                                        if (checkTokenType(newCtx.getChild(0).getChild(1).getText()).equals("IDENTIFIER")) {
                                            String varname = newCtx.getChild(0).getChild(1).getText();
                                            arrSize = stack.peek().getSymbolTable().get(varname).getValue() + "";
                                            fml = Integer.parseInt(arrSize);
                                            if(fml <= 0){
                                                mainController.appendError("Run Time Error: Array size should not be negative or 0 at line " + ctx.getStart().getLine());
                                                return null;
                                            }
                                        } else {
                                            arrSize = newCtx.getChild(0).getChild(1).getText();
                                            fml  = Integer.parseInt(arrSize);
                                            if(fml <= 0){
                                                mainController.appendError("Semantic Error: Array size should not be negative or 0 at line " + ctx.getStart().getLine());
                                            }
                                        }

                                    }catch (Exception e ){
                                        mainController.appendError("Semantic Error: Invalid array size at line " + ctx.getStart().getLine());
                                        return null;
                                    }


                                    Object[] newval = null;
                                    System.out.println(datatype);
                                    newval = new Object[Integer.parseInt(arrSize)];

                                    System.out.println("Datatype " + datatype + " New Object[] created " + newval + " with array size " + arrSize);
                                    stack.peek().pushToSymbolTable(datatype, var_name, newval);
                                }
                                else{
                                    mainController.appendSemanticError("Semantic Error: Index initializer can only be an Integer at line " + ctx.getStart().getLine());
                                }
                            }
                            else{
                                Object value = processExpressionPlease(newCtx.getChild(0));


                                try{

                                    if(!nullError && !indexError && !arithmeticError){


                                        if(datatype.equals("int")){
                                            value = makeInt(checkDecimal(Double.parseDouble(value.toString())));
                                        }
                                        else if(datatype.equals("float")){
                                            value = makeDouble(value);
                                        }
                                        else if(datatype.equals("char")){
                                            if (((String) value).length() > 1){
                                                mainController.appendError("Semantic Error: Datatype mismatch at line " + ctx.getStart().getLine());
                                            }

                                        }
                                    }
                                }
                                catch (Exception e){
                                    mainController.appendError("Semantic Error: Datatype mismatch at line " + ctx.getStart().getLine());
                                }

                                stack.peek().pushToSymbolTable(datatype, var_name, value);
                                // push the value
                            }
                        }
                        else {

                        }
                    }
                }
            }
        }
        if (!stack.isEmpty())
            printSymbolTable(stack.peek(), stack.peek().getSymbolTable());

        System.out.println("Datatype = " + datatype);

        return null;
    }
    
    public void addGivenSymbols(){
        temphashmap = new HashMap<String, Symbol>();
        Iterator hmIterator = stack.peek().getSymbolTable().entrySet().iterator();
        while (hmIterator.hasNext()) {


            Map.Entry mapElement = (Map.Entry) hmIterator.next();
            Symbol symbol = (Symbol) mapElement.getValue();
            String varname = mapElement.getKey().toString();
            temphashmap.put(varname, symbol);

        }
    }

    public void removeGivenSymbols(){

        Iterator hmIterator = stack.peek().getSymbolTable().entrySet().iterator();
        while (hmIterator.hasNext()) {


            Map.Entry mapElement = (Map.Entry) hmIterator.next();
            Symbol symbol = (Symbol) mapElement.getValue();
            String varname = mapElement.getKey().toString();
            try{
                temphashmap.get(varname).getValue();
            }
            catch (Exception e){
                stack.peek().getSymbolTable().remove(varname);
            }

        }
    }

    @Override
    public Object visitStatement_2(sklLanguageParser.Statement_2Context ctx) {
        System.out.println("======================Statement 2=========================");

        for (int i = 0; i < ctx.getChildCount(); i++) {
            if (ctx.getChild(i).getText().equals("if")) {
                List<Token> tokens = getFlatTokenList(ctx.getChild(i + 1)); // GET THE COMPARISON CHILD

                String comparison = processBooleanExpressions(ctx.getChild(i+1).getChild(1));
                System.out.println(ctx.getChild(i + 1).getText());
                System.out.println("+++++++++++++++++");
                System.out.println(comparison);
                try {
                    Expression expression = new Expression(comparison);
                    System.out.println(expression.eval().intValue());
                    if (expression.eval().intValue() == 1) {
                        System.out.println(ctx.getChild(i + 3).getText());
                        System.out.println(ctx.getChild(i+3).getClass());
                        System.out.println("ANDITO AKO DIBA??");
                        temphashmap = stack.peek().getSymbolTable();

                        addGivenSymbols();
                        Object visitChildsss = visit(ctx.getChild(i + 3));

                        removeGivenSymbols();
                        return visitChildsss;
                    }
                    else{
                        try{
                            System.out.println(ctx.getChild(i+5).getText() + ctx.getChild(i+5).getClass());
                            System.out.println(ctx.getChild(i+6).getText() + ctx.getChild(i+6).getClass());
                            if(ctx.getChild(i+5).getText().equals("else")){
                                if(ctx.getChild(i+6).getText().equals("{")){
                                    System.out.println(ctx.getChild(i+7).getText() + ctx.getChild(i+7).getClass());
                                    addGivenSymbols();
                                    Object visitChildsss = visit(ctx.getChild(i+7));
                                    System.out.println("==>>>WTFFFFFF");
                                    printSymbolTable(stack.peek(), temphashmap);
                                    removeGivenSymbols();
                                    return visitChildsss;
                                }
                            }


                        }
                        catch (Exception e){

                        }

                    }
                    System.out.println("+++++++++++++++++");

                } catch (Exception ex) {
                    // run time Arithmetic error
                    mainController.appendError(comparison + " Error: RunTimeException - ArithmeticException ");
                    return null;
                }
            }
        }

        System.out.println("====================== Statement 2 DONE =========================");
        return null;
    }

    @Override
    public Object visitStatement_3(sklLanguageParser.Statement_3Context ctx) {
        System.out.println("STATEMENT_3");

        boolean valid = false;
        for (int i = 0; i < ctx.getChildCount(); i++) {
            if(ctx.getChild(i).getText().contains("++")){
                mainController.appendError("Syntax Error: Invalid operator at line " + ctx.getStart().getLine());
                return null;
            }
            System.out.println(ctx.getChildCount());
            System.out.println(getFlatTokenList(ctx.getChild(i)));

            if (ctx.getChild(i) instanceof sklLanguageParser.ForControlContext) {
                System.out.println("instance of forcontrol");
                if (!stack.isEmpty()) {
                    visit(ctx.getChild(i).getChild(0)); // visit forInit

                    valid = true;
                    while (valid) {
                        String comparison = processBooleanExpressions(ctx.getChild(i).getChild(2));;

                        try {
                            Expression expression = new Expression(comparison);
                            System.out.println(expression.eval().intValue());
                            if (expression.eval().intValue() == 1) {
                                valid = true;
                            }
                            else{
                                valid = false;
                            }
                            System.out.println("+++++++++++++++++");

                        } catch (Exception ex) {
                            // run time Arithmetic error
                            mainController.appendError(comparison + "Error: RunTimeException - ArithmeticException ");
                            return null;
                        }


                        if (valid) {
                            // EXECUTE STATEMENT TREE
                            System.out.println("IN VALIDD");
                            System.out.println("DEBUG " + ctx.getChild(i).getChild(4).getClass());
                            System.out.println("DEBUG " + ctx.getChild(i).getChild(2).getChild(2).getClass());
                            printSymbolTable(stack.peek(), stack.peek().getSymbolTable());
                            visit(ctx.getChild(5));
                            visit(ctx.getChild(i).getChild(4));
                        }
                    }
                }
            }
        }
        return null;
    }

    public String processParExpression(String token){
        if (token.equals("==")) {
            return "== ";
        } else if (token.equals("!=")) {
            return "!= ";
        } else if (token.equals("and")) {
            return "&& ";
        } else if (token.equals("or")) {
            return "|| ";
        } else if (token.equals("!")) {
            return "not ";
        }
        else{
            return token + " ";
        }
    }

    public Object getSymbolValue(Scope scope, HashMap<String, Symbol> map, String var) {
        for (String keys : map.keySet()) {
            if (keys.equals(var)) {
                return map.get(keys).getValue();
            }
        }
        return null;
    }

    @Override
    public Object visitStatement_4(sklLanguageParser.Statement_4Context ctx) {

        for (int i = 0; i < ctx.getChildCount(); i++) {
            if (ctx.getChild(i) instanceof sklLanguageParser.ParExpressionContext) {

                    boolean valid = true;
                    while (valid) {
                        String comparison = processBooleanExpressions(ctx.getChild(i).getChild(1));

                        System.out.println("+++++++++++++++++");
                        System.out.println(comparison);
                        try {
                            Expression expression = new Expression(comparison);
                            System.out.println(expression.eval().intValue());
                            if (expression.eval().intValue() == 1) {
                                System.out.println(ctx.getChild(3).getText());
                                valid = true;
                                visit(ctx.getChild(3));
                            }
                            else{
                                valid = false;
                            }
                            System.out.println("+++++++++++++++++");

                        } catch (Exception ex) {
                            // run time Arithmetic error
                            mainController.appendError(comparison + "Error: RunTimeException - ArithmeticException ");
                            return null;
                        }

                    }
                }
            }
        System.out.println("statement 4 = while loops");


        return null;
    }

    @Override
    public Object visitStatement_5(sklLanguageParser.Statement_5Context ctx) {
        if (ctx.getText().contains("<missing ';'>"))
            mainController.appendError("Error: Missing ';' at line " + ctx.getStart().getLine());


        for (int i = 0; i < ctx.getChildCount(); i++) {
            if (ctx.getChild(i) instanceof sklLanguageParser.ParExpressionContext) {
                //   visit(ctx.getChild(i)); // visit parExpression
                visit(ctx.getChild(2));
                List<Token> tokens = getFlatTokenList(ctx.getChild(i));

                if (!stack.isEmpty()) {
                    boolean valid = true;
                    while (valid) {
                        String comparison = "";
                        for (int z = 0; z < tokens.size(); z++) {
                            System.out.println(tokens.get(z).getText());
                            if (checkTokenType(tokens.get(z).getText()).equals("IDENTIFIER")) {
                                try {
                                    comparison += getSymbolValue(stack.peek(), stack.peek().getSymbolTable(), tokens.get(z).getText()).toString() + " ";
                                } catch (Exception e) {
                                    mainController.appendError("CODE LINE 1323 Semantic Error: Undeclared Variable at line " + ((sklLanguageParser.ParExpressionContext) ctx.getChild(i)).getStart().getLine());
                                    return null;
                                }

                            } else if (tokens.get(z).getText().equals("==")) {
                                comparison += "== ";
                            } else if (tokens.get(z).getText().equals("!=")) {
                                comparison += "!= ";
                            } else if (tokens.get(z).getText().equals("and")) {
                                comparison += "&& ";
                            } else if (tokens.get(z).getText().equals("or")) {
                                comparison += "|| ";
                            } else if (tokens.get(z).getText().equals("!")) {
                                comparison += "not ";
                            }
                            else {
                                comparison += tokens.get(z).getText() + " ";
                            }
                        }

                        System.out.println("+++++++++++++++++");
                        System.out.println(comparison);
                        try {
                            Expression expression = new Expression(comparison);
                            System.out.println(expression.eval().intValue());
                            if (expression.eval().intValue() == 1) {
                                System.out.println(ctx.getChild(2).getText());
                                valid = true;
                                visit(ctx.getChild(2));
                            }
                            else{
                                valid = false;
                            }
                            System.out.println("+++++++++++++++++");

                        } catch (Exception ex) {
                            // run time Arithmetic error
                            mainController.appendError(comparison + "Error: RunTimeException - ArithmeticException ");
                            return null;
                        }
                    }
                }
            }
        }

        return null;
    }

    @Override
    public Object visitStatement_6(sklLanguageParser.Statement_6Context ctx) {


            visit(ctx.getChild(2));
            System.out.println(" im in try STATEMENT_6" );

            if(indexError) {
                System.out.println("IM IN INDEX OUT OF BOUNDS STATEMENT_6 ");
                stack.peek().pushToSymbolTable("String", "e", "ArrayIndexOutOfBoundsException");
                visit(ctx.getChild(4));
                stack.peek().getSymbolTable().remove("e"); // removing e from symbol table
                indexError = false;
            }
            else if(nullError) {

                System.out.println("IM IN NullPointerException STATEMENT_6 ");
                stack.peek().pushToSymbolTable("String", "e", "NullPointerException");
                visit(ctx.getChild(4));
                stack.peek().getSymbolTable().remove("e");
                nullError = false;

            }
            else if(arithmeticError) {
                System.out.println("IM IN ArithmeticException STATEMENT_6 ");
                stack.peek().pushToSymbolTable("String", "e", "ArithmeticException");
                visit(ctx.getChild(4));
                stack.peek().getSymbolTable().remove("e"); // remove e
                arithmeticError = false;
            }


            for(int i = 0; i < ctx.getChildCount(); i++){ // checking if may finally ba
                if(ctx.getChild(i) instanceof sklLanguageParser.FinallyBlockContext){
                    visit(ctx.getChild(i));
                }
            }




        return null;
    }

    @Override
    public Object visitStatement_7(sklLanguageParser.Statement_7Context ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Object visitStatement_8(sklLanguageParser.Statement_8Context ctx) {

        System.out.println("IM IN STATEMENT 8 DUHHHHH");
        String cut = ctx.getText().substring(4, ctx.getText().length() - 1);

        if (checkTokenType(cut).equals("KEYWORD"))
            mainController.appendError("Error: Expected IDENTIFIER at line " + ctx.getStart().getLine());

        if (ctx.getText().contains("<missing ';'>"))
            mainController.appendError("Error: Missing ';' at line " + ctx.getStart().getLine());


        Object value = processExpressionPlease(ctx.getChild(1));


        System.out.println("RETURN FROM STATEMENT 8 = " + value);
        return value;
    }

    public String checkTokenType(String token) {
        for (int a = 0; a < tokenList.size(); a++) {
            if (tokenList.get(a).getText().equals(token)) {
                for (int b = 1; b <= lexerList.getVocabulary().getMaxTokenType(); b++) {
                    if (tokenList.get(a).getType() == b) {
                        if (keywords.contains(lexerList.getVocabulary().getSymbolicName(b))) {// its a keyword
                            return "KEYWORD";
                        } else if (identifiers.contains(lexerList.getVocabulary().getSymbolicName(b))) {
                            return "IDENTIFIER";
                        } else if (operators.contains(lexerList.getVocabulary().getSymbolicName(b))) {
                            return "OPERATOR";
                        } else if (literals.contains(lexerList.getVocabulary().getSymbolicName(b))) {
                            return lexerList.getVocabulary().getSymbolicName(b);
                        } else if (separators.contains(lexerList.getVocabulary().getSymbolicName(b))) {
                            return "SEPARATOR";
                        } else if (comments.contains(lexerList.getVocabulary().getSymbolicName(b))) {
                            return "COMMENT";
                        }
                    }
                }
            }

        }
        return "null";
    }

    @Override
    public Object visitStatement_9(sklLanguageParser.Statement_9Context ctx) {
        if (ctx.getText().contains("<missing ';'>"))
            mainController.appendError("Error: Missing ';' at line " + ctx.getStart().getLine());

        return visitChildren(ctx);
    }

    @Override
    public Object visitStatement_10(sklLanguageParser.Statement_10Context ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Object visitStatement_11(sklLanguageParser.Statement_11Context ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Object visitStatement_12(sklLanguageParser.Statement_12Context ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Object visitStatement_13(sklLanguageParser.Statement_13Context ctx) {
    	System.out.println("Statement 13!");
        return visitChildren(ctx);
    }

    @Override
    public Object visitCatchClause(sklLanguageParser.CatchClauseContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Object visitCatchType(sklLanguageParser.CatchTypeContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Object visitFinallyBlock(sklLanguageParser.FinallyBlockContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Object visitResourceSpecification(sklLanguageParser.ResourceSpecificationContext ctx) {
        if (ctx.getText().contains("<missing ';'>"))
            mainController.appendError("Error: Missing ';' at line " + ctx.getStart().getLine());
        return visitChildren(ctx);
    }

    @Override
    public Object visitResources(sklLanguageParser.ResourcesContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Object visitResource(sklLanguageParser.ResourceContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Object visitForControl(sklLanguageParser.ForControlContext ctx) {

        System.out.println("FORCONTROL");
        boolean valid = false;
        for (int i = 0; i < ctx.getChildCount(); i++) {
            System.out.println(ctx.getChildCount());
            System.out.println(getFlatTokenList(ctx.getChild(i)));
            List<Token> temp = getFlatTokenList(ctx.getChild(i));
            if (!stack.isEmpty()) {
                if (ctx.getChild(i) instanceof sklLanguageParser.ExpressionContext) {
                    System.out.println("ExpressionContext");
                    temp = getFlatTokenList(ctx.getChild(i));
                    Symbol paramSymbolToPass = stack.peek().getSymbolTable().get(temp.get(0).getText());
                } else if (ctx.getChild(i) instanceof sklLanguageParser.ExpressionListContext) {
                    System.out.println("ExpressionListContext");
                    temp = getFlatTokenList(ctx.getChild(i));
                    if (valid) {
                        Symbol paramSymbolToPass = stack.peek().getSymbolTable().get(temp.get(0).getText());
                        double value = (Double) paramSymbolToPass.getValue();
                        if (temp.get(1).getText().contains("+")) {
                            paramSymbolToPass.setValue(value + Double.parseDouble(temp.get(2).getText()));
                        }
                    }
                }
            }
        }
        return visitChildren(ctx);
    }

    @Override
    public Object visitForInit(sklLanguageParser.ForInitContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Object visitEnhancedForControl(sklLanguageParser.EnhancedForControlContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Object visitComparison(sklLanguageParser.ComparisonContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Object visitExpressionList(sklLanguageParser.ExpressionListContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Object visitCorrectFunc(sklLanguageParser.CorrectFuncContext ctx) {
        System.out.println("CORRECT FUNC " + ctx.getText());

        Scope passingParamStack = null;
        Scope current = null;
        Scope add = null;
        // look for the method that is being called // then store the Scope in current
        for (Scope item : scopeList) {
            if (ctx.IDENTIFIER().getText().equals(item.getName())) {
                System.out.println("=============== VISIT METHOD " + ctx.IDENTIFIER().getText() + "================");
                current = item;
                add = new Scope(ctx.IDENTIFIER().getText(), null, current.getTreeOfFunction());
                passingParamStack = stack.peek();
                stack.add(add);
                System.out.println("===========> Mult stack = " + stack.peek().hashCode());

                // TODO need to place all parameters of correct func to null
                System.out.println(current.getName() + " compare with " + add.getName());
                for (int z =0; z < current.getTreeOfFunction().getParent().getChildCount(); z++){
                    if(current.getTreeOfFunction().getParent().getChild(z) instanceof sklLanguageParser.FormalParametersContext){

                        for (int j=0; j<current.getTreeOfFunction().getParent().getChild(z).getChild(1).getChildCount(); j++){
                            if (current.getTreeOfFunction().getParent().getChild(z).getChild(1).getChild(j) instanceof sklLanguageParser.FormalParameterContext){
                                String dataT = current.getTreeOfFunction().getParent().getChild(z).getChild(1).getChild(j).getChild(0).getText();
                                String varN = current.getTreeOfFunction().getParent().getChild(z).getChild(1).getChild(j).getChild(1).getText();
                                stack.peek().pushToSymbolTable(dataT, varN, null);
                                System.out.println("===========> Inserting to stack = " + dataT + " " + varN);
                            }
                        }
                    }
                }
                printSymbolTable(stack.peek(), stack.peek().getSymbolTable());
                System.out.println("======================================================");
            }
        }

        String str = "";
        ArrayList<Object> paramContainer = new ArrayList<>();
        if (!stack.isEmpty()){
            if (ctx.getChildCount() == 3) {
                Iterator hmIterator = add.getSymbolTable().entrySet().iterator();
                System.out.println("ctx.getChildCount == 3");
                while (hmIterator.hasNext()) {
                    Map.Entry mapElement = (Map.Entry) hmIterator.next();
                    Symbol symbol = (Symbol) mapElement.getValue();
                    if (symbol.getValue() == null) {
                        System.out.println();
                        mainController.appendError("Error: Incorrect number of parameters at function call in line " + ctx.getStart().getLine());
                        return null;
                    }
                }
            } else { // if method call has a parameter

                for (int i = 0; i < ctx.getChildCount(); i++) {

                    if (ctx.getChild(i) instanceof sklLanguageParser.ExpressionListContext) {

                        for (int j = 0; j < ctx.getChild(i).getChildCount(); j++) {
                            // TODO needs to be fixed, unstable with a function
                            if (ctx.getChild(i).getChild(j) instanceof sklLanguageParser.Expression_10Context | ctx.getChild(i).getChild(j) instanceof sklLanguageParser.Expression_1Context) { // do flattokens then EvalEx concat str
                                List<Token> tempExpression = getFlatTokenList(ctx.getChild(i).getChild(j));
                                str = "";
                                System.out.println("TEMPEXPRESSION CHECKING " + tempExpression);

                                String varname = null;
                                for (int x = 0; x < tempExpression.size(); x++) {
                                    if (checkTokenType(tempExpression.get(x).getText()).equals("IDENTIFIER")) {
                                        try {
                                            varname = tempExpression.get(x).getText();
                                            str += passingParamStack.getSymbolTable().get(tempExpression.get(x).getText()).getValue();
                                        } catch (Exception e) {
                                            System.out.println("DITO KA BA SA LINE 1564");
                                            return null;
                                        }
                                    } else {
                                        str += tempExpression.get(x).getText();
                                    }
                                }
                                Object value ="";
                                System.out.println("parameter expression is " + str); // feed str to EvalEx

                                if(str.contains("\"")){// if string literal is passed
                                    value = str;
                                }else if (passingParamStack.getSymbolTable().get(varname) != null){
                                    if (passingParamStack.getSymbolTable().get(varname).getData_type().equals("String") ) {
                                        value = "\"" + str + "\"";
                                    }else if (passingParamStack.getSymbolTable().get(varname).getData_type().equals("int") ) {
                                        Expression expression = new Expression(str);
                                        BigDecimal val = expression.eval();
                                        value = val.setScale(0, BigDecimal.ROUND_FLOOR).toString();
                                    }
                                    else{
                                        Expression expression = new Expression(str);
                                        BigDecimal val = expression.eval();
                                        value = val.setScale(2, BigDecimal.ROUND_HALF_DOWN).toString();

                                    }
                                }else{// if passed is not a string literal
                                    Expression expression = new Expression(str);
                                    BigDecimal val = expression.eval();
                                    value = val.setScale(2, BigDecimal.ROUND_HALF_DOWN).toString();
                                }
                                System.out.println("COMPUTED parameter expression is " + value); // feed str to EvalEx


                                printSymbolTable(stack.peek(), stack.peek().getSymbolTable());

                                String tempVarn = stack.peek().getTreeOfFunction().getParent().getChild(2).getChild(1).getChild(j).getChild(1).getText();
                                System.out.println("tempVarn => " + tempVarn);

                                String datat = stack.peek().getSymbolTable().get(tempVarn).getData_type();
                                if (stack.peek().getSymbolTable().get(tempVarn).getData_type().equals("int")){

                                    try{


                                        value = makeInt(checkDecimal(Double.parseDouble(value.toString())));
                                        symbolHolder.add(new Symbol(datat, value, tempVarn));
                                        stack.peek().getSymbolTable().get(tempVarn).setValue(value);
                                    }
                                    catch (Exception e){
                                        mainController.appendError("Error: Parameter Data type mismatch for function call at line " + ctx.getStart().getLine());
                                    }

                                }
                                else if (stack.peek().getSymbolTable().get(tempVarn).getData_type().equals("float")){
                                    try{
                                        value = makeDouble(value);
                                        symbolHolder.add(new Symbol(datat, value, tempVarn));
                                        stack.peek().getSymbolTable().get(tempVarn).setValue(value);

                                    }
                                    catch (Exception e){
                                        mainController.appendError("Error: Parameter Data type mismatch for function call at line " + ctx.getStart().getLine());
                                    }
                                }
                                else if (stack.peek().getSymbolTable().get(tempVarn).getData_type().equals("String")){
                                    try{
                                        value = ((String) value).replaceAll("\"", " ");
                                        symbolHolder.add(new Symbol(datat, value, tempVarn));
                                        stack.peek().getSymbolTable().get(tempVarn).setValue(value);
                                    }
                                    catch (Exception e){
                                        mainController.appendError("Error: Parameter Data type mismatch for function call at line " + ctx.getStart().getLine());
                                    }
                                }
                                else if (stack.peek().getSymbolTable().get(tempVarn).getData_type().equals("char")){
                                    if(((String) value).length() > 1){
                                        mainController.appendError("Error: Parameter Data type mismatch for function call at line " + ctx.getStart().getLine());
                                    }
                                    else{
                                        value = ((String) value).charAt(1);
                                        symbolHolder.add(new Symbol(datat, value, tempVarn));
                                        stack.peek().getSymbolTable().get(tempVarn).setValue(value);


                                    }
                                }

                                printSymbolTable(stack.peek(), stack.peek().getSymbolTable());
                            }
                        }
                    }
                }
            }
        }
        
        Object ret= null;
        System.out.println("stack =  " + stack.peek().getName() + stack.peek().hashCode());
        if (stack.peek() != null) {
            printSymbolTable(stack.peek(), stack.peek().getSymbolTable());
            System.out.println(ret + "");
            ret = visit(stack.peek().getTreeOfFunction()); // visitMethodBody
            System.out.println("returned before popping stack + " + ret + "");

            System.out.println("POPPING STACK " + stack.peek().getName());
            stack.pop();
            System.out.println(stack.peek().getName());
            if (ctx.parent.parent instanceof sklLanguageParser.VariableInitializerContext) {
                if (checkTokenType(ctx.parent.parent.parent.getChild(0).getText()).equals("IDENTIFIER")) {
                    Symbol sym = stack.peek().getSymbolTable().get(ctx.parent.parent.parent.getChild(0).getText());
                }
            }
            else if (ctx.parent.parent instanceof sklLanguageParser.Expression_17Context) {
                if (checkTokenType(ctx.parent.parent.getChild(0).getText()).equals("IDENTIFIER")) {
                    Symbol sym = stack.peek().getSymbolTable().get(ctx.parent.parent.getChild(0).getText());

                    System.out.println("test now2 " + checkTokenType(ret + ""));
                    // SEMANTIC ERROR CHECKING FOR DATA TYPE MISMATCH IN ASSIGNMENT OF A FUNCTION TO A VARIABLE Ex. number a = testOne(); testOne should return a number return type to be valid
                    if (checkTokenType(ret + "").equals("DECIMAL_LITERAL")) { // object returned by the function
                        if (sym.getData_type().equals("int")) {
                            sym.setValue(ret);
                        } else {
                            mainController.appendError("Error: Function return type mismatch at line " + ctx.getStart().getLine());
                        }
                    } else if (checkTokenType(ret + "").equals("FLOAT_LITERAL")) {
                        if (sym.getData_type().equals("float")) {
                            sym.setValue(ret);

                        } else {
                            mainController.appendError("Error: Function return type mismatch at line " + ctx.getStart().getLine());
                        }
                    } else if (checkTokenType(ret + "").equals("STRING_LITERAL")) {
                        if (sym.getData_type().equals("String")) {
                            sym.setValue(ret);

                        } else {
                            mainController.appendError("Error: Function return type mismatch at line " + ctx.getStart().getLine());

                        }
                    }
                }
            }

        }

        System.out.println("RET NOW " + ret);

        return ret;
    }

    @Override
    public Object visitWrongFunc(sklLanguageParser.WrongFuncContext ctx) {
        mainController.appendError("Error: Redundant parentheses at line " + ctx.getStart().getLine());
        return visitChildren(ctx);
    }

    public boolean checkFunctionDefined(String funcName) {

        for (Scope item : scopeList) {
            if (funcName.equals(item.getName())) {
                return true;

            }
        }

        return false;

    }

    public boolean checkInGlobalVar(String varname){

        for (Symbol item : globalVar) { // searching through the globalVar stack if varname exists
            if (item.getGlobal_var_name().equals(varname)) {
                return true;
            }
        }
        return false;
    }

    public boolean checkInGlobalVarConstant(String varname){

        for (Symbol item : globalVar) { // searching through the globalVar stack if varname exists
            if (item.getGlobal_var_name().equals(varname)) {
                if(item.getData_type().contains("constant"))
                    return true;
            }
        }
        return false;
    }

    @Override
    public Object visitExpression_17(sklLanguageParser.Expression_17Context ctx) {

        System.out.println("IM IN EXPR 17 AGAIN HEHE");

        if(ctx.getChild(0) instanceof sklLanguageParser.Expression_3Context){
            System.out.println("Visit Expression 17");
            String varn = ctx.getChild(0).getChild(0).getText();
            int index = 0;
            try {
                 index = checkDecimal(Double.parseDouble(processExpressionPlease(ctx.getChild(0).getChild(2))));
            }catch (Exception e){
                mainController.appendError("Semantic Error: Invalid array index at line " +ctx.getStart().getLine());
                return null;
            }
            Object tempVal = processExpressionPlease(ctx.getChild(2));
            System.out.println(varn +" = " +index + " = " + tempVal.toString());
            Object[] tempArr = (Object[])stack.peek().getSymbolTable().get(varn).getValue();
            tempArr[index] = tempVal;
        }
        else{
            String varn = ctx.getChild(0).getText();
            Object value = processExpressionPlease(ctx.getChild(2));

            String datatype;
            try {
                datatype = stack.peek().getSymbolTable().get(varn).getData_type();
                try {
                    if(datatype.equals("int")){
                        value = makeInt(checkDecimal(Double.parseDouble(value.toString())));
                    }
                    else if(datatype.equals("float")){
                        value = makeDouble(value);
                    }
                    else if(datatype.equals("char")){
                        if (((String) value).length() > 1){
                            mainController.appendSemanticError("Semantic Error: Datatype mismatch at line " + ctx.getStart().getLine());
                        }
                        else{
                            // value = value;
                        }
                    }
                    else if(datatype.equals("String")){
//                if(newCtx.getChild(0).getText().matches("[a-zA-Z]+") || newCtx.getChild(0).getText().contains("\"")){
//
//                }
//                else{
//                    mainController.appendSemanticError("Semantic Error: Datatype mismatch at line " + ctx.getStart().getLine());
//                }
                    }
                    else{
                        mainController.appendSemanticError("Semantic Error: Cannot reassign value to constant at line " + ctx.getStart().getLine());
                    }
                }
                catch (Exception e){
                    mainController.appendSemanticError("Semantic Error: Data type mismatch at line " + ctx.getStart().getLine());
                }



                stack.peek().getSymbolTable().get(varn).setValue(value);
            }
            catch (Exception e){
                mainController.appendSemanticError("Semantic Error: Undeclared Variable at line " + ctx.getStart().getLine());
                return null;
            }
            stack.peek().getSymbolTable().get(varn).setValue(value);
        }
        return null;
    }

    public void computeAssign(sklLanguageParser.Expression_17Context ctx) {
        Object val1, val2;
        String dataType1, dataType2;

        if (checkTokenType(ctx.getChild(0).getText()).equals("IDENTIFIER")) {
            Symbol sym = stack.peek().getSymbolTable().get(ctx.getChild(0).getText());
            val1 = sym.getValue();
        } else {
            val1 = ctx.getChild(0).getText();
        }

        if (checkTokenType(ctx.getChild(2).getText()).equals("IDENTIFIER")) {
            Symbol sym = stack.peek().getSymbolTable().get(ctx.getChild(2).getText());
            val2 = sym.getValue();
        } else {
            val2 = ctx.getChild(2).getText();
        }

        stack.peek().getSymbolTable().get(ctx.getChild(0).getText()).setValue(Double.parseDouble(val1 + "") + Double.parseDouble(val2 + ""));

        if (ctx.parent.parent instanceof sklLanguageParser.ForControlContext)
            visit(ctx.parent.parent.getChild(2));
    }

    @Override
    public Object visitExpression_18(sklLanguageParser.Expression_18Context ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Object visitExpression_19(sklLanguageParser.Expression_19Context ctx) {

        System.out.println("PLEASE HELP ME " + ctx.getText());
        String varn = ctx.getChild(0).getText();
        ParseTree newCtx = ctx.getChild(2).getChild(2);
        if (!stack.isEmpty()) {

            Object tempObject;
            tempObject = processInput(ctx.getChild(2).getChild(0).getText(), processExpressionPlease(newCtx));

            System.out.println("BLEHHH" + tempObject + newCtx.getParent().getChild(0).getText());
            if (checkVariableDeclaredinScope(stack.peek(), varn, ctx.getStart().getLine())) {
                if (stack.peek().getSymbolTable().get(varn).getData_type().equals("int")) { // if var has number data type
                    try {
                        Integer.parseInt(tempObject + "");
                        stack.peek().getSymbolTable().get(varn).setValue(tempObject);
                        System.out.println("tempObject value pushed");
                    } catch (Exception e) {
                        mainController.appendError("Error: Input data type mismatch at line " + ctx.getStart().getLine());
                        return null;
                    }
                } else if (stack.peek().getSymbolTable().get(varn).getData_type().equals("float")) {
                    try {
                        Double.parseDouble(tempObject + "");
                        stack.peek().getSymbolTable().get(varn).setValue(tempObject);
                        System.out.println("tempObject value pushed");
                    } catch (Exception e) {
                        mainController.appendError("Error: Input data type mismatch at line " + ctx.getStart().getLine());
                        return null;
                    }
                } else if (stack.peek().getSymbolTable().get(varn).getData_type().equals("String")) {
                    try {
                        Double.parseDouble(tempObject + "");
                        mainController.appendError("Error: Input data type mismatch at line " + ctx.getStart().getLine());
                        return null;
                    } catch (Exception e) {
                        stack.peek().getSymbolTable().get(varn).setValue(tempObject);
                        System.out.println("tempObject value pushed");
                    }
                }
            } else {
                mainController.appendError("Undeclared Variable at line " + ctx.getStart().getLine());
            }
            printSymbolTable(stack.peek(), stack.peek().getSymbolTable());
        }
        return null;
    }

    @Override
    public Object visitExpression_1(sklLanguageParser.Expression_1Context ctx) {

        List<String> terminalNodes = new ArrayList<String>();
        for (int i = 0; i < ctx.getChildCount(); i++) {
            if (ctx.getChild(i) instanceof TerminalNode) {
                terminalNodes.add((ctx.getChild(i).getText()));
            }
        }

        return visitChildren(ctx);
    }
    
    @Override
    public Object visitExpression_2(sklLanguageParser.Expression_2Context ctx) {
        return visitChildren(ctx);
    }
    

    @Override
    public Object visitExpression_3(sklLanguageParser.Expression_3Context ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Object visitExpression_4(sklLanguageParser.Expression_4Context ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Object visitExpression_5(sklLanguageParser.Expression_5Context ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Object visitExpression_10(sklLanguageParser.Expression_10Context ctx) {


        if(ctx.getChild(0) instanceof sklLanguageParser.Expression_3Context){
            String temp = str;
            String arrIndex = processExpressionPlease(ctx.getChild(0).getChild(2));

            str = temp; // rineset kasi si str when i called process expression please


            Object[] tempArr = (Object[])stack.peek().getSymbolTable().get(ctx.getChild(0).getChild(0).getText()).getValue();
            try {
                int newIndex = checkDecimal(Double.parseDouble(arrIndex));
                System.out.println(newIndex);
                System.out.println("I JUST GOT AN ARRAY IN EXPRESSION 10" + tempArr[newIndex]);
                str += tempArr[newIndex];
            }catch (Exception e){
                System.out.println(e);
            }

        }
        else if(ctx.getChild(0) instanceof sklLanguageParser.Expression_4Context){
            System.out.println("EXPRESSION 4 CONTEXT IN EXPRESSION 10 ");
            Object getVal = null;
            String temp = str;
            System.out.println("str before method call = " + temp);
            getVal = visit(ctx.getChild(0));
            System.out.println("returned val from method call is = " + getVal);
            while(getVal == null){
                System.out.println("WAITING EXPRESSION 4 CONTEXT SA EXPRESSION 10 VISIT");
            }
            str = temp;
            System.out.println("str after method call = " + str);
            str += getVal;


            System.out.println("DONE EXPRESSION 4 CONTEXT IN EXPRESSION 10 ");


            str += ctx.getChild(1).getText();
            processChild2(ctx.getChild(2));
            return "done";

        }
        else if(ctx.getChild(0) instanceof sklLanguageParser.Expression_1Context){
            System.out.println("EXPRESSION 1 CONTEXT IN EXPRESSION 10 ");
            if(ctx.getChild(0).getChild(0) instanceof  sklLanguageParser.Primary_1Context){
                str += "(";
                Object getVal = null;
                getVal = visit(ctx.getChild(0).getChild(0).getChild(1));
                while (getVal == null){
                    System.out.println("WAITING PRIMARY 1 CONTEXT SA EXPRESSION 10 VISIT");
                }
                str += ")";
            }
            else if(ctx.getChild(0).getChild(0) instanceof sklLanguageParser.Primary_3Context){

                try {
                    if(stack.peek().getSymbolTable().get(ctx.getChild(0).getChild(0).getText()).getData_type().equals("String") || stack.peek().getSymbolTable().get(ctx.getChild(0).getChild(0).getText()).getData_type().equals("constant String")){
                        str += "\"" + stack.peek().getSymbolTable().get(ctx.getChild(0).getChild(0).getText()).getValue() + "\"";
                    }
                    else if(stack.peek().getSymbolTable().get(ctx.getChild(0).getChild(0).getText()).getData_type().equals("char") || stack.peek().getSymbolTable().get(ctx.getChild(0).getChild(0).getText()).getData_type().equals("constant char")){
                        str += "\'" + stack.peek().getSymbolTable().get(ctx.getChild(0).getChild(0).getText()).getValue() + "\'";
                    }
                    else if(stack.peek().getSymbolTable().get(ctx.getChild(0).getChild(0).getText()).getData_type().equals("int") || stack.peek().getSymbolTable().get(ctx.getChild(0).getChild(0).getText()).getData_type().equals("constant int")){
                        str += makeInt(stack.peek().getSymbolTable().get(ctx.getChild(0).getChild(0).getText()).getValue());
                    }
                    else if(stack.peek().getSymbolTable().get(ctx.getChild(0).getChild(0).getText()).getData_type().equals("float") || stack.peek().getSymbolTable().get(ctx.getChild(0).getChild(0).getText()).getData_type().equals("constant float")){
                        str += makeDouble(stack.peek().getSymbolTable().get(ctx.getChild(0).getChild(0).getText()).getValue());
                    }
                } catch (Exception ex) {
                    mainController.appendError("Semantic Error: Undeclared Variable at line " + ctx.getStart().getLine());
                }
            }
            else {
                visit(ctx.getChild(0));
                str += ctx.getChild(0).getText();
            }
            str += ctx.getChild(1).getText();
            processChild2(ctx.getChild(2));
            return "done";
        }
        else if(ctx.getChild(0) instanceof sklLanguageParser.Expression_10Context){
            Object getVal1 = null;
            getVal1 = visit(ctx.getChild(0));
            while(getVal1 == null){

            }

            str += ctx.getChild(1);
            processChild2(ctx.getChild(2));
            return "done";
        }
        return visitChildren(ctx);

    }

    @Override
    public Object visitExpression_8(sklLanguageParser.Expression_8Context ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Object visitMissingQuote(sklLanguageParser.MissingQuoteContext ctx) {

        mainController.appendError("Syntax Error: Missing Quotes at line " + ctx.getStart().getLine());
        return null;
    }

    @Override
    public Object visitPrimary_1(sklLanguageParser.Primary_1Context ctx) {

        if (ctx.getText().contains("<missing ')'>"))
            mainController.appendError("Error: Uneven parenthesis. Duplicate \"(\" at line " + ctx.getStart().getLine());

        char[] text = ctx.getText().replaceAll("[^(|^)]", "").toCharArray();
        int lparen = 0;
        int rparen = 0;

        for (int i = 0; i < text.length; i++) {
            if (text[i] == '(')
                lparen++;
            else
                rparen++;

        }

        if (lparen > rparen)
            mainController.appendError("Error: Uneven parenthesis. Duplicate \"(\" at line " + ctx.getStart().getLine());
        else if (lparen < rparen)
            mainController.appendError("Error: Uneven parenthesis. Duplicate \")\" at line " + ctx.getStart().getLine());

        return visitChildren(ctx);
    }

    @Override
    public Object visitPrimary_2(sklLanguageParser.Primary_2Context ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Object visitPrimary_3(sklLanguageParser.Primary_3Context ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Object visitLiteralError(sklLanguageParser.LiteralErrorContext ctx) {

        for (int i = 0; i < ctx.getChildCount(); i++) {
            if (ctx.getChild(i).getText().equals("++")) {
                mainController.appendError("Error: Duplicate + sign at line " + ctx.getStart().getLine());
            } else if (ctx.getChild(i).getText().equals("--")) {
                mainController.appendError("Error: Duplicate - sign at line " + ctx.getStart().getLine());
            }
        }
        return visitChildren(ctx);
    }


    @Override
    public Object visitCreator(sklLanguageParser.CreatorContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Object visitCreatedName(sklLanguageParser.CreatedNameContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Object visitArrayCreatorRest(sklLanguageParser.ArrayCreatorRestContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Object visitTypeArgumentsOrDiamond(sklLanguageParser.TypeArgumentsOrDiamondContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Object visitNonWildcardTypeArgumentsOrDiamond(sklLanguageParser.NonWildcardTypeArgumentsOrDiamondContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Object visitNonWildcardTypeArguments(sklLanguageParser.NonWildcardTypeArgumentsContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Object visitTypeArguments(sklLanguageParser.TypeArgumentsContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Object visitTypeArgument(sklLanguageParser.TypeArgumentContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Object visitTypeList(sklLanguageParser.TypeListContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Object visitTypeType(sklLanguageParser.TypeTypeContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Object visitPrimitiveType(sklLanguageParser.PrimitiveTypeContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Object visitSuperSuffix(sklLanguageParser.SuperSuffixContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Object visitArguments(sklLanguageParser.ArgumentsContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public Object visitOutputMissingQuote(sklLanguageParser.OutputMissingQuoteContext ctx) {
        mainController.appendError("Error: Missing \" at line " + ctx.getStart().getLine());
        return visitChildren(ctx);
    }

    @Override
    public Object visitOutputCorrect(sklLanguageParser.OutputCorrectContext ctx) {
        System.out.println("OUTPUT CORRECT: " + ctx.getText());
        String print = "";
        print = processExpressionPlease(ctx.getChild(2));



        System.out.println("=============DISPLAY OUTPUT TO GUI=================");
        System.out.println(print);
        mainController.appendLine(print);
        System.out.println("===================================================");
        return null;

    }

    public int makeInt(Object val){
        return Integer.parseInt(val.toString());
    }

    public double makeDouble(Object val){
        return Double.parseDouble(val.toString());
    }

    public int checkDecimal(Double val) {
        String value = Double.toString(val);
        int index = value.indexOf(".");
        if (index >= 1) {
            double newVal = Double.parseDouble(value.substring(index + 1));
            if (newVal <= 0) {
                return Integer.parseInt(value.substring(0, index));
            } else {
                double roundOff = (double) Math.round(val * 100) / 100;
                return Integer.parseInt(roundOff + "");
            }

        } else {
            return Integer.parseInt(val + "");
        }
    }

    @Override
    public Object visitOutputExtraPlus(sklLanguageParser.OutputExtraPlusContext ctx) {
        mainController.appendError("Error: Additional ‘+’ sign at line " + ctx.getStart().getLine());
        return visitChildren(ctx);
    }

    @Override
    public Object visitOutputMissingPlus(sklLanguageParser.OutputMissingPlusContext ctx) {
        int ctr = 0;
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ctr += checkQuote(ctx.getChild(i).getText());
        }
        if (ctr % 2 == 0) {
            mainController.appendError("Error: Missing + sign at line " + ctx.getStart().getLine());
        } else {
            // missing " but is shown in another error
        }

        return visitChildren(ctx);
    }

    public int checkQuote(String temp) {
        int ctr = 0;
        if (temp.charAt(0) == '"') {
            ctr++;
        }
        if (temp.charAt(temp.length() - 1) == '"') {
            ctr++;
        }
        return ctr;
    }

    @Override
    public Object visitInputCorrect(sklLanguageParser.InputCorrectContext ctx) {
//        for (int i=0; i<ctx.getChildCount(); i++){
//            System.out.println(i + " = " + ctx.getChild(i).getText());
//            if(ctx.getChild(i).getText().contains("input")){
//                processInput(ctx.getChild(i).getText());
//            }
//        }
        return visitChildren(ctx);
    }


    @Override
    public Object visitInputMissingQuote(sklLanguageParser.InputMissingQuoteContext ctx) {
        mainController.appendError("Error: Missing \" at line " + ctx.getStart().getLine());
        parseError = true;
        return null;
    }

    @Override
    public Object visitInputExtraPlus(sklLanguageParser.InputExtraPlusContext ctx) {
        System.out.println("HEEYYY MIND ME HEREEEEE");
        mainController.appendError("Error: Additional ‘+’ sign at line " + ctx.getStart().getLine());
        parseError = true;
        return null;
    }

    @Override
    public Object visitInputMissingPlus(sklLanguageParser.InputMissingPlusContext ctx) {
        mainController.appendError("Error: Missing + sign at line " + ctx.getStart().getLine());
        parseError = true;
        return null;
    }


    public void printSymbolTable(Scope scope, HashMap<String, Symbol> map) {
        System.out.println();
        System.out.println("=============== PRINT SYMBOL TABLE SCOPE:" + scope.getName() + " =================");
        for (String keys : map.keySet())
            System.out.println("[DATA TYPE] " + map.get(keys).getData_type() + " \t [VARIABLE] " + keys + " \t [VALUE] " + map.get(keys).getValue());
        System.out.println("=====================================================================");
        System.out.println();
    }

    public boolean checkVariableDeclaredinScope(Scope scope, String var_name, int line) {
        if (scope.getSymbolTable().containsKey(var_name)) {
            System.out.println("Variable '" + var_name + "' declared in scope " + scope.getName());
            return true;
        } else {
            return false;
        }
    }

    public boolean checkVariableDuplicateinGlobalVar(String var_name, int line) {

        for (Symbol item : globalVar) {
            if (item.getGlobal_var_name().equals(var_name)) {
                mainController.appendError("Error: Duplicate global variable " + var_name + " at line " + line);
                return false;
            } else {
                return true;
            }
        }
        return true;
    }

    public boolean checkVariableDuplicateinScope(Scope scope, String var_name, int line) {

        // check global variable Stack for duplicates
        for (Symbol item : globalVar) {
            if (item.getGlobal_var_name().equals(var_name)) {
                mainController.appendError("Error: Duplicate global variable " + var_name + " at line " + line);
                return false;
            }
        }

        // checks symbol table of Scope for duplicate variables.
        if (scope.getSymbolTable().containsKey(var_name)) {
            mainController.appendError("Error: Duplicate local variable " + var_name + " at line " + line);
            return false;
        } else {
            System.out.println(var_name + " IS A VALID VARIABLE NAME GOOD JOB!");
            return true;
        }
    }

    public static List<Token> getFlatTokenList(ParseTree tree) {
        List<Token> tokens = new ArrayList<Token>();
        inOrderTraversal(tokens, tree);
        if (tokens.size() == 0) {
            TerminalNode node = (TerminalNode) tree;
            tokens.add(node.getSymbol());
        }
        return tokens;
    }

    /**
     * Makes an in-order traversal over {@code parent} (recursively) collecting
     * all Tokens of the terminal nodes it encounters.
     *
     * @param tokens the list of tokens.
     * @param parent the current parent node to inspect for terminal nodes.
     */
    private static void inOrderTraversal(List<Token> tokens, ParseTree parent) {

        // Iterate over all child nodes of `parent`.
        for (int i = 0; i < parent.getChildCount(); i++) {

            // Get the i-th child node of `parent`.
            ParseTree child = parent.getChild(i);

            if (child instanceof TerminalNode) {
                // We found a leaf/terminal, add its Token to our list.
                TerminalNode node = (TerminalNode) child;
                tokens.add(node.getSymbol());
            } else {
                // No leaf/terminal node, recursively call this method.
                inOrderTraversal(tokens, child);
            }
        }
    }
}