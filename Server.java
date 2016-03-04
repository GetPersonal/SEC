import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Server implements Runnable {
  
Socket ss;
   
    Server(Socket csocket) {
      this.ss = csocket;
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
	String msg="", res, line;
	String[] cmd;
	while( !msg.equals("-1")){
	  
	  msg=sc.next();    //retirar msg recebida
	  if (msg.equals("-1")){
	    res="********* Sessao Terminada. Obrigado! *******";
	  }else{
	    res= msg+" CHECKED"+'\n' ;
	  }
    
	
	    cmd =msg.split("\\(");
	    switch (cmd[0]) {
		case "write":  
			//ESCREVE NO FICHEIRO (cria/continua à frente)
			FileOutputStream outputStream = new FileOutputStream("MyFile.txt", true);
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
			BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);            
			bufferedWriter.write(res);
			bufferedWriter.close();
			break;
			
		case "read":  
			if (! new File("MyFile.txt").exists() ){      //VÊ SE O FICHEIRO EXISTE
			    System.out.println("Nao existe ficheiro");
			    break;
			}
			//LÊ O FICHEIRO
			FileReader reader = new FileReader("MyFile.txt");
			BufferedReader bufferedReader = new BufferedReader(reader);
			res="";
			while ((line = bufferedReader.readLine()) != null) {
				res=res+line+'\n';
			}
			reader.close();
			break;
            }    
            
            System.out.println(res);
         p.println(res);   //envia para cli
         
         
		/*//ESCREVE NO FICHEIRO (cria/continua à frente)
		FileOutputStream outputStream = new FileOutputStream("MyFile.txt", true);
		OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
		BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);            
		bufferedWriter.write(res + '\n');
		bufferedWriter.close();*/
		
		
		/*//LÊ O FICHEIRO
		FileReader reader = new FileReader("MyFile.txt");
		BufferedReader bufferedReader = new BufferedReader(reader);
		while ((line = bufferedReader.readLine()) != null) {
			System.out.println(line);
		}
		reader.close();*/
		
		
	}		
	sc.close();
	p.close();
	ss.close();
      } catch (IOException e) {
        System.out.println(e);
      }
      
   }
}
