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

    private final MoviesStore store;

    public MoviesHandler(MoviesStore store) {
        this.store = store;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        String method = exchange.getRequestMethod();

        switch (method) {
            case "GET":
                handleGet(exchange);
                break;
            case "POST":
                handlePost(exchange);
                break;
            default:
                sendJson(
                        exchange,
                        new ErrorResponse("Method Not Allowed", List.of()),
                        405
                );
        }
    }

    private void handleGet(HttpExchange exchange) throws IOException {
        sendJson(exchange, store.getAll(), 200);
    }

    private void handlePost(HttpExchange exchange) throws IOException {

        String contentType =
                exchange.getRequestHeaders().getFirst("Content-Type");

        if (contentType == null ||
                !contentType.contains("application/json")) {

            sendJson(
                    exchange,
                    new ErrorResponse("Unsupported Media Type", List.of()),
                    415
            );
            return;
        }

        String body = new String(exchange.getRequestBody().readAllBytes());

        Movie movie;

        try {
            movie = gson.fromJson(body, Movie.class);
        } catch (Exception e) {
            sendJson(
                    exchange,
                    new ErrorResponse("Некорректный JSON", List.of()),
                    400
            );
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

        Movie saved = store.save(movie);

        sendJson(exchange, saved, 201);
    }

    private List<String> validate(Movie movie) {

        List<String> errors = new ArrayList<>();

        if (movie.getTitle() == null || movie.getTitle().isBlank()) {
            errors.add("название не должно быть пустым");
        }

        if (movie.getTitle() != null &&
                movie.getTitle().length() > 100) {
            errors.add("title не должен быть длиннее 100 символов");
        }

        int maxYear = Year.now().getValue() + 1;

        if (movie.getYear() < 1888 || movie.getYear() > maxYear) {
            errors.add("год должен быть между 1888 и " + maxYear);
        }

        return errors;
    }
}