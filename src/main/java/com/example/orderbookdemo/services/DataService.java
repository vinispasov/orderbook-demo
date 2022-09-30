package com.example.orderbookdemo.services;


public interface DataService {
    void processData(CharSequence data);

    void openAndStreamWebSocketSubscription(String connectionURL, String webSocketSubscription);

    boolean closeWebSocketSubscription();
}
