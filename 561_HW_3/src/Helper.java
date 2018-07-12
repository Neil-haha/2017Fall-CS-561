import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class Helper {
	private Set<String> Sentences = null;
	private Map<String, List<String>> KB = null;
	
	public Helper() {
		this.Sentences = new HashSet<String>();
		this.KB = new HashMap<String, List<String>>();
	}
	
	public void convert(List<String> statements) {
		for(int i = 0; i < statements.size(); i++) {
			String s = statements.get(i);
			String standard = standardizeVariables(s, i);
			Sentences.add(standard);
			updateKB(standard);
		}
	}
	
	/** 
	 * Construct Knowledge Base
	 * 
	 * Example: 
	 * 		input:    "A(x)|B(x)"
	 * 				  "A(x)|C(x)"
	 * 		result: key: A, value: [A(x)|B(x), A(x)|C(x)]
	 * 				key: B, value: [A(x)|B(x)]
	 * 				key: C, value: [A(x)|C(x)]
	 * 
	 * 
	 */
	
	private void updateKB(String sentence) {
		String[] array = sentence.split("\\|");
		Set<String> visited = new HashSet<>();
		
		for(String cur : array) {
			int left = cur.indexOf("(");
			// "A(x)" -> "A"
			cur = cur.substring(0, left);
			
			// add to KB
			if(visited.add(cur)) {
				List<String> list = KB.get(cur);
				if(list == null) {
					list = new ArrayList<String>();
				}
				list.add(sentence);
				KB.put(cur, list);
			}
		}
	}
	
	/**
	 * Standardize Variables
	 * 
	 * Example:
	 * 		input:		sentence = "A(x)|~B(x)|C(John)"   index = 0
	 * 		output:		"A(x0)|~B(x0)|C(John)"
	 * 
	 * 
	 * 
	 */
	
	private String standardizeVariables(String sentence, int index) {
		StringBuilder sb = new StringBuilder();
		String[] array = sentence.split("\\|");
		
		for(String s : array) {
			int left = s.indexOf("(");
			int right = s.indexOf(")");
			String[] vars = s.substring(left + 1, right).split(",");
			sb.append(s.substring(0, left + 1));
			for(String var : vars) {
				if(var.charAt(0) >= 'a' && var.charAt(0) <= 'z') {
					sb.append(var + index + ",");
				} else {
					sb.append(var + ",");
				}
			}
			sb.deleteCharAt(sb.length() - 1);
			sb.append(s.substring(right));
			sb.append("|");
		}
		
		sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}
	
	/** 
	 *  input:  original statement in the "input.txt" file (removed spaces)
	 *  result: keep only predicates, and update map
	 *  
	 *  Example: 
	 * 		input   : "A(Bob)|~C(Dave)" 
	 * 		return  : "A|~C"
	 * 
	 * 
	 */
	
	public Map<String, List<String>> getKB() {
		return this.KB;
	}
	
	public Set<String> getSentences() {
		return this.Sentences;
	}
	
}
