package com.tclee.tailog;

import java.io.IOException;

/**
 * Tailog command line application.
 * 
 * Usage: java -jar tailog.jar [-n number] file
 *
 * -n number : output the last 'number' of lines from file
 *
 * Example: java -jar tailog.jar -n 10 somefile.txt
 * 
 * 
 * @author Lee Tze Cheun
 */
public class TailogConsole {
	
	/**
	 * Private constructor. This class is not meant to be instantiated.
	 */
	private TailogConsole() {}
	
	public static void main(String[] args) {
		// tailog -n <number of lines> <filename>
		if (args.length == 3) {
			if (args[0].equals("-n")) {				
				try {
					int numberOfLines = Integer.parseInt(args[1]);					
					String filename = args[2];
					String result = Tailog.tailog(filename, numberOfLines);
					System.out.println(result);					
				} catch (NumberFormatException e) {				
					System.err.println("Number Error: " + e.getLocalizedMessage());
				} catch (IOException e) {
					System.err.println("I/O Error: " + e.getLocalizedMessage());
				}
				return;
			}
		}
		
		// Default Case:
		// Print the command's help text, if arguments don't match.
		printUsage();
	}
	
	/**
	 * Prints the command help text to the console.
	 */
	private static void printUsage() {
		System.out.println();
		System.out.println("Usage: java -jar tailog.jar [-n number] file");
		System.out.println();		
		System.out.println("-n number : output the last 'number' of lines from file");
		System.out.println();
		System.out.println("Example: java -jar tailog.jar -n 10 somefile.txt");
		System.out.println();
	}
}