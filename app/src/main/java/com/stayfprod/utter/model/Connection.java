package com.stayfprod.utter.model;


public class Connection {
    public enum State {
        Waiting_for_network("Waiting for network..."),
        Connecting("Connecting..."),
        Updating("Updating..."),
        Ready("Utter");

        public String text;

        State(String text) {
            this.text = text;
        }
    }

    public static volatile State currentState = State.Ready;
    public static volatile boolean isConnected = false;
}
