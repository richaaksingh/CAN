import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
/**
 * Interface class of CanClient class
 * 
 * @author Richa Singh
 * 
 */
public interface ICanClient extends Remote{
	/**
	 * @return		cordinates of a peer
	 */
	public ArrayList<Point> getZone()throws RemoteException, NotBoundException;
	/**
	 * @param       points to be added
	 */
	public void setZone(ArrayList<Point> zone)throws RemoteException;
	/**
	 * @return		neighbours from the list
	 */
	public ArrayList<String> getNeighbourList()throws RemoteException;
	/**
	 * @param		ips to be added as neighbours to the list
	 */
	public void setNeighbourList(ArrayList<String> neighbourList)throws RemoteException;
	/**
	 * Every peer enters through bootstrap server
	 *
	 * @throws Exception
	 */
	public void lookup() throws AccessException, RemoteException, NotBoundException, NoSuchAlgorithmException;
	/**
	 * @return		files from the list
	 */
	public String getMyIp() throws  RemoteException, NotBoundException ;
	/**
	 * @return		files from the list
	 */
	public void setMyIp(String myIp) throws RemoteException, NotBoundException;
	/**
	 * @return		files from the list
	 */
	public ArrayList<String> getFiles() throws RemoteException, NotBoundException;
	/**
	 * @return		files from the list
	 */
	public void setFiles(ArrayList<String> files) throws RemoteException, NotBoundException;
	/**
	 * splits the peer and assigns new zone to it
	 * 
	 * @param initPeer	node to be split
	 * @param p	 cordinates of the new peer
	 * @param visited	maintains list for peers that has already been checked
	 * @throws Exception
	 */
	public void split(ICanClient initPeer, Point p, ArrayList<String> visited) throws RemoteException, NotBoundException, NoSuchAlgorithmException;
	/**
	 * used to display all the details of self and other peer using ip address
	 *
	 * @throws Exception
	 */
	public void view() throws RemoteException, NotBoundException;
	/**
	 * used to remove the ip of peer that are no longer neighbours
	 *
	 * @throws Exception
	 */
	public void removeNeighbours() throws RemoteException, NotBoundException;
	/**
	 * used to send a new file
	 *
	 * @param fileName name of file to be inserted
	 * @param p the cordinates of new file to be inserted
	 * @param visited list of already checked neighbours
	 * @param fileToSend file tobe sent in bytes
	 * @throws Exception
	 */
	public void route(Point p, String fileName, ArrayList<String> visited, byte[] fileToSend)throws RemoteException, NotBoundException;
	/**
	 * used to remove a file that is no longer part of peer
	 *
	 * @param newPeer object of peer that is removing file
	 * 
	 * @throws Exception
	 */
	public void removeFiles(ICanClient newPeer) throws NoSuchAlgorithmException, RemoteException, NotBoundException;
	/**
	 * used to insert a new file
	 *
	 * @throws Exception
	 */
	public void insert() throws NoSuchAlgorithmException, RemoteException, NotBoundException;
	/**
	 * used to calculate the X-cordinate using Sha1 algorithm
	 *
	 * @param  p cordinate of file to be searched
	 * @param  fileName name of file to be searched
	 * @param  visited keep track of visited neighbours
	 * @param  sourceIp ip address of peer wanting the file
	 * @return ip address of string that has file
	 * @throws Exception
	 */
	public String routing(Point p, String fileName, ArrayList<String> visited, String sourceIp) throws RemoteException, NotBoundException;
	/**
	 * used to locate a file and download it
	 *
	 * @param  sourceIp ip address of peer serching the file
	 * @return the ip address of peer that has the file
	 * @throws Exception
	 */
	public String search(String myIp) throws NoSuchAlgorithmException,RemoteException, NotBoundException;
	/**
	 * used to leave a peer from CAN
	 *
	 * @throws Exception
	 */
	public void leave() throws RemoteException, NotBoundException;
	/**
	 * used to send a new file
	 *
	 * @param fileName name of file to be inserted
	 * @return byte sends the stored file in bytes
	 * @throws Exception
	 */
	public byte[] sendFile(String fileName)throws RemoteException, NotBoundException;
	/**
	 * used to receive a new file
	 *
	 * @param fileName name of file to be inserted
	 * @param fileToSend has the byte array to receive
	 * @throws Exception
	 */
	public void receiveFile(byte[] fileToSend, String fileName)throws RemoteException, NotBoundException;
}
