/** Project Main
 *
 * TODO
 * 1) fix the "jump" on resume from a pause
 * 2) fix the occasional freeze right after button-press to get out of splash screen
 * 3) add features; some ideas:
 *    i) static home-screen
 *   ii) set difficulty
 *  iii) board layout presets (or random board)
 *   iv) score-tracker
 *    v) modify/choose screen size
 *   vi) choose snake color (or choose to randomly color)
 * 4) orientation lookup (and/or color lookup if applicable); see notes at Snake.java l.91 and Board.Java l.74
 */

package SnakeGameProject;

import javax.swing.JFrame;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class SnakeGame extends JFrame {
    SplashScreen s;
    GameEngine g;
    Board board;
    Snake snake;

    Direction currentDirection;

    Random fruitLocationGenerator;
    int fruitLocation;

    Point prevTail;

    boolean paused;
    boolean inGame;

    boolean directionChanged;
    boolean directionChangeFactor;
    boolean snakeMoved; // ensures direction switches aren't "too quick"

    int currentBoardSize = 40;
    int currentElementSize = 10;
    int currentGameSpeed = 9;   // measure of "difficulty"

    public SnakeGame() {
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        s = new SplashScreen(currentBoardSize, currentElementSize);
        add(s);
        pack();
        setVisible(true);
        setLocation((Toolkit.getDefaultToolkit().getScreenSize().width)  / 2  - getWidth()  / 2,
                    (Toolkit.getDefaultToolkit().getScreenSize().height) / 2  - getHeight() / 2);

        waitForSplashScreenExitSignal();

        setupGameKeyListener();
        initializeGame();
    }

    public void waitForSplashScreenExitSignal() {
        final CountDownLatch latch = new CountDownLatch(1);
        KeyEventDispatcher dispatcher = new KeyEventDispatcher() {
            // Anonymous class invoked from EDT
            public boolean dispatchKeyEvent(KeyEvent e) {
                latch.countDown();
                return false;
            }
        };
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(dispatcher);
        try {
            latch.await();  // current thread waits until countDown() called
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        s.runIntroLoop = false;
        KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(dispatcher);
    }

    public void setupGameKeyListener() {
        addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                int keycode = e.getKeyCode();
                Direction curr = getDirection();

                if (Math.abs((double) keycode - 38.5) <= 3.0 && snakeMoved) {
                    switch (keycode) {
                        case KeyEvent.VK_UP:
                            if (getDirection().ordinal() > 1)
                                setDirection(Direction.North);
                            break;
                        case KeyEvent.VK_DOWN:
                            if (getDirection().ordinal() > 1)
                                setDirection(Direction.South);
                            break;
                        case KeyEvent.VK_LEFT:
                            if (getDirection().ordinal() < 2)
                                setDirection(Direction.West);
                            break;
                        case KeyEvent.VK_RIGHT:
                            if (getDirection().ordinal() < 2)
                                setDirection(Direction.East);
                    }
                    snakeMoved = false;
                    directionChanged = true;
                    directionChangeFactor = Math.abs(curr.ordinal() - getDirection().ordinal()) == 2 ? true : false;
                } else switch (keycode) {
                    case KeyEvent.VK_ENTER:
                        break;
                    case KeyEvent.VK_P:
                        paused = true;
                }
            }
        });
    }

    public void initializeGame() {
        board = new Board(currentBoardSize, currentElementSize, this);
        add(board);
        pack();

        paused = false;

        currentDirection = Direction.East;

        prevTail = new Point();

        inGame = true;

        snake = new Snake( currentBoardSize / 4 );
        placeSnakeOnBoard();

        fruitLocationGenerator = new Random();
        spawnFruit(null);

        setVisible(true);

        g = new GameEngine(currentGameSpeed, this);
        g.runGameLoop();
    }

    public void gameOver() {
        inGame = false;
    }

    private Point getCoordinates(int i) {
        return new Point((int)Math.floor( i / currentBoardSize ), i % currentBoardSize);
    }

    public Direction getDirection() {
        return currentDirection;
    }

    public void setDirection(Direction d) {
        this.currentDirection = d;
    }

    public void clearTail() {
        board.setElementAtPosition(snake.getTail(), null);
    }

    public void placeSnakeOnBoard() {
        int i = 0;
        for(Point p : snake.getSnakeArrList()) {
            try {
                board.setElementAtPosition(p, i == 0 ? GameElement.Head : GameElement.Body);
            } catch (ArrayIndexOutOfBoundsException a) {
                gameOver();
            }
            i++;
        }
    }

    public void update() {
        // determine position update factor

        Direction curr = getDirection();

        if (directionChanged) {
            snake.updateOrientation( curr.ordinal() < 2 ? Orientation.Vertical : Orientation.Horizontal,
                                     directionChangeFactor );
            directionChanged = false;
        }
        else snake.fullOrient();

        // after getting info from current direction, alert KeyListener that it's safe to update current direction
        snakeMoved = true;

        int moveX = 0, moveY = 0;

        switch (curr) {
            case East:
                moveX++;
                break;
            case West:
                moveX--;
                break;
            case North:
                moveY--;
                break;
            case South:
                moveY++;
        }

        int oldX = snake.getHead().x,
            oldY = snake.getHead().y,
            newX = oldX + moveX,
            newY = oldY + moveY;

        // check for a collision before moving
        boolean ateFruit = checkCollision( new Point(newX, newY) );

        // update snake and board

        clearTail();

        snake.moveBodyAfterHead(snake.getHead().x + moveX, snake.getHead().y + moveY);

        if(ateFruit)
            snake.addLink(prevTail);

        placeSnakeOnBoard();
    }

    public boolean checkCollision(Point newHeadPos) {
        if (getCoordinates(fruitLocation).equals(newHeadPos)) {
            spawnFruit(newHeadPos);
            prevTail = new Point(snake.getTail());
            return true;
        }
        else {
            ArrayList<Point> snakeArrList = snake.getSnakeArrList();

            for (int i = 3; i < snake.length() - 1; i++)
                if (newHeadPos.equals(snakeArrList.get(i)))
                    gameOver();
        }
        return false;
    }

    public void spawnFruit(Point newHead) {
        fruitLocation = fruitLocationGenerator.nextInt( currentBoardSize * currentBoardSize - snake.length() );

        Point p;

        for ( ; true; fruitLocation++ ) {
            p = getCoordinates(fruitLocation);
            if ( board.getElementAtPosition(p) == null && p != newHead ) {
                board.setElementAtPosition(p, GameElement.Fruit);
                break;
            }
        }
    }

    /** Project Main */
    public static void main(String[] args) {
                // try {
                //    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                // } catch (ClassNotFoundException e) {
                //    e.printStackTrace();
                // }
                new SnakeGame();
    }
}
