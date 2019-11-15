package controller.connection;

import model.Throttle;
import exception.NetworkConnectionException;
import util.Protocol;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TimerTask;

/**
 * Class handling the outputs from this client to the remote car.
 */
class ConnOutputController extends TimerTask {
    private final ConnController connection;
    private final BufferedWriter writer;
    private final ConnOutputQueue queue;

    /**
     * Creates a new ConnOutputController that handles the outgoing data to the car.
     * @param connection The controller handling the general connection to the car.
     * @param writer The output stream writer to the car.
     */
    ConnOutputController(ConnController connection, BufferedWriter writer) {
        this.connection = connection;
        this.writer = writer;
        this.queue = new ConnOutputQueue();
    }

    /**
     * Sends the given String to the remote car.
     * @param message The String to send to the remote car.
     * @throws NetworkConnectionException If sending the message resulted in an error.
     */
    private void write(String message) throws NetworkConnectionException {
        try {
            writer.write(message);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            throw new NetworkConnectionException("Error while trying to write to the output stream", e);
        }
    }

    /**
     * Sends a handshake command to the remote car to establish whether the connection is valid.
     * @throws NetworkConnectionException If sending the command resulted in an error.
     */
    void handshake() throws NetworkConnectionException {
        write(Protocol.HANDSHAKE);
    }

    /**
     * Closes this controller's associated OutputStream.
     * @throws IOException If something went wrong while closing the OutputStream.
     */
    void close() throws IOException {
        writer.close();
    }

    /**
     * Sends a throttle command to the remote car to throttle in the given direction.
     * @param dir The direction to throttle in, either neutral, forward, or backward.
     */
    void throttle(Throttle dir) {
        queue.add(Protocol.getThrottleCommand(dir));
    }

    /**
     * Sends a steering command to the remote car to put the wheels at the given angle.
     * @param angle The angle to put the wheels in, which must be between 0 and 180.
     */
    void steer(int angle) {
        queue.add(Protocol.getSteeringCommand(angle));
    }

    @Override
    public void run() {
        try {
            // Send a command from the queue if there is any, and a general heartbeat command otherwise
            String message = queue.isEmpty() ? Protocol.HEARTBEAT : queue.read();
            write(message);
        } catch (NetworkConnectionException e) {
            connection.disconnect();
        }
    }

    static class ConnOutputQueue {
        private final ArrayList<String> queue;

        /**
         * Creates a new OutputQueue.
         */
        ConnOutputQueue() {
            queue = new ArrayList<>();
        }

        /**
         * Returns true if the current output queue contains is empty.
         * @return True if the output queue currently contains no new commands to send.
         */
        synchronized boolean isEmpty() {
            return queue.size() == 0;
        }

        /**
         * Returns the first command from the queue (FIFO) and removes it from the queue.
         * @return The first command from the queue.
         */
        synchronized String read() {
            String message = queue.get(0);
            queue.remove(0);
            return message;
        }

        /**
         * Adds a new command to the queue.
         * @param message The command to add to the queue.
         */
        synchronized void add(String message) {
            queue.add(message);
        }
    }
}
