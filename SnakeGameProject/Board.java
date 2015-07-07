package SnakeGameProject;

import javax.swing.JPanel;
import java.awt.*;

// needed to color snake randomly
// import java.util.Random;

public class Board extends JPanel {
    SnakeGame gameInstance;
    GameElement[][] boardArray;
    GameElement ge;
    Graphics2D g2d;

    int boardSize;
    int elementSize;
    double rotationConst = 0.0, rotationSpeed = 24.0;
    int fruitShape = 3;

    boolean isVertical;
    Polygon[] psHead, psBody, psFruit;
    double centerX, centerY;

    // needed to color snake randomly
    // private final Color[] colors = {
    //        Color.BLUE, Color.CYAN, Color.GRAY, Color.GREEN, Color.RED, Color.PINK, Color.MAGENTA, Color.YELLOW,
    //        Color.ORANGE, Color.DARK_GRAY, Color.WHITE, Color.LIGHT_GRAY
    // };
    // private Random colorGen = new Random();

    public Board(int bSize, int eSize, SnakeGame gi) {
       boardSize = bSize;
       elementSize = eSize;
       boardArray = new GameElement[boardSize][boardSize];
       gameInstance = gi;
       setPreferredSize( new Dimension(boardSize * elementSize, boardSize * elementSize) );
       setBackground(Color.BLACK);
    }

    public GameElement getElementAtPosition(Point p) {
        return boardArray[p.x][p.y];
    }

    public void setElementAtPosition(Point p, GameElement ge) {
        boardArray[p.y][p.x] = ge;
    }

    // needed to color snake randomly
    // public Color randomColor() {
    //    return colors[ colorGen.nextInt(colors.length) ];
    //}

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if(!gameInstance.inGame) {
            String msg = "Game Over. Play Again? (y or n)";
            Font small = new Font("Herculanum", Font.BOLD, 18); // no promises about OS font compatibility!
            g2d.setColor(Color.white);
            g2d.setFont(small);
            g2d.drawString(msg, getWidth() / 2 - 145, getHeight() / 2 - 10);
            gameInstance.g.gameOver = true;
        }
        else {
            isVertical = gameInstance.getDirection().ordinal() < 2;

            // to have each body link a different color permanently, must have snake links have a color field.
            // we'd then have to have a getColorAt(x, y) like the orientation method, so maybe better to have a
            // getLinkNoAt(x, y) then use that to retrieve color and orientation, for efficiency.
            // or could have a hash-table in snake class defined by:
            // (K, V) = (point(x-coord, y-coord), link #)
            // this would achieve lookup efficiency; question: would we clear after every position update?
            // unclear if space or speed concerns matter more here, or maybe doesn't matter; test...

            for (int x = 0; x < boardSize; x++)
                for (int y = 0; y < boardSize; y++)
                    if ((ge = boardArray[y][x]) != null) {
                        centerX = (double) elementSize * ((double) x + (0.5));
                        centerY = (double) elementSize * ((double) y + (0.5));

                        switch (ge) {
                            case Head:
                                psHead = new PolygonFactory(6, centerX, centerY, 5, 2).bullsEyePattern(0.5);
                                g2d.setColor(Color.YELLOW);

                                if (isVertical) {
                                    g2d.drawPolygon(PolygonFactory.nDegreeRotation(psHead[0], Math.PI / 2.0, centerX, centerY));
                                    g2d.setColor(Color.RED);
                                    g2d.fillPolygon(PolygonFactory.nDegreeRotation(psHead[1], Math.PI / 2.0, centerX, centerY));
                                } else {
                                    g2d.drawPolygon(psHead[0]);
                                    g2d.setColor(Color.RED);
                                    g2d.fillPolygon(psHead[1]);
                                }
                                break;

                            case Body:
                                psBody = new PolygonFactory(6, centerX, centerY, 5, 3).bullsEyePattern(2.0);
                                // g2d.setColor( randomColor() /* Color. ... */ )
                                int i = 0;

                                switch (gameInstance.snake.getOrientationAt(x, y)) {
                                    case Horizontal:
                                        for (Polygon p : psBody) {
                                            g2d.setColor(i++ % 2 == 0 ? Color.RED : Color.YELLOW /*randomColor()*/);
                                            g2d.drawPolygon(p);
                                        }
                                        break;

                                    case Vertical:
                                        for (Polygon p : psBody) {
                                            g2d.setColor(i++ % 2 == 0 ? Color.RED : Color.YELLOW /*randomColor()*/);
                                            g2d.drawPolygon(PolygonFactory.nDegreeRotation(p, Math.PI / 2.0, centerX, centerY));
                                        }
                                        break;

                                    case Corner_N$E_S$W:
                                        for (Polygon p : psBody) {
                                            g2d.setColor(i++ % 2 == 0 ? Color.RED : Color.YELLOW /*randomColor()*/);
                                            g2d.drawPolygon(PolygonFactory.nDegreeRotation(p, -Math.PI / 4.0, centerX, centerY));
                                        }
                                        break;

                                    case Corner_N$W_S$E:
                                        for (Polygon p : psBody) {
                                            g2d.setColor(i++ % 2 == 0 ? Color.RED : Color.YELLOW /*randomColor()*/);
                                            g2d.drawPolygon(PolygonFactory.nDegreeRotation(p, Math.PI / 4.0, centerX, centerY));
                                        }
                                }
                                break;

                            case Fruit:
                                // only re-render if fruit changed?
                                // raises larger question about re-rendering only parts of board
                                g2d.setColor(Color.GREEN);
                                psFruit = new PolygonFactory(fruitShape, centerX, centerY, 5, 3).bullsEyePattern(2.0);

                                for (Polygon p : psFruit)
                                    g2d.drawPolygon(PolygonFactory.nDegreeRotation(p, rotationConst, centerX, centerY));

                                rotationConst += ((Math.PI / rotationSpeed) % (2 * Math.PI));
                        }
                    }

            if(gameInstance.paused) {
                String msg = "Paused";
                Font small = new Font("Pristina", Font.BOLD, 18); // no promises about OS font compatibility!
                g2d.setColor(Color.white);
                g2d.setFont(small);
                g2d.drawString(msg, getWidth() / 2 - 105, getHeight() / 2 - 10);
            }
        }
        Toolkit.getDefaultToolkit().sync();
        g2d.dispose();
    }
}