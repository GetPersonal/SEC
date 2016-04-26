import java.rmi.*;
import java.rmi.Naming;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.util.Random;
import java.security.*;
import javax.xml.bind.DatatypeConverter;
import java.util.Arrays;
import java.util.TreeMap;
import java.util.ArrayList;
import java.lang.Integer;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;

/*import pteidlib.PTEID_Certif;
import pteidlib.PTEID_ID;
import pteidlib.PTEID_PIC;
import pteidlib.PTEID_Pin;
import pteidlib.PTEID_TokenInfo;
import pteidlib.PteidException;
import pteidlib.pteid;*/

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

	private IBlockServer server1, server2, server3;

	private PrivateKey privKey;
	private PublicKey pubKey;
	private String myId;
	private TreeMap<Integer, String> idMap;
	private final static int BLOCK_SIZE = 1000;
	
	private void UpdateKeyBlock() throws Exception {
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(byteOut);
		out.writeObject(idMap);
		byte[] block = byteOut.toByteArray();
		
		Signature sig = Signature.getInstance("SHA256withRSA");
		sig.initSign(privKey);
		sig.update(block);
		byte[] signature = sig.sign();
		
		/*pteid.Init("");
		pteid.SetSODChecking(false); // Don't check the integrity of the ID, address and photo (!)
		
		System.out.println("Insert auth pin to update key block");
		PKCS11 pkcs11;
		String libName = "libpteidpkcs11.so";
		Class pkcs11Class = Class.forName("sun.security.pkcs11.wrapper.PKCS11");
		Method getInstanceMethode = pkcs11Class.getDeclaredMethod("getInstance", new Class[] { String.class, String.class, CK_C_INITIALIZE_ARGS.class, boolean.class });
		pkcs11 = (PKCS11)getInstanceMethode.invoke(null, new Object[] { libName, "C_GetFunctionList", null, false });
		long p11_session = pkcs11.C_OpenSession(0, PKCS11Constants.CKF_SERIAL_SESSION, null, null);
		pkcs11.C_Login(p11_session, 1, null);
		CK_SESSION_INFO info = pkcs11.C_GetSessionInfo(p11_session);
		CK_ATTRIBUTE[] attributes = new CK_ATTRIBUTE[1];
		attributes[0] = new CK_ATTRIBUTE();
		attributes[0].type = PKCS11Constants.CKA_CLASS;
		attributes[0].pValue = new Long(PKCS11Constants.CKO_PRIVATE_KEY);
		pkcs11.C_FindObjectsInit(p11_session, attributes);
		long[] keyHandles = pkcs11.C_FindObjects(p11_session, 5);
		long signatureKey = keyHandles[0];		//auth key
		pkcs11.C_FindObjectsFinal(p11_session);
		CK_MECHANISM mechanism = new CK_MECHANISM();
		mechanism.mechanism = PKCS11Constants.CKM_SHA1_RSA_PKCS;
		mechanism.pParameter = null;
		pkcs11.C_SignInit(p11_session, mechanism, signatureKey);
		byte[] signature = pkcs11.C_Sign(p11_session, block);
		
		pteid.Exit(pteid.PTEID_EXIT_LEAVE_CARD);*/
		
		try {
			myId = server1.put_k(block, signature, pubKey);
			/*******************************//*******************************/
			myId = server2.put_k(block, signature, pubKey);
			myId = server3.put_k(block, signature, pubKey);
			/*******************************//*******************************/
		}
		catch(Exception e) {
			System.out.println("Something happened");
			e.printStackTrace();
		}
		
	}

	public String FS_init() throws Exception {
		Registry reg = LocateRegistry.getRegistry(55555);
		server1 = (IBlockServer) reg.lookup("BlockServer1");
		server2 = (IBlockServer) reg.lookup("BlockServer2");
		server3 = (IBlockServer) reg.lookup("BlockServer3");
		System.out.println("1 BlockServer found");
		/*******************************//*******************************/
		SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
		KeyPairGenerator keygen = KeyPairGenerator.getInstance("RSA");
		keygen.initialize(1024, random);
		KeyPair pair = keygen.generateKeyPair();
		privKey = pair.getPrivate();
		pubKey = pair.getPublic();
		
		
		/*System.loadLibrary("pteidlibj");
		pteid.Init(""); // Initializes the eID Lib
		pteid.SetSODChecking(false); // Don't check the integrity of the ID, address and photo (!)
		byte[] certificate_bytes = null;
		PTEID_Certif[] certs = pteid.GetCertificates();
		certificate_bytes = certs[0].certif;
		CertificateFactory f = CertificateFactory.getInstance("X.509");
		InputStream in = new ByteArrayInputStream(certificate_bytes);
		X509Certificate cert = (X509Certificate)f.generateCertificate(in);
		pteid.Exit(pteid.PTEID_EXIT_LEAVE_CARD);
		
		pubKey = cert.getPublicKey();*/
		server1.storePubKey(pubKey);
		/*******************************//*******************************/
		server2.storePubKey(pubKey);
		server3.storePubKey(pubKey);
		/*******************************//*******************************/
		
		idMap = new TreeMap<Integer, String>();
		
		UpdateKeyBlock();
		
		//idMap.put(0, myId);
		return myId;
	}
	
	public PublicKey[] FS_List() {
		PublicKey[] keys = null;
		try {
			keys = server1.readPubKeys();
			/*******************************//*******************************/
			keys = server2.readPubKeys();
			keys = server3.readPubKeys();
			/*******************************//*******************************/
		}
		catch(Exception e) {
			System.out.println("Something happened");
			e.printStackTrace();
		}
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
				
				try {
					id = server1.put_h(block);
					/*******************************//*******************************/
					id = server2.put_h(block);
					id = server3.put_h(block);
					/*******************************//*******************************/
					idMap.put(new Integer(i), id);
					//UpdateKeyBlock();
				}
				catch(Exception e) {
					System.out.println("Something happened");
					e.printStackTrace();
				}
			}
			else { //block exists
				try {
					block = server1.get(id);
					/*******************************//*******************************/
					block = server2.get(id);
					block = server3.get(id);
					/*******************************//*******************************/
				}
				catch(Exception e) {
					System.out.println("Something happened");
					e.printStackTrace();
				}
				
				if(block == null) {
					System.out.println("Error: Block mismatch");
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
					
					try {
						id = server1.put_h(block);
						/*******************************//*******************************/
						id = server2.put_h(block);
						id = server3.put_h(block);
						/*******************************//*******************************/
						idMap.put(new Integer(i), id);
						//UpdateKeyBlock();
					}
					catch(Exception e) {
						System.out.println("Something happened");
						e.printStackTrace();
					}
					
				}
			}
		}
		
		for(int i = 0; i < index; ++i) { //padding with zeros
			String id = idMap.get(new Integer(i));
			if(id.equals(null)) {
				byte[] block = new byte[BLOCK_SIZE];
				try {
					id = server1.put_h(block);
					/*******************************//*******************************/
					id = server2.put_h(block);
					id = server3.put_h(block);
					/*******************************//*******************************/
					idMap.put(new Integer(i), id);
					//UpdateKeyBlock();
				}
				catch(Exception e) {
					System.out.println("Something happened");
					e.printStackTrace();
				}
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
		int bytesRead = 0;
		int index = pos / BLOCK_SIZE;
		int last = (pos + size) / BLOCK_SIZE;
		int startPos, endPos;
		byte[] block = null;
		
		MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
		messageDigest.update(key.toString().getBytes());
        byte[] digest = messageDigest.digest();
		String id = DatatypeConverter.printBase64Binary(digest);
		
		try {
			block = server1.get(id);
			/*******************************//*******************************/
			block = server2.get(id);
			block = server3.get(id);
			/*******************************//*******************************/
		}
		catch(Exception e) {
			System.out.println("Something happened");
			e.printStackTrace();
			return 0;
		}
		
		if(block == null) {
			System.out.println("Error: Block mismatch");
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
					try {
						block = server1.get(id2);
						/*******************************//*******************************/
						block = server2.get(id2);
						block = server3.get(id2);
						/*******************************//*******************************/
					}
					catch(Exception e) {
						System.out.println("Something happened");
						e.printStackTrace();
					}
				
					if(block == null) {
						System.out.println("Error: Block mismatch");
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
