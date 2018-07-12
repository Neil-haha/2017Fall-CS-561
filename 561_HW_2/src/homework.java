import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.PriorityQueue;


public class homework {	
	static class Node {
		int row;
		int col;
		
		public Node(int row, int col) {
			this.row = row;
			this.col = col;
		}
	}
	
	static class ChangedNode {
		int col;
		int oldRow;
		int newRow;
		
		public ChangedNode(int oldRow, int col, int newRow) {
			this.col = col;
			this.oldRow = oldRow;
			this.newRow = newRow;
		}
	}
	
	static class Element {
		char fruit;
		int val;
		List<Node> group;
		
		public Element(char fruit, int val, List<Node> group) {
			this.fruit = fruit;
			this.val = val;
			this.group = group;
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		long startTime = System.nanoTime();
		try (BufferedReader br = new BufferedReader(new FileReader("input.txt"))) {
			String line = br.readLine();
			int i = 0, n = 0, p = 0;
			double remainingT = 0;
			char[][] matrix = null;
			
			while(line != null) {
				if(i == 0) {
					n = Integer.valueOf(line);
					matrix = new char[n][n];
				} else if (i == 1) {
					p = Integer.valueOf(line);
				} else if (i == 2) {
					remainingT = Double.valueOf(line);
				} else {
					matrix[i - 3] = line.toCharArray();
				}
				i++;
				line = br.readLine();
			}
			
			int targetDepth = 4;
			if(remainingT < 3) {
				targetDepth = 1;
			} else if (remainingT < 20) {
				targetDepth = 2;
			}
			
			List<Node> res = findBestMove(matrix, n, targetDepth, p);
			set(matrix, res);
			gravity(matrix, n);
			
			output(res.get(0), matrix, n);
			
			
			// For battle
			// output(n, p, remainingT, matrix);
			
			br.close();
			
			long endTime = System.nanoTime();
			System.out.println("Took "+ (endTime - startTime) / 1000000.0 + " ms"); 
			// For battle
			// System.out.println("Move " +  (char)(res.get(0).col + 'A') + (res.get(0).row + 1) +   ", Score " + res.size() * res.size());
			
		} catch (Exception e) {
			e.printStackTrace(System.out);
		} 
	}
	
	private static List<Node> findBestMove(char[][] matrix, int n, int targetDepth, int p) {
		int best = Integer.MIN_VALUE;
		List<Node> result = null;

		PriorityQueue<Element> maxHeap = getMaxHeap(matrix, n);
		
		int alpha = Integer.MIN_VALUE;
		int beta = Integer.MAX_VALUE;
		
		while(!maxHeap.isEmpty()) {
			Element cur = maxHeap.poll();
			int curVal = cur.val * cur.val;
			
			set(matrix, cur.group);
			List<ChangedNode> list = gravity(matrix, n);
			
			int evaVal = 0;
			if(targetDepth > 1) {
				evaVal = minimax(matrix, n, p, curVal, 1, targetDepth, false, alpha, beta);
			} else {
				evaVal = curVal;
			}
			
			if(evaVal > best) {
				result = cur.group;
				alpha = evaVal;
				best = evaVal;
			}
			
			// reset
			reset(matrix, cur, list);
		}
		return result;
	}
	
	private static int minimax(char[][] matrix, int n, int p, int curVal, int depth, int targetDepth, boolean isMax, int alpha, int beta) {
		if(!isNodeLeft(matrix, n)) {
			return curVal;
		}
		
		if(isMax) {
			int best = Integer.MIN_VALUE;
			PriorityQueue<Element> maxHeap = getMaxHeap(matrix, n);
			
			Map<Integer, Integer> map = new HashMap<>();
			//int prevBest = 0;
			
			while(!maxHeap.isEmpty()) {
				Element cur = maxHeap.poll();
				int temp = cur.val * cur.val;
				curVal += temp;
				
				if(depth == targetDepth) {
					best = curVal;
					break;
				}
				
				set(matrix, cur.group);
				List<ChangedNode> list = gravity(matrix, n);
				
				int val = minimax(matrix, n, p, curVal, depth + 1, targetDepth, !isMax, alpha, beta);
				
				best = Math.max(best, val);
				alpha = Math.max(alpha, best);
				
				// reset
				curVal -= temp;
				reset(matrix, cur, list);
				
				// alpha-beta pruning
				if(beta <= alpha) {
					break;
				}
				
				Integer count = map.get(best);
				if(count == null) {
					map.put(best, 1);
				} else if (count < 5){
					map.put(best, 1 + count);
				} else {
					break;
				}
			}
			return best;
		} else {
			int best = Integer.MAX_VALUE;
			PriorityQueue<Element> maxHeap = getMaxHeap(matrix, n);
			
			Map<Integer, Integer> map = new HashMap<>();
			
			while(!maxHeap.isEmpty()) {
				Element cur = maxHeap.poll();
				int temp = cur.val * cur.val;
				curVal -= temp;
				
				if(depth == targetDepth) {
					best = curVal;
					break;
				}
				
				set(matrix, cur.group);
				List<ChangedNode> list = gravity(matrix, n);
				
				int val = minimax(matrix, n, p, curVal, depth + 1, targetDepth, !isMax, alpha, beta);
				
				best = Math.min(best, val);
				beta = Math.min(beta, best);
				
				// reset
				curVal += temp;
				reset(matrix, cur, list);
				
				// alpha-beta pruning
				if(beta <= alpha) {
					break;
				}
				
				Integer count = map.get(best);
				if(count == null) {
					map.put(best, 1);
				} else if (count < 5){
					map.put(best, 1 + count);
				} else {
					break;
				}
			}
			return best;
		}
	}
	
	private static void set(char[][] matrix, List<Node> group) {
		for(Node node : group) {
			matrix[node.row][node.col] = '*';
		}
	}
	
	private static PriorityQueue<Element> getMaxHeap(char[][] matrix, int n) {
		boolean[][] visited = new boolean[n][n];
		PriorityQueue<Element> maxHeap = new PriorityQueue<Element>(11, new Comparator<Element>(){
			@Override
			public int compare(Element e1, Element e2) {
				return e2.val - e1.val;
			}
		});
		// find all the valid Element
		for(int i = 0; i < n; i++) {
			for(int j = 0; j < n; j++) {
				if(matrix[i][j] != '*' && !visited[i][j]) {
					List<Node> group = new ArrayList<Node>();
					mark(matrix, visited, n, i, j, i, j, group);
					maxHeap.offer(new Element(matrix[i][j], group.size(), group));
				}
			}
		}
		return maxHeap;
	}
	
	private static boolean isNodeLeft(char[][] matrix, int n) {
		for(int i = 0; i < n; i++) {
			for(int j = 0; j < n; j++) {
				if(matrix[i][j] != '*') {
					return true;
				}
			}
		}
		return false;
	}
	
	private static void reset(char[][] matrix, Element cur, List<ChangedNode> list) {
		for(int i = list.size() - 1; i >= 0; i--) {
			ChangedNode node = list.get(i);
			swap(matrix, node.col, node.oldRow, node.newRow);
		}
		for(Node node : cur.group) {
			matrix[node.row][node.col] = cur.fruit;
		}
	}
	
	private static List<ChangedNode> gravity(char[][] matrix, int n) {
		List<ChangedNode> list = new ArrayList<>();
		for(int col = 0; col < n; col++) {
			int up = n - 1;
			for(int row = n - 1; row >= 0; row--) {
				if(matrix[row][col] == '*') {
					if(up == n - 1) {
						up = row - 1;
					}
					while(up >= 0 && matrix[up][col] == '*') {
						up--;
					}
					if(up < 0) {
						break;
					}
					swap(matrix, col, row, up--);
					list.add(new ChangedNode(row, col, up + 1));
				}
			}
		}
		return list;
	}
	
	private static void swap(char[][] matrix, int col, int oldRow, int newRow) {
		char c = matrix[oldRow][col];
		matrix[oldRow][col] = matrix[newRow][col];
		matrix[newRow][col] = c;
	}
	
	private static void mark(char[][] matrix, boolean[][] visited, int n, int startRow, int startCol, int endRow, int endCol, List<Node> group) {
		if(endRow < 0 || endRow >= n || endCol < 0 || endCol >= n || visited[endRow][endCol] || matrix[endRow][endCol] != matrix[startRow][startCol]) {
			return;
		}
		group.add(new Node(endRow, endCol));
		visited[endRow][endCol] = true;
		mark(matrix, visited, n, startRow, startCol, endRow + 1, endCol, group);
		mark(matrix, visited, n, startRow, startCol, endRow - 1, endCol, group);
		mark(matrix, visited, n, startRow, startCol, endRow, endCol + 1, group);
		mark(matrix, visited, n, startRow, startCol, endRow, endCol - 1, group);
	}
	
	private static void output(Node bestMove, char[][] matrix, int n) throws IOException {
		BufferedWriter out = new BufferedWriter(new FileWriter("output.txt"));
		out.write((char)(bestMove.col + 'A') + "" + (bestMove.row + 1));
		out.newLine();
		for(int i = 0; i < n; i++) {
			out.write(new String(matrix[i]));
			out.newLine();
		}
		out.close();
	}
	
	// For battle
	
//	private static void output(int n, int p, double remainingT, char[][] matrix) throws IOException {
//		BufferedWriter out = new BufferedWriter(new FileWriter("../input.txt"));
//		out.write(n + "");
//		out.newLine();
//		out.write(p + "");
//		out.newLine();
//		out.write(remainingT + "");
//		out.newLine();
//		for(int i = 0; i < n; i++) {
//			out.write(new String(matrix[i]));
//			out.newLine();
//		}
//		out.close();
//	}
}
