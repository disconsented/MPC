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


import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Logging {
    public static Logger logger = LogManager.getLogger("MPC");
    
    private static ArrayList<String> miscInfo = new ArrayList<String>();

    public static void info(Object info) {
        logger.info(info);
    }
    
    public static void error(Object error) {
        logger.error(error);
    }
    
    public static void warn(Object warn){
    	logger.warn(warn);    	
    }
    
	public static void addMiscInfo(String e){
		miscInfo.add(e);
	}
	
	public static void flushMiscInfo(){
		for (String e : miscInfo){
			info(e);
		}
		miscInfo.clear();
	}
	
	public static void genericWarning(Exception e){
		Logging.warn("An unhandled exception has occured please report this");
		Logging.warn(e.getLocalizedMessage());
	}
	
	public static void testFail(int i){
		Logging.error("Test "+i+" has failed ("+Checks.checkDescriptions[i-1]+")");
	}
	
	public static void testPass(int i){
		Logging.info("Test "+i+" has passed ("+Checks.checkDescriptions[i-1]+")");
	}
}
