package com.test.eventsparser.controller;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.Test;

import com.test.eventsparser.data.EventItem;

public class InputFileSupplierTest {

	@Test
	public final void givenInputFileSupplier_whenProcesEventsAndSendThemToConsumer_BrokenInput_thenOK() throws Exception {
		//prepare values
		List<EventItem> recorder = new ArrayList<EventItem>(); 
		
		String strBuffer = 		
		"{\"id\":\"scsmbstgra\", " + System.lineSeparator()
		+"{\"id\":\"scsmbstgrb\", \"state\":\"STARTED\", \"timestamp\":1491377495213}" + System.lineSeparator()
		+"345\", \"timestamp\":1491377495217}";
		
		
		InputFileSupplier supplier = 
				new InputFileSupplier("\\tmp\\null") {
					protected Stream<String> createInputStream() throws IOException {
						return Stream.of(strBuffer.split(System.lineSeparator()));
					}			
				};
		//
		
		supplier.addConsumer(
			ei -> {
				recorder.add(ei);
			}
		);
				
		supplier.start();	
		
		
		Thread.sleep(1000L);
		
		assertEquals(1, recorder.size());
		EventItem ei2 = recorder.get(0);
		
		assertNotNull(ei2);
	
		Long ei2Started = ei2.getTimeStamp(EventItem.State.STARTED); 
		Long ei2Finished = ei2.getTimeStamp(EventItem.State.FINISHED); 
		
		
		assertEquals("scsmbstgrb", ei2.getId());
		assertEquals(Long.valueOf(1491377495213L), ei2Started);
		assertNull(ei2Finished);
		assertNull(ei2.getHost());
		assertNull(ei2.getType());
		
		supplier.stop();
		
	}
	
	
	@Test
	public final void givenInputFileSupplier_whenProcesEventsAndSendThemToConsumer_thenOK() throws Exception {
		//prepare values
		List<EventItem> recorder = new ArrayList<EventItem>(); 
		
		String strBuffer = 		
		"{\"id\":\"scsmbstgra\", \"state\":\"STARTED\", \"type\":\"APPLICATION_LOG\", \"host\":\"12345\", \"timestamp\":1491377495212}" + System.lineSeparator()
		+"{\"id\":\"scsmbstgrb\", \"state\":\"STARTED\", \"timestamp\":1491377495213}" + System.lineSeparator()
		+"{\"id\":\"scsmbstgrb\", \"state\":\"FINISHED\", \"timestamp\":1491377495218}" + System.lineSeparator()
		+"{\"id\":\"scsmbstgra\", \"state\":\"FINISHED\", \"type\":\"APPLICATION_LOG\", \"host\":\"12345\", \"timestamp\":1491377495217}";
		
		
		InputFileSupplier supplier = 
				new InputFileSupplier("\\tmp\\null") {
					protected Stream<String> createInputStream() throws IOException {
						return Stream.of(strBuffer.split(System.lineSeparator()));
					}			
				};
		//
		
		supplier.addConsumer(
			ei -> {
				recorder.add(ei);
			}
		);
				
		supplier.start();	
		
		
		Thread.sleep(1000L);
		
		assertEquals(4, recorder.size());
		EventItem ei1 = recorder.get(0);
		EventItem ei2 = recorder.get(1);
		EventItem ei3 = recorder.get(2);
		EventItem ei4 = recorder.get(3);
		
		assertNotNull(ei1);
		assertNotNull(ei2);
		assertNotNull(ei3);
		assertNotNull(ei4);				
	
		Long ei1Started = ei1.getTimeStamp(EventItem.State.STARTED); 
		Long ei1Finished = ei1.getTimeStamp(EventItem.State.FINISHED); 
		Long ei2Started = ei2.getTimeStamp(EventItem.State.STARTED); 
		Long ei2Finished = ei2.getTimeStamp(EventItem.State.FINISHED); 
		Long ei3Started = ei3.getTimeStamp(EventItem.State.STARTED); 
		Long ei3Finished = ei3.getTimeStamp(EventItem.State.FINISHED); 
		Long ei4Started = ei4.getTimeStamp(EventItem.State.STARTED); 
		Long ei4Finished = ei4.getTimeStamp(EventItem.State.FINISHED); 
		
		assertEquals("scsmbstgra", ei1.getId());
		assertEquals(Long.valueOf(1491377495212L), ei1Started);
		assertNull(ei1Finished);
		assertEquals("12345", ei1.getHost());
		assertEquals("APPLICATION_LOG", ei1.getType());
		
		assertEquals("scsmbstgrb", ei2.getId());
		assertEquals(Long.valueOf(1491377495213L), ei2Started);
		assertNull(ei2Finished);
		assertNull(ei2.getHost());
		assertNull(ei2.getType());

		assertEquals("scsmbstgrb", ei3.getId());
		assertEquals(Long.valueOf(1491377495218L), ei3Finished);
		assertNull(ei3Started);
		assertNull(ei3.getHost());
		assertNull(ei3.getType());

		assertEquals("scsmbstgra", ei4.getId());
		assertEquals(Long.valueOf(1491377495217L), ei4Finished);
		assertNull(ei4Started);
		assertEquals("12345", ei4.getHost());
		assertEquals("APPLICATION_LOG", ei4.getType());
		
		supplier.stop();
	}
}
