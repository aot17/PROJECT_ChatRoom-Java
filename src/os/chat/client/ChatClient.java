package os.chat.client;

import os.chat.server.ChatServerInterface;
import os.chat.server.ChatServerManagerInterface;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Vector;

/**
 * This class implements a chat client that can be run locally or remotely to
 * communicate with a {@link ChatServer} using RMI.
 */
public class ChatClient extends UnicastRemoteObject implements CommandsFromWindow,CommandsFromServer {

	ChatServerManagerInterface csm;
	Registry registry;

	/**
	 * The name of the user of this client
	 */
	private String userName;
	
  /**
   * The graphical user interface, accessed through its interface. In return,
   * the GUI will use the CommandsFromWindow interface to call methods to the
   * ChatClient implementation.
   */
	private final CommandsToWindow window ;
	
  /**
   * Constructor for the <code>ChatClient</code>. Must perform the connection to the
   * server. If the connection is not successful, it must exit with an error.
   * 
   * @param window reference to the GUI operating the chat client
   * @param userName the name of the user for this client
   * @since Q1
   */
	public ChatClient(CommandsToWindow window, String userName) throws RemoteException{
		this.window = window;
		this.userName = userName;

		// Initialize RMI registry lookup for the ChatServerManager
		try{
			//registry = LocateRegistry.getRegistry(); // Locate the RMI registry in the local host
			registry = LocateRegistry.getRegistry("127.0.0.1", 1099); //to be used with multi-hosting

			csm = (ChatServerManagerInterface)registry.lookup("ChatServerManager"); // Lookup and cast the remote object to a ChatServerManagerInterface
		} catch (RemoteException e) {
			System.out.println("cannot-locate-registry"); // Handle case where registry lookup fails
			e.printStackTrace();
		} catch (NotBoundException e) {
			System.out.println("cannot-lookup-for-ChatServerManager"); // Handle case where ChatServerManager is not bound in registry
			e.printStackTrace();
		}
	}

	/*
	 * Implementation of the functions from the CommandsFromWindow interface.
	 * See methods description in the interface definition.
	 */

	/**
	 * Sends a new <code>message</code> to the server to propagate to all clients
	 * registered to the chat room <code>roomName</code>.
	 * @param roomName the chat room name
	 * @param message the message to send to the chat room on the server
	 */
	public void sendText(String roomName, String message) {
		try {
			// Lookup the specific ChatServer for the room
			ChatServerInterface chatServer = (ChatServerInterface) registry.lookup("ChatServer_" + roomName);

			// Publish the message to the server, which will forward it to all clients in the room
			chatServer.publish(message, userName); // Assuming the publish method includes a publisher identifier
		} catch (Exception e) {
			System.err.println("Error sending message to room '" + roomName + "': " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Retrieves the list of chat rooms from the server (as a {@link Vector}
	 * of {@link String}s)
	 * @return a list of available chat rooms or an empty Vector if there is
	 * none, or if the server is unavailable
	 * @see Vector
	 */
	public Vector<String> getChatRoomsList() {

		// Attempt to retrieve the list of chat rooms from the ChatServerManager
		try {
			return csm.getRoomsList(); // Call the remote method to get chat rooms list
		} catch (RemoteException e) {
			System.out.println("can-not-call-ChatServerManager.getRoomsList()");
			e.printStackTrace();
			return null; // Return null to indicate failure
		}
	}

	/**
	 * Join the chat room. Does not leave previously joined chat rooms. To
	 * join a chat room we need to know only the chat room's name.
	 * @param name the name (unique identifier) of the chat room
	 * @return <code>true</code> if joining the chat room was successful,
	 * <code>false</code> otherwise
	 */
	public boolean joinChatRoom(String roomName) {

		try {
			// Lookup the chat server for the specific room
			ChatServerInterface chatServer = (ChatServerInterface) registry.lookup("ChatServer_" + roomName);

			// Register this client with the chat server
			chatServer.register(this); // Assuming ChatServerInterface defines a register method accepting CommandsFromServer

			System.out.println("Successfully joined chat room: " + roomName);
			return true;
		} catch (Exception e) {
			System.err.println("Error joining chat room '" + roomName + "': " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Leaves the chat room with the specified name
	 * <code>roomName</code>. The operation has no effect if has not
	 * previously joined the chat room.
	 * @param roomName the name (unique identifier) of the chat room
	 * @return <code>true</code> if leaving the chat room was successful,
	 * <code>false</code> otherwise
	 */	
	public boolean leaveChatRoom(String roomName) {

		try {
			// Lookup the chat server for the specific room
			ChatServerInterface chatServer = (ChatServerInterface) registry.lookup("ChatServer_" + roomName);

			// Unregister this client from the chat server
			chatServer.unregister(this); // Assuming ChatServerInterface defines an unregister method accepting CommandsFromServer

			System.out.println("Successfully left chat room: " + roomName);
			return true;
		} catch (Exception e) {
			System.err.println("Error leaving chat room '" + roomName + "': " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

    /**
     * Creates a new room named <code>roomName</code> on the server.
     * @param roomName the chat room name
     * @return <code>true</code> if chat room was successfully created,
     * <code>false</code> otherwise.
     */
	public boolean createNewRoom(String roomName) {
		try {
			return csm.createRoom(roomName); // Call the method on ChatServerManager
		} catch (RemoteException e) {
			System.err.println("Failed to create new room: " + roomName);
			e.printStackTrace();
			return false;
		}
	}

	/*
	 * Implementation of the functions from the CommandsFromServer interface.
	 * See methods description in the interface definition.
	 */
	
	
	/**
	 * Publish a <code>message</code> in the chat room <code>roomName</code>
	 * of the GUI interface. This method acts as a proxy for the
	 * {@link CommandsToWindow#publish(String chatName, String message)}
	 * interface i.e., when the server calls this method, the {@link
	 * ChatClient} calls the 
	 * {@link CommandsToWindow#publish(String chatName, String message)} method 
	 * of it's window to display the message.
	 * @param roomName the name of the chat room
	 * @param message the message to display
	 */
	public void receiveMsg(String roomName, String message) {
		// Display the received message in the appropriate chat room
		window.publish(roomName, message);
	}
		
	// This class does not contain a main method. You should launch the whole program by launching ChatClientWindow's main method.
}
