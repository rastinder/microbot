package net.runelite.client.plugins.microbot.rasMasterScript;

import net.runelite.api.Point;
import net.runelite.client.plugins.microbot.Microbot;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import static net.runelite.client.plugins.microbot.util.Global.sleep;
import static net.runelite.client.plugins.microbot.util.math.Random.random;

public class MousePositionSender {

    public static void main(String[] args) {
        // Example target point, this will be passed to the function
        int targetX = 100; // Replace with point.getX() in actual use
        int targetY = 100; // Replace with point.getY() in actual use

        String serverIp = "192.168.1.103";
        int port = 4455;
        //while (true) {
        //    String message = "a,8," + random(-100,100) + "," + random(-100,100)+"|";
        //    sendStringToServer(message, serverIp, port);
        //}

        // Call the function to calculate the difference and send the TCP packet
        //sendMousePositionDifference(targetX, targetY);
        tcpIp(targetX,targetY);
    }

    public static void sendMousePositionDifference(int targetX, int targetY) {
        try {
            // Get the current mouse position
            Point mousePosition = Microbot.getClient().getMouseCanvasPosition();
            int currentX = mousePosition.getX(); // Current X-coordinate
            int currentY = mousePosition.getY(); // Current Y-coordinate
            int tx = 765; // Replace with point.getX() in actual use
            int ty = 503; // Replace with point.getY() in actual use

            // Calculate the difference
            int x = tx + targetX - currentX;
            int y = ty+ targetY - currentY;


            tcpIp(x,y);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void sendStringToServer(String message, String serverIp, int port) {
        int delay = random(100,1000); // random.nextInt(201) gives 0 to 200, subtract 100 to get -100 to 100
        try (Socket socket = new Socket(serverIp, port)) {
            // Get the output stream of the socket
            OutputStream outputStream = socket.getOutputStream();

            // Convert the message to UTF-8 bytes
            byte[] messageBytes = message.getBytes("UTF-8");

            // Send the bytes to the server
            outputStream.write(messageBytes);
            Thread.sleep(delay);
            //outputStream.flush(); // Ensure all data is sent
            // Generate a random sleep time between -100 and 100 milliseconds

            Thread.sleep(delay);

            System.out.println("Message sent: " + message + " (sleep for " + delay + " ms)");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException("Sleep interrupted", e);
        }
    }
    public static void tcpIp(int x, int y) {
        try {
            // Format the string similar to Python's f-string
            //String message = "a,8," + x + "," + "0|";
            //System.out.print(message);
            // Create a socket connection to the server
            Socket socket = new Socket("192.168.1.103", 4455);
            OutputStream outputStream = socket.getOutputStream();

            // Send the formatted string encoded in UTF-8

            for (int i =0; i < 100; i++) {
                String message = "a,8," + random(-100,100) + "," + random(-100,100)+"|";
                outputStream.write(message.getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
                int delay = random(100,1000); // random.nextInt(201) gives 0 to 200, subtract 100 to get -100 to 100
                Thread.sleep(delay);
            }

            // Close the socket connection
            outputStream.close();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
