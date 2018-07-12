import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class Resolution {
	private Map<String, List<String>> KB;
	private Set<String> Sentences;
	private List<String> listOfSentences;
	
	public Resolution(Map<String, List<String>> KB, Set<String> Sentences) {
		this.KB = KB;
		this.Sentences = Sentences;
		this.listOfSentences = new ArrayList<String>(Sentences);
	}
	
	
	public boolean solve(String query) {
		String negatedQuery = negate(query);
		if(!Sentences.add(negatedQuery)) {
			return false;
		}
		listOfSentences.add(negatedQuery);	
		
		int diff = -1;
		while(diff != 0) {
			int size = listOfSentences.size();
			int index = size - 1;
			while(index >= 0) {
				if(listOfSentences.size() > 48796) {
					return false;
				}
				if(unification(listOfSentences.get(index))) {
					return true;
				}
				index--;
			}
			diff = Sentences.size() - size;
		}
		
		return false;
	}
	
	private boolean unification(String sentence) {
		String[] sentenceAtomics = sentence.split("\\|");
		
		if(sentenceAtomics.length == 1 && Sentences.contains(negate(sentenceAtomics[0]))){
			return true;
		}
		
		for(int i = 0; i < sentenceAtomics.length; i++) {
			String negatedAtomic = negate(sentenceAtomics[i]);
			String atomicPredicate = getPredicate(negatedAtomic);
			String[] atomicParameters = getParameters(negatedAtomic);
			
			List<String> implications = KB.get(atomicPredicate);
			
			if(implications != null) {
				boolean included = false;
				for(String s : implications) {
					if(sentence.equals(s)) {
						included = true;
						break;
					}
				}
				if(!included) {
					for(int j = 0; j < implications.size(); j++) {
						String[] impliAtomics = implications.get(j).split("\\|");
						//boolean canUnify = false;
						//boolean mayUnify = false;
						boolean[] canUnify = new boolean[2];
						
						for(int k = 0; k < impliAtomics.length; k++) {
							String impliAtomic = impliAtomics[k];
							
							if(getPredicate(impliAtomic).equals(atomicPredicate)) {
								Map<String, String> parametersMap = new HashMap<String, String>();
								String[] impliParameters = getParameters(impliAtomic);
								
								checkUnify(sentenceAtomics, atomicParameters, impliAtomics, impliParameters, parametersMap, canUnify);
								
								if((canUnify[1] && sentenceAtomics.length == 1) || canUnify[0]) {
									String sentenceUnified = resolveSentence(sentenceAtomics, i, parametersMap);
									String implicationUnified = resolveSentence(impliAtomics, k, parametersMap);
									String unified = combineSentence(sentenceUnified, implicationUnified);
									
									if(unified == null) {
										return true;
									}
									
									if(Sentences.add(unified)){
										String[] atomics = unified.split("\\|");
										
										if (atomics.length == 1 && Sentences.contains(negate(unified))){
											return true;
										}
										
										for (String atomic : atomics){
											String p = getPredicate(atomic);
											List<String> list = KB.get(p);
											if(list == null) {
												list = new ArrayList<String>();
											}
											list.add(unified);
											KB.put(p, list);
										}
										listOfSentences.add(unified);
									}
									break;
								}
							}
						}
					}
				}
			}
		}

		return false;
	}
	
	private void checkUnify(String[] sentenceAtomics, String[] atomicParameters, String[] impliAtomics, String[] impliParameters, Map<String, String> parametersMap, boolean[] canUnify) {
		// compare atomicParameters with impliParameters
		for(int i = 0; i < impliParameters.length; i++) {
			String a = atomicParameters[i];
			String b = impliParameters[i];
			
			if(a.equals(b)) {
				parametersMap.put(a, b);
				canUnify[1] = isConstant(a);
			} else if (isConstant(a) && isConstant(b)) {
				canUnify[0] = canUnify[1] = false;
				break;
			} else if (isConstant(a)) {
				parametersMap.put(b, a);
				canUnify[0] = true;
			} else if (isConstant(b)) {
				parametersMap.put(a, b);
				canUnify[0] = true;
			} else {
				parametersMap.put(a, b);
				if(impliAtomics.length == 1 || sentenceAtomics.length == 1) {
					canUnify[0] = true;
				}
			}
		}
	}

	
	private String combineSentence(String a, String b) {
		if(a.length() == 0 && b.length() == 0) {
			return null;
		} else if (a.length() == 0) {
			return b;
		} else if (b.length() == 0) {
			return a;
		} else {
			return a + "|" + b;
		}
	}
	
	private String resolveSentence(String[] array, int original, Map<String, String> map) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < array.length; i++) {
			if(i != original) {
				String cur = array[i];
				sb.append(cur.substring(0, cur.indexOf("(") + 1));
				
				for(String p : getParameters(cur)) {
					if(map.get(p) != null) {
						sb.append(map.get(p) + ",");
					} else {
						sb.append(p + ",");
					}
				}
				sb.deleteCharAt(sb.length() - 1);
				sb.append(")|");
			}
		}
		if(sb.length() > 0) {
			return sb.deleteCharAt(sb.length() - 1).toString();
		} else {
			return "";
		}
	}
	
	private boolean isConstant(String var) {
		return var != null && var.length() > 0 && var.charAt(0) >= 'A' && var.charAt(0) <= 'Z';
	}
	
	private String[] getParameters(String atomic) {
		return atomic.substring(atomic.indexOf("(") + 1, atomic.indexOf(")")).split(",");
	}
	
	private String getPredicate(String atomic) {
		return atomic.substring(0, atomic.indexOf("("));
	}
	
	private String negate(String query) {
		return query.charAt(0) == '~' ? query.substring(1) : "~" + query;
	}
	
}
