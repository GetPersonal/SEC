import java.util.*;
import java.io.*;
import Pack.*;
import java.util.Scanner;

/**
 *  Test suite
 */
public class TestClient implements Runnable{

	private static Client client1;
	private static Client client2;
	private static String id1;
	private static String id2;
	int tests=0;
	
    public void init(){
		this.client1= new Client();
		this.id1=this.client1.FS_init();
		this.client2= new Client();
		this.id2=this.client2.FS_init();
    }
    
    public  void validate(boolean bool) {
	  if(bool){System.out.println("Expected Result, Success!");tests++;
	  }else{System.out.println("Not Expected Result, Failure!");}
   }
    
    public void run() {        
    int count=1;
	  System.out.println(count+ " Creating 2 users...");count++;
	    init();
	    boolean cmp;
	    cmp= id1.equals(id2);
	    validate(!cmp);
	    
	    System.out.println("client1 id is "+id1+" client2 id is "+id2);
	    System.out.println("Start auto-testing!");
    
     
	   System.out.println( count+" ****No File on server***");count++;
	   cmp= (client1.FS_read(id1,"0","10")).equals("Server file doesn't exist!");
	   validate(cmp);
	   
	   System.out.println(count+" ****Read before writing anything***");count++;
	   try{
		FileOutputStream outputStream = new FileOutputStream("Block.txt",true);
	   }catch(IOException e){
	      System.out.println(e);}
	   cmp= (client1.FS_read(id1,"0","10")).equals("There is no file from that user!");
	   validate(cmp);
	   
	   
	   System.out.println( count+" ****Write a file***");count++;
	   client1.FS_write("0","10","StringTest");
	   cmp= (client1.FS_read(id1,"0","10")).equals("StringTest");
	   validate(cmp);
	   
	   System.out.println( count+" ****Write on top of file ***");count++;
	   client1.FS_write("0","10","TestString");
	   cmp= (client1.FS_read(id1,"0","10")).equals("TestString");
	   validate(cmp);
	   
	   System.out.println( count+" ****Write after file ***");count++;
	   client1.FS_write("10","10","TestString");
	   cmp= (client1.FS_read(id1,"0","20")).equals("TestStringTestString");
	   validate(cmp);
	   
	   System.out.println( count+" ****Write after file with padding ***");count++;
	   client1.FS_write("22","3","end");
	   cmp= (client1.FS_read(id1,"0","25")).equals("TestStringTestString  end");
	   validate(cmp);
    
	   
	   System.out.println( count+" ****Write with other client and read both ***");count++;
	   client2.FS_write("0","25","TestStringTestString  end");
	   cmp= (client1.FS_read(id1,"0","25")).equals("TestStringTestString  end") && (client2.FS_read(id2,"0","25")).equals("TestStringTestString  end");
	   validate(cmp);
	   
	   
	   System.out.println( count+" ****Read first data part of second block ***");count++;
	   cmp= (client2.FS_read(id2,"0","20")).equals("TestStringTestString");
	   validate(cmp);
	   
	   System.out.println( count+" ****Read first data part of second block ***");count++;
	   cmp= (client2.FS_read(id2,"0","20")).equals("TestStringTestString");
	   validate(cmp);
	   
	   System.out.println( count+" ****Read in the middle of data  of second block ***");count++;
	   cmp= (client2.FS_read(id2,"14","6")).equals("String");
	   validate(cmp);
	   
	   System.out.println( count+" ****Read data and after it ***");count++;
	   cmp= (client2.FS_read(id2,"0","1000000000")).equals("TestStringTestString  end");
	   validate(cmp);
	   
	   System.out.println( count+" ****Read data of other client ***");count++;
	   cmp= (client1.FS_read(id2,"0","25")).equals("TestStringTestString  end");
	   validate(cmp);
	   
	   
	   
	   
	   
	   
	   
	   
	    System.out.println( " ================  Security Tests =================");
	    
	    
	    
	   System.out.println( count+" *********Files on server are corrupted *********");count++;
	   client1.FS_write("0","10","StringTest");
	    //ALTER DATA BLOCK, HACK ON SERVER
	   try{
		  String text="";
		  FileReader reader = new FileReader("Block.txt");
		  BufferedReader bufferedReader = new BufferedReader(reader);
		  String line="";
		  String[] block;
		  while ((line = bufferedReader.readLine()) != null) { //LÊ O FICHEIRO
		      block=line.split("\\|");
		      if (!block[0].equals(id1)){
			  text=text+line+'\n';
		      }else{text=text+block[0]+"|DATA|"+block[2]+"|"+block[3]+'\n';}
		  }
		  reader.close();
		  FileOutputStream outputStream = new FileOutputStream("Block.txt");
		  OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
		  BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter); 
		  bufferedWriter.write(text);
		  bufferedWriter.close();
	   }catch(IOException e){
	      System.out.println(e);}	      
	   cmp= (client1.FS_read(id1,"0","10")).equals("Wrong Signature, Files Corrupt!");
	   validate(cmp);
	    
	    
	    
	    
	   System.out.println( count+" ********* Man in the middle, data!= signed data *********");count++;
	   PrintStream out= client1.getStreamout();
	   //generate a wrong request signed data != data
	   out.println("put_k(DATA,WIRzwXVxaioPW4wyAC2RS6Xhd2gBtu8n7hVXh62OWY88Z4y1ApGk3ON3NIXLjNH4N3gdWSwR+iJj/6AiUI2pQw==,MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAIBV+NFanZ+RuWSBaEn5eIchHmHRSI2UF7BmMDDnJm5uDlixXoR7VE1zXikAbTHAyV+J1YOpw1z6Ambj4HZxqjsCAwEAAQ==");
	   Scanner in= client1.getStreamin();	
	   cmp= (in.next()).equals("Wrong Signature");
	   validate(cmp);
	   
	    
	   client1.exit();
	   client2.exit();
	   System.out.println("SUCCESSFUL TESTS: "+tests);
    }
	
	
       
    
    public static void main (String args[]){
	    new Thread(new TestClient()).start();	    
    }
}