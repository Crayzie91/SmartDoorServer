package smartdoor;

import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;
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
import com.thingworx.types.InfoTable;
import com.thingworx.types.primitives.IPrimitiveType;
import com.thingworx.types.primitives.IntegerPrimitive;
import com.thingworx.types.primitives.NumberPrimitive;
import com.thingworx.types.primitives.StringPrimitive;
import com.thingworx.types.properties.Property;


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
	// This property is setup for collecting time series data. Each value
	// that is collected will be pushed to the platfrom from within the
	// processScanRequest() method.
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
	private String ServerName = null;
	private ConnectedThingClient ClientHandle = null;

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
		this.ServerName=name;
		this.ClientHandle=client;
		this.initializeFromAnnotations();
		
//		try {
//			this.setPropertyValue("SetPoint", new IntegerPrimitive(100));
//		} catch (Exception e) {
//			LOG.warn("Could not ser default value for SetPoint");
//		}
	}
	
	/**
	 * This method provides a common interface amongst VirtualThings for processing
	 * periodic requests. It is an opportunity to access data sources, update 
	 * property values, push new values to the server, and take other actions.
	 * 
	 * @see VirtualThing#processScanRequest()
	 */
	@Override
	public void processScanRequest() {

	}
	
	/**
	 * This is where property writes from the server are handled. 
 	 * 
 	 * @see VirtualThing#processPropertyWrite(PropertyDefinition, IPrimitiveType)
 	 */
	@Override
	public void processPropertyWrite(PropertyDefinition property, @SuppressWarnings("rawtypes") IPrimitiveType value) throws Exception {
		String propName = property.getName();
		System.out.println(propName);
		System.out.println("Clients connected was set to: "+value.getValue());
	}
	
	/**
	 * This Method is used to read a Property of a Thing on the Thingworx Platform.
	 * 
	 * @param ServerName Name of the ServerThing
	 * @param PropertyName	Name of the Property to change
	 * @return Returns Object that contains the read value
	 * @throws Exception
	 */
	public Object getProperty(String ServerName, String PropertyName) throws Exception {
		Object var = this.ClientHandle.readProperty(ThingworxEntityTypes.Things, ServerName, PropertyName, 10000).getReturnValue();	
		return var;
	}
	
	/**
	 * This Method is used to write a Property of a Thing on the Thingworx Platform.
	 * Value is casted to a primitive based on its Type.
	 * 
	 * @param ServerName Name of the ServerThing
	 * @param PropertyName	Name of the Property to change
	 * @param value	New Value of the Property
	 * @throws Exception
	 */
	@SuppressWarnings("rawtypes")
	public void setProperty(String ServerName, String PropertyName, Object value) throws Exception {
		IPrimitiveType var=null;
		if(value instanceof Integer)
			var = IntegerPrimitive.convertFromObject(value);
		else if (value instanceof Double)
			var = NumberPrimitive.convertFromObject(value);
		else if (value instanceof String)
			var = StringPrimitive.convertFromObject(value);
		
		this.ClientHandle.writeProperty(ThingworxEntityTypes.Things, ServerName, PropertyName, var, 1000);
	}
	
	/** The following annotation makes a method available to the 
	 	ThingWorx Server for remote invocation.  The annotation includes the
	 	name of the server, the name and base types for its parameters, and 
	 	the base type of its result.
	 	URL: https://developer.thingworx.com/resources/guides/thingworx-java-sdk-quickstart/creating-data-model
	*/
	@ThingworxServiceDefinition(name="addClient", description="Increments the ClientsConnected Property of the ServerThing")
	@ThingworxServiceResult(name="result", description="TRUE if excecution was successfull.", baseType="BOOLEAN")
 	public boolean addClient() throws Exception {
		Object var = getProperty(this.ServerName,"ClientsConnected");
		var=(Double)var+1;
		setProperty(this.ServerName,"ClientsConnected", var);
		
		LOG.info("{} was set. New Value: {}", this.ServerName, var);
		return true;
	}
	
	
}
