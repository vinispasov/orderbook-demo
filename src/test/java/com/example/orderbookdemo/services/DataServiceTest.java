package com.example.orderbookdemo.services;

import com.example.orderbookdemo.repositories.OrderbookRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DataServiceTest {

    @Autowired
    private DataService dataService;
    @MockBean
    private OrderbookRepository orderbookRepository;

    @Test
    public void when_closeWebSocketSubscription_And_isNotOpen_then_isClosedFalse() {
        boolean isClosed = dataService.closeWebSocketSubscription();

        Assertions.assertFalse(isClosed);
    }

}
