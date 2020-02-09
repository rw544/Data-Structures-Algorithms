package app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import game.HuntState;
import game.Hunter;
import game.Node;
import game.NodeStatus;
import game.ScramState;

/** A solution with huntOrb optimized and scram getting out as fast as possible. */
public class Pollack extends Hunter {

    /** Get to the orb in as few steps as possible. <br>
     * Once you get there, you must return from the function in order to pick it up. <br>
     * If you continue to move after finding the orb rather than returning, it will not count.<br>
     * If you return from this function while not standing on top of the orb, it will count as a
     * failure.
     *
     * There is no limit to how many steps you can take, but you will receive<br>
     * a score bonus multiplier for finding the orb in fewer steps.
     *
     * At every step, you know only your current tile's ID and the ID of all<br>
     * open neighbor tiles, as well as the distance to the orb at each of <br>
     * these tiles (ignoring walls and obstacles).
     *
     * In order to get information about the current state, use functions<br>
     * currentLocation(), neighbors(), and distanceToOrb() in HuntState.<br>
     * You know you are standing on the orb when distanceToOrb() is 0.
     *
     * Use function moveTo(long id) in HuntState to move to a neighboring<br>
     * tile by its ID. Doing this will change state to reflect your new position.
     *
     * A suggested first implementation that will always find the orb, but <br>
     * likely won't receive a large bonus multiplier, is a depth-first search. <br>
     * Some modification is necessary to make the search better, in general. */
    @Override
    public void huntOrb(HuntState state) {
        // TODO 1: Get the orb
        if (state == null) { return; }
        dfsWalk(state);
    }

    // Stores an ArrayList of visited nodes.
    ArrayList<Long> arrVisited= new ArrayList<>();

    /** Get to the orb in as few steps as possible using DFS. */
    public boolean dfsWalk(HuntState state) {
        // Stores the ID corresponding to the current location Pollack is at.
        Long currID= state.currentLocation();
        if (arrVisited.contains(currID)) { return false; }
        arrVisited.add(state.currentLocation());
        if (state.distanceToOrb() == 0) { return true; }
        // Stores a List of type NodeStatus of the neighbors of the current state.
        List<NodeStatus> lsList= new ArrayList<>(state.neighbors());
        Collections.sort(lsList);
        for (NodeStatus ns : lsList) {
            if (!arrVisited.contains(ns.getId())) {
                state.moveTo(ns.getId());
                if (dfsWalk(state)) {
                    return true;
                } else {
                    state.moveTo(currID);
                }
            }
        }
        return false;
    }

    /** Get out the cavern before the ceiling collapses, trying to collect as <br>
     * much gold as possible along the way. Your solution must ALWAYS get out <br>
     * before time runs out, and this should be prioritized above collecting gold.
     *
     * You now have access to the entire underlying graph, which can be accessed <br>
     * through ScramState. <br>
     * currentNode() and getExit() will return Node objects of interest, and <br>
     * getNodes() will return a collection of all nodes on the graph.
     *
     * Note that the cavern will collapse in the number of steps given by <br>
     * getStepsRemaining(), and for each step this number is decremented by the <br>
     * weight of the edge taken. <br>
     * Use getStepsRemaining() to get the time still remaining, <br>
     * pickUpGold() to pick up any gold on your current tile <br>
     * (this will fail if no such gold exists), and <br>
     * moveTo() to move to a destination node adjacent to your current node.
     *
     * You must return from this function while standing at the exit. <br>
     * Failing to do so before time runs out or returning from the wrong <br>
     * location will be considered a failed run.
     *
     * You will always have enough time to scram using the shortest path from the <br>
     * starting position to the exit, although this will not collect much gold. <br>
     * For this reason, using Dijkstra's to plot the shortest path to the exit <br>
     * is a good starting solution */
    @Override
    public void scram(ScramState state) {
        // TODO 2: Get out of the cavern before it collapses, picking up gold along the way.
        // The node-carrying gold nearest to the current state.
        Node nearestGoldNode= getNearestGoldNode(state);
        while (nearestGoldNode != state.currentNode()) {
            moveToTarget(state, state.currentNode(), nearestGoldNode);
            nearestGoldNode= getNearestGoldNode(state);
        }
        moveToTarget(state, state.currentNode(), state.getExit());
    }

    /** Return the nearest node containing gold based on <br>
     * the maximum ratio of collecting more gold by applying <br>
     * the gold in a tile over the distance factor. */
    public Node getNearestGoldNode(ScramState state) {
        ArrayList<Node> allNodes= new ArrayList<>(state.allNodes());
        // The node-carrying gold nearest to the current state.
        Node nearestGoldNode= state.currentNode();
        // Stores the max ratio of the gold in a tile to the distance from the
        // current state to the tile containing gold.
        int maxBenefitRatio= 0;
        for (Node n : allNodes) {
            int tileGold= n.getTile().gold(); // Stores the amount of gold in the tile.
            if (tileGold > 0) {
                // Sum of the weights of the edges from the current node to the
                // node containing gold.
                int pathSumToGold= Path.pathSum(Path.shortest(state.currentNode(), n));
                // Sum of the weights of the edges from the node containing gold
                // to the exit node.
                int pathSumToExit= Path.pathSum(Path.shortest(n, state.getExit()));
                // Stores the total distance from the current node to the node
                // containing gold to the exit.
                int totalDist= pathSumToGold + pathSumToExit;
                if (totalDist <= state.stepsLeft()) {
                    // Stores the ratio of the gold in a tile to the distance from
                    // the current state to the tile containing gold.
                    int benefitRatio= tileGold / pathSumToGold;
                    if (benefitRatio >= maxBenefitRatio) {
                        maxBenefitRatio= benefitRatio;
                        nearestGoldNode= n;
                    }
                }
            }
        }
        return nearestGoldNode;

    }

    /** Move the hunter from the start node to the end node in the grid. */
    public void moveToTarget(ScramState state, Node startNode, Node endNode) {
        // Stores a List containing all nodes along the shortest path from the
        // start node to the end node.
        List<Node> lList= Path.shortest(startNode, endNode);
        Iterator<Node> it= lList.iterator();
        it.next();
        while (it.hasNext()) {
            state.moveTo(it.next());
        }
    }

}
