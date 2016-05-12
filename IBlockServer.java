import java.rmi.*;
import java.security.PublicKey;

public interface IBlockServer extends java.rmi.Remote {
	public byte[] get(String id) throws Exception;
	public String put_k(byte wts, byte[] data, byte[] signature, PublicKey pubKey) throws Exception;
	public String put_h(byte[] data) throws Exception;
	public boolean storePubKey(PublicKey key) throws Exception;
	public PublicKey[] readPubKeys() throws Exception;
}
