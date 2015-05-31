import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * This class is bootstrap server with which peer communicates
 * 
 * @author Richa Singh
 *
 */
public class CanServer extends java.rmi.server.UnicastRemoteObject implements ICanServer , Serializable{

	private String Ipadd;
	private int port = 31088;
	private Registry register;
	public String ownerIp;
    /*
     * default constructor
     */
	public CanServer() throws RemoteException, UnknownHostException
	{
		super();
		Ipadd = (InetAddress.getLocalHost()).toString();
		register = LocateRegistry.createRegistry(port);
		register.rebind("canserver",this);
		System.out.println("binded");
		//UnicastRemoteObject.unexportObject(register, true);
	}
	/**
	 * used for setting the ip of first peer as ownerIp
	 *
	 * @param ownerIp Ip address of first peer 
	 * @throws Exception
	 */
	public void setOwnerIp(String ownerIp)throws RemoteException {
		this.ownerIp = ownerIp;
		System.out.println("i m" + this.ownerIp);
	}
	/**
	 * used for getting the ip of first peer as ownerIp
	 *
	 * @return ownerIp Ip address of first peer 
	 * @throws Exception
	 */
	public String getOwnerIp() throws RemoteException{
		return ownerIp;
	}
	/**
	 * Main method for server
	 * 
	 * @throws Exception
	 */
	public static void main(String args[]) throws RemoteException, UnknownHostException{
		ICanServer server = new CanServer();
		//server.setOwnerIp("object");
	}
}

