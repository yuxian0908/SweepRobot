
package com.yuxian.sweeprobot;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.TimeUnit;

public class SweepRobot3 {
	
	// [up, right, down, left]
	private static int[][] directions = new int[][] {{-1,0},{0,1},{0,-1},{1,0}};
			
	private static int barrier = -3;
	
	private static int start = 0;
	
	private static int free = -2;
	
	private static int visited = 1;
	
	private static int maxStep = 0;
	
	private static int mazeHeight = 0;
	
	private static int mazeWidth = 0;
	
	private static int totalCount = 0;
	
	private static int countStep = 0;
	
	private static int entryRow = 0;
	
	private static int entryCol = 0;
	
	private static Stack<int[]> thisRoad = new Stack<>();
	
	private static Stack<int[]> waitedRoad = new Stack<>();
	
	private static boolean hasWaitedRoad = false;

	private static boolean holdRoad = false;

	private static int[][] visitedMaze = new int[0][0];
	
	private static int[][] maze = new int[0][0];
	
	// calculate the distance from entry
 	private static void findRoad(int[][] maze, int r, int c) {
 		Queue<int[]> store = new LinkedList<>();
 		store.offer(new int[] {r,c});
 		while(!store.isEmpty()) {
 			int[] temp = store.poll();
 			int row = temp[0];
 			int col = temp[1];
 			for(int[] dir: directions) {
 				int rowMove = dir[0];
 				int colMove = dir[1];
 				int next = maze[row+rowMove][col+colMove];
 				if( next!=barrier && next!=start && (next>maze[row][col]+1 || next == free)) {
 					maze[row+rowMove][col+colMove] = maze[row][col]+1;
 					store.offer(new int[] {row+rowMove,col+colMove});
 				}
 			}
 		}
	}
	
 	// sweep next road
	private static int[] sweep(int[][] maze, int row, int col, int prev) {

		boolean hasNext = false;
		int nowStep = maze[row][col];
		int nextStep = 0;
		int[] now = new int[] {row,col};
		int[] next = new int[] {row,col,prev};
		if(!hasWaitedRoad) {
			// <--- check what is next step --->
			if(nowStep<maxStep/2 && (nowStep>=prev || nowStep==0)) {
				// check is there any unvisited road
				for(int[] dir: directions) {
					int nRow = now[0]+dir[0];
					int nCol = now[1]+dir[1];
					if(maze[nRow][nCol]==maze[row][col]+1 && visitedMaze[nRow][nCol]!=visited) {
						hasNext = true;
						break;
					}
				}
			}
			
			//<--- go next step --->
			if(hasNext) {
				nextStep = nowStep + 1;
				int nRow = 0;
				int nCol = 0;
				
				// check first priority which has next one and not visited
				for(int[] dir: directions) {
					nRow = now[0]+dir[0];
					nCol = now[1]+dir[1];
					if(maze[nRow][nCol]==nextStep && visitedMaze[nRow][nCol]!=visited) {
						next[0] = nRow;
						next[1] = nCol;
						next[2] = nowStep;
						visitedMaze[nRow][nCol] = visited;
						totalCount--;
						break;
					}
				}
				thisRoad.push(new int[] {row,col});
			}
			
		}else {
			// <--- check what is next step --->
			if(nowStep<maxStep/2) {
				hasNext = true;
				if(waitedRoad.isEmpty()) {
					hasWaitedRoad = false;
				}
				if(!waitedRoad.isEmpty()) {
					int[] road = waitedRoad.pop();
					next[0] = road[0];
					next[1] = road[1];
					next[2] = nowStep;
					visitedMaze[road[0]][road[1]] = visited;
					if(!waitedRoad.isEmpty()) {
						thisRoad.push(new int[] {road[0],road[1]});
					}else {
						totalCount--;
					}
				}
			}else {
				hasNext = false;
			}
		}
		
		if(!hasNext){
			int[] road = thisRoad.pop();
			next[0] = road[0];
			next[1] = road[1];
			next[2] = nowStep;
			visitedMaze[road[0]][road[1]] = visited;
			if(!holdRoad) {
				for(int[] dir: directions) {
					int nRow = now[0]+dir[0];
					int nCol = now[1]+dir[1];
					if(maze[nRow][nCol]==maze[row][col]+1 && visitedMaze[nRow][nCol]!=visited) {
						if(nRow!=road[0] || nCol!=road[1]) {
							waitedRoad.clear();
							waitedRoad.push(new int[] {nRow, nCol});
							waitedRoad.push(new int[] {row, col});
							holdRoad = true;
						}
					}
				}
			}
			waitedRoad.push(new int[] {road[0], road[1]});
			
		}
		return next;
	}
	
	// sweep all road
	private static void sweepAll(int[][] maze, int row, int col) {
		int[] now = sweep(maze,row,col,-1);
		visitedMaze[row][col] = visited;
		totalCount--;
		while(now[0]!=row || now[1]!=col || totalCount>0){
			countStep++;
			writeStep(Arrays.toString(new int[] {now[0],now[1]}));
			int[] next = sweep(maze,now[0],now[1],now[2]);
			now[0] = next[0];
			now[1] = next[1];
			now[2] = next[2];
			if(holdRoad) {
				if(now[0]==row && now[1]==col) {
					hasWaitedRoad = true;
					holdRoad = false;
				}
			}
		}
		writeStep( Integer.toString(countStep));
	}
	
	// find total count 
	private static int findCountAndSetupVisited(int[][] maze){
		int count = 0;
		for(int i=0; i<mazeHeight+2; i++) {
			for(int j=0; j<mazeWidth+2; j++) {
				if(maze[i][j]==free) {
					count++;
				}
				if(maze[i][j]==barrier) {
					visitedMaze[i][j] = visited;
				}
			}
		}
		return count;
	}
	
	private static void checkBattery(int[][] maze) throws Exception {

		int minNeededStep = 0;
		for(int i=0; i<mazeHeight+2; i++) {
			for(int j=0; j<mazeWidth+2; j++) {
				minNeededStep = Math.max(minNeededStep, maze[i][j]);
			}
		}
		if(minNeededStep>maxStep/2) {
			throw new Exception("battery is not enough");
		}
	}
	
	private static void writeStep(String step) {
		try {
			FileWriter fw = new FileWriter("output.txt", true);
			fw.write(step+"\n");
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		
		FileReader fr;
		try {
			fr = new FileReader("input.txt");
			BufferedReader bufferedReader = new BufferedReader(fr);
			String firstLine = bufferedReader.readLine();
			System.out.println(firstLine);
			String[] initAry = firstLine.split(" ");
			mazeHeight = Integer.parseInt(initAry[0]);
			mazeWidth = Integer.parseInt(initAry[1]);
			maxStep = Integer.parseInt(initAry[2]);
			maze = new int[mazeHeight+2][mazeWidth+2];
			visitedMaze = new int[mazeHeight+2][mazeWidth+2];
			// set up side barrier
			for(int i=0; i<mazeHeight+2; i++) {
				for(int j=0; j<mazeWidth+2; j++)
					if( (i==mazeHeight+1 || i==0) || (j==mazeWidth+1 || j==0) )
						maze[i][j] = barrier;
			}
			String in = "";
			int row = 1;
			while((in = bufferedReader.readLine())!=null) {
				String[] inAry = in.split(" ");
				for(int i=0; i<inAry.length; i++) {
					if(inAry[i].equals("R")) {
						entryRow = row;
						entryCol = i+1;
						maze[entryRow][entryCol] = start;
					}
					else if(inAry[i].equals("1")) maze[row][i+1] = barrier;
					else if(inAry[i].equals("0")) maze[row][i+1] = free;
				}
				row++;
			}
	        fr.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// setup count
		totalCount = findCountAndSetupVisited(maze);
		findRoad(maze, entryRow, entryCol);

		// check if battery is enough 
		try {
			checkBattery(maze);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return;
		}		
		
		for(int[] ary: maze) {
			System.out.println(Arrays.toString(ary));
		}
		
		// do main algorithm
		sweepAll(maze, entryRow, entryCol);

	}
	
	
	
	
	
	public static void DebugTest(int[][] maze) {
		// for debug
		System.out.println("===================");
		for(int[] ary: visitedMaze) {
			System.out.println(Arrays.toString(ary));
		}
		System.out.println("");
		try {
			TimeUnit.NANOSECONDS.sleep(1000000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void DebugStack(Stack stack) {
		ArrayList<int[]> list = new ArrayList<int[]>(stack);
		ArrayList<String> str = new ArrayList<>();
		for(int i=0; i<list.size(); i++) {
			str.add(Arrays.toString(list.get(i)));
		}
		System.out.println(str.toString());
	}
	public static void DebugQueue(Queue queue) {
		ArrayList<int[]> list = new ArrayList<int[]>(queue);
		ArrayList<String> str = new ArrayList<>();
		for(int i=0; i<list.size(); i++) {
			str.add(Arrays.toString(list.get(i)));
		}
		System.out.println(str.toString());
	}
}




/*
 * 
 * 
[-3, -3, -3, -3, -3, -3, -3]
[-3,  0, -3,  8,  7,  8, -3]
[-3,  1,  2, -3,  6,  7, -3]
[-3,  2,  3,  4,  5, -3, -3]
[-3,  3,  4,  5,  6,  7, -3]
[-3,  4,  5,  6,  7,  8, -3]
[-3, -3, -3, -3, -3, -3, -3]
 * 
 * */