package com.yuxian.sweeprobot;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class Test {
	
	// [up, down, right, left]
	public static int[][] directions = new int[][] {{-1,0},{0,1},{0,-1},{1,0}};
	
	public static int barrier = -1;
	
	public static int start = 0;
	
	public static int free = -2;
	
	public static int visited = -3;
	
	public static int minStep = 8;
	
	public static int mazeHeight = 6;
	
	public static int mazeWidth = 5;
	
	public static int totalCount = 20;

	public static int[][] visitedMaze = new int[mazeHeight+2][mazeWidth+2];
	
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
	
	public static int[] sweep(int[][] maze, int row, int col, int prev) {
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
		boolean hasNext = false;
		
		if(nowStep < minStep) {
			for(int[] dir: directions) {
				if(maze[now[0]+dir[0]][now[1]+dir[1]] == maze[now[0]][now[1]]+1) {
					hasNext = true;
				}
			}
		}
		
		if(nowStep>=minStep || !hasNext || prev>nowStep) {
			nowStep--;
		}else {
			nowStep++;
		}

		for(int[] dir: directions) {
			int next = maze[now[0]+dir[0]][now[1]+dir[1]];
			int v = visitedMaze[now[0]+dir[0]][now[1]+dir[1]];
			if(next == nowStep && next!=barrier && v!=visited) {
				visitedMaze[now[0]+dir[0]][now[1]+dir[1]] = visited;
				now[0] = now[0]+dir[0];
				now[1] = now[1]+dir[1];
				totalCount--;
				break;
			}
		}
		
		if(prevNow[0]==now[0] && prevNow[1]==now[1]) {
			for(int[] dir: directions) {
				int next = maze[now[0]+dir[0]][now[1]+dir[1]];
				int v = visitedMaze[now[0]+dir[0]][now[1]+dir[1]];
				if(next == nowStep && next!=barrier) {
					visitedMaze[now[0]+dir[0]][now[1]+dir[1]] = visited;
					now[0] = now[0]+dir[0];
					now[1] = now[1]+dir[1];
					break;
				}
			}
		}
		
		return new int[] {now[0], now[1], prevStep};
	}
	
	public static void sweepAll(int[][] maze, int row, int col) {
		int[] now = sweep(maze,row,col,0);
		while(now[0]!=row || now[1]!=col || totalCount!=0){
			int[] next = sweep(maze,now[0],now[1],now[2]);
			now[0] = next[0];
			now[1] = next[1];
			now[2] = next[2];
			DebugTest(maze);
			System.out.println(Arrays.toString(now));
		}
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
		int[][] barriers = new int[][] { {1,2}, {2,3}, {3,5}, {5,6} };
		for(int i=0; i<barriers.length; i++) {
			test[barriers[i][0]][barriers[i][1]] = barrier;
		}
		
		for(int[] ary: test) {
			System.out.println(Arrays.toString(ary));
		}
		
		System.out.println("");
		test[1][1] = start;
		findRoad(test, 1, 1);
		sweepAll(test,1,1);
//		sweep(test,2,1);
//		

		
		for(int[] ary: visitedMaze) {
			System.out.println(Arrays.toString(ary));
		}	
		System.out.println("");
		for(int[] ary: test) {
			System.out.println(Arrays.toString(ary));
		}
	}
	
	public static void DebugTest(int[][] maze) {

		
		// for debug
		for(int[] ary: visitedMaze) {
			System.out.println(Arrays.toString(ary));
		}
		System.out.println("");
		for(int[] ary: maze) {
			System.out.println(Arrays.toString(ary));
		}
		try {
			TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("=========================");
		System.out.println("");
	}
}
