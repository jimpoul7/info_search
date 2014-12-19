package info_search;


import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;


import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;



public class Main {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws ParseException 
	 */
	
	public static void main(String[] args) throws IOException, ParseException {
		
		DecimalFormat df = new DecimalFormat("0.0000");
		
		Html_to_txt ht_txt = new Html_to_txt();
		
		ht_txt.get_eng_html_files();
		ht_txt.get_text();
		
		Indexer indx = new Indexer();
		/*
		 * The following function creates the index of the files that are stored
		 * in the given directory. The first parameter is the directory where the 
		 * documents to be indexed are.
		 * The second parameter, when set to "true" will create a new index, removing
		 * any other existing. When set to false, only un-indexed files will be indexed.
		 */
		indx.createIndex("files", true);
		
		//	Define Queries 
				
		String Queries[][] = 
		    {
		      {"Applied", "Research", "and", "Innovation"},
		      {"Cloud", "Computing"},
		      {"Laboratory", "Staff"},
		      {"Air", "Pollution"},
		      {"Data", "Analysis", "and", "Forecasting"}
		      
		    };
		
		//	Query preprocessing
		
		Analyzer analyzer = new EnglishAnalyzer();
		QueryParser parser = new QueryParser("", analyzer);
		
		int i,j ;
		
		for (i=0; i<Queries.length; i++){
			for (j=0; j<Queries[i].length; j++){
				Queries[i][j] = parser.parse(Queries[i][j]).toString();
			}			
		}
		
		int  NUM_OF_QUERIES = Queries.length;
		
		//	Print doc info , Calc wd , wq
		
		IndexReader reader = indx.getIndexReader("index");
		
		DocsInfo docinfo = new DocsInfo(reader);
		
		int numofDocs = docinfo.getnumofDocs();
		int max_num_of_terms = docinfo.MAX_NUM_OF_TERMS;
		
		int NUM_OF_DOCS = numofDocs;
		int MAX_NUM_OF_TERMS = max_num_of_terms;
		
		int numofTerms;
		double[][] wd = new double[MAX_NUM_OF_TERMS][NUM_OF_DOCS];
		double[][][] wq = new double[MAX_NUM_OF_TERMS][NUM_OF_DOCS][NUM_OF_QUERIES];
		int[] check_i_term = new int[MAX_NUM_OF_TERMS]; 
		
		
		System.out.println("Number of documents: "+numofDocs);
		
		for (j=0; j<numofDocs; j++){
			
			//System.out.println(docinfo.getDocNames(j));
			numofTerms = docinfo.getnumofTerms(j);
			Terms terms = reader.getTermVector(j, "contents");			
			TermsEnum te = terms.iterator(null);
			
			for (i=0; i<numofTerms; i++){
				String temp_term = te.next().utf8ToString();
				//System.out.println("Term: "+temp_term);
				//System.out.print("docFreq: "+docinfo.getdocFreq(i,j));
				//System.out.print(" ,TermFreq: "+docinfo.getTermFreq(i,j));
				//System.out.print(" ,MaxTermFreq: "+docinfo.getMaxTermFreq(j));
				wd[i][j] = docinfo.getWd(i, j);
				//System.out.println(" ,Wd["+i+"]["+j+"]: "+docinfo.getWd(i, j));
				
				for (int k=0; k<Queries.length; k++){
					for (int l=0; l<Queries[l].length; l++){						
						if (Queries[k][l].equals(temp_term)){
							check_i_term[i] = 1;
						}
						else{
							check_i_term[i] = 0;
						}
					}
					if(check_i_term[i] == 1){
						wq[i][j][k] = wd[i][j] ;
						//System.out.println("wq["+i+"]["+j+"]["+k+"]: "+wq[i][j][k]);
					}
					else{
						wq[i][j][k] = 0;
					}
								
				}
			}
		}
			
		// Calc sim_score 
		
		double[][] sim_score = new double[NUM_OF_QUERIES][NUM_OF_DOCS];
		double[] doc_lengths = new double[NUM_OF_DOCS];
		double[] query_lengths = new double[NUM_OF_QUERIES];
		
		for (j=0; j<numofDocs; j++){
			numofTerms = docinfo.getnumofTerms(j);
			for (i=0; i<numofTerms; i++){
				doc_lengths[j] = doc_lengths[j] + (wd[i][j]*wd[i][j]);
			}
			//System.out.println("doc_lengths["+j+"]: "+doc_lengths[j]);
		}
		
		for (j=0; j<numofDocs; j++){
			doc_lengths[j] = Math.sqrt(doc_lengths[j]);
		}
		
		for (int k=0; k<Queries.length; k++){
			for (j=0; j<numofDocs; j++){
				numofTerms = docinfo.getnumofTerms(j);
				for (i=0; i<numofTerms; i++){
					query_lengths[k] = query_lengths[k] + (wq[i][j][k]*wq[i][j][k]);
				}	
			}
			//System.out.println("query_lengths["+k+"]: "+query_lengths[k]);
		}
		
		for (int k=0; k<Queries.length; k++){
			query_lengths[k] = Math.sqrt(query_lengths[k]);
		}
		
		for (j=0; j<numofDocs; j++){
			for (int k=0; k<Queries.length; k++){
				numofTerms = docinfo.getnumofTerms(j);
				double sum_of_prods = 0;
				for (i=0; i<numofTerms; i++){				
					sum_of_prods = sum_of_prods + (wd[i][j] * wq[i][j][k]); 					
				}
			sim_score[k][j] = (sum_of_prods / (doc_lengths[j]*query_lengths[k]));  
			//System.out.println("Sim_score["+k+"]["+j+"]: "+sim_score[k][j]);
			}	
		}
		
		// Sort similarities , Print top 10 results and the doc names
		
		for (int k=0; k<Queries.length; k++){
			
			ArrayIndexComparator comparator = new ArrayIndexComparator(sim_score[k]);
			Integer[] indexes = comparator.createIndexArray();
			Arrays.sort(indexes, comparator);
		
		
			System.out.println("\nPrinting top 10 Results for Query "+(k+1)+": \n\n");
			for (j=indexes.length-1; j>=indexes.length-10; j--){		
				System.out.println("Sim_score: "+df.format(sim_score[k][indexes[j]])+"  in doc :"+docinfo.getDocNames(indexes[j]));
			}
		}
	}
	

}
