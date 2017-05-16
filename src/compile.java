import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author Ellie
 * @date 2017/5/1
 * 解析程序_main
 *
 */
public class compile {
	public static Map<String, Float> global_float_variable = new HashMap<>();

	
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		Source source = new Source(br);
		jar_source j_source = new jar_source("a = 1\n");
		
		Scanner scanner = new Scanner();
//		scanner.Scan(source);
		scanner.Scan(j_source);
	}
}


