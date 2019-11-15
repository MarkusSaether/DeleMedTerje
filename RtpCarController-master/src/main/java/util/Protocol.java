package util;

import model.Steer;
import model.Throttle;

public class Protocol {
    public static final String HANDSHAKE = "HANDSHAKE";
    private static final String THROTTLE = "THROTTLE";
    private static final String STEER = "STEER";
    public static final String HEARTBEAT = "HB";
    public static final String CLOSE_CONNECTION = "CLOSE";
    public static final int HEARTBEAT_PERIOD = 500;

    /**
     * Returns the steering angle transformed from a direction to degrees.
     * @param dir The steering direction
     * @return The given steering direction transformed into an angle in degrees; 90 degrees (neutral) if given
     * direction is invalid.
     */
    public static int getDegreesFromSteer(Steer dir) {
        switch (dir) {
            case LEFT: return 0;
            case RIGHT: return 180;
            default: return 90;
        }
    }

    /**
     * Returns a command to send to the RC Car using the given key and value.
     * @param key The type of command that must be created.
     * @param value The value that must be passed with the command.
     * @return A formatted string that can be sent to the RC Car as a command.
     */
    private static String formatCommand(String key, String value) {
        return String.format("%s %s", key, value);
    }

    /**
     * Returns a throttle command with the given throttle direction.
     * @param dir The direction to throttle in.
     * @return A formatted string that can be sent to the RC Car as a throttling command.
     */
    public static String getThrottleCommand(Throttle dir) {
        return formatCommand(Protocol.THROTTLE, dir.toString());
    }

    /**
     * Returns a steering command with the given steering direction.
     * @param angle The angle to steer in.
     * @return A formatted string that can be sent to the RC Car as a steering command.
     */
    public static String getSteeringCommand(int angle) {
        return formatCommand(Protocol.STEER, String.valueOf(angle));
    }
}
