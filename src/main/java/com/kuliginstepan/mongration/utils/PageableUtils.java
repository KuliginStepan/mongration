package com.kuliginstepan.mongration.utils;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.experimental.UtilityClass;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@UtilityClass
public class PageableUtils {

    public static <T> void executePageable(Function<Pageable, List<T>> query, Consumer<List<T>> consumer) {
        executePageable(PageRequest.of(0, 30), query, consumer);
    }

    public static <T> void executePageable(Pageable initial, Function<Pageable, List<T>> query,
        Consumer<List<T>> consumer) {
        Pageable pageable = initial;
        List<T> result;
        do {
            result = query.apply(pageable);
            consumer.accept(result);
            pageable = pageable.next();
        } while (!result.isEmpty());
    }
}
