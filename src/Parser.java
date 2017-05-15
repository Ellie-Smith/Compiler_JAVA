import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.lmz.RPN.Calculator2;

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

	//存放临时的token
	private static Map<String, String> temp_map = new HashMap<String,String>();
	private static Map<String, String> temp_map1 = new HashMap<String,String>();
	private static Map<String, String> temp_map2 = new HashMap<String,String>();
	
	public Parser(ArrayList<Token> tks){
		this.tokens = tks;
	}
	
	/**
	 * 一组tokens可能包含多条语句，需要识别出每一条语句进行解析
	 */
	public Map<String, String> parse() throws Exception{
		int index = 0;
		Map<String, String> result = new HashMap<String,String>();
		while (index < tokens.size()){
			if(tokens.get(index).type==Scanner.Type.EndSymbol && sentence.size()>0){
				result = parseSentence(0);
				if (result.get("error")=="SUCCESS" && result.get("execute_result").equals("")){
					System.out.println("-------------------------------------------");
					System.out.println("----  variable  ----  |  ----  value  ----");
					for(String key:compile.global_float_variable.keySet()){
						System.out.println("      "+key+"      |      "+compile.global_float_variable.get(key));
					}
					System.out.println("-------------------------------------------");
					System.out.println(result.get("result"));
				}else if(result.get("error")=="SUCCESS"){
					System.out.println("-------------------------------------------");
					System.out.println("----  variable  ----  |  ----  value  ----");
					for(String key:compile.global_float_variable.keySet()){
						System.out.println("      "+key+"      |      "+compile.global_float_variable.get(key));
					}
					System.out.println("-------------------------------------------");
					System.out.println(result.get("execute_result"));
				}else{
					System.out.println("-------------------------------------------");
					System.out.println("----  variable  ----  |  ----  value  ----");
					for(String key:compile.global_float_variable.keySet()){
						System.out.println("      "+key+"      |      "+compile.global_float_variable.get(key));
					}
					System.out.println("-------------------------------------------");
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
	public Map<String, String> parseSentence(int sent_index) throws Exception{
		Map<String, String> result = initResult();
		if(sentence.size()-sent_index==1){
			if (sentence.get(sent_index).type==Scanner.Type.Number){
				result.put("result",sentence.get(sent_index).value);
				result.put("error","SUCCESS");
			}else if (sentence.get(sent_index).type==Scanner.Type.Variable){
				result.put("result",String.valueOf(compile.global_float_variable.get(sentence.get(sent_index).value)));
				result.put("error","SUCCESS");
			}else if (sentence.get(sent_index).type==Scanner.Type.EndSymbol){
				result.put("result","");
				result.put("error","SUCCESS");
			}else{
				System.out.println("error:  it is not a leagal sentence at index " + sent_index);
				System.exit(0);
			}
			return result;
		}

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
		if(grammar_type.equals("if sentence")){
			return IfSentence(sent_index1);
		}


		//Variable = []
		if(grammar_type.equals("assign") && sentence.size()-sent_index1>=3 && sentence.get(sent_index1).type == Scanner.Type.Variable &&
				sentence.get(++sent_index1).value.equals("=") &&
				sentence.get(++sent_index1).type == Scanner.Type.delete_symbol){

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

			String value = ArithExpr(sent_index1).get("result");
			String execute_result = ArithExpr(sent_index1).get("execute_result");
			compile.global_float_variable.put(variable,Float.parseFloat(value));
			result.put("error", "SUCCESS");
			result.put("result", value);
			result.put("execute_result",execute_result);
			result.put("index", String.valueOf(sent_index1));
			return result;
		}else{
				for(Token t:sentence){
					System.out.print(t.value+" ");
				}
				System.out.println("error1:  it is not a leagal sentence at index " + sent_index1);
				System.exit(0);
		}
		return result;
	}
	
	/**
	 * IfSentence -> if ( BoolExpr ) Sentence else Sentence end
	 * @param index
	 * @return
	 */
	private Map<String, String> IfSentence(int index) throws Exception{
		this.temp_map = initResult();
		if(sentence.get(index).value.toLowerCase().equals("if")){
			index++;
			if(sentence.get(index).value.toLowerCase().equals("(")){
				index++;
				this.temp_map1 = BoolExpr(index);
				if(this.temp_map1.get("error").equals("SUCCESS")){
					if(this.temp_map1.get("value").toLowerCase().equals("true")){
						//sentence切片
						sentence = slice(sentence,to_index(index,")"),to_index(index,"else")-1);
						this.temp_map = parseSentence(0);
						return this.temp_map;
					}else{
						index = to_index(index,"else");
						//sentence切片
						sentence = slice(sentence,index,to_index(index,"end")-1);
						this.temp_map = parseSentence(0);
						return this.temp_map;
					}
				}else{
					this.temp_map.put("error", "expect an BoolExpr at index " + index);
				}
			}else{
				this.temp_map.put("error", "expect a '(' at index " + index);
			}
		}else{
			this.temp_map.put("error", "expect an 'if' at index " + index);
		}
		return this.temp_map;
	}

	/**
	 * 算术表达式
	 * @param index
	 * @return
	 * @throws Exception
	 */
	private Map<String, String> ArithExpr(int index) throws Exception{
		this.temp_map = initResult();
		String s = "";
		while(index < sentence.size() && sentence.get(index).type != Scanner.Type.EndSymbol && sentence.get(index).type != Scanner.Type.if_grammar
				&& sentence.get(index).type != Scanner.Type.compare_oper && sentence.get(index).type != Scanner.Type.right_bracket){
			if (sentence.get(index).type == Scanner.Type.Variable){
				try{
					if (compile.global_float_variable.keySet().contains(sentence.get(index).value)){
						s += compile.global_float_variable.get(sentence.get(index).value);
					}else {
						s += 0;
						this.temp_map.put("execute_result",sentence.get(index).value+" may not initial.");
					}
				}catch (Exception e){
					e.printStackTrace();
				}
			}else{
				s += sentence.get(index).value;
			}
			index++;
		}
		this.temp_map.put("result",String.valueOf(Calculator2(s)));
		this.temp_map.put("error","SUCCESS");
		this.temp_map.put("index",String.valueOf(index));
		return this.temp_map;
	}

	/**
	 * 布尔表达式
	 * @param index
	 * @return
	 * @throws Exception(只实现了 Number == )
	 */
	private Map<String, String> BoolExpr(int index) throws Exception{
		this.temp_map = initResult();
		String s = "";
		Map<String,String> left = ArithExpr(index);
		if (!left.get("error").equals("SUCCESS")){
			this.temp_map.put("error","expect an ArithExpr at index " + index);
			return this.temp_map;
		}else if(left.get("error").equals("SUCCESS") && !left.get("execute_result").equals("")){
			this.temp_map.put("error","variable may not initialize.");
			return this.temp_map;
		}
		index = Integer.parseInt(left.get("index"));
		if(sentence.get(index).type == Scanner.Type.compare_oper){
			index++;
			Map<String,String> right = ArithExpr(index);
			if (!right.get("error").equals("SUCCESS")){
				this.temp_map.put("error","expect an ArithExpr at index " + index);
			}else if(right.get("error").equals("SUCCESS") && !right.get("execute_result").equals("")){
				this.temp_map.put("error","variable may not initialize.");
			}else{

				if (left.get("result").equals(right.get("result"))){
					this.temp_map.put("value","true");
					this.temp_map.put("error","SUCCESS");
				}else{
					this.temp_map.put("value","false");
					this.temp_map.put("error","SUCCESS");
				}

			}

		}else{
			this.temp_map.put("error","it may requre a compare oper at "+ index);
		}


		return this.temp_map;
	}


	private int to_index(int index,String s){
		while(!sentence.get(index).value.toLowerCase().equals(s)){
			index++;
		}
		return ++index;
	}

	/**
	 * ArrayList切片
	 */
	private ArrayList<Token> slice(ArrayList<Token> s, int begin, int end){
		ArrayList<Token> slice = new ArrayList<Token>();
		while(begin < end){
			slice.add(s.get(begin));
			begin++;
		}
		return slice;
	}

	private Map<String, String> initResult(){
		return new HashMap<String,String>(){{put("result", "");put("error", "");put("index", "");put("execute_result","");}};
	}
}
