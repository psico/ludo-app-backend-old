package com.ludoApp.ludoAppbackend;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Component
public class GraphQLDataFetchers {

    private FileInputStream serviceAccount;
    FirebaseOptions options;
    Firestore db;

    GraphQLDataFetchers() throws IOException {
        this.serviceAccount = new FileInputStream("firebase_connection.json");

        this.options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(this.serviceAccount))
                .setDatabaseUrl("https://ludoapp-b612.firebaseio.com")
                .build();

        FirebaseApp.initializeApp(options);
        this.db = FirestoreClient.getFirestore();
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

    private static List<Map<String, String>> friendsList;
    private static List<Map<String, String>> matchesList;

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
            String userUid = dataFetchingEnvironment.getArgument("id");
            return userInfo
                    .stream()
                    .filter(userInfo -> userInfo.get("uid").equals(userUid))
                    .findFirst()
                    .orElse(null);
        };
    }

    public DataFetcher getFriendsDataFetcher() {
        try {
            ApiFuture<QuerySnapshot> query = this.db.collection("usersInfo").get();

            QuerySnapshot querySnapshot = query.get();
            List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();
            for (QueryDocumentSnapshot document : documents) {
                System.out.println(document.get("friends"));
                friendsList = Arrays.asList(
                        new ImmutableMap[]{(
                                ImmutableMap.of(
                                        "uid", document.getId(),
                                        "name", document.getString("name"),
                                        "friends", document.get("friends")
                                )
                        )}
                );
//                if (document.contains("middle")) {
//                    System.out.println("Middle: " + document.getString("middle"));
//                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }


        return dataFetchingEnvironment -> {
//            Map<String,String> userInfo = dataFetchingEnvironment.getSource();
            return friendsList;
        };
    }

    public DataFetcher getMatchesFetcher() {
        try {
            ApiFuture<QuerySnapshot> query = this.db.collection("matches").get();

            QuerySnapshot querySnapshot = query.get();
            List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();
            for (QueryDocumentSnapshot document : documents) {
                System.out.println(document.get("matches"));
                matchesList = Arrays.asList(
                        new ImmutableMap[]{(
                                ImmutableMap.of(
                                        "uid", document.getId(),
                                        "gameMoment", document.getString("gameMoment")
                                )
                        )}
                );
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }


        return dataFetchingEnvironment -> {
            return matchesList;
        };
    }

}
