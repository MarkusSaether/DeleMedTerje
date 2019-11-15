package controller.connection;

import model.Throttle;
import exception.NetworkConnectionException;
import model.Car;
import util.Protocol;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;

public enum ConnController implements Observer {
    INSTANCE;

    private Car car;
    private Socket socket;
    private ConnInputController inputController;
    private ConnOutputController outputController;
    private Timer heartbeatTimer;

    private boolean active;
    private double steer;
    private Throttle throttle;

    private ConnController() {
        active = false;
    }

    public static ConnController getInstance() {
        return INSTANCE;
    }

    /**
     * Returns true if this ConnController currently has an active connection.
     * @return True if this ConnController currently has an active connection.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Registers a Car to this ConnController so that this controller is informed when the state of the car
     * changes.
     * @param car The Car to observe.
     */
    public void registerCar(Car car) {
        this.car = car;
        this.car.addObserver(this);
    }

    /**
     * Returns the Car associated with this ConnController.
     * @return The Car associated with this ConnController.
     */
    public Car getCar() {
        return car;
    }

    /**
     * Returns whether the provided value would be a valid server address.
     * @param value The value to check.
     * @return True if the provided value would be a valid server address.
     */
    public static boolean isValidServerAddress(String value) {
        try {
            InetAddress.getByName(value);
        } catch (UnknownHostException e) {
            return false;
        }
        return true;
    }

    /**
     * Returns whether the provided value could be a valid port number (integer and in the right range).
     * @param value The value to check.
     * @return True if the provided value could be a valid port number.
     */
    public static boolean isValidPortNumber(String value) {
        return value.matches(
                "^([0-9]{1,4}|[1-5][0-9]{4}|6[0-4][0-9]{3}|65[0-4][0-9]{2}|655[0-2][0-9]|6553[0-5])$"
        );
    }

    public void connect(String ipAddress, String port) throws NetworkConnectionException {
        try {
            InetAddress serverAddress = InetAddress.getByName(ipAddress);
            socket = new Socket(serverAddress, Integer.parseInt(port));
//            socket.setSoTimeout(Protocol.HEARTBEAT_PERIOD + 500);
            initialiseHandlers();
            sendHandshake();
        } catch (UnknownHostException e) {
            throw new NetworkConnectionException("Exception while trying to get host", e);
        } catch (SocketException e) {
            throw new NetworkConnectionException("Exception while trying to set the socket timeout duration", e);
        } catch (IOException e) {
            throw new NetworkConnectionException("Exception while trying to set up a socket connection and streams", e);
        }
    }

    /**
     * Initialises the handlers handling the in- and output streams from/to the car.
     * @throws IOException If something went wrong while trying to set up the in- and output handlers.
     */
    private void initialiseHandlers() throws IOException {
        inputController = new ConnInputController(this,
                new BufferedReader(new InputStreamReader(socket.getInputStream())));
        outputController = new ConnOutputController(this,
                new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));
    }

    /**
     * Sets the Car to an active state and starts the heartbeat service.
     */
    void setConnectionValidated() {
        active = true;
        car.setActive(true);
        car.reset();
        startHeartbeat();
    }

    /**
     * Sets the Car to an inactive state, stops the heartbeat service and closes the socket connection if not yet closed.
     */
    public void disconnect() {
        System.out.println("Deactivating connection");
        if (active) {
            active = false;
            car.setActive(false);
            stopHeartbeat();
            if (!socket.isClosed()) {
                try {
                    outputController.close();
                    inputController.close();
                } catch (IOException ex) {
                    // TODO: Implement exception handling for ConnController disconnect mechanism.
                }
            }
        }
    }

    /**
     * Sends a handshake message to the server to establish whether a proper connection was set up.
     */
    private void sendHandshake() throws NetworkConnectionException {
        outputController.handshake();
    }

    /**
     * Starts the heartbeat-part of the protocol (timed heartbeat messages are sent to the RC Car).
     */
    private void startHeartbeat() {
        heartbeatTimer = new Timer();
        heartbeatTimer.scheduleAtFixedRate(outputController, 0, Protocol.HEARTBEAT_PERIOD);
    }

    /**
     * Stops the heartbeat-part of the protocol (timed heartbeat messages are no longer sent to the RC car).
     */
    private void stopHeartbeat() {
        heartbeatTimer.cancel();
    }

    @Override
    public void update(Observable o, Object arg) {
        int nSteer = car.getSteer();
        Throttle nThrottle = car.getThrottle();
        try {
            if (!active) {
                sendHandshake();
            } else if (steer != nSteer) {
                outputController.steer(nSteer);
                steer = nSteer;
            } else if (throttle != nThrottle) {
                outputController.throttle(car.getThrottle());
                throttle = nThrottle;
            }
        } catch (NetworkConnectionException ex) {
            // TODO: Implement exception handling for ConnController update mechanism.
        }
    }
}
