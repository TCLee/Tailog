package com.tclee.tailog;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Simple implementation of the UNIX-like "tail -n" command.
 * 
 * @author Lee Tze Cheun
 */
public class Tailog {
	
	/** Chunk of bytes to read at any one time. */
	private static final int BUFFER_SIZE = 4096;
	
	/** Default number of lines to tail. */
	private static final int DEFAULT_N_LINES = 10;
	
	/**
	 * Private constructor. This class is not meant to be instantiated.
	 */
	private Tailog() {}			
	
	/**
	 * Returns a string representing the last 10 lines of text from 
	 * given file.
	 * 
	 * @param filename the filename of the file to tail 
	 * @return A string representing the last 10 lines of text from given file.
	 * @throws IOException if an I/O error occurs.
	 */
	public static String tailog(String filename) throws IOException {
		return tailog(filename, DEFAULT_N_LINES);
	}	
	
	/**
	 * Returns a string representing the last N lines of text from 
	 * given file.
	 * 
	 * @param filename the filename of the file to tail
	 * @param nLines number of lines to be returned
	 * @return A string representing the last N lines of text from given file.
	 * @throws IOException if an I/O error occurs. 
	 */
	public static String tailog(String filename, int nLines) 
		throws IOException {
		
		// Convert nLines to a positive value. Does not make sense to be negative!
		// Unfortunately, Java does not have an unsigned int type. :(
		nLines = Math.abs(nLines);		
		
		// Do the tailog operation, if the arguments are valid.
		if (nLines > 0 && filename != null && filename.length() > 0) {			
			return doTailog(filename, nLines);
		}
		
		// By default, returns an empty string to indicate no result.
		return "";
	}
	
	/**
	 * Tailog private helper method that does the actual work. 
	 * 
	 * Basically, we go backwards through the file. Read BUFFER_SIZE 
	 * chunks at a time until we hit the start of the file or have read 
	 * N number of new lines.
	 */
	private static String doTailog(String filename, int nLines) 
		throws IOException {
		
		RandomAccessFile file = null;
		int lineCount = 0;
		long currentPosition = 0;
		long previousPosition = 0;
		long endOfFilePosition = 0;
		byte[] buffer = new byte[BUFFER_SIZE];		
		
		try {
			// Use RandomAccessFile to read the file in reverse.
			file = new RandomAccessFile(filename, "r");
			
			// Get the EOF position of the file.
			endOfFilePosition = file.length();
																	
			// Start at the end of file and read till the start of file.
			previousPosition = endOfFilePosition;
			
			//* Go backwards through the file. Read BUFFER_SIZE chunks at 
			//* a time until we hit the start of the file or have read N
			//* number of new lines.
			do {
				// Move current file position up by BUFFER_SIZE bytes amount.
				currentPosition = previousPosition - BUFFER_SIZE;
				
				// If less than BUFFER_SIZE bytes available to read, then
				// just read all the remaining bytes from the file.
				if (currentPosition < 0) {
					currentPosition = 0;
					
					// Calculate remaining bytes to read.
					int remainingBytesToRead = (int) (previousPosition - currentPosition);
					buffer = new byte[remainingBytesToRead];
				}
												
				// Read the chunk of bytes starting from current file position.
				file.seek(currentPosition);
				file.readFully(buffer);
				
				// If read in last block of the file, we check to see if
				// line at the end of file ends with a new line.
				// If it does not, we must still include it in the line count.
				if (previousPosition == endOfFilePosition && 
					buffer.length > 0 && 
					buffer[buffer.length - 1] != '\n') {
					lineCount++;
				}
				
				// Scan backwards and count number of new lines in the buffer.				
				for (int index = buffer.length - 1; index >= 0; index--) {
					if ((char) buffer[index] == '\n') {
						lineCount++;
						
						// If we have N number of lines we can stop. 
						if (lineCount > nLines) {
							// The position we want is one after the extra new line char.
							currentPosition += (index + 1);
							break;
						}
					}
				}				
				// Move previous file position up to current file position.
				previousPosition = currentPosition;
			} while (lineCount <= nLines && currentPosition > 0);
			 
			// Returns the last N lines from file starting from saved position.
			return getLinesFromFile(file, currentPosition, endOfFilePosition);
		} finally {
			// Close the file and release the resources.
			if (file != null) { 
				file.close(); 
			}			
		}				
	}
	
	/**
	 * Returns all the lines from file starting from given startPosition to 
	 * end of file (EOF).
	 */
	private static String getLinesFromFile(RandomAccessFile file, 
		long startPosition, long endOfFilePosition) 
		throws IOException {
		
		byte[] byteBuffer = new byte[BUFFER_SIZE];
		StringBuffer stringBuffer = new StringBuffer();		
		
		for (long position = startPosition; position < endOfFilePosition; position += byteBuffer.length) {
			// If less than BUFFER_SIZE bytes available to read, then
			// just read all the remainder bytes.
			int remainingBytesToRead = (int) (endOfFilePosition - position);
			if (remainingBytesToRead < BUFFER_SIZE) {
				byteBuffer = new byte[remainingBytesToRead];	
			}
			
			// Read the chunk of bytes starting from current file position.
			file.seek(position);
			file.readFully(byteBuffer);
			stringBuffer.append(new String(byteBuffer));
		}
		return stringBuffer.toString();
	}
}