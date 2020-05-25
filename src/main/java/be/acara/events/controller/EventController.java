package be.acara.events.controller;

import be.acara.events.controller.dto.CategoriesList;
import be.acara.events.controller.dto.CategoryDto;
import be.acara.events.controller.dto.EventDto;
import be.acara.events.controller.dto.EventList;
import be.acara.events.domain.Event;
import be.acara.events.domain.User;
import be.acara.events.service.EventService;
import be.acara.events.service.UserService;
import be.acara.events.service.mapper.CategoryMapper;
import be.acara.events.service.mapper.EventMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;
    private final UserService userService;
    private final EventMapper eventMapper;
    private final CategoryMapper categoryMapper;

    @Autowired
    public EventController(EventService eventService, UserService userService, EventMapper eventMapper, CategoryMapper categoryMapper) {
        this.eventService = eventService;
        this.userService = userService;
        this.eventMapper = eventMapper;
        this.categoryMapper = categoryMapper;
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventDto> findById(@PathVariable("id") Long eventId) {
        Event event = eventService.findById(eventId);
        EventDto eventDto = eventMapper.eventToEventDto(event);
        enrichEventDtosWithLikes(Collections.singleton(eventDto));
        List<EventDto> relatedEvents = eventMapper.eventListToEventDtoList(eventService.relatedEvents(event));
        eventDto.setRelatedEvents(relatedEvents);
        enrichEventDtosWithLikes(relatedEvents);
        return ResponseEntity.ok(eventDto);
    }

    @GetMapping()
    public ResponseEntity<EventList> findAllByAscendingDate(@RequestParam Map<String, String> params, Pageable pageable) {
        Page<Event> eventPage = eventService.findAll(params, pageable);
        EventList eventList = eventMapper.pageToEventList(eventPage);
        enrichEventDtosWithLikes(eventList.getContent());
        setPopularEventsWithLikes(eventList);
        setNextAttendingEventsWithLikes(eventList);
        return ResponseEntity.ok(eventList);
    }

    private void setPopularEventsWithLikes(EventList eventList) {
        List<Event> popularEvents = eventService.mostPopularEvents();
        eventList.setPopularEvents(eventMapper.eventListToEventDtoList(popularEvents));
        enrichEventDtosWithLikes(eventList.getPopularEvents());
    }

    private void setNextAttendingEventsWithLikes(EventList eventList) {
        List<Event> nextEvents = eventService.nextAttendingEvents();
        eventList.setNextAttendingEvents(eventMapper.eventListToEventDtoList(nextEvents));
        enrichEventDtosWithLikes(eventList.getNextAttendingEvents());
    }

    private void enrichEventDtosWithLikes(Collection<EventDto> eventDtos) {
        User user = userService.getCurrentUser();
        if (user != null) {
            Set<Long> ids = user.getLikedEvents().stream().map(Event::getId).collect(Collectors.toSet());
            eventDtos.forEach(eventDto -> eventDto.setLiked(ids.contains(eventDto.getId())));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteEvent(@PathVariable("id") Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/categories")
    public ResponseEntity<CategoriesList> findAllCategories() {
        List<CategoryDto> categoryDtos = eventService.getAllCategories().stream().map(categoryMapper::categoryToCategoryDto).collect(Collectors.toList());
        return ResponseEntity.ok(new CategoriesList(categoryDtos));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventDto> addEvent(@RequestBody @Valid EventDto eventDto) {
        Event event = eventService.addEvent(eventMapper.eventDtoToEvent(eventDto));
        URI uri = URI.create(String.format("/api/events/%d", event.getId()));
        return ResponseEntity.created(uri).body(eventMapper.eventToEventDto(event));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventDto> editEvent(@PathVariable("id") Long id, @RequestBody @Valid EventDto eventDto) {
        Event event = eventService.editEvent(id, eventMapper.eventDtoToEvent(eventDto));
        return ResponseEntity.ok(eventMapper.eventToEventDto(event));
    }

    @GetMapping("/userevents/{id}")
    public ResponseEntity<EventList> findEventsByUserId(@PathVariable("id") Long id, Pageable pageable) {
        return ResponseEntity.ok(eventMapper.pageToEventList(eventService.findEventsByUserId(id, pageable)));
    }

    @GetMapping("/likedevents/{id}")
    public ResponseEntity<EventList> findLikedEventsByUserId(@PathVariable("id") Long id, Pageable pageable) {
        return ResponseEntity.ok(eventMapper.pageToEventList(eventService.findLikedEventsByUserId(id, pageable)));
    }
}
