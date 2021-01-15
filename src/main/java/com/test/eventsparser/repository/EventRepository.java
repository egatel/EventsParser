package com.test.eventsparser.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.test.eventsparser.model.Event;

@Repository
public interface EventRepository extends CrudRepository<Event, String> {

}
