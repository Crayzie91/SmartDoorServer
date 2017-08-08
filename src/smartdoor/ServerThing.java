package smartdoor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thingworx.communications.client.ConnectedThingClient;
import com.thingworx.communications.client.things.VirtualThing;
import com.thingworx.metadata.PropertyDefinition;
import com.thingworx.metadata.annotations.ThingworxPropertyDefinition;
import com.thingworx.metadata.annotations.ThingworxPropertyDefinitions;
import com.thingworx.metadata.annotations.ThingworxServiceDefinition;
import com.thingworx.metadata.annotations.ThingworxServiceParameter;
import com.thingworx.metadata.annotations.ThingworxServiceResult;
import com.thingworx.relationships.RelationshipTypes.ThingworxEntityTypes;
import com.thingworx.types.collections.ValueCollection;
import com.thingworx.types.primitives.IPrimitiveType;
import com.thingworx.types.primitives.IntegerPrimitive;
import com.thingworx.types.primitives.NumberPrimitive;
import com.thingworx.types.primitives.StringPrimitive;

import ch.maxant.rules.CompileException;
import ch.maxant.rules.DuplicateNameException;
import ch.maxant.rules.NoMatchingRuleFoundException;
import ch.maxant.rules.ParseException;

/**
Aspects:	
  	dataChangeType:
  	Specifies when a DataChange Event may be triggered for a Property value change. The options are as follows:
	Always:  Fire the event to subscribers for any property value change
	Never:  Do not fire a change event
	On:  For most values, any change will trigger this.  For more complex data types, such as InfoTables, refer to the specific product documentation.
	Off:  Fire the event if the new value evaluates to a Boolean false
	Value:  For Numbers, if the new value has changed by more than the threshold value, fire the change event.  For non-Numbers, this setting behaves the same as Always except in the case of logging to value streams. 
			Non-Number properties will only log to value streams using the Value data change type if the value actually changes from the previous value.
	
	dataChangeThreshold:
	Specifies if the property value should be persisted through a Thing or system restart. Properties that are not persistent will reset their values to their default after a Thing or system restart.
	
	cacheTime:
	
	isPersistent:
	Specifies if the property value should be persisted through a Thing or system restart. Properties that are not persistent will reset their values to their default after a Thing or system restart.
	
	isReadOnly:
	Specifies if the property value is read-only. If true, the property value can not be changed.
	
	pushType:
	Edge Thing Properties have the ability to push their value changes to the server. This ability is configurable via the server property binding. The configuration choices are as follows:
	Value:  Pushed based on Value Change - you can also configure a value change threshold
	Never:  Never pushed
	Always:  Always pushed every change
	
	defaultValue:
	If the property is configured to have a default value, this value will be present at Thing initialization unless otherwise overwritten by a Persistent value set before system shutdown.		                    		 	

	 */
@SuppressWarnings("serial")
@ThingworxPropertyDefinitions(properties = {
	@ThingworxPropertyDefinition(name="ClientsConnected",       
			                     description="Number of connected CLients",
			                     baseType="NUMBER",
			                     aspects={"dataChangeType:VALUE",
			                    		  "dataChangeThreshold:0",
			                    		  "cacheTime:0", 
			                    		  "isPersistent:FALSE", 
			                    		  "isReadOnly:FALSE", 
			                    		  "pushType:ALWAYS", 
			                              "defaultValue:0"}),
})

/**
 * Implementation of the Remote ServerThing.
 * This Class implements and handles the Properties, Services, Events and Subscriptions of the ServerThing.
 * It also implements processScanRequest to handle periodic actions.
 */
public class ServerThing extends VirtualThing {

	private static final Logger LOG = LoggerFactory.getLogger(ServerThing.class);
	private final String ServerName;
	private final ConnectedThingClient ClientHandle;
	private boolean[] ConnectedClientsArr = new boolean[10];
	private RuleEngine eng = new RuleEngine();

	/**
	 * A custom constructor. The Constructor is needed to call initializeFromAnnotations,
	 * which processes all of the VirtualThing's annotations and applies them to the
	 * object.
	 * 
	 * @param name The name of the thing.
	 * @param description A description of the thing.
	 * @param client The client that this thing is associated with.
	 * 
	 * URL: https://developer.thingworx.com/resources/guides/thingworx-java-sdk-quickstart/creating-data-model
	 */
	public ServerThing(String name, String description, ConnectedThingClient client) {
		super(name, description, client);
		ServerName = name;
		ClientHandle=client;
		this.initializeFromAnnotations();
	}
	
	/**	
	 * This method will get called when a connect or reconnect happens
	 * The called functions synchronize the state and the properties of the virtual thing
	 */
	@Override
	public void synchronizeState() {
		super.synchronizeState();
		// Send the property values to ThingWorx when a synchronization is required
		super.syncProperties();
	}
	
	/**
	 * This method provides a common interface amongst the VirtualThings for processing
	 * periodic requests. It is an opportunity to access data sources, update 
	 * property values, push new values to the server, and take other actions.
	 * 
	 * @see VirtualThing#processScanRequest()
	 */
	@Override
	public void processScanRequest() {
		try {
			this.updateSubscribedProperties(1000);
			this.updateSubscribedEvents(1000);
		}
		catch(Exception eProcessing) {
			System.out.println("Error Processing Scan Request");
			}	
	}
	
	/**
	 * This function handles the property writes from the server
 	 * 
 	 * @see VirtualThing#processPropertyWrite(PropertyDefinition, IPrimitiveType)
 	 */
	@Override
	public void processPropertyWrite(PropertyDefinition property, @SuppressWarnings("rawtypes") IPrimitiveType value) throws Exception {
		String propName = property.getName();
		setProperty(propName,value);		
		LOG.info("{} was set. New Value: {}", propName, value);
	}
		
	/**
	 * This Method is used to read a Property of a Thing on the Thingworx Platform.
	 * 
	 * @param PropertyName	Name of the Property to change
	 * @return Returns Object that contains the read value
	 * @throws Exception
	 */
	public Object getClientProperty(String PropertyName) {
		Object var = getProperty(PropertyName).getValue().getValue();	
		LOG.info("{} was set. New Value: {}", this.ServerName, var);
		return var;
	}
	
	/**
	 * This Method is used to write a Property of a Thing on the Thingworx Platform.
	 * Value is casted to a generic type for further use.
	 * 
	 * @param PropertyName	Name of the Property to change
	 * @param value	New Value of the Property
	 * @throws Exception
	 */
	public void setClientProperty(String PropertyName, Object value) throws Exception{
		setProperty(PropertyName, value);		
		LOG.info("{} was set. New Value: {}", this.ServerName, value);
	}
	
	/**
	 * This method returns an open ID.
	 * 
	 * @return Open ID for new Client
	 * @throws Exception
	 * @see https://developer.thingworx.com/resources/guides/thingworx-java-sdk-quickstart/creating-data-model
	 */
	public int getOpenClientID() throws Exception {
		//Check for free id
		for(int i = 0; i<ConnectedClientsArr.length; i++) {
			if(ConnectedClientsArr[i] == false)
				return i;
		}
		//if no free id was found
		return -1;
	}
	
	/**
	 * This method increments the ConnectedClient property. And returns ID of new Client.
	 * 
	 * @return Open ID of new Client.
	 * @throws Exception
	 * @see https://developer.thingworx.com/resources/guides/thingworx-java-sdk-quickstart/creating-data-model
	 */
	@ThingworxServiceDefinition(name="addClient", description="Increments the ClientsConnected Property of the ServerThing. And returns an open ID for the new Client.")
	@ThingworxServiceResult(name="result", description="TRUE if excecution was successfull.", baseType="INTEGER")
 	public int addClient() throws Exception {
		Object var = getClientProperty("ClientsConnected");
		var=(Double)var+1;
		setClientProperty("ClientsConnected", var);
		
		int ID = getOpenClientID();
		this.ConnectedClientsArr[ID]=true;
		
		LOG.info("{} was set. New Value: {}", this.ServerName, var);
		return ID;
	}

	/**
	 * This method increments the ConnectedClient property.
	 * 
	 * @param ID if Client
	 * @return TRUE if execution was successful
	 * @throws Exception
	 * @see: https://developer.thingworx.com/resources/guides/thingworx-java-sdk-quickstart/creating-data-model
	 */
	@ThingworxServiceDefinition(name="removeClient", description="Decrements the ClientsConnected Property of the ServerThing")
	@ThingworxServiceResult(name="result", description="TRUE if excecution was successfull.", baseType="BOOLEAN")
 	public boolean removeClient(
 			@ThingworxServiceParameter( name="ID", description="ID of Client.", baseType="INTEGER" ) int ID) throws Exception {
		boolean success=true;
		Object var = getClientProperty("ClientsConnected");
		
		if((double)var<1) {
			LOG.info("All Clients are disconnected.");
			success=false;
		}
		else
			var=(Double)var-1;
			setClientProperty("ClientsConnected", var);
		
		this.ConnectedClientsArr[(ID-1)] = false;
		
		LOG.info("Client with ID {} was deleted.", ID);
		return success;
	}
	
	/**
	 * This method deletes a ClientThing.
	 * 
	 * @param name Name of Client
	 * @param ID ID of Client
	 * @return TRUE if execution was successful
	 * @throws Exception
	 * @see https://developer.thingworx.com/resources/guides/thingworx-java-sdk-quickstart/creating-data-model
	 */
	@ThingworxServiceDefinition(name="DeleteClient", description="Deletes a ClientThing")
	@ThingworxServiceResult(name="result", description="TRUE if excecution was successfull.", baseType="BOOLEAN")
 	public boolean DeleteClient(
 			@ThingworxServiceParameter( name="name", description="Name of Client.", baseType="STRING" ) String name,
 			@ThingworxServiceParameter( name="ID", description="ID of Client.", baseType="INTEGER" ) Integer ID) throws Exception {
		
		ValueCollection payload = new ValueCollection();
		payload.put("name", new StringPrimitive(name));
		payload.put("ID", new IntegerPrimitive(ID));
		ClientHandle.invokeService(ThingworxEntityTypes.Resources, "EntityServices", "DeleteThing", payload, 10000);
		ClientHandle.invokeService(ThingworxEntityTypes.Things, "ServerThing", "removeClient", payload, 10000);
		
		return true;
	}
	
	@ThingworxServiceDefinition(name="checkRules", description="Checks rules for entering person")
	@ThingworxServiceResult(name="result", description="TRUE if person is granted access.", baseType="BOOLEAN")
 	public boolean checkRules(
 			@ThingworxServiceParameter( name="name", description="Name of Client.", baseType="STRING" ) String name){	

		try {
			eng.checkRules(name);
		} catch (NoMatchingRuleFoundException e) {
			LOG.info("No matching rule was found for {}.", name);
			return false;
		} catch (Exception e) {
			LOG.error("Error occured in checkRules: {}.", e);	
			return false;
		}
		
		return true;
 	}
 }
