import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author Ellie
 * @date 2017/5/1
 * 解析各个命令单元
 *
 */
public class Parser {
	private final ArrayList<Token> tokens;
	private ArrayList<Token> sentence = new ArrayList<Token>();
	
	public Parser(ArrayList<Token> tks){
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
		
		//ArithExpr
		if(ArithExpr(sent_index).get("error").equals("SUCCESS")){
			return ArithExpr(sent_index);
		}
		//IfSentence
		else if(IfSentence(sent_index).get("error").equals("SUCCESS")){
			return IfSentence(sent_index);
		}
		//Variable = []
		else if(sentence.size()-sent_index>=3 && sentence.get(sent_index).type == Scanner.Type.Variable &&
				sentence.get(++sent_index).value == "=" &&
				sentence.get(++sent_index).type == Scanner.Type.delete_symbol){
			compile.global_float_variable.remove(sentence.get((sent_index-2)).value);
			result.put("error", "SUCCESS");
			result.put("result", "true");
			result.put("index", String.valueOf(sent_index));
			return result;
		}
		//Variable = ArithExpr
		else if(sentence.size()-sent_index>=3 && sentence.get(sent_index).type == Scanner.Type.Variable &&
				sentence.get(++sent_index).value == "=" && ArithExpr(++sent_index).get("error").equals("SUCCESS")){

			String variable = sentence.get((sent_index-2)).value;
			String value = ArithExpr(sent_index).get("result");
			compile.global_float_variable.put(variable,Float.parseFloat(value));
			result.put("error", "SUCCESS");
			result.put("result", "true");
			result.put("index", String.valueOf(sent_index));
			return result;
		}else{
				result.put("error", "it is not a leagal sentence at index " + sent_index);
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
		Map<String, String> result = initResult();
		
		//Number
		if(sentence.size()<3){
			if(sentence.get(index).type==Scanner.Type.Number && sentence.size()-index==1){
				result.put("result", String.valueOf(sentence.get(index).value));
				result.put("error", "SUCCESS");
				result.put("index", String.valueOf(++index));
				return result;
			}
			//Variable
			else if(sentence.get(index).type==Scanner.Type.Variable && sentence.size()-index==1){
				if(compile.global_float_variable.keySet().contains(sentence.get(index).value)){
					result.put("result", String.valueOf(compile.global_float_variable.get(sentence.get(index).value)));
				}else{
					result.put("error", "variable: '"+sentence.get(index).value + "' may not initial.");
					return result;
				}
				result.put("error", "SUCCESS");
				result.put("index", String.valueOf(++index));
				return result;
			}
			// - ArithExpr
			else if(sentence.get(index).value=="-"){
				index++;
				Map<String, String> isArithExpr = ArithExpr(index);
				if(isArithExpr.get("error").equals("SUCCESS")){
					index = Integer.parseInt(isArithExpr.get("index"));
					result.put("index", String.valueOf(index));
					Double value = Double.parseDouble(isArithExpr.get("result"));
					result.put("result", String.valueOf(-value));
					result.put("error", "SUCCESS");
				}else{
					result.put("error", "expect an ArithExpr at index "+index);
				}
			}
		}
		// ( ArithExpr ) 
		else if(sentence.get(index).type==Scanner.Type.left_bracket){
			index++;
			Map<String, String> isArithExpr = ArithExpr(index);
			if(isArithExpr.get("error").equals("SUCCESS")){
				index = Integer.parseInt(isArithExpr.get("index"));
				result.put("index", String.valueOf(++index));
				if (sentence.get(index).type != Scanner.Type.right_bracket) {
					result.put("error", "expect a '(' at index "+index);
					return result;
				}
				Double value = Double.parseDouble(isArithExpr.get("result"));
				result.put("result", String.valueOf(value));
				result.put("error", "SUCCESS");
			}else{
				result.put("error", "expect an ArithExpr at index "+index);
			}
		}
		// ArithExpr oper ArithExpr
		else if(ArithExpr(index).get("error")=="SUCCESS"){
			index++;
			Map<String, String> isArithExpr1 = ArithExpr(index);
			if(isArithExpr1.get("error").equals("SUCCESS")){
				index = Integer.parseInt(isArithExpr1.get("index"));
				if(sentence.get(index).type == Scanner.Type.oper){
					String oper = sentence.get(index).value;
					index++;
					Map<String, String> isArithExpr2 = ArithExpr(index);
					if(isArithExpr2.get("error").equals("SUCCESS")){
						index = Integer.parseInt(isArithExpr1.get("index"));
						result.put("index", String.valueOf(index));
						
						Double value1 = Double.parseDouble(isArithExpr1.get("result"));
						Double value2 = Double.parseDouble(isArithExpr2.get("result"));
						if(oper.equals("+")){
							result.put("result", String.valueOf(value1+value2));
							result.put("error", "SUCCESS");
						}else if(oper.equals("-")){
							result.put("result", String.valueOf(value1-value2));
							result.put("error", "SUCCESS");
						}else if (oper.equals("*")){
							result.put("result", String.valueOf(value1*value2));
							result.put("error", "SUCCESS");
						}else if(oper.equals("/")){
							if (value2 == 0) {
								result.put("error", "Can not divide by zero at index" + index);
								return result;
							}
							result.put("result", String.valueOf(value1/value2));
							result.put("error", "SUCCESS");
						}else{
							result.put("error", "oper not found at index" + index);
						}
					}else{
						result.put("error", "expect an ArithExpr at index "+index);
					}
				}else{
					result.put("error", "expect an oper at index "+index);
				}
			}else{
				result.put("error", "expect an ArithExpr at index "+index);
			}
		}
		// BoolExpr ? ArithExpr : ArithExpr
		else if(BoolExpr(index).get("error")=="SUCCESS"){
			String condition = BoolExpr(index).get("result");
			index++;
			if(sentence.get(index).value=="?"){
				Map<String, String> isArithExpr1 = ArithExpr(index);
				if (isArithExpr1.get("error").equals("SUCCESS")) {
					index = Integer.parseInt(isArithExpr1.get("index"));
					if (sentence.get(index).type == Scanner.Type.colon) {
						index++;
						Map<String, String> isArithExpr2 = ArithExpr(index);
						if (isArithExpr2.get("error").equals("SUCCESS")) {
							index = Integer.parseInt(isArithExpr1.get("index"));
							result.put("index", String.valueOf(index));

							Double value1 = Double.parseDouble(isArithExpr1.get("result"));
							Double value2 = Double.parseDouble(isArithExpr2.get("result"));
							if (condition.toLowerCase().equals("true")) {
								result.put("result", String.valueOf(value1));
								result.put("error", "SUCCESS");
							} else {
								result.put("result", String.valueOf(value2));
								result.put("error", "SUCCESS");
							}
						} else {
							result.put("error", "expect an ArithExpr at index " + index);
						}
					} else {
						result.put("error", "expect a ':' at index " + index);
					}
				} else {
					result.put("error", "expect an ArithExpr at index " + index);
				}
			}else{
				result.put("error", "expect a '?' at index "+index);
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
			result.put("error", "expect an ArithExpr at index "+index);
		}
		return result;
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
					result.put("error", "expect a ')' at index "+index);
					return result;
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
				result.put("error", "expect an ArithExpr at index "+index);
			}
		}else{
			result.put("error", "expect a '(' at index "+index);
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
							result.put("error", "expect a ')' at index "+index);
							return result;
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
						result.put("error", "expect an ArithExpr at index "+index);
					}
				}else{
					result.put("error", "expect a ',' at index "+index);
				}
			}else{
				result.put("error", "expect an ArithExpr at index "+index);
			}
		}else{
			result.put("error", "expect a '(' at index "+index);
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
		Map<String, String> result = initResult();
		if(sentence.get(index).type==Scanner.Type.Bool){
			result.put("result", sentence.get(index).value);
			result.put("error", "SUCCESS");
			result.put("index", String.valueOf(++index));
			return result;
		}
		// ( BoolExpr )
		else if(sentence.get(index).type == Scanner.Type.left_bracket){
				index++;
				Map<String, String> isBoolExpr = BoolExpr(index);
				if(isBoolExpr.get("error").equals("SUCCESS")){
					index = Integer.parseInt(isBoolExpr.get("index"));
					result.put("index", String.valueOf(++index));
					if (sentence.get(index).type != Scanner.Type.right_bracket) {
						result.put("error", "expect a ')' at index "+index);
						return result;
					}
					String value = isBoolExpr.get("result");
					result.put("result", String.valueOf(value));
					result.put("error", "SUCCESS");
				}else{
					result.put("error", "expect a BoolExpr at index "+index);
				}
		}
		// ArithExpr compare_oper/bool_oper ArithExpr 
		else if(BoolExpr(index).get("error")=="SUCCESS"){
			index++;
			Map<String, String> isBoolExpr1 = BoolExpr(index);
			if(isBoolExpr1.get("error").equals("SUCCESS")){
				index = Integer.parseInt(isBoolExpr1.get("index"));
				
				if(sentence.get(index).type == Scanner.Type.compare_oper){
					String oper = sentence.get(index).value;
					index++;
					Map<String, String> isBoolExpr2 = ArithExpr(index);
					if(isBoolExpr2.get("error").equals("SUCCESS")){
						index = Integer.parseInt(isBoolExpr2.get("index"));
						result.put("index", String.valueOf(index));
						Double value1 = Double.parseDouble(isBoolExpr1.get("result"));
						Double value2 = Double.parseDouble(isBoolExpr2.get("result"));
						if(oper.equals(">")){
							result.put("result", String.valueOf(value1>value2));
							result.put("error", "SUCCESS");
						}else if(oper.equals("<")){
							result.put("result", String.valueOf(value1<value2));
							result.put("error", "SUCCESS");
						}else if (oper.equals("<=")){
							result.put("result", String.valueOf(value1<=value2));
							result.put("error", "SUCCESS");
						}else if(oper.equals(">=")){
							result.put("result", String.valueOf(value1>=value2));
							result.put("error", "SUCCESS");
						}else{
							result.put("error", "oper not found at index" + index);
						}
					}
					else{
						result.put("error", "expect an BoolExpr at index "+index);
					}
				}else if(sentence.get(index).type == Scanner.Type.bool_oper){
					String oper = sentence.get(index).value;
					index++;
					Map<String, String> isBoolExpr2 = ArithExpr(index);
					if(isBoolExpr2.get("error").equals("SUCCESS")){
						index = Integer.parseInt(isBoolExpr2.get("index"));
						result.put("index", String.valueOf(index));
						Boolean value1 = isBoolExpr1.get("result").toLowerCase().equals("true")?true:false;
						Boolean value2 = isBoolExpr2.get("result").toLowerCase().equals("true")?true:false;
						if(oper.equals("&&")){
							result.put("result", String.valueOf(value1&&value2));
							result.put("error", "SUCCESS");
						}else if(oper.equals("||")){
							result.put("result", String.valueOf(value1||value2));
							result.put("error", "SUCCESS");
						}else{
							result.put("error", "oper not found at index" + index);
						}
					}
				}else{
					result.put("error", "expect an oper at index "+index);
				}
			}else{
				result.put("error", "expect an BoolExpr at index "+index);
			}
		}
		// ! BoolExpr
		else if(sentence.get(index).value=="!"){
			index++;
			Map<String, String> isBoolExpr = BoolExpr(index);
			if(isBoolExpr.get("error").equals("SUCCESS")){
				index = Integer.parseInt(isBoolExpr.get("index"));
				result.put("index", String.valueOf(index));
				String value = isBoolExpr.get("result");
				if(value.toLowerCase().equals("true")){
					value="false";
				}else{
					value="true";
				}
				result.put("result", String.valueOf(value));
				result.put("error", "SUCCESS");
			}else{
				result.put("error", "expect an BoolExpr at index "+index);
			}
		}else{
			result.put("error", "expect a BoolExpr at index "+index);
		}
		
		return result;
	}
	
	private Map<String, String> initResult(){
		Map<String, String> result = new HashMap<String,String>();
		result.put("result", "");
		result.put("error", "");
		result.put("index", "");
		return result;
	}
}
