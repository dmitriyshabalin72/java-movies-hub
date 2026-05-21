package ru.practicum.moviehub.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.practicum.moviehub.api.ErrorResponse;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

public class MoviesHandler extends BaseHttpHandler implements HttpHandler {

    private static final int MAX_TITLE_LENGTH = 100;
    private static final int FIRST_MOVIE_YEAR = 1888;

    private final MoviesStore store;

    public MoviesHandler(MoviesStore store) {
        this.store = store;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        switch (exchange.getRequestMethod()) {
            case "GET" -> handleGet(exchange);
            case "POST" -> handlePost(exchange);
            default -> sendError(
                    exchange,
                    "Method Not Allowed",
                    405
            );
        }
    }

    private void handleGet(HttpExchange exchange) throws IOException {
        sendJson(exchange, store.getAll(), 200);
    }

    private void handlePost(HttpExchange exchange) throws IOException {

        if (!isJsonRequest(exchange)) {
            sendError(exchange, "Unsupported Media Type", 415);
            return;
        }

        Movie movie = parseMovie(exchange);

        if (movie == null) {
            sendError(exchange, "Некорректный JSON", 400);
            return;
        }

        List<String> errors = validate(movie);

        if (!errors.isEmpty()) {
            sendJson(
                    exchange,
                    new ErrorResponse("Ошибка валидации", errors),
                    422
            );
            return;
        }

        sendJson(exchange, store.save(movie), 201);
    }

    private boolean isJsonRequest(HttpExchange exchange) {
        String contentType =
                exchange.getRequestHeaders().getFirst("Content-Type");

        return contentType != null &&
                contentType.contains("application/json");
    }

    private Movie parseMovie(HttpExchange exchange) throws IOException {
        try {
            String body =
                    new String(exchange.getRequestBody().readAllBytes());

            return gson.fromJson(body, Movie.class);

        } catch (Exception e) {
            return null;
        }
    }

    private List<String> validate(Movie movie) {

        List<String> errors = new ArrayList<>();

        validateTitle(movie, errors);
        validateYear(movie, errors);

        return errors;
    }

    private void validateTitle(Movie movie, List<String> errors) {

        String title = movie.getTitle();

        if (title == null || title.isBlank()) {
            errors.add("название не должно быть пустым");
            return;
        }

        if (title.length() > MAX_TITLE_LENGTH) {
            errors.add(
                    "title не должен быть длиннее "
                            + MAX_TITLE_LENGTH
                            + " символов"
            );
        }
    }

    private void validateYear(Movie movie, List<String> errors) {

        int currentMaxYear = Year.now().getValue() + 1;
        int year = movie.getYear();

        if (year < FIRST_MOVIE_YEAR || year > currentMaxYear) {
            errors.add(
                    "год должен быть между "
                            + FIRST_MOVIE_YEAR
                            + " и "
                            + currentMaxYear
            );
        }
    }

    private void sendError(
            HttpExchange exchange,
            String message,
            int statusCode
    ) throws IOException {

        sendJson(
                exchange,
                new ErrorResponse(message, List.of()),
                statusCode
        );
    }
}