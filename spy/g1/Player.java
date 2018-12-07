package spy.g1;

import java.util.Collections;
import java.util.List;
import java.util.LinkedList;
import java.util.Queue;
import java.util.ArrayList;
import java.util.Random;

import spy.g1.Edge;

//import javafx.scene.shape.MoveTo;

import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;

import spy.sim.Point;
import spy.sim.Record;
import spy.sim.CellStatus;
import spy.sim.Simulator;
import spy.sim.Observation;

public class Player implements spy.sim.Player {

    private ArrayList<ArrayList<Record>> records;
    //private ArrayList<ArrayList<Record>> map;
    //private HashMap<Point,Boolean> visited;
    private int id;
    private Point loc;
    private HashMap<String,Point> water = new HashMap<String,Point>();
    //private HashSet existingEdges = new HashSet();
    private Dijkstra djk = new Dijkstra();

    private Point packageLocation;
    private Point targetLocation;
    private int moveMode;
    private boolean findPackage, findTarget;
    private List<Point> ourPath;
    private Queue<Vertex> moves = new LinkedList<>();
    private HashMap<Integer, Point> allSoldiers = new HashMap<Integer, Point>();

    private HashMap<Integer,Integer> meetups;
    private static final int minMeetWaitTime = 25;


    public void init(int n, int id, int t, Point startingPos, List<Point> waterCells, boolean isSpy)
    {
        // Hashmap of water cells for more efficient check
        for (Point w : waterCells){
          int x = w.x;
          int y = w.y;
          String p = Integer.toString(x) + "," + Integer.toString(y);
          water.put(p, w);
          // System.out.print(water.containsKey(p));
          // System.out.println(p);
        }

        // Construct Dijkstra graph of land cells
        this.id = id;
        this.records = new ArrayList<ArrayList<Record>>();
        this.ourPath = new ArrayList<Point>();
        //this.map = new ArrayList<ArrayList<Record>>();
        //this.visited = new HashMap<Point,Boolean>();
        for (int i = 0; i < 100; i++)
        {
            ArrayList<Record> row = new ArrayList<Record>();
            for (int j = 0; j < 100; j++)
            {
                String name = Integer.toString(i) + "," + Integer.toString(j);
                //System.out.print(water.contains(newVertex));
                if(!water.containsKey(name)){
                  Vertex newVertex = new Vertex(name,i,j);
                  djk.addVertex(newVertex);
                  // System.out.println(newVertex);
                }
                row.add(null);
                //visited.put(new Point(i,j),false);
            }
            this.records.add(row);
        }
        for (Vertex source : djk.getVertices()){
            // construct edge weights -- assume muddy
            setIncomingEdges(source, true);
        }
        // doesn't know package location or target location at beginning
        this.findPackage = false;
        this.findTarget = false;
        this.moveMode = 0;
        // moveMode = 0, initial exploration
        // moveMode = 1, saw package or target
        // moveMode = 2, reached package or target -- looking for other one
        // moveMode = 3, saw the other one -- trying to reach target
        // moveMode = 4, saw the other one -- go to package to propose path
        // moveMode = 5, done -- just stay put
    }

    private void setIncomingEdges(Vertex source, boolean isMuddy) {
        int x = source.x;
        int y = source.y;
        int[][] adjacent = {
            {x-1,y-1},
            {x-1,y},
            {x-1,y+1},
            {x,y+1},
            {x+1,y+1},
            {x+1,y},
            {x+1,y-1},
            {x,y-1}
        };

        for (int k = 0; k < adjacent.length; ++k) {
            int i = adjacent[k][0], j = adjacent[k][1];
            String name = Integer.toString(i) + "," + Integer.toString(j);

            if(i>=0 && i<=99 && j>=0 && j<=99 && !water.containsKey(name)) {
                Vertex target = djk.getVertex(name);
                Vertex[] key = {target, source};
                double weight = (k%2==0) ? 3 : 2;
                if (isMuddy) {
                  if (moveMode<2 || moveMode>3) {weight *= 2;}
                  if (moveMode>=2){weight = Double.POSITIVE_INFINITY;}
                }
                djk.setEdge(target.name, source.name, weight);
                //existingEdges.add(key);
            }
        }
    }

    // updates the state of the player based on surroundings
    public void observe(Point loc, HashMap<Point, CellStatus> statuses)
    {
        // update location
        this.loc = loc;

        // Clear observed soldiers
        allSoldiers.clear();
        for (Map.Entry<Point, CellStatus> entry : statuses.entrySet())
        {
            Point p = entry.getKey();
            CellStatus status = entry.getValue();
            ArrayList<Integer> cellSoldiers = status.getPresentSoldiers();
            for(int soldierID : cellSoldiers){
              allSoldiers.put(soldierID, p);
            }

            // record the data learned
            Record record = records.get(p.x).get(p.y);
            if (record == null || record.getC() != status.getC() || record.getPT() != status.getPT())
            {
                ArrayList<Observation> observations = new ArrayList<Observation>();
                record = new Record(p, status.getC(), status.getPT(), observations);
                records.get(p.x).set(p.y, record);
            }
            //map.get(p.x).set(p.y, new Record(p, status.getC(), status.getPT(), new ArrayList<Observation>()));
            record.getObservations().add(new Observation(this.id, Simulator.getElapsedT()));
            update(record);
        }
        // Observed soldiers
        if(!allSoldiers.isEmpty()){
          int lowestID = this.id;
          Point toVisit = null;
          for (Map.Entry<Integer, Point> entry: allSoldiers.entrySet()){
            int id = entry.getKey();
            Point soldierLoc = entry.getValue();
            if (id < lowestID){
              lowestID = id;
              toVisit = soldierLoc;
            }
          }
        }
    }

    public List<Record> sendRecords(int id)
    {
        ArrayList<Record> toSend = new ArrayList<Record>();

        if (Simulator.getElapsedT() - meetups.getOrDefault(id, 0) < minMeetWaitTime) {
            for (ArrayList<Record> row : records)
            {
                for (Record record : row)
                {
                    if (record != null)
                    {
                        toSend.add(record);
                    }
                }
            }

            meetups.put(id, Simulator.getElapsedT());
        }

        return toSend;
    }

    public void receiveRecords(int id, List<Record> records)
    {
        for(Record rec: records) {
        	// record the data learned
        	Point p = rec.getLoc();
          Record record = this.records.get(p.x).get(p.y);
          if (record == null || record.getC() != rec.getC() || record.getPT() != rec.getPT())
          {
              ArrayList<Observation> observations = new ArrayList<Observation>();
              record = new Record(rec);
              this.records.get(p.x).set(p.y, record);
          }
          //map.get(p.x).set(p.y, new Record(p, status.getC(), status.getPT(), new ArrayList<Observation>()));
          record.getObservations().add(new Observation(this.id, Simulator.getElapsedT()));
          update(rec);
        }
    }

    private void update(Record record) {
    	Point p = record.getLoc();
    	// check tile status
        if(record.getPT() != 0) {
            switch(moveMode) {
                case 0:
                    moveMode = 1; // reach the first special tile
                    break;
                case 2:
                    if(findPackage && record.getPT()==2) {
                        moveMode = 3;
                        // found package first and just discorvered target
                        // move to target
                    } else if(findTarget && record.getPT()==1) {
                        moveMode = 4;
                        // found target first and just discovered package
                        // just go to package and we're done
                    }
                    break;
                default:
                    break;
            }
            if(record.getPT()==1) {
                this.findPackage = true;
                this.packageLocation = p;
            } else {
                this.findTarget = true;
                this.targetLocation = p;
            }
        }
        // update the graph to reflect new information
        String name = Integer.toString(p.x) + "," + Integer.toString(p.y);
        if(!water.containsKey(name)) {
        	Vertex v = djk.getVertex(name);
        	v.explored = true;
	        setIncomingEdges(v, record.getC()==1);
        }

        // check on location
        boolean atPackage = this.loc.equals(packageLocation);
        boolean atTarget = this.loc.equals(targetLocation);
        if(atPackage || atTarget) {
            switch(moveMode) {
                case 1:
                    moveMode = 2;
                    // update graph so all muddy edges are infinite
                    for (Vertex source : djk.getVertices()){
                        Record r = records.get(source.x).get(source.y);
                        if(r!=null && r.getC()==1) {
                            setIncomingEdges(source, true);
                        }
                    }
                    break;
                case 3:
                    if(atTarget) {moveMode = 4;}
                    break;
                case 4:
                    if(atPackage) {moveMode = 5;}
                    break;
                default:
                    break;
            }
        }
    }

    public List<Point> proposePath()
    {
        //update all map weights before proposal
        for(int i = 0; i<100; i++){
          for(int j = 0; j<100; j++){
            Record rec = records.get(i).get(j);
            if(rec != null){
              int muddy = rec.getC();
              if(muddy == 1){
                String name = Integer.toString(i) + "," + Integer.toString(j);
                Vertex v = djk.getVertex(name);
                setIncomingEdges(v, true);
              }
            }
          }
        }

        String packageLoc;
        String targetLoc;
        packageLoc = Integer.toString(packageLocation.x) + "," + Integer.toString(packageLocation.y);
        targetLoc = Integer.toString(targetLocation.x) + "," + Integer.toString(targetLocation.y);

        List<Edge> validPath = djk.getDijkstraPath(packageLoc,targetLoc);
        for(int i=0;i<validPath.size();i++){
            Vertex nextVertex = validPath.get(i).target;
            Point nextPoint = new Point(nextVertex.x,nextVertex.y);
            System.out.println("step"+i+nextPoint);
            this.ourPath.add(nextPoint);
        }
        if(ourPath.size()>1){
            return ourPath;
        }
        return null;
    }

    public List<Integer> getVotes(HashMap<Integer, List<Point>> paths)
    {
        for (Map.Entry<Integer, List<Point>> entry : paths.entrySet())
        {
            ArrayList<Integer> toReturn = new ArrayList<Integer>();
            toReturn.add(entry.getKey());
            return toReturn;
        }
        return null;
    }
    public void receiveResults(HashMap<Integer, Integer> results)
    {

    }

    // runs algorithms to decide which move to make based on the current state
    public Point getMove()
    {
        System.out.println(moveMode);
        //System.err.println(moveMode);
        if(!this.moves.isEmpty()){
          Vertex curNext = moves.peek();
          if(moveMode >= 2 && moveMode <=4){
            Record rec = records.get(curNext.x).get(curNext.y);
            int muddy = rec.getC();
            if(muddy == 1){
              this.moves.clear();
              System.out.println("Clearing queue");
            }
          }
        }
        // moveMode = 0, initial exploration
        // moveMode = 1, saw package or target
        // moveMode = 2, reached package or target -- looking for other one
        // moveMode = 3, saw the other one -- trying to reach target
        // moveMode = 4, reached the other one -- go to package to propose path
        // moveMode = 5, done -- just stay put
        if(this.moves.isEmpty()){
          List<Edge> curPath;
          String source = Integer.toString(loc.x) + "," + Integer.toString(loc.y);
          String target;
          switch(moveMode) {
              case 0:
                  curPath = djk.getShortestPathToUnexplored(source);
                  break;

              case 1:
                  if(findPackage) {
                      target = Integer.toString(packageLocation.x) + "," + Integer.toString(packageLocation.y);
                  } else {
                      target = Integer.toString(targetLocation.x) + "," + Integer.toString(targetLocation.y);
                  }
                  curPath = djk.getDijkstraPath(source, target);
                  break;

              case 2:
                  curPath = djk.getShortestPathToUnexplored(source);
                  break;

              case 3:
                  target = Integer.toString(targetLocation.x) + "," + Integer.toString(targetLocation.y);
                  curPath = djk.getDijkstraPath(source, target);
                  break;

              case 4:
                  target = Integer.toString(packageLocation.x) + "," + Integer.toString(packageLocation.y);
                  curPath = djk.getDijkstraPath(source, target);
                  break;

              default:
                  return new Point(0,0);
          }
          for(Edge e : curPath){
            Vertex next = e.target;
            moves.add(next);
          }
        }
        Vertex nextMove = moves.poll();

        return new Point(nextMove.x - loc.x, nextMove.y - loc.y);
    }
}
