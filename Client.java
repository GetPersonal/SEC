package ClientPack;
import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.Base64;
import java.security.*;

public class Client{
  Key Kpriv;
  PrintStream Sout;
  Scanner  Sin;
  
   
            
   public void FS_write (String pos, String size){
   
	
   }
   public void exit (){
	this.Sout.println("exit");
	
   }
   public String FS_read (String id, String pos, String size){
	this.Sout.println("get("+id+")");         //envia
	return this.Sin.next();
   } 
   
   
   
   public String FS_init (){
   
	try{	
	      Socket s= new Socket("127.0.0.1",1342);    
	      this.Sout= new PrintStream(s.getOutputStream());            // envia po server
	      this.Sin=new Scanner(s.getInputStream()).useDelimiter("\n");
	      
	      //gera keys
	      KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
	      kpg.initialize(512);

	      KeyPair kp = kpg.genKeyPair();
	      Key publicKey = kp.getPublic();
	      this.Kpriv= kp.getPrivate();
	  
	      //escreve Kpub no ficheiro
	      String encodedBytesPub = Base64.getEncoder().encodeToString(publicKey.getEncoded());
	      FileOutputStream outputStream = new FileOutputStream("Keys.txt", true);
	      OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
	      BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
	      bufferedWriter.write(encodedBytesPub+'\n');
	      bufferedWriter.close();
	      
	      //faz o hash da chave publica
	      byte[] decodedBytes = Base64.getDecoder().decode(encodedBytesPub);
	      MessageDigest md = MessageDigest.getInstance("SHA-256");
	      md.update(decodedBytes); 
	      byte[] digest = md.digest();
	      String id = Base64.getEncoder().encodeToString(digest);
	      //envia o id ao cliente
	      return id;
			      
			      
      }catch(NoSuchAlgorithmException|IOException e){
	      System.out.println(e);
      }
      return "Ocorreu um erro";
   
   }
   

   
   
}
  
  
