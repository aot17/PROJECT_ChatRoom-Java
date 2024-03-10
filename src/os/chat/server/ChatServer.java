package os.chat.server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Vector;

import jdk.jfr.Registered;
import os.chat.client.CommandsFromServer;

/**
 * Each instance of this class is a server for one room.
 * <p>
 * At first there is only one room server, and the names of the room available
 * is fixed.
 * <p>
 * Later you will have multiple room server, each managed by its own
 * <code>ChatServer</code>. A {@link ChatServerManager} will then be responsible
 * for creating and adding new rooms.
 */
public class ChatServer implements ChatServerInterface {
	
	private String roomName;
	private Vector<CommandsFromServer> registeredClients;
	private Registry registry;
	
  /**
   * Constructs and initializes the chat room before registering it to the RMI
   * registry.
   * @param roomName the name of the chat room
   */
	public ChatServer(String roomName){
		this.roomName = roomName;
		registeredClients = new Vector<CommandsFromServer>();

		try {
			// Export the ChatServer object to make it accessible remotely
			ChatServerInterface stub = (ChatServerInterface) UnicastRemoteObject.exportObject(this, 0);
			registry = LocateRegistry.getRegistry(); // Locate the RMI registry on the local host

			// Bind the exported stub in the registry with a unique name based on the chat room name
			registry.rebind("ChatServer_" + roomName, stub);
			System.out.println("ChatServer for room '" + roomName + "' was created and bound in the registry");
		} catch (Exception e) {
			System.out.println("Cannot export ChatServer object or bind it in the registry for room '" + roomName + "'");
			e.printStackTrace();
		}
	}

	/**
	 * Publishes to all subscribed clients (i.e. all clients registered to a
	 * chat room) a message send from a client.
	 * @param message the message to propagate
	 * @param publisher the client from which the message originates
	 */	
	public void publish(String message, String publisher) {
		Vector<CommandsFromServer> clientsToRemove = new Vector<>();

		synchronized (registeredClients) { // Synchronize on the registeredClients vector to prevent concurrent modification issues
			for (CommandsFromServer client : registeredClients) {
				try {
					client.receiveMsg(roomName, publisher + ": " + message);
				} catch (RemoteException e) {
					System.err.println("Failed to send message to a client due to RemoteException. Client will be unregistered.");
					clientsToRemove.add(client); // Add the client to the list of clients to remove
				}
			}

			// Remove all unresponsive clients from the registeredClients list
			for (CommandsFromServer client : clientsToRemove) {
				registeredClients.remove(client);
				System.out.println("Client unregistered due to disconnection: " + client);
			}
		}
	}

	/**
	 * Registers a new client to the chat room.
	 * @param client the name of the client as registered with the RMI
	 * registry
	 */
	public void register(CommandsFromServer client) {

		// Add the client to the registeredClients list
		registeredClients.add(client);
		System.out.println("Client registered: " + client);
	}

	/**
	 * Unregisters a client from the chat room.
	 * @param client the name of the client as registered with the RMI
	 * registry
	 */
	public void unregister(CommandsFromServer client) {

		// Remove the client from the registeredClients list
		registeredClients.remove(client);
		System.out.println("Client unregistered: " + client);
	}

	public String getRoomName() {
		return roomName;
	}
	
}
