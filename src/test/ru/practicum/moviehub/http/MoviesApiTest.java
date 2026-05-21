package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.practicum.moviehub.model.Movie;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MoviesApiTest {

    private static final String MOVIES_URL =
            "http://localhost:8080/movies";

    private static MoviesServer server;

    private final Gson gson = new Gson();

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(2))
            .build();

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
    void getMovies_whenEmpty_returnsEmptyArray() throws Exception {

        HttpResponse<String> response = sendGetMovies();

        assertEquals(200, response.statusCode());

        List<Movie> movies = gson.fromJson(
                response.body(),
                ListOfMoviesTypeToken.LIST_OF_MOVIES
        );

        assertTrue(movies.isEmpty());
    }

    @Test
    void postMovie_success_returnsCreatedMovie() throws Exception {

        Movie newMovie =
                new Movie(0, "Interstellar", 2014);

        HttpResponse<String> response = postMovie(newMovie);

        assertEquals(201, response.statusCode());

        Movie savedMovie =
                gson.fromJson(response.body(), Movie.class);

        assertTrue(savedMovie.getId() > 0);
        assertEquals("Interstellar", savedMovie.getTitle());
        assertEquals(2014, savedMovie.getYear());
    }

    private HttpResponse<String> sendGetMovies()
            throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(MOVIES_URL))
                .GET()
                .build();

        return client.send(
                request,
                HttpResponse.BodyHandlers.ofString(
                        StandardCharsets.UTF_8
                )
        );
    }

    private HttpResponse<String> postMovie(Movie movie)
            throws IOException, InterruptedException {

        String json = gson.toJson(movie);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(MOVIES_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        return client.send(
                request,
                HttpResponse.BodyHandlers.ofString(
                        StandardCharsets.UTF_8
                )
        );
    }
}