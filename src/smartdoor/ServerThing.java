package smartdoor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thingworx.communications.client.ConnectedThingClient;
import com.thingworx.communications.client.things.VirtualThing;
import com.thingworx.metadata.DataShapeDefinition;
import com.thingworx.metadata.FieldDefinition;
import com.thingworx.metadata.PropertyDefinition;
import com.thingworx.metadata.annotations.ThingworxPropertyDefinition;
import com.thingworx.metadata.annotations.ThingworxPropertyDefinitions;
import com.thingworx.metadata.annotations.ThingworxServiceDefinition;
import com.thingworx.metadata.annotations.ThingworxServiceParameter;
import com.thingworx.metadata.annotations.ThingworxServiceResult;
import com.thingworx.metadata.collections.FieldDefinitionCollection;
import com.thingworx.relationships.RelationshipTypes.ThingworxEntityTypes;
import com.thingworx.types.BaseTypes;
import com.thingworx.types.InfoTable;
import com.thingworx.types.collections.ValueCollection;
import com.thingworx.types.primitives.IPrimitiveType;
import com.thingworx.types.primitives.InfoTablePrimitive;
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
			                              "defaultValue:0"})//,
	//@ThingworxPropertyDefinition(name="ConnectedClientsInfo2", description="The number of deliveries the truck has made.", baseType="INFOTABLE", aspects={"isReadOnly:false"}),
})

/**
 * Implementation of the Remote ServerThing.
 * This Class implements and handles the Properties, Services, Events and Subscriptions of the ServerThing.
 * It also implements processScanRequest to handle periodic actions.
 */
public class ServerThing extends VirtualThing {

	private static final Logger LOG = LoggerFactory.getLogger(ServerThing.class);
	private InfoTable ConnectedClientsInfo = new InfoTable(getDataShapeDefinition("SmartDoorClientDataShape"));
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
		this.initializeFromAnnotations();
		
		// Data Shape definition that is used by the delivery stop event
		// The event only has one field, the message
        FieldDefinitionCollection fields = new FieldDefinitionCollection();
        fields.addFieldDefinition(new FieldDefinition("ID", BaseTypes.INTEGER));
        fields.addFieldDefinition(new FieldDefinition("name", BaseTypes.STRING));
        fields.addFieldDefinition(new FieldDefinition("Location", BaseTypes.STRING));
        defineDataShapeDefinition("ClientEntryShape", fields);
        
        ConnectedClientsInfo = new InfoTable(getDataShapeDefinition("ClientEntryShape"));
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
		LOG.info("{} was set. New Value: {}", this.getBindingName(), var);
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
		LOG.info("{} was set. New Value: {}", this.getBindingName(), value);
	}
	
	/**
	 * This method returns an open ID.
	 * 
	 * @return Open ID for new Client
	 * @throws Exception
	 * @see https://developer.thingworx.com/resources/guides/thingworx-java-sdk-quickstart/creating-data-model
	 */
	@ThingworxServiceDefinition(name="getOpenClientID", description="Returns an open ID for the new Client.")
	@ThingworxServiceResult(name="result", description="Open ID for new Client.", baseType="INTEGER")
 	public int getOpenClientID() throws Exception {
		int ID=1;
		ValueCollection filter=new ValueCollection();
		filter.put("name", new StringPrimitive("ClientThing_"+ID));
		//Check for free id	
		for(;ConnectedClientsInfo.find(filter)!=null;) {
			ID++;
			filter.put("name", new StringPrimitive("ClientThing_"+ID));
		}
		
		return ID;
	}
	
	/**
	 * This method increments the ConnectedClient property. And returns ID of new Client.
	 * 
	 * @return Open ID of new Client or -1 if function failed
	 * @throws Exception
	 * @see https://developer.thingworx.com/resources/guides/thingworx-java-sdk-quickstart/creating-data-model
	 */
	@ThingworxServiceDefinition(name="addClient", description="Increments the ClientsConnected Property of the ServerThing. And returns ID of the new Client.")
	@ThingworxServiceResult(name="result", description="ID of created client.", baseType="INTEGER")
 	public int addClient(
 			@ThingworxServiceParameter( name="ID", description="Name of Client.", baseType="INTEGER" ) Integer ID,
 			@ThingworxServiceParameter( name="name", description="Name of Client.", baseType="STRING" ) String name,
 			@ThingworxServiceParameter( name="Location", description="Location of Client.", baseType="STRING") String loc) throws Exception {
		
		ValueCollection payload = new ValueCollection();
		payload.put("ID", new IntegerPrimitive(ID));
		payload.put("name", new StringPrimitive(name));
		payload.put("Location", new StringPrimitive(loc));
		
		try {
			ConnectedClientsInfo.addRow(payload);
			setClientProperty("ClientsConnected", ConnectedClientsInfo.getLength());
		} catch (Exception e) {
			LOG.error("Error occured in addClient: {}.", e);
			return -1;
		}
			
		LOG.info("Client {} was added.", name);
		return ID;
	}
	
	@ThingworxServiceDefinition(name="createClient", description="Creates a ClientThing")
	@ThingworxServiceResult(name="result", description="TRUE if excecution was successfull.", baseType="BOOLEAN")
 	public boolean createClient(
 			@ThingworxServiceParameter( name="name", description="Name of Client.", baseType="STRING" ) String name,
 			@ThingworxServiceParameter( name="description", description="Description for client.", baseType="STRING" ) String desc,
 			@ThingworxServiceParameter( name="thingTemplateName", description="Template for ClientThing.", baseType="STRING") String template) throws Exception {
		
		try {
			ValueCollection payload = new ValueCollection();
			payload.put("name", new StringPrimitive(name));
			payload.put("description", new StringPrimitive(desc));
			payload.put("thingTemplateName", new StringPrimitive(template));
			
			this.getClient().invokeService(ThingworxEntityTypes.Resources, "EntityServices", "CreateThing", payload, 10000);	
			
		} catch (Exception e) {
			LOG.error("Error occured in DeleteClient: {}.", e);
			return false;
		}		
		return true;
	}

	/**
	 * This method increments the ConnectedClient property.
	 * 
	 * @param ID if Client
	 * @return New Number of connected Clients or -1 if function failed
	 * @throws Exception
	 * @see: https://developer.thingworx.com/resources/guides/thingworx-java-sdk-quickstart/creating-data-model
	 */
	@ThingworxServiceDefinition(name="removeClient", description="Decrements the ClientsConnected Property of the ServerThing")
	@ThingworxServiceResult(name="result", description="New Number of connected Clients.", baseType="INTEGER")
 	public int removeClient(
 			@ThingworxServiceParameter( name="ID", description="ID of Client.", baseType="INTEGER" ) Integer ID) throws Exception {
		
		try {
			ValueCollection filter=new ValueCollection();
			filter.put("ID", new IntegerPrimitive(ID));
			ConnectedClientsInfo.delete(filter);
			setClientProperty("ClientsConnected", ConnectedClientsInfo.getLength());
		} catch (Exception e) {
			LOG.error("Error occured in removeClient: {}.", e);
			return -1;
		}
				
		LOG.info("Client with ID {} was deleted.", ID);
		return ConnectedClientsInfo.getLength();
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
	@ThingworxServiceDefinition(name="deleteClient", description="Deletes a ClientThing")
	@ThingworxServiceResult(name="result", description="TRUE if excecution was successfull.", baseType="BOOLEAN")
 	public boolean deleteClient(
 			@ThingworxServiceParameter( name="name", description="Name of Client.", baseType="STRING" ) String name,
 			@ThingworxServiceParameter( name="ID", description="ID of Client.", baseType="INTEGER" ) Integer ID) throws Exception {
		
		try {
			ValueCollection payload = new ValueCollection();
			payload.put("name", new StringPrimitive(name));
			payload.put("ID", new IntegerPrimitive(ID));
			this.getClient().invokeService(ThingworxEntityTypes.Resources, "EntityServices", "DeleteThing", payload, 10000);
			this.getClient().invokeService(ThingworxEntityTypes.Things, "ServerThing", "removeClient", payload, 10000);
		} catch (Exception e) {
			LOG.error("Error occured in DeleteClient: {}.", e);
			return false;
		}		
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
	
	@ThingworxServiceDefinition(name="getConnectedClients", description="Returns a Infotable of all connected clients.")
	@ThingworxServiceResult(name="result", description="List of connected Clients.", baseType="INFOTABLE")
 	public InfoTable getConnectedClients(){
		return ConnectedClientsInfo;
		}
 }
