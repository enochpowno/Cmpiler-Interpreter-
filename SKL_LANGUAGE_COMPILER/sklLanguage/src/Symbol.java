public class Symbol {

    private String data_type;
    private String global_var_name;
    private Object value;

    public Symbol(String datatype, Object val){
        data_type = datatype;
        value = val;
    }
    public Symbol(String datatype, Object val, String name){
        data_type = datatype;
        value = val;
        global_var_name = name;
    }
    
    public String getData_type() {
        return data_type;
    }

    public Object getValue() {
        return value;
    }

    public void setData_type(String data_type) {
        this.data_type = data_type;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getGlobal_var_name() {
        return global_var_name;
    }
}
