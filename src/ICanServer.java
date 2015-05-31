import java.rmi.*;
/**
 * Interface class of CanServer class
 * 
 * @author Richa Singh
 * 
 */
public interface ICanServer extends Remote{
	/**
	 * used for setting the ip of first peer as ownerIp
	 *
	 * @param ownerIp Ip address of first peer 
	 * @throws Exception
	 */
	public void setOwnerIp(String test) throws RemoteException;
	/**
	 * used for getting the ip of first peer as ownerIp
	 *
	 * @return ownerIp Ip address of first peer 
	 * @throws Exception
	 */
	public String getOwnerIp()throws RemoteException;

}
