package be.acara.events.repository;

import be.acara.events.domain.Event;
import be.acara.events.domain.User;
import be.acara.events.testutil.EventUtil;
import be.acara.events.testutil.UserUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

import javax.persistence.EntityManager;
import javax.sql.DataSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class EventRepositoryTest {
    @Autowired
    private DataSource dataSource;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private EventRepository eventRepository;
    
    @Test
    void injectedComponentsAreNotNull(){
        assertThat(dataSource).isNotNull();
        assertThat(jdbcTemplate).isNotNull();
        assertThat(entityManager).isNotNull();
        assertThat(eventRepository).isNotNull();
    }
    
    @Test
    void getRelatedEvents() {
        Event event = EventUtil.firstEvent();
        List<Event> answer = eventRepository.getRelatedEvents(event, event.getCategory(), PageRequest.of(0, 2));
        
        assertThat(answer).extracting(Event::getCategory).containsOnly(event.getCategory());
        assertThat(answer).hasSize(2);
    }
    
    @Test
    void getTop2ByAttendeesContainsAndEventDateAfter() {
        User user = UserUtil.firstUser();
        List<Event> answer = eventRepository.getTop2ByAttendeesContainsAndEventDateAfter(user, PageRequest.of(0, 2));
    
        assertThat(answer).hasSize(0);
    }
}
