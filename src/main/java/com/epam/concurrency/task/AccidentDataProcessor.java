package com.epam.concurrency.task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epam.data.RoadAccident;

/**
 * Created by Tanmoy on 6/17/2016.
 */
public class AccidentDataProcessor {

    private static final String FILE_PATH_1 = "src/main/resources/DfTRoadSafety_Accidents_2010.csv";
    private static final String FILE_PATH_2 = "src/main/resources/DfTRoadSafety_Accidents_2011.csv";
    private static final String FILE_PATH_3 = "src/main/resources/DfTRoadSafety_Accidents_2012.csv";
    private static final String FILE_PATH_4 = "src/main/resources/DfTRoadSafety_Accidents_2013.csv";

    private static final String OUTPUT_FILE_PATH = "target/DfTRoadSafety_Accidents_consolidated.csv";

    private static final int DATA_PROCESSING_BATCH_SIZE = 10000;
    
    BlockingQueue<List<RoadAccident>> roadAccidentQueue = new ArrayBlockingQueue<>(3);
    BlockingQueue<List<RoadAccidentDetails>> roadAccidentDetailsQueue = new ArrayBlockingQueue<>(3);
    private volatile boolean running = true;

    private AccidentDataReader accidentDataReader = new AccidentDataReader();
    private AccidentDataEnricher accidentDataEnricher = new AccidentDataEnricher();
    private AccidentDataWriter accidentDataWriter = new AccidentDataWriter();
    

    private List<String> fileQueue = new ArrayList<String>();

    private Logger log = LoggerFactory.getLogger(AccidentDataProcessor.class);


    public void init(){
        fileQueue.add(FILE_PATH_1);
        //fileQueue.add(FILE_PATH_2);
        //fileQueue.add(FILE_PATH_3);
        //fileQueue.add(FILE_PATH_4);

        accidentDataWriter.init(OUTPUT_FILE_PATH);
    }

    public void process(){
        for (String accidentDataFile : fileQueue){
            log.info("Starting to process {} file ", accidentDataFile);
            accidentDataReader.init(DATA_PROCESSING_BATCH_SIZE, accidentDataFile);
            try {
				processFile();
			} catch (InterruptedException e) {
				System.out.println("Existing system due to interruption!!");
			}
        }
    }

    private void processFile() throws InterruptedException{
    	ExecutorService executor = Executors.newFixedThreadPool(3);
    	FutureTask<RoadAccident> readerTaskFuture = new FutureTask<RoadAccident>(new Runnable() {
					@Override
					public void run() {
						int batchCount = 1;
						while (!accidentDataReader.hasFinished()) {
							try {
								List<RoadAccident> roadAccidents = accidentDataReader.getNextBatch();
								roadAccidentQueue.put(roadAccidents);
								log.info("Read [{}] records in batch [{}]", roadAccidents.size(), batchCount++);
							} catch (InterruptedException e) {
								running = false;
							}
						}
					}
				}, null);
		executor.execute(readerTaskFuture);
		
		FutureTask<RoadAccidentDetails> enrichTaskFuture = new FutureTask<RoadAccidentDetails>(new Runnable() {
			@Override
			public void run() {
				try {
					while (running) {
						List<RoadAccidentDetails> roadAccidentDetailsList = accidentDataEnricher.enrichRoadAccidentData(roadAccidentQueue
										.take());
						roadAccidentDetailsQueue.put(roadAccidentDetailsList);
						log.info("Enriched records");
					}
				} catch (InterruptedException e) {
					running = false;
				}
				
			}
		}, null);
		executor.execute(enrichTaskFuture);
		
		FutureTask<RoadAccidentDetails> writerTaskFuture = new FutureTask<RoadAccidentDetails>(new Runnable() {
			@Override
			public void run() {
				try {
					while (running) {
						accidentDataWriter.writeAccidentData(roadAccidentDetailsQueue.take());
						log.info("Written records");
					}
				} catch (InterruptedException e) {
					running = false;
				}
			}
		}, null);
		executor.execute(writerTaskFuture);
		
		try {
			if(readerTaskFuture.get() != null || !running){
				readerTaskFuture.cancel(true);
				enrichTaskFuture.cancel(true);
				writerTaskFuture.cancel(true);
				executor.shutdownNow();
			}
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
    }
    
    
    public static void main(String[] args) {
        AccidentDataProcessor dataProcessor = new AccidentDataProcessor();
        long start = System.currentTimeMillis();
        System.out.println("Start : " + start/1000);
        dataProcessor.init();
        dataProcessor.process();
        long end = System.currentTimeMillis();
        System.out.println("end : " + end/1000);
        System.out.println("Process finished in s : " + (end-start)/1000);
    }
}
