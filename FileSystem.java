import java.rmi.*;
import java.rmi.Naming;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.security.*;
import javax.xml.bind.DatatypeConverter;
import java.util.*;
import java.lang.Integer;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;

import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import sun.security.pkcs11.wrapper.CK_ATTRIBUTE;
import sun.security.pkcs11.wrapper.CK_C_INITIALIZE_ARGS;
import sun.security.pkcs11.wrapper.CK_MECHANISM;
import sun.security.pkcs11.wrapper.CK_SESSION_INFO;
import sun.security.pkcs11.wrapper.PKCS11;
import sun.security.pkcs11.wrapper.PKCS11Constants;

public class FileSystem {

	private IBlockServer server1, server2, server3, server4;

	private PrivateKey privKey;
	private PublicKey pubKey;
	private String myId;
	private TreeMap<Integer, String> idMap;
	private final static int BLOCK_SIZE = 1000;
	
	private Integer writeTS= new Integer(0);
	private Integer readTS= new Integer(0);
	private ArrayList<String> ackList= new ArrayList<String>(); 
	private ArrayList<String> readList= new ArrayList<String>();

	private void initACK(ArrayList<String> array){
		for(int i=0;i<4;i++){
			array.add(i,"");
		}		
	}
	
	private int counterACK(ArrayList<String> array){
		int result=0;
		for(int i=0;i<4;i++){
			if(ackList.get(i).equals("ACK")){
				result++;
			}
		}
		return result;
	}
	
	
	
	
	private void UpdateKeyBlock() throws Exception {
		writeTS=writeTS.valueOf(writeTS.intValue()+1);
		initACK(ackList);
		//ackList = [false, false, false];
		
		
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(byteOut);
		out.writeObject(idMap);
		byte[] block = byteOut.toByteArray();
		
		Signature sig = Signature.getInstance("SHA256withRSA");
		sig.initSign(privKey);
		
		sig.update(block);
		//sig.update(writeTS.byteValue());
		
		byte[] signature = sig.sign();
		

			/*******************************//*******************************/
			String auxID;
			try {
				auxID = server1.put_k(writeTS.byteValue(), block, signature, pubKey);
			}catch(Exception e) {auxID=null;}
			if(auxID!=null){ackList.add(0,"ACK");}
			
			try {
				auxID = server2.put_k(writeTS.byteValue(), block, signature, pubKey);
			}catch(Exception e) {auxID=null;}
			if(auxID!=null){ackList.add(1,"ACK");}
			
			try {
				auxID = server3.put_k(writeTS.byteValue(), block, signature, pubKey);
			}catch(Exception e) {auxID=null;}
			if(auxID!=null){ackList.add(2,"ACK");}
			
			if(counterACK(ackList) > (4+1)/2){ //// se 3 estiverem correctos termina
				auxID = server4.put_k(writeTS.byteValue(), block, signature, pubKey); /////RETIRAAAAAAAAAAAAAAAR
				myId= auxID;
				initACK(ackList);
			}else{
				try {
					auxID = server4.put_k(writeTS.byteValue(), block, signature, pubKey);
				}catch(Exception e) {auxID=null;}
				if(auxID!=null){ackList.add(3,"ACK");}
				if(counterACK(ackList) > (4+1)/2){ //// se 3 estiverem correctos em 4 termina
					myId= auxID;
					initACK(ackList);
				}else{
					myId= null; 					//// não estão suficientes correctos
				}
			}
			
			
			
			/*******************************//*******************************/
			
			
			
			
			
		
		
	}

	public String FS_init() throws Exception {
		Registry reg = LocateRegistry.getRegistry(55555);
		server1 = (IBlockServer) reg.lookup("BlockServer1");
		server2 = (IBlockServer) reg.lookup("BlockServer2");
		server3 = (IBlockServer) reg.lookup("BlockServer3");
		server4 = (IBlockServer) reg.lookup("BlockServer4");
		System.out.println("1 BlockServer found");
		/*******************************//*******************************/
		SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
		KeyPairGenerator keygen = KeyPairGenerator.getInstance("RSA");
		keygen.initialize(1024, random);
		KeyPair pair = keygen.generateKeyPair();
		privKey = pair.getPrivate();
		pubKey = pair.getPublic();
		
		/*******************************//*******************************/
		try {
			server1.storePubKey(pubKey);
		}catch(Exception e) {System.out.println("Server Down!");}
		try {
			server2.storePubKey(pubKey);
		}catch(Exception e) {System.out.println("Server Down!");}
		try {
			server3.storePubKey(pubKey);
		}catch(Exception e) {System.out.println("Server Down!");}
		try {
			server4.storePubKey(pubKey);
		}catch(Exception e) {System.out.println("Server Down!");}
		/*******************************//*******************************/
		
		idMap = new TreeMap<Integer, String>();
		
		UpdateKeyBlock();
		
		return myId;
	}
	
	public PublicKey[] FS_List() {
		PublicKey[] keys = null;
			/*******************************//*******************************/
			try {
				keys = server1.readPubKeys();
			}catch(Exception e) {System.out.println("Server Down!");}
			try {
				keys = server2.readPubKeys();
			}catch(Exception e) {System.out.println("Server Down!");}
			try {
				keys = server3.readPubKeys();
			}catch(Exception e) {System.out.println("Server Down!");}
			try {
				keys = server4.readPubKeys();
			}catch(Exception e) {System.out.println("Server Down!");}
			/*******************************//*******************************/
		
		return keys;
	}
	
	public void FS_write(int pos, int size, byte[] contents) throws Exception {
			
		int index = pos / BLOCK_SIZE;
		int last = (pos + size) / BLOCK_SIZE;
		int startPos, endPos;
		
		for(int i = index; i <= last; ++i) {
			byte[] block = null;
			String id = idMap.get(new Integer(i));
			
			if(id == null) { //no block yet
				block = new byte[BLOCK_SIZE];
				if(i == index) {
					startPos = pos % BLOCK_SIZE;
				}
				else {
					startPos = 0;
				}
				if(i == last) {
					endPos = (pos + size) % BLOCK_SIZE;
				}
				else {
					endPos = BLOCK_SIZE - 1;
				}
				System.arraycopy(contents,0, block, startPos, endPos - startPos);
				
				
					/*******************************//*******************************/
					try {
						id = server1.put_h(block);
					}catch(Exception e) {System.out.println("Server Down!");}
					try {
						id = server2.put_h(block);
					}catch(Exception e) {System.out.println("Server Down!");}
					try {
						id = server3.put_h(block);
					}catch(Exception e) {System.out.println("Server Down!");}
					try {
						id = server4.put_h(block);
					}catch(Exception e) {System.out.println("Server Down!");}
					/*******************************//*******************************/
					idMap.put(new Integer(i), id);
				
			}
			else { //block exists
					MessageType mt;
					/*******************************//*******************************/
					try {
						mt= server1.get(id, readTS);
						block=mt.getData();
						//block = server1.get(id);
					}catch(Exception e) {System.out.println("Server Down!");}
					try {
						mt= server2.get(id, readTS);
						block=mt.getData();
						//block = server2.get(id);
					}catch(Exception e) {System.out.println("Server Down!");}
					try {
						mt= server3.get(id, readTS);
						block=mt.getData();
						//block = server3.get(id);
					}catch(Exception e) {System.out.println("Server Down!");}
					try {
						mt= server4.get(id, readTS);
						block=mt.getData();
						//block = server4.get(id);
					}catch(Exception e) {System.out.println("Server Down!");}
					/*******************************//*******************************/
				
				
				if(block == null) {
					System.out.println("Error:2 Block mismatch");
					return;
				}
				else {
					if(i == index) {
						startPos = pos % BLOCK_SIZE;
					}
					else {
						startPos = 0;
					}
					if(i == last) {
						endPos = (pos + size) % BLOCK_SIZE;
					}
					else {
						endPos = BLOCK_SIZE;
					}
					System.arraycopy(contents, 0, block, startPos, endPos - startPos);
					
						/*******************************//*******************************/
						try {
							id = server1.put_h(block);
						}catch(Exception e) {System.out.println("Server Down!");}
						try {
							id = server2.put_h(block);
						}catch(Exception e) {System.out.println("Server Down!");}
						try {
							id = server3.put_h(block);
						}catch(Exception e) {System.out.println("Server Down!");}
						try {
							id = server4.put_h(block);
						}catch(Exception e) {System.out.println("Server Down!");}
						/*******************************//*******************************/
						idMap.put(new Integer(i), id);
					
					
				}
			}
		}
		
		for(int i = 0; i < index; ++i) { //padding with zeros
			String id = idMap.get(new Integer(i));
			if(id.equals(null)) {
				byte[] block = new byte[BLOCK_SIZE];
					/*******************************//*******************************/
					try {
						id = server1.put_h(block);
					}catch(Exception e) {System.out.println("Server Down!");}
					try {
						id = server2.put_h(block);
					}catch(Exception e) {System.out.println("Server Down!");}
					try {
						id = server3.put_h(block);
					}catch(Exception e) {System.out.println("Server Down!");}
					try {
						id = server4.put_h(block);
					}catch(Exception e) {System.out.println("Server Down!");}
					/*******************************//*******************************/
					idMap.put(new Integer(i), id);
				
			}
		}
		
		try {
			UpdateKeyBlock();
		}
		catch(Exception e) {
			System.out.println("Something happened");
			e.printStackTrace();
		}
		
		
	}
	
	public int FS_read(PublicKey key, int pos, int size, byte[] contents) throws Exception {
		
		readTS=readTS.valueOf(readTS.intValue()+1);
		initACK(readList);

		
		int bytesRead = 0;
		int index = pos / BLOCK_SIZE;
		int last = (pos + size) / BLOCK_SIZE;
		int startPos, endPos;
		byte[] block = null;
		
		MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
		messageDigest.update(key.toString().getBytes());
        byte[] digest = messageDigest.digest();
		String id = DatatypeConverter.printBase64Binary(digest);

			MessageType mt;
			/*******************************//*******************************/
			try {
				mt= server1.get(id, readTS);
				block=mt.getData();
				//block = server1.get(id);
			}catch(Exception e) {e.printStackTrace();System.out.println("Server Down!");}
			try {
				mt= server2.get(id, readTS);
				block=mt.getData();
				//block = server2.get(id);
			}catch(Exception e) {System.out.println("Server Down!");}
			try {
				mt= server3.get(id, readTS);
				block=mt.getData();
				//block = server3.get(id);
			}catch(Exception e) {System.out.println("Server Down!");}
			try {
				mt= server4.get(id, readTS);
				block=mt.getData();
				//block = server4.get(id);
			}catch(Exception e) {System.out.println("Server Down!");}
			/*******************************//*******************************/
	
		
		if(block == null) {
			System.out.println("Error:3 Block mismatch");
			return 0;
		}
		else {
			ByteArrayInputStream byteIn = new ByteArrayInputStream(block);
			ObjectInputStream in = new ObjectInputStream(byteIn);
			TreeMap<Integer, String> map = (TreeMap<Integer, String>) in.readObject();
			
			for(int i = index; i <= last; ++i) {
				block = null;
				String id2 = map.get(new Integer(i));
				
				if(id2 != null) {

						/*******************************//*******************************/
						try {
							mt= server1.get(id2, readTS);
							block=mt.getData();
							//block = server1.get(id2);
						}catch(Exception e) {System.out.println("Server Down!");}
						try {
							mt= server2.get(id2, readTS);
							block=mt.getData();
							//block = server2.get(id2);
						}catch(Exception e) {System.out.println("Server Down!");}
						try {
							mt= server3.get(id2, readTS);
							block=mt.getData();
							//block = server3.get(id2);
						}catch(Exception e) {System.out.println("Server Down!");}
						try {
							mt= server4.get(id2, readTS);
							block=mt.getData();
							//block = server4.get(id2);
						}catch(Exception e) {System.out.println("Server Down!");}
						/*******************************//*******************************/

				
					if(block == null) {
						System.out.println("Error:1 Block mismatch");
						return bytesRead;
					}
					else {
						if(i == index) {
							startPos = pos % BLOCK_SIZE;
						}
						else {
							startPos = 0;
						}
						if(i == last) {
							endPos = (pos + size) % BLOCK_SIZE;
						}
						else {
							endPos = BLOCK_SIZE;
						}
						System.arraycopy(block, startPos, contents, startPos + (BLOCK_SIZE * i), endPos - startPos);
						bytesRead += endPos - startPos;
					}
				}
			}
		}
		return bytesRead;
	}

}
