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
	public Map<String,String> result = new HashMap<String,String>(){{put("value", "");put("result", "");put("error", "");put("index", "");put("execute_result","");}};

	public Map<String, String> getResult(){
		return result;
	}
	public void run(String s) throws Exception{
		jar_source j_source = new jar_source(s+"\n");

		Scanner scanner = new Scanner();
//		scanner.Scan(source);
		try {
			result = scanner.Scan(j_source);
		}catch(Exception e){
			result.put("input",s+"\n");
			result.put("value","invalid input.");
		}
//        for (String key:result.keySet()){
//            System.out.println(key+":  "+result.get(key));
//        }
	}

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		
//		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
//		Source source = new Source(br);
//		jar_source j_source = new jar_source("a = 123; b = 12\n");
//
//		Scanner scanner = new Scanner();
////		scanner.Scan(source);
//		Map<String,String> rr = scanner.Scan(j_source);
//		for (String key:rr.keySet()){
//			System.out.println(key+":  "+rr.get(key));
//		}
        compile c= new compile();
//        c.run();

	}
}


