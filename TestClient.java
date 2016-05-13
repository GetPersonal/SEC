import java.security.PublicKey;


public class TestClient {
	public static void main(String[] args) throws Exception {
		FileSystem fs = new FileSystem();
		String id = fs.FS_init();
		System.out.println("My ID: " + id);
		
		PublicKey[] keys = fs.FS_List();
		PublicKey key = keys[0];
		
		String test = "qwerty";
		byte[] testFile = test.getBytes();
		System.out.println(test);
		fs.FS_write(0, testFile.length, testFile);
		
		String test3 = "0123456789";
		byte[] testFile3 = test3.getBytes();
		System.out.println(test3);
		fs.FS_write(995, testFile3.length, testFile3);
		
		byte[] testRead = new byte[1000];
		int r = fs.FS_read(key, 0, 1000, testRead);
		String test2 = new String(testRead);
		System.out.println(test2);
		System.out.println("Bytes read: " + r);
		
		byte[] testRead0 = new byte[10];
		int r0 = fs.FS_read(key, 995, 10, testRead0);
		String test0 = new String(testRead0);
		System.out.println(test0);
		System.out.println("Bytes read: " + r0);
		
		
	}
}
