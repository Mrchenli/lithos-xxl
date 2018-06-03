package com.ik2k.lithos.core.enums;

public enum  ExecutorBlockStrategy {

    SERIAL_EXECUTION("Serial execution"),
    /*CONCURRENT_EXECUTION("并行"),*/
    DISCARD_LATER("Discard Later"),
    COVER_EARLY("Cover Early");

    private String title;

    ExecutorBlockStrategy(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }


}
