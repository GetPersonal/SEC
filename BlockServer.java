import java.util.HashMap;
import java.util.ArrayList;
import java.util.Random;
import java.security.MessageDigest;
import java.security.Signature;
import java.security.PublicKey;
//import java.util.Base64;
import javax.xml.bind.DatatypeConverter;
import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;

public class BlockServer extends UnicastRemoteObject implements IBlockServer {

	private class KeyBlock {
		PublicKey key;
		byte[] signature;
		byte[] block;
		private Integer writeTS;
		public KeyBlock(PublicKey key, byte[] sig, byte[] data, int wts) {
			this.key = key;
			this.signature = sig;
			this.block = data;
			this.writeTS = this.writeTS.valueOf(wts);
		}
	}
	private HashMap<String, byte[]> hashBlocks;
	private HashMap<String, KeyBlock> keyBlocks;
	private ArrayList<PublicKey> keys;
	
		
	public BlockServer() throws RemoteException {
		super();
		hashBlocks = new HashMap<String, byte[]>();
		keyBlocks = new HashMap<String, KeyBlock>();
		keys = new ArrayList<PublicKey>();
	}

	public byte[] get(String id) throws Exception {
		if(hashBlocks.containsKey(id)) {
			byte[] block = hashBlocks.get(id);
			
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
			messageDigest.update(block);
			byte[] digest = messageDigest.digest();
			String blockId = DatatypeConverter.printBase64Binary(digest);
			
			if(id.equals(blockId))
				return block;
			else
				return null;
		}
		else {
			if(keyBlocks.containsKey(id)) {
				KeyBlock block = keyBlocks.get(id);
				
				MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
				messageDigest.update(block.key.toString().getBytes());
				byte[] digest = messageDigest.digest();
				String keyId = DatatypeConverter.printBase64Binary(digest);
				
				if(id.equals(keyId)) {
					Signature sig = Signature.getInstance("SHA256withRSA");
					sig.initVerify(block.key);
					sig.update(block.block);
					boolean result = sig.verify(block.signature);
					if(result) {
						return block.block;
					}
				}
			}
		}
		return null;
	}

	public String put_k(byte wts, byte[] data, byte[] signature, PublicKey pubKey) throws Exception {
		System.out.println("Recieved key block");
		MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
		messageDigest.update(pubKey.toString().getBytes());
		byte[] digest = messageDigest.digest();
		String id = DatatypeConverter.printBase64Binary(digest);
		
		KeyBlock blk= keyBlocks.get(id);
		
		
		if(blk!=null){
			if (wts> blk.writeTS.intValue()){
						blk.key = pubKey;
						blk.signature = signature;
						blk.block = data;
						blk.writeTS= blk.writeTS.valueOf(wts);
						
						System.out.println("Stored key block - ID: " + id);
						return id;
					//}
			}
		}else{
			KeyBlock block = new KeyBlock(pubKey, signature, data, wts);
			keyBlocks.put(id, block);
			System.out.println("Stored key block - ID: " + id);
			return id;			
		}
		return null;
	}

	public String put_h(byte[] data) throws Exception {
		System.out.println("Recieved hash block");
		MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
		messageDigest.update(data);
		byte[] digest = messageDigest.digest();
		String id = DatatypeConverter.printBase64Binary(digest);
		
		hashBlocks.put(id, data);
		System.out.println("Stored hash block - ID: " + id);
		return id;
	}
	
	public boolean storePubKey(PublicKey key) throws Exception {
		boolean result = keys.add(key);
		return result;
	}
	
	public PublicKey[] readPubKeys() throws Exception {
		PublicKey[] keyArray = new PublicKey[keys.size()];
		keyArray = keys.toArray(keyArray);
		return keyArray;
	}

	public static void main(String[] args) throws Exception {
		BlockServer server = new BlockServer();
		Registry reg = null;
		try{
		  reg = LocateRegistry.createRegistry(55555);
		 		
		}catch (Exception e){
		  System.out.println("Locating registry");
		   reg= LocateRegistry.getRegistry(55555);
		}

		
	System.out.println("BlockServer is ready");
        reg.rebind("BlockServer" + args[0], server);
		//LocateRegistry.createRegistry(55555);
		//Naming.bind("BlockServer", server);
	}
}
