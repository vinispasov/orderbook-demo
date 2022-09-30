package com.example.orderbookdemo.controllers;

import com.example.orderbookdemo.services.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderbookController {

    private final DataService dataService;

    @Autowired
    public OrderbookController(DataService dataService) {
        this.dataService = dataService;
    }

    @GetMapping("/subscribe")
    public ResponseEntity<String> subscribe() {
        String publicWebSocketURL = "wss://ws.kraken.com/";
        String publicWebSocketSubscriptionMsg = "{ \"event\": \"subscribe\", \"subscription\": { \"name\": \"spread\"}, \"pair\": [ \"XBT/USD\",\"ETH/USD\" ]}";

        new Thread(() -> dataService.openAndStreamWebSocketSubscription(publicWebSocketURL, publicWebSocketSubscriptionMsg)).start();

        return new ResponseEntity<>("subscribed", HttpStatus.OK);
    }

    @GetMapping("/unsubscribe")
    public ResponseEntity<String> unsubscribe() {
        boolean isClosed = dataService.closeWebSocketSubscription();
        if (isClosed) {
            return new ResponseEntity<>("unsubscribed", HttpStatus.OK);
        }
        return new ResponseEntity<>("You should subscribe first.", HttpStatus.BAD_REQUEST);
    }
}
