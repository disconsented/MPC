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
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
	public static final ArrayList<String> directories = new ArrayList<String>(){{
		add("bin/");
		add("config/");
		add("mods/");
		}};
		
	@SuppressWarnings("serial")
	public static final ArrayList<String> fileWhitelist = new ArrayList<String>(){{
		add(".jar");
		add(".zip");
	}};
		
	private static String mcVersion;

	public static boolean fullZipFileChecks(String fileString) throws IOException{
		ZipFile file = null;
		try{
			file = new ZipFile(new File(fileString));
		} catch (ZipException e){
			Logging.error(e.getLocalizedMessage());
			Logging.info("Maybe "+ fileString +" is not a real ZIP file? (Caused by a ZIP formatting error");
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
		
		if(!containsMatchingVersions(file)){
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
	
	private static boolean canAccessZipFile(ZipFile file){
		try {
			Enumeration<? extends ZipEntry> entries = file.entries();
			ArrayList<ZipEntry> zipEntry = new ArrayList<ZipEntry>();
			while(entries.hasMoreElements()){
				zipEntry.add(entries.nextElement());		        
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
		Set<String> topLevelContents = new HashSet<String>();
		try {
			Enumeration<? extends ZipEntry> entries = file.entries();
			
			while(entries.hasMoreElements()){
				String nextElement = entries.nextElement().toString();
				if(nextElement.contains("/")){
					topLevelContents.add(nextElement.substring(0, nextElement.indexOf("/")+1));
				}
		    }
		} catch (Exception e) {
			Logging.warn("An unhandled exception has occured please report this");
			Logging.error(e.getLocalizedMessage());
			return false;
		}
		if(topLevelContents.size() == 1){
			Logging.error("Top level of ZIP only contains" + topLevelContents.toArray()[0]);
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
				//If the modpack.jar file contains forge universal then it fails the test
				if (entry.matches("(forge)") && entry.matches(("universal+\\.jar$"))){
					Logging.error("modpack.jar appears to be Forge Installer not Forge Universal");
					modpackjar.close();
					return false;
				}
				
				Pattern forgeVersion = Pattern.compile("-[0-9]+\\.[0-9]+\\.[0-9]+-");
				Matcher m = forgeVersion.matcher(entry);
				if(m.find()){
					mcVersion = m.group().replace("-", "");
					Logging.info("Modpacks apears to be for Minecraft version " + mcVersion);
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
	
	private static boolean containsMatchingVersions(ZipFile file){
		if (mcVersion == null){
			Logging.error("Minecraft version was not detected; Maybe Forge is not installed correctly? (Report this error)");
			return false;
		}
		try{
			
		Enumeration<? extends ZipEntry> entries = file.entries();
		ArrayList<String> modFiles = new ArrayList<String>();
		while(entries.hasMoreElements()){
			ZipEntry next = entries.nextElement();
			if(!next.isDirectory() && next.getName().contains("mods/")){
				modFiles.add(next.getName());
			}
		}
		
		Pattern p = Pattern.compile("\\.[a-z]+$");
		Matcher m;
		
		for(String entry : modFiles){
			m = p.matcher(entry);
			if(m.find()){
				if(!fileWhitelist.contains(m.group())){
					Logging.error("Unknown file in mods/"+entry);
					return false;
				}
				if(!entry.contains(mcVersion)){
					Logging.warn(entry+" does NOT appear to be for Minecraft Version " + mcVersion);
				}				
				
			} else {
				Logging.error("Unknown file in mods/"+entry);
			}			
			
		}
		Logging.addMiscInfo("Minecraft Version: " + mcVersion);
		Logging.addMiscInfo("Mod file count: " + modFiles.size());
		} catch (Exception e){
			Logging.warn("An unhandled exception has occured please report this");
			Logging.error(e.getLocalizedMessage());
			return false;
		}		
		Logging.info(file.getName()+" has passed test 6 (file extentions and Minecraft version)");
		return true;
		
	}
	
	
}
