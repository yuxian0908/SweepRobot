package com.yuxian.sweeprobot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;
import java.util.concurrent.TimeUnit;

public class SweepRobot {
	
	// [up, down, right, left]
	public static int[][] directions = new int[][] {{-1,0},{0,1},{0,-1},{1,0}};
			
	public static int barrier = -3;
	
	public static int start = 0;
	
	public static int free = -2;
	
	public static int visited = 1;
	
	public static int minStep = 9;
	
	public static int mazeHeight = 6;
	
	public static int mazeWidth = 5;
	
	public static int totalCount = 0;
	
	public static boolean waited = false;
	
	public static boolean hold = false;
	
	public static Stack<int[]> waitedRoad = new Stack<>();

	public static int[][] visitedMaze = new int[mazeHeight+2][mazeWidth+2];
	
	// calculate the distance from entry
 	public static void findRoad(int[][] maze, int row, int col) {
		// psuedo code
		/*
		 * for dir in [up, down, right, left]
		 * 		if up is not -1
		 * 			up = maze[row][col] + 1
		 * 
		 * */
		
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
		 * prevNow = now
		 * 
		 * <-------- check next -------->
		 * if now < minStep
		 * 		for directions
		 * 			if directions == now+1
		 * 				count++
		 * 
		 * if now>=minStep || count==0
		 * 		next--
		 * else
		 * 		next++
		 * 
		 * <-------- go next if not visited -------->
		 * for directions
		 * 		if directions == next && directions != barrier && directions != visited
		 * 			now = directions
		 * 
		 * <-------- go next if visited -------->
		 * if prevNow == now
		 * 		for directions
		 * 			if directions == next && directions != barrier
		 * 				now = directions
		 * 
		 * */
		
		int[] now = new int[] {row,col};
		int[] prevNow = new int[] {row,col};
		int nowStep = maze[now[0]][now[1]];
		int prevStep = nowStep;
		int nextStep = nowStep;
		boolean hasNext = false;

		// if there is waitedRoad, then go that road first
		if(!waited) {
			
			// determine whether next step is bigger or smaller
			if(nowStep < minStep) {
				for(int[] dir: directions) {
					int v = visitedMaze[now[0]+dir[0]][now[1]+dir[1]];
					if(maze[now[0]+dir[0]][now[1]+dir[1]] == maze[now[0]][now[1]]+1 && v!=visited) {
						hasNext = true;
						break;
					}
					if(maze[now[0]+dir[0]][now[1]+dir[1]] == maze[now[0]][now[1]]-1 && v!=visited) {
						hasNext = false;
						break;
					}
					if(maze[now[0]+dir[0]][now[1]+dir[1]] == maze[now[0]][now[1]]+1 && v==visited) {
						hasNext = true;
					}
				}
			}
			
			
			// if you come to some other road in the way back to entry, store it into waitedRoad
			if(nowStep>=minStep || !hasNext || prev>nowStep) {
				nextStep--;
				boolean isNext = true;
				for(int[] dir: directions) {
					int next = maze[now[0]+dir[0]][now[1]+dir[1]];
					int v = visitedMaze[now[0]+dir[0]][now[1]+dir[1]];
					if(nextStep<nowStep && next!=barrier && next!=nowStep && v!=visited ) {
						if(!hold) {
							if(next>nowStep) {
								waitedRoad.add(new int[] {now[0]+dir[0],now[1]+dir[1]});
							}else {
								if(isNext) {
									continue;
								}
								isNext = false;
							}
							waitedRoad.add(new int[] {now[0],now[1]});
							hold = true;
						}
					}
				}
			}else {
				nextStep++;
			}

			// go next step which is not visited first
			for(int[] dir: directions) {
				int next = maze[now[0]+dir[0]][now[1]+dir[1]];
				int v = visitedMaze[now[0]+dir[0]][now[1]+dir[1]];
				if(next == nextStep && next!=barrier && v!=visited) {
					visitedMaze[now[0]+dir[0]][now[1]+dir[1]] = visited;
					now[0] = now[0]+dir[0];
					now[1] = now[1]+dir[1];
					totalCount--;
					break;
				}
			}
			
			// go next step visited when there is no other way
			if(prevNow[0]==now[0] && prevNow[1]==now[1]) {
				for(int[] dir: directions) {
					int next = maze[now[0]+dir[0]][now[1]+dir[1]];
					if(next == nextStep && next!=barrier) {
						visitedMaze[now[0]+dir[0]][now[1]+dir[1]] = visited;
						now[0] = now[0]+dir[0];
						now[1] = now[1]+dir[1];
						break;
					}
				}
			}
			
			
		}else {
			// go the waitedRoad
			if(!waitedRoad.isEmpty()) {
				int[] road = waitedRoad.pop();
				int v = visitedMaze[road[0]][road[1]];
				if(v!=visited) {
					totalCount--;
					System.out.println(totalCount);
				}
				visitedMaze[road[0]][road[1]] = visited;
				now[0] = road[0];
				now[1] = road[1];
			}else{
				waited = false;
			}
		}
		

		return new int[] {now[0], now[1], prevStep};
	}
	
	// sweep all road
	public static void sweepAll(int[][] maze, int row, int col) {
		int[] now = sweep(maze,row,col,0);
		while(now[0]!=row || now[1]!=col || totalCount>0){
			int[] next = sweep(maze,now[0],now[1],now[2]);
			now[0] = next[0];
			now[1] = next[1];
			now[2] = next[2];
			
			// if you spot some other road when you are going back to entry, store it in the waitedRoad stack
			if(hold) {
				if(now[0]==row && now[1]==col) {
					waited = true;
					hold = false;
				}else {
					waitedRoad.add(new int[] {now[0],now[1]});
				}
			}
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
		int[][] barriers = new int[][] { {1,2}, {2,3}, {3,5}, {5,6}, {5,5}, {1,5}, {3,3}, {4,1}, {5,3}, {6,2} };
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
		
		findRoad(test, 1, 1);
		sweepAll(test,1,1);

	}
	
	
	public static void DebugTest(int[][] maze) {

		
		// for debug
		for(int[] ary: visitedMaze) {
			System.out.println(Arrays.toString(ary));
		}
		System.out.println("");
		System.out.println("=========================");
		try {
			TimeUnit.NANOSECONDS.sleep(100000000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void DebugWaitedRoad() {
		ArrayList<int[]> list = new ArrayList<int[]>(waitedRoad);
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
[-1, -1, -1, -1, -1, -1, -1]
[-1,  0, -1,  8,  7,  8, -1]
[-1,  1,  2, -1,  6,  7, -1]
[-1,  2,  3,  4,  5, -1, -1]
[-1,  3,  4,  5,  6,  7, -1]
[-1,  4,  5,  6,  7,  8, -1]
[-1,  5,  6,  7,  8,  9, -1]
[-1, -1, -1, -1, -1, -1, -1]
 * 
 * */
