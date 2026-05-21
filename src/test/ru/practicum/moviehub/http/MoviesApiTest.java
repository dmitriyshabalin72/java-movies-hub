package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import ru.practicum.moviehub.http.MoviesServer;
import ru.practicum.moviehub.model.Movie;
import org.junit.jupiter.api.*;

import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MoviesApiTest {

    private static MoviesServer server;
    private final Gson gson = new Gson();

    @BeforeAll
    static void start() {
        server = new MoviesServer();
        server.start();
    }

    @AfterAll
    static void stop() {
        server.stop();
    }

    @Test
    @Order(1)
    void getMovies_whenEmpty_returnsEmptyArray() throws Exception {

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/movies"))
                .GET()
                .build();

        HttpResponse<String> resp = client.send(req,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, resp.statusCode());

        List<Movie> movies = gson.fromJson(
                resp.body(),
                ru.practicum.moviehub.http.ListOfMoviesTypeToken.LIST_OF_MOVIES
        );

        assertTrue(movies.isEmpty());
    }

    @Test
    @Order(2)
    void postMovie_success_returnsCreatedMovie() throws Exception {

        HttpClient client = HttpClient.newHttpClient();

        String json = "{\"title\":\"Interstellar\",\"year\":2014}";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/movies"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> resp = client.send(req,
                HttpResponse.BodyHandlers.ofString());

        assertEquals(201, resp.statusCode());

        Movie movie = gson.fromJson(resp.body(), Movie.class);

        assertTrue(movie.getId() > 0);
    }
}