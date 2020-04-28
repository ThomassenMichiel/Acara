package be.acara.events.controller.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

public class OrderList extends PageImpl<CreateOrderDto> {

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public OrderList(@JsonProperty("content") List<CreateOrderDto> content,
                     @JsonProperty("number") int page,
                     @JsonProperty("size") int size,
                     @JsonProperty("totalElements") long total,
                     @JsonProperty("pageable") JsonNode pageable,
                     @JsonProperty("last") boolean last,
                     @JsonProperty("totalPages") int totalPages,
                     @JsonProperty("sort") JsonNode sort,
                     @JsonProperty("first") boolean first,
                     @JsonProperty("numberOfElements") int numberOfElements,
                     @JsonProperty("empty") boolean empty) {
        super(content, PageRequest.of(page, size), total);
    }

    public OrderList(List<CreateOrderDto> content) {
        super(content);
    }

    public OrderList(List<CreateOrderDto> content, Pageable pageable, long total) {
        super(content, pageable, total);
    }
}