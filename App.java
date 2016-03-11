
import Pack.*;
import java.io.*;
import java.net.*;
import java.util.Scanner;


public class App{

private static String Id;
  
    public static void main (String args[]) throws IOException{
    String input="",res, PrivKey;
    String[] cmd, arg;
    Scanner sc=new Scanner(System.in).useDelimiter("\n");           //user input  
    //Inicializar Client
    Client c= new Client();
    
    while( ! input.equals("exit")){
      System.out.println("Welcome! Please start with the FS_init() command");
      input=sc.next();          //msg do user
      if(input.equals("FS_init()")){
	  Id=c.FS_init();
	  System.out.println("Your ID: "+ Id);
	  System.out.print("Secure communication started!");
	  break;
	      }
    }
    
    if(!input.equals("exit")){
    
	while(true){
		System.out.println("Waiting for your command...");
		input=sc.next();          //msg do user    
		if(input.equals("exit")){
			c.exit();
			break;}
		cmd =input.split("\\(|\\)");
		if(cmd.length==2){
			arg =cmd[1].split("\\,");
			switch (cmd[0]) {			  
			    case "FS_write":
				    if(arg.length==3){
				    c.FS_write(arg[0],arg[1],arg[2]);
				    }else{System.out.println("Wrong number of arguments, please try again");}
				    break;					
			    case "FS_read":
				    if(arg.length==3){
				    System.out.println(c.FS_read(arg[0],arg[1], arg[2]));
				    }else{System.out.println("Wrong number of arguments, please try again");}
				    break;
			    default:
				    System.out.println("Wrong Command, please try again");
				    break;
			}
		}else{System.out.print("Wrong Command, please try again");}
	    
	  
	  }
	
    }  
    sc.close();
}}
