# EventsParser
The Events Parser java application in version 1.0 allows for reading events from external file and keeps them in the HSQLDB database.
This is Spring Boot command-line application.

The external file should be provided and an application parameter <br>
    Example: 
       java -jar build/libs/EventsParser-0.0.1-SNAPSHOT.jar example.log

The HSQLDB database and its parameters should be provided in file application.properties 
that is located at src/main/resources directory


in order to use application please
- make sure you have java installed on your machine
- download this project
- build it using command: ./gradlew build
- create jar file using command: ./gradlew jar 

Know issues of the 1.0 version:
1. It does not allow for processing large datasets as it was not tested on such configuration yet,
2. There are more JUnit tests required to fully test all of the corner cases of the implementation,
3. Smaller issues commented as TODO in the source code.
