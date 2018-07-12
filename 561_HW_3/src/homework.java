import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class homework {
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//long startTime = System.nanoTime();
		//System.out.println("First-order-logic Resolution: ");
		
		List<String> queries = new ArrayList<>();
		List<String> statements = new ArrayList<>();
		List<Boolean> results = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader("input1.txt"))) {
			int nQ = 0, nS = 0;
			
			String line = br.readLine();
			
			// get all the queries
			nQ = Integer.parseInt(line);
			for(int i = 0; i < nQ; i++) {
				line = br.readLine().replaceAll("\\s+", "");
				queries.add(line);
			}
			
			// get all the statements
			nS = Integer.parseInt(br.readLine());
			for(int i = 0; i < nS; i++) {
				line = br.readLine().replaceAll("\\s+", "");
				statements.add(line);
			}
			br.close();
			
			Helper helper =  new Helper();
			helper.convert(statements);
		
			//System.out.println("/********************KB*****************/");
			//for(String key : helper.getKB().keySet()) {
			//	System.out.println(key + ": " + helper.getKB().get(key));
			//}
			//System.out.println("KB size: " + helper.getKB().size());
			
			// Example: "~G(x) | H(x)"
			//			"~R(x) | H(x)"
			//          "H(x) | A(x) | ~B(x)"
			// KB
			// ~G : ~G(x)|H(x)
			// H  : ~G(x)|H(x), ~R(x)|H(x), H(x)|A(x)|~B(x)
			// ~R : ~R(x)|H(x)
			// A  : H(x)|A(x)|~B(x)
			// ~B : H(x)|A(x)|~B(x)
			
			for(String query : queries) {
				Map<String, List<String>> KBCopy = KBDeepCopy(helper.getKB());
				Set<String> SentencesCopy = SentenceesDeepCopy(helper.getSentences());
				
				Resolution rs = new Resolution(KBCopy, SentencesCopy);
				results.add(rs.solve(query));
			}
			
			output(results);
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
		
		//long endTime = System.nanoTime();
		//System.out.println("Took "+ (endTime - startTime) / 1000000.0 + " ms"); 
	}
	
	private static Map<String, List<String>> KBDeepCopy(Map<String, List<String>> KB) {
		Map<String, List<String>> rs = new HashMap<String, List<String>>();
		for(String key : KB.keySet()) {
			List<String> new_list = new ArrayList<>();
			for(String s : KB.get(key)) {
				new_list.add(s);
			}
			rs.put(key, new_list);
		}
		return rs;
	}
	
	private static Set<String> SentenceesDeepCopy(Set<String> Sentences) {
		Set<String> rs = new HashSet<>();
		for(String s : Sentences) {
			rs.add(s);
		}
		return rs;
	}

	private static void output(List<Boolean> results) {
		System.out.println("/********************results*****************/");
		try(FileWriter out = new FileWriter(new File("output.txt"))) {
			for(int i = 0; i < results.size(); i++) {
				Boolean result = results.get(i);
				String r = result.toString().toUpperCase();
				if(i == results.size() - 1) {
					out.write(r);
				} else {
					out.write(r + "\n");
				}
				System.out.println(r);
			}
			out.close();
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
	}
}
