package Pack;
import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.Base64;
import java.util.Arrays;
import java.security.*;
import java.security.spec.*;

public class Client{
  String Id;
  String Kpub64;
  PrivateKey Kpriv;
  PrintStream Sout;
  Scanner  Sin;
   public PrintStream getStreamout (){
	return this.Sout;
	
   }   
   
   public Scanner getStreamin (){
	return this.Sin;
	
   }
   
   
    public void exit (){
	this.Sout.println("exit");
	
   }      
   
    public  String FS_list (){
	
	this.Sout.println("readPubKeys()");
	String res= this.Sin.next();
	if (!(res.equals("No keys stored!") || res.equals("Error reading keys!"))){
	    String[] lines= res.split("\\s");
	    res="";
	    for(int i=0; i<lines.length; i++){
		res=res + "KEY"+i+": "+lines[i]+'\n';
	    }
	}
	return res;
	
    }
    
    
   public void FS_write (String pos, String size, String data){
      this.Sout.println("get("+this.Id+")");         //envia
      String res= this.Sin.next();
      	try{ 
            Signature instance = Signature.getInstance("SHA1withRSA");
	    instance.initSign(this.Kpriv);
      if(res.equals("There is no file from that user!")||res.equals("Server file doesn't exist!")){
	    //Assinatura
	    instance.update(data.getBytes());
	    byte[] signature = instance.sign();
	    String PlainSign = Base64.getEncoder().encodeToString(signature);
	  this.Sout.println("put_k("+data+","+PlainSign+","+this.Kpub64+")");
      }else{
	    String [] block= res.split("\\|");
	    //percorre data para o sitio certo
	    byte[] text= block[1].getBytes();
	    byte[] newtext= data.getBytes();
	    int i=0,j=0, bufferSize;
	    if (Integer.parseInt(pos)+Integer.parseInt(size)>text.length){
		   bufferSize = Integer.parseInt(pos)+Integer.parseInt(size);
	    }else{ bufferSize = text.length;}
	    
	    byte[] aux = new byte[bufferSize];
	    Arrays.fill( aux, (byte) ' ' );
	    while( i< bufferSize ){
		  if(i>=Integer.parseInt(pos) && j<Integer.parseInt(size)){
		    aux[i]=newtext[j];
		    j++;
		  }else{
		    if(i<text.length){
			aux[i]=text[i];
		    }
		  }
		  i++;
	    }
	    
	    String result= new String(aux, "UTF-8");
	    //Assinatura
	    instance.update(aux);
	    byte[] signature = instance.sign();
	    String PlainSign = Base64.getEncoder().encodeToString(signature);
	    this.Sout.println("put_k("+result+","+PlainSign+","+this.Kpub64+")");
      }
      
      String resID= this.Sin.next();
      if(!resID.equals(this.Id)){System.out.println("Server with wrong ID for my file!");}
	  
}catch(SignatureException|UnsupportedEncodingException|InvalidKeyException|NoSuchAlgorithmException e){ System.out.println(e);}

   }
   
   public String FS_read (String id, String pos, String size){
    this.Sout.println("get("+id+")");         //envia
    String res= this.Sin.next();
    if(!(res.equals("There is no file from that user!")||res.equals("Wrong Signature, Files Corrupt!")||res.equals("Server file doesn't exist!"))){
	String [] block= res.split("\\|");
	try{
	    //generate key from text
	    byte[] decodedKey = Base64.getDecoder().decode(block[3]); //plain publick
	    X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedKey);
	    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
	    PublicKey pubKey = keyFactory.generatePublic(keySpec);
	    //CHECK SIGNATURE
	    String OriginalData = Base64.getEncoder().encodeToString(pubKey.getEncoded());
	    Signature sig = Signature.getInstance("SHA1withRSA");
	    sig.initVerify(pubKey);
	    byte[] BytesSign= Base64.getDecoder().decode(block[2]);
	    sig.update(block[1].getBytes());
	    boolean isValid = sig.verify(BytesSign);
	    if(!isValid){return "Wrong Signature Sent by the Server!";}
	    
	    //percorre data para o sitio certo
	    byte[] text= block[1].getBytes();
	    byte[] aux;
	    if(Integer.parseInt(pos)>= text.length){return "";}
	    if(Integer.parseInt(size)> text.length){aux = new byte[text.length];
	    }else{aux = new byte[Integer.parseInt(size)];}
	    Arrays.fill( aux, (byte) 0 );
	    int i=Integer.parseInt(pos);
	    int j=0;
	    while(i<(Integer.parseInt(pos)+Integer.parseInt(size)) && i< text.length ){
		  aux[j]=text[i];
		  i++;
		  j++;
	    }
	    
	   String result= new String(aux, "UTF-8");
	   return result;
	    
	    
	  }catch(NoSuchAlgorithmException|UnsupportedEncodingException|SignatureException|InvalidKeyException|InvalidKeySpecException e){
	      System.out.println(e);
	  }
	  
	 }else{ return res;} 
	 
	 return "ERROR";
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
	      
	      
	      
	      ///******************************************/////////////////**********************
	      this.Sout.println("storePubKey("+encodedBytesPub+")");
	      return this.Id+ " " + this.Sin.next();
	      
	      ///******************************************/////////////////**********************
	      
	      
	      //envia o id ao cliente
	      //return this.Id;                  <----
			      
			      
      }catch(NoSuchAlgorithmException|IOException e){
	      System.out.println(e);
      }
      return "Ocorreu um erro";
   
   }
   

   
   
}
  
  
