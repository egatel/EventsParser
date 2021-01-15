package com.test.eventsparser.controller;

import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.test.eventsparser.EventsParserException;
import com.test.eventsparser.data.EventItem;
import com.test.eventsparser.model.Event;

/**
 * The Events processing controller that allows for reading event items from in input source, called supplier
 * and delivers the final data to the output storage, called consumer.
 * 
 * @author egatel
 *
 */
public abstract class EventsParserController {
	
	private EventsSupplier supplier = null;
	private EventsConsumer consumer = null;

	//A storage of temporary results. 
	//Every event should be started and finished before sending it to the Consumer
	private HashMap<String, EventItem> eventsForMatching = new HashMap<String, EventItem>();
	
	//concurrency protection 
	private ReadWriteLock rwLock = new ReentrantReadWriteLock();
	private Lock readLock        = rwLock.readLock();
	private Lock writeLock       = rwLock.writeLock(); 
	
	private boolean isStarted 	 = false;


	static Logger logger = LoggerFactory.getLogger(EventsParserController.class);
	
	public EventsParserController() {		
	}

	protected abstract EventsSupplier createEventsSupplier() throws EventsParserException;
	protected abstract EventsConsumer createEventsConsumer() throws EventsParserException;
	
	
	public boolean start() throws EventsParserException {
				
		writeLock.lock();
		try {
			if (isStarted) {
				if (logger.isDebugEnabled()) {
					logger.debug("Attempt to start controller that has been already started.");
				}
				
				return false;
			}
			
	    	if(logger.isInfoEnabled()) {
	    		logger.info("The controller processing is starting.");
	    	}

			supplier = createEventsSupplier();
			consumer = createEventsConsumer();

			supplier.addConsumer(
					ei -> acceptEvent(ei)
				);
			
			supplier.start();
    		consumer.start();
    		
    		
    		if(logger.isInfoEnabled()) {
    			logger.info("The controller processing has been started.");
    		}
    		
    		isStarted = true;
		} catch (EventsParserException ex) {
			logger.info("Fatal error detected ", ex);
			
			localStop();
			
			throw ex;			
		} catch (Exception ex) {
			logger.info("Fatal error detected ", ex);
			
			localStop();
			
			throw new EventsParserException("Fatal error detected ", ex);
		} finally {
			writeLock.unlock();
		}
		
		return true;
	}
	
	private void localStop() {
		
		try {
			if (supplier != null) {
				supplier.stop();
			}
		} catch (EventsParserException ex) {
			logger.error("Error while closing Supplier :", ex);
		}
		
		try {
			if (consumer != null ) {
				consumer.stop();
			}
		} catch (EventsParserException ex) {
			logger.error("Error while closing Consumer :", ex);
		}
		
	}
	
	public void stop() throws EventsParserException {
		
		writeLock.lock();
		try {
			if (!isStarted) {
				if (logger.isDebugEnabled()) {
					logger.debug("Attempt to stop controller that has been already stopped.");
				}
				return;
			}
			
        	if(logger.isInfoEnabled()) {
        		logger.info("The controller processing is closing.");
        	}
    		
        	localStop();

    		supplier = null;
    		consumer = null;
    		
    		isStarted = false;
		} finally {
			writeLock.unlock();
		}
	}
	
	public boolean isDone() {
		readLock.lock();
		try {
			if (supplier != null) {
				return !supplier.hasNext();
			}
		} finally {
			readLock.unlock();
		}
		
		return true;
	}
	
	//Business logic 
	/**
	 * Does a mapping between EventItem and Event that will be saved to Database.
	 * 
	 * @param ei An EventItem that holds data
	 * @return Event that is ready to be send to database 
	 */
	private Event createEvent(EventItem ei) {
		
		Boolean alert   = false;
		Long duration   = null;
		Long startTime  = ei.getTimeStamp(EventItem.State.STARTED);
		Long finishTime = ei.getTimeStamp(EventItem.State.FINISHED);
		
		if (startTime != null && finishTime != null) {
			duration = finishTime - startTime;
		}
		
		if (duration != null) {
			//TODO That should be configured from the file 
			alert = duration > 4 ? true : false;
		}
		
		return 
				new Event(
						ei.getId(),
						duration,
						ei.getType(), 
						ei.getHost(), 
						alert);
	}
	
	
	private EventItem compareTimes(EventItem.State state, EventItem orgEI, EventItem newEI) {
		Long orgTime = orgEI.getTimeStamp(state);
		Long newTime = newEI.getTimeStamp(state);
		
		if( newTime != null) {
			if (orgTime != null && !orgTime.equals(newTime)) {
				logger.warn("New %s event log detected ");//TODO
			}
			
			orgEI.setTimeStamp(state, newTime);
		}
		
		return orgEI;
	}

	
	private EventItem mergeEventItems(EventItem orgEI, EventItem newEI) {
		assert orgEI.getId().equals(newEI.getId());//TODO ??
		
		orgEI = compareTimes(EventItem.State.STARTED, orgEI, newEI);
		orgEI = compareTimes(EventItem.State.FINISHED, orgEI, newEI);

		//TODO: what if Type changes between event logs?
		orgEI.setType(newEI.getType());
		
		//TODO: what if Host changes between event logs?
		orgEI.setHost(newEI.getHost()); 
	
		return orgEI;
	}	
	
	private void acceptEvent(EventItem ei) {
		if (ei == null) {
			logger.warn("Null EventItem has been detected ! Request ignored.");
			return;
		}
		
		try {			
			
			if (eventsForMatching.containsKey(ei.getId())) {
				
				ei = mergeEventItems(eventsForMatching.get(ei.getId()), ei);
				
				Long startTime  = ei.getTimeStamp(EventItem.State.STARTED);
				Long finishTime = ei.getTimeStamp(EventItem.State.FINISHED);
				
				//check id EventItem is complete and can be send. If not, add it back to the queue
				if (startTime != null && finishTime != null) {
					if (consumer == null) {
						logger.error("There is no Consumer defined hence event gets lost");
					}
					
					//TODO: Warning is consumer will throw an exception the event will be lost but will be logged below
					consumer.accept(createEvent(ei));
					
					return;	
				} else {
					logger.warn(String.format("Unable to save event log %s", ei));
				}
			} 
			
			//TODO: warning adding new items can cause memory overload. 
			//The cleaner of unfinished Events should be implemented 
			eventsForMatching.put(ei.getId(), ei); 
			
		} catch (RuntimeException ex) {
			logger.error(String.format("Unable to parse event %s", ei), ex);
		} catch (Exception ex) {
			logger.error(String.format("Unable to parse event %s", ei), ex);
		}
	}
}
