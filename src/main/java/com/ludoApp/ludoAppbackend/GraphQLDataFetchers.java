package com.ludoApp.ludoAppbackend;

import com.google.common.collect.ImmutableMap;
import graphql.schema.DataFetcher;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class GraphQLDataFetchers {

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
                    "name", "Teste 6",
                    "friends", Arrays.asList(
                            ImmutableMap.of(
                                    "name", "teste 5",
                                    "uid", "39K3LW8i3BU7e9yatuoSfFuAkAc2"
                            )
                    )),
            ImmutableMap.of("uid", "wAoGIkyZ1L50Kba9CAlf",
                    "name", "Desenvolvedor JG",
                    "friends", Arrays.asList(
                            ImmutableMap.of(
                                    "name", "teste 5",
                                    "uid", "39K3LW8i3BU7e9yatuoSfFuAkAc2"
                            )
                    ))
    );

    private static List<Map<String, String>> friendsList = Arrays.asList(
            ImmutableMap.of("uid", "0IhNFZFa7QMwBY6yZT8l24L1AX32",
                    "name", "Desenvolvedor JG"),
            ImmutableMap.of("uid", "MHIMaAOuYZY9RCmQxP4CckIcpQi2",
                    "name", "MailparaJG")
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
            Map<String,String> book = dataFetchingEnvironment.getSource();
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
            Map<String,String> book = dataFetchingEnvironment.getSource();
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
        return dataFetchingEnvironment -> {
            Map<String,String> userInfo = dataFetchingEnvironment.getSource();
//            String friendsId = userInfo.get("friendUid");
            List<Map<String, Object>> friendsId = Arrays.asList(ImmutableMap.of(
                    "uid", "0IhNFZFa7QMwBY6yZT8l24L1AX32",
                    "name", "Desenvolvedor JG"
            ), ImmutableMap.of(
                    "uid", "0IhNFZFa7QMwBY6yZT8l24L1AX32",
                    "name", "Desenvolvedor JG"
            ));
            return friendsList;
        };
    }

}
