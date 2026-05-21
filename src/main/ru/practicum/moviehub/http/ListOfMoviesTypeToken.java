package ru.practicum.moviehub.http;

import com.google.gson.reflect.TypeToken;
import ru.practicum.moviehub.model.Movie;

import java.lang.reflect.Type;
import java.util.List;

public class ListOfMoviesTypeToken {

    public static final Type LIST_OF_MOVIES =
            new TypeToken<List<Movie>>() {}.getType();
}