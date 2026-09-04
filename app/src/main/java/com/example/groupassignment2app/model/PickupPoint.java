package com.example.groupassignment2app.model;

import java.util.ArrayList;
import java.util.List;

public class PickupPoint {

    public final String name;
    public final float x;
    public final float y;

    public PickupPoint(String name, float x, float y) {
        this.name = name;
        this.x = x;
        this.y = y;
    }

    public static List<PickupPoint> all() {
        List<PickupPoint> points = new ArrayList<>();

        points.add(new PickupPoint("Student Pavilion II",              0.61f, 0.11f));
        points.add(new PickupPoint("Lecture Complex II (LDK)",         0.53f, 0.16f));
        points.add(new PickupPoint("Dewan Tun Dr Ling Liong Sik",      0.38f, 0.28f));
        points.add(new PickupPoint("Faculty of Business and Finance",  0.76f, 0.29f));
        points.add(new PickupPoint("Faculty of Information and Communication Technology",
                0.32f, 0.42f));
        points.add(new PickupPoint("Faculty of Engineering and Green Technology",
                0.77f, 0.42f));
        points.add(new PickupPoint("ZUS Coffee",                       0.67f, 0.56f));
        points.add(new PickupPoint("Centre for Extension Education",   0.59f, 0.72f));

        return points;
    }

    public static PickupPoint byName(String name) {
        if (name == null) return null;
        for (PickupPoint p : all()) {
            if (p.name.equals(name)) return p;
        }
        return null;
    }

    public String shortName() {
        if (name.startsWith("Faculty of Information")) return "FICT";
        if (name.startsWith("Faculty of Engineering")) return "FEGT";
        if (name.startsWith("Faculty of Business")) return "FBF";
        if (name.startsWith("Dewan")) return "Dewan Tun";
        if (name.startsWith("Centre for Extension")) return "Extension Ed.";
        if (name.startsWith("Lecture Complex")) return "LDK";
        if (name.startsWith("Student Pavilion")) return "Pavilion II";
        return name;
    }
}