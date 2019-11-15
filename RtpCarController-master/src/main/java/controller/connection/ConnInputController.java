package controller.connection;

import util.Protocol;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Class handling the inputs received from the remote car.
 */
class ConnInputController extends Thread {
    private final ConnController connection;
    private final BufferedReader reader;
    private boolean active;

    public ConnInputController(ConnController connection, BufferedReader reader) {
        this.connection = connection;
        this.reader = reader;
        this.active = true;
        this.start();
    }

    private void parseInput(String input) {
        if (input == null || input.split(" ").length > 2) {
            return;
        }

        switch (input) {
            case Protocol.HANDSHAKE:
                connection.setConnectionValidated();
                break;
            case Protocol.CLOSE_CONNECTION:
                active = false;
                connection.disconnect();
                break;
            default:
                break;
        }
    }

    /**
     * Closes this ConnInputController's associated InputStream.
     */
    void close() {
        try {
            reader.close();
        } catch (IOException e) {}
    }

    @Override
    public void run() {
        String input;
        while (active) {
            try {
                input = reader.readLine();
                parseInput(input);
            } catch (IOException e) {
                connection.disconnect();
                break;
            }
        }
    }
}
