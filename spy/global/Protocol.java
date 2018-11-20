package spy.global;

import spy.sim.Point;
import java.util.List;

public class Protocol {
	// time to wait before meeting the same player again
	public static final int MEET_WAIT_TIME = 25;

	// 4 points are in the 'center'. Choose the northwestern one
	public static final Point CENTER = new Point(49,49);

	/* Returns the move that will bring the player to the same point
	as the players in the list of targets, following Bohan's protocol.
	Note: assumes the path between is unobstructed. */
	public static Point getMoveToMeet(Point current, List<Point> targets) {
		// find the northwest corner of the smallest rectangle containing all the targets.
		Point nw = new Point(100,100); // off southest corner of map
		for (Point p : targets) {
			nw.x = Math.min(nw.x, p.x);
			nw.y = Math.min(nw.y, p.y);
		}

		// compute relative step
		Point rel = new Point(nw.x-current.x, nw.y-current.y);

		// take a step towards meeting nw target point -- or stay put
		int dx = (rel.x==0 || (rel.x==1 && rel.y>=-1) || (rel.x==-1 && rel.y>=2)) ? 0 : (int)Math.signum(rel.x);
		int dy = (rel.y==0 || (rel.y==1 && rel.x>=-1) || (rel.y==-1 && rel.x>=2)) ? 0 : (int)Math.signum(rel.y);

		// return the step
		return new Point(dx, dy);
	}
}