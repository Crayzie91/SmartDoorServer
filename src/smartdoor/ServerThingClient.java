package smartdoor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thingworx.communications.client.ClientConfigurator;
import com.thingworx.communications.client.ConnectedThingClient;
import com.thingworx.communications.client.things.VirtualThing;

public class ServerThingClient extends ConnectedThingClient {

	private static final Logger LOG = LoggerFactory.getLogger(ServerThingClient.class);
	
	private static String ThingName = "ServerThing";

	public ServerThingClient(ClientConfigurator config) throws Exception {
		super(config);
	}
	
	/*
	 * URL: https://developer.thingworx.com/resources/guides/thingworx-java-sdk-tutorial/understanding-example-client-connection
	 * @param args CLI arguments 
     *              [0] Ip of Server 
     *              [1] AppKey
	 */
	public static void main(String[] args) {
	
		ClientConfigurator config = new ClientConfigurator();
		//String uri="http://34.252.164.220:80/Thingworx/WS";
        String uri="http://"+args[0]+":80/Thingworx/WS";
        //String AppKey="ce22e9e4-2834-419c-9656-ef9f844c784c";
        String AppKey=args[1];
	
		// Set the URI of the server that we are going to connect to
		config.setUri(uri);
		
		// Set the ApplicationKey. This will allow the client to authenticate with the server.
		// It will also dictate what the client is authorized to do once connected.
		config.setAppKey(AppKey);
		
		// This will allow us to test against a server using a self-signed certificate.
		// This should be removed for production systems.
		config.ignoreSSLErrors(true); // All self signed certs
	
		try {			
			// Create the Edge Client for the ServerThing.
			ServerThingClient client = new ServerThingClient(config);
			
			// Connect an authenticate to the server by starting the client.
			client.start();
			
			// Wait for the client to connect.
			if (client.waitForConnection(30000)) {
				LOG.info("The client is now connected.");
								
				// Create a new VirtualThing to connect to a thing on the Thingworx platform
				ServerThing thing = new ServerThing(ThingName, "A basic server thing", client);	
				
				// Bind the VirtualThing to the client. This will tell the Platform that
				// the RemoteThing is now connected and that it is ready to receive requests.
				client.bindThing(thing);

				while (!client.isShutdown()) {
					
					Thread.sleep(1000);
					
					// This loop iterates to all VirtualThings connected to the Client and starts 
					// a routine functions.
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
