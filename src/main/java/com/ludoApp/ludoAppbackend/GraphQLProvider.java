package com.ludoApp.ludoAppbackend;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.database.*;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Component
public class GraphQLProvider {

    @Autowired
    GraphQLDataFetchers graphQLDataFetchers;

    private GraphQL graphQL;

    @Bean
    public GraphQL graphQL() {
        return graphQL;
    }

    @PostConstruct
    public void init() throws IOException {
        URL url = Resources.getResource("schema.graphqls");
        String sdl = Resources.toString(url, Charsets.UTF_8);
        GraphQLSchema graphQLSchema = buildSchema(sdl);
        this.graphQL = GraphQL.newGraphQL(graphQLSchema).build();
    }

    private GraphQLSchema buildSchema(String sdl) {
        TypeDefinitionRegistry typeRegistry = new SchemaParser().parse(sdl);
        RuntimeWiring runtimeWiring = buildWiring();
        SchemaGenerator schemaGenerator = new SchemaGenerator();
        return schemaGenerator.makeExecutableSchema(typeRegistry, runtimeWiring);
    }

    private RuntimeWiring buildWiring() {
        FirebaseOptions options;
        try (FileInputStream serviceAccount = new FileInputStream("firebase_connection.json")) {

            options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://ludoapp-b612.firebaseio.com")
                    .build();

            FirebaseApp.initializeApp(options);
            Firestore db = FirestoreClient.getFirestore();


            ApiFuture<QuerySnapshot> query = db.collection("usersInfo").get();

            QuerySnapshot querySnapshot = query.get();
            List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();
            for (QueryDocumentSnapshot document : documents) {
                System.out.println(document);
                System.out.println("UID: " + document.getId());
                System.out.println("Name: " + document.getString("name"));
//                if (document.contains("middle")) {
//                    System.out.println("Middle: " + document.getString("middle"));
//                }
//                System.out.println("Last: " + document.getString("last"));
//                System.out.println("Born: " + document.getLong("born"));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }


//        FirebaseOptions options = new FirebaseOptions.Builder()
//                .setCredentials(GoogleCredentials.getApplicationDefault())
//                .setDatabaseUrl("https://ludoapp-b612.firebaseio.com/")
//                .build();
//
//        FirebaseApp.initializeApp(options);


        return RuntimeWiring.newRuntimeWiring()
                .type(TypeRuntimeWiring.newTypeWiring("Query")
                        .dataFetcher("bookById", graphQLDataFetchers.getBookByIdDataFetcher())
                        .dataFetcher("userInfoId", graphQLDataFetchers.getUserInfoById())
                )
                .type(TypeRuntimeWiring.newTypeWiring("Book")
                        .dataFetcher("author", graphQLDataFetchers.getAuthorDataFetcher())
                        // This line is new: we need to register the additional DataFetcher
                        .dataFetcher("pageCount", graphQLDataFetchers.getPageCountDataFetcher())
                )
                .type(TypeRuntimeWiring.newTypeWiring("UserInfo")
                        .dataFetcher("friends", graphQLDataFetchers.getFriendsDataFetcher())
                )
                .build();
    }
}
