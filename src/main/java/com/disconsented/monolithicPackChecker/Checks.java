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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class Checks{
/*
 * Check is valid zip
 * Check top level for /mods/ /bin/ and /config/
 * 	report a warning on other files there
 * check /bin/ for modpack.jar
 * 	check modpack.jar does not contain forge.jar
 *  check forge version is appropiate for mc
 *  maybe check its recommended 
 * check /bin/ doesn't have minecraft.jar
 * 
 * All checks need to report erros if failed
 */
	@SuppressWarnings("serial")
	public static ArrayList<String> directories = new ArrayList<String>(){{
		add("bin/");
		add("config/");
		add("mods/");
		}};

	public static boolean fullZipFileChecks(String fileString) throws IOException{
		ZipFile file = null;
		try{
			file = new ZipFile(new File(fileString));
		} catch (ZipException e){
			Logging.error(e.getLocalizedMessage());
			Logging.info("Maybe fileString is not a real ZIP file? (Caused by a ZIP formatting error");
			return false;
		} catch (IOException e){
			Logging.error(e.getLocalizedMessage());
			return false;
		} catch (Exception e){
			Logging.warn("An unhandled exception has occured please report this");
			Logging.error(e.getLocalizedMessage());
			return false;
		}
		
		Logging.info(file.getName()+" has passed test 1 (binding text to file object)");
		
		if(!canAccessZipFile(file)){
			file.close();
			return false;
		}
		
		if(!isValidStructure(file)){
			file.close();
			return false;
		}
		
		if(!containsModpackJar(file)){
			file.close();
			return false;
		}
		
		if(!isForge(file)){
			file.close();
			return false;
		}
		file.close();
		return true;
		
	}
	
	@SuppressWarnings("unused")
	private static boolean isValidURL(String urlString){
		//Check if dropbox file
		//check for ?dl=1
		//check for dl.dropbox.com instead of www.dropbox.com
		return false;
		
	}
	
	@SuppressWarnings("unused")
	private static boolean canAccessZipFile(ZipFile file){
		try {
			Enumeration<? extends ZipEntry> entries = file.entries();
			
			while(entries.hasMoreElements()){
		        ZipEntry entry = entries.nextElement();
		        InputStream stream = file.getInputStream(entry);
		    }
		} catch (Exception e) {
			Logging.error("Unable to access ZipFile");
			Logging.warn("An unhandled exception has occured please report this");
			Logging.error(e.getLocalizedMessage());
			return false;
		}		
		Logging.info(file.getName()+" has passed test 2 (accessing file)");
		return true;
	}
	
	private static boolean isValidStructure(ZipFile file){
		ArrayList<String> topLevelContents = new ArrayList<String>();
		try {
			Enumeration<? extends ZipEntry> entries = file.entries();
			
			while(entries.hasMoreElements()){
				String nextElement = entries.nextElement().toString();
				if((nextElement.endsWith("/") && nextElement.indexOf('/') == nextElement.length()-1) || !nextElement.contains("/")){
					topLevelContents.add(nextElement);
				}
		    }
		} catch (Exception e) {
			Logging.warn("An unhandled exception has occured please report this");
			Logging.error(e.getLocalizedMessage());
			return false;
		}
		if(topLevelContents.size() == 1){
			Logging.error("Top level of ZIP only contains + entry");
			Logging.error("Expecting: ['mods/', 'bin/', 'config/']");
			return false;
		}
		for(String entry : directories){
			if(topLevelContents.contains(entry)){
				Logging.info(entry+" has been found; Proceeding.");
				topLevelContents.remove(entry);
			} else {
				Logging.error(entry+" has not been found; Ending test");
				return false;
			}
		} 
		for(String entry : topLevelContents){
			Logging.warn(entry + " is an unknown file/directory. Is this misplaced?");
		}
		
		Logging.info(file.getName()+" has passed test 3 (Top Level Structure)");
		return true;
		
	}
	
	private static boolean containsModpackJar(ZipFile file){
		Enumeration<? extends ZipEntry> entries = file.entries();
		
		while(entries.hasMoreElements()){
			if(entries.nextElement().getName().equals("bin/modpack.jar")){;
				Logging.info(file.getName()+" has passed test 4 (bin/modpack.jar was found)");
				return true;
			}
	    }
		Logging.error("bin/modpack.jar has not been found; Ending test");
		return false;
	}
	
	private static boolean isForge(ZipFile file){
		File tempJar = new File("temp.jar");
		tempJar.deleteOnExit();
		try {
			InputStream inputStream = file.getInputStream(file.getEntry("bin/modpack.jar"));
			OutputStream outputStream = new FileOutputStream(tempJar);
			
			int read = 0;
			byte[] bytes = new byte[1024];
	 
			while ((read = inputStream.read(bytes)) != -1) {
				outputStream.write(bytes, 0, read);
			}
			outputStream.close();
			inputStream.close();
			
			JarFile modpackjar = new JarFile(tempJar);
			Enumeration<? extends JarEntry> entries = modpackjar.entries();
			
			ArrayList<String> modpackEntries = new ArrayList<String>();
			while(entries.hasMoreElements()){
				String entry = entries.nextElement().getName();
				if (entry.matches("(forge)") && entry.matches(("universal+\\.jar$"))){
					Logging.error("modpack.jar appears to be Forge Installer not Forge Universal");
					modpackjar.close();
					return false;
				} 
				modpackEntries.add(entry);
		    }
			if(!(modpackEntries.contains("forge_logo.png") &&
					modpackEntries.contains("forge_at.cfg") &&
					modpackEntries.contains("CREDITS-fml.txt"))) {
				modpackjar.close();
				return false;
			}
			modpackjar.close();
		} catch (Exception e) {
			Logging.warn("An unhandled exception has occured please report this");
			Logging.error(e.getLocalizedMessage());
			return false;
		}
		
		Logging.info(file.getName()+" has passed test 5 (modpack.jar appears to be valid Minecraft Forge)");
		return true;
		
	}
}
