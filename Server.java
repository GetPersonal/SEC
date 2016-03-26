import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.Base64;
import java.security.*;
import java.security.spec.*;
public class Server implements Runnable {
  
Socket ss;
PrintStream printer;
String userKey;
   
    Server(Socket csocket) {
      this.ss = csocket;
   }

   public String store(String key) {
      try{
	  if (new File("Keys.txt").exists() ){
	    FileReader reader = new FileReader("Keys.txt");
	    BufferedReader bufferedReader = new BufferedReader(reader);
	    String line="";
	    while ((line = bufferedReader.readLine()) != null) { //LÊ O FICHEIRO
	      if (line.equals(key)){
		  return "Key was successfully stored!";
	      }
	    reader.close();
	    }
	  }
	  FileOutputStream outputStream = new FileOutputStream("Keys.txt", true);
	  OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
	  BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
	  bufferedWriter.write(key+'\n');
	  bufferedWriter.close();
	  return "Key was successfully stored!";
      }catch( IOException e){
	  System.out.println(e);
	}
	return "Error report on storage";
   }
   
   
   
   public String read() {
	try{
	  if (new File("Keys.txt").exists() ){
		FileReader reader = new FileReader("Keys.txt");
		BufferedReader bufferedReader = new BufferedReader(reader);
		String line="",lines="";
		while ((line = bufferedReader.readLine()) != null) { //LÊ O FICHEIRO
		    lines=lines + line + " " ;
		}
		reader.close();
		return lines;
	  }else{
	    return "No keys stored!";
	  }
   
	}catch(IOException e){
	  System.out.println(e);
	}
	return "Error reading keys!";
  }

   
  public String get(String id)throws IOException{
  
    if (new File("Block.txt").exists() ){      //VÊ SE O FICHEIRO EXISTE
      FileReader reader = new FileReader("Block.txt");
      BufferedReader bufferedReader = new BufferedReader(reader);
      String line="";
      String[] block;
      while ((line = bufferedReader.readLine()) != null) { //LÊ O FICHEIRO
	block=line.split("\\|");
	if (block[0].equals(id)){
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
	    if(!isValid){return "Wrong Signature, Files Corrupt!";}
	  }catch(NoSuchAlgorithmException|SignatureException|InvalidKeyException|InvalidKeySpecException e){
	      System.out.println(e);
	  }
	  reader.close();
	  return line;
	}
      }
      reader.close();
      return "There is no file from that user!";
    
    }else{return "Server file doesn't exist!";}
      
  
  } 
  
    public String put_k (String data, String sign, String key)throws IOException{
    
 try{
        byte[] decodedKey = Base64.getDecoder().decode(key); //plain publick
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedKey);
	KeyFactory keyFactory = KeyFactory.getInstance("RSA");
	PublicKey pubKey = keyFactory.generatePublic(keySpec);
	
	//CHECK SIGNATURE
	String OriginalData = Base64.getEncoder().encodeToString(pubKey.getEncoded());
	Signature sig = Signature.getInstance("SHA1withRSA");
	sig.initVerify(pubKey);
	byte[] BytesSign= Base64.getDecoder().decode(sign);
	sig.update(data.getBytes());
	boolean isValid = sig.verify(BytesSign);
	if(!isValid){return "Wrong Signature";}
	
	//hash id
	byte[] decodedBytes = Base64.getDecoder().decode(key);
	MessageDigest md = MessageDigest.getInstance("SHA-256");
	md.update(decodedBytes); 
	byte[] digest = md.digest();
	String Id = Base64.getEncoder().encodeToString(digest);
	
	String text="";  
	if (new File("Block.txt").exists() ){      //VÊ SE O FICHEIRO EXISTE
	    FileReader reader = new FileReader("Block.txt");
	    BufferedReader bufferedReader = new BufferedReader(reader);
	    String line="";
	    String[] block;
	    while ((line = bufferedReader.readLine()) != null) { //LÊ O FICHEIRO
		block=line.split("\\|");
		if (!block[0].equals(Id)){
		    text=text+line+'\n';
		}
	    }
	    reader.close();
	}
	text=text+Id+"|"+data+"|"+sign+"|"+key+'\n';
	FileOutputStream outputStream = new FileOutputStream("Block.txt");
	OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
	BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter); 
	bufferedWriter.write(text);
	bufferedWriter.close();
	return Id;
}catch(NoSuchAlgorithmException|SignatureException|InvalidKeyException|InvalidKeySpecException e){
	      System.out.println(e);
      }
   return "ERROR";
  }
   
   
  public static void main (String args[]) throws IOException{

      ServerSocket s1= new ServerSocket(1342);
      System.out.println("Listening");
      while(true){
	Socket sss=s1.accept();       //aceita ligação cliente
	System.out.println("Connected" );
	new Thread(new Server(sss)).start();
      }
   }  
    
  



  public void run() {
      try {
	Scanner sc=new Scanner(ss.getInputStream()).useDelimiter("\n"); //aceita input
	PrintStream p= new PrintStream(ss.getOutputStream()); //envia output
	printer= p;
	
	String msg="", res="", data, sign, pubk;
	String[] cmd, arg;
	
	    //retirar msg recebida
	
	while( !msg.equals("exit")){
	  
	  msg=sc.next();    //retirar msg recebida
	  if (msg.equals("exit")){
	    System.out.println("********* Sessao Terminada. Obrigado! *******");
	    break;
	  }
    
	
	    cmd =msg.split("\\(|\\)");
		  switch (cmd[0]) {
		      case "put_k":
			      if(cmd.length==2){
				arg=cmd[1].split("\\,");
				if(arg.length!=3){
					res="Argumentos errados";
					break;
			      }}else{res="Argumentos errados";break;}
			      this.userKey=arg[2];
			      res=put_k(arg[0], arg[1], arg[2]);
			      break;
			      
		      case "get":
			      if(cmd.length==2){
				arg=cmd[1].split("\\,");
				if(arg.length!=1){
					res="Argumentos errados";
					break;
			      }}else{res="Argumentos errados";break;}
			      res=get(cmd[1]);
			      break;
			      
		      case "put_h":
			      if(cmd.length==2){
			      arg=cmd[1].split("\\,");
			      if(arg.length!=1){
				      res="Argumentos errados";
				      break;
			      }}else{res="Argumentos errados";break;}
			      break;
			      
		      case "storePubKey":
			      res=store(cmd[1]);
			      break;
			      
		      case "readPubKeys":
			      res=read();
			      break;
			      
		      default:
			      res="Comando errado";
			      break;
		  }
            
            
            System.out.println("For the client:"+res);
	    p.println(res);   //envia para cli
        
	}		
	sc.close();
	p.close();
	ss.close();
      } catch (IOException e) {
        System.out.println(e);
      }
      
   }
}
