package uek.mh.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Coordinates {
    private final double latitude;
    private final double longitude;
    private String name;
}
