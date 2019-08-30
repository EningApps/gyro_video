package com.example.videorotation;


public class ShiftQueue {

    public static void main(String[] args) {
        ShiftQueue queue = new ShiftQueue(5);
        queue.put(1);
        queue.put(2);
        queue.put(3);
        queue.put(4);
        queue.put(5);
        queue.put(6);
        queue.put(7);


    }

    private int size;

    public ShiftQueue(int size) {
        this.size = size;
        values = new float[size];
    }

    float[] values;

    private static final int HEAD_INDEX = 0;

    public float get() {
        return values[HEAD_INDEX];
    }

    public void put(float value) {
        shiftRight();
        values[HEAD_INDEX] = value;
    }

    private void shiftRight() {
        if (size > 2) {
            for (int i = size - 2; i >= 0; i--) {
                values[i + 1] = values[i];
            }
        }
    }

}