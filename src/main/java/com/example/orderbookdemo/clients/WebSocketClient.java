package com.example.orderbookdemo.clients;

import com.example.orderbookdemo.services.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.http.WebSocket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;

@Component
public class WebSocketClient implements WebSocket.Listener {

    private final CountDownLatch latch;
    @Autowired
    private DataService dataService;

    @Autowired
    public WebSocketClient(DataService dataService, CountDownLatch latch) {
        this.dataService = dataService;
        this.latch = latch;
    }

    @Override
    public void onOpen(WebSocket webSocket) {

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        System.out.println(dtf.format(now) + ": " + webSocket.getSubprotocol());
        WebSocket.Listener.super.onOpen(webSocket);
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        dataService.processData(data);
        return WebSocket.Listener.super.onText(webSocket, data, false);

    }

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        System.out.println("ERROR OCCURED: " + webSocket.toString());
        WebSocket.Listener.super.onError(webSocket, error);
    }

    @Override
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
        return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
    }
}
