import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.HashMap;

public class Scope {


    private String name;
    private HashMap<String, Symbol> symbolTable;
    private String parent; //parent scope
    private RuleContext caller;
    private ParseTree treeOfFunction;
    private Object returnValue;
    private String returnType;

    public Scope(String name, String parent, ParseTree tree){
        this.name = name;
        symbolTable = new HashMap<String, Symbol>();
        this.parent = parent;
        treeOfFunction = tree;
    }

    public void pushToSymbolTable(String data_type, String var_name, Object val){
        Symbol sym = new Symbol(data_type, val);

        symbolTable.put(var_name, sym);
    }

    public HashMap<String, Symbol> getSymbolTable() {
        return symbolTable;
    }

    public String getName() {
        return name;
    }

    public ParseTree getTreeOfFunction() {
        return treeOfFunction;
    }

    public void setSymbolTable(HashMap<String, Symbol> symbolTable) {
        this.symbolTable = symbolTable;
    }

    public void setTreeOfFunction(ParseTree treeOfFunction) {
        this.treeOfFunction = treeOfFunction;
    }

    public Object getReturnValue() {
        return returnValue;
    }

    public void setReturnValue(Object returnValue) {
        this.returnValue = returnValue;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }
}
