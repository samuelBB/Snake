/**
 * Splash Screen
 *
 * TODO
 * -Right now the splash screen is a miniature version of the entire snake-game; there should be a way to re-use and
 *  make it a limiting case! ...
 */

package SnakeGameProject;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import java.awt.*;
import java.util.Random;

public class SplashScreen extends JPanel {
    GameElement [][] board;
    GameElement ge;
    ImageIcon snakeIcon;
    Image snakeImage;
    Snake snake;
    Graphics2D g2d;

    int boardSize, elemSize;
    boolean runIntroLoop = true;

    Random directionGen = new Random();
    int moveCount = 0;
    Direction[] directionArray = Direction.values();
    Direction currentDirection = Direction.East, newDirection;
    boolean directionChanged, directionChangeFactor;

    boolean isVertical;
    Polygon[] psHead, psBody;
    double centerX, centerY;

    // private final Color[] colors = {
    //        Color.BLUE, Color.BLACK, Color.GRAY, Color.GREEN, Color.RED, Color.PINK, Color.MAGENTA,
    //        Color.YELLOW, Color.ORANGE, Color.DARK_GRAY, Color.WHITE, Color.LIGHT_GRAY
    // };
    // private Random colorGen = new Random();

    public SplashScreen(int bSize, int eSize) {
        boardSize = bSize;
        elemSize = eSize;
        setPreferredSize( new Dimension(boardSize * elemSize, boardSize * elemSize) );
        setBackground(Color.RED);
        loadSnakeImage();

        board = new GameElement[boardSize * elemSize][boardSize * elemSize];

        snake = new Snake( boardSize / 4 );
        placeSnakeOnBoard();

        runSplashScreenSnakeDemo();
    }

    public void runSplashScreenSnakeDemo() {
        Thread threadLoop = new Thread() {
            public void run() {
                SplashScreenDemoGameLoop();
            }
        };
        threadLoop.start();
    }

    private void SplashScreenDemoGameLoop() {
        double hertz = 9;
        final double TIME_BETWEEN_UPDATES = 1000000000 / hertz;
        final int MAX_UPDATES_BEFORE_RENDER = 5;
        double lastUpdateTime = System.nanoTime();
        double lastRenderTime;
        final double TARGET_FPS = 60;
        final double TARGET_TIME_BETWEEN_RENDERS = 1000000000 / TARGET_FPS;

        while (runIntroLoop) {
            double now = System.nanoTime();
            int updateCount = 0;

            while( now - lastUpdateTime > TIME_BETWEEN_UPDATES && updateCount < MAX_UPDATES_BEFORE_RENDER ) {
                update();
                lastUpdateTime += TIME_BETWEEN_UPDATES;
                updateCount++;
            }

            if ( now - lastUpdateTime > TIME_BETWEEN_UPDATES) {
                lastUpdateTime = now - TIME_BETWEEN_UPDATES;
            }

            repaint();
            lastRenderTime = now;

            while ( now - lastRenderTime < TARGET_TIME_BETWEEN_RENDERS && now - lastUpdateTime < TIME_BETWEEN_UPDATES) {
                Thread.yield();
                try {Thread.sleep(1);} catch(Exception e) {}
                now = System.nanoTime();
            }
        }
    }

    private void loadSnakeImage() {
        snakeIcon = new ImageIcon("snk.png");
        snakeImage = snakeIcon.getImage();
    }

    public void setElementAtPosition(Point p, GameElement ge) {
        board[p.y][p.x] = ge;
    }

    public void placeSnakeOnBoard() {
        int i = 0;
        for(Point p : snake.getSnakeArrList()) {
                setElementAtPosition(p, i == 0 ? GameElement.Head : GameElement.Body);
                i++;
        }
    }

    // make sure direction changes don't cause the snake to go out of bounds
    public boolean willHitWall(Direction newDirec) {
        int headX = snake.getHead().x,
            headY = snake.getHead().y;

        switch (newDirec) {
            case East:
                if (headX + 5 < boardSize)
                    return false;
                break;
            case West:
                if (headX - 5 >= 0)
                    return false;
                break;
            case North:
                if (headY - 5 >= 0)
                    return false;
                break;
            case South:
                if (headY + 5 < boardSize)
                    return false;
        }
        return true;
    }

    public void update() {
        moveCount++;

        if(moveCount == 5) {
            moveCount = 0;

            for(newDirection = directionArray[directionGen.nextInt(4)]; true;
                newDirection = directionArray[directionGen.nextInt(4)]) {
                if( newDirection == Direction.East && currentDirection == Direction.West)
                    moveCount = 0;
                if ((currentDirection.ordinal() <  2 && newDirection.ordinal()  < 2 && currentDirection != newDirection)
                 || (currentDirection.ordinal() >= 2 && newDirection.ordinal() >= 2 && currentDirection != newDirection)
                 ||  willHitWall(newDirection))
                    continue;
                else break;
            }
            if (currentDirection != newDirection) {
                currentDirection = newDirection;
                directionChangeFactor = Math.abs(newDirection.ordinal() - currentDirection.ordinal()) == 2 ?
                                        true : false;
                directionChanged = true;
            }
        }

        if (directionChanged) {
            snake.updateOrientation( currentDirection.ordinal() < 2 ?
                                     Orientation.Vertical : Orientation.Horizontal, directionChangeFactor );
            directionChanged = false;
        }
        else snake.fullOrient();

        int moveX = 0, moveY = 0;

        switch ( currentDirection ) {
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

        setElementAtPosition(snake.getTail(), null);
        snake.moveBodyAfterHead(snake.getHead().x + moveX, snake.getHead().y + moveY);
        placeSnakeOnBoard();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    private void draw(Graphics g) {
        g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.drawImage(snakeImage, (getWidth()  - snakeIcon.getIconWidth())  / 2,
                                  (getHeight() - snakeIcon.getIconHeight()) / 2, this);

        String msg = "Press any Key to Continue";
        Font small = new Font("Herculanum", Font.BOLD, 18); // no promises about OS font compatibility!
        FontMetrics metr = getFontMetrics(small);
        g2d.setColor(Color.BLACK);
        g2d.setFont(small);
        g2d.drawString(msg, getWidth() / 2 - 120 , getHeight() / 2 + 130);

        isVertical = currentDirection.ordinal() < 2 ? true : false;

        for( int x = 0; x < boardSize; x++ )
            for( int y = 0; y < boardSize; y++ )
                if( (ge = board[y][x]) != null  ) {
                    centerX = (double)elemSize * ((double)x + (0.5));
                    centerY = (double)elemSize * ((double)y + (0.5));

                    switch (ge) {
                        case Head:
                            psHead = new PolygonFactory(6, centerX, centerY, 5, 2).bullsEyePattern(0.5);
                            g2d.setColor(Color.YELLOW);

                            if(isVertical) {
                                g2d.drawPolygon( PolygonFactory.nDegreeRotation(psHead[0], Math.PI / 2.0, centerX, centerY) );
                                g2d.setColor(Color.BLACK);
                                g2d.fillPolygon( PolygonFactory.nDegreeRotation(psHead[1], Math.PI / 2.0, centerX, centerY) );
                            }
                            else {
                                g2d.drawPolygon( psHead[0]);
                                g2d.setColor(Color.BLACK);
                                g2d.fillPolygon(psHead[1]);
                            }
                            break;

                        case Body:
                            psBody = new PolygonFactory(6, centerX, centerY, 5, 2).bullsEyePattern(0.5);

                            switch ( snake.getOrientationAt(x , y) ) {
                                case Vertical:
                                    psBody[0] = PolygonFactory.nDegreeRotation(psBody[0], Math.PI / 2.0, centerX, centerY);
                                    psBody[1] = PolygonFactory.nDegreeRotation(psBody[1], Math.PI / 2.0, centerX, centerY);
                                    break;

                                case Corner_N$E_S$W:
                                    psBody[0] = PolygonFactory.nDegreeRotation(psBody[0], -Math.PI / 4.0, centerX, centerY);
                                    psBody[1] = PolygonFactory.nDegreeRotation(psBody[1], -Math.PI / 4.0, centerX, centerY);
                                    break;

                                case Corner_N$W_S$E:
                                    psBody[0] = PolygonFactory.nDegreeRotation(psBody[0], Math.PI / 4.0, centerX, centerY);
                                    psBody[1] = PolygonFactory.nDegreeRotation(psBody[1], Math.PI / 4.0, centerX, centerY);

                                default: break; // horizontal
                            }
                            g2d.setColor(Color.YELLOW);
                            g2d.drawPolygon( psBody[0] );
                            g2d.setColor(Color.BLACK);
                            g2d.fillPolygon( psBody[1] );
                    }
                }
        Toolkit.getDefaultToolkit().sync();
        g.dispose();
    }
}
