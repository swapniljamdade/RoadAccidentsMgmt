package com.epam.concurrency;

import static com.epam.custom.hamcrect.FileContainsRecords.containsRecords;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.epam.concurrency.task.AccidentDataEnricher;
import com.epam.concurrency.task.AccidentDataProcessor;
import com.epam.concurrency.task.AccidentDataReader;
import com.epam.concurrency.task.AccidentDataWriter;

public class FileWriteIntegrationTest {

	private static List<String> fileQueue;
	private static AccidentDataProcessor accidentDataProcessor;
    private static final String FILE_PATH_2 = "src/main/resources/DfTRoadSafety_Accidents_2011.csv";
    private static final String OUTPUT_FILE = "target/DfTRoadSafety_Accidents_consolidated.csv";
    
    @BeforeClass
    public static void setUp(){
		fileQueue = new ArrayList<>();
		accidentDataProcessor = new AccidentDataProcessor(new AccidentDataReader(), new AccidentDataEnricher(),
				new AccidentDataWriter());
    }
    
    @AfterClass
    public static void tearDown(){
    	fileQueue = null;
    	accidentDataProcessor = null;
    }

    @Test
    public void shouldReadTheFileParallelyAndWriteToAnotherFile() throws Exception{
    	fileQueue.add(FILE_PATH_2);
    	accidentDataProcessor.process(fileQueue);
    	assertThat(OUTPUT_FILE, containsRecords(OUTPUT_FILE));
    }
}