package behaviours;


import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import equipletAgent.EquipletAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class ScheduleStep extends ReceiveBehaviour {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static MessageTemplate messageTemplate = MessageTemplate.MatchOntology("ScheduleStep");
	private EquipletAgent equipletAgent;

	public ScheduleStep(Agent a) {
		super(a, -1, messageTemplate);
		equipletAgent = (EquipletAgent) a;
	}

	@Override
	public void handle(ACLMessage message) {
		Object contentObject = null;
		String contentString = message.getContent();

		try {
			contentObject = message.getContentObject();
		} catch (UnreadableException e) {
			// System.out.println("Exception Caught, No Content Object Given");
		}
		System.out.format("%s received message from %s (%s:%s)%n", myAgent.getLocalName(), message.getSender().getLocalName(), message.getOntology(), contentObject == null ? contentString : contentObject);

		try{
			
			long timeslot = Long.parseLong(contentString);
			ObjectId contentObjectId = equipletAgent.getCommunicationSlot(message.getConversationId());
			BasicDBObject query = new BasicDBObject();
			query.put("_id", contentObjectId);
			DBObject productStep = equipletAgent.getEquipletBBclient().findDocuments(query).get(0);
			System.out.format("%d%n", timeslot);
			ACLMessage timeslotMessage = new ACLMessage(
					ACLMessage.REQUEST);
			timeslotMessage.addReceiver(equipletAgent.getServiceAgent());
			timeslotMessage.setOntology("scheduleStepWithLogistics");
			timeslotMessage.setContent(String.valueOf(timeslot));
			timeslotMessage.setConversationId(message.getConversationId());
			myAgent.send(timeslotMessage);
		
		/* 
		  * TODO: Ask service agent to schedule the step with the
		  * logistics at time X if possible. Wait for result.
		  * Report result back to product agent. If the result is
		  * positive: Set the status of the step on the product
		  * steps blackboard to PLANNED and add the schedule
		  * data.
		  */
		 
			ACLMessage confirmScheduleStep = new ACLMessage(ACLMessage.CONFIRM);
			confirmScheduleStep.setConversationId(message.getConversationId());
			confirmScheduleStep.addReceiver((AID) productStep.get("productAgentId"));
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
}
