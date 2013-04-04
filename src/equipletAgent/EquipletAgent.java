package equipletAgent;


import newDataClasses.Parameter;
import newDataClasses.ParameterGroup;
import newDataClasses.ParameterList;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

@SuppressWarnings("serial")
public class EquipletAgent extends Agent {
	
	private int _canPerformStepId;
	private ParameterList _params;
	
	
	public int getCanPerformStepId(){
		return _canPerformStepId;
	}
	
	
	private class WaitMsgBehaviour extends CyclicBehaviour{

		public void action() {
			try{
				ACLMessage msg = receive(MessageTemplate.MatchOntology("CanPerformStep"));
				if (msg != null){
					
					_params = (ParameterList) msg.getContentObject();
					System.out.println("Receiving parameters = " + writeParamsToString(_params));
					
					//check params. Not really our thing
					ACLMessage message = new ACLMessage(ACLMessage.DISCONFIRM);
					message.setOntology("CanPerformStep");
					message.addReceiver(msg.getSender());
					if(!true){
						message.setPerformative(ACLMessage.CONFIRM);
					} 
					send(message);
				} else {
					block();
				}	
			}
			catch(Exception e){
			System.out.println("Error : " + e);
			}
		}
	}
	private String writeParamsToString(ParameterList p){
		String[] Groups = new String[3];
		Groups[0] = "Color";
		Groups[1] = "Shape";
		Groups[2] = "loc";
		
		String returnString = "Parameters are: \n";
		
		for(int i = 0; i < Groups.length; i++){
			returnString += "Group : " + Groups[i] + " \n";
			ParameterGroup pg = p.GetParameterGroup(Groups[i]);
			Parameter[] pga = pg.getParameters();
			for(int j = 0; j < pga.length; j++){
				returnString += "Parameter : " + pg.getParameters()[j].getKey() + " value: " + pg.getParameterValue(pg.getParameters()[j].getKey());
			}
			
		}
		
		return returnString;
	}
	
	//Sets up the equiplet. The canperformStep id is the id of the step this equiplet can perform
	  protected void setup() {
			try {
				WaitMsgBehaviour behaviour = new WaitMsgBehaviour();
				addBehaviour(behaviour);
				Object[] args = getArguments();
				_canPerformStepId = (int) args[0];
			} catch (Exception e) {
				System.out.println("Exited with: " + e);
				doDelete();
			}
	  } 
	}