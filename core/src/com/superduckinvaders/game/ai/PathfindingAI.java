
package com.superduckinvaders.game.ai;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.superduckinvaders.game.DuckGame;
import com.superduckinvaders.game.Round;
import com.superduckinvaders.game.assets.TextureSet;
import com.superduckinvaders.game.entity.mob.InfectedMob;
import com.superduckinvaders.game.entity.mob.Mob;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

/**
 * AI that follows and attacks the player within a certain range.
 */
public class PathfindingAI extends AI {

    /**
     * How many iterations to use in the pathfinding algorithm.
     */
    public final static int PATHFINDING_ITERATION_LIMIT = 20;

    /**
     * How often to update the AI.
     */
    public final static float PATHFINDING_RATE = (float) 0.4f;

    /**
     * The random offset to be added or taken from the base pathfinding rate.
     */
    public final static float PATHFINDING_RATE_OFFSET = (float) 0.05;

    /**
     * Width of one tile in the map.
     */
    private int tileWidth;

    /**
     * Height of one tile in the map.
     */
    private int tileHeight;

    /**
     * Player's last position.
     */
    private Vector2 playerPos;

    /**
     * Used to calculate rate of pathfinding.
     */
    private float deltaOffsetLimit = 0;

    /**
     * Used to track when to recalculate AI.
     */
    private float currentOffset = 0;

    /**
     * How far away from the player this PathfindingAI can attack.
     */
    private int targetRange;

    /**
     * The Coordinate for the AI to find a path for.
     */
    public Coordinate target;
    //////////////////////////NEW FOR ASSESSMENT 4 /////////////////////////
    /**
     * The Coordinate for the AI to find a path for.
     */
    
    public int[][] possibleDir = {{3,0,1},{0,1,2},{1,2,3},{2,3,0}};
    
    /**
     * Possible allowed directions
     */
    
    public int[] allowedDir = {0,1,2};
	//////////////////////////////////////////////////////
    /**
     * Initialises this PathfindingAI.
     *
     * @param round       the round the Mob this AI controls is a part of
     * @param targetRange how close to the player this PathfindingAI tries to get
     */
    public PathfindingAI(Round round, int targetRange) {
        super(round);

        this.tileWidth = round.getTileWidth();
        this.tileHeight = round.getTileHeight();
        this.targetRange = targetRange;
    }

    /**
     * Updates this PathfindingAI.
     *
     * @param mob   the Mob using this AI
     * @param delta time since the previous update
     */
    @Override
    public void update(Mob mob, float delta) {
        playerPos = round.getPlayer().getCentre();
        TextureSet.FaceDirection lastDir = round.getPlayer().getFacing();
        
        float distanceToPlayer = mob.distanceTo(playerPos);
        float distanceToTargetTile = (target != null) ? mob.getCentre().sub(target.vector()).len() : 0f;
        
        currentOffset += delta;
        if ((currentOffset >= deltaOffsetLimit || distanceToTargetTile < 2) && (int) distanceToPlayer < 1280 / 4) {
            deltaOffsetLimit = PATHFINDING_RATE + (MathUtils.random() % PATHFINDING_RATE_OFFSET);
            currentOffset = 0;
            //////////////////////////////////////////////////////////
            switch(lastDir){
            	case BACK : allowedDir=possibleDir[0];
            	break;
            	case RIGHT : allowedDir=possibleDir[1];
            	break;
            	case FRONT : allowedDir=possibleDir[2];
            	break;
            	case LEFT : allowedDir=possibleDir[3];
            	break;
            }

            if (round.getPlayer().dementionTimer==0) {
            	target = FindPath(mob);
            } else {
            	if (mob instanceof InfectedMob) {
            		target = FindRandPath(mob);
            	} else{
            		target = FindPath(mob);
            	}
            }
            //////////////////////////////////////////////////////////
        }
        
        // targetPoint = (target != null) ? target.vector() : new Vector2(playerPos).setLength(1f);
        if (target != null) {
            mob.applyVelocity(target.vector());
        }
    }
//////////////////////////////////////////////////////////
    private Coordinate FindRandPath(Mob mob) {
    	Coordinate currentCoord = roundToTile(mob.getCentre());
    	Coordinate[] perm = {
                new Coordinate(currentCoord.x,             currentCoord.y + tileHeight),
                new Coordinate(currentCoord.x + tileWidth, currentCoord.y             ),
                new Coordinate(currentCoord.x,             currentCoord.y - tileHeight),
                new Coordinate(currentCoord.x - tileWidth, currentCoord.y             )
            };
    	float randRes = MathUtils.random();
    	if (randRes<0.2f) return perm[allowedDir[0]];
    	if (randRes<0.8f) return perm[allowedDir[1]];
    	else return perm[allowedDir[2]];
	}
//////////////////////////////////////////////////////////
	/**
     * A variation of A* algorithm. Returns a meaningful target coordinate as a pair of integers.
     * Recalculated every tick as player might move and change pathfinding coordinates.
     *
     * @param mob Mob that a path is being generated for
     * @return Returns a Coordinate for the path finding
     */
    private Coordinate FindPath(Mob mob) {
        Vector2 mobPos = mob.getCentre();
        Vector2 mobSize = mob.getSize();
        Coordinate startCoord = roundToTile(mobPos);
        Coordinate finalCoord = roundToTile(playerPos);
        boolean finalFound = false;
        
        if (round.pathIsClear(mobPos, mobSize, playerPos)){
            if (new Vector2(playerPos).sub(mobPos).len() < targetRange){
                return null;
            }
            else {
                currentOffset = deltaOffsetLimit;
                return new Coordinate(playerPos);
            }
        }

        PriorityQueue<Coordinate> fringe = new PriorityQueue<Coordinate>();
        HashMap<Coordinate, SearchNode> visitedStates = new HashMap<Coordinate, SearchNode>();
        fringe.add(startCoord);
        visitedStates.put(startCoord, new SearchNode(null, startCoord, 0));

        while (!fringe.isEmpty()) {

            Coordinate currentCoord = fringe.poll();
            SearchNode currentState = visitedStates.get(currentCoord);

            if (currentState.iteration >= PATHFINDING_ITERATION_LIMIT) {
                continue;
            }
            
            if (currentCoord.inSameTile(finalCoord)) {
                finalCoord = currentCoord;
                finalFound = true;
                break;
            }

            //work out N, E, S, W permutations
            Coordinate[] perm = {
                new Coordinate(currentCoord.x,             currentCoord.y + tileHeight),
                new Coordinate(currentCoord.x + tileWidth, currentCoord.y             ),
                new Coordinate(currentCoord.x,             currentCoord.y - tileHeight),
                new Coordinate(currentCoord.x - tileWidth, currentCoord.y             )
            };

            for (Coordinate currentPerm : perm) {
                if (!visitedStates.containsKey(currentPerm) && !round.collidePoint(currentPerm.vector())) {
                    fringe.add(currentPerm);
                    visitedStates.put(currentPerm, new SearchNode(currentState, currentPerm, currentState.iteration + 1));
                }
            }
        }
        if (!finalFound) {
            return null;
        } else {
            SearchNode resultNode;
            List<SearchNode> path = new ArrayList<SearchNode>();
            path.add(visitedStates.get(finalCoord));
            while (path.get(path.size() - 1) != visitedStates.get(startCoord)) {
                SearchNode pred = path.get(path.size() - 1).predecessor;
                if (pred == null){
                    break;
                }
                path.add(pred);
            }
            int index = path.size()-1;
            while (index > 0) {
                SearchNode tempNode = path.get(index-1);
                if(!round.pathIsClear(mobPos, mobSize, tempNode.coord.vector())){
                    break;
                }
                index--;
            }
            resultNode = path.get(index);

            return resultNode.coord;
        }
    }

    /**
     * Round an exact position to the nearest tile coordinate.
     * @param pos the position vector
     * @return the Coordinate of the tile
     */
    public Coordinate roundToTile(Vector2 pos){
        return roundToTile(pos.x, pos.y);
        
    }

    /**
     * Round an exact position to the nearest tile coordinate.
     * @param x the x position
     * @param y the y position
     * @return the Coordinate of the tile
     */
    public Coordinate roundToTile(float x, float y){
        int nx = (int)(((int)(x/tileWidth)+0.5f) * tileWidth);
        int ny = (int)(((int)(y/tileHeight)+0.5f) * tileHeight);
        return new Coordinate(nx, ny);
    }

    /**
     * Represents a pair of coordinates.
     */
    public class Coordinate implements Comparable<Coordinate> {
        /**
         * The X coordinate.
         */
        public int x;
        /**
         * The Y coordinate.
         */
        public int y;

        /**
         * Initialises this Coordinate.
         *
         * @param point The point vector.
         */
        public Coordinate(Vector2 point) {
            this((int) point.x, (int) point.y);
            
        }

        /**
         * Initialises this Coordinate.
         *
         * @param x the x position.
         * @param y the y position.
         */
        public Coordinate(int x, int y) {
            this.x = x;
            this.y = y;
        }

        /**
         * Compares this Coordinate to another Coordinate.
         *
         * @param o the coordinate to compare to
         * @return the result of the comparison
         */
        @Override
        public int compareTo(Coordinate o) {
            float playerDistanceA = this.vector().sub(playerPos).len();
            float playerDistanceB =    o.vector().sub(playerPos).len();
            return new Float(playerDistanceA).compareTo(playerDistanceB);
        }

        /**
         * Tests this Coordinate with another object for equality.
         *
         * @param o the object to compare to
         * @return true of the objects are equal, false if not
         */
        @Override
        public boolean equals(Object o) {
            if (o == null) return false;
            if (getClass() != o.getClass()) return false;
            final Coordinate other = (Coordinate) o;
            return (this.x == other.x && this.y == other.y);
        }

        /**
         * Gets a unique hash code for this coordinate.
         *
         * @return the hash code
         */
        @Override
        public int hashCode() {
            int hash = 17;
            hash = hash * 31 + this.x;
            hash = hash * 31 + this.y;
            return hash;
        }

        /**
         * Gets whether this Coordinate is in the same map tile as another.
         *
         * @param b the coordinate to compare with
         * @return true if the coordinates are in the same map tile, false if not
         */
        public boolean inSameTile(Coordinate b) {
            return (this.x / tileWidth == b.x / tileWidth && this.y / tileHeight == b.y / tileHeight);
        }

        /**
         * Returns a string representation of this Coordinate.
         *
         * @return a string representation of this Coordinate
         */
        public String toString() {
            return ("(" + Integer.toString(this.x) + ", " + Integer.toString(this.y) + ")");
        }
        
        public Vector2 vector() {
            return new Vector2(x, y);
        }
    }

    /**
     * Represents a node in the A* search tree.
     */
    class SearchNode {
        /**
         * The predecessor node in the search tree.
         */
        public SearchNode predecessor;
        public Coordinate coord;
        /**
         * The iteration this node is a part of.
         */
        public int iteration;

        /**
         * Initialises this SearchNode.
         *
         * @param predecessor the predecessor node
         * @param iteration   the iteration of this node
         */
        public SearchNode(SearchNode predecessor, Coordinate coord, int iteration) {
            this.predecessor = predecessor;
            this.coord = coord;
            this.iteration = iteration;
        }
    }
}
