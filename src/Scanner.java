import java.util.ArrayList;

/**
 * 
 * @author Ellie
 * @date 2017/5/1
 * 扫描语句，将输入流转换成命令单元:
 * 例如：输入-- a = 1
 * 		 输出-- variable  assign_oper  number
 *
 */
public class Scanner {
	/**
	 * Variable:变量名
	 * if_grammar:  if、else、elif、end
	 * Bool: True or False
	 * EndSymbol:终结符“;”
	 * Space:空格
	 * Number:数值型，不区分整型和浮点型
	 * oper:运算操作符(+、-、*、/、^)
	 * compare_oper:比较操作符(==、!=、>、<、>=、<=、!)
	 * assign_oper:赋值操作符(=)
	 * bool_oper:逻辑比较符
	 * String:字符串
	 * delete_symbol: []
	 * bracket:括号
	 * single_function:函数(sin、cos)
	 * mutiple_function:函数(max、min)
	 * colon: ":"
	 * question_mark: "?"
	 * exclamatory_mark: "!"
	 * comma: ","
	 */
	public static enum Type { 
		Variable, if_grammar, Bool, EndSymbol, Space, Number,
		oper, compare_oper, assign_oper, String, delete_symbol,
		bool_oper, left_bracket, right_bracket, single_function,
		multiple_function, colon, question_mark, exclamatory_mark, comma; 
	}
	
	private static ArrayList<Token> tks = new ArrayList<Token>();
	private String token = "";
	private Type curType = Type.Space;
	
	public ArrayList<Token> Scan(Source source) throws Exception{
		char curChar = source.currentChar();
		while(true){

			/**
			 * @判断是否是Variable(同时也可能为if_grammar、Bool、EndSymbol)
			 * Variable -> letter(suffix | null)
			 * suffix -> (letter | digit | _ )+
			 * letter -> A-Z | a-z
			 * digit -> 0-9
			 */
			if((curChar>='a' && curChar<='z') || (curChar>='A' && curChar<='Z')){
				while((curChar>='a' && curChar<='z') || (curChar>='A' && curChar<='Z') 
						|| (curChar>='0' && curChar<='9') || curChar=='_'){
					this.token += curChar;
					this.curType = Type.Variable;
					curChar = source.nextChar();
				}
				addToken(this.curType, this.token);
				continue;
			}
			//若当前字符为空格，跳过
			if(curChar == ' '){
				curChar = source.nextChar();
				continue;
			}
			/**
			 * 判断是否是整数或实数
			 * Number -> integral(fraction | null)(exponent | null)
			 * fraction -> .integral
			 * exponent -> (E | e)(+ | - | null)integral
			 * integral -> digit+
			 * digit -> 0-9
			 */
			if(curChar>='0' && curChar<='9'){
				while(curChar>='0' && curChar<='9'){
					this.token += curChar;
					this.curType = Type.Number;
					curChar = source.nextChar();
				}
				if(curChar=='.'){
					this.token += curChar;
					curChar = source.nextChar();
					while(curChar>='0' && curChar<='9'){
						this.token += curChar;
						this.curType = Type.Number;
						curChar = source.nextChar();
					}
				}
				if(curChar=='E' || curChar=='e'){
					this.token += curChar;
					curChar = source.nextChar();
					if(curChar=='+' || curChar=='-'){
						this.token += curChar;
						curChar = source.nextChar();
					}
					if(curChar>='0' && curChar<='9'){
						while(curChar>='0' && curChar<='9'){
							this.token += curChar;
							this.curType = Type.Number;
							curChar = source.nextChar();
						}
					}
					else{
						System.out.println("error" + "    " + curChar);
					}
				}
				addToken(this.curType, this.token);
				continue;
			}
			/**
			 * 判断是否是实数(.123)
			 * Number -> .integral
			 * integral -> digit+
			 * digit -> 0-9
			 */
			if(curChar=='.'){
				this.token += curChar;
				this.curType = Type.Number;
				curChar = source.nextChar();
				if(curChar>='0' && curChar<='9'){
					while(curChar>='0' && curChar<='9'){
						this.token += curChar;
						this.curType = Type.Number;
						curChar = source.nextChar();
					}
				}
				else{
					System.out.println("error" + "    " + curChar);
				}
				addToken(this.curType, this.token);
				continue;
			}
			/**
			 * 判断是否是运算操作符
			 * oper -> + | - | * | / | ++ | -- | ^
			 */
			if(curChar=='+' || curChar=='-' || curChar=='*' || curChar=='/' || curChar=='^'){
				this.token += curChar;
				this.curType = Type.oper;
				char c = curChar;
				curChar = source.nextChar();
//				if((c=='+' && curChar=='+') || (c=='-' && curChar=='-')){
//					this.token += curChar;
//					this.curType = Type.oper;
//					curChar = source.nextChar();
//				}
				addToken(this.curType, this.token);
				continue;
			}
			/**
			 * 判断是否是比较操作符或赋值操作符
			 */
			if(curChar=='!' || curChar=='='){
				this.token += curChar;
				this.curType = Type.assign_oper;
				char c = curChar;
				curChar = source.nextChar();
				if((c=='!'||c=='='||c=='<'||c=='>') && curChar=='='){
					this.token += curChar;
					this.curType = Type.compare_oper;
					curChar = source.nextChar();
				}else if(c=='!'||c=='<'||c=='>'){
					this.curType = Type.compare_oper;
				}
				addToken(this.curType, this.token);
				continue;
			}
			/**
			 * 判断是否是bool比较符
			 */
			if(curChar=='&'){
				curChar = source.nextChar();
				if(curChar=='&'){
					curChar = source.nextChar();
					addToken(Type.bool_oper, "&&");
					continue;
				}
				else{
					System.out.println("error" + "    " + curChar);
				}
			}else if(curChar=='|'){
				curChar = source.nextChar();
				if(curChar=='|'){
					curChar = source.nextChar();
					addToken(Type.bool_oper, "||");
					continue;
				}
				else{
					System.out.println("error" + "    " + curChar);
				}
			}
			/**
			 * 判断是否是String
			 */
			if(curChar=='"'){
				this.token += curChar;
				this.curType = Type.String;
				curChar = source.nextChar();
				while(curChar!='"'){
					this.token += curChar;
					curChar = source.nextChar();
				}
				if(curChar=='"'){
					this.token += curChar;
					this.curType = Type.String;
					curChar = source.nextChar();
				}
				addToken(this.curType, this.token);
				continue;
			}
			/**
			 * 判断是否是delete_symbol
			 */
			if(curChar=='['){
				curChar = source.nextChar();
				if(curChar==']'){
					curChar = source.nextChar();
					addToken(Type.delete_symbol, "[]");
					continue;
				}
				else{
					System.out.println("error" + "    " + curChar);
				}
			}
			/**
			 * 判断是否是左右括号
			 */
			if(curChar=='('){
				addToken(Type.left_bracket, "(");
				curChar = source.nextChar();
				continue;
			}else if(curChar==')'){
				addToken(Type.right_bracket, ")");
				curChar = source.nextChar();
				continue;
			}
			/**
			 * 判断是否是冒号、问号、感叹号或逗号
			 */
			if(curChar=='?'){
				addToken(Type.question_mark, "?");
				curChar = source.nextChar();
				continue;
			}else if (curChar==':'){
				addToken(Type.colon, ":");
				curChar = source.nextChar();
				continue;
			}else if (curChar=='!'){
				addToken(Type.exclamatory_mark, "!");
				curChar = source.nextChar();
				continue;
			}else if (curChar==','){
				addToken(Type.comma, ",");
				curChar = source.nextChar();
				continue;
			}
			/**
			 * 判断是否是句尾';'或空
			 */
			if(curChar==';'){
				addToken(Type.EndSymbol, ";");
				curChar = source.nextChar();
				continue;
			}else if(curChar=='\n'){
				addToken(Type.EndSymbol, "");
				
				//若为空，则到了句尾，解析这条语句
				if(this.curType == Type.EndSymbol){
//					for(Token t:tks){
//						System.out.println(t.type + "    " + t.value);
//					}
					Parser parser = new Parser(tks);
					parser.parse();
					tks = new ArrayList<Token>();
					System.out.println("tokens re initialize");
				}
				
				curChar = source.nextChar();
				continue;
			}
			
			
			System.out.println("error3" + "    " + curChar);
			curChar = source.nextChar();
		}	
	}
	
	/**
	 * 注册token
	 * @param curType
	 * @param token
	 */
	public void addToken(Type curType,String token){
		if(curType == Type.Variable){
			if(token.equals("if") || token.equals("else") || token.equals("elif")
					|| this.token.equals("end")){
				curType = Type.if_grammar;
			}else if(token.toLowerCase().equals("true") || token.toLowerCase().equals("false")){
				curType = Type.Bool;
			}else if(token.equals("sin") || token.equals("cos")){
				curType = Type.single_function;
			}else if(token.equals("max") || token.equals("min")){
				curType = Type.multiple_function;
			}
		}
		
		Scanner.tks.add(new Token(curType,token));
		this.token = "";
		if(curType== Type.EndSymbol){
			this.curType = Type.EndSymbol;
		}else{
			this.curType = Type.Space;
		}
		
	}
}
