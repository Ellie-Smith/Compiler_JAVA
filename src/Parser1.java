import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Ellie
 * @date 2017/5/15
 * (备份)解析各个命令单元
 *
 */
public class Parser1 {
    private final ArrayList<Token> tokens;
    private ArrayList<Token> sentence = new ArrayList<Token>();

    //存放临时的token
    private static Map<String, String> temp_map = new HashMap<String,String>();
    private static Map<String, String> temp_map1 = new HashMap<String,String>();
    private static Map<String, String> temp_map2 = new HashMap<String,String>();

    public Parser1(ArrayList<Token> tks){
        this.tokens = tks;
    }

    /**
     * 一组tokens可能包含多条语句，需要识别出每一条语句进行解析
     */
    public Map<String, String> parse(){
        int index = 0;
        Map<String, String> result = new HashMap<String,String>();
        while (index < tokens.size()){
            if(tokens.get(index).type==Scanner.Type.EndSymbol && sentence.size()>0){
                result = parseSentence(0);
                if (result.get("error")=="SUCCESS"){
                    System.out.println(result.get("result"));
                }else{
                    System.err.println(result.get("error"));
                }
                sentence = new ArrayList<Token>();
            }
            sentence.add(tokens.get(index));
            index++;
        }

        return result;
    }

    /**
     * 解析Sentence语句
     * Sentence -> Variable = ArithExpr | Variable = [] | ArithExpr | IfSentence
     */
    public Map<String, String> parseSentence(int sent_index){
        Map<String, String> result = initResult();
        int sent_index1 = sent_index;
        String grammar_type = "";
        for(Token t:sentence){
            if (t.type==Scanner.Type.oper){
                grammar_type = "oper";
                break;
            }else if(t.type==Scanner.Type.assign_oper){
                grammar_type = "assign";
                break;
            }else if(t.value.equals("if")){
                grammar_type = "if sentence";
                break;
            }
        }

        //ArithExpr
        if(grammar_type.equals("oper") && ArithExpr(sent_index1).get("error").equals("SUCCESS")){
            return ArithExpr(sent_index1);
        }
        //IfSentence
        if(grammar_type.equals("if sentence") && IfSentence(sent_index1).get("error").equals("SUCCESS")){
            return IfSentence(sent_index1);
        }


        //Variable = []
        if(grammar_type.equals("assign") && sentence.size()-sent_index1>=3 && sentence.get(sent_index1).type == Scanner.Type.Variable &&
                sentence.get(++sent_index1).value.equals("=") &&
                sentence.get(++sent_index1).type == Scanner.Type.delete_symbol){
            System.out.println("Hello!!!!");
            compile.global_float_variable.remove(sentence.get((sent_index1-2)).value);
            result.put("error", "SUCCESS");
            result.put("result", "true");
            result.put("index", String.valueOf(sent_index1));
            return result;
        }

        //Variable = ArithExpr
        sent_index1 = sent_index;
        if(grammar_type.equals("assign") && sentence.size()-sent_index1>=3 && sentence.get(sent_index1).type == Scanner.Type.Variable &&
                sentence.get(++sent_index1).value.equals("=") && ArithExpr(++sent_index1).get("error").equals("SUCCESS")){

            String variable = sentence.get((sent_index1-2)).value;
            System.out.println("asdfa"+sent_index1);
            String value = ArithExpr(sent_index1).get("result");
            compile.global_float_variable.put(variable,Float.parseFloat(value));
            result.put("error", "SUCCESS");
            result.put("result", "true");
            result.put("index", String.valueOf(sent_index1));
            return result;
        }else{
            System.out.println("error1:  it is not a leagal sentence at index " + sent_index1);
            System.exit(0);
        }
        return result;
    }

    /**
     * IfSentence -> if ( ArithExpr ) Sentence else Sentence end
     * @param index
     * @return
     */
    private Map<String, String> IfSentence(int index){
        Map<String, String> result = initResult();
        if(sentence.get(index).value.toLowerCase().equals("if")){
            index++;
            if(sentence.get(index).value.toLowerCase().equals("(")){
                index++;
                Map<String, String> isArithExpr = ArithExpr(index);
                if(isArithExpr.get("error").equals("SUCCESS")){
                    index = Integer.parseInt(isArithExpr.get("index"));

                }else{
                    result.put("error", "expect an ArithExpr at index " + index);
                }
            }else{
                result.put("error", "expect a '(' at index " + index);
            }
        }else{
            result.put("error", "expect an 'if' at index " + index);
        }
        return result;
    }

    /**
     * ArithExpr -> Number | Variable | ( ArithExpr ) | ArithExpr + ArithExpr | ArithExpr - ArithExpr |
     * 				ArithExpr * ArithExpr | ArithExpr / ArithExpr | ArithExpr ^ ArithExpr |
     * 				- ArithExpr | BoolExpr ? ArithExpr : ArithExpr | SingleFunc | MultiFunc
     */
    private Map<String, String> ArithExpr(int index){
        this.temp_map = initResult();

//		System.out.println("hhh: "+index);

        //Number
        if((sentence.size()-index)<3){
            if(sentence.get(index).type==Scanner.Type.Number && sentence.size()-index==1){
                this.temp_map.put("result", String.valueOf(sentence.get(index).value));
                this.temp_map.put("error", "SUCCESS");
                this.temp_map.put("index", String.valueOf(++index));
                return this.temp_map;
            }
            //Variable
            else if(sentence.get(index).type==Scanner.Type.Variable && sentence.size()-index==1){
                if(compile.global_float_variable.keySet().contains(sentence.get(index).value)){
                    this.temp_map.put("result", String.valueOf(compile.global_float_variable.get(sentence.get(index).value)));
                }else{
                    System.out.println("error:  variable: '"+sentence.get(index).value + "' may not initial.");
                    System.exit(0);
                }
                this.temp_map.put("error", "SUCCESS");
                this.temp_map.put("index", String.valueOf(++index));
                return this.temp_map;
            }
            // - ArithExpr
            else if(sentence.get(index).value.equals("-")){
                index++;
                this.temp_map = ArithExpr(index);
                if(this.temp_map.get("error").equals("SUCCESS")){
                    index = Integer.parseInt(this.temp_map.get("index"));
                    this.temp_map.put("index", String.valueOf(index));
                    Double value = Double.parseDouble(this.temp_map.get("result"));
                    this.temp_map.put("result", String.valueOf(-value));
                    this.temp_map.put("error", "SUCCESS");
                }else{
                    this.temp_map.put("error", "expect an ArithExpr at index "+index);
                }
            }
        }
        // ( ArithExpr )
        else if(sentence.get(index).type==Scanner.Type.left_bracket){
            index++;
            this.temp_map = ArithExpr(index);
            if(this.temp_map.get("error").equals("SUCCESS")){
                index = Integer.parseInt(this.temp_map.get("index"));
                this.temp_map.put("index", String.valueOf(++index));
                if (sentence.get(index).type != Scanner.Type.right_bracket) {
                    System.out.println("error:  expect a '(' at index "+index);
                    System.exit(0);
                }
                Double value = Double.parseDouble(this.temp_map.get("result"));
                this.temp_map.put("result", String.valueOf(value));
                this.temp_map.put("error", "SUCCESS");
            }else{
                System.out.println("error1:  expect an ArithExpr at index "+index);
                System.exit(0);
            }
        }
        // Number|Variable oper ArithExpr
        if((sentence.size()-index)<3 && (sentence.get(index).type==Scanner.Type.Number || sentence.get(index).type==Scanner.Type.Variable)){
            index++;
            if(sentence.get(index).type == Scanner.Type.oper){
                String oper = sentence.get(index).value;
                index++;
                this.temp_map2 = ArithExpr(index);
                if(this.temp_map2.get("error").equals("SUCCESS")){
                    index = Integer.parseInt(this.temp_map2.get("index"));
                    this.temp_map.put("index", String.valueOf(index));

                    Double value1 = Double.parseDouble(this.temp_map1.get("result"));
                    Double value2 = Double.parseDouble(this.temp_map2.get("result"));
                    if(oper.equals("+")){
                        this.temp_map.put("result", String.valueOf(value1+value2));
                        this.temp_map.put("error", "SUCCESS");
                    }else if(oper.equals("-")){
                        this.temp_map.put("result", String.valueOf(value1-value2));
                        this.temp_map.put("error", "SUCCESS");
                    }else if (oper.equals("*")){
                        this.temp_map.put("result", String.valueOf(value1*value2));
                        this.temp_map.put("error", "SUCCESS");
                    }else if(oper.equals("/")){
                        if (value2 == 0) {
                            System.out.println("error:  Can not divide by zero at index" + index);
                            System.exit(0);
                        }
                        this.temp_map.put("result", String.valueOf(value1/value2));
                        this.temp_map.put("error", "SUCCESS");
                    }else{
                        System.out.println("error:  oper not found at index" + index);
                        System.exit(0);
                    }
                }else{
                    System.out.println("error:  expect an ArithExpr at index "+index);
                    System.exit(0);
                }
            }else{
                System.out.println("error2:  expect an oper at index "+index);
                System.exit(0);
            }
        }

        //  SingleFunc
        else if(SingleFunc(index).get("error")=="SUCCESS"){
            return SingleFunc(index);
        }
        //  MultiFunc
        else if(MultipleFunc(index).get("error")=="SUCCESS"){
            return MultipleFunc(index);
        }else{
            System.out.println("error:  expect an ArithExpr at index "+index);
            System.exit(0);
        }
        return this.temp_map;
    }

    /**
     * SingleFunc -> type.single_function ( ArithExpr )
     */
    private Map<String, String> SingleFunc(int index) {
        Map<String, String> result = initResult();
        String func = sentence.get(index).value;
        if(sentence.get(++index).type == Scanner.Type.left_bracket){
            index++;
            Map<String, String> isArithExpr = ArithExpr(index);
            if(isArithExpr.get("error").equals("SUCCESS")){
                index = Integer.parseInt(isArithExpr.get("index"));
                result.put("index", String.valueOf(++index));
                if (sentence.get(index).type != Scanner.Type.right_bracket) {
                    System.out.println("error:  expect a ')' at index "+index);
                    System.exit(0);
                }
                Double value = Double.parseDouble(isArithExpr.get("result"));
                if(func.equals("cos")){
                    result.put("result", String.valueOf(Math.cos(value)));
                    result.put("error", "SUCCESS");
                }else{
                    result.put("result", String.valueOf(Math.sin(value)));
                    result.put("error", "SUCCESS");
                }
            }else{
                System.out.println("error:  expect an ArithExpr at index "+index);
                System.exit(0);
            }
        }else{
            System.out.println("error:  expect a '(' at index "+index);
            System.exit(0);
        }
        return result;
    }

    /**
     * MultipleFunc -> type.multiple_function ( ArithExpr, ArithExpr )
     */
    private Map<String, String> MultipleFunc(int index){
        Map<String, String> result = initResult();
        String func = sentence.get(index).value;
        if(sentence.get(++index).type == Scanner.Type.left_bracket){
            index++;
            Map<String, String> isArithExpr1 = ArithExpr(index);
            if(isArithExpr1.get("error").equals("SUCCESS")){
                index = Integer.parseInt(isArithExpr1.get("index"));
                if(sentence.get(index).type == Scanner.Type.comma){
                    index++;
                    Map<String, String> isArithExpr2 = ArithExpr(index);
                    if(isArithExpr2.get("error").equals("SUCCESS")){
                        index = Integer.parseInt(isArithExpr1.get("index"));
                        result.put("index", String.valueOf(++index));
                        if (sentence.get(index).type != Scanner.Type.right_bracket) {
                            System.out.println("error:  expect a ')' at index "+index);
                            System.exit(0);
                        }
                        Double value1 = Double.parseDouble(isArithExpr1.get("result"));
                        Double value2 = Double.parseDouble(isArithExpr2.get("result"));
                        if(func.equals("max")){
                            result.put("result", value1>value2?String.valueOf(value1):String.valueOf(value2));
                            result.put("error", "SUCCESS");
                        }else{
                            result.put("result", value1>value2?String.valueOf(value2):String.valueOf(value1));
                            result.put("error", "SUCCESS");
                        }
                    }else{
                        System.out.println("error:  expect an ArithExpr at index "+index);
                        System.exit(0);
                    }
                }else{
                    System.out.println("error:  expect a ',' at index "+index);
                    System.exit(0);
                }
            }else{
                System.out.println("error:  expect an ArithExpr at index "+index);
                System.exit(0);
            }
        }else{
            System.out.println("error:  expect a '(' at index "+index);
            System.exit(0);
        }
        return result;
    }


    /**
     * BoolExpr -> true | false | ( BoolExpr ) | ArithExpr == ArithExpr | ArithExpr != ArithExpr |
     * 			   ArithExpr < ArithExpr | ArithExpr > ArithExpr | ArithExpr <= ArithExpr |
     * 			   ArithExpr >= ArithExpr | ArithExpr && ArithExpr | ArithExpr || ArithExpr |
     * 			   ! BoolExpr
     */
    private Map<String, String> BoolExpr(int index){
        this.temp_map = initResult();
        if(sentence.get(index).type==Scanner.Type.Bool){
            this.temp_map.put("result", sentence.get(index).value);
            this.temp_map.put("error", "SUCCESS");
            this.temp_map.put("index", String.valueOf(++index));
            return this.temp_map;
        }
        // ( BoolExpr )
        else if(sentence.get(index).type == Scanner.Type.left_bracket){
            index++;
            this.temp_map = BoolExpr(index);
            if(this.temp_map.get("error").equals("SUCCESS")){
                index = Integer.parseInt(this.temp_map.get("index"));
                this.temp_map.put("index", String.valueOf(++index));
                if (sentence.get(index).type != Scanner.Type.right_bracket) {
                    this.temp_map.put("error", "expect a ')' at index "+index);
                    return this.temp_map;
                }
                String value = this.temp_map.get("result");
                this.temp_map.put("result", String.valueOf(value));
                this.temp_map.put("error", "SUCCESS");
            }else{
                System.out.println("error:  expect a BoolExpr at index "+index);
                System.exit(0);
            }
        }
        // ArithExpr compare_oper/bool_oper ArithExpr
        else if(ArithExpr(index).get("error")=="SUCCESS"){
            index++;
            this.temp_map1 = ArithExpr(index);
            if(this.temp_map1.get("error").equals("SUCCESS")){
                index = Integer.parseInt(this.temp_map1.get("index"));

                if(sentence.get(index).type == Scanner.Type.compare_oper){
                    String oper = sentence.get(index).value;
                    index++;
                    this.temp_map2 = ArithExpr(index);
                    if(this.temp_map2.get("error").equals("SUCCESS")){
                        index = Integer.parseInt(this.temp_map2.get("index"));
                        this.temp_map.put("index", String.valueOf(index));
                        Double value1 = Double.parseDouble(this.temp_map1.get("result"));
                        Double value2 = Double.parseDouble(this.temp_map2.get("result"));
                        if(oper.equals(">")){
                            this.temp_map.put("result", String.valueOf(value1>value2));
                            this.temp_map.put("error", "SUCCESS");
                        }else if(oper.equals("<")){
                            this.temp_map.put("result", String.valueOf(value1<value2));
                            this.temp_map.put("error", "SUCCESS");
                        }else if (oper.equals("<=")){
                            this.temp_map.put("result", String.valueOf(value1<=value2));
                            this.temp_map.put("error", "SUCCESS");
                        }else if(oper.equals(">=")){
                            this.temp_map.put("result", String.valueOf(value1>=value2));
                            this.temp_map.put("error", "SUCCESS");
                        }else{
                            System.out.println("error:  oper not found at index" + index);
                            System.exit(0);
                        }
                    }
                    else{
                        System.out.println("error:  expect an BoolExpr at index "+index);
                        System.exit(0);
                    }
                }else if(sentence.get(index).type == Scanner.Type.bool_oper){
                    String oper = sentence.get(index).value;
                    index++;
                    this.temp_map2 = ArithExpr(index);
                    if(this.temp_map2.get("error").equals("SUCCESS")){
                        index = Integer.parseInt(this.temp_map2.get("index"));
                        this.temp_map.put("index", String.valueOf(index));
                        Boolean value1 = this.temp_map1.get("result").toLowerCase().equals("true")?true:false;
                        Boolean value2 = this.temp_map2.get("result").toLowerCase().equals("true")?true:false;
                        if(oper.equals("&&")){
                            this.temp_map.put("result", String.valueOf(value1&&value2));
                            this.temp_map.put("error", "SUCCESS");
                        }else if(oper.equals("||")){
                            this.temp_map.put("result", String.valueOf(value1||value2));
                            this.temp_map.put("error", "SUCCESS");
                        }else{
                            System.out.println("error:  oper not found at index" + index);
                            System.exit(0);
                        }
                    }
                }else{
                    System.out.println("error:  expect an oper at index "+index);
                    System.exit(0);
                }
            }else{
                System.out.println("error:  expect an BoolExpr at index "+index);
                System.exit(0);
            }
        }
        // ! BoolExpr
        else if(sentence.get(index).value.equals("!")){
            index++;
            this.temp_map = BoolExpr(index);
            if(this.temp_map.get("error").equals("SUCCESS")){
                index = Integer.parseInt(this.temp_map.get("index"));
                this.temp_map.put("index", String.valueOf(index));
                String value = this.temp_map.get("result");
                if(value.toLowerCase().equals("true")){
                    value="false";
                }else{
                    value="true";
                }
                this.temp_map.put("result", String.valueOf(value));
                this.temp_map.put("error", "SUCCESS");
            }else{
                System.out.println("error:  expect an BoolExpr at index "+index);
                System.exit(0);
            }
        }else{
            System.out.println("error:  expect a BoolExpr at index "+index);
            System.exit(0);
        }

        return this.temp_map;
    }

    private Map<String, String> initResult(){
        return new HashMap<String,String>(){{put("result", "");put("error", "");put("index", "");}};
    }
}

