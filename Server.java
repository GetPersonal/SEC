import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.security.*;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class Server implements Runnable {
  
Socket ss;
PrintStream printer;
   
    Server(Socket csocket) {
      this.ss = csocket;
   }
   
   
   public String FS_init ()throws IOException{
	try{	
				      KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
				      kpg.initialize(512);
				      
				      KeyPair kp = kpg.genKeyPair();
				      Key publicKey = kp.getPublic();
				      Key privateKey = kp.getPrivate();
				      BASE64Decoder decoder = new BASE64Decoder();
				      BASE64Encoder encoder = new BASE64Encoder();
				      String encodedBytesPub = encoder.encodeBuffer(publicKey.getEncoded());
				      String encodedBytesPriv = encoder.encodeBuffer(privateKey.getEncoded());
				      printer.println(encodedBytesPriv.replace("\n", ""));
				      
				      
				/* Escreve keys no ficheiro      
				      
				      FileOutputStream outputStream = new FileOutputStream("Keys.txt", true);
				      OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
				      BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
				      String msg = encodedBytesPriv + "|" + encodedBytesPub; 
				      msg=msg.replace("\n", "");
				      bufferedWriter.write(msg +'\n');
				      bufferedWriter.close();
				 */
			      
			       /*procura a PubKey e retorna PrivKey
			       
					if (new File("Keys.txt").exists() ){      //VÊ SE O FICHEIRO EXISTE
					      FileReader reader = new FileReader("Keys.txt");
					      BufferedReader bufferedReader = new BufferedReader(reader);
					      String line="";
					      String[] block;
					      while ((line = bufferedReader.readLine()) != null) { //LÊ O FICHEIRO
						block=line.split("\\|");
						if (block[1].equals(encodedBytesPub.replace("\n", ""))){
						  reader.close();
						  return block[0];
						}
					      }
					      reader.close();
					      return "Key nao existe";
					    
					}else{return "Nao existe ficheiro de Keys";}*/
			      
			      
			      //faz o hash da chave publica
			      byte[] decodedBytes = decoder.decodeBuffer(encodedBytesPub);
			      MessageDigest md = MessageDigest.getInstance("SHA-256");
			      md.update(decodedBytes); 
			      byte[] digest = md.digest();
			      String id = encoder.encodeBuffer(digest);
			      //envia o id ao cliente
			      return id;	
			      
			      }catch(NoSuchAlgorithmException NSE){
				      System.out.println("deu merda");
			      }
	 return "-1";
   } 
   
   
   public String FS_write (int pos, int size){
   
	return "";
   }
   public String FS_read (String id, int pos, int size){
	return "";
   }
   
  public String get (String id)throws IOException{
  
    if (new File("Block.txt").exists() ){      //VÊ SE O FICHEIRO EXISTE
      FileReader reader = new FileReader("Block.txt");
      BufferedReader bufferedReader = new BufferedReader(reader);
      String line="";
      String[] block;
      while ((line = bufferedReader.readLine()) != null) { //LÊ O FICHEIRO
	block=line.split("\\|");
	if (block[0].equals(id)){
	  reader.close();
	  return block[1] + '\n';
	}
      }
      reader.close();
      return "Utilizador nao existe";
    
    }else{return "Nao existe ficheiro de bloco de dados";}
      
  
  } 
   
   
  public static void main (String args[]) throws IOException{

    String msg="", res;
    
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
	FS_init();
	
	String msg="", res, data, sign, pubk;
	String[] cmd;
	while( !msg.equals("-1")){
	  
	  msg=sc.next();    //retirar msg recebida
	  if (msg.equals("-1")){
	    res="********* Sessao Terminada. Obrigado! *******";
	  }else{
	    res= msg+" CHECKED"+'\n' ;
	  }
    
	
	    cmd =msg.split("\\(|\\)");
		  switch (cmd[0]) {
		
		      case "put_k":
			      if(cmd.length!=4){
				      res="Comando errado";
				      break;
			      }
			      data= cmd[1];
			      sign= cmd[2];
			      pubk= cmd[3];
			      //ESCREVE NO FICHEIRO (cria/continua à frente)
			      FileOutputStream outputStream = new FileOutputStream("MyFile.txt", true);
			      OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
			      BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);            
			      bufferedWriter.write(res);
			      bufferedWriter.close();
			      break;
			      
		      case "get":
			      if(cmd.length!=2){
				      res="Comando errado";
				      break;
			      }
			      res=get(cmd[1]);
			      break;
			      
		      case "put_h":
			      if(cmd.length!=2){
				      res="Comando errado";
				      break;
			      }
			      break;
			      
		      default:
			      res="Comando errado";
			      break;
		  }
            
            
            System.out.println("RES:"+res);
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
