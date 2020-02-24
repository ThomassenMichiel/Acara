package be.acara.events.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class EventTest {
    private Event event;
    private LocalDateTime dateTime;
    
    @BeforeEach
    void setUp() {
        dateTime = LocalDateTime.now().plusDays(2);
        event = Event.builder()
                .id(1L)
                .description("This is a description")
                .eventDate(dateTime)
                .name("Event!")
                .image(new byte[0])
                .build();
    }
    
    @Test
    void eventCreator() {
        event = new Event();
        assertThat(event).isNotNull();
    }
    
    @Test
    void getId() {
        assertThat(event.getId()).isEqualTo(1L);
    }
    
    @Test
    void getEventDate() {
        assertThat(event.getEventDate()).isEqualTo(dateTime);
    }
    
    @Test
    void getName() {
        assertThat(event.getName()).isEqualTo("Event!");
    }
    
    @Test
    void getDescription() {
        assertThat(event.getDescription()).isEqualTo("This is a description");
    }
    
    @Test
    void getImage() {
        assertThat(event.getImage()).isEqualTo(new byte[0]);
    }
    
    @Test
    void setId() {
        Long id = 10L;
        event.setId(id);
        assertThat(event.getId()).isEqualTo(id);
    }
    
    @Test
    void setEventDate() {
        LocalDateTime date = LocalDateTime.now().plusDays(7);
        event.setEventDate(date);
        assertThat(event.getEventDate()).isEqualTo(date);
    }
    
    @Test
    void setName() {
        String name = "New name";
        event.setName(name);
        assertThat(event.getName()).isEqualTo(name);
    }
    
    @Test
    void setDescription() {
        String description = "Description";
        event.setDescription(description);
        assertThat(event.getDescription()).isEqualTo(description);
    }
    
    @Test
    void setImage() {
        byte[] image = new byte[10];
        event.setImage(image);
        assertThat(event.getImage()).isEqualTo(image);
    }
}
