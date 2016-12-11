package com.epam.custom.hamcrect;

import java.io.FileReader;
import java.io.Reader;
import java.util.Iterator;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class FileContainsRecords {

	public static Matcher<String> containsRecords(final String filePath) {
		return new TypeSafeMatcher<String>() {

			@Override
			public void describeTo(final Description description) {
				 description.appendText("expected result from containsRecords(): ")
				 .appendValue(description);
			}

			@SuppressWarnings("resource")
			public boolean matchesSafely(final String filePath) {
				int count = 0;
				  try{
			            Reader reader = new FileReader(filePath);
						Iterator<CSVRecord> recordIterator = new CSVParser(reader, CSVFormat.EXCEL.withHeader()).iterator();
						while(count <= 10000 || recordIterator.hasNext()){
							recordIterator.next();
							count += 1;
						}
						if(count >= 1){
							return true;
						}
			        }catch (Exception e){
			            throw new RuntimeException("Failed to prepare file iterator for  file : " + filePath, e);
			        }
				return false;
			}


			public void describeMismatchSafely(final String filePath,
					final Description mismatchDescription) {
				 mismatchDescription.appendText("was ").appendValue(
						 filePath);
			}

		};
	}
}
