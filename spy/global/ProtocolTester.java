package spy.global;

import spy.sim.Point;
import spy.global.Protocol;

import java.util.List;
import java.util.ArrayList;

public class ProtocolTester {
	public static void main(String[] args) {
		// defines test cases
		int[][] expected = {
			{-1,0}, //1
			{-1,-1}, //2
			{0,-1}, //3
			{0,-1}, //4
			{0,0}, //5
			{0,0}, //6
			{0,0}, //7
			{-1,0}, //8
			{-1,0}, //9
			{-1,-1}, //10
			{-1,-1}, //11
			{-1,-1}, //12
			{0,-1}, //13
			{1,-1}, //14
			{1,-1}, //15
			{1,0}, //16
			{1,0}, //17
			{1,0}, //18
			{1,1}, //19
			{0,1}, //20
			{0,1}, //21
			{0,1}, //22
			{-1,1}, //23
			{-1,1}, //24
			{-1,0}, //25
			{0,-1}, //26
			{1,0}, //27
			{0,1} //28
		};

		int currentX = 50;
		int currentY = 50;

		int[][][] targets = {
			{{49,50}}, //1
			{{49,49}}, //2
			{{50,49}}, //3
			{{51,49}}, //4
			{{51,50}}, //5
			{{51,51}}, //6
			{{50,51}}, //7
			{{49,51}}, //8
			{{48,50}}, //9
			{{48,49}}, //10
			{{48,48}}, //11
			{{49,48}}, //12
			{{50,48}}, //13
			{{51,48}}, //14
			{{52,48}}, //15
			{{52,49}}, //16
			{{52,50}}, //17
			{{52,51}}, //18
			{{52,52}}, //19
			{{51,52}}, //20
			{{50,52}}, //21
			{{49,52}}, //22
			{{48,52}}, //23
			{{48,51}}, //24
			{{47,50}}, //25
			{{50,47}}, //26
			{{53,50}}, //27
			{{50,53}} //28
		};

		// perform all tests
		System.err.println("Starting tests");

		for (int i = 0; i < expected.length; ++i) {
			//System.err.print(".");
			Point exp = new Point(expected[i][0], expected[i][1]);
			Point cur = new Point(currentX, currentY);
			String expect = "expected=(" + exp.x + "," + exp.y + ")";

			int[][] target = targets[i];
			List<Point> tar = new ArrayList(target.length);
			for (int j = 0; j < target.length; ++j) {
				tar.add(new Point(target[j][0], target[j][1]));
			}

			Point res = Protocol.getMoveToMeet(cur, tar);
			String result = "result=(" + res.x + "," + res.y + ")";

			//System.err.println(res.x==exp.x && res.y==exp.y);
			assert (res.x==exp.x && res.y==exp.y) : "Wrong answer: test=" + (i+1) + " " + expect + " " + result;
		}

		System.err.println("All tests pass");
	}
}