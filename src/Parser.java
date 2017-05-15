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

	private Map<String, String> ArithExpr(int index){
		return null;
	}




	private Map<String, String> initResult(){
		return new HashMap<String,String>(){{put("result", "");put("error", "");put("index", "");}};
	}
}
