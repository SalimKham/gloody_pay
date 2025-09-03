package com.zsoft.gloody.gloodypayroll.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CommonUtils {
    public static <T> List<Set<T>> splitSet(Set<T> originalSet, int chunkSize) {
        List<T> list = new ArrayList<>(originalSet);
        return IntStream.range(0, (list.size() + chunkSize - 1) / chunkSize)
                .mapToObj(i -> list.subList(
                        i * chunkSize,
                        Math.min(list.size(), (i + 1) * chunkSize)))
                .map(HashSet::new)
                .collect(Collectors.toList());
    }
}
