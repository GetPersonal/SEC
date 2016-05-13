import java.security.PublicKey;


public class TestBizantine {
	public static void main(String[] args) throws Exception {

	//Requires BIzantine Server Online
   
  
		
		
		System.out.println("*********************** Normal ID from bizantine");
		
		FileSystem fs = new FileSystem();
		String id = fs.FS_init();
	
		PublicKey[] keys = fs.FS_List();
		PublicKey key = keys[0];
		
		String test = "qwerty";
		byte[] testFile = test.getBytes();
		System.out.println(test);
		fs.FS_write(0, testFile.length, testFile);
		
		System.out.println("My ID: " + id);

		
		
		System.out.println("*********************** Null ID from bizantine");
		
		String id2 = fs.FS_init();
		System.out.println("My ID: " + id2);
		
		System.out.println("*********************** Null Wrong ID from bizantine");
		
		String id3 = fs.FS_init();
		System.out.println("My ID: " + id3);

		
		
		
		System.out.println("*********************** read normal from bizantine");
		
		byte[] testRead = new byte[10];
		int r = fs.FS_read(key, 0, 10, testRead);
		String test2 = new String(testRead);
		System.out.println(test2);
		System.out.println("Bytes read: " + r);
		
		System.out.println("*********************** read old from bizantine");
		
		 r = fs.FS_read(key, 0, 10, testRead);
		 test2 = new String(testRead);
		
		
		
		
		
		
	}
}
