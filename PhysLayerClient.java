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
			System.out.println("Connected to: " + socket.getInetAddress() + ":" + socket.getPort() + "\n");
			
			OutputStream os = socket.getOutputStream();
			InputStream is = socket.getInputStream();
			
			double baseline = 0;
			for (int i = 0; i < 64; ++i){
				baseline += is.read();
			}
			System.out.println("Baseline: " + (baseline /= 64));
			
			get5BNRZI(is, bitStorage);
			for (int i = 0; i < 512; ++i){
				System.out.println(i + ": " + (bitStorage[i] & 0x1F));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void get5BNRZI(InputStream is, byte[] bitStorage) throws IOException {
		byte receiver = 0, hold = 0;
		int arrSlot = 0;
		
		for (int i = 0; i < 64; ++i){ //320/5 = 64
			receiver = (byte) is.read();
			//1
			bitStorage[arrSlot++] = (byte) (receiver >> 3);
//			System.out.println(" 0: " + Integer.toBinaryString(bitStorage[0] & 0x1F));
			receiver <<= 5;
			receiver >>= 5; //clear stored left 5 bits, 3left
			hold = receiver; // hold 3 bits
			hold <<= 2;
			
			receiver = (byte) is.read();
			hold = (byte) (hold | (receiver >> 6)); // give hold 2 bits
			receiver <<= 2;
			receiver >>= 2;	// 6 bits left
			
			//2
			bitStorage[arrSlot++] = hold;
//			System.out.println("1: " + Integer.toBinaryString(bitStorage[1] & 0x1F));
			//3
			bitStorage[arrSlot++] = (byte) (receiver >> 1);
//			System.out.println("2: " + Integer.toBinaryString(bitStorage[2] & 0x1F));
			receiver <<= 7;
			receiver >>= 7; // 1 bit left
			hold = receiver; // has 1 bit
			hold <<= 4;
			receiver = (byte) is.read();
			hold = (byte) (hold | (receiver >> 4)); // give hold 1bit
			//4
			bitStorage[arrSlot++] = hold; 
			receiver <<= 4;
			receiver >>= 4;
			
			hold = receiver; //4 bit hold
			receiver = (byte) is.read();
			hold <<= 1;
			hold = (byte) (hold | receiver >> 7); //give hold 1 bit
			receiver <<= 1;
			receiver >>= 1; // receiver has 7 bits
			//5
			bitStorage[arrSlot++] = hold;
			//6
			bitStorage[arrSlot++] = (byte) (receiver >> 2);
			receiver <<= 6;
			receiver >>= 6;
			
			hold = receiver; // has 2 bits
			receiver = (byte) is.read();
			hold <<= 3;
			hold = (byte) (hold | (receiver >> 5));
			//7 
			bitStorage[arrSlot++] = hold;
			receiver <<= 3;
			receiver >>= 3;
			//8
			bitStorage[arrSlot++] = receiver;
			
		}
	}
/*	public static void get5BNRZI(InputStream is, byte[] bitStorage) throws IOException {
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
				digits -= 5;
			}
		}
	}*/
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

