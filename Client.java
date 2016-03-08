import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client{
  
  public static void main (String args[]) throws IOException{

    String msg="",res, PrivKey;
    Socket s= new Socket("127.0.0.1",1342);
    Scanner sc=new Scanner(System.in).useDelimiter("\n");           //user input  
    PrintStream p= new PrintStream(s.getOutputStream());            // envia po server
    Scanner sc1=new Scanner(s.getInputStream()).useDelimiter("\n");
    
    PrivKey=sc1.nextLine();
    System.out.println("PRIVKEY:"+PrivKey+'\n');
    
    while( !msg.equals("-1")){
      System.out.println("Enter any string to be checked");
      msg=sc.next();          //msg do user
      p.println(msg);         //envia
      res=sc1.next();         //recebe	
      System.out.println(res+'\n');
    }
    s.close();
    sc.close();
    sc1.close();
    p.close();
}}
