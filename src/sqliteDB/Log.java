package sqliteDB;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Log {

	public static void initializeLog(Logger logger) throws IOException, SecurityException{
		FileHandler fileHandler;
			fileHandler = new FileHandler("Logs/LogTest.log");
			logger.addHandler(fileHandler);
			SimpleFormatter formatter = new SimpleFormatter();  
	        fileHandler.setFormatter(formatter); 
	        
	        logger.info("New Log started");  
	}
}
