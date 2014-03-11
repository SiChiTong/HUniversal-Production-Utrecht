package HAL;

import libraries.knowledgedb_client.KeyNotFoundException;
import libraries.knowledgedb_client.KnowledgeDBClient;
import libraries.knowledgedb_client.KnowledgeException;
import libraries.knowledgedb_client.Row;

public abstract class Module { 
	protected KnowledgeDBClient knowledgeDBClient;
	protected ModuleIdentifier moduleIdentifier;
	
	public Module(ModuleIdentifier moduleIdentifier) throws KnowledgeException{
		this.moduleIdentifier = moduleIdentifier;
		this.knowledgeDBClient = new KnowledgeDBClient();
	}	
	
	
	protected Module getParentModule() throws KnowledgeException, KeyNotFoundException{
		String sql = "SELECT * FROM Module " +
						"WHERE attachedToLeft < (" +
							"SELECT attachedToLeft FROM Module " +
								"WHERE manufacturer = '" + moduleIdentifier.getManufacturer() +
								"' AND typeNumber = '" + moduleIdentifier.getTypeNumber() +
								"' AND serialNumber = '" + moduleIdentifier.getSerialNumber() +
						"') AND attachedToRight > (" +
 							"SELECT attachedToRight FROM Module " +
 								"WHERE manufacturer = '" + moduleIdentifier.getManufacturer() +
 								"' AND typeNumber = '" + moduleIdentifier.getTypeNumber() +
 								"' AND serialNumber = '" + moduleIdentifier.getSerialNumber() +
 						"') ORDER BY abs(attachedToLeft - attachedToRight) ASC LIMIT 1";
		
		Row[] resultSet = knowledgeDBClient.executeSelectQuery(sql);
		
		ModuleIdentifier moduleIdentifier = new ModuleIdentifier(resultSet[0].get("manufacturer").toString(),
																 resultSet[0].get("typeNumber").toString(),
																 resultSet[0].get("serialNumber").toString()
																);
		ModuleFactory moduleFactory = new ModuleFactory(null);		
		return moduleFactory.getModuleByIdentifier(moduleIdentifier);
	}
	
	
}