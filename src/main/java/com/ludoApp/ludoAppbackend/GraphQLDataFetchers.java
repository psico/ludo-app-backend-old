package com.ludoApp.ludoAppbackend;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.*;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import graphql.schema.DataFetcher;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Component
public class GraphQLDataFetchers {

    private FileInputStream serviceAccount;
    FirebaseOptions options;
    Firestore db;

    private CollectionReference usersInfoCollection;
    private CollectionReference matchesCollection;

    GraphQLDataFetchers() throws IOException {
        this.serviceAccount = new FileInputStream("firebase_connection.json");

        this.options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(this.serviceAccount))
                .setDatabaseUrl("https://ludoapp-b612.firebaseio.com")
                .build();

        FirebaseApp.initializeApp(options);
        this.db = FirestoreClient.getFirestore();

        this.usersInfoCollection = this.db.collection("usersInfo");
        this.matchesCollection = this.db.collection("matches");
    }

    public DataFetcher getUserInfoById() {
        return dataFetchingEnvironment -> {
            QuerySnapshot querySnapshot = this.usersInfoCollection.get().get();
            List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();

            String userUid = dataFetchingEnvironment.getArgument("id");

            return documents
                    .stream()
                    .filter(userInfo -> userInfo.get("uid").equals(userUid))
                    .findFirst()
                    .orElse(null)
                    .getData();
        };
    }

    public DataFetcher getUsersInfoFetcher() {
        List<Map<String, Object>> result = new ArrayList<>();

        try {
            QuerySnapshot querySnapshot = this.usersInfoCollection.get().get();
            List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();

            for (QueryDocumentSnapshot document : documents) {
                Map<String, Object> userInfo = new HashMap<>();

                userInfo.put("friends", document.get("friends"));
                userInfo.put("name", document.getString("name"));
                userInfo.put("uid", document.getString("uid"));

                result.add(userInfo);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return dataFetchingEnvironment -> result;
    }

    public DataFetcher getMatchByID() {
        return dataFetchingEnvironment -> {
            QuerySnapshot querySnapshot = this.matchesCollection.get().get();
            List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();

            String matchUid = dataFetchingEnvironment.getArgument("id");

            return documents
                    .stream()
                    .filter(match -> match.getId().equals(matchUid))
                    .findFirst()
                    .orElse(null)
                    .getData();
        };
    }

    public DataFetcher getMatchesFetcher() {
        List<Map<String, Object>> result = new ArrayList<>();

        try {
            QuerySnapshot querySnapshot = this.matchesCollection.get().get();
            List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();

            for (QueryDocumentSnapshot document : documents) {
                Map<String, Object> match = new HashMap<>();

                match.put("game", document.get("game"));
                match.put("gameMoment", document.getString("gameMoment"));
                match.put("players", document.get("players"));
                match.put("uid", document.getString("uid"));

                result.add(match);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return dataFetchingEnvironment -> result;
    }

    public DataFetcher createMatchFetcher() {
        return dataFetchingEnvironment -> {
            Map<String,Object> args = dataFetchingEnvironment.getArgument("match");
            Map<String, Object> docData = new HashMap<>();

            docData.put("docId", args.get("docId"));
            docData.put("uid", args.get("uid"));
            docData.put("gameMoment", args.get("gameMoment"));
            docData.put("game", args.get("game"));
            docData.put("players", args.get("players"));

            DocumentReference docInput = this.matchesCollection.document();
            docData.put("docId", docInput.getId());

            return docData;
        };
    }

    public DataFetcher createComment() {
        return dataFetchingEnvironment -> {
            Map<String,Object> args = dataFetchingEnvironment.getArgument("comment");
            List<Map<String, Object>> listDocData = new ArrayList<>();

            DocumentReference docRefMatch = this.matchesCollection.document(args.get("matchId").toString());
            DocumentSnapshot docMatch = docRefMatch.get().get();

            Optional<QueryDocumentSnapshot> docUser = this.usersInfoCollection
                    .whereEqualTo("uid", args.get("uid").toString())
                    .get().get()
                    .getDocuments().stream()
                    .findFirst();

            Map<String, Object> docData = new HashMap<>();
            docData.put("uid", docUser.get().get("uid"));
            docData.put("name", docUser.get().get("name"));
            docData.put("comment", args.get("comment"));

            Object arrayList = docMatch.get("comments");
            ((ArrayList) arrayList).add(docData);
            listDocData.add(docData);

            docRefMatch.update("comments",arrayList);

            return listDocData;
        };
    }

}
