package info_search;

import java.io.IOException;
import java.util.ArrayList;
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.util.BytesRef;


public class DocsInfo {
	
	int MAX_NUM_OF_DOCS = 500;
	int MAX_NUM_OF_TERMS = 5000;
	
	double[][] wd = new double[MAX_NUM_OF_TERMS][MAX_NUM_OF_DOCS];
	int max = 0 ;
	int i = 0;
	int[] MaxTermFreq = new int[MAX_NUM_OF_TERMS];
	int[] numofTerms = new int[MAX_NUM_OF_TERMS];
	int[][] docFreq = new int[MAX_NUM_OF_TERMS][MAX_NUM_OF_DOCS];
	int[][] TermFreq = new int[MAX_NUM_OF_TERMS][MAX_NUM_OF_DOCS];
	int j;
	ArrayList<String> Doc_names = new ArrayList<String>();
	
	public DocsInfo(IndexReader reader){
		
		
		try {
			
			//System.out.println("Number of documents: "+reader.numDocs());
			
					
			for(j=0; j<reader.numDocs(); j++){
				
				String documentName = reader.document(j).getField("path").stringValue();
				//System.out.println(documentName);
				Doc_names.add(documentName);
				
				
				try{
					Terms terms = reader.getTermVector(j, "contents");
					
					TermsEnum te = terms.iterator(null);
					BytesRef t = new BytesRef();
					
			
					i = 0 ;
					max = 0 ;
					
					while((t = te.next()) != null){
						
						DocsEnum docsEnum = te.docs(null, null); // enumerate through documents, in this case only one
						
						docFreq[i][j] = reader.docFreq(new Term("contents", t.utf8ToString()));
						
						
						while ((docsEnum.nextDoc()) != DocIdSetIterator.NO_MORE_DOCS) {
							TermFreq[i][j] = docsEnum.freq();
							if (docsEnum.freq() > max){
					        	max = docsEnum.freq();
					        }
						}
						
						
						i++;
				        		     
					}
				} catch (NullPointerException e){
					System.err.println("Document \""+documentName+"\" is empty.");
				}
				
				numofTerms[j]=i;
				MaxTermFreq[j] = max ;
				
				double x ;
				double y ;
				
				for(int m=0; m<i; m++){
					x = Math.log10((double)reader.numDocs()/(double)docFreq[m][j])/Math.log10(2);
					y = (double)TermFreq[m][j]/(double)MaxTermFreq[j];
					wd[m][j] = x*y;

				}
				
				
			}
			
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			e.getMessage();
		}
		
		
	}
	
	public int getMax_num_of_terms(){
		return MAX_NUM_OF_TERMS;
	}
	
	public double getWd(int i , int j){
		
		return wd[i][j];
		
	}
	
	public int getTermFreq(int i , int j){
		
		return TermFreq[i][j];
		
	}
	
	public int getdocFreq(int i , int j){
		
		return docFreq[i][j];
		
	}
	
	public int getMaxTermFreq(int j){
		
		return MaxTermFreq[j];
		
	}
	
	public int getnumofDocs(){
		
		return j;
		
	}
	
	public int getnumofTerms(int j){
		
		return numofTerms[j];
		
	}
	
	public String getDocNames(int j){
		
		return Doc_names.get(j);
		
	}
		
	
}
