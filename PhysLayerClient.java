import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;

public class PhysLayerClient {
	public enum Signal {UP, DOWN}
	boolean start = false;
	Signal sign = Signal.DOWN;
	
	public static void main(String args[]) throws IOException{
		byte[] bitStorage = new byte[512];
		
		try (Socket socket = new Socket("codebank.xyz", 38002)){
//			System.out.println("Connected to: " + socket.getInetAddress() + ":" + socket.getPort() + "\n");
			
			OutputStream os = socket.getOutputStream();
			InputStream is = socket.getInputStream();
			
			double baseline = 0;
			for (int i = 0; i < 64; ++i){
				baseline += is.read();
			}
//			System.out.println("Baseline: " + (baseline /= 64));
			
			get5BNRZI(is, bitStorage);
/*			for (int j = 0; j < bitStorage.length; ++j){
				System.out.println(j + ":" + Integer.toBinaryString(bitStorage[j] & 0x1F));
			}*/
			
//			convert5B4B(bitStorage);
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void get5BNRZI(InputStream is, byte[] bitStorage) throws IOException {
		int digits = 0, arrSlot = 0;
		short fiveBitStorage = 0;
		byte received = 0;
		
		for (int j = 0; j < 5; ++j){	// incoming 320bytes
			received = (byte) is.read();
			fiveBitStorage = (short) (fiveBitStorage | received);
	//		System.out.println(Integer.toBinaryString(fiveBitStorage & 0xFF) + "  " + Integer.toBinaryString(received & 0xFF));
			digits += 8;
			
			while (digits / 5 > 0){	// never more than 4 bits long after loop, shift remainder to left 8 bits
	//			System.out.println("digits : " + digits);
				
				bitStorage[arrSlot++] = (byte) (fiveBitStorage >> (digits - 5));	// get the five bit representation of the byte to be converted
				
	//			System.out.println("storing: " +  Integer.toBinaryString(bitStorage[arrSlot] & 0x1F));
				System.out.println(fiveBitStorage & 0xFF);
				if (fiveBitStorage > 15 && fiveBitStorage < 65536){		// 1 0000 - 0111 1111 1111 1111
					fiveBitStorage <<= (20-(digits));
					System.out.println("in");
				}
				else if (fiveBitStorage < 16 && fiveBitStorage >= 0){	// 0 - 1111  
					fiveBitStorage <<= (15-digits);
					System.out.println("out");
				}
	//			System.out.println("new fbs: " + Integer.toBinaryString(fiveBitStorage & 0x1F));
				digits -= 5;
			}
		}
	}
	/**
	 * convert 5B table to 4B
	 * @param bitStorage
	 */
	public static void convert5B4B(byte[] bitStorage){
/*		byte fiveBitTable[] = {30, 9, 20, 21, 10, 11, 14, 15,
				18, 19, 22, 23, 26, 27, 28, 29};*/
		HashMap<Integer, Integer> fourBitToFiveBit = new HashMap<Integer ,Integer>(){{
			put(30, 0);
			put(9, 1);
			put(20, 2);
			put(21, 3);
			put(10, 4);
			put(11, 5);
			put(14, 6);
			put(15, 7);
			put(18, 8);
			put(19, 9);
			put(22, 10);
			put(23, 11);
			put(26, 12);
			put(27, 13);
			put(28, 14);
			put(29, 15);
		}};
		int hold;
		for (int i = 0; i < bitStorage.length; ++i){
			hold = fourBitToFiveBit.get(bitStorage[i]);
			bitStorage[i] = (byte) hold;
		}
	}
	
}

				
/*				if (fivebitStorage == 0){// gets 2^exponent, 0-7
					digits = 0;
				}
				else{
					digits = (int) Math.round((Math.log10(decoder)/Math.log10(2))); 
				}
				
				if (digits > 3){
					for (int k = digits; k > digits-5; --k){
						
					}
				}*/
				
/*				if (decoder == 0){
					decoder = (byte) is.read();
				}
				else if(decoder > 255){
					decoder = (short) (decoder | is.read());
				}
				else{
//					digits = (int) Math.round((Math.log10(decoder)/Math.log10(2)));
					decoder <<= 8;
				}*/
//				System.out.println("count: " + ++count + " byte: " + temp);

