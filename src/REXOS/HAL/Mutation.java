package HAL;

import java.util.ArrayList;

import HAL.libraries.knowledgedb_client.KnowledgeDBClient;
import HAL.libraries.knowledgedb_client.Row;

import org.json.JSONArray;
import org.json.JSONException;
/**
 * This method provides methods for serializing and deserializing of Mutations
 * @author Tommas Bakker
 *
 */
public class Mutation {
	private static final String getSupportedMutationTypesForModuleType =
			"SELECT mutation \n" + 
			"FROM SupportedMutation \n" + 
			"WHERE manufacturer = ? AND \n" + 
			"		typeNumber = ?;";
	private static final String addSupportedMutationTypeForModuleType =
			"INSERT INTO SupportedMutation \n" + 
			"(manufacturer, typeNumber, mutation) \n" + 
			"VALUES(?, ?, ?);";
	private static final String removeAllSupportedMutationTypesForModuleType =
			"DELETE FROM SupportedMutation \n" + 
			"WHERE manufacturer = ? AND \n" + 
			"		typeNumber = ?;";
	
	private String mutationType;
	public String getMutationType() {
		return mutationType;
	}
	
	public Mutation(String mutationType) {
		this.mutationType = mutationType;
	}
	/**
	 * This method will return the supported mutations for the module identified by the {@link ModuleIdentifier} using the provided {@link KnowledgeDBClient}.
	 * @param moduleIdentifier
	 * @param knowledgeDBClient
	 * @return
	 */
	public static ArrayList<Mutation> getSupportedMutations(ModuleIdentifier moduleIdentifier, KnowledgeDBClient knowledgeDBClient) {
		ArrayList<Mutation> mutations = new ArrayList<Mutation>();
		Row[] rows = knowledgeDBClient.executeSelectQuery(getSupportedMutationTypesForModuleType, 
				moduleIdentifier.getManufacturer(), moduleIdentifier.getTypeNumber());
		for (Row row : rows) {
			mutations.add(new Mutation((String) row.get("mutation")));
		}
		return mutations;
	}
	/**
	 * This method serializes the supported mutations from the knowledge database for the module identified by the {@link ModuleIdentifier}. This method does NOT remove the supported mutations from the knowledge database.
	 * This method uses a new KnowledgeDBClient which may cause locking issues. It is recommended to provide your own KnowledgeDBClient.
	 * @param moduleIdentifier
	 * @return
	 */
	public static JSONArray serializeAllSupportedMutations(ModuleIdentifier moduleIdentifier) {
		return serializeAllSupportedMutations(moduleIdentifier, new KnowledgeDBClient());
	}
	/**
	 * This method serializes the supported mutations from the knowledge database for the module identified by the {@link ModuleIdentifier}. This method does NOT remove the supported mutations from the knowledge database.
	 * @param moduleIdentifier
	 * @param knowledgeDBClient
	 * @return
	 */
	public static JSONArray serializeAllSupportedMutations(ModuleIdentifier moduleIdentifier, 
			KnowledgeDBClient knowledgeDBClient) {
		JSONArray supportedMutationEntries = new JSONArray();
		ArrayList<Mutation> mutations = getSupportedMutations(moduleIdentifier, knowledgeDBClient);
		for (Mutation mutation : mutations) {
			supportedMutationEntries.put(mutation.getMutationType());
		}
		return supportedMutationEntries;
	}
	/**
	 * This method deserializes the supported mutations and copies it to the knowledge database for the module identified by the {@link ModuleIdentifier}.
	 * This method uses a new KnowledgeDBClient which may cause locking issues. It is recommended to provide your own KnowledgeDBClient.
	 * @param moduleIdentifier
	 * @param supportedMutationEntries
	 * @return
	 * @throws JSONException 
	 */
	public static ArrayList<Mutation> insertSupportedMutations(ModuleIdentifier moduleIdentifier, JSONArray supportedMutationEntries) throws JSONException {
		return insertSupportedMutations(moduleIdentifier, supportedMutationEntries, new KnowledgeDBClient());
	}
	/**
	 * This method deserializes the supported mutations and copies it to the knowledge database for the module identified by the {@link ModuleIdentifier}.
	 * @param moduleIdentifier
	 * @param supportedMutationEntries
	 * @param knowledgeDBClient
	 * @return
	 * @throws JSONException 
	 */
	public static ArrayList<Mutation> insertSupportedMutations(ModuleIdentifier moduleIdentifier, 
			JSONArray supportedMutationEntries, KnowledgeDBClient knowledgeDBClient) throws JSONException {
		ArrayList<Mutation> mutations = new ArrayList<Mutation>();
		
		for (int i = 0; i < supportedMutationEntries.length(); i++) {
			String mutationType = supportedMutationEntries.getString(i);
			knowledgeDBClient.executeUpdateQuery(addSupportedMutationTypeForModuleType, 
					moduleIdentifier.getManufacturer(), moduleIdentifier.getTypeNumber(), mutationType);
			mutations.add(new Mutation(mutationType));
		}
		return mutations;
	}
	/**
	 * This method removes the supported mutations from the knowledge database for the module identified by the {@link ModuleIdentifier} 
	 * This method uses a new KnowledgeDBClient which may cause locking issues. It is recommended to provide your own KnowledgeDBClient.
	 * @param moduleIdentifier
	 */
	public static void removeSupportedMutations(ModuleIdentifier moduleIdentifier) {
		removeSupportedMutations(moduleIdentifier, new KnowledgeDBClient());
	}
	/**
	 * This method removes the supported mutations from the knowledge database for the module identified by the {@link ModuleIdentifier} 
	 * @param moduleIdentifier
	 * @param knowledgeDBClient
	 */
	public static void removeSupportedMutations(ModuleIdentifier moduleIdentifier, KnowledgeDBClient knowledgeDBClient) {
		knowledgeDBClient.executeUpdateQuery(removeAllSupportedMutationTypesForModuleType, 
				moduleIdentifier.getManufacturer(), moduleIdentifier.getTypeNumber());
	}
}
