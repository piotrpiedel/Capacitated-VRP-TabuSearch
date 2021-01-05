package uek.mh.algorithms;

import static java.lang.Math.*;

public class HaversineDistanceCalculator {

    private static final int EARTH_RADIUS_IN_KILOMETERS_UNIT = 6371;

    public double calculateDistance(double latitude1, double longitude1, double latitude2, double longitude2) {
        double deltaLatitude = toRadians(latitude2 - latitude1);
        double deltaLongitude = toRadians(longitude2 - longitude1);
        double squareOfHalfChordLengthBetweenPoints =
                sin(deltaLatitude / 2) * sin(deltaLatitude / 2)
                        + cos(toRadians(latitude1)) * cos(toRadians(latitude2))
                        * sin(deltaLongitude / 2) * sin(deltaLongitude / 2);
        double angularDistanceInRadians = 2 * atan2(
                sqrt(squareOfHalfChordLengthBetweenPoints),
                sqrt(1 - squareOfHalfChordLengthBetweenPoints));
        return angularDistanceInRadians * EARTH_RADIUS_IN_KILOMETERS_UNIT;
    }
}
