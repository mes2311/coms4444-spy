package spy.g1;

import java.util.List;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

import spy.g1.Edge;

import javafx.scene.shape.MoveTo;

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
    private ArrayList<ArrayList<Record>> map;
    private HashMap<Point,Boolean> visited;
    private int id;
    private Point loc;
    private HashSet water = new HashSet();
    private HashSet existingEdges = new HashSet();
    private Dijkstra djk = new Dijkstra();
    private Boolean findPackage;
    private Boolean findTarget;
    private Point packageLocation;
    private Point targetLocation;
    private int moveMode;
    private int state;
    private boolean targetFound, packageFound;


    public void init(int n, int id, int t, Point startingPos, List<Point> waterCells, boolean isSpy)
    {
        // Hashmap of water cells for more efficient check
        for (Point w : waterCells){
          int x = w.x;
          int y = w.y;
          int[] p = {x,y};
          water.add(p);
        }

        // Construct Dijkstra graph of land cells
        this.id = id;
        this.records = new ArrayList<ArrayList<Record>>();
        this.map = new ArrayList<ArrayList<Record>>();
        this.visited = new HashMap<Point,Boolean>();
        for (int i = 0; i < 100; i++)
        {
            ArrayList<Record> row = new ArrayList<Record>();
            ArrayList<Record> r = new ArrayList<Record>();

            for (int j = 0; j < 100; j++)
            {
                int[] coords = {i,j};
                if(!water.contains(coords)){
                  String name = Integer.toString(i) + "," + Integer.toString(j);
                  Vertex newVertex = new Vertex(name,i,j);
                  djk.addVertex(newVertex);
                }
                row.add(null);
                r.add(null);
                visited.put(new Point(i,j),false);
            }
            this.records.add(row);
            this.map.add(r);
        }
        for (Vertex source : djk.getVertices()){
          // construct edge weights -- assume muddy
          setIncomingEdges(source, true);
        }

        // doesn't know package location or target location at beginning
        this.findPackage = false;
        this.findTarget = false;
        this.moveMode = 0;
        // moveMode = 0, did not find package or target
        // moveMode = 1/2, know package location, not start/start from package location
        // moveMode = 3/4, know target location, not start/start from target location

        // for (Vertex source : djk.getVertices()){

        //   //Construct edge weights as if each land cell is muddy
        //   int i = source.x;
        //   int j = source.y;
        //   int[] northwest = {i-1, j+1};
        //   if (i > 0 && j < 99){
        //     if(!water.contains(northwest)){
        //       String name = Integer.toString(i) + "," + Integer.toString(j);
        //       Vertex target = djk.getVertex(name);
        //       Vertex[] key1 = {source, target};
        //       Vertex[] key2 = {target, source};
        //       if(! (existingEdges.contains(key1) || existingEdges.contains(key2)) ){
        //         djk.addUndirectedEdge(source.name, target.name, 6);
        //         existingEdges.add(key1);
        //         existingEdges.add(key2);
        //       }
        //     }
        //   }
          // int[] west = {i-1, j};
          // if (i > 0){
          //   if(!water.contains(west)){
          //     String name = Integer.toString(i) + "," + Integer.toString(j);
          //     Vertex target = djk.getVertex(name);
          //     Vertex[] key1 = {source, target};
          //     Vertex[] key2 = {target, source};
          //     if(! (existingEdges.contains(key1) || existingEdges.contains(key2)) ){
          //       djk.addUndirectedEdge(source.name, target.name, 4);
          //       existingEdges.add(key1);
          //       existingEdges.add(key2);
          //     }
          //   }
          // }
          // int[] southwest = {i-1, j-1};
          // if (i > 0 && j > 0){
          //   if(!water.contains(southwest)){
          //     String name = Integer.toString(i) + "," + Integer.toString(j);
          //     Vertex target = djk.getVertex(name);
          //     Vertex[] key1 = {source, target};
          //     Vertex[] key2 = {target, source};
          //     if(! (existingEdges.contains(key1) || existingEdges.contains(key2)) ){
          //       djk.addUndirectedEdge(source.name, target.name, 6);
          //       existingEdges.add(key1);
          //       existingEdges.add(key2);
          //     }
          //   }
          // }
          // int[] north = {i, j+1};
          // if (j < 99){
          //   if(!water.contains(north)){
          //     String name = Integer.toString(i) + "," + Integer.toString(j);
          //     Vertex target = djk.getVertex(name);
          //     Vertex[] key1 = {source, target};
          //     Vertex[] key2 = {target, source};
          //     if(! (existingEdges.contains(key1) || existingEdges.contains(key2)) ){
          //       djk.addUndirectedEdge(source.name, target.name, 4);
          //       existingEdges.add(key1);
          //       existingEdges.add(key2);
          //     }
          //   }
          // }
          // int[] south = {i, j-1};
          // if (j > 0){
          //   if(!water.contains(south)){
          //     String name = Integer.toString(i) + "," + Integer.toString(j);
          //     Vertex target = djk.getVertex(name);
          //     Vertex[] key1 = {source, target};
          //     Vertex[] key2 = {target, source};
          //     if(! (existingEdges.contains(key1) || existingEdges.contains(key2)) ){
          //       djk.addUndirectedEdge(source.name, target.name, 4);
          //       existingEdges.add(key1);
          //       existingEdges.add(key2);
          //     }
          //   }
          // }
          // int[] northeast = {i+1, j+1};
          // if (i < 99 && j < 99){
          //   if(!water.contains(northeast)){
          //     String name = Integer.toString(i) + "," + Integer.toString(j);
          //     Vertex target = djk.getVertex(name);
          //     Vertex[] key1 = {source, target};
          //     Vertex[] key2 = {target, source};
          //     if(! (existingEdges.contains(key1) || existingEdges.contains(key2)) ){
          //       djk.addUndirectedEdge(source.name, target.name, 6);
          //       existingEdges.add(key1);
          //       existingEdges.add(key2);
          //     }
          //   }
          // }
          // int[] east = {i+1, j};
          // if (i < 99){
          //   if(!water.contains(east)){
          //     String name = Integer.toString(i) + "," + Integer.toString(j);
          //     Vertex target = djk.getVertex(name);
          //     Vertex[] key1 = {source, target};
          //     Vertex[] key2 = {target, source};
          //     if(! (existingEdges.contains(key1) || existingEdges.contains(key2)) ){
          //       djk.addUndirectedEdge(source.name, target.name, 4);
        //         existingEdges.add(key1);
        //         existingEdges.add(key2);
        //       }
        //     }
        //   }
        //   int[] southeast = {i+1, j-1};
        //   if (i < 99 && j < 99){
        //     if(!water.contains(southeast)){
        //       String name = Integer.toString(i) + "," + Integer.toString(j);
        //       Vertex target = djk.getVertex(name);
        //       Vertex[] key1 = {source, target};
        //       Vertex[] key2 = {target, source};
        //       if(! (existingEdges.contains(key1) || existingEdges.contains(key2)) ){
        //         djk.addUndirectedEdge(source.name, target.name, 6);
        //         existingEdges.add(key1);
        //         existingEdges.add(key2);
        //       }
        //     }
        //   }
        // }

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

          if(i>=0 && i<=99 && j>=0 && j<=99 && !water.contains(adjacent[k])) {
              String name = Integer.toString(i) + "," + Integer.toString(j);
              Vertex target = djk.getVertex(name);
              Vertex[] key = {target, source};
              double weight = (k%2==0) ? 3 : 2;
              if (isMuddy) {
                if (state==0) {weight *= 2;}
                if (state==1) {weight = Double.POSITIVE_INFINITY;}
              }
              djk.setEdge(target.name, source.name, weight);
              //existingEdges.add(key);
          }
      }
    }


    public void observe(Point loc, HashMap<Point, CellStatus> statuses)
    {
        this.loc = loc;

        for (Map.Entry<Point, CellStatus> entry : statuses.entrySet())
        {
            Point p = entry.getKey();
            CellStatus status = entry.getValue();
            Record record = records.get(p.x).get(p.y);
            if (record == null || record.getC() != status.getC() || record.getPT() != status.getPT())
            {
                ArrayList<Observation> observations = new ArrayList<Observation>();
                record = new Record(p, status.getC(), status.getPT(), observations);
                records.get(p.x).set(p.y, record);
            }
            if (status.getPT() == 1){
               this.findPackage = true;
               this.moveMode = 2; // now at the package location
               this.packageLocation = p;
            }
            if (status.getPT() == 2){
              findTarget = true;
              this.moveMode = 4; // now at the target location
              this.targetLocation = p;
            }
            map.get(p.x).set(p.y, new Record(p, status.getC(), status.getPT(), new ArrayList<Observation>()));
            record.getObservations().add(new Observation(this.id, Simulator.getElapsedT()));
            String name = Integer.toString(p.x) + "," + Integer.toString(p.y);
            Vertex v = djk.getVertex(name);
            v.explored = true;
            setIncomingEdges(v, record.getC()==1);
            
            if(record.getPT() != 0) {
                if(state==0) {
                    // switch to state 1
                    state = 1;
                    for (Vertex source : djk.getVertices()){
                        // set all muddy edges to infinite weight
                        Record r = records.get(source.x).get(source.y);
                        if(r!=null && r.getC()==1) {
                            setIncomingEdges(source, true);
                        }
                    }
                }

                if(record.getPT() == 1) {packageFound=true;}
                if(record.getPT() == 2) {targetFound=true;}

                if(packageFound && targetFound) {
                    state = 2;
                }
            }
          }
    }

    public List<Record> sendRecords(int id)
    {
        ArrayList<Record> toSend = new ArrayList<Record>();
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
        return toSend;
    }

    public void receiveRecords(int id, List<Record> records)
    {

    }

    public List<Point> proposePath()
    {
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

    public Point getMove()
    {  
      Point currentLoc = this.loc;
      Point nextLoc = currentLoc;
      visited.put(currentLoc, true);
      // int currentx = 0;
      // int currenty = 0;
      // int packageX = 0;
      // int packageY = 0;
      // int targetX = 0;
      // int targetY = 0;
      // String packageLoc = "";
      // String targetLoc = "";

      // targetLoc = "targetX,targetY";
      // System.out.println("targetLoc:"+targetLoc);

      // if ((findPackage == true)&&(findTarget == true)){
      //   packageX = packageLocation.x;
      //   packageY = packageLocation.y;
      //   targetX = targetLocation.x;
      //   targetY = targetLocation.y;
      //   packageLoc = "packageX,packageY";
      //   targetLoc = "targetX,targetY";
      //   // String targetLoc = {targetX,targetY};
        
      //   List<Edge> path = djk.getDijkstraPath(packageLoc, targetLoc);
      //   if(!path.isEmpty()){
      //     return move(packageLocation);
      //   }
      // }
      
      if (findPackage == true || findTarget == true){
        // know packageLocation from communication
        if ((findPackage == true) && (this.moveMode == 1)){
          // go to package location
          return move(packageLocation);
        }
        // know packageLocation from observation, start from observation
        else if ((findPackage == true) && (this.moveMode == 2)){
          // System.out.println("222222222");
          nextLoc = findNextAvoid(this.loc);
          visited.put(new Point(nextLoc.x+currentLoc.x,nextLoc.y+currentLoc.y), true);
          return nextLoc;
        }
        // know targetLocation from communication
        else if ((findPackage != true) && (this.moveMode == 3)){
            // go to target location
            return move(targetLocation);
        }
        // know targetLocation from observation
        else{
          nextLoc = findNextAvoid(this.loc);
          visited.put(new Point(nextLoc.x+currentLoc.x,nextLoc.y+currentLoc.y), true);
          return nextLoc;
        }
      }
      // if didn't know either target location nor package location
      nextLoc = findNext(currentLoc);
      visited.put(new Point(nextLoc.x+currentLoc.x,nextLoc.y+currentLoc.y), true);
      // System.out.println("nextLoc"+nextLoc);
      return nextLoc;
    }

    public Point move(Point target)
    {
      int dx = 0;
      int dy = 0;
      Point start = this.loc;
      if (target.x > start.x){
          dx = 1;
      }
      else if (target.x == start.x){
          dx = 0;
      }
      else{
          dx = -1;
      }
      if (target.y > start.y){
          dy = 1;
      }
      else if (target.y == start.y){
          dy = 0;
      }
      else{
          dy = -1;
      }
      return new Point (dx, dy);
    }

    public Point findNext(Point start)
    {
      Record recordNext;
      Point toReturn = start;
      Map<Point, Integer> possible_move = new HashMap<Point, Integer>();
      for(int i = 0;i<3;i++)
      {
        int dx = this.loc.x+i-1;
        if (dx > 99 || dx < 0){
          continue;
        }
        for(int j = 0;j<3;j++){
          int dy = this.loc.y+j-1;
          if (i==1 && j==1){
              continue;
          }
          if(dy>99||dy<0){
            continue;
          }
          if(this.water.contains(new Point(dx,dy))){
            continue;
          }

          else{
            if (visited.get(new Point(dx, dy))){
              if (Math.abs(i)+Math.abs(j)>1){
                if (map.get(dx).get(dy).getC() == 1){
                    possible_move.put(new Point(i-1,j-1),0);
                }
                else{
                    possible_move.put(new Point(i-1,j-1),0);
                }
            }
              else{
                if (map.get(dx).get(dy).getC() == 1){
                    possible_move.put(new Point(i-1,j-1),0);
                }
                else{
                    possible_move.put(new Point(i-1,j-1),0);
                }
              }
            }
            else{
              if (Math.abs(i)+Math.abs(j)>1){
                if (map.get(dx).get(dy).getC() == 1){
                    possible_move.put(new Point(i-1,j-1),1);
                }
                else{
                    possible_move.put(new Point(i-1,j-1),4);
                }
            }
            else{
                if (map.get(dx).get(dy).getC() == 1){
                    possible_move.put(new Point(i-1,j-1),1);
                }
                else{
                    possible_move.put(new Point(i-1,j-1),5);
                }
              }
            }
            
            }
          }
      }
      double max_reward = 0;
      for (Point p: possible_move.keySet()){
        Point next = new Point(start.x+p.x,start.y+p.y);
        if (possible_move.get(p)>max_reward){
            max_reward = possible_move.get(p);
            toReturn = p;
        }
        if (max_reward == 0){
            toReturn = find_unknown(start);
        }
      }
      return toReturn;
    }

    public Point findNextAvoid(Point start)
    {
      Record recordNext;
      Point toReturn = start;
      Map<Point, Integer> possible_move = new HashMap<Point, Integer>();
      for(int i = 0;i<3;i++)
      {
        for(int j = 0;j<3;j++){
          int dx = this.loc.x+i-1;
          int dy = this.loc.y+j-1;
          if (i==1 && j==1){
              continue;
          }
          if(dx>100||dx<0||dy>100||dy<0){
            continue;
          }
          // next step is muddy cell
          // Point(i-1,j-1) is next step
          if(this.water.contains(new Point(dx,dy))){
            continue;
          }
          else{
              if (visited.get(new Point(dx,dy)) == true){
                if (Math.abs(i)+Math.abs(j)>1){
                  // System.out.println("trueeeeeeeeee");
                  if (map.get(dx).get(dy).getC() == 1){
                    possible_move.put(new Point(i-1,j-1),-2);
                  }
                  else{
                    possible_move.put(new Point(i-1,j-1),0);
                    }
                }

                  else{
                    if (Math.abs(i)+Math.abs(j)>1){
                      if (map.get(dx).get(dy).getC() == 1){
                        possible_move.put(new Point(i-1,j-1),-1);
                      }
                      else {
                        possible_move.put(new Point(i-1,j-1),0);
                      }
                    }
                    else{
                      if (map.get(dx).get(dy).getC() == 1){
                        possible_move.put(new Point(i-1,j-1),-1);
                      }
                      else{
                        possible_move.put(new Point(i-1,j-1),3);
                      }
                    }
                }
            }
            else{
                if (map.get(dx).get(dy).getC() == 1){
                    possible_move.put(new Point(i-1,j-1),-1);
                }
                else{
                    possible_move.put(new Point(i-1,j-1),5);
                }
              }
            }
          }
      }
      double max_reward = 0;
      for (Point p: possible_move.keySet()){
        Point next = new Point(start.x+p.x,start.y+p.y);
        if (possible_move.get(p)>max_reward){
            max_reward = possible_move.get(p);
            toReturn = p;
        }
        if (max_reward == 0){
            toReturn = find_unknown(start);
        }
      }
      return toReturn;
    }
  
  public Point find_unknown(Point loc){
      int minimum = 200;
      int tx = 0;
      int ty = 0;
      for (int i=0; i<map.size();++i){
          for (int j=0; j<map.get(i).size();++j){
              if (map.get(i).get(j)==null){
                  if (Math.abs(i-loc.x)+Math.abs(j-loc.y)< minimum){
                      minimum = Math.abs(i-loc.x)+Math.abs(j-loc.y);
                      tx = i;
                      ty = j;
                  }
              }
          }
      }
      return move(new Point(tx, ty));
  }

}
