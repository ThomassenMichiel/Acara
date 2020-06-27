package be.acara.events.service;

import be.acara.events.domain.Category;
import be.acara.events.domain.Event;
import be.acara.events.domain.Event_;
import be.acara.events.domain.User;
import be.acara.events.exceptions.*;
import be.acara.events.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Business logic related to {@link Event}
 */
@Service
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserService userService;

    @Autowired
    public EventServiceImpl(EventRepository repository, UserService userService) {
        this.eventRepository = repository;
        this.userService = userService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Event findById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException(String.format("Event with ID %d not found", id)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<Event> findAll(Map<String, String> params, Pageable pageable) {
        Sort sort = Sort.by("eventDate").ascending();
        if (pageable.getSort().isSorted()) {
            List<Sort.Order> collect = pageable.getSort().get().map(Sort.Order::ignoreCase).collect(Collectors.toList());
            sort = Sort.by(collect);
        }
        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        return eventRepository.findAll(createSpecification(params), pageRequest);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Category> getAllCategories() {
        return Arrays.stream(Category.values()).collect(Collectors.toList());
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteEvent(Long id) {
        if (!eventRepository.existsById(id)) {
            throw new EventNotFoundException(String.format("Event with ID %d not found", id));
        }
        eventRepository.deleteById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Event addEvent(Event event) {
        if (event.getId() != null) {
            throw new IdAlreadyExistsException("A new entity cannot already contain an id");
        }
        if (event.getEventDate().isBefore(LocalDateTime.now())) {
            throw new InvalidDateException("Date has to be in the present or future");
        }
        event.setYoutubeId(event.getYoutubeId());
        return eventRepository.saveAndFlush(event);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Event editEvent(Long id, Event event) {
        if (!event.getId().equals(id)) {
            throw new IdNotFoundException(String.format("Id of event to edit does not match given id. Event id = %d, and given id = %d", event.getId(), id));
        }
        return eventRepository.saveAndFlush(event);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<Event> findEventsByUserId(Long id, Pageable pageable) {
        return eventRepository.findAllByAttendeesContains(userService.findById(id), pageable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Specification<Event> createSpecification(Map<String, String> params) {
        Specification<Event> specification = Specification.where(
                (root, cq, cb) ->
                        cb.greaterThanOrEqualTo(
                                root.get(Event_.eventDate),
                                LocalDateTime.now()
                        )
        );

        if (params.containsKey("startDate")) {
            specification = specification.and(
                    (root, cq, cb) ->
                            cb.greaterThanOrEqualTo(
                                    root.get(Event_.eventDate),
                                    LocalDate.parse(params.get("startDate")).atStartOfDay()
                            )
            );
        }

        if (params.containsKey("location")) { //check if param exists
            specification = specification.and(
                    (root, cq, cb) ->
                            cb.like( //the where operator
                                    cb.lower(root.get(Event_.location)), // database value
                                    String.format("%%%s%%", params.get("location").toLowerCase()))); // the value to test
        }
        if (params.containsKey("minPrice")) {
            specification = specification.and(
                    (root, cq, cb) ->
                            cb.greaterThanOrEqualTo(
                                    root.get(Event_.price),
                                    new BigDecimal(params.get("minPrice"))));
        }
        if (params.containsKey("maxPrice")) {
            specification = specification.and(
                    (root, cq, cb) ->
                            cb.lessThanOrEqualTo(
                                    root.get(Event_.price),
                                    new BigDecimal(params.get("maxPrice"))));
        }
        if (params.containsKey("endDate")) {
            specification = specification.and(
                    (root, cq, cb) ->
                            cb.lessThanOrEqualTo(
                                    root.get(Event_.eventDate),
                                    LocalDate.parse(params.get("endDate")).atStartOfDay()
                            )
            );
        }
        if (params.containsKey("category")) {
            specification = specification.and(
                    (root, cq, cb) ->
                            cb.equal(
                                    root.get(Event_.CATEGORY),
                                    Category.valueOf(params.get("category").toUpperCase())));
        }
        if (params.containsKey("name")) {
            specification = specification.and(
                    ((root, cq, cb) ->
                            cb.like(
                                    cb.lower(root.get(Event_.name)),
                                    String.format("%%%s%%", params.get("name").toLowerCase())))); // the value to test
        }
        return specification;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<Event> findLikedEventsByUserId(Long id, Pageable pageable) {
        return eventRepository.findAllByUsersThatLikeThisEventContains(userService.findById(id), pageable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Event> mostPopularEvents() {
        return eventRepository.findTop4ByAttendeesSize(PageRequest.of(0, 4));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Event> nextAttendingEvents() {
        User user = userService.getCurrentUser();
        return eventRepository.getTop2ByAttendeesContainsAndEventDateAfter(user, PageRequest.of(0, 2));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Event> relatedEvents(Event event) {
        return eventRepository.getRelatedEvents(event, event.getCategory(), PageRequest.of(0, 2));
    }
}
