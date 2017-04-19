import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;


public class PhysLayerClient {
	public static void main(String args[]) throws UnknownHostException, IOException{
		byte fiveBitTable[] = {30, 9, 20, 21, 10, 11, 14, 15,
								18, 19, 22, 23, 26, 27, 28, 29};
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
			int count = 0, temp = 0;
			
			for (int j = 0; j < 320; ++j){
				temp = is.read();
				System.out.println("count: " + ++count + " byte: " + temp);
			}
		}
	}
}
