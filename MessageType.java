import java.security.*;
import java.io.*;

public class MessageType  implements java.io.Serializable{
	PublicKey key;
	byte[] signature;
	byte[] data = null;
	private Integer readTS;
	private Integer writeTS;
		
	public MessageType(Integer rts, PublicKey key, Integer wts, byte[] d, byte[] sig )  {
			this.key = key;
			this.signature = sig;
			this.data = d;
			this.readTS = this.readTS.valueOf(rts);
			this.writeTS = this.writeTS.valueOf(wts);
		}
		
	public MessageType(Integer rts, byte[] d )  {
			this.data = d;
			this.readTS = this.readTS.valueOf(rts);
		}	

		
	public PublicKey getKey(){
		return key;
	}
	public byte[] getSignature(){
		return signature;
	}
	public byte[] getData(){
		return data;
	}
	public Integer getRead(){
		return readTS;
	}
	public Integer getTS(){
		return writeTS;
	}
}