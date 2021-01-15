package com.test.eventsparser.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.test.eventsparser.data.EventItem;
import com.test.eventsparser.data.EventItemBuilder;


/**
 * The events supplier from an input file.
 * 
 * @author egatel
 *
 */
public class InputFileSupplier implements EventsSupplier {
	//An input stream of data 
	private Stream<String> 			 inputStream;
	//a Path to the input file
	private Path                     path;	
	//Consumer of events
	private Consumer<EventItem> 	 consumer;	
	
	private ExecutorService 		 executor;
	private List<Future<Boolean>>    workerResults;		
	
	//concurrency protection 
	private ReadWriteLock rwLock = new ReentrantReadWriteLock();
	private Lock readLock        = rwLock.readLock();
	private Lock writeLock       = rwLock.writeLock(); 
	
	private boolean isStarted 	 = false;
	
	
	static Logger logger = LoggerFactory.getLogger(InputFileSupplier.class);
	
	public InputFileSupplier(String aPath) throws IOException {
		Objects.requireNonNull(aPath, "Missing a path to the input file!");
		
		this.path = Paths.get(aPath);
		
		this.executor  = Executors.newFixedThreadPool(1);
		this.workerResults = new LinkedList<Future<Boolean>>(); 
	}

	@Override
	public void addConsumer(Consumer<EventItem> consumer) {
		this.consumer = consumer;
	}
	
	protected Stream<String> createInputStream() throws IOException {
		return Files.lines(path);
	}

	@Override
	public boolean start() {		
		writeLock.lock();
		try {
			if (isStarted) {
				//there is no need to start it again
				return false;
			}
			
			if (consumer == null) {
				logger.error("Missing consumer !");
			}
			
			if (logger.isInfoEnabled()) {
				logger.info(
						String.format("InputFileSupplier starts parsing lines from an input file %s" , this.path.toString())
				);
			}
			
			
			inputStream = createInputStream();
			
			//TODO In the future here more treads can be started
			Future<Boolean> workerResult = 
					executor.submit(
							() -> { return doWork(); }
					);
			
			workerResults.add(workerResult);
			
			isStarted = true;
			
		} catch(IOException ex) {
			logger.error("Fail during opening input file! ", ex);
			return false;
		} finally {
			writeLock.unlock();
		}
		
		return true;
	}
	
	private boolean doWork() {
		JSONEventItemBuilder eventItemBulider = new JSONEventItemBuilder();
					
		inputStream.forEach(
			line -> {
				
				try { 
					eventItemBulider.add(new JSONObject(line));

					EventItem ei = eventItemBulider.build();
					consumer.accept(ei);
				} catch(RuntimeException ex) {
					logger.warn(String.format("An issue during parsing input line: %s", line));
				} catch(Exception ex) {
					logger.warn(String.format("An issue during parsing input line: %s", line));
				}
			}
		);
		
		if (logger.isInfoEnabled()) {
			logger.info("InputFileSupplier has completed parsing lines from an input file.");
		}
		
		
		
		return true;
	}

	@Override
	public void stop() {
		
		writeLock.lock();
		try {
			if (!isStarted) {
				//do not stop it again
				return;
			}
			
			if (logger.isDebugEnabled()) {
				logger.debug("Producer stops.");
			}

			if (executor != null) {
				executor.shutdown();
			}
			
			if (inputStream != null) {
				inputStream.close();
			}
			
			inputStream = null;

			workerResults.clear();
			
			isStarted = false;
		} finally {
			writeLock.unlock();
		}
		
	}

	@Override
	public boolean hasNext() {		

		readLock.lock();
		try {
			/*
			 causes 
			 java.lang.IllegalStateException: stream has already been operated upon or closed
			 
			if (inputStream != null) {
				return inputStream.iterator().hasNext();
			}
			*/
			long workers = 
					workerResults.stream().filter(wr -> !wr.isDone()).count();
			return workers > 0;
			
			//return false;
			
		} finally {
			readLock.unlock();
		}
		
		//return true;
	}
	
	//A customization that allows to read from JSON
	private class JSONEventItemBuilder extends EventItemBuilder {
		public EventItemBuilder add(JSONObject jobj) {
			Objects.requireNonNull(jobj, "Expected JSON Object");
			
			add(EventItem.ID, jobj.get(EventItem.ID));
			add(EventItem.STATE, jobj.opt(EventItem.STATE));
			add(EventItem.TIMESTAMP, jobj.opt(EventItem.TIMESTAMP));
			add(EventItem.TYPE, jobj.opt(EventItem.TYPE));
			add(EventItem.HOST, jobj.opt(EventItem.HOST));

			return this;
		}
	}
	
}
