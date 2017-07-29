package smartdoor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thingworx.communications.client.ClientConfigurator;
import com.thingworx.communications.client.ConnectedThingClient;
import com.thingworx.communications.client.things.VirtualThing;
import com.thingworx.types.properties.Property;

public class ServerThingClient extends ConnectedThingClient {

	private static final Logger LOG = LoggerFactory.getLogger(ServerThingClient.class);
	
	private static String ThingName = "ServerThing";

	public ServerThingClient(ClientConfigurator config) throws Exception {
		super(config);
	}
	
	/*
	 * URL: https://developer.thingworx.com/resources/guides/thingworx-java-sdk-tutorial/understanding-example-client-connection
	 */
	public static void main(String[] args) {
	
		ClientConfigurator config = new ClientConfigurator();
	
		// Set the URI of the server that we are going to connect to
		config.setUri("http://34.227.165.169:80/Thingworx/WS");
		
		// Set the ApplicationKey. This will allow the client to authenticate with the server.
		// It will also dictate what the client is authorized to do once connected.
		config.setAppKey("ce22e9e4-2834-419c-9656-ef9f844c784c");
		
		// This will allow us to test against a server using a self-signed certificate.
		// This should be removed for production systems.
		config.ignoreSSLErrors(true); // All self signed certs
	
		try {
			
			// Create our client.
			ServerThingClient client = new ServerThingClient(config);
			
			// Start the client. The client will connect to the server and authenticate
			// using the ApplicationKey specified above.
			client.start();
			
			// Wait for the client to connect.
			if (client.waitForConnection(30000)) {
				
				LOG.info("The client is now connected.");
				
				//
				// Create a VirtualThing and bind it to the client
				///////////////////////////////////////////////////////////////
				
				// Create a new VirtualThing. The name parameter should correspond with the 
				// name of a RemoteThing on the Platform.
				ServerThing thing = new ServerThing(ThingName, "A basic server thing", client);
				
				// Bind the VirtualThing to the client. This will tell the Platform that
				// the RemoteThing 'Simple1' is now connected and that it is ready to 
				// receive requests.
				client.bindThing(thing);
				
				// This will prevent the main thread from exiting. It will be up to another thread
				// of execution to call client.shutdown(), allowing this main thread to exit.
				while (!client.isShutdown()) {
					
					Thread.sleep(1000);
					
					// Every 15 seconds we tell the thing to process a scan request. This is
					// an opportunity for the thing to query a data source, update property
					// values, and push new property values to the server.
					//
					// This loop demonstrates how to iterate over multiple VirtualThings
					// that have bound to a client. In this simple example the things
					// collection only contains one VirtualThing.
					for (VirtualThing vt : client.getThings().values()) {
						vt.processScanRequest();
					}
				}
				
			} else {
				// Log this as a warning. In production the application could continue
				// to execute, and the client would attempt to reconnect periodically.
				LOG.warn("Client did not connect within 30 seconds. Exiting");
			}
			
		} catch (Exception e) {
			LOG.error("An exception occured during execution.", e);
		}
		
		LOG.info("SimpleThingClient is done. Exiting");
	}
}
