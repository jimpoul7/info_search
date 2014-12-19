package info_search;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Scanner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;


public class Html_to_txt {
	
	public Html_to_txt(){}
		
		public  void get_eng_html_files() throws IOException
		{

			File folder = new File("htmls");
			File[] listOfFiles = folder.listFiles();
			
			for (File file : listOfFiles) {
			    if (file.isFile()) {
			    	Document doc = Jsoup.parse(file, "UTF-8");
			    	org.jsoup.select.Elements elem = doc.getElementsByTag("html");
			    	String lang = elem.attr("lang");
			    	if (lang.equals("el"))
			    	{
			    		file.delete();
			    	 }
			    }
			}
		  
		}
		
		public  void get_text() throws IOException
		{

			try{
				  File folder = new File("htmls");
				  File[] listOfFiles = folder.listFiles();
				  for (File file : listOfFiles) {
					  
					  Scanner br = new Scanner(new FileInputStream(file), "UTF-8");
					  StringBuilder sb = new StringBuilder();
				      Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("/home/dimitris/workspace/Info Search/files/" + file.getName()+".txt"), "UTF-8"));			            
				      String line = null;		    
				     
				      while(br.hasNext())
				      {   	  
				    	  line=br.nextLine(); 
				    	  sb.append(line);		 
				    	}
				      
				      String textOnly = Jsoup.parse(sb.toString(),"UTF-8").body().text();		      
				 	  String words[]=textOnly.split("  ");
				         for(int i=0;i<words.length;i++)
				         {
				        	 if (words[i].length() > 0) 		
					    	    {
				        		 out.write(words[i]);		    				  
				        		 ((BufferedWriter) out).newLine();
					    	    } 
				         }
				     
				      out.close();
					  br.close();
					
				   }	
			 }catch (Exception e){
			System.err.println("Error: " + e.getMessage());
			}

		}
		
		

}
