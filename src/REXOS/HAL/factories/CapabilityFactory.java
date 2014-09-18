package HAL.factories;

import generic.Service;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import util.log.LogLevel;
import util.log.LogSection;
import util.log.Logger;
import HAL.Capability;
import HAL.HardwareAbstractionLayer;
import HAL.JavaSoftware;
import HAL.ModuleIdentifier;
import HAL.exceptions.FactoryException;
import HAL.libraries.dynamicloader.DynamicClassDescription;
import HAL.libraries.dynamicloader.DynamicClassFactory;
import HAL.libraries.dynamicloader.InstantiateClassException;
import HAL.libraries.dynamicloader.JarFileLoaderException;
import HAL.libraries.knowledgedb_client.KnowledgeDBClient;
import HAL.libraries.knowledgedb_client.KnowledgeException;
import HAL.libraries.knowledgedb_client.Row;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * The CapabilityFactory is the factory for the {@link Capability}. 
 * It does not only instantiate capabilities using the {@link DynamicClassFactory} (allowing dynamic addition of classes) but also manages part of the knowledge database.
 * @author Tommas Bakker
 *
 */
public class CapabilityFactory extends Factory{
	// SQL queries
	/**
	 * SQL query for selecting the supported serviceTypes for an equiplet.
	 * Input: equipletName
	 * A serviceType is supported when at least one capabilityType is supported. 
	 * A capabilityType is supported when all the function module trees could be matched with the corresponding physical module trees.
	 */
	private static final String getSupportedServiceTypes = 
			"SELECT serviceType \n" + 
			"FROM ServiceType_CapabilityType \n" + 
			"WHERE NOT EXISTS( \n" +
			"	SELECT * \n" +
			"	FROM CapabilityTypeRequiredMutation \n" +
			"	WHERE ServiceType_CapabilityType.capabilityType = CapabilityTypeRequiredMutation.capabilityType AND \n" +
			"	treeNumber NOT IN( \n" +
			"		SELECT treeNumber \n" +
			"		FROM CapabilityTypeRequiredMutation AS currentRequiredMutation \n" +
			"		JOIN Module AS currentModule \n" +
			"		WHERE ServiceType_CapabilityType.capabilityType = currentRequiredMutation.capabilityType AND \n" +
			"		NOT EXISTS( \n" +
			"			SELECT * \n" +
			"			FROM CapabilityTypeRequiredMutation \n" +
			"			WHERE currentRequiredMutation.capabilityType = capabilityType AND \n" +
			"			currentRequiredMutation.treeNumber = treeNumber AND \n" +
			"			mutation NOT IN( \n" +
			"				SELECT mutation \n" +
			"				FROM SupportedMutation \n" +
			"				JOIN Module ON SupportedMutation.manufacturer = Module.manufacturer AND \n" +
			"					SupportedMutation.typeNumber = Module.typeNumber \n" +
			"				WHERE currentModule.attachedToLeft >= attachedToLeft AND \n" +
			"					currentModule.attachedToRight <= attachedToRight AND \n" +
			"					currentModule.equiplet = ? \n" +
			"			) \n" +
			"		) AND \n" +
			"		currentModule.attachedToRight = currentModule.attachedToLeft + 1 \n" +
			"	) \n" +
			");";
	/**
	 * SQL query for selecting the supported capabilityTypes for an equiplet.
	 * Input: equipletName
	 * A capabilityType is supported when all the functionalModuleTrees could be matched with the corresponding physicalModuleTrees.
	 */
	private static final String getSupportedCapabilityTypes = 
			"SELECT name \n" + 
			"FROM CapabilityType \n" + 
			"WHERE NOT EXISTS( \n" +
			"	SELECT * \n" +
			"	FROM CapabilityTypeRequiredMutation \n" +
			"	WHERE CapabilityType.name = CapabilityTypeRequiredMutation.capabilityType AND \n" +
			"	treeNumber NOT IN( \n" +
			"		SELECT treeNumber \n" +
			"		FROM CapabilityTypeRequiredMutation AS currentRequiredMutation \n" +
			"		JOIN Module AS currentModule \n" +
			"		WHERE CapabilityType.name = currentRequiredMutation.capabilityType AND \n" +
			"		NOT EXISTS( \n" +
			"			SELECT * \n" +
			"			FROM CapabilityTypeRequiredMutation \n" +
			"			WHERE currentRequiredMutation.capabilityType = capabilityType AND \n" +
			"			currentRequiredMutation.treeNumber = treeNumber AND \n" +
			"			mutation NOT IN( \n" +
			"				SELECT mutation \n" +
			"				FROM SupportedMutation \n" +
			"				JOIN Module ON SupportedMutation.manufacturer = Module.manufacturer AND \n" +
			"					SupportedMutation.typeNumber = Module.typeNumber \n" +
			"				WHERE currentModule.attachedToLeft >= attachedToLeft AND \n" +
			"					currentModule.attachedToRight <= attachedToRight AND \n" +
			"					currentModule.equiplet = ? \n" +
			"			) \n" +
			"		) AND \n" +
			"		currentModule.attachedToRight = currentModule.attachedToLeft + 1 \n" +
			"	) \n" +
			");";
	/**
	 * SQL query for selecting the supported capabilityTypes for a serviceType.
	 * Input: serviceTypeName, equipletName
	 * A serviceType is supported when at least one capabilityType is supported. 
	 * A capabilityType is supported when all the function module trees could be matched with the corresponding physical module trees.
	 */
	private static final String getSupportedCapabilityTypesForServiceType = 
			"SELECT * \n" + 
			"FROM CapabilityType \n" + 
			"WHERE \n" +
			"	name IN(\n" +
			"		SELECT CapabilityType \n" +
			"		FROM ServiceType_CapabilityType \n" +
			"		WHERE ServiceType = ? \n" +
			"	) AND NOT EXISTS( \n" +
			"		SELECT * \n" +
			"		FROM CapabilityTypeRequiredMutation \n" +
			"		WHERE CapabilityType.name = CapabilityTypeRequiredMutation.capabilityType AND \n" +
			"		treeNumber NOT IN( \n" +
			"			SELECT treeNumber \n" +
			"			FROM CapabilityTypeRequiredMutation AS currentRequiredMutation \n" +
			"			JOIN Module AS currentModule \n" +
			"			WHERE CapabilityType.name = currentRequiredMutation.capabilityType AND \n" +
			"			NOT EXISTS( \n" +
			"				SELECT * \n" +
			"				FROM CapabilityTypeRequiredMutation \n" +
			"				WHERE currentRequiredMutation.capabilityType = capabilityType AND \n" +
			"				currentRequiredMutation.treeNumber = treeNumber AND \n" +
			"				mutation NOT IN( \n" +
			"					SELECT mutation \n" +
			"					FROM SupportedMutation \n" +
			"					JOIN Module ON SupportedMutation.manufacturer = Module.manufacturer AND \n" +
			"						SupportedMutation.typeNumber = Module.typeNumber \n" +
			"					WHERE currentModule.attachedToLeft >= attachedToLeft AND \n" +
			"						currentModule.attachedToRight <= attachedToRight AND \n" +
			"						currentModule.equiplet = ? \n" +
			"				) \n" +
			"			) AND \n" +
			"			currentModule.attachedToRight = currentModule.attachedToLeft + 1 \n" +
			"		) \n" +
			"	);";
	
	/**
	 * SQL query for adding a capabilityType.
	 * Input: capabilityTypeName, halSoftwareId
	 */
	private static final String addCapabilityType =
			"INSERT IGNORE INTO CapabilityType \n" + 
			"(name, halSoftware) \n" + 
			"VALUES(?, ?);";
	
	/**
	 * SQL query for adding a serviceType.
	 * Input: serviceTypeName
	 */
	private static final String addServiceType =
			"INSERT IGNORE INTO ServiceType \n" + 
			"(name) \n" + 
			"VALUES(?);";
	/**
	 * SQL query for selecting the serviceTypes for a capabilityType.
	 * Input: capabilityTypeName
	 */
	private static final String getServiceTypesForCapabilityType =
			"SELECT serviceType \n" + 
			"FROM ServiceType_CapabilityType \n" + 
			"WHERE capabilityType = ?;";
	
	/**
	 * SQL query for adding a required mutation to a capabilityType.
	 * Input: treeNumber, capabilityTypeName, mutation
	 */
	private static final String addRequiredMutationForCapabilityType =
			"INSERT IGNORE INTO CapabilityTypeRequiredMutation \n" + 
			"(treeNumber, capabilityType, mutation) \n" + 
			"VALUES(?, ?, ?);";
	/**
	 * SQL query for selecting required mutations for a capabilityType.
	 * Input: capabilityTypeName
	 */
	private static final String getRequiredMutationsForCapabilityType =
			"SELECT mutation, treeNumber \n" + 
			"FROM CapabilityTypeRequiredMutation \n" + 
			"WHERE capabilityType = ?;";
	
	/**
	 * SQL query for selecting all the associated capabilityTypes to a ModuleIdentifier.
	 * Input: ModuleIdentifierManufacturer, ModuleIdentifierTypeNumber
	 * A capabilityTypes is considered associated when at least one required mutation matches with a supported mutation of this module type (which is identified with by {@link ModuleIdentifier}).
	 */
	private static final String getAllAssociatedCapabilityTypesForModuleIdentifier = 
			"SELECT DISTINCT capabilityType \n" + 
			"FROM CapabilityTypeRequiredMutation \n" + 
			"WHERE mutation IN( \n" + 
			"	SELECT mutation \n" + 
			"	FROM SupportedMutation \n" + 
			"	WHERE manufacturer = ? AND \n" + 
			"		typeNumber = ? \n" + 
			");";
	
	/**
	 * SQL query for adding a relation between a serviceType and a capabilityType.
	 * Input: serviceTypeName, capabilityTypeName
	 */
	private static final String addServiceType_CapabilityType = 
			"INSERT IGNORE INTO ServiceType_CapabilityType \n" + 
					"(serviceType,capabilityType) \n" + 
					"VALUES(?, ?);";
	
	/**
	 * The {@link DynamicClassFactory} used by the CapabilityFactory to load classes of capabilities.
	 */
	private DynamicClassFactory<Capability> dynamicClassFactory;
	private HardwareAbstractionLayer hal;
	
	/**
	 * Constructs a new CapabilityFactory with a new {@link KnowledgeDBClient}.
	 * @param hal
	 * @throws KnowledgeException
	 */
	public CapabilityFactory(HardwareAbstractionLayer hal) throws KnowledgeException {
		super(new KnowledgeDBClient());
		this.hal = hal;
		this.dynamicClassFactory = new DynamicClassFactory<>();
	}
	
	/**
	 * This method will all return all the capabilities supported by this equiplet.
	 * @return
	 * @throws Exception
	 */
	public ArrayList<Capability> getAllSupportedCapabilities() throws Exception{
		ArrayList<Capability> capabilities = new ArrayList<Capability>();
		
		Row[] rows = knowledgeDBClient.executeSelectQuery(getSupportedCapabilityTypes, hal.getEquipletName());
		for (Row row : rows) {
			String capabilityName = (String) row.get("name");
			capabilities.add(this.getCapabilityByName(capabilityName));
		}
		return capabilities;
	}
	/**
	 * This method will all return all the services supported by this equiplet.
	 * @return
	 */
	public ArrayList<Service> getAllSupportedServices() {
		ArrayList<Service> services = new ArrayList<Service>();
		
		Row[] rows = knowledgeDBClient.executeSelectQuery(getSupportedServiceTypes, hal.getEquipletName());
		for (Row row : rows) {
			String serviceName = (String) row.get("serviceType");
			services.add(new Service(serviceName));
		}
		return services;
	}
	/**
	 * This method will all return all the capabilities associated with the service and supported by this equiplet. 
	 * @param service
	 * @return
	 * @throws Exception
	 */
	public ArrayList<Capability> getCapabilitiesForService(Service service) {
		ArrayList<Capability> capabilities = new ArrayList<Capability>();
		
		Row[] rows = knowledgeDBClient.executeSelectQuery(getSupportedCapabilityTypesForServiceType, service.getName(), hal.getEquipletName());
		for (Row row : rows) {
			String capabilityName = (String) row.get("name");
			try{
				capabilities.add(this.getCapabilityByName(capabilityName));
			} catch (FactoryException | JarFileLoaderException ex) {
				Logger.log(LogSection.HAL_CAPABILITY_FACTORY, LogLevel.ERROR, "Unable to get capability with name: " + capabilityName, ex);
			}
		}
		return capabilities;
	}
	/**
	 * This method will return the instantiated capability for the capabilityTypeName.
	 * If the capability has not been instantiated, it will be instantiated by downloading the software from the knowledge database and dynamically loading the class.
	 * @param capabilityName
	 * @return
	 * @throws FactoryException 
	 * @throws JarFileLoaderException 
	 * @throws  
	 */
	private Capability getCapabilityByName(String capabilityTypeName) throws FactoryException, JarFileLoaderException {
		JavaSoftware javaSoftware = JavaSoftware.getJavaSoftwareForCapabilityName(capabilityTypeName);
		DynamicClassDescription description = javaSoftware.getDynamicClassDescription();
		try {
			Class<Capability> capabilityClass = dynamicClassFactory.getClassFromDescription(description);
			return capabilityClass.getConstructor(ModuleFactory.class).newInstance(hal.getModuleFactory());
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException | InstantiateClassException ex) {
			throw new FactoryException("well, we are fucked", ex);
		}
	}

	/**
	 * This method will insert a array of capabilityTypes into the knowledge database, using the data provided in the JSONArray.
	 * @param capabilityTypes
	 * @return true if successful, false otherwise
	 */
	public boolean insertCapabilityTypes(JSONArray capabilityTypes) {
		try{
			try{
				for (int i = 0; i < capabilityTypes.length(); i++) {
					JSONObject capabilityTypeEntry = capabilityTypes.getJSONObject(i);
					String name = capabilityTypeEntry.getString("name");
					
					JSONObject capabilitySoftware = capabilityTypeEntry.getJSONObject("halSoftware");
					JavaSoftware halSoftware = JavaSoftware.insertJavaSoftware(capabilitySoftware, knowledgeDBClient);
					int halSoftwareId = halSoftware.getId();
					
					knowledgeDBClient.executeUpdateQuery(addCapabilityType, name, halSoftwareId);
					//TODO update behavior for the required mutations
					JSONArray requiredMutationsTrees = capabilityTypeEntry.getJSONArray("requiredMutationsTrees");
					deserializeRequiredMutations(name, requiredMutationsTrees);
					
					for (int j = 0; j < capabilityTypeEntry.length(); j++) {
						String serviceName = capabilityTypeEntry.getJSONArray("services").getString(j);
						knowledgeDBClient.executeUpdateQuery(addServiceType, serviceName);
						knowledgeDBClient.executeUpdateQuery(addServiceType_CapabilityType, serviceName,name);
					}
				}
			} catch(Exception ex) {
				Logger.log(LogSection.HAL_CAPABILITY_FACTORY, LogLevel.WARNING, "Error occured while inserting capability ", ex);
				knowledgeDBClient.getConnection().rollback();
				knowledgeDBClient.getConnection().setAutoCommit(true);
				return false;
			}
		} catch (SQLException ex) {
			return false;
		}
		return true;
	}
	/**
	 * This method will serialize all the capabilityTypes associated with the moduleType (which is identified by the {@link ModuleIdentifier}).
	 * This method will also remove all the capabilityTypes which have become obsolete after removing the module type. 
	 * A capabilityType is considered to be obsolete if none of the required mutations matches a supported mutation.
	 * @param moduleIdentifier
	 * @return The serialized associated capabilities.
	 */
	public JSONArray removeCapabilities(ModuleIdentifier moduleIdentifier) {
		ArrayList<String> capabilityNames = new ArrayList<String>();
		try{
			try{
				Row[] rows = knowledgeDBClient.executeSelectQuery(getAllAssociatedCapabilityTypesForModuleIdentifier, 
						moduleIdentifier.getManufacturer(), moduleIdentifier.getTypeNumber());
				for (Row row : rows) {
					capabilityNames.add((String) row.get("capabilityType"));
				}
				
				JSONArray capabilities = new JSONArray();
				for (String capabilityName : capabilityNames) {
					JSONObject capability = new JSONObject();
					capability.put("name", capabilityName);
					
					JavaSoftware javaSoftware = JavaSoftware.getJavaSoftwareForCapabilityName(capabilityName);
					capability.put("halSoftware", javaSoftware.serialize());
					
					capability.put("requiredMutationsTrees", serializeRequiredMutations(capabilityName));
					
					//TODO actually remove the capability
					JSONArray services = new JSONArray();
					Row[] serviceRows = knowledgeDBClient.executeSelectQuery(getServiceTypesForCapabilityType, capabilityName);
					for (Row serviceRow : serviceRows) {
						services.put(serviceRow.get("serviceType"));
					}
					capability.put("services", services);
					
					capabilities.put(capability);
				}
				return capabilities;
			} catch(Exception ex) {
				Logger.log(LogSection.HAL_CAPABILITY_FACTORY, LogLevel.WARNING, "Error occured while removing capability ", ex);
				knowledgeDBClient.getConnection().rollback();
				knowledgeDBClient.getConnection().setAutoCommit(true);
				return null;
			}
		} catch (SQLException ex) {
			return null;
		}
	}
	
	/**
	 * This method will serialize all the required mutations of a capabilityType from the knowledge database, but does NOT remove them.
	 * @param capabilityTypeName
	 * @return
	 * @throws JSONException 
	 */
	private JSONArray serializeRequiredMutations(String capabilityTypeName) throws JSONException {
		HashMap<Integer, JSONObject> requiredTreesMap = new HashMap<Integer, JSONObject>();
		Row[] rows = knowledgeDBClient.executeSelectQuery(getRequiredMutationsForCapabilityType, 
				capabilityTypeName);
		for (Row row : rows) {
			Integer treeNumber = (Integer) row.get("treeNumber");
			String mutation = (String) row.get("mutation");
			
			if(requiredTreesMap.containsKey(treeNumber) == false) {
				JSONObject tree = new JSONObject();
				tree.put("treeNumber", treeNumber);
				tree.put("mutations", new JSONArray());
				requiredTreesMap.put(treeNumber, tree);
			}
			
			requiredTreesMap.get(treeNumber).getJSONArray("mutations").put(mutation);
		}
		JSONArray requiredMutationTrees = new JSONArray();
		for (JSONObject entry : requiredTreesMap.values()) {
			requiredMutationTrees.put(entry);
		}
		return requiredMutationTrees;
	}
	/**
	 * This method deserializes the required mutations and stores them in the knowledge database.
	 * @param capabilityTypeName
	 * @param requiredMutationTrees
	 * @throws JSONException 
	 */
	private void deserializeRequiredMutations(String capabilityTypeName, JSONArray requiredMutationTrees) throws JSONException {
		for (int i = 0; i < requiredMutationTrees.length(); i++) {
			JSONObject requiredMutationTree = requiredMutationTrees.getJSONObject(i);
			Integer requiredMutationTreeNumber = requiredMutationTree.getInt("treeNumber");
			JSONArray requiredMutations = requiredMutationTree.getJSONArray("mutations");
			for (int j = 0; j < requiredMutations.length(); j++) {
				String requiredMutation = requiredMutations.getString(j);
				knowledgeDBClient.executeUpdateQuery(addRequiredMutationForCapabilityType, 
						requiredMutationTreeNumber, capabilityTypeName, requiredMutation);
			}
		}
	}
}