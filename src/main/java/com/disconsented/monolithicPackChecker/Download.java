/*The MIT License (MIT)

Copyright (c) 2015 Disconsented, James Kerr
Copyright (c) 2015 CanVox

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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;


public class Download {
	public static void FileFromUrl(String url) {
		try {
			URL pack = new URL(url);			
			String fileName = url.substring(url.lastIndexOf("/")+1, url.length());
			
			 InputStream in = new BufferedInputStream(pack.openStream());
			 ByteArrayOutputStream out = new ByteArrayOutputStream();			 
			 byte[] buf = new byte[1024];
			 int n = 0;
			 int count = 1;
			 while (-1!=(n=in.read(buf)))
			 {
			    out.write(buf, 0, n);			   
			    if(1000/count == 0){
			    	Logging.info(out.size()/1024+" KB downloaded");
			    	count = 1;
			    } else {
			    	count++;
			    }
			 }
			 out.close();
			 in.close();
			 byte[] response = out.toByteArray();

			 FileOutputStream fos = new FileOutputStream(fileName);
			 fos.write(response);
			 fos.close();
			 Logging.info("A total of " + out.size()/1024 + "KB has been downloaded");
			 Checks.fullZipFileChecks(fileName);
		} catch (MalformedURLException e) {
			Logging.genericWarning(e);			
		} catch (FileNotFoundException e) {
			Logging.genericWarning(e);
		} catch (IOException e) {
			Logging.genericWarning(e);
		}
	}
}