package web_socket_server;
import grid_server.ServerConfigurations;

import com.google.gson.JsonArray;

import jade.core.AID;
import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;


public class ProductAgentSpawnerAgent extends Agent {
	private static final long serialVersionUID = 1L;
	 
	private String productSteps;
	 
	public void setProductSteps(String productSteps){
		this.productSteps = productSteps;
	}
	 
	public void sendMessage(String message, int type){
		  ACLMessage acl = new ACLMessage(type);
		  AID aid=new AID(ServerConfigurations.GS_NAME,AID.ISGUID);
		  aid.addAddresses(ServerConfigurations.GS_ADDRESS);
		  
		  acl.addReceiver(aid);
		  acl.setContent(message);
		  send(acl);
		  System.out.println("Send message: " + message);
	}
	protected void setup(){
		System.out.println(productSteps);
		sendMessage(productSteps, 0);
		try {
			getContainerController().kill();
		} catch (StaleProxyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}