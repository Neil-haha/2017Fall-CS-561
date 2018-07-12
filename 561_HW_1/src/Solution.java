import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;


public class Solution {
	private static final String SUCCESS = "OK";
	private static final String FAIL = "FAIL";
	
	public static void main(String[] args) {
		long startTime = System.nanoTime();
		try (BufferedReader br = new BufferedReader(new FileReader("input.txt"))) {
			String line = br.readLine();
			String method = "";
			int i = 0, n = 0, p = 0, trees = 0;
			char[][] matrix = null;
			
			while(line != null) {
				if(i == 0) {
					method = line;
				} else if (i == 1) {
					n = Integer.valueOf(line);
					matrix = new char[n][n];
				} else if (i == 2) {
					p = Integer.valueOf(line);
				} else {
					for(int j = 0; j < line.length(); j++) {
						matrix[i - 3][j] = line.charAt(j);
						if(matrix[i - 3][j] == '2') {
							trees++;
						}
					}
				}
				i++;
				line = br.readLine();
			}
			
			output(method, matrix, n, p, trees);
			br.close();
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
		
		long endTime = System.nanoTime();
		System.out.println("Took "+ (endTime - startTime) / 1000000.0 + " ms"); 
	}
	
	private static void output(String method, char[][] matrix, int n, int p, int trees) throws IOException {
		if(p == 0) {
			output_success(n, matrix);
			return;
		} else if(p > 2 * n || p > n * n - trees) {
			output_fail();
			return;
		}
		
		if(method.equals("DFS")) {
			boolean[][] visited = new boolean[n][n];
			if(DFS(matrix, n, p, visited, 0, 0)) {
				output_success(n, matrix);
			} else {
				output_fail();
			}
		} else if (method.equals("BFS")) {
			char[][] rs = BFS(matrix, n, p);
			if(rs != null) {
				output_success(n, rs);
			} else {
				output_fail();
			}
		} else {
			char[][] rs = SA(matrix, n, p, trees);
			if(rs != null) {
				output_success(n, rs);
			} else {
				output_fail();
			}
		}
	}
	
	private static char[][] SA(char[][] matrix, int n, int p, int trees) {
		long startTime = System.nanoTime();
		
		List<Integer> queens = new ArrayList<>();
		HashSet<Integer> visited = new HashSet<>();
		
		for(int i = 0; i < p; i++) {
			int random = (int)(Math.random() * (n * n));
			while(matrix[random / n][random % n] == '2' || visited.contains(random)) {
				random = (int)(Math.random() * (n * n));
			}
			queens.add(random);
			visited.add(random);
		}
		
		long endTime = System.nanoTime();
			
		for(double temperature = 100.0; temperature > 0.00001 && endTime - startTime <= (long)(280) * (long)(1000000000); temperature *= 0.99) {
			int next_random = (int)(Math.random() * p);
			List<Integer> next_queens = sa_getNextState(matrix, n, p, queens, next_random, visited);
			
			int currentEnergy = sa_getEnergy(matrix, n, queens);
			int nextEnergy = sa_getEnergy(matrix, n, next_queens);
			int delta = nextEnergy - currentEnergy;
			
			if(nextEnergy == 0) {
				bfs_mark(matrix, n, next_queens);
				return matrix;
			}
			
			if(delta < 0) {
				visited.remove(queens.get(next_random));
				visited.add(next_queens.get(next_random));
				queens = next_queens;
			} else if (Math.random() <= Math.exp(-delta / temperature)) {
				visited.remove(queens.get(next_random));
				visited.add(next_queens.get(next_random));
				queens = next_queens;
			}
			endTime = System.nanoTime();
		}
			
		return null;
	}
	
	private static int sa_getEnergy(char[][] matrix, int n, List<Integer> queens) {
		int cost = 0;
		
		bfs_mark(matrix, n, queens);
		
		for(Integer queen : queens) {
			int row = queen / n;
			int col = queen % n;
			if(!bfs_check(matrix, n, row, col)) {
				cost++;
			}
		}
		
		bfs_unmark(matrix, n, queens);
		
		return cost;
	}

	private static List<Integer> sa_getNextState(char[][] matrix, int n, int p, List<Integer> queens, int rand, HashSet<Integer> visited) {
		
		List<Integer> nextState = new ArrayList<>();
		
		for(int i = 0; i < p; i++) {
			
			if(rand == i) {
				int change = (int)(Math.random() * (n * n));
				while(matrix[change / n][change % n] == '2' || visited.contains(change)) {
					change = (int)(Math.random() * (n * n));
				}
				nextState.add(change);
			} else {
				nextState.add(queens.get(i));
			}
			
		}
		
		return nextState;
	}
	
	
	private static char[][] BFS(char[][] matrix, int n, int p) {
		if(p == 0) {
			return matrix;
		}
		
		Deque<List<Integer>> queue = new LinkedList<>();
		
		// point: row, col ==> int: row * n + col 
		// row : array[0]
		// col : array[1]
		int[] array = new int[2];
		
		while(array[0] < n && queue.isEmpty()) {
			for(; array[1] < n && matrix[array[0]][array[1]] != '2'; array[1]++) {
				if(p == 1) {
					matrix[array[0]][array[1]] = '1';
					return matrix;
				}
				queue.offer(Arrays.asList(array[0] * n + array[1]));
			}
			bfs_update_row_col(matrix, n, array);
		}
		
		while(array[0] < n) {
			int size = queue.size();
			while(size-- > 0) {
				// pull the element from the front of the queue.
				List<Integer> cur = queue.poll();
				bfs_mark(matrix, n, cur);
				
				for(int i = array[1]; i < n && matrix[array[0]][i] != '2'; i++) {
					if(bfs_check(matrix, n, array[0], i)) {
						if(cur.size() + 1 == p) {
							matrix[array[0]][i] = '1';
							return matrix;
						}
						
						List<Integer> next = new ArrayList<>(cur);
						next.add(array[0] * n + i);
						queue.offer(next);
					} else {
						queue.offer(cur);
					}
				}
				
				bfs_unmark(matrix, n, cur);
			}
			
			// update row, col to the next available position.
			while(array[1] < n && matrix[array[0]][array[1]] == '0') {
				array[1]++;
			}
			
			bfs_update_row_col(matrix, n, array);
		}
		return null;
	}
	
	private static void bfs_mark(char[][] matrix, int n, List<Integer> list) {
		if(list != null && list.size() > 0) {
			for(Integer point : list) {
				matrix[point / n][point % n] = '1';
			}
		}
	}
	
	private static void bfs_unmark(char[][] matrix, int n, List<Integer> list) {
		if(list != null && list.size() > 0) {
			for(Integer point : list) {
				matrix[point / n][point % n] = '0';
			}
		}
	}	
	
	private static void bfs_update_row_col(char[][] matrix, int n, int[] array) {
		if(array[1] == n) {
			array[0]++;
			array[1] = 0;
		}
		
		while(array[0] < n && array[1] < n && matrix[array[0]][array[1]] == '2') {
			array[1]++;
			if(array[1] == n) {
				array[0]++;
				array[1] = 0;
			}
		}
	}
	
	private static boolean bfs_check(char[][] matrix, int n, int row, int col) {
		// check row
		for(int i = col - 1; i >= 0; i--) {
			if(matrix[row][i] == '1') {
				return false;
			} else if (matrix[row][i] == '2') {
				break;
			}
		}
		
		for(int i = col + 1; i < n; i++) {
			if(matrix[row][i] == '1') {
				return false;
			} else if (matrix[row][i] == '2') {
				break;
			}
		}
		
		// check col
		for(int i = row - 1; i >= 0; i--) {
			if(matrix[i][col] == '1') {
				return false;
			} else if (matrix[i][col] == '2') {
				break;
			}
		}
		
		for(int i = row + 1; i < n; i++) {
			if(matrix[i][col] == '1') {
				return false;
			} else if (matrix[i][col] == '2') {
				break;
			}
		}
		
		// check diag
		int tmp = col + 1;
		for(int i = row - 1; i >= 0; i--) {
			if(tmp < n) {
				if(matrix[i][tmp] == '1') {
					return false;
				} else if (matrix[i][tmp] == '2') {
					break;
				}
			} else {
				break;
			}
			tmp++;
		}
		
		tmp = col - 1;
		for(int i = row + 1; i < n; i++) {
			if(tmp >= 0) {
				if(matrix[i][tmp] == '1') {
					return false;
				} else if (matrix[i][tmp] == '2') {
					break;
				}
			} else {
				break;
			}
			tmp--;
		}
		
		// check revdiag
		tmp = col - 1;
		for(int i = row - 1; i >= 0; i--) {
			if(tmp >= 0) {
				if(matrix[i][tmp] == '1') {
					return false;
				} else if (matrix[i][tmp] == '2') {
					break;
				}
			} else {
				break;
			}
			tmp--;
		}
		
		tmp = col + 1;
		for(int i = row + 1; i < n; i++) {
			if(tmp < n) {
				if(matrix[i][tmp] == '1') {
					return false;
				} else if (matrix[i][tmp] == '2') {
					break;
				}
			} else {
				break;
			}
			tmp++;
		}
		
		return true;
	}
	
	private static boolean DFS(char[][] matrix, int n, int p, boolean[][] visited, int row, int col) {
		if(p == 0) {
			return true;
		}
		if(row == n) {
			return false;
		}
		int trees = 0;
		for(int i = col; i < n; i++) {
			if(!visited[row][i]) {
				if(matrix[row][i] == '0') {
					List<Integer> markedPoints = dfs_mark(matrix, row, i, n, visited);
					for(int j = i + 1; j < n; j++) {
						if(matrix[row][j] == '2') {
							if(DFS(matrix, n, p - 1, visited, row, j + 1)) 
								return true;
						}
					}
					if(DFS(matrix, n, p - 1, visited, row + 1, 0)) {
						return true;
					}
					dfs_unmark(matrix, row, i, n, visited, markedPoints);
				} else {
					trees++;
				}
			}
		}
		if(trees == n - col) {
			return DFS(matrix, n, p, visited, row + 1, 0);
		}
		return false;
	}
	
	private static List<Integer> dfs_mark(char[][] matrix, int row, int col, int n, boolean[][] visited) {
		matrix[row][col] = '1';
		visited[row][col] = true;
		List<Integer> markedPoints = new ArrayList<>();
		markedPoints.add(row * n + col);
		
		// row
		for(int i = col - 1; i >= 0; i--) {
			if(matrix[row][i] == '0') {
				if(!visited[row][i]) {
					visited[row][i] = true;
					markedPoints.add(row * n + i);
				}
			} else if (matrix[row][i] == '2') {
				break;
			}
		}
		for(int i = col + 1; i < n; i++) {
			if(matrix[row][i] == '0') {
				if(!visited[row][i]) {
					visited[row][i] = true;
					markedPoints.add(row * n + i);
				}
			} else if (matrix[row][i] == '2') {
				break;
			}
		}
		// col
		for(int i = row - 1; i >= 0; i--) {
			if(matrix[i][col] == '0') {
				if(!visited[i][col]) {
					visited[i][col] = true;
					markedPoints.add(i * n + col);
				}
			} else if (matrix[i][col] == '2') {
				break;
			}
		}
		for(int i = row + 1; i < n; i++) {
			if(matrix[i][col] == '0') {
				if(!visited[i][col]) {
					visited[i][col] = true;
					markedPoints.add(i * n + col);
				}
			} else if (matrix[i][col] == '2') {
				break;
			}
		}
		// diag
		int tmp = col + 1;
		for(int i = row - 1; i >= 0; i--) {
			if(tmp < n) {
				if(matrix[i][tmp] == '0') {
					if(!visited[i][tmp]) {
						visited[i][tmp] = true;
						markedPoints.add(i * n + tmp);
					}
				} else if (matrix[i][tmp] == '2') {
					break;
				}
			} else {
				break;
			}
			tmp++;
		}
		tmp = col - 1;
		for(int i = row + 1; i < n; i++) {
			if(tmp >= 0) {
				if(matrix[i][tmp] == '0') {
					if(!visited[i][tmp]) {
						visited[i][tmp] = true;
						markedPoints.add(i * n + tmp);
					}
				} else if (matrix[i][tmp] == '2') {
					break;
				}
			} else {
				break;
			}
			tmp--;
		}
		// revdiag
		tmp = col - 1;
		for(int i = row - 1; i >= 0; i--) {
			if(tmp >= 0) {
				if(matrix[i][tmp] == '0') {
					if(!visited[i][tmp]) {
						visited[i][tmp] = true;
						markedPoints.add(i * n + tmp);
					}
				} else if (matrix[i][tmp] == '2') {
					break;
				}
			} else {
				break;
			}
			tmp--;
		}
		tmp = col + 1;
		for(int i = row + 1; i < n; i++) {
			if(tmp < n) {
				if(matrix[i][tmp] == '0') {
					if(!visited[i][tmp]) {
						visited[i][tmp] = true;
						markedPoints.add(i * n + tmp);
					}
				} else if (matrix[i][tmp] == '2') {
					break;
				}
			} else {
				break;
			}
			tmp++;
		}
		return markedPoints;
	}
	
	private static void dfs_unmark(char[][] matrix, int row, int col, int n, boolean[][] visited, List<Integer> markedPoints) {
		matrix[row][col] = '0';
		for(Integer point : markedPoints) {
			visited[point / n][point % n] = false;
		}
	}
	
	private static void output_success(int n, char[][] matrix) throws IOException {
		BufferedWriter out = new BufferedWriter(new FileWriter("output.txt"));
		out.write(SUCCESS);
		out.newLine();
		for(int i = 0; i < n; i++) {
			out.write(new String(matrix[i]));
			out.newLine();
		}
		out.close();
	}
	
	private static void output_fail() throws IOException {
		FileWriter fw = new FileWriter("output.txt");
		fw.write(FAIL);
		fw.close();
	}
}
