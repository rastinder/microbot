package net.runelite.client.plugins.microbot.rasMasterScript;

import net.runelite.api.Point;
import net.runelite.client.plugins.microbot.Microbot;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Random;

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
    //private static final String SERVER_ADDRESS = "192.168.1.103";
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int SERVER_PORT = 4456;
    private static final int MAX_RETRIES = 5;
    private static final int RETRY_DELAY_MS = 1000; // 1 second

    public static void tcpIp(int x, int y) {
        Random random = new Random();
        int delay = random.nextInt(6) + 5; // Random delay between 5 and 10 milliseconds

        int retries = 0;
        boolean connected = false;

        while (!connected && retries < MAX_RETRIES) {
            Socket socket = null;
            OutputStream outputStream = null;

            try {
                // Create a socket connection to the server
                socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                outputStream = socket.getOutputStream();

                // Send the formatted string encoded in UTF-8
                String message = "a,8," + x + "," + y ;
                System.out.println("Sending message: " + message);
                outputStream.write(message.getBytes(StandardCharsets.UTF_8));
                outputStream.flush();

                System.out.println("Message sent successfully.");
                connected = true; // Connection was successful

                // Wait before closing the connection
                Thread.sleep(delay);
            } catch (Exception e) {
                System.err.println("Connection failed or error occurred: " + e.getMessage());
                e.printStackTrace();
                retries++;

                if (retries < MAX_RETRIES) {
                    System.out.println("Retrying connection in 1 second... (" + retries + "/" + MAX_RETRIES + ")");
                    try {
                        Thread.sleep(RETRY_DELAY_MS); // Wait before retrying
                    } catch (InterruptedException ie) {
                        System.err.println("Interrupted during retry delay: " + ie.getMessage());
                        Thread.currentThread().interrupt(); // Restore interrupted status
                    }
                } else {
                    System.err.println("Max retries reached. Unable to connect.");
                }
            } finally {
                // Ensure resources are closed
                try {
                    if (outputStream != null) {
                        outputStream.close();
                    }
                } catch (Exception e) {
                    System.err.println("Error closing OutputStream: " + e.getMessage());
                }

                try {
                    if (socket != null && !socket.isClosed()) {
                        socket.close();
                    }
                } catch (Exception e) {
                    System.err.println("Error closing Socket: " + e.getMessage());
                }
            }
        }
    }
}
