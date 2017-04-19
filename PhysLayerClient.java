import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;


public class PhysLayerClient {
	public static void main(String args[]) throws UnknownHostException, IOException{
		try (Socket socket = new Socket("codebank.xyz", 38002)){
			System.out.println("Connected to: " + socket.getInetAddress() + ":" + socket.getPort() + "\n");
			
		}
	}
}
