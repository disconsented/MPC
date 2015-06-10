/*The MIT License (MIT)

Copyright (c) 2015 Disconsented, James Kerr

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 */
package com.disconsented.monolithicPackChecker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Disconsented
 *
 */
public class Main {
	@SuppressWarnings("serial")
	private static ArrayList<String> welcome = new ArrayList<String>(){{
		add("########################################################################################");
		add("You're using Monolithic Pack Checker (MPC)");
		add("Created by: Disconsented");
		add("Github: disconsented/MPC");
		add("########################################################################################");
	}};	
	
	public static void main(String[] args) throws IOException{
		for (String entry : welcome){
			System.out.println(entry);
		}
		
		Logging.warn("This project is under development");		
		
		//Check for arguments
		if(args.length == 2){
			switch(args[0].toLowerCase()){
			case "-download":
				Download.FileFromUrl(args[1]);
				break;
			case "-file":
				Checks.fullZipFileChecks(args[1]);					
				break;
			}
			Logging.info("All checks completed");
			Logging.flushMiscInfo();
		} else {
			Logging.error("Correct usage: -[download/file] [url/file]");
			Logging.error(args.length+" arguments detected (expected 2)");
			Logging.error(Arrays.toString(args));
		}
	}

}
