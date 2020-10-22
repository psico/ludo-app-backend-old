package com.ludoApp.ludoAppbackend;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.common.collect.ImmutableMap;
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

    GraphQLDataFetchers() throws IOException, ExecutionException, InterruptedException {
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

    private static List<Map<String, String>> books = Arrays.asList(
            ImmutableMap.of("id", "book-1",
                    "name", "Harry Potter and the Philosopher's Stone",
                    "pageCount", "223",
                    "authorId", "author-1"),
            ImmutableMap.of("id", "book-2",
                    "name", "Moby Dick",
                    "pageCount", "635",
                    "authorId", "author-2"),
            ImmutableMap.of("id", "book-3",
                    "name", "Interview with the vampire",
                    "pageCount", "371",
                    "authorId", "author-3")
    );

    private static List<Map<String, String>> authors = Arrays.asList(
            ImmutableMap.of("id", "author-1",
                    "firstName", "Joanne",
                    "lastName", "Rowling"),
            ImmutableMap.of("id", "author-2",
                    "firstName", "Herman",
                    "lastName", "Melville"),
            ImmutableMap.of("id", "author-3",
                    "firstName", "Anne",
                    "lastName", "Rice")
    );

    private static List<Map<String, Object>> userInfo = Arrays.asList(
            ImmutableMap.of("uid", "9gwyi7B6PHBr0RDuSHwS",
                    "name", "Teste333 6",
                    "friends", Arrays.asList(
                            ImmutableMap.of(
                                    "name", "teste 5",
                                    "uid", "39K3LW8i3BU7e9yatuoSfFuAkAc2"
                            )
                    )),
            ImmutableMap.of("uid", "wAoGIkyZ1L50Kba9CAlf",
                    "name", "Desenvolvedoras JG",
                    "friends", Arrays.asList(
                            ImmutableMap.of(
                                    "name", "teste 5",
                                    "uid", "39K3LW8i3BU7e9yatuoSfFuAkAc2"
                            )
                    ))
    );

    public DataFetcher getBookByIdDataFetcher() {
        return dataFetchingEnvironment -> {
            String bookId = dataFetchingEnvironment.getArgument("id");
            return books
                    .stream()
                    .filter(book -> book.get("id").equals(bookId))
                    .findFirst()
                    .orElse(null);
        };
    }

    public DataFetcher getAuthorDataFetcher() {
        return dataFetchingEnvironment -> {
            Map<String, String> book = dataFetchingEnvironment.getSource();
            String authorId = book.get("authorId");
            return authors
                    .stream()
                    .filter(author -> author.get("id").equals(authorId))
                    .findFirst()
                    .orElse(null);
        };
    }

    // In the GraphQLDataFetchers class
    // Implement the DataFetcher
    public DataFetcher getPageCountDataFetcher() {
        return dataFetchingEnvironment -> {
            Map<String, String> book = dataFetchingEnvironment.getSource();
            return book.get("totalPages");
        };
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
            Map<String, Object> args = dataFetchingEnvironment.getArguments();
            System.out.println(args.values());

            Map<String, Object> docData = new HashMap<>();
            docData.put("gameMoment", dataFetchingEnvironment.getArgument("gameMoment"));
            docData.put("uid", dataFetchingEnvironment.getArgument("uid"));
//            docData.put("country", "USA");
//            docData.put("regions", Arrays.asList("west_coast", "socal"));

            this.matchesCollection.document().set(docData);


            return books
                    .stream()
                    .filter(book -> book.get("id").equals("book-1"))
                    .findFirst()
                    .orElse(null);
        };
    }

}
