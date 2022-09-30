package com.example.orderbookdemo.repositories;

import com.example.orderbookdemo.config.DatabaseConfig;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Repository
public class OrderbookRepositoryImpl implements OrderbookRepository {

    @Override
    public List<QueryDocumentSnapshot> getSpreadDocuments() throws IOException, ExecutionException, InterruptedException {
        Firestore db = DatabaseConfig.getFirestore();
        ApiFuture<QuerySnapshot> query = db.collection("Spread").get();
        QuerySnapshot querySnapshot = query.get();
        return querySnapshot.getDocuments();
    }

    @Override
    public void updateSpreadDocument(String documentId, String field1, Double value1, String field2, Double value2) throws IOException {
        Firestore db = DatabaseConfig.getFirestore();
        db.collection("Spread").document(documentId)
                .update(field1, value1, field2, value2);
    }

    @Override
    public void saveSpreadDocument(Map<String, Object> docData) throws IOException {
        Firestore db = DatabaseConfig.getFirestore();
        db.collection("Spread").document(String.valueOf(UUID.randomUUID())).set(docData);
    }
}
