package os.chat.server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Locale;
import java.util.Vector;

/**
 * This class manages the available {@link ChatServer}s and available rooms.
 * <p>
 * At first you should not modify its functionalities but only export
 * them for being called by the {@link ChatClient}.
 * <p>
 * Later you will modify this to allow creating new rooms and
 * looking them up from the {@link ChatClient}.
 */
public class ChatServerManager implements ChatServerManagerInterface {

    /**
     * NOTE: technically this vector is redundant, since the room name can also
     * be retrieved from the chat server vector.
     */
	private Vector<String> chatRoomsList;
	
	private Vector<ChatServer> chatRooms;

    private static ChatServerManager instance = null;

	private Registry registry;
	
	/**
	 * Constructor of the <code>ChatServerManager</code>.
	 * <p>
	 * Must register its functionalities as stubs to be called from RMI by
	 * the {@link ChatClient}.
	 */
	public ChatServerManager () {

		chatRoomsList = new Vector<String>(); // Initialize the list of chat room names
		chatRooms = new Vector<ChatServer>(); // Initialize the list of ChatServer instances
		
		// initial: we create a single chat room and the corresponding ChatServer
		chatRooms.add(new ChatServer("sports"));
		chatRoomsList.add("sports");

		// Export ChatServerManager object and bind it to the RMI registry
		try {
			// Export the ChatServerManager object to make it accessible remotely
			ChatServerManagerInterface stub = (ChatServerManagerInterface) UnicastRemoteObject.exportObject(this, 0);
			registry = LocateRegistry.getRegistry(); // Locate the RMI registry
			registry.rebind("ChatServerManager", stub); // Bind the exported stub in the registry with the name "ChatServerManager"
		} catch (Exception e) {
			System.out.println("can-not-export-the-object");
			e.printStackTrace();
		}
		System.out.println("ChatServerManager-was-created");
	}

    /**
     * Retrieves the chat server manager instance. This method creates a
     * singleton chat server manager instance if none was previously created.
     * @return a reference to the singleton chat server manager instance
     */
    public static ChatServerManager getInstance() {
	if (instance == null)
	    instance = new ChatServerManager();

	return instance;
    }

        /**
	 * Getter method for list of chat rooms.
	 * @return  a list of chat rooms
	 * @see Vector
	 */
	public Vector<String> getRoomsList() {
		return chatRoomsList;
	}

        /**
	 * Creates a chat room with a specified room name <code>roomName</code>.
	 * @param roomName the name of the chat room
	 * @return <code>true</code> if the chat room was successfully created,
	 * <code>false</code> otherwise.
	 */
	public boolean createRoom(String roomName) {
		// Check if the room already exists by iterating over the chatRooms vector
		for (ChatServer server : chatRooms) {
			if (server.getRoomName().equals(roomName)) {
				System.out.println("Room already exists: " + roomName);
				return false; // Room already exists
			}
		}

		try {
			// If the room does not exist, create a new instance of ChatServer for the room
			ChatServer newRoom = new ChatServer(roomName);
			chatRooms.add(newRoom); // Add the new ChatServer instance to the vector
			chatRoomsList.add(roomName); // Also keep track of the room name in a separate list for easy access

			System.out.println("Chat room created: " + roomName);
			return true;
		} catch (Exception e) {
			System.err.println("Failed to create chat room: " + roomName);
			e.printStackTrace();
			return false;
		}
	}

	public static void main(String[] args) {
/**
		// Create the RMI registry on the default port 1099
		try{
			LocateRegistry.createRegistry(1099);
		} catch (RemoteException e) {
			System.out.println("error: cannot-create-registry");
			e.printStackTrace();
		}
		System.out.println("registry-was-created");
		// Initialize the ChatServerManager instance to set up the server environment
		getInstance();
 */

		  		try {
		  			// Set the hostname or IP address that clients will use to connect to this server
		  			System.setProperty("java.rmi.server.hostname", "127.0.0.1");

		  			// Create the RMI registry on the default port 1099
		  			LocateRegistry.createRegistry(1099);
		  			System.out.println("RMI registry was created");

		  			// Initialize the ChatServerManager instance to set up the server environment
		  			getInstance();
		                 } catch (RemoteException e) {
		  			System.out.println("Error: cannot create registry");
		  			e.printStackTrace();
		         }

	}
}