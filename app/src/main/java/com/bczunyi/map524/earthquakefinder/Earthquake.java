package com.bczunyi.map524.earthquakefinder;

import java.io.Serializable;

public class Earthquake implements Serializable {
    String time;
    String location;
    double lat;
    double lon;
    double depth;
    double magnitude;
    String magType;

    public Earthquake(String t, String l, double l1, double l2, double d, double m, String mt) {
        time = t;
        location = l;
        lat = l1;
        lon = l2;
        depth = d;
        magnitude = m;
        magType = mt;
    }
}
