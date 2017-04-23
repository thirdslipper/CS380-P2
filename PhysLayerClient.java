/**
 * Author: Colin Koo
 * Professor: Nima Davarpanah
 * Description: Emulates the physical layer of computer networking.  The program establishes a baseline from
 * the initial 64 byte preamble, then builds a message through signals based on the baseline, which further needs
 * to have its NRZI and 4B/5B encryption decoded.
 *  
 */
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;

public class PhysLayerClient {
	public enum Signal {UP, DOWN}

	public static void main(String args[]) throws UnknownHostException, IOException{
		byte[] bitStorage = new byte[320];
		byte[] convertedBits;
		byte[] origMsg;

		try (Socket socket = new Socket("codebank.xyz", 38002)){
			System.out.println("Connected to: " + socket.getInetAddress() + ":" + socket.getPort() + "\n");
			OutputStream os = socket.getOutputStream();
			InputStream is = socket.getInputStream();

			double baseline = 0;
			for (int i = 0; i < 64; ++i){
				baseline += is.read();
			}
			System.out.println("Baseline: " + (baseline /= 64));

			get5BNRZI(is, bitStorage, baseline);
			convertedBits = decodeNRZI(bitStorage);
			
			convert5B4B(convertedBits);
			origMsg = halfArray(convertedBits);	
			os.write(origMsg);
			System.out.print("Received 32 bytes: ");
			for (int j = 0; j < origMsg.length; ++j){
				System.out.print(Integer.toHexString(origMsg[j] & 0xFF));
			}
			
			if (is.read() == 1){
				System.out.println("\nResponse good.");
			}
			else{
				System.out.println("\nResponse bad.");
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Reads the input stream after the preamble and builds an byte array from the message, determining
	 * value of the signal based on the baseline.
	 * @param is
	 * @param bitStorage
	 * @param baseline
	 * @throws IOException
	 */
	public static void get5BNRZI(InputStream is, byte[] bitStorage, double baseline) throws IOException {
		byte receiver = 0;

		for (int i = 0; i < 320; ++i){ 			//320
			receiver = (byte) is.read();
			if ((receiver & 0xFF) > baseline){
				bitStorage[i] = (byte) 0x01;
			}else if ((receiver & 0xFF) < baseline){
				bitStorage[i] = (byte) 0x00;
			}
		}
	}

	/**
	 * Takes an input byte array and for each slot, undos the NRZI encoding, replacing each slot with
	 * the new decoded bits.
	 * @param bitStorage
	 * @return
	 */
	public static byte[] decodeNRZI(byte[] bitStorage){
		Signal sign = Signal.DOWN;
		byte[] result = new byte[64];
		byte decoded = 0x00;

		for (int i = 0; i < 64; ++i){ 			
			decoded &= 0x00;
			
			// if prev signal is down and new signal is up, flip, and vice versa.
			//iter 1
			if (((bitStorage[5*i] & 0x01)  == 1 && sign == Signal.DOWN) 	
				|| ((bitStorage[5*i] & 0x01) == 0 && sign == Signal.UP)){
				decoded |= 0x10;
				if (sign == Signal.DOWN){
					sign = Signal.UP;
				}
				else{
					sign = Signal.DOWN;
				}
			}

			//iter 2
			if(((bitStorage[(5*i) + 1] & 0x01) == 0 && sign == Signal.UP)
					|| ((bitStorage[(5*i) + 1] & 0x01) == 1 && sign == Signal.DOWN)){
				decoded |= 0x08;
				if (sign == Signal.DOWN){
					sign = Signal.UP;
				}
				else{
					sign = Signal.DOWN;
				}
			}

			//iter 3
			if(((bitStorage[(5*i) +2] & 0x01) == 0x00 && sign == Signal.UP)
					|| ((bitStorage[(5*i) +2] & 0x01) == 1 && sign == Signal.DOWN)){
				decoded |= 0x04;
				if (sign == Signal.DOWN){
					sign = Signal.UP;
				}
				else{
					sign = Signal.DOWN;
				}
			}

			//iter 4
			if(((bitStorage[(5*i) +3] & 0x01) == 0x00 && sign == Signal.UP)
					|| ((bitStorage[(5*i) +3] & 0x01) == 1 && sign == Signal.DOWN)){
				decoded |= 0x02;
				if (sign == Signal.DOWN){
					sign = Signal.UP;
				}
				else{
					sign = Signal.DOWN;
				}
			}

			//iter 5
			if(((bitStorage[(5*i)+4] & 0x01) == 0x00 && (sign == Signal.UP))
					|| ((bitStorage[(5*i)+4] & 0x01) == 1 && sign == Signal.DOWN)){
				decoded |= 0x01;
				if (sign == Signal.DOWN){
					sign = Signal.UP;
				}
				else{
					sign = Signal.DOWN;
				}
			}
			result[i] = decoded;
		}
		return result;
	}
	/**
	 * Halves an input half-byte array by shifting one slot 4 times then concatenating(OR) it
	 * with the following slot.
	 * @param bitStorage
	 * @return
	 */
	public static byte[] halfArray(byte[] bitStorage){
		byte[] halfArray = new byte[bitStorage.length/2];
		byte combine = 0;
		for (int i = 0; i < bitStorage.length/2; i++){
			combine = bitStorage[2*i] <<= 4;
			combine &= 0xF0;
			combine = (byte) (combine | bitStorage[2*i+1]);
			halfArray[i] = (byte) (bitStorage[2*i] | bitStorage[2*i+1]);
		}
		return halfArray;
	}
	/**
	 * Converts the input byte array's right 5 bits to 4 bit mapping.
	 * @param bitStorage
	 */
	public static void convert5B4B(byte[] bitStorage){
		HashMap<Integer, Integer> fiveBitToFourBit = new HashMap<Integer ,Integer>(){{
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
		int temp;
		for (int i = 0; i < bitStorage.length; ++i){
			temp = fiveBitToFourBit.get(bitStorage[i] & 0x1F);
			bitStorage[i] = (byte) (temp & 0xF);
		}
	}
}

