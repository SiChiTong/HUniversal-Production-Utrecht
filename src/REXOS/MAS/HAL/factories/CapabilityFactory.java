package HAL.factories;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.codec.binary.Base64;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import libraries.dynamicloader.DynamicClassDescription;
import libraries.dynamicloader.DynamicClassFactory;
import libraries.knowledgedb_client.KeyNotFoundException;
import libraries.knowledgedb_client.KnowledgeDBClient;
import libraries.knowledgedb_client.KnowledgeException;
import libraries.knowledgedb_client.Row;
import HAL.Capability;
import HAL.HardwareAbstractionLayer;
import HAL.JavaSoftware;
import HAL.ModuleIdentifier;
import HAL.Service;

public class CapabilityFactory extends Factory{
	// SQL queries
	private static final String getSupportedCapabilities = 
			"SELECT * \n" + 
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
	private static final String getSupportedCapabilitiesForService = 
			"SELECT * \n" + 
			"FROM CapabilityType \n" + 
			"WHERE \n" +
			"	name IN(\n" +
			"		SELECT * CapabilityType\n" +
			"		FROM ServiceType_CapabilityType\n" +
			"		WHERE ServiceType = ?\n" +
			"	) AND NOT EXISTS( \n" +
			"		SELECT * \n" +
			"		FROM CapabilityTypeDependencySet \n" +
			"		WHERE CapabilityType.name =CapabilityTypeDependencySet.capabilityType AND \n" +
			"		treeNumber NOT IN( \n" +
			"			SELECT treeNumber \n" +
			"			FROM CapabilityTypeDependencySet AS currentDependencySet \n" +
			"			JOIN Module AS currentModule \n" +
			"			WHERE CapabilityType.name = currentDependencySet.capabilityType AND \n" +
			"			NOT EXISTS( \n" +
			"				SELECT * \n" +
			"				FROM CapabilityTypeDependencySet \n" +
			"				WHERE currentDependencySet.capabilityType = capabilityType AND \n" +
			"				currentDependencySet.treeNumber = treeNumber AND \n" +
			"				mutation NOT IN( \n" +
			"					SELECT mutation \n" +
			"					FROM ModuleCommandType \n" +
			"					JOIN Module ON ModuleCommandType.manufacturer = Module.manufacturer AND \n" +
			"						ModuleCommandType.typeNumber = Module.typeNumber \n" +
			"					WHERE currentModule.attachedToLeft >= attachedToLeft AND \n" +
			"						currentModule.attachedToRight <= attachedToRight AND \n" +
			"						currentModule.equiplet = ? \n" +
			"				) \n" +
			"			) AND \n" +
			"			currentModule.attachedToRight = currentModule.attachedToLeft + 1 \n" +
			"		) \n" +
			"	);";
	
	private static final String addCapabilityType =
			"INSERT INTO CapabilityType \n" + 
			"(name, halSoftware) \n" + 
			"VALUES(?, ?);";
	
	private static final String addRequiredMutationForCapabilityType =
			"INSERT INTO CapabilityTypeRequiredMutation \n" + 
			"(treeNumber, capabilityType, mutation) \n" + 
			"VALUES(?, ?, ?);";
	private static final String getRequiredMutationsForCapabilityType =
			"SELECT mutation, treeNumber \n" + 
			"FROM CapabilityTypeRequiredMutation \n" + 
			"WHERE capabilityType = ?;";
	
	private DynamicClassFactory<Capability> dynamicClassFactory;
	private HardwareAbstractionLayer hal;
	
	public CapabilityFactory(HardwareAbstractionLayer hal) throws KnowledgeException {
		super(new KnowledgeDBClient());
		this.hal = hal;
	}
	
	public ArrayList<Capability> getAllSupportedCapabilities() throws Exception{
		ArrayList<Capability> capabilities = new ArrayList<Capability>();
		
		try {
			Row[] rows = knowledgeDBClient.executeSelectQuery(getSupportedCapabilities, hal.getEquipletName());
			for (Row row : rows) {
				String capabilityName = (String) row.get("name");
				capabilities.add(this.getCapabilityByName(capabilityName));
			}
		} catch (KnowledgeException | KeyNotFoundException ex) {
			System.err.println("HAL::CapabilityFactory::getAllSupportedCapabilities(): Error occured which is considered to be impossible " + ex);
			ex.printStackTrace();
			return null;
		}
		
		return capabilities;
	}
	public ArrayList<Capability> getCapabilitiesForService(Service service) throws Exception{
		ArrayList<Capability> capabilities = new ArrayList<Capability>();
		
		try {
			Row[] rows = knowledgeDBClient.executeSelectQuery(getSupportedCapabilitiesForService, service.getName(), hal.getEquipletName());
			for (Row row : rows) {
				String capabilityName = (String) row.get("name");
				capabilities.add(this.getCapabilityByName(capabilityName));
			}
		} catch (KnowledgeException | KeyNotFoundException ex) {
			System.err.println("HAL::CapabilityFactory::getCapabilitiesForService(): Error occured which is considered to be impossible " + ex);
			ex.printStackTrace();
			return null;
		}
		
		return capabilities;
	}
	private Capability getCapabilityByName(String capabilityName) throws Exception {
		DynamicClassDescription description = new DynamicClassDescription(1, "a");
		Class<Capability> capabilityClass = dynamicClassFactory.getClassFromDescription(description);
		return capabilityClass.getConstructor().newInstance();
	}

	
	public boolean insertCapabilities(JsonArray capabilities) {
		try{
			try{
				for (JsonElement capabilityElement : capabilities) {
					JsonObject capabilityEntry = capabilityElement.getAsJsonObject();
					String name = capabilityEntry.get("name").getAsString();
					
					JsonObject capabilitySoftware = capabilityEntry.get("halSoftware").getAsJsonObject();
					int halSoftwareId = JavaSoftware.deserializeJavaSoftware(knowledgeDBClient, capabilitySoftware);
					
					knowledgeDBClient.executeUpdateQuery(addCapabilityType, name, halSoftwareId);
					
					JsonArray requiredMutationsTrees = capabilityEntry.get("requiredMutationsTrees").getAsJsonArray();
					deserializeRequiredMutations(name, requiredMutationsTrees);
				}
			} catch(Exception ex) {
				System.err.println("HAL::CapabilityFactory::insertCapabilities(): Error occured while inserting capability " + ex);
				ex.printStackTrace();
				knowledgeDBClient.getConnection().rollback();
				knowledgeDBClient.getConnection().setAutoCommit(true);
				return false;
			}
		} catch (SQLException ex) {
			return false;
		}
		return true;
	}
	private JsonArray serializeRequiredMutations(String capabilityTypeName) {
		HashMap<Integer, JsonObject> requiredTreesMap = new HashMap<Integer, JsonObject>();
		try {
			Row[] rows = knowledgeDBClient.executeSelectQuery(getRequiredMutationsForCapabilityType, 
					capabilityTypeName);
			for (Row row : rows) {
				Integer treeNumber = (Integer) row.get("treeNumber");
				String mutation = (String) row.get("mutation");
				
				if(requiredTreesMap.containsKey(treeNumber) == false) {
					JsonObject tree = new JsonObject();
					tree.addProperty("treeNumber", treeNumber);
					tree.add("mutations", new JsonArray());
					requiredTreesMap.put(treeNumber, tree);
				}
				
				requiredTreesMap.get(treeNumber).get("mutations").getAsJsonArray().add(new JsonPrimitive(mutation));
			}
		} catch (KnowledgeException | KeyNotFoundException ex) {
			System.err.println("HAL::ModuleFactory::serializeCalibrationData(): Error occured which is considered to be impossible " + ex);
			ex.printStackTrace();
		}
		JsonArray requiredMutationTrees = new JsonArray();
		for (JsonObject entry : requiredTreesMap.values()) {
			requiredMutationTrees.add(entry);
		}
		return requiredMutationTrees;
	}
	private void deserializeRequiredMutations(String capabilityTypeName, JsonArray requiredMutationTrees) {
		try {
			for (JsonElement requiredMutationTreeElement : requiredMutationTrees) {
				JsonObject requiredMutationTree = requiredMutationTreeElement.getAsJsonObject();
				Integer requiredMutationTreeNumber = requiredMutationTree.get("treeNumber").getAsInt();
				JsonArray requiredMutations = requiredMutationTree.get("mutations").getAsJsonArray();
				for (JsonElement requiredMutationElement : requiredMutations) {
					String requiredMutation = requiredMutationElement.getAsString();
					knowledgeDBClient.executeUpdateQuery(addRequiredMutationForCapabilityType, 
							requiredMutationTreeNumber, capabilityTypeName, requiredMutation);
				}
			}
		} catch (KnowledgeException ex) {
			System.err.println("HAL::CapabilityFactory::deserializeRequiredMutations(): Error occured which is considered to be impossible " + ex);
			ex.printStackTrace();
		}
	}
}