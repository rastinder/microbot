package net.runelite.client.plugins.microbot.util.mouse;

import net.runelite.api.Point;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.math.Random;

import javax.inject.Inject;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static net.runelite.client.plugins.microbot.util.Global.sleep;
import static net.runelite.client.plugins.microbot.util.math.Random.random;

import java.awt.Robot;
import java.awt.MouseInfo;

public class VirtualMouse extends Mouse {

    private final ScheduledExecutorService scheduledExecutorService;

    @Inject
    public VirtualMouse() {
        super();
        this.scheduledExecutorService = Executors.newScheduledThreadPool(10);
    }

    public Mouse click(Point point, boolean rightClick) {

        if (point == null) return this;

        mouseEvent(MouseEvent.MOUSE_ENTERED, jitterPoint(point), rightClick);
        mouseEvent(MouseEvent.MOUSE_EXITED, jitterPoint(point), rightClick);
        mouseEvent(MouseEvent.MOUSE_MOVED, jitterPoint(point), rightClick);
        sleep(random(10, 30));  // Human-like delay

        mouseEvent(MouseEvent.MOUSE_PRESSED, point, rightClick);

        // Randomly move the mouse while pressed
        if (random(0, 100) < 50) {  // 50% chance to perform the movement
            moveWhilePressed(point);
        }

        sleep(random(80, 120)); // Simulate hold time before release
        mouseEvent(MouseEvent.MOUSE_RELEASED, jitterPoint(point), rightClick);
        sleep(random(20, 50));  // Slight delay before the last event
        mouseEvent(MouseEvent.MOUSE_FIRST, jitterPoint(point), rightClick);

        return this;
    }

    private void moveWhilePressed(Point point) {
        long time = System.currentTimeMillis();
        int moveTime = random(8, 20); // Randomly move the mouse for 8 to 20 ms

        while (System.currentTimeMillis() - time < moveTime) {
            Point randomMove = jitterPoint(point);
            MouseEvent mouseMove = new MouseEvent(getCanvas(), MouseEvent.MOUSE_MOVED, System.currentTimeMillis(), 0, randomMove.getX(), randomMove.getY(), 1, false, MouseEvent.BUTTON1);
            getCanvas().dispatchEvent(mouseMove);
            sleep(random(1, 5)); // Short sleep to simulate natural movement
        }
    }

    public Mouse click(int x, int y) {
        return click(new Point(x, y), false);
    }

    public Mouse click(double x, double y) {
        return click(new Point((int) x, (int) y), false);
    }

    public Mouse click(Rectangle rectangle) {
        return click(new Point((int) rectangle.getCenterX(), (int) rectangle.getCenterY()), false);
    }

    @Override
    public Mouse click(int x, int y, boolean rightClick) {
        return click(new Point(x, y), rightClick);
    }

    @Override
    public Mouse click(Point point) {
        return click(point, false);
    }

    @Override
    public Mouse click() {
        return click(new Point((int) MouseInfo.getPointerInfo().getLocation().getX(), (int) MouseInfo.getPointerInfo().getLocation().getY()));
    }

    public Mouse move(Point point) {
        long time = System.currentTimeMillis();

        Point jitteredPoint = jitterPoint(point); // Add jitter for human-like behavior
        MouseEvent mouseMove = new MouseEvent(getCanvas(), MouseEvent.MOUSE_MOVED, time, 0, jitteredPoint.getX(), jitteredPoint.getY(), 1, false, MouseEvent.BUTTON1);

        getCanvas().dispatchEvent(mouseMove);

        return this;
    }

    public Mouse move(Rectangle rect) {
        long time = System.currentTimeMillis();

        Point jitteredPoint = jitterPoint(new Point((int) rect.getCenterX(), (int) rect.getCenterY()));
        MouseEvent mouseMove = new MouseEvent(getCanvas(), MouseEvent.MOUSE_MOVED, time, 0, jitteredPoint.getX(), jitteredPoint.getY(), 1, false, MouseEvent.BUTTON1);

        getCanvas().dispatchEvent(mouseMove);

        return this;
    }

    public Mouse move(Polygon polygon) {
        long time = System.currentTimeMillis();
        Point point = new Point((int) polygon.getBounds().getCenterX(), (int) polygon.getBounds().getCenterY());

        Point jitteredPoint = jitterPoint(point);
        MouseEvent mouseMove = new MouseEvent(getCanvas(), MouseEvent.MOUSE_MOVED, time, 0, jitteredPoint.getX(), jitteredPoint.getY(), 1, false, MouseEvent.BUTTON1);

        getCanvas().dispatchEvent(mouseMove);

        return this;
    }

    public Mouse scrollDown(Point point) {
        long time = System.currentTimeMillis();

        move(point);

        scheduledExecutorService.schedule(() -> {
            Point jitteredPoint = jitterPoint(point);
            MouseEvent mouseScroll = new MouseWheelEvent(getCanvas(), MouseEvent.MOUSE_WHEEL, time, 0, jitteredPoint.getX(), jitteredPoint.getY(), 0, false,
                    0, 10, 2);

            getCanvas().dispatchEvent(mouseScroll);

        }, random(40, 100), TimeUnit.MILLISECONDS);
        return this;
    }

    public Mouse scrollUp(Point point) {
        long time = System.currentTimeMillis();

        Point jitteredPoint = jitterPoint(point);
        MouseEvent mouseScroll = new MouseWheelEvent(getCanvas(), MouseEvent.MOUSE_WHEEL, time, 0, jitteredPoint.getX(), jitteredPoint.getY(), 0, false,
                0, -10, -2);

        getCanvas().dispatchEvent(mouseScroll);

        return this;
    }

    @Override
    public java.awt.Point getMousePosition() {
        PointerInfo pointerInfo = MouseInfo.getPointerInfo();

        return pointerInfo != null ? pointerInfo.getLocation() : null;
    }

    @Override
    public Mouse move(int x, int y) {
        return move(new Point(x, y));
    }

    @Override
    public Mouse move(double x, double y) {
        return move(new Point((int) x, (int) y));
    }

    private void mouseEvent(int id, Point point, boolean rightClick)
    {
        int button = rightClick ? MouseEvent.BUTTON3 : MouseEvent.BUTTON1;

        Point jitteredPoint = jitterPoint(point); // Add jitter for human-like behavior
        MouseEvent e = new MouseEvent(Microbot.getClient().getCanvas(), id, System.currentTimeMillis(), 0, jitteredPoint.getX(), jitteredPoint.getY(), 1, false, button);

        getCanvas().dispatchEvent(e);
    }

    private Point jitterPoint(Point point) {
        int jitterX = random(-1, 2); // Increased jitter for a more human-like effect
        int jitterY = random(-1, 2);
        return new Point(point.getX() + jitterX, point.getY() + jitterY);
    }
}
