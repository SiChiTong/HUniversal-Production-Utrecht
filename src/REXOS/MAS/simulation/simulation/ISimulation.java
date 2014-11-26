package MAS.simulation.simulation;

public interface ISimulation {

	void notifyProductCreationFailed(String localName);

	void notifyProductCreated(String productName, String equipletName);

	void notifyProductTraveling(String productName, String equipletName);

	void notifyProductProcessing(String productName, String equipletName, String service, int index);

	void notifyProductFinished(String productName);

	void notifyReconfigReady(String equipletName);

}
