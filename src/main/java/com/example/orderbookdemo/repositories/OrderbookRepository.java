package com.example.orderbookdemo.repositories;

import com.google.cloud.firestore.QueryDocumentSnapshot;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public interface OrderbookRepository {
    List<QueryDocumentSnapshot> getSpreadDocuments() throws IOException, ExecutionException, InterruptedException;

    void updateSpreadDocument(String documentId, String field1, Double value1, String field2, Double value2) throws IOException;

    void saveSpreadDocument(Map<String, Object> docData) throws IOException;
}
