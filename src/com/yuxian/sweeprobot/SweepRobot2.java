package com.yuxian.sweeprobot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.TimeUnit;

public class SweepRobot2 {
	
	// [up, right, down, left]
	public static int[][] directions = new int[][] {{-1,0},{0,1},{0,-1},{1,0}};
			
	public static int barrier = -3;
	
	public static int start = 0;
	
	public static int free = -2;
	
	public static int visited = 1;
	
	public static int minStep = 9;
	
	public static int mazeHeight = 5;
	
	public static int mazeWidth = 5;
	
	public static int totalCount = 0;
	
	public static int entryRow = 1;
	
	public static int entryCol = 1;
	
	public static Stack<int[]> thisRoad = new Stack<>();
	
	public static Queue<int[]> tempRoad = new LinkedList<>();
	
	public static Queue<int[]> waitedRoad = new LinkedList<>();
	
	public static boolean hasWaitedRoad = false;

	public static boolean holdRoad = false;

	public static int[][] visitedMaze = new int[mazeHeight+2][mazeWidth+2];
	
	// calculate the distance from entry
 	public static void findRoad(int[][] maze, int row, int col) {
		for(int i=0; i<directions.length; i++) {
			int rowMove = directions[i][0];
			int colMove = directions[i][1];
			int next = maze[row+rowMove][col+colMove];
			if( next!=barrier && next!=start && (next>maze[row][col]+1 || next == free) ) {
				maze[row+rowMove][col+colMove] = maze[row][col]+1;
				findRoad(maze,row+rowMove,col+colMove);
			}
		}
	}
	
 	// sweep next road
	private static int[] sweep(int[][] maze, int row, int col, int prev) {
		// psuedo code
		/*
		 * if(!hasWaitedRoad){
			 * hasNext = true
			 * nowStep
			 * nextStep
			 * 
			 * <--- check what is next step --->
			 * if(nowStep<minStep and nowStep>prev){
			 * 		for dir in directions{
			 * 			if there is next then 
			 * 				set hasNext to true
			 * 			if there is more then two next then 
			 * 				store the second to waitedRoad and store tempRoad to waitedRoad too
			 * 				set hasWaitedRoad to true
			 * 		}
			 * }else{
			 * 		hasNext = false
			 * }
			 * 
			 * <--- go next step --->
			 * if(hasNext){
			 *		nextStep = nowStep + 1 
			 *		check all dir and go next
			 *		thisRoad.push(dir)
			 *		if(!hasWaitedRoad){
			 *			tempRoad.push(dir)
			 *		}
			 * }else{
			 * 		thisRoad.pop()
			 * }
		 * }else{
		 * 		if(!waitedRoad.isEmpty){
		 * 			waitedRoad.pop()
		 * 		}
		 * 		if(watedRoad.isEmpty()){
		 * 			hasWaitedRoad = false
		 * 		}
		 * }
		 * 
		 * */

		boolean hasNext = false;
		int nowStep = maze[row][col];
		int nextStep = 0;
		int[] now = new int[] {row,col};
		int[] next = new int[] {row,col,prev};
		
//		System.out.println("waitedRoad: ");
//		DebugQueue(waitedRoad);
//		System.out.println("tempRoad: ");
//		DebugQueue(tempRoad);
//		System.out.println("thisRoad: ");
//		DebugStack(thisRoad);
		if(!hasWaitedRoad) {
			// <--- check what is next step --->
			if(nowStep<minStep && (nowStep>prev || nowStep==0)) {
				int countNext = 0;
				int countVisited = 0;
				// check is there any unvisited road
				for(int[] dir: directions) {
					int nRow = now[0]+dir[0];
					int nCol = now[1]+dir[1];
					if(maze[nRow][nCol]==maze[row][col]+1 && visitedMaze[nRow][nCol]!=visited) {
						if(!holdRoad) {
							if(countNext==1) {
//								System.out.println("hold");
								while(!tempRoad.isEmpty()) {
									waitedRoad.offer(tempRoad.poll());
								}
								waitedRoad.offer(new int[] {row,col});
								waitedRoad.offer(new int[] {nRow,nCol});
								holdRoad = true;
								break;
							}
							hasNext = true;
							countNext++;
						}else {
							hasNext = true;
							break;
						}
					}
					if(visitedMaze[nRow][nCol]==visited) {
						countVisited++;
					}
				}
//				System.out.println(countVisited);
				if(countVisited==directions.length) {
					hasNext = true;
				}
			}
			
			//<--- go next step --->
//			System.out.println(Arrays.toString(now));
//			System.out.println(nowStep+"\t"+prev);
//			System.out.println(hasNext);
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
				// check second priority which has next one and visited
				if(next[0]==now[0] && next[1]==now[1]) {
					for(int[] dir: directions) {
						nRow = now[0]+dir[0];
						nCol = now[1]+dir[1];
						if(maze[nRow][nCol]==nextStep && visitedMaze[nRow][nCol]==visited) {
							next[0] = nRow;
							next[1] = nCol;
							next[2] = nowStep;
							break;
						}
					}
				}
				
				// if you can not find next one now then it means you don't have one
				if(next[0]==now[0] && next[1]==now[1]) {
					hasNext = false;
				}else {
					thisRoad.push(new int[] {row,col});
					if(!holdRoad) {
						tempRoad.offer(new int[] {row,col});
					}
				}
			}
			if(!hasNext){
				int[] road = thisRoad.pop();
				next[0] = road[0];
				next[1] = road[1];
				next[2] = nowStep;
				visitedMaze[road[0]][road[1]] = visited;
			}
		}else {
//			System.out.println("hasWaitedRoad");
			if(!waitedRoad.isEmpty()) {
				int[] road = waitedRoad.poll();
				next[0] = road[0];
				next[1] = road[1];
				next[2] = nowStep;
				visitedMaze[road[0]][road[1]] = visited;
				if(!waitedRoad.isEmpty()) {
					thisRoad.push(new int[] {road[0],road[1]});
					tempRoad.offer(new int[] {road[0],road[1]});
				}else {
					totalCount--;
				}
			}
			if(waitedRoad.isEmpty()) {
				hasWaitedRoad = false;
			}
		}
		
		return next;
	}
	
	// sweep all road
	public static void sweepAll(int[][] maze, int row, int col) {
		int[] now = sweep(maze,row,col,-1);
		visitedMaze[row][col] = visited;
		totalCount--;
		System.out.println("====================");
		System.out.println(Arrays.toString(now));
		DebugTest(maze);
		while(now[0]!=row || now[1]!=col || totalCount>0){
			int[] next = sweep(maze,now[0],now[1],now[2]);
			now[0] = next[0];
			now[1] = next[1];
			now[2] = next[2];
			System.out.println(Arrays.toString(now));
			if(holdRoad) {
				if(now[0]==row && now[1]==col) {
					hasWaitedRoad = true;
					holdRoad = false;
				}
			}
			System.out.println(totalCount);
			DebugTest(maze);
		}
	}
	
	// find total count 
	private static int findCountAndSetupVisited(int[][] maze) {
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
	
	public static void main(String[] args) {
		int[][] test = new int[mazeHeight+2][mazeWidth+2];
		
		
		// set up maze
		for(int i=0; i<mazeHeight+2; i++) {
			for(int j=0; j<mazeWidth+2; j++)
				test[i][j] = free;
		}
		
		// set up side barrier
		for(int i=0; i<mazeHeight+2; i++) {
			for(int j=0; j<mazeWidth+2; j++)
				if( (i==mazeHeight+1 || i==0) || (j==mazeWidth+1 || j==0) )
					test[i][j] = barrier;
		}
		
		
		// set up some other barrier
		int[][] barriers = new int[][] { {1,2}, {2,3}, {3,5}};
		for(int i=0; i<barriers.length; i++) {
			test[barriers[i][0]][barriers[i][1]] = barrier;
		}
		
		
		// setup count
		totalCount = findCountAndSetupVisited(test);
		
		for(int[] ary: test) {
			System.out.println(Arrays.toString(ary));
		}
		
		System.out.println("");
		test[1][1] = start;
		
		findRoad(test, entryRow, entryCol);
		

		for(int[] ary: test) {
			System.out.println(Arrays.toString(ary));
		}
		sweepAll(test, entryRow, entryCol);

	}
	
	
	public static void DebugTest(int[][] maze) {

		
		// for debug
		System.out.println("");
		for(int[] ary: visitedMaze) {
			System.out.println(Arrays.toString(ary));
		}
		System.out.println("");
		System.out.println("=========================");
		try {
			TimeUnit.NANOSECONDS.sleep(200000000);
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
