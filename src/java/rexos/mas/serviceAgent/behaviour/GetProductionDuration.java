package rexos.mas.serviceAgent.behaviour;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import rexos.libraries.blackboard_client.BlackboardClient;
import rexos.libraries.blackboard_client.GeneralMongoException;
import rexos.libraries.blackboard_client.InvalidDBNamespaceException;

import org.bson.types.ObjectId;

import rexos.mas.serviceAgent.Service;
import rexos.mas.serviceAgent.ServiceFactory;

import rexos.mas.behaviours.ReceiveBehaviour;

import com.mongodb.BasicDBObject;

/**
 * @author Peter Bonnema
 * 
 */
public class GetProductionDuration extends ReceiveBehaviour {
	static final long serialVersionUID = 1L;

	private BlackboardClient productionStepBlackBoard, serviceStepBlackBoard;

	/**
	 * @param a
	 */
	public GetProductionDuration(Agent a,
			BlackboardClient productionStepBlackBoard,
			BlackboardClient serviceStepBlackBoard) {
		super(a, MessageTemplate.MatchOntology("GetProductionStepDuration"));
		this.productionStepBlackBoard = productionStepBlackBoard;
		this.serviceStepBlackBoard = serviceStepBlackBoard;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see behaviours.ReceiveBehaviour#handle(jade.lang.acl.ACLMessage)
	 */
	@Override
	public void handle(ACLMessage message) {
		try {
			ObjectId productStepId = (ObjectId) message.getContentObject();
			BasicDBObject productStep = (BasicDBObject) productionStepBlackBoard
					.findDocumentById(productStepId);
			long productStepType = productStep.getLong("type");

			System.out.format(
					"%s got message GetProductionDuration for step type %s%n",
					myAgent.getLocalName(), productStepType);

			Service[] services = null;
			Service service = null;
			ServiceFactory factory = new ServiceFactory(message.getSender()
					.getName());
			if ((services = factory.getServicesForStep(productStepType)).length > 0) {
				service = services[0];
			} else {
				myAgent.doDelete();
				throw new RuntimeException(
						"Service Agent - No available services for stepType "
								+ productStep);
			}

			BasicDBObject parameters = (BasicDBObject) productStep
					.get("parameters");

			BasicDBObject[] serviceSteps = service.getServiceSteps(
					productStepType, parameters);
			for (BasicDBObject serviceStep : serviceSteps) {
				serviceStep.put("productStepId", productStepId);
			}

			myAgent.addBehaviour(new GetServiceDuration(myAgent,
					productionStepBlackBoard, serviceStepBlackBoard,
					serviceSteps, msg.getConversationId()));
		} catch (UnreadableException | InvalidDBNamespaceException
				| GeneralMongoException e) {
			e.printStackTrace();
			myAgent.doDelete();
		}
	}
}
