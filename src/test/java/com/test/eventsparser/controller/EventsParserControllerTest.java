package com.test.eventsparser.controller;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.junit.Test;

import com.test.eventsparser.EventsParserException;
import com.test.eventsparser.data.EventItem;
import com.test.eventsparser.model.Event;

public class EventsParserControllerTest {
	EventItem ei11 = new EventItem("ID1", EventItem.State.FINISHED, 1491377495212L, "TYPE_TEST", "HOST_TEST");
	EventItem ei12 = new EventItem("ID1", EventItem.State.STARTED, 1491377495211L, "TYPE_TEST", "HOST_TEST");
	
	EventItem ei21 = new EventItem("ID2", EventItem.State.STARTED, 1491377495215L, null, "HOST_TEST");
	EventItem ei22 = new EventItem("ID2", EventItem.State.FINISHED, 1491377495220L, null, "HOST_TEST");
	
	EventItem ei3 = new EventItem("ID3", EventItem.State.STARTED, 1491377495217L, "TYPE_TEST", null);
	
	EventItem ei4 = new EventItem("ID5", EventItem.State.FINISHED, 1491377495219L, "TYPE_TEST", null);

	@Test
	public final void givenEventsParserController_whenReceivingUnPairedEvents_thenOK() throws Exception {
		List<EventItem> input = Arrays.asList(ei21, ei3, ei22, ei4);
		//
		EventsSupplierForTest esft = new EventsSupplierForTest(input);
		EventsConsumerForTest ecft = new EventsConsumerForTest();
		
		EventsParserController epc = new EventsParserController() {

			@Override
			protected EventsSupplier createEventsSupplier() throws EventsParserException {
				return esft;
			}

			@Override
			protected EventsConsumer createEventsConsumer() throws EventsParserException {
				return ecft;
			}
		};
		//
		
		assertTrue(epc.start());

		assertTrue(epc.isDone());
		
		epc.stop();
		
		ArrayList<Event> recorded = ecft.getRecordedEvents();
		
		assertEquals(1, recorded.size());
		
		Event ev2 = recorded.get(0);

		assertEquals("ID2", ev2.getId());
		assertEquals(Long.valueOf(5L), ev2.getDuration());
		assertTrue(ev2.getAlert());
		assertEquals("HOST_TEST", ev2.getHost());
		assertNull(ev2.getType());		
	}	

	@Test
	public final void givenEventsParserController_whenReceivingPairedEvents_thenOK() throws Exception {
		List<EventItem> input = Arrays.asList(ei21, ei12, ei11, ei22);
		//
		EventsSupplierForTest esft = new EventsSupplierForTest(input);
		EventsConsumerForTest ecft = new EventsConsumerForTest();
		
		EventsParserController epc = new EventsParserController() {

			@Override
			protected EventsSupplier createEventsSupplier() throws EventsParserException {
				return esft;
			}

			@Override
			protected EventsConsumer createEventsConsumer() throws EventsParserException {
				return ecft;
			}
		};
		//
		
		assertTrue(epc.start());

		assertTrue(epc.isDone());
		
		epc.stop();
		
		ArrayList<Event> recorded = ecft.getRecordedEvents();
		
		assertEquals(2, recorded.size());
		
		Event ev1 = recorded.get(0);
		Event ev2 = recorded.get(1);
		
		assertEquals("ID1", ev1.getId());
		assertEquals(Long.valueOf(1L), ev1.getDuration());
		assertFalse(ev1.getAlert());
		assertEquals("HOST_TEST", ev1.getHost());
		assertEquals("TYPE_TEST", ev1.getType());
		
		assertEquals("ID2", ev2.getId());
		assertEquals(Long.valueOf(5L), ev2.getDuration());
		assertTrue(ev2.getAlert());
		assertEquals("HOST_TEST", ev2.getHost());
		assertNull(ev2.getType());
	}
	
	private class EventsSupplierForTest implements EventsSupplier {
		private Consumer<EventItem> consumer;
		private List<EventItem> eventItemsToSend;

		EventsSupplierForTest(List<EventItem> eventItemsToSend) {
			this.eventItemsToSend = eventItemsToSend;
		}
		
		@Override
		public void addConsumer(Consumer<EventItem> consumer) {
			this.consumer = consumer;
		}
		
		@Override
		public boolean start() throws EventsParserException {
			
			eventItemsToSend.forEach(ei -> consumer.accept(ei));
			
			return true;
		}

		@Override
		public boolean hasNext() {
			return false;
		}

		@Override
		public void stop() throws EventsParserException {
		}
		
	}
	
	private class EventsConsumerForTest implements EventsConsumer {
		private ArrayList<Event> eventItemsReceived = new ArrayList<Event>();

		@Override
		public void accept(Event ei) {
			eventItemsReceived.add(ei);
		}

		public ArrayList<Event> getRecordedEvents() {
			return eventItemsReceived;
		}
		
		@Override
		public boolean start() throws EventsParserException {
			return true;
		}

		@Override
		public void stop() throws EventsParserException {
		}
		
	}
}
