import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.Base64;
import java.security.*;
public class Server implements Runnable {
  
Socket ss;
PrintStream printer;
   
    Server(Socket csocket) {
      this.ss = csocket;
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
	  return line;
	}
      }
      reader.close();
      return "User does not exist!";
    
    }else{return "Nao existe ficheiro de bloco de dados";}
      
  
  } 
  
    public void put_k (String id)throws IOException{
  
  
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
	
	String msg="", res="", data, sign, pubk;
	String[] cmd;
	while( !msg.equals("exit")){
	  
	  msg=sc.next();    //retirar msg recebida
	  if (msg.equals("exit")){
	    System.out.println("********* Sessao Terminada. Obrigado! *******");
	    break;
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
