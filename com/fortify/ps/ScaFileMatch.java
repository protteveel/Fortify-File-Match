package com.fortify.ps;

import java.util.Iterator;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;


public class ScaFileMatch {

	// Fortify SCA properties file
	private static final String SCA_PROPERTIES_FILE = "fortify-sca.properties"; 
	// Fortify SCA properties file
	private static final String SCA_PROPERTIES_EXTENSION_FILE = "ScaFileMatch.properties";
	// Maximum file counter length
	private static final int MAX_FILE_COUNTER_LEN = 6;
	// Maximum line counter length
	private static final int MAX_LINE_COUNTER_LEN = 7;
	// Root location of the project
	private String projectRootDir = null;
	// Location of the Fortify SCA properties file
	private String fortifyScaPropertiesFilePath = null;
	// Location of the Fortify SCA match properties file
	private String fortifyScaMatchPropertiesFilePath = null;
	// Fortify SCA file extension mapping
	private HashMap <String, String> fortifyScaFileExtensionMapping = null;
	// Counts of supported file extensions
	private HashMap <String, fileStatisticCounter> countConfiguredFileExtensions = null;
	// Counts of non Java related file extensions
	private HashMap <String, Integer> countNonConfiguredFileExtensions = null;
	// Output text file
	private PrintWriter outputFile = null;
	
	ScaFileMatch( String projectRootDirectory )
	{
		// Get the file separator
		String fileSeparator = System.getProperty("file.separator");
		// Does the project root ends with a file separator?
		if ( projectRootDirectory.endsWith( fileSeparator )) {
			// Remove the terminating file separator
			projectRootDirectory = projectRootDirectory.substring( 0, projectRootDirectory.length() - 1 );
		}
		// Save the project root directory
		projectRootDir = projectRootDirectory;
	}
	
	private class fileStatisticCounter {
		// Keep track of the number of files
		private int fileCounter = 0;
		// Keep track of the number of text lines in the files
		private int lineCounter = 0;
		// Default constructor
		fileStatisticCounter( int initFileCounter, int initLineCounter ) {
			// Initialize the file counter
			setFileCounter( initFileCounter );
			// Initialize the line counter
			setLineCounter( initLineCounter );
		}
		// Set the file counter
		public void setFileCounter( int newFileCounter ) {
			fileCounter = newFileCounter;
		}
		// Set the line counter
		public void setLineCounter( int newLineCounter ) {
			lineCounter = newLineCounter;
		}
		// Retrieve the file counter
		public int getFileCounter() {
			return fileCounter;
		}
		// Retrieve the line counter
		public int getLineCounter() {
			return lineCounter;
		}
		// Add a number of files
		public int addFileCounter( int addFiles ) {
			return fileCounter += addFiles;
		}
		// Add a number of lines
		public int addLineCounter( int addLines ) {
			return lineCounter += addLines;
		}
		// Get the string representation of the file counter
		public String getFileCounterStr( int len ) {
			Integer theInt = new Integer( fileCounter );
			String fileCounterStr = theInt.toString();
			while ( fileCounterStr.length() < len ) {
				fileCounterStr = " " + fileCounterStr;
			}
			return fileCounterStr;
		}
		// Get the string representation of the line counter
		public String getLineCounterStr( int len ) {
			Integer theInt = new Integer( lineCounter );
			String lineCounterStr = theInt.toString();
			while ( lineCounterStr.length() < len ) {
				lineCounterStr = " " + lineCounterStr;
			}
			return lineCounterStr;
		}
	}
	
	/**
	 * findFortifyScaPropertiesFilePath
	 * 
	 * Looks for the Fortify SCA properties file. It assumes the path to the SCA binary 
	 *   is in the current PATH. From here the file is at:
	 *   - Windows: ..\Core\config\fortify-sca.properties
	 *   - Unix:    ../Core/config/fortify-sca.properties
	 * 
	 * @return Whether or not the location of the Fortify SCA properties file was found.
	 */
	private boolean findFortifyScaPropertiesFilePath()
	{
		// Set the default result
		boolean locationFound = false;
		// Get the environment variable PATH
		String path = System.getenv("PATH");
		// Get the path separator
		String pathSeparator = System.getProperty("path.separator");
		// Get the file separator
		String fileSeparator = System.getProperty("file.separator");
		// Get the individual paths
		String[] paths = path.split( pathSeparator );
		// Loop through the list of paths in search for "Fortify"
		for (int index = 0; index < paths.length; index++ ) {
			// Is the word "Fortify" in the path?
			if ( paths[index].toLowerCase().indexOf("fortify") != -1 ) {
				String location = paths[index];
				// Does not it end with a file separator?
				if ( !location.endsWith( fileSeparator )) {
					// Add the file separator
					location += fileSeparator;
				}
				// Set the possible path to the SCA configuration file
				location += ".." + fileSeparator + "Core" + fileSeparator + "config" + fileSeparator + SCA_PROPERTIES_FILE;
				// Does this file exists?
				File handle = new File ( location );
				if (( handle != null  ) && ( handle.exists())) {
					// Copy the file location
					fortifyScaPropertiesFilePath = location;
					// Set the location of being found
					locationFound = true;
					// Terminate the loop
					index = paths.length;
				}
			}
		}
		// Return the result
		return locationFound;
	}
	
	/**
	 * findFortifyScaMatchPropertiesFilePath
	 * 
	 * Looks for the Fortify SCA match properties file. It assumes it is the current location
	 *   or in the current PATH.
	 * 
	 * @return Whether or not the location of the Fortify SCA match properties file was found.
	 */
	private boolean findFortifyScaMatchPropertiesFilePath()
	{
		// Set the default result
		boolean locationFound = false;
		// Get the environment variable PATH
		String path = System.getenv("PATH");
		// Get the path separator
		String pathSeparator = System.getProperty("path.separator");
		// Get the file separator
		String fileSeparator = System.getProperty("file.separator");
		// Add the current directory to the beginning of the path
		path = "." + pathSeparator + path;
		// Get the individual paths
		String[] paths = path.split( pathSeparator );
		// Loop through the list of paths in search for "Fortify"
		for (int index = 0; index < paths.length; index++ ) {
			// Get the location
			String location = paths[index];
			// Does not it end with a file separator?
			if ( !location.endsWith( fileSeparator )) {
				// Add the file separator
				location += fileSeparator;
			}
			// Set the possible path to the SCA configuration file
			location += SCA_PROPERTIES_EXTENSION_FILE;
			// Does this file exists?
			File handle = new File ( location );
			if (( handle != null  ) && ( handle.exists())) {
				// Copy the file location
				fortifyScaMatchPropertiesFilePath = location;
				// Set the location of being found
				locationFound = true;
				// Terminate the loop
				index = paths.length;
			}
		}
		// Return the result
		return locationFound;
	}
	
	private boolean readScaFileExtensionMapping( String filePath, HashMap <String, String> extensionMap )
	{
		// Set default return value
		boolean readMappings = false;
		// There is a file name?
		if (( filePath != null ) && ( filePath.length() > 0 )) {
			// Create the file handle
			File handle = new File( filePath );
			// File could be opened?
			if ( handle != null) {
				try {
					// Open the file for reading
					BufferedReader reader = new BufferedReader( new FileReader( handle ));
					// Create a string to hold the lines read
					String line = null;
					// Read the entire file
					while (( line = reader.readLine()) != null ) {
						// Do we have some text?
						if ( line.length() > 0 ) {
							// Is it a Fortify SCA file extension mapping?
							if ( line.toLowerCase().indexOf("com.fortify.sca.fileextensions") != -1 ) {
								// Split the string based on the "=" character
								String parts[] = line.split("=");
								// Do we have two parts?
								if (( parts != null ) && ( parts.length > 1 )) {
									// First part is the file extension
									String firstPart = parts[0].trim();
									// Split it up based on the "." character
									String fileExtensions[] = firstPart.split("\\.");
									// Could the string be split?
									if ( fileExtensions.length > 4 ) {
										// The last string is the file extension
										String fileExtension = fileExtensions[fileExtensions.length-1].toLowerCase();
										// The second part is the SCA interpretation
										String interpretation = parts[1].trim();
										// Do we have a mapping?
										if (( fileExtension != null ) && ( fileExtension.length() > 0 ) && 
											( interpretation != null ) && ( interpretation.length() > 0 )) {
											// Add the mapping to the Fortify SCA file extension mapping
											extensionMap.put( fileExtension, interpretation );
											// Save the file extension mapping could be read
											readMappings = true;
										}
									}
								}
							}
						}
					}
				}
				catch ( FileNotFoundException e ) {
					System.out.println( "Could open file: \"" + filePath + "\"" );
				}
				catch ( IOException e ) {
					System.out.println( "Could not read file: \"" + filePath + "\"" );
				}
			}
		}
		// Return the result
		return readMappings;
		
	}
	
	private boolean seedScaFileExtensionMapping()
	{
		// Set the initial result
		boolean seeded = true;
		// Create the hash map to hold the mapping
		fortifyScaFileExtensionMapping = new HashMap <String, String>();
		// And seed the mapping
		fortifyScaFileExtensionMapping.put( "asp", "ASP" );
		fortifyScaFileExtensionMapping.put( "bas", "VB6" );
		fortifyScaFileExtensionMapping.put( "cfc", "CFML" );
		fortifyScaFileExtensionMapping.put( "cfm", "CFML" );
		fortifyScaFileExtensionMapping.put( "cfml", "CFML" );
		fortifyScaFileExtensionMapping.put( "cls", "VB6" );
		fortifyScaFileExtensionMapping.put( "config", "XML" );
		fortifyScaFileExtensionMapping.put( "ctl", "VB6" );
		fortifyScaFileExtensionMapping.put( "ctp", "PHP" );
		fortifyScaFileExtensionMapping.put( "frm", "VB6" );
		fortifyScaFileExtensionMapping.put( "htm", "HTML" );
		fortifyScaFileExtensionMapping.put( "html", "HTML" );
		fortifyScaFileExtensionMapping.put( "ini", "JAVA_PROPERTIES" );
		fortifyScaFileExtensionMapping.put( "java", "JAVA" );
		fortifyScaFileExtensionMapping.put( "js", "JAVASCRIPT" );
		fortifyScaFileExtensionMapping.put( "js", "JAVASCRIPT" );
		fortifyScaFileExtensionMapping.put( "jsp", "JSP" );
		fortifyScaFileExtensionMapping.put( "jspx", "JSP" );
		fortifyScaFileExtensionMapping.put( "php", "PHP" );
		fortifyScaFileExtensionMapping.put( "pkb", "PLSQL" );
		fortifyScaFileExtensionMapping.put( "pkh", "PLSQL" );
		fortifyScaFileExtensionMapping.put( "pks", "PLSQL" );
		fortifyScaFileExtensionMapping.put( "properties", "JAVA_PROPERTIES" );
		fortifyScaFileExtensionMapping.put( "py", "PYTHON" );
		fortifyScaFileExtensionMapping.put( "sql", "PLSQL" );
		fortifyScaFileExtensionMapping.put( "vbscript", "VBSCRIPT" );
		fortifyScaFileExtensionMapping.put( "vbs", "VB6" );
		fortifyScaFileExtensionMapping.put( "wsdd", "XML" );
		fortifyScaFileExtensionMapping.put( "xmi", "XML" );
		fortifyScaFileExtensionMapping.put( "xml", "XML" );
		fortifyScaFileExtensionMapping.put( "xsd", "XML" );
		// Return the result
		return seeded;
	}
	
	private boolean createOutputTextFile()
	{
		// Set the initial result
		boolean created = false;
		// Establish the file name
		String fileName = this.getClass().getName();
		// Split the name by the "." character
		String[] parts = fileName.split("\\.");
		if (( parts != null ) && ( parts.length > 0 )) {
			// The last part is the actual class name
			fileName = parts[ parts.length - 1 ] + ".txt"; 
			// Try to create the file
			try {
				// Create the file writer
				FileWriter fileWriter = new FileWriter( fileName );
				// Create the print writer
				outputFile = new PrintWriter(fileWriter);
				// The file was successfully created
				created = true;
			}
			catch ( IOException e) {
				// Report the error
				System.out.println( "Could not create the file \"" + fileName + "\" (maybe you have no write access)." );
			}
		}
		// Return the result
		return created;
		
	}
	public boolean init() {
		// Set the initial result
		boolean initialized = true;
		// Initialize the counted supported file extensions
		countConfiguredFileExtensions = new HashMap <String, fileStatisticCounter> ();
		// Initialize the counted non Java related file extensions
		countNonConfiguredFileExtensions = new HashMap <String, Integer> ();
		// See if the location of the Fortify SCA properties file can be found
		if ( findFortifyScaPropertiesFilePath()) {
			// Create the hash map to hold the mapping
			fortifyScaFileExtensionMapping = new HashMap <String, String>();
			// Read the list of supported Fortify SCA file extension mapping
			initialized = readScaFileExtensionMapping( fortifyScaPropertiesFilePath, fortifyScaFileExtensionMapping );
		}
		else
		{
			// Seed the list of supported Fortify SCA file extension mapping
			initialized = seedScaFileExtensionMapping();
			// Report it to the user
			System.out.println("Could not find \"" + SCA_PROPERTIES_FILE + "\" (use internal list).");
		}
		// Initialization was successful?
		if ( initialized ) {
			// See if the location of the Fortify SCA properties file can be found
			if ( findFortifyScaMatchPropertiesFilePath()) {
				// Read the list of supported Fortify SCA match file extension mapping
				readScaFileExtensionMapping( fortifyScaMatchPropertiesFilePath, fortifyScaFileExtensionMapping );
			}
			else {
				// Report it to the user
				System.out.println("Could not find \"" + SCA_PROPERTIES_EXTENSION_FILE + "\" (use internal list).");
			}
			// Create the output text file
			initialized = createOutputTextFile();
		}
		//return the result
		return initialized;
	}
	
	private int getlineCount( String directory, String fileName ) {
		// Set the default return value
		int lineCount = 0;
		// Get the file separator
		String fileSeparator = System.getProperty("file.separator");
		// Compose the file path
		String path = directory + fileSeparator + fileName;
		// Try to read the file
		try {
			// Create a new line reader
			LineNumberReader lineCounter = new LineNumberReader( new InputStreamReader(new FileInputStream( path )));
			// Read the file, line by line
			while (lineCounter.readLine() != null) {}
			// Copy the line counter
			lineCount = lineCounter.getLineNumber();
		} 
		catch (FileNotFoundException e) {
			System.out.println("Could not find file \"" + path + "\" (maybe you do not have read access).");
		}
		catch (IOException e ) {
			System.out.println("Could not read file \"" + path + "\" (maybe you do not have read access).");
		}
		
		// Return the result
		return lineCount;
	}
	
	private void countFile( String directory, String fileName )
	{
		// Do we have a valid file name?
		if (( fileName != null ) && ( fileName.length() > 0 ) && ( !fileName.equals(".")) & ( !fileName.equals("..") ))
		{
			// Split the file name by the "." character
			String fileParts[] = fileName.split("\\.");
			// Do we at least have two parts?
			if ( fileParts.length > 1 ) {
				// Get the file extension
				String fileExtension = fileParts[ fileParts.length - 1 ].toLowerCase();
				// Is it one of the SCA supported file extensions?
				if ( fortifyScaFileExtensionMapping.containsKey( fileExtension ) ) {
					// Is the file extension already counted?
					if ( countConfiguredFileExtensions.containsKey( fileExtension ) ) {
						// Yes, get the file statistic counter
						fileStatisticCounter counter = ( fileStatisticCounter )( countConfiguredFileExtensions.remove( fileExtension ));
						// Increase the file counter
						counter.addFileCounter( 1 );
						// Increase the line counter
						counter.addLineCounter( getlineCount( directory, fileName ));
						// Put it back in
						countConfiguredFileExtensions.put( fileExtension , counter );
					}
					else {
						// No, initialize the counter
						countConfiguredFileExtensions.put( fileExtension , new fileStatisticCounter( 1, getlineCount( directory, fileName ) ));
					}
				}
				else {
					// Is the file extension already counted?
					if ( countNonConfiguredFileExtensions.containsKey( fileExtension ) ) {
						// Yes, get the counter
						Integer counter = ( Integer )( countNonConfiguredFileExtensions.remove( fileExtension ));
						// Increase the counter
						counter += 1;
						// Put it back in
						countNonConfiguredFileExtensions.put( fileExtension , counter );
					}
					else {
						// No, initialize the counter
						countNonConfiguredFileExtensions.put( fileExtension , new Integer( 1 ));
					}
				}
			}
		}
	}
	
	private void countConfiguredFiles( String directory )
	{
		// Get the file separator
		String fileSeparator = System.getProperty("file.separator");
		// Get the files in the directory
		File dirHandle = new File( directory );
		// We a directory handle?
		if ( dirHandle != null) {
			// Get the list of files in
			File fileList[] = dirHandle.listFiles();
			// Loop through the list of files
			for ( int index = 0; index < fileList.length; index++ ) {
				// Is it a directory?
				if ( fileList[index].isDirectory()) {
					
					// Can we read from that directory?
					if ( fileList[index].canRead()) {
						// List the files in the new directory name
						countConfiguredFiles( directory + fileSeparator + fileList[index].getName());
					}
					else
					{
						System.out.println("Could not read from the directory: \"" + fileList[index].getName() + "\" (you probably don\'t have read access).");
					}
				}
				else
				{
					// It must be a file
					countFile( directory, fileList[index].getName());
				}
			}
		}
	}
	
	private void writeOut( String msg )
	{
		// Write the message to the output
		System.out.println( msg );
		// Write the message to the text file
		outputFile.println( msg );
	}
	
	private void listConfiguredFiles() {
		// Display the header
		writeOut("======================================================");
		writeOut("List of Fortify SCA configured file extensions:");
		writeOut("======================================================");
		// Convert the keys of the hash map to an array
		Object keys[] = countConfiguredFileExtensions.keySet().toArray();
		// Maximum file extension length
		int maxFileExtLen = 0;
		// Walk through the list of found supported file extensions
		for ( int index = 0; index < keys.length; index++) {
	    	// Get the file extension
	    	String fileExtension = (String)(keys[index]);
	    	// Is this file extension longer?
	    	if ( fileExtension.length() > maxFileExtLen ) {
	    		// Copy the new length
	    		maxFileExtLen = fileExtension.length();
	    	}
		}
		// Sort by file name extension
	    SortedSet<String> sortedset= new TreeSet<String>(countConfiguredFileExtensions.keySet());
	    // Count the number of files
	    int totFileCount = 0;
	    // Count the number of lines
	    int totLineCount = 0;
		// Walk through the list of found supported file extensions
	    Iterator<String> iterator = sortedset.iterator();
	    while (iterator.hasNext()) {
	    	// Get the file extension
	    	String fileExtension = ( String )( iterator.next());
	        // Get the file statistic counter
	    	fileStatisticCounter counter = ( fileStatisticCounter )( countConfiguredFileExtensions.get( fileExtension ));
	    	// Get the interpretation
	    	String interpretation = ( String )( fortifyScaFileExtensionMapping.get( fileExtension ) );
	    	// Stretch the file extension
	    	fileExtension = stretchStr( fileExtension, " ", false, maxFileExtLen );
	    	// Stretch the file counter
	    	String strFileCounter = counter.getFileCounterStr( MAX_FILE_COUNTER_LEN );
	    	// Stretch the line counter
	    	String strLineCounter = counter.getLineCounterStr( MAX_LINE_COUNTER_LEN );
	    	// Display the results
	    	writeOut( fileExtension + "|" + strFileCounter + " files |" + strLineCounter + " lines | " + interpretation );	    	
	    	// Count the files
	    	totFileCount += counter.getFileCounter();
	    	// Count the lines
	    	totLineCount += counter.getLineCounter();
	    }
	    // Create a separator
	    String separator = stretchStr( "---", "-", false, maxFileExtLen ) + "+" + stretchStr( "---", "-", false, MAX_FILE_COUNTER_LEN ) + "-------+" + stretchStr( "---", "-", false, MAX_LINE_COUNTER_LEN ) + "-------+---------------";
	    // Display the separator
    	writeOut( separator );
	    // Stretch the title
	    String totFileStr = stretchStr( "Tot", " ", false, maxFileExtLen );
    	// Stretch the file counter
	    String totFileCntStr = stretchInt( totFileCount, " ", true, MAX_FILE_COUNTER_LEN );
    	// Stretch the line counter
	    String totLineCntStr = stretchInt( totLineCount, " ", true, MAX_LINE_COUNTER_LEN );
    	// Display the totals
    	writeOut( totFileStr + "|" + totFileCntStr + " files |" + totLineCntStr + " lines |" );	    	
    	// Display an empty line
	    writeOut("");
	}
	
	private String stretchStr( String theStr, String filler, boolean atFront, int len ) {
		while ( theStr.length()< len ) {
			if ( atFront ) {
				theStr = filler + theStr;
			}
			else {
				theStr += filler;
			}
		}
		return theStr;
	}
	
	private String stretchInt( int theInt, String filler, boolean atFront, int len ) {
		Integer theInteger = new Integer( theInt );
		return stretchStr( theInteger.toString(), filler, atFront, len );
	}
	
	private void listNonConfiguredFiles() {
		// Display the header
		writeOut("======================================================");
		writeOut("List of Fortify SCA non configured file extensions:");
		writeOut("======================================================");
		// Convert the keys of the hash map to an array
		Object keys[] = countNonConfiguredFileExtensions.keySet().toArray();
		// Maximum file extension length
		int maxFileExtLen = 0;
		// Walk through the list of found supported file extensions
		for ( int index = 0; index < keys.length; index++) {
	    	// Get the file extension
	    	String fileExtension = (String)(keys[index]);
	    	// Is this file extension longer?
	    	if ( fileExtension.length() > maxFileExtLen ) {
	    		// Copy the new length
	    		maxFileExtLen = fileExtension.length();
	    	}
		}
		// Sort by file name extension
	    SortedSet<String> sortedset= new TreeSet<String>(countNonConfiguredFileExtensions.keySet());
		// Walk through the list of found supported file extensions
	    Iterator<String> iterator = sortedset.iterator();
	    // Count the number of files
	    int totFileCount = 0;
		// Walk through the list of found supported file extensions
	    while (iterator.hasNext()) {
	    	// Get the file extension
	    	String fileExtension = ( String )( iterator.next());
	        // Get the counter
	    	Integer counter = ( Integer )( countNonConfiguredFileExtensions.get( fileExtension ));
	    	// Stretch the file extension
	    	fileExtension = stretchStr( fileExtension, " ", false, maxFileExtLen );
	    	// Stretch the counter
	    	String strCount = stretchStr( counter.toString(), " ", true, MAX_FILE_COUNTER_LEN );
	    	// Display the results
	    	writeOut( fileExtension + "|" + strCount + " files");
	    	// Count the files
	    	totFileCount += counter.intValue();
	    }
	    // Create a separator
	    String separator = stretchStr( "---", "-", false, maxFileExtLen ) + "+" + stretchStr( "---", "-", false, MAX_FILE_COUNTER_LEN ) + "------";
	    // Display the separator
    	writeOut( separator );
	    // Stretch the title
	    String totFileStr = stretchStr( "Tot", " ", false, maxFileExtLen );
    	// Stretch the file counter
	    String totFileCntStr = stretchInt( totFileCount, " ", true, MAX_FILE_COUNTER_LEN );
	    // Display the totals
    	writeOut( totFileStr + "|" + totFileCntStr + " files");
	}
	
	public void run() {
		//Is there a project root directory?
		if (( projectRootDir != null ) && ( projectRootDir.length() > 0 )) {
			// Examine all files in that directory and sub directories
			countConfiguredFiles( projectRootDir );
			// For all supported file extensions, list all extensions, counter, and interpretation
			listConfiguredFiles();
			// For all non Java related file extensions, list all extensions and counter
			listNonConfiguredFiles();
		}
	}
	
	public void done() {
		// Was the output text file created?
		if ( outputFile != null) {
			// Flush the output
			outputFile.flush();
			// Close the file
			outputFile.close();
			// Reset the file
			outputFile = null;
		}
	}
	
	public static void main(String[] args) {
		// Is a path specified?
		if (( args != null ) && ( args.length > 0 )) {
			// Create the project root directory
			String projectRootDir = "";
			for ( int index = 0; index < args.length; index++ )
			{
				// Is there already something in there?
				if ( projectRootDir.length() > 0 ) {
					// Add a space before adding the next part
					projectRootDir += " " + args[index];
				}
				else
				{
					// Just initialize with the first part
					projectRootDir = args[index];
				}
			}
			// Create the object
			ScaFileMatch scaFileMatch = new ScaFileMatch( projectRootDir );
			// Object created?
			if ( scaFileMatch != null )
			{
				// Object initialized?
				if ( scaFileMatch.init()) {
					// Search for files
					scaFileMatch.run();
					// Terminate the object
					scaFileMatch.done();
				}
			}
		}
		else
		{
			System.out.println("ScaFileMatch - Creates an overview of Fortify SCA configured and non-configured file extensions.");
			System.out.println("Usage:   java ScaFileMatch <project source root directory>");
			System.out.println("Example: java ScaFileMatch C:\\Program Files\\Fortify Software\\Fortify 360 v2.6.5\\Samples\\advanced\\webgoat");
			System.out.println("Note:    1) It is expected to directory to the SCA bin directory is in the environment variable PATH.");
			System.out.println("         2) It is expected the application can at least read the project source root directory.");
			System.out.println("Version: 1.0 - 2010/10/07 - PWAR - Created.");
			System.out.println("         1.1 - 2010/10/08 - PWAR - Sort list by file name extension.");
			System.out.println("                          - PWAR - In case where fortify-sca.properties cannot be found, it will use a hardcoded list.");
			System.out.println("                          - PWAR - Writes the result to a text file with the name \"ScaFileMatch.txt\".");
			System.out.println("         1.2 - 2010/10/13 - PWAR - Counts the number of lines, per Fortify SCA configured file extension.");
			System.out.println("                          - PWAR - Counts the total number of files and lines, per Fortify SCA configured file extension.");
			System.out.println("         1.3 - 2011/03/21 - PWAR - Added support for non SCA configured file extension, using the file \"" + SCA_PROPERTIES_EXTENSION_FILE + "\".");
		}
	}
}
