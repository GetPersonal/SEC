package ClientPack;
import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.Base64;
import java.security.*;

public class Client{
  String Id;
  String Kpub64;
  PrivateKey Kpriv;
  PrintStream Sout;
  Scanner  Sin;
  
   
    public void exit (){
	this.Sout.println("exit");
	
   }           
   public void FS_write (String pos, String size, String data){
      this.Sout.println("get("+this.Id+")");         //envia
      String res= this.Sin.next();
      	try{ 
            Signature instance = Signature.getInstance("SHA1withRSA");
	    instance.initSign(this.Kpriv);
	    instance.update(data.getBytes());
	    byte[] signature = instance.sign();
	    String PlainSign = Base64.getEncoder().encodeToString(signature);
	    
      if(res.equals("There is no file from that user!")||res.equals("Server file doesn't exist!")){;
	  this.Sout.println("put_k("+data+","+PlainSign+","+this.Kpub64+")");
      }else{
	  // // percorrer a mensagem bloco até ao pos ver se é preciso fazer padding
	  this.Sout.println("put_k("+data+","+PlainSign+","+this.Kpub64+")");
      }
      System.out.println( "With "+ this.Sin.next());
	  
}catch(SignatureException|InvalidKeyException|NoSuchAlgorithmException e){ System.out.println(e);}

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
	      PublicKey publicKey = kp.getPublic();
	      this.Kpriv= kp.getPrivate();
	  
	      //escreve Kpub no ficheiro
	      String encodedBytesPub = Base64.getEncoder().encodeToString(publicKey.getEncoded());
	      this.Kpub64=encodedBytesPub;
	      System.out.println("PUUUUB: "+encodedBytesPub);
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
	      this.Id = Base64.getEncoder().encodeToString(digest);
	      //envia o id ao cliente
	      return this.Id;
			      
			      
      }catch(NoSuchAlgorithmException|IOException e){
	      System.out.println(e);
      }
      return "Ocorreu um erro";
   
   }
   

   
   
}
  
  
