import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.ListIterator;
import java.util.Scanner;
import java.util.Set;
/**
 * File: CanClient.java
 * 
 * This class is a Peer which communicates with a Bootstrap server 
 * and join CAN
 * 
 * @author Richa Singh
 *
 */

public class CanClient extends java.rmi.server.UnicastRemoteObject implements ICanClient , Serializable{

	private static final long serialVersionUID = 1L;
	private ICanServer server;
	private Registry register;
	private String BootstrapIp = "129.21.37.35";
	private int port = 31088;
	private String myIp;
	/* List containing cordinates of the respective node */
	public ArrayList< Point> zone = new ArrayList<Point>();
	/* List containing neighbours of the respective node */
	public ArrayList< String> neighbourList = new ArrayList<String>();
	/* List containing files of the respective node */
	public ArrayList<String> files = new ArrayList<String>();

	/**
	 * Initializing all the variables
	 */
	protected CanClient() throws RemoteException, UnknownHostException {
		super();
		myIp = InetAddress.getLocalHost().getHostAddress();
		System.out.println("i am"+myIp);
	}
	/**
	 * @return		files from the list
	 */
	public ArrayList<String> getFiles()throws RemoteException, NotBoundException{
		return files;
	}
	/**
	 * @param files		adds a file to the list
	 */
	public void setFiles(ArrayList<String> files)throws RemoteException, NotBoundException {
		this.files = files;
	}
	/**
	 * @return	returns if address of neighbour from the list
	 */
	public ArrayList<String> getNeighbourList() throws RemoteException{
		return neighbourList;
	}
	/**
	 * @param neighbourList	used for adding the ip address of neighbours
	 */
	public void setNeighbourList(ArrayList<String> neighbourList) throws RemoteException{
		//System.out.println("reference check " + neighbourList);
		this.neighbourList = new ArrayList<String>();
		this.neighbourList = neighbourList;
	}
	/**
	 * @return		the x & y cordinates of peer
	 */
	public ArrayList<Point> getZone() throws RemoteException{
		return zone;
	}
	/**
	 * @param zone	sets x & y cordinate of a peer
	 */
	public void setZone(ArrayList<Point> zone) throws RemoteException{
		this.zone = zone;
	}
	/**
	 * @return		ip address of peer
	 */
	public String getMyIp() throws RemoteException{
		return myIp;
	}
	/**
	 * @param key	sets ip address of a peer
	 */
	public void setMyIp(String myIp) throws RemoteException{
		this.myIp = myIp;
	}
	/**
	 * Every peer enters through bootstrap server
	 *
	 * @throws Exception
	 */
	public void lookup() throws AccessException, RemoteException, NotBoundException, NoSuchAlgorithmException{
		//Ip address of Buddy
		register = LocateRegistry.getRegistry(BootstrapIp,port);
		// retrieving server object from registry
		server = (ICanServer)(register.lookup("canserver"));
		this.join();

	}
	/**
	 * used to join this peer to CAN
	 *
	 * @throws Exception
	 */
	public void join() throws RemoteException, NotBoundException, NoSuchAlgorithmException{

		ArrayList<String> visited = new ArrayList<String>();
		register = LocateRegistry.createRegistry(port);
		register.rebind("canclient",this);
		//UnicastRemoteObject.unexportObject(register, true);

		String ownerIp = server.getOwnerIp();
		/*
		 * Case 1: When the peer is first peer joining
		 * line
		 */
		if(ownerIp == null)
		{
			System.out.println("i m first");
			Point p1 = new Point(0,0);
			Point p2 = new Point(100,0);
			Point p3 = new Point(0,100);
			Point p4 = new Point(100,100);
			zone.add(p1);
			zone.add(p2);
			zone.add(p3);
			zone.add(p4);
			server.setOwnerIp(myIp);
			System.out.println("first joined");
		}
		/*
		 * Case 2: When peer is not the first peer
		 * line
		 */
		else
		{
			System.out.println("i m rest");
			register = LocateRegistry.getRegistry(ownerIp,port);
			ICanClient initPeer = (ICanClient)(register.lookup("canclient"));
			double x = (Math.random()*100)+1;
			double y = (Math.random()*100)+1;
			System.out.println("cordinates " + x + ", " +  y );
			Point p = new Point(x,y);
			System.out.println("I am splitiing");
			this.split(initPeer,p,visited);
		}
	}
	/**
	 * splits the peer and assigns new zone to it
	 * 
	 * @param initPeer	node to be split
	 * @param p	 cordinates of the new peer
	 * @param visited	maintains list for peers that has already been checked
	 * @throws Exception
	 */
	public void split(ICanClient initPeer, Point p, ArrayList<String> visited) throws RemoteException, NotBoundException, NoSuchAlgorithmException{
		boolean flag = false;
		ArrayList<Point> zone = initPeer.getZone();

		Point p1 = zone.get(0);
		Point p2 = zone.get(1);
		Point p3 = zone.get(2);
		Point p4 = zone.get(3);

		/*
		 * Case 1: checking if point p lies within the initial peer
		 * line
		 */
		if(p1.getX() <= p.getX() && p1.getY() <= p.getY() && 
				p2.getX() >= p.getX() && p2.getY() <= p.getY() &&
				p3.getX() <= p.getX() && p3.getY() >= p.getY() &&
				p4.getX() >= p.getX() && p4.getY() >= p4.getY()){
			//empties arraylist
			visited.clear();
			System.out.println("point lies within " + initPeer.getMyIp());

			double hL = p2.getX()-p1.getX();
			double vL = p3.getY()-p1.getY();
			/*
			 * Case 1: checking condition for horizontal 
			 * line
			 */
			if( hL == vL )
			{
				System.out.println("vertical split");
				double mid = hL/2;
				//checking for new peer right or left
				if (mid < p.getX()){
					Point newP2 = new Point(p2.getX(),p2.getY());
					Point newP4 = new Point(p4.getX(),p4.getY());

					p2.setX((newP2.getX() + p1.getX())/2);
					Point newP1 = new Point(p2.getX(),p2.getY());
					p4.setX((newP2.getX() + p1.getX())/2);
					Point newP3 = new Point(p4.getX(),p4.getY());

					ArrayList<Point> newZone = new ArrayList<Point>();
					newZone.add(p1);
					newZone.add(p2);
					newZone.add(p3);
					newZone.add(p4);

					initPeer.setZone(newZone);

					newZone = new ArrayList<Point>();
					newZone.add(newP1);
					newZone.add(newP2);
					newZone.add(newP3);
					newZone.add(newP4);

					this.setZone(newZone);

					//updating the neighbour list
					ArrayList<String> initialPeer = initPeer.getNeighbourList();
					for(String ip : initialPeer){
						this.updateNeigbours(ip);
					}

					initialPeer.add(this.myIp);
					initPeer.setNeighbourList(initialPeer);
					initPeer.removeNeighbours();

					this.neighbourList.add(initPeer.getMyIp());
					this.removeFiles(initPeer);
				}
				//checking for new peer right or left
				else {
					Point newP1 = new Point(p1.getX(),p1.getY());
					Point newP3 = new Point(p3.getX(),p3.getY());

					p1.setX((p2.getX() + newP1.getX())/2);
					Point newP2 = new Point(p1.getX(),p1.getY());;
					p3.setX((p2.getX() + newP1.getX())/2);
					Point newP4 = new Point(p3.getX(),p3.getY());
					ArrayList<Point> newZone = new ArrayList<Point>();
					newZone.add(p1);
					newZone.add(p2);
					newZone.add(p3);
					newZone.add(p4);

					initPeer.setZone(newZone);

					newZone = new ArrayList<Point>();
					newZone.add(newP1);
					newZone.add(newP2);
					newZone.add(newP3);
					newZone.add(newP4);

					this.setZone(newZone);

					//updating the neighbour list
					ArrayList<String> initialPeer = initPeer.getNeighbourList();
					for(String ip : initialPeer){
						this.updateNeigbours(ip);
					}

					initialPeer.add(this.myIp);
					initPeer.setNeighbourList(initialPeer);
					initPeer.removeNeighbours();

					this.neighbourList.add(initPeer.getMyIp());
					this.removeFiles(initPeer);
				}
			}
			/*
			 * Case 2: checking condition for vertical split
			 * line
			 */
			else{
				System.out.println("horizontal split");
				double mid = vL/2;
				if(mid < p.getY()){
					Point newP3 = new Point(p3.getX(),p3.getY());
					Point newP4 = new Point(p4.getX(),p4.getY());
					p3.setY((newP3.getY() + p1.getY())/2);
					Point newP1 = new Point(p3.getX(),p3.getY());
					p4.setY((newP3.getY() + p1.getY())/2);
					Point newP2 = new Point(p4.getX(),p4.getY());
					ArrayList<Point> newZone = new ArrayList<Point>();
					newZone.add(p1);
					newZone.add(p2);
					newZone.add(p3);
					newZone.add(p4);

					initPeer.setZone(newZone);

					newZone = new ArrayList<Point>();
					newZone.add(newP1);
					newZone.add(newP2);
					newZone.add(newP3);
					newZone.add(newP4);

					this.setZone(newZone);

					//updating the neighbour list
					ArrayList<String> initialPeer = initPeer.getNeighbourList();
					for(String ip : initialPeer){
						this.updateNeigbours(ip);
					}

					initialPeer.add(this.myIp);
					initPeer.setNeighbourList(initialPeer);
					initPeer.removeNeighbours();

					this.neighbourList.add(initPeer.getMyIp());
					this.removeFiles(initPeer);
				}
				else{
					Point newP1 = new Point(p1.getX(),p1.getY());
					Point newP2 = new Point(p2.getX(),p2.getY());
					p1.setY((p3.getY() + newP1.getY())/2);
					Point newP3 = new Point(p1.getX(),p1.getY());
					p2.setY((p3.getY() + newP1.getY())/2);
					Point newP4 = new Point(p2.getX(),p2.getY());;
					ArrayList<Point> newZone = new ArrayList<Point>();
					newZone.add(p1);
					newZone.add(p2);
					newZone.add(p3);
					newZone.add(p4);

					initPeer.setZone(newZone);

					newZone = new ArrayList<Point>();
					newZone.add(newP1);
					newZone.add(newP2);
					newZone.add(newP3);
					newZone.add(newP4);

					this.setZone(newZone);

					//updating the neighbour list
					ArrayList<String> initialPeer = initPeer.getNeighbourList();
					for(String ip : initialPeer){
						this.updateNeigbours(ip);
					}

					initialPeer.add(this.myIp);
					initPeer.setNeighbourList(initialPeer);
					initPeer.removeNeighbours();

					this.neighbourList.add(initPeer.getMyIp());
					this.removeFiles(initPeer);
				}
			}
		}
		//else condition for main If and it comes here for checking the neighbours if not found in first peerlist
		else {
			// to check if neigbour has already been checked
			visited.add(initPeer.getMyIp());
			//System.out.println();
			ArrayList<String> initialPeer = initPeer.getNeighbourList();
			for (String Ip : initialPeer ){
				register = LocateRegistry.getRegistry(Ip,port);
				ICanClient newPeer = (ICanClient)(register.lookup("canclient"));

				if(p1.getX() <= p.getX() && p1.getY() <= p.getY() && 
						p2.getX() >= p.getX() && p2.getY() <= p.getY() &&
						p3.getX() <= p.getX() && p3.getY() >= p.getY() &&
						p4.getX() >= p.getX() && p4.getY() >= p4.getY()){
					this.split(newPeer, p ,visited);
					flag = true;
					break;
				}

			}
			if (flag == false){
				double minDiff = 1000;
				ICanClient closestPeer = null;
				initialPeer = new ArrayList<String>();
				initialPeer = initPeer.getNeighbourList();

				for (String Ip : initialPeer ){
					if(!visited.contains(Ip)){
						register = LocateRegistry.getRegistry(Ip,port);
						ICanClient newPeer = (ICanClient)(register.lookup("canclient"));
						zone = newPeer.getZone();
						p1 = zone.get(0);
						p2 = zone.get(1);
						p3 = zone.get(2);
						p4 = zone.get(3);
						double centreX = ((p2.getX() - p1.getX())/2);
						double centreY = ((p3.getY() - p1.getY())/2);
						double difference = Math.sqrt(Math.pow(p.getX()-centreX,2)+ Math.pow(p.getY()-centreY,2));
						if(minDiff > difference){
							minDiff = difference;
							closestPeer = newPeer;
						}
					}
				}
				this.split(closestPeer,p,visited);

			}
		}
	}
	/**
	 * used to update the neignourlist of peer that split
	 *
	 * @param ip It is Ip address of peer that needs neighbours updated
	 * @throws Exception
	 */
	private void updateNeigbours(String ip) throws RemoteException, NotBoundException {
		register = LocateRegistry.getRegistry(ip,port);
		// retrieving client object from registry
		ICanClient neighbourPeer = (ICanClient)(register.lookup("canclient"));
		Point p1 = neighbourPeer.getZone().get(0);
		Point p2 = neighbourPeer.getZone().get(1);
		Point p3 = neighbourPeer.getZone().get(2);
		Point p4 = neighbourPeer.getZone().get(3);

		Point myP1 = this.getZone().get(0);
		Point myP2 = this.getZone().get(1);
		Point myP3 = this.getZone().get(2);
		Point myP4 = this.getZone().get(3);
		/*
		 * Case 1: checking condition if points are neighbour of a peer
		 * line
		 */
		if((((myP2.getY() == p3.getY()) || (myP4.getY() == p1.getY())) && 
				((p3.getX() < myP2.getX() && myP2.getX() <= p4.getX()) || (p3.getX() <= myP1.getX() && myP1.getX() < p4.getX()))) ||
				(((myP2.getX() == p3.getX()) || (myP1.getX() == p4.getX())) &&
						((p1.getY() <= myP2.getY() && myP2.getY() < p3.getY()) || (p1.getY() < myP4.getY() && myP4.getY() <= p3.getY())))){

			this.neighbourList.add(ip);
			ArrayList<String> addNeighbour = neighbourPeer.getNeighbourList();
			addNeighbour.add(this.myIp);
			neighbourPeer.setNeighbourList(addNeighbour);
		}
	}
	/**
	 * used to remove the ip of peer that are no longer neighbours
	 *
	 * @throws Exception
	 */
	public void removeNeighbours() throws RemoteException, NotBoundException{

		Point myP1 = this.getZone().get(0);
		Point myP2 = this.getZone().get(1);
		Point myP3 = this.getZone().get(2);
		Point myP4 = this.getZone().get(3);

		Set<String> removeIp = new HashSet<String>(); 
		ArrayList<String> newNeighbours = this.getNeighbourList();
		ListIterator<String> i = newNeighbours.listIterator();
		while(i.hasNext()){
			String Ip = i.next();
			register = LocateRegistry.getRegistry(Ip,port);
			// retrieving client object from registry
			ICanClient newPeer = (ICanClient)(register.lookup("canclient"));
			Point p1 = newPeer.getZone().get(0);
			Point p2 = newPeer.getZone().get(1);
			Point p3 = newPeer.getZone().get(2);
			Point p4 = newPeer.getZone().get(3);
			/*
			 * Case 1: checking condition if points are not part of neighbour anymore
			 * line
			 */
			if(!((((myP2.getY() == p3.getY()) || (myP4.getY() == p1.getY())) && 
					((p3.getX() < myP2.getX() && myP2.getX() <= p4.getX()) || (p3.getX() <= myP1.getX() && myP1.getX() < p4.getX()))) ||
					(((myP2.getX() == p3.getX()) || (myP1.getX() == p4.getX())) &&
							((p1.getY() <= myP2.getY() && myP2.getY() < p3.getY()) || (p1.getY() < myP4.getY() && myP4.getY() <= p3.getY()))))){

				//System.out.println("I entered remove hahaha!!! " + Ip);
				removeIp.add(Ip);

				ArrayList<String> removeNeighbour = newPeer.getNeighbourList();
				removeNeighbour.remove(this.myIp);
				newPeer.setNeighbourList(removeNeighbour);
			}
		}

		for(String ip : removeIp){
			if(this.neighbourList.contains(ip)){
				ArrayList<String> neighborList = this.getNeighbourList();
				neighborList.remove(ip);
				System.out.println("finally removing " + neighborList);
				this.setNeighbourList(neighborList);
			}
		}
	}
	/**
	 * used to send a new file
	 *
	 * @param fileName name of file to be inserted
	 * @return byte sends the stored file in bytes
	 * @throws Exception
	 */
	public byte[] sendFile(String fileName){
		try {
			File file = new File(fileName);
			byte buffer[] = new byte[(int)file.length()];
			BufferedInputStream input = new BufferedInputStream(new FileInputStream(fileName));
			input.read(buffer,0,buffer.length);
			input.close();
			System.out.println("I am sending: " + this.myIp);
			return(buffer);
		} catch(Exception e){
			System.out.println("FileImpl: "+e.getMessage());
			e.printStackTrace();
			return(null);
		}
	}
	/**
	 * used to receive a new file
	 *
	 * @param fileName name of file to be inserted
	 * @param fileToSend has the byte array to receive
	 * @throws Exception
	 */
	public void receiveFile(byte[] fileToSend, String fileName){
		try{
			BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(fileName));
			output.write(fileToSend,0,fileToSend.length);
			output.flush();
			output.close();
			System.out.println("I am receiving the file: " + this.myIp);
		} catch(Exception e) {
			System.err.println("FileServer exception: "+ e.getMessage());
			e.printStackTrace();
		}
	}
	/**
	 * used to insert a new file
	 *
	 * @throws Exception
	 */
	public void insert() throws NoSuchAlgorithmException, RemoteException, NotBoundException{
		System.out.println("Enter the file name");
		Scanner sc = new Scanner(System.in);
		String fileName = sc.next().trim();
		//ArrayList<String> duplicateFiles = new ArrayList<String>();
		//if(!duplicateFiles.contains(fileName)){
		//	duplicateFiles.add(fileName);
		File file = new File(fileName);
		if(file.exists()){
			byte[] fileToSend = this.sendFile(fileName);
			int xCordinate = this.sha1X(fileName);
			int yCordinate = this.sha1Y(fileName);
			System.out.println("files should be at " + xCordinate + ", " +  yCordinate);
			Point p = new Point (xCordinate,yCordinate);
			ArrayList<String> visited = new ArrayList<String>();
			Point p1 = this.getZone().get(0);
			Point p2 = this.getZone().get(1);
			Point p3 = this.getZone().get(2);
			Point p4 = this.getZone().get(3);
			if(p1.getX() <= p.getX() && p1.getY() <= p.getY() && 
					p2.getX() >= p.getX() && p2.getY() <= p.getY() &&
					p3.getX() <= p.getX() && p3.getY() >= p.getY() &&
					p4.getX() >= p.getX() && p4.getY() >= p4.getY()){
				if(!this.files.contains(fileName))
					this.files.add(fileName);
				else
					System.out.println("file already exists");
			}
			else{
				this.route(p, fileName, visited, fileToSend);
			}
		}
		else
			System.out.println("Failure: No such file found in directory");
		//}
	}
	/**
	 * used to send a new file
	 *
	 * @param fileName name of file to be inserted
	 * @param p the cordinates of new file to be inserted
	 * @param visited list of already checked neighbours
	 * @param fileToSend file tobe sent in bytes
	 * @throws Exception
	 */
	public void route(Point p, String fileName, ArrayList<String> visited, byte[] fileToSend) throws RemoteException, NotBoundException {
		boolean flag = false;
		visited.add(this.myIp);
		for ( String Ip :this.neighbourList){

			register = LocateRegistry.getRegistry(Ip,port);
			// retrieving client object from registry
			ICanClient newPeer = (ICanClient)(register.lookup("canclient"));
			Point p1 = newPeer.getZone().get(0);
			Point p2 = newPeer.getZone().get(1);
			Point p3 = newPeer.getZone().get(2);
			Point p4 = newPeer.getZone().get(3);
			/*
			 * Case 1: checking condition if file lied within the peer
			 * line
			 */
			if(p1.getX() <= p.getX() && p1.getY() <= p.getY() && 
					p2.getX() >= p.getX() && p2.getY() <= p.getY() &&
					p3.getX() <= p.getX() && p3.getY() >= p.getY() &&
					p4.getX() >= p.getX() && p4.getY() >= p4.getY()){

				ArrayList<String> newFiles = newPeer.getFiles();
				if(!newFiles.contains(fileName)){
					newFiles.add(fileName);
					newPeer.setFiles(newFiles);
					newPeer.receiveFile(fileToSend, fileName);
				}
				else
					System.out.println("file already exists..");

				flag = true;
				break;
			}
		}

		if (flag == false){
			double minDiff = 1000;
			ICanClient closestPeer = null;
			ArrayList<String> initialPeer = new ArrayList<String>();
			initialPeer = this.getNeighbourList();

			for (String Ip : initialPeer ){
				if(!visited.contains(Ip)){
					register = LocateRegistry.getRegistry(Ip,port);
					ICanClient newPeer = (ICanClient)(register.lookup("canclient"));
					zone = newPeer.getZone();
					Point p1 = zone.get(0);
					Point p2 = zone.get(1);
					Point p3 = zone.get(2);
					Point p4 = zone.get(3);
					double centreX = ((p2.getX() - p1.getX())/2);
					double centreY = ((p3.getY() - p1.getY())/2);
					double difference = Math.sqrt(Math.pow(p.getX()-centreX,2)+ Math.pow(p.getY()-centreY,2));
					if(minDiff > difference){
						minDiff = difference;
						closestPeer = newPeer;
					}
				}
			}
			closestPeer.route(p, fileName, visited, fileToSend );
		}
	}
	/**
	 * used to remove a file that is no longer part of peer
	 *
	 * @param newPeer object of peer that is removing file
	 * 
	 * @throws Exception
	 */
	public void removeFiles(ICanClient newPeer) throws NoSuchAlgorithmException, RemoteException, NotBoundException{
		System.out.println("i am removing files");
		Point p1 = newPeer.getZone().get(0);
		Point p2 = newPeer.getZone().get(1);
		Point p3 = newPeer.getZone().get(2);
		Point p4 = newPeer.getZone().get(3);

		Set<String> removeFile = new HashSet<String>();
		ArrayList<String> peer = newPeer.getFiles();
		ListIterator<String> i = peer.listIterator();
		while(i.hasNext()){
			String file = i.next();
			int xCordinate = this.sha1X(file);
			int yCordinate = this.sha1Y(file);
			Point p = new Point(xCordinate,yCordinate);
			/*
			 * Case 1: checking condition if files are not part of peer anymore
			 * line
			 */
			if(!(p1.getX() <= p.getX() && p1.getY() <= p.getY() && 
					p2.getX() >= p.getX() && p2.getY() <= p.getY() &&
					p3.getX() <= p.getX() && p3.getY() >= p.getY() &&
					p4.getX() >= p.getX() && p4.getY() >= p4.getY())){
				// removes files from existing peer and adds to new peer)
				//i.remove();
				removeFile.add(file);
				System.out.println(" removed file"+ file );
				byte[] fileToSend = newPeer.sendFile(file);
				ArrayList<String> removeFiles = this.getFiles();
				removeFiles.add(file);
				this.setFiles(removeFiles);
				this.receiveFile(fileToSend, file);
			}
		}
		ArrayList<String> fileRemove = newPeer.getFiles();
		for(String file : removeFile){
			if(fileRemove.contains(file)){
				fileRemove.remove(file);
			}
		}
		newPeer.setFiles(fileRemove);
	}
	/**
	 * used to calculate the X-cordinate using Sha1 algorithm
	 *
	 * @param  input name of file to be inserted
	 * @return int returns the number value for x cordinate
	 * @throws Exception
	 */
	public int sha1X(String input) throws NoSuchAlgorithmException {
		MessageDigest mDigest = MessageDigest.getInstance("SHA1");
		byte[] result = mDigest.digest(input.getBytes());
		int x = 0;
		for (int i = 0; i < result.length; i = i + 2) {
			x += ((result[i] & 0xff) + 0x100);
		}
		return x%100;
	}
	/**
	 * used to calculate the Y-cordinate using Sha1 algorithm
	 *
	 * @param  input name of file to be inserted
	 * @return int returns the number value for Y cordinate
	 * @throws Exception
	 */
	public int sha1Y(String input) throws NoSuchAlgorithmException {
		MessageDigest mDigest = MessageDigest.getInstance("SHA1");
		byte[] result = mDigest.digest(input.getBytes());
		int y = 0;
		for (int i = 1; i < result.length; i = i + 2) {
			y += ((result[i] & 0xff) + 0x100);
		}
		return y%100;
	}
	/**
	 * used to locate a file and download it
	 *
	 * @param  sourceIp ip address of peer serching the file
	 * @return the ip address of peer that has the file
	 * @throws Exception
	 */
	public String search(String sourceIp) throws NoSuchAlgorithmException, RemoteException, NotBoundException{
		System.out.println("Enter the name of file to be searched");
		Scanner sc = new Scanner(System.in);
		String fileName = sc.next().trim();
		int xCordinate = this.sha1X(fileName);
		int yCordinate = this.sha1Y(fileName);
		Point p = new Point (xCordinate,yCordinate);
		ArrayList<String> visited = new ArrayList<String>();
		Point p1 = this.getZone().get(0);
		Point p2 = this.getZone().get(1);
		Point p3 = this.getZone().get(2);
		Point p4 = this.getZone().get(3);
		/*
		 * Case 1: checking condition for file to be searched
		 * 
		 */
		if(p1.getX() <= p.getX() && p1.getY() <= p.getY() && 
				p2.getX() >= p.getX() && p2.getY() <= p.getY() &&
				p3.getX() <= p.getX() && p3.getY() >= p.getY() &&
				p4.getX() >= p.getX() && p4.getY() >= p4.getY()){
			if(this.getFiles().contains(fileName)){
				return this.myIp;
			}
			else{
				return null;
			}
		}
		else 
			return this.routing(p, fileName, visited, sourceIp);


	}
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
	public String routing(Point p, String fileName, ArrayList<String> visited, String sourceIp) throws RemoteException, NotBoundException {
		boolean flag = false;
		visited.add(this.myIp);
		for ( String Ip :this.neighbourList){

			register = LocateRegistry.getRegistry(Ip,port);
			// retrieving client object from registry
			ICanClient newPeer = (ICanClient)(register.lookup("canclient"));
			Point p1 = newPeer.getZone().get(0);
			Point p2 = newPeer.getZone().get(1);
			Point p3 = newPeer.getZone().get(2);
			Point p4 = newPeer.getZone().get(3);
			/*
			 * Case 1: checking condition if file lies within a peer
			 * 
			 */
			if(p1.getX() <= p.getX() && p1.getY() <= p.getY() && 
					p2.getX() >= p.getX() && p2.getY() <= p.getY() &&
					p3.getX() <= p.getX() && p3.getY() >= p.getY() &&
					p4.getX() >= p.getX() && p4.getY() >= p4.getY()){
				if(newPeer.getFiles().contains(fileName)){
					flag = true;
					byte[] fileToSend = newPeer.sendFile(fileName);
					register = LocateRegistry.getRegistry(sourceIp , port);
					// retrieving client object from registry
					ICanClient sourcePeer = (ICanClient)(register.lookup("canclient"));
					sourcePeer.receiveFile(fileToSend, fileName);
					return Ip;
				}
			}
		}
		if (flag == false){
			double minDiff = 1000;
			ICanClient closestPeer = null;
			ArrayList<String> initialPeer = new ArrayList<String>();
			initialPeer = this.getNeighbourList();

			for (String Ip : initialPeer ){
				if(!visited.contains(Ip)){
					register = LocateRegistry.getRegistry(Ip,port);
					ICanClient newPeer = (ICanClient)(register.lookup("canclient"));
					zone = newPeer.getZone();
					Point p1 = zone.get(0);
					Point p2 = zone.get(1);
					Point p3 = zone.get(2);
					Point p4 = zone.get(3);
					double centreX = ((p2.getX() - p1.getX())/2);
					double centreY = ((p3.getY() - p1.getY())/2);
					double difference = Math.sqrt(Math.pow(p.getX()-centreX,2)+ Math.pow(p.getY()-centreY,2));
					if(minDiff > difference){
						minDiff = difference;
						closestPeer = newPeer;
					}
				}
			}
			if(closestPeer == null){
				return null;
			}
			return closestPeer.routing(p, fileName,visited, sourceIp);
		}
		return null;
	}
	/**
	 * used to display all the details of self and other peer using ip address
	 *
	 * @throws Exception
	 */
	public void view() throws RemoteException, NotBoundException{
		int ch = 0;
		System.out.println("1: View my information ");
		System.out.println("2 : View information of given peer");
		Scanner sc = new Scanner(System.in);
		ch = sc.nextInt();
		switch(ch){
		case 1:
			System.out.println("i m " + this.myIp);
			// arraylist returns object of type point..so getter method required to fetch the value of x and y
			for (Point p :zone){
				System.out.println("(" + p.getX()+"," + p.getY()+")");
			}
			for(String Ip : this.neighbourList ){
				System.out.println(" My neighbours are " + Ip);}
			for (String file : this.files){
				System.out.println(" My Files are " + file);
			}
			break;

		case 2 :
			System.out.println("enter the Ip address");
			sc = new Scanner(System.in);
			String peerIp = sc.next();
			System.out.println("peer ip " + peerIp);
			register = LocateRegistry.getRegistry(peerIp,port);
			// retrieving client object from registry
			ICanClient newPeer = (ICanClient)(register.lookup("canclient"));

			for (Point p :newPeer.getZone()){
				System.out.println("(" + p.getX()+"," + p.getY()+")");
			}
			for(String Ip : newPeer.getNeighbourList()){
				System.out.println(" Neighbours are " + Ip);}
			for (String file : newPeer.getFiles()){
				System.out.println(" Files are " + file);
			}
			break;
		}
	}
	/**
	 * used to leave a peer from CAN
	 *
	 * @throws Exception
	 */
	public void leave() throws RemoteException, NotBoundException{

		System.out.println("yay!!!! I am leaving"+ this.myIp);
		Point p1 = this.getZone().get(0);
		Point p2 = this.getZone().get(1);
		Point p3 = this.getZone().get(2);
		Point p4 = this.getZone().get(3);
		for(String ip : this.getNeighbourList()){
			boolean flag = false;
			register = LocateRegistry.getRegistry(ip,port);
			// retrieving client object from registry
			ICanClient newPeer = (ICanClient)(register.lookup("canclient"));
			Point oldp1 = newPeer.getZone().get(0);
			Point oldp2 = newPeer.getZone().get(1);
			Point oldp3 = newPeer.getZone().get(2);
			Point oldp4 = newPeer.getZone().get(3);

			/*
			 * Case 1: checking condition for top peer leaving
			 * 
			 */
			if((p1.getX()==oldp3.getX()&&p1.getY()==oldp3.getY())&&
					(p2.getX()==oldp4.getX()&&p2.getY()==oldp4.getY())){
				System.out.println("top leaving");

				Point newp1 = new Point(oldp1.getX(),oldp1.getY());
				Point newp2 = new Point(oldp2.getX(),oldp2.getY());
				Point newp3 = new Point(p3.getX(),p3.getY());
				Point newp4 = new Point(p4.getX(),p4.getY());

				ArrayList<Point> newZone = new ArrayList<Point>();
				newZone.add(newp1);
				newZone.add(newp2);
				newZone.add(newp3);
				newZone.add(newp4);

				newPeer.setZone(newZone);
				flag = true;
			}

			/*
			 * Case 2: checking condition for bottom peer leaving
			 * 
			 */
			else if((p3.getX()==oldp1.getX()&&p3.getY()==oldp1.getY())&&
					(p4.getX()==oldp2.getX()&&p4.getY()==oldp2.getY())){
				System.out.println("bottom leaving");

				Point newp1 = new Point(p1.getX(),p1.getY());
				Point newp2 = new Point(p2.getX(),p2.getY());
				Point newp3 = new Point(oldp3.getX(),oldp3.getY());
				Point newp4 = new Point(oldp4.getX(),oldp4.getY());

				ArrayList<Point> newZone = new ArrayList<Point>();
				newZone.add(newp1);
				newZone.add(newp2);
				newZone.add(newp3);
				newZone.add(newp4);

				newPeer.setZone(newZone);
				flag = true;
			}
			/*
			 * Case 2: checking condition for left peer leaves 
			 * 
			 */
			else if((p2.getX()==oldp1.getX()&&p2.getY()==oldp1.getY())&&
					(p4.getX()==oldp3.getX()&&p4.getY()==oldp3.getY())){
				System.out.println("left leaving");

				Point newp1 = new Point(p1.getX(),p1.getY());
				Point newp2 = new Point(oldp2.getX(),oldp2.getY());
				Point newp3 = new Point(p3.getX(),p3.getY());
				Point newp4 = new Point(oldp4.getX(),oldp4.getY());

				ArrayList<Point> newZone = new ArrayList<Point>();
				newZone.add(newp1);
				newZone.add(newp2);
				newZone.add(newp3);
				newZone.add(newp4);

				newPeer.setZone(newZone);
				flag = true;
			}
			/*
			 * Case 2: checking condition for right peer leaves 
			 * 
			 */
			else if((p1.getX()==oldp2.getX()&&p1.getY()==oldp2.getY())&&
					(p3.getX()==oldp4.getX()&&p3.getY()==oldp4.getY()))

			{
				System.out.println("right leaving");

				Point newp1 = new Point(oldp1.getX(),oldp1.getY());
				Point newp2 = new Point(p2.getX(),p2.getY());
				Point newp3 = new Point(oldp3.getX(),oldp3.getY());
				Point newp4 = new Point(p4.getX(),p4.getY());

				ArrayList<Point> newZone = new ArrayList<Point>();
				newZone.add(newp1);
				newZone.add(newp2);
				newZone.add(newp3);
				newZone.add(newp4);

				newPeer.setZone(newZone);
				flag = true;

			}
			else {
				ArrayList<String> neighborIps = newPeer.getNeighbourList();
				neighborIps.remove(this.myIp);
				newPeer.setNeighbourList(neighborIps);

			}

			if (flag == true){
				ArrayList<String> newList = newPeer.getNeighbourList();
				newList.remove(this.myIp);
				for(String i : this.getNeighbourList()){
					if((!newList.contains(i)) && !(i.equals(newPeer.getMyIp()))){
						newList.add(i);
					}
				}
				newPeer.setNeighbourList(newList);
				ArrayList<String> files = newPeer.getFiles();

				ArrayList<String> newfile = this.getFiles();
				for(String s :newfile){
					// check if it doesnot contain only den add
					if(!files.contains(s)){
						files.add(s);
						byte[] fileToSend = this.sendFile(s);
						newPeer.receiveFile(fileToSend, s);
					}
				}
				newPeer.setFiles(files);

				System.out.println("myIp" + this.myIp);
				System.out.println("BootstrapIp" + server.getOwnerIp());
				if(this.myIp.equals(server.getOwnerIp())){
					System.out.println("I m owner and now i m leaving");
					server.setOwnerIp(newPeer.getMyIp());

				}
			}
		}
		System.out.println("I m finally leaving");
	}

	/**
	 * Main method for CAN
	 *
	 * @throws Exception
	 */
	public static void main(String args[]) throws RemoteException, NotBoundException, UnknownHostException, NoSuchAlgorithmException{
		//serverAdd = args[0];
		//serverPort = args[1];
		//word = args[2];
		boolean joined = false;
		ICanClient client = new CanClient();
		//client.lookup();
		Scanner sc = new Scanner(System.in);
		while (true){
			try{
				System.out.println("1: join");
				System.out.println("2: view");
				System.out.println("3: insert");
				System.out.println("4: search");
				System.out.println("5: leave");
				int ch  = sc.nextInt();

				switch (ch){

				case 1:
					if(joined == false){
						client.lookup();
						joined = true;
					} else 
						System.out.println("Failure");
					break;
				case 2:
					client.view();
					break;
				case 3:
					client.insert();
					break;
				case 4:
					String ip = client.search(client.getMyIp());
					if(ip==null ){
						System.out.println("Failure: file not found");}
					else{
						System.out.println(" found file " + ip);}
					break;
				case 5:
					client.leave();
					System.exit(0);
					break;
				}
			}
			catch(Exception e){

			}
		}
	}
}
