package ru.practicum.moviehub.store;

import ru.practicum.moviehub.model.Movie;

import java.util.*;

public class MoviesStore {

    private final Map<Integer, Movie> movies = new HashMap<>();
    private int idCounter = 1;

    public List<Movie> getAll() {
        return new ArrayList<>(movies.values());
    }

    public Movie save(Movie movie) {
        movie.setId(idCounter++);
        movies.put(movie.getId(), movie);
        return movie;
    }
}