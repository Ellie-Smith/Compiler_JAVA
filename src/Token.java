/**
 * 
 * @author Ellie
 * @date 2017/5/1
 * 基本单元类
 *
 */
public class Token { 

	final Scanner.Type type; 
	final String value; 
	
	public Token(Scanner.Type curType, String value) {
		this.type = curType; 
		this.value = value;

	} 
}


