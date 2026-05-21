package ru.practicum.moviehub.http;

import ru.practicum.moviehub.store.MoviesStore;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class MoviesServer {

    private final HttpServer server;

    public MoviesServer() {

        try {
            server = HttpServer.create(
                    new InetSocketAddress(8080),
                    0
            );

            MoviesStore store = new MoviesStore();

            server.createContext(
                    "/movies",
                    new MoviesHandler(store)
            );

            server.setExecutor(Executors.newFixedThreadPool(4));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop(0);
    }
}