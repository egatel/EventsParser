package com.test.eventsparser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.test.eventsparser.controller.DBConsumer;
import com.test.eventsparser.controller.EventsConsumer;
import com.test.eventsparser.controller.EventsParserController;
import com.test.eventsparser.controller.EventsSupplier;
import com.test.eventsparser.controller.InputFileSupplier;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A main class of the application for events processing.
 * The implementation assumes that events are taken form the input file and result of the processing 
 * is stored in the database
 * 
 * The assumption is that events are kept in separate lines as JSON objects, as in example below:
 * 
 * {"id":"scsmbstgra", "state":"STARTED", "type":"APPLICATION_LOG", "host":"12345", "timestamp":1491377495212}
 * {"id":"scsmbstgrb", "state":"STARTED", "timestamp":1491377495213}
 * {"id":"scsmbstgrc", "state":"FINISHED", "timestamp":1491377495218}
 * {"id":"scsmbstgra", "state":"FINISHED", "type":"APPLICATION_LOG", "host":"12345", "timestamp":1491377495217}
 * {"id":"scsmbstgrc", "state":"STARTED", "timestamp":1491377495210}
 * {"id":"scsmbstgrb", "state":"FINISHED", "timestamp":1491377495216}
 * ... 
 * 
 * @author egatel
 *
 */

@SpringBootApplication
public class EventsParserApplication implements CommandLineRunner {
	
	private static Logger logger = LoggerFactory.getLogger(EventsParserApplication.class);
	
	@Autowired
	private DBConsumer dbConsumer;
	
	private String createHelpMessage() {
		StringBuffer sb = new StringBuffer();
		
		sb.append("Missing input file!").append(System.lineSeparator());
		sb.append("Example of usage: java -jar EventLogger-all.jar ../example.log").append(System.lineSeparator());
	
		return sb.toString();
	}

	protected EventsParserController createController(String... args) throws EventsParserException {
		if (args == null || args.length == 0 || args[0] == null) {
			String helpMessage = createHelpMessage();
			
			System.out.println(helpMessage);
			
			throw new IllegalArgumentException("Missing input file!");
		}
		
		return new EventsParserController() {

			@Override
			protected EventsSupplier createEventsSupplier() throws EventsParserException {
				try {
					return new InputFileSupplier(args[0]);
				} catch (IOException ex) {
					logger.error("An issue while creating supplier ", ex);
					throw new EventsParserException("An issue while creating supplier ", ex);
				}
			}

			@Override
			protected EventsConsumer createEventsConsumer() throws EventsParserException {
				return dbConsumer;
			}
			
		};
	}
	
    @Override
    public void run(String... args) {
    	if(logger.isInfoEnabled()) {
    		logger.info("The Processing is starting.");
    	}
    	
    	EventsParserController controller = null;
    	
    	try {
    		controller = createController(args);
    		
    		if(logger.isInfoEnabled()) {
    			logger.info("The Processing has been started.");
    		}
    		
    		controller.start();
    		
    		//wait for Producer to process the whole work
    		while(!controller.isDone()) {
    			
    			Thread.sleep(100);//wait a bit 
    			
    			if(Thread.currentThread().isInterrupted()) {
    				break;
    			}
    		}
    		
    	} catch (InterruptedException ex) { 
    		logger.error("Thread interrupted while processing! ", ex);
    	} catch (RuntimeException ex) { 
    		logger.error("Falal runtime error while processing! ", ex);
    	} catch (EventsParserException ex) { 
    		logger.error("Falal error while processing! ", ex);    		
    	} finally {
        	if(logger.isInfoEnabled()) {
        		logger.info("The Processing is closing.");
        	}
    		
    		try {
    			if (controller != null) {
    				controller.stop();
    			}
    		} catch (EventsParserException ex) {
    			logger.error("Error while closing Controller", ex);
    		}
    		
    		controller = null;
    	}
    }	
		
    /**
     * Stars application
     * @param args Arguments required to sent up the processing. 
     */
	public static void main(String[] args) {
		if (logger.isInfoEnabled()) {
			logger.info("Application starts.");
		}

		SpringApplication.run(EventsParserApplication.class, args);
	}

}
