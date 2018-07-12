
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;


public class player2 {
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
		int row;
		int col;
		char fruit;
		int val;
		List<Node> group;
		
		public Element(int row, int col, char fruit, int val, List<Node> group) {
			this.row = row;
			this.col = col;
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
					
					br.close();
					
					if(!isNodeLeft(matrix, n)) {
						System.out.println("Game Over!");
						return;
					}
					
					List<Node> res = findBestMove(matrix, n);
					
					//for(Node node : res) {
					//	matrix[node.row][node.col] = '*';
					//}
					
					//gravity(matrix, n);
					
					//output(n, p, remainingT, matrix);
					long endTime = System.nanoTime();
					
					output(res.get(0), res.size() * res.size(), (endTime - startTime) / 1000000000.0, matrix, n);
					
					//long endTime = System.nanoTime();
					//System.out.println("Took "+ (endTime - startTime) / 1000000.0 + " ms"); 
					//System.out.println("Move " +  (char)(res.get(0).col + 'A') + (res.get(0).row + 1) +   ", Score " + res.size() * res.size());
				} catch (Exception e) {
					e.printStackTrace(System.out);
				}
				

	}
	
	private static List<Node> findBestMove(char[][] matrix, int n) {
		int bestVal = Integer.MIN_VALUE;
		
		List<Node> result = null;
		
		boolean[][] visited = new boolean[n][n];
		
		// maintain a maxHeap of Element
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
					maxHeap.offer(new Element(i, j, matrix[i][j], group.size(), group));
				}
			}
		}
		
		//int[] alphaBeta = new int[]{Integer.MIN_VALUE, Integer.MAX_VALUE};
		
	
			
			Element cur = maxHeap.poll();
			
			int curVal = cur.val * cur.val;
			
			for(Node node : cur.group) {
				matrix[node.row][node.col] = '*';
			}
			
			// apply gravity
			List<ChangedNode> list = gravity(matrix, n);
			
			//int evaVal = minimax(matrix, n, curVal, 1, false, alpha, beta);
			
			// reset
			// reset(matrix, cur, list);
			
			result = cur.group;
		
		
		
		return result;
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
	
	/*
	private static void output(int n, int p, double remainingT, char[][] matrix) throws IOException {
		BufferedWriter out = new BufferedWriter(new FileWriter("input.txt"));
		out.write(n + "");
		out.newLine();
		out.write(p + "");
		out.newLine();
		out.write(remainingT + "");
		out.newLine();
		for(int i = 0; i < n; i++) {
			out.write(new String(matrix[i]));
			out.newLine();
		}
		out.close();
	}*/
	
	private static void output(Node bestMove, int score, double time, char[][] matrix, int n) throws IOException {
		BufferedWriter out = new BufferedWriter(new FileWriter("output.txt"));
		out.write((char)(bestMove.col + 'A') + "" + (bestMove.row + 1));
		out.newLine();
		for(int i = 0; i < n; i++) {
			out.write(new String(matrix[i]));
			out.newLine();
		}
		out.write("" + score);
		out.newLine();
		out.write("" + time);
		out.close();
	}

}
