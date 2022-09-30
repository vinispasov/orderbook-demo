package com.example.orderbookdemo.services;

import com.example.orderbookdemo.clients.WebSocketClient;
import com.example.orderbookdemo.repositories.OrderbookRepository;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

@Service
public class DataServiceImpl implements DataService {

    private static final int MAX_ELEMENTS = 10;
    private static final String DECIMAL_NUMBER_REGEX = "^\\d*\\.\\d+|\\d+\\.\\d*$";
    private static final List<String> PAIRS = List.of("XBT/USD", "ETH/USD");
    private static final Map<Double, Double> ethBids = new LinkedHashMap<>() {
        private static final long serialVersionUID = 1L;

        @Override
        protected boolean removeEldestEntry(Map.Entry<Double, Double> eldest) {
            return this.size() > MAX_ELEMENTS;
        }
    };
    private static final Map<Double, Double> ethAsks = new LinkedHashMap<>() {
        private static final long serialVersionUID = 1L;

        @Override
        protected boolean removeEldestEntry(Map.Entry<Double, Double> eldest) {
            return this.size() > MAX_ELEMENTS;
        }
    };
    private static final Map<Double, Double> xbtBids = new LinkedHashMap<>() {
        private static final long serialVersionUID = 1L;

        @Override
        protected boolean removeEldestEntry(Map.Entry<Double, Double> eldest) {
            return this.size() > MAX_ELEMENTS;
        }
    };
    private static final Map<Double, Double> xbtAsks = new LinkedHashMap<>() {
        private static final long serialVersionUID = 1L;

        @Override
        protected boolean removeEldestEntry(Map.Entry<Double, Double> eldest) {
            return this.size() > MAX_ELEMENTS;
        }
    };
    private static boolean isAborted = false;
    private static WebSocket ws;
    private final OrderbookRepository orderbookRepository;
    Map<Double, Double> orderedBids = new TreeMap<>() {
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            forEach((key, value) -> sb.append("[").append(key).append(", ").append(value).append("]").append(System.getProperty("line.separator")));
            return sb.toString();
        }
    };
    Map<Double, Double> orderedAsks = new TreeMap<>() {
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            forEach((key, value) -> sb.append("[").append(key).append(", ").append(value).append("]").append(System.getProperty("line.separator")));
            return sb.toString();
        }
    };

    @Autowired
    public DataServiceImpl(OrderbookRepository orderbookRepository) {
        this.orderbookRepository = orderbookRepository;
    }

    @Override
    public void openAndStreamWebSocketSubscription(String connectionURL, String webSocketSubscription) {

        try {
            CountDownLatch latch = new CountDownLatch(1);

            if (isAborted || ws == null) {
                ws = HttpClient
                        .newHttpClient()
                        .newWebSocketBuilder()
                        .buildAsync(URI.create(connectionURL), new WebSocketClient(this, latch))
                        .join();
            }

            ws.sendText(webSocketSubscription, true);

            latch.await();

        } catch (Exception e) {
            System.out.println();
            System.out.println("AN EXCEPTION OCCURED :(");
            System.out.println(e);
        }

    }

    @Override
    public boolean closeWebSocketSubscription() {
        if (ws != null) {
            ws.abort();
            isAborted = true;
            return true;
        }
        return false;
    }

    @Override
    public void processData(CharSequence data) {
        String dataString = String.valueOf(data);

        if (dataString != null && dataString.contains("spread")) {
            dataString = dataString.replaceAll("\"", ",");

            String[] stringArr = dataString.split("[\\[  ,]+");
            String currentPair = "";
            double currentBidPrice = 0.0;
            double currentAskPrice = 0.0;
            double currentBidAmount = 0.0;
            double currentAskAmount = 0.0;

            if (stringArr.length > 9 && PAIRS.contains(stringArr[9])) {
                currentPair = stringArr[9];
            }

            for (int i = 0; i < stringArr.length; i++) {
                if (i == 2 && stringArr[i].matches(DECIMAL_NUMBER_REGEX)) {

                    currentBidPrice = new BigDecimal(stringArr[i]).setScale(1, RoundingMode.HALF_UP).doubleValue();
                    currentBidAmount = new BigDecimal(stringArr[5]).setScale(1, RoundingMode.HALF_UP).doubleValue();

                    orderedBids.clear();
                    if (currentPair.equals("ETH/USD")) {
                        ethBids.put(currentBidPrice, currentBidAmount);
                        orderedBids.putAll(ethBids);
                    } else {
                        xbtBids.put(currentBidPrice, currentBidAmount);
                        orderedBids.putAll(xbtBids);
                    }

                } else if (i == 3 && stringArr[i].matches(DECIMAL_NUMBER_REGEX)) {

                    currentAskPrice = new BigDecimal(stringArr[i]).setScale(1, RoundingMode.HALF_UP).doubleValue();
                    currentAskAmount = new BigDecimal(stringArr[6]).setScale(1, RoundingMode.HALF_UP).doubleValue();

                    orderedAsks.clear();
                    if (currentPair.equals("ETH/USD")) {
                        ethAsks.put(currentAskPrice, currentAskAmount);
                        orderedAsks.putAll(ethAsks);
                    } else {
                        xbtAsks.put(currentAskPrice, currentAskAmount);
                        orderedAsks.putAll(xbtAsks);
                    }
                }
            }

            try {
                List<QueryDocumentSnapshot> documents = orderbookRepository.getSpreadDocuments();

                List<Double> bestBidNumbers = new ArrayList<>();
                List<Double> bestAskNumbers = new ArrayList<>();
                boolean existInDb = false;
                for (QueryDocumentSnapshot document : documents) {
                    String pairFromDb = String.valueOf(document.get("pair"));

                    if (pairFromDb.equals(currentPair)) {
                        String bestBidPriceFromDb = String.valueOf(document.get("bestBidPrice"));
                        String bestAskPriceFromDb = String.valueOf(document.get("bestAskPrice"));
                        String bestBidAmountFromDb = String.valueOf(document.get("bestBidAmount"));
                        String bestAskAmountFromDb = String.valueOf(document.get("bestAskAmount"));

                        if (currentBidPrice > Double.parseDouble(bestBidPriceFromDb)) {
                            orderbookRepository.updateSpreadDocument(document.getId(), "bestBidPrice", currentBidPrice, "bestBidAmount", currentBidAmount);
                            bestBidPriceFromDb = String.valueOf(currentBidPrice);
                            bestBidAmountFromDb = String.valueOf(currentBidAmount);
                        }
                        if (currentAskPrice < Double.parseDouble(bestAskPriceFromDb)) {
                            orderbookRepository.updateSpreadDocument(document.getId(), "bestAskPrice", currentAskPrice, "bestAskAmount", currentAskAmount);
                            bestAskPriceFromDb = String.valueOf(currentAskPrice);
                            bestAskAmountFromDb = String.valueOf(currentAskAmount);

                        }
                        bestBidNumbers.add(Double.parseDouble(bestBidPriceFromDb));
                        bestBidNumbers.add(Double.parseDouble(bestBidAmountFromDb));
                        bestAskNumbers.add(Double.parseDouble(bestAskPriceFromDb));
                        bestAskNumbers.add(Double.parseDouble(bestAskAmountFromDb));
                        existInDb = true;
                    }

                }
                if (!existInDb && PAIRS.contains(currentPair)) {
                    Map<String, Object> docData = new HashMap<>();
                    docData.put("pair", currentPair);
                    docData.put("bestBidPrice", currentBidPrice);
                    docData.put("bestBidAmount", currentBidAmount);
                    docData.put("bestAskPrice", currentAskPrice);
                    docData.put("bestAskAmount", currentAskAmount);

                    orderbookRepository.saveSpreadDocument(docData);
                    bestBidNumbers.add(currentBidPrice);
                    bestBidNumbers.add(currentBidAmount);
                    bestAskNumbers.add(currentAskPrice);
                    bestAskNumbers.add(currentAskAmount);
                }
                printResult(orderedBids, orderedAsks, currentPair, bestBidNumbers, bestAskNumbers);
            } catch (IOException | ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    private void printResult(Map<Double, Double> orderedBids, Map<Double, Double> orderedAsks, String currentPair, List<Double> bestBidNumbers, List<Double> bestAskNumbers) {
        System.out.println("<------------------------------------>");
        System.out.println("asks:");
        System.out.print("[ ");
        System.out.println(orderedAsks);
        System.out.println(" ]");
        System.out.print("best bid: ");
        System.out.println(bestBidNumbers);
        System.out.print("best ask: ");
        System.out.println(bestAskNumbers);
        System.out.println("bids:");
        System.out.print("[ ");
        System.out.println(orderedBids);
        System.out.println(" ]");
        System.out.println(currentPair);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        System.out.println(dtf.format(now));
        System.out.println(">-------------------------------------<");
    }
}
