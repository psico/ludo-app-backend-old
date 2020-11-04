package com.ludoApp.ludoAppbackend;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URL;

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
        return RuntimeWiring.newRuntimeWiring()
                .type(TypeRuntimeWiring.newTypeWiring("Query")
//                        .dataFetcher("bookById", graphQLDataFetchers.getBookByIdDataFetcher())
                        .dataFetcher("userInfoById", graphQLDataFetchers.getUserInfoById())
                        .dataFetcher("usersInfo", graphQLDataFetchers.getUsersInfoFetcher())
                        .dataFetcher("matchByID", graphQLDataFetchers.getMatchByID())
                        .dataFetcher("matches", graphQLDataFetchers.getMatchesFetcher())
//                        .dataFetcher("createMatch", graphQLDataFetchers.createMatchFetcher())
                )
                .type(TypeRuntimeWiring.newTypeWiring("Mutation")
                        .dataFetcher("createMatch", graphQLDataFetchers.createMatchFetcher())
                )
//                .type(TypeRuntimeWiring.newTypeWiring("Book")
//                        .dataFetcher("author", graphQLDataFetchers.getAuthorDataFetcher())
//                        // This line is new: we need to register the additional DataFetcher
//                        .dataFetcher("pageCount", graphQLDataFetchers.getPageCountDataFetcher())
//                )
                .build();
    }
}
