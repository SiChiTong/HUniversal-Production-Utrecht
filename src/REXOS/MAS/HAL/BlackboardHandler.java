package HAL;

import java.net.UnknownHostException;
import java.util.ArrayList;

import libraries.blackboard_client.BlackboardClient;
import libraries.blackboard_client.data_classes.BlackboardSubscriber;
import libraries.blackboard_client.data_classes.FieldUpdateSubscription;
import libraries.blackboard_client.data_classes.FieldUpdateSubscription.MongoUpdateLogOperation;
import libraries.blackboard_client.data_classes.GeneralMongoException;
import libraries.blackboard_client.data_classes.InvalidDBNamespaceException;
import libraries.blackboard_client.data_classes.InvalidJSONException;
import libraries.blackboard_client.data_classes.MongoOperation;
import libraries.blackboard_client.data_classes.OplogEntry;
import libraries.utillities.log.LogLevel;
import libraries.utillities.log.LogSection;
import libraries.utillities.log.Logger;
import HAL.exceptions.BlackboardUpdateException;
import HAL.listeners.BlackboardListener;

import com.google.gson.JsonObject;
import com.mongodb.DBObject;

import configuration.Configuration;
import configuration.ConfigurationFiles;


/**
 * 
 * @author Aristides Ayala Mendoza
 *
 */
public class BlackboardHandler implements BlackboardSubscriber {
	
	private ArrayList<BlackboardListener> updateSubscribers;
	
	private BlackboardClient stateBlackboardBBClient;
	private BlackboardClient modeBlackboardBBClient;
	private BlackboardClient equipletStepBBClient;
	
	private FieldUpdateSubscription stateSubscription;
	private FieldUpdateSubscription modeSubscription;
	private FieldUpdateSubscription statusSubscription;
	
	private String equipletName;
	private String state = null;
	private String mode = null;
	
	/**
	 * @throws BlackboardUpdateException 
	 * 
	 */
	public BlackboardHandler(String equipletName) throws BlackboardUpdateException{
		this.equipletName = equipletName;
		updateSubscribers = new ArrayList<BlackboardListener>();
			
		try {
			stateSubscription = new FieldUpdateSubscription("state", this);
			stateSubscription.addOperation(MongoUpdateLogOperation.SET);
			
			stateBlackboardBBClient = new BlackboardClient(Configuration.getProperty(ConfigurationFiles.MONGO_DB_PROPERTIES, "collectiveDbIp"));
			stateBlackboardBBClient.setDatabase(Configuration.getProperty(ConfigurationFiles.MONGO_DB_PROPERTIES, "stateBlackBoardName"));
			stateBlackboardBBClient.setCollection(Configuration.getProperty(ConfigurationFiles.MONGO_DB_PROPERTIES, "equipletStateCollectionName"));
			stateBlackboardBBClient.subscribe(stateSubscription);
			
			
			modeSubscription = new FieldUpdateSubscription("mode", this);
			modeSubscription.addOperation(MongoUpdateLogOperation.SET);
			
			modeBlackboardBBClient = new BlackboardClient(Configuration.getProperty(ConfigurationFiles.MONGO_DB_PROPERTIES, "collectiveDbIp"));
			modeBlackboardBBClient.setDatabase(Configuration.getProperty(ConfigurationFiles.MONGO_DB_PROPERTIES, "stateBlackBoardName"));
			modeBlackboardBBClient.setCollection(Configuration.getProperty(ConfigurationFiles.MONGO_DB_PROPERTIES, "equipletStateCollectionName"));
			modeBlackboardBBClient.subscribe(modeSubscription);
			
			
			statusSubscription = new FieldUpdateSubscription("status", this);
			statusSubscription.addOperation(MongoUpdateLogOperation.SET);
		   
			equipletStepBBClient = new BlackboardClient(Configuration.getProperty(ConfigurationFiles.EQUIPLET_DB_PROPERTIES, "DbIp", equipletName));
			equipletStepBBClient.setDatabase(Configuration.getProperty(ConfigurationFiles.EQUIPLET_DB_PROPERTIES, "DbName", equipletName));
			equipletStepBBClient.setCollection(Configuration.getProperty(ConfigurationFiles.EQUIPLET_DB_PROPERTIES, "EquipletStepsBlackBoardName", equipletName));
			equipletStepBBClient.subscribe(statusSubscription);
		} catch (InvalidDBNamespaceException | UnknownHostException | GeneralMongoException ex) {
			throw new BlackboardUpdateException("Unable to initialize HAL.BlackBoardHandler", ex);
		}
	}
	

	/**
	 * 
	 * @param blackboardListener
	 */
	public void addBlackboardListener(BlackboardListener blackboardListener){
		updateSubscribers.add(blackboardListener);
	}

	/**
	 * @see BlackboardSubscriber#onMessage(MongoOperation, OplogEntry)
	 */
	@Override
	public void onMessage(MongoOperation operation, OplogEntry entry) {
		DBObject dbObject;
		try{
			switch (entry.getNamespace().split("\\.")[1]) {
				case "equipletState":
					Logger.log(LogSection.HAL_BLACKBOARD, LogLevel.DEBUG, "EQ state or mode changed");
					dbObject = stateBlackboardBBClient.findDocumentById(entry.getTargetObjectId());
					if(dbObject != null) {
						equipletName = dbObject.get("equipletName").toString();
						if(dbObject.containsField("state")){
							state = dbObject.get("state").toString();
						}
						if(dbObject.containsField("mode")){
							mode = dbObject.get("mode").toString();
						}	
						for(BlackboardListener listener: updateSubscribers){
							listener.OnEquipletStateChanged(equipletName,state);
							listener.OnEquipletModeChanged(equipletName, mode);
						}
					}
					break;
				case "EquipletStepsBlackBoard":
					dbObject = equipletStepBBClient.findDocumentById(entry.getTargetObjectId());
					if(dbObject != null) {
						String status = dbObject.get("status").toString();
						Logger.log(LogSection.HAL_BLACKBOARD, LogLevel.DEBUG, "EQ step process status changed");
						
						for(BlackboardListener listener: updateSubscribers) {
							listener.onProcessStatusChanged(status); 
						}
					}
				    break;
				default:
					break;
			}
		}catch(InvalidDBNamespaceException | GeneralMongoException ex) {
			Logger.log(LogLevel.ERROR, "Unknown exception occured:", ex);
		}
	}
	
	public void postHardwareStep(JsonObject hardwareStep) throws InvalidJSONException, InvalidDBNamespaceException, GeneralMongoException {
		equipletStepBBClient.insertDocument(hardwareStep.toString());
	}

}