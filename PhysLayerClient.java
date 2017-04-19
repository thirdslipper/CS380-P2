import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;


public class PhysLayerClient {
	public enum Signal {UP, DOWN}
	boolean start = false;
	Signal sign = Signal.DOWN;
	
	public static void main(String args[]) throws IOException{
		byte[] decoded = new byte[512];
		
		try (Socket socket = new Socket("codebank.xyz", 38002)){
			System.out.println("Connected to: " + socket.getInetAddress() + ":" + socket.getPort() + "\n");
			
			OutputStream os = socket.getOutputStream();
			InputStream is = socket.getInputStream();
			
			double baseline = 0;
			//Preamble baseline
			for (int i = 0; i < 64; ++i){
				baseline += is.read();
			}
			System.out.println("Baseline: " + (baseline /= 64));
			
			get5BNRZI(is, decoded);
			for (int j = 0; j < decoded.length; ++j){
				System.out.println(j + ":" + Integer.toHexString(decoded[j] & 0xFF));
			}
			
			convert5B4B(decoded);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void get5BNRZI(InputStream is, byte[] decoded) throws IOException {
		int digits = 0, arrSlot = 0;
		short fiveBitStorage = 0;
		
		for (int j = 0; j < 320; ++j){
			fiveBitStorage = (short) (fiveBitStorage | is.read());
			digits += 8;
			
			while (digits / 5 > 0){	// never more than 4 bits long after loop, shift remainder to left 8 bits
				decoded[arrSlot++] = (byte) (fiveBitStorage >> (digits - 5));	// get the five bit representation of the byte to be converted
				if (fiveBitStorage > 15 && fiveBitStorage < 65536){		// 1 0000 - 0111 1111 1111 1111
					fiveBitStorage <<= (16-(digits-4));
				}
				else if (fiveBitStorage < 16 && fiveBitStorage >= 0){	// 0 - 1111 
					fiveBitStorage <<= (15-digits);
				}
				digits -= 5;
			}
		}
	}
	public static void convert5B4B(byte[] decoded){
		byte fiveBitTable[] = {30, 9, 20, 21, 10, 11, 14, 15,
				18, 19, 22, 23, 26, 27, 28, 29};
		for (int i = 0; i < decoded.length; ++i){
			
		}
		
	}
	
}

				
/*				if (fiveBitStorage == 0){// gets 2^exponent, 0-7
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

