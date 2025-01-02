package com.zsoft.gloody.gloodypayroll.utils;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SectionListUtils {
    private static final Map<String, List<String>> ORDER_MAPPER = new HashMap<>();

    static {
        ORDER_MAPPER.put("created_at", Collections.singletonList("createdAt"));
    }

    private SectionListUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static Pageable getPageable(Pageable pageable) {
        if (pageable == null) {
            return Pageable.unpaged();
        }
        Sort sort = getSort(pageable.getSort());
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }

    public static Sort getSort(Sort sort) {
        for (Sort.Order order : sort) {
            List<String> properties;
            if (ORDER_MAPPER.containsKey(order.getProperty())) {
                properties = ORDER_MAPPER.get(order.getProperty());
            } else {
                properties = Collections.singletonList(order.getProperty());
            }
            sort = Sort.by(order.getDirection(), properties.toArray(new String[0]));
        }
        List<Sort.Order> orders = sort.get().map(Sort.Order::nullsLast).collect(Collectors.toList());
        return Sort.by(orders);
    }
}
