package com.epam.concurrency.task;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Tanmoy on 6/17/2016.
 */
public class AccidentDataProcessor {
	
	private Logger log = LoggerFactory.getLogger(AccidentDataProcessor.class);
	
	protected AccidentDataReader accidentDataReader;
    protected AccidentDataEnricher accidentDataEnricher;
    protected AccidentDataWriter accidentDataWriter;
    
    private static final String FILE_PATH_1 = "src/main/resources/DfTRoadSafety_Accidents_2010.csv";
    private static final String FILE_PATH_2 = "src/main/resources/DfTRoadSafety_Accidents_2011.csv";
    private static final String FILE_PATH_3 = "src/main/resources/DfTRoadSafety_Accidents_2012.csv";
    private static final String FILE_PATH_4 = "src/main/resources/DfTRoadSafety_Accidents_2013.csv";

    private static final String OUTPUT_FILE_PATH = "target/DfTRoadSafety_Accidents_consolidated.csv";

    private static final int DATA_PROCESSING_BATCH_SIZE = 10000;
    
    public AccidentDataProcessor(){}
    
    public AccidentDataProcessor(AccidentDataReader accidentDataReader, AccidentDataEnricher accidentDataEnricher, 
    		AccidentDataWriter accidentDataWriter){
    	this.accidentDataReader = accidentDataReader;
    	this.accidentDataEnricher = accidentDataEnricher;
    	this.accidentDataWriter = accidentDataWriter;
    }

	public void init(){
        accidentDataWriter.init(OUTPUT_FILE_PATH);
    }

	public void process(List<String> fileQueue) throws InterruptedException {
		long start = System.currentTimeMillis();
		System.out.println("Start : " + start / 1000);
		init();
		for (String accidentDataFile : fileQueue) {
			log.info("Starting to process {} file ", accidentDataFile);
			accidentDataReader.init(DATA_PROCESSING_BATCH_SIZE, accidentDataFile);
			try {
				new AccidentalDataParallelProcessor().processFile(accidentDataReader, accidentDataEnricher, accidentDataWriter);
			} catch (InterruptedException e) {
				log.debug("System exiting due to interruption!");
				throw e;
			}
		}
		long end = System.currentTimeMillis();
		System.out.println("end : " + end / 1000);
		System.out.println("Process finished in s : " + (end - start) / 1000);
	}

	public static void main(String[] args) throws InterruptedException {
		List<String> fileQueue = new ArrayList<>();
		fileQueue.add(FILE_PATH_2);
        fileQueue.add(FILE_PATH_2);
        fileQueue.add(FILE_PATH_3);
        fileQueue.add(FILE_PATH_4);
        AccidentDataProcessor dataProcessor = new AccidentDataProcessor();
        dataProcessor.process(fileQueue);
    }
}
