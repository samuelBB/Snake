package SnakeGameProject;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.concurrent.CountDownLatch;

public class GameEngine {

    boolean gameOver;
    char playAgain;
    private final double gameSpeed;
    private SnakeGame gameInstance;

    public GameEngine(double gs, SnakeGame gi) {
        gameSpeed = gs;
        gameInstance = gi;
    }

    // start new thread and run the game loop in it
    public void runGameLoop() {
        Thread threadLoop = new Thread() {
            public void run() {
                gameLoop();
            }
        };
        threadLoop.start();
    }

    private void gameLoop() {
        /** Anything commented out is for diagnostics re fps */

        // int fps = 60,
        //     frameCount = 0;

        final double TIME_BETWEEN_UPDATES = 1000000000 / gameSpeed; // how many ns each frame should take for target game hz
        final int MAX_UPDATES_BEFORE_RENDER = 5;
        double lastUpdateTime = System.nanoTime();
        double lastRenderTime;
        final double TARGET_FPS = 60; // stop rendering if desired fps achieved
        final double TARGET_TIME_BETWEEN_RENDERS = 1000000000 / TARGET_FPS;

        // Simple way of finding FPS.
        // int lastSecondTime = (int) (lastUpdateTime / 1000000000);

        while (true) {
            /** on unpause snake "jumps" */
            if(gameInstance.paused) {
                waitOnUnPause();
                gameInstance.paused = false;
            }
            // use latch on a bool to check for a pause - the snakeGame class should have a var that gets set at input
            // P and the latch will be unlatched at input Q
            // where to put the latch in this loop (minimize snake movement after P is pressed)

            double now = System.nanoTime();
            int updateCount = 0;

            // update game as many times as needed ("play catchup" if necessary)
            while( now - lastUpdateTime > TIME_BETWEEN_UPDATES && updateCount < MAX_UPDATES_BEFORE_RENDER ) {
                gameInstance.update();
                lastUpdateTime += TIME_BETWEEN_UPDATES;
                updateCount++;
            }

            // If an update hangs, don't do too many "catchups"
            if ( now - lastUpdateTime > TIME_BETWEEN_UPDATES) {
                lastUpdateTime = now - TIME_BETWEEN_UPDATES;
            }

            // render
            gameInstance.board.repaint();
            lastRenderTime = now;

            if(gameOver) {
                waitOnUser();
                if(Character.toLowerCase(playAgain) == 'n')
                    System.exit(0);
                else break;
            }

            // update frames
            // int thisSecond = (int) (lastUpdateTime / 1000000000);
            // if (thisSecond > lastSecondTime)
            // {
            //    System.out.println("NEW SECOND " + thisSecond + " " + frameCount);
            //    fps = frameCount;
            //    frameCount = 0;
            //    lastSecondTime = thisSecond;
            //}

            // yield until target time achieved to avoid hogging CPU
            while ( now - lastRenderTime < TARGET_TIME_BETWEEN_RENDERS && now - lastUpdateTime < TIME_BETWEEN_UPDATES) {
                Thread.yield();

                // prevents game from hogging CPU
                // removing this still works (maybe even better), but CPU-hogging will be a factor on some OSs
                // on the other hand, this can cause bad stuttering on some OSs
                try {Thread.sleep(1);} catch(Exception ignored) {}

                now = System.nanoTime();
            }
        }

        gameOver = false;
        gameInstance.initializeGame();
    }

    public void waitOnUser() {
        final CountDownLatch latch = new CountDownLatch(1);
        KeyEventDispatcher dispatcher = new KeyEventDispatcher() {
            // Anonymous class invoked from EDT
            public boolean dispatchKeyEvent(KeyEvent e) {
                char key = e.getKeyChar();
                switch(key) {
                    case 'N':
                    case 'n':
                    case 'Y':
                    case 'y':
                        playAgain = key;
                        latch.countDown();
                        return false;

                    default:
                        return false;
                }
            }
        };
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(dispatcher);
        try {
            latch.await();  // current thread waits until countDown() called
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(dispatcher);
    }

    public void waitOnUnPause() {
        final CountDownLatch latch = new CountDownLatch(1);
        KeyEventDispatcher dispatcher = new KeyEventDispatcher() {
            // Anonymous class invoked from EDT
            public boolean dispatchKeyEvent(KeyEvent e) {
                char key = e.getKeyChar();
                switch(key) {
                    case 'Q':
                    case 'q':
                        latch.countDown();
                        return false;
                    default:
                        return false;
                }
            }
        };
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(dispatcher);
        try {
            latch.await();  // current thread waits until countDown() called
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(dispatcher);
    }
}