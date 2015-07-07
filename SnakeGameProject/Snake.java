package SnakeGameProject;

import java.awt.Point;
import java.util.ArrayList;

public class Snake {

    /** Members */

    private static final int INIT_LENGTH = 5;
    private ArrayList<Point> snakeArrList;
    private ArrayList<Orientation> linkOrientation;


    /** Constructors */

    public Snake(int initialLocationFactor) {
        linkOrientation = new ArrayList<>( INIT_LENGTH );
        snakeArrList = new ArrayList<>( INIT_LENGTH );

        for (int i = INIT_LENGTH - 1; i >= 0; i--) {
            snakeArrList.add(new Point(initialLocationFactor + i, initialLocationFactor));
            addOrientation(Orientation.Horizontal);
        }
    }

    /** Adders */

    public void addLink(Point p) {
        linkOrientation.add( getOrientation(length() - 1) );
        snakeArrList.add(p);
    }

    public void addOrientation( Orientation o ) {
        linkOrientation.add(o);
    }

    /** Setters */

    private void setXY(int pos, int x, int y) {
        snakeArrList.get(pos).setLocation(x, y);
    }

    private void setOrientation(int pos, Orientation o) {
        linkOrientation.set(pos, o);
    }

    public void fullOrient() {
        for(int i = length() - 1; i > 0; i--)
            setOrientation(i, getOrientation(i - 1));
    }

    public void updateOrientation(Orientation newHeadOrient, boolean directionFactor) {
        for(int i = length() - 1; i > 1; i--)
            setOrientation(i, getOrientation(i - 1));

        setOrientation(0, newHeadOrient);
        setOrientation(1, directionFactor ? Orientation.Corner_N$E_S$W : Orientation.Corner_N$W_S$E);  //obviously change to match case
    }

    /** Getters */

    private Orientation getOrientation(int pos) {
        return linkOrientation.get(pos);
    }

    public int length() {
        return snakeArrList.size();
    }

    public Point getHead() {
        return snakeArrList.get(0);
    }

    public Point getTail() {
        return snakeArrList.get( length() - 1 );
    }

    private int getX(int pos) {
        return snakeArrList.get(pos).x;
    }

    private int getY(int pos) {
        return snakeArrList.get(pos).y;
    }

    public ArrayList<Point> getSnakeArrList() {
        return snakeArrList;
    }

    // could have hash table H with all directions at positions; every addLink updates H saves on time, but could
    // have bad space requirements...
    public Orientation getOrientationAt(int x, int y) {
        Point p = new Point(x, y);

        for (int i = 0; i < length(); i++)
            if (snakeArrList.get(i).equals(p))
                return linkOrientation.get(i);

        return null; // can't happen
    }

    /** Movers */

    public void moveBodyAfterHead(int headX, int headY) {
        for( int i = length() - 1; i > 0; i--)
            setXY(i, getX(i - 1), getY(i - 1));
        setXY(0, headX, headY);
    }
}