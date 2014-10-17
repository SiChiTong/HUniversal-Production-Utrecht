/*
 * ModuleProxy.cpp
 *
 *  Created on: Jun 14, 2013
 *      Author: joris
 */

#include "equiplet_node/ModuleProxy.h"
#include <actionlib/client/simple_action_client.h>
#include <node_spawner_node/spawnNode.h>
#include <jsoncpp/json/writer.h>

namespace equiplet_node {

ModuleProxy::ModuleProxy(std::string equipletName, rexos_knowledge_database::ModuleIdentifier moduleIdentifier, ModuleProxyListener* mpl) :
		moduleNamespaceName(moduleIdentifier.getManufacturer() + "/" + moduleIdentifier.getTypeNumber() + "/" + moduleIdentifier.getSerialNumber()),
		equipletNamespaceName(equipletName),
		moduleIdentifier(moduleIdentifier),
		changeStateActionClient(nodeHandle, equipletNamespaceName + "/" + moduleNamespaceName + "/change_state"),
		changeModeActionClient(nodeHandle, equipletNamespaceName + "/" + moduleNamespaceName + "/change_mode"),
		setInstructionActionClient(nodeHandle, equipletNamespaceName + "/" + moduleNamespaceName + "/set_instruction"),
		transitionActionServer(nodeHandle, equipletNamespaceName + "/" + moduleNamespaceName + "/transition", 
			boost::bind(&ModuleProxy::onModuleTransitionGoalCallback, this, _1), false),
		allowedToContinue(false),
		currentMode(rexos_statemachine::Mode::MODE_SERVICE),
		currentState(rexos_statemachine::State::STATE_OFFLINE),
		moduleProxyListener(mpl),
		connectedWithNode(false),
		bond(NULL) {
	stateUpdateServiceServer = nodeHandle.advertiseService(
			equipletNamespaceName + "/" + moduleNamespaceName + "/state_update",
			&ModuleProxy::onStateChangeServiceCallback, this);

	modeUpdateServiceServer = nodeHandle.advertiseService(
			equipletNamespaceName + "/" + moduleNamespaceName + "/mode_update",
			&ModuleProxy::onModeChangeServiceCallback, this);
	
	transitionActionServer.start();
	
	REXOS_INFO_STREAM("Setting state action client: " << equipletNamespaceName + "/" + moduleNamespaceName << "/change_state");
	REXOS_INFO_STREAM("Setting mode action client: " << equipletNamespaceName + "/" + moduleNamespaceName << "/change_mode");
	REXOS_INFO_STREAM("Setting instruction action client: " << equipletNamespaceName + "/" + moduleNamespaceName << "/set_instruction");
	REXOS_INFO_STREAM("Setting state update server: " << equipletNamespaceName + "/" + moduleNamespaceName + "/state_update");
	REXOS_INFO_STREAM("Setting mode update server: " << equipletNamespaceName + "/" + moduleNamespaceName + "/mode_update");
	

}

ModuleProxy::~ModuleProxy() {
	delete bond;
	// TODO Auto-generated destructor stub
}

rexos_statemachine::State ModuleProxy::getCurrentState(){
	return currentState;
}

rexos_statemachine::Mode ModuleProxy::getCurrentMode(){
	return currentMode;
}

rexos_knowledge_database::ModuleIdentifier ModuleProxy::getModuleIdentifier(){
	return moduleIdentifier;
}

void ModuleProxy::setModuleProxyListener(ModuleProxyListener* mpl){
	moduleProxyListener = mpl;
}

void ModuleProxy::changeState(rexos_statemachine::State state) {
	REXOS_INFO("ModuleProxy of %s send new state goal %s", moduleIdentifier.toString().c_str(), rexos_statemachine::state_txt[state]);
	REXOS_INFO_STREAM("state " << rexos_statemachine::state_txt[state] << " " << rexos_statemachine::state_txt[getCurrentState()]);
	
	if(state == rexos_statemachine::State::STATE_SAFE && getCurrentState() == rexos_statemachine::State::STATE_OFFLINE) {
		if(connectedWithNode == false) {
			ros::ServiceClient spanNodeClient(nodeHandle.serviceClient<node_spawner_node::spawnNode>("spawnNode"));
			ROS_INFO_STREAM("Spawning node for " << moduleIdentifier);
			node_spawner_node::spawnNode spawnNodeCall;
			spawnNodeCall.request.manufacturer = moduleIdentifier.getManufacturer();
			spawnNodeCall.request.typeNumber = moduleIdentifier.getTypeNumber();
			spawnNodeCall.request.serialNumber = moduleIdentifier.getSerialNumber();
			spanNodeClient.call(spawnNodeCall);
			
			// wait for the node to come online
			if(connectedWithNode == false) {
				boost::unique_lock<boost::mutex> lock(nodeStartupMutex);
				nodeStartupCondition.wait(lock);
			}
		} else {
			REXOS_WARN("Node has already been stated, which is not expected (did someone manually start this node?)");
		}
	}
	
	desiredState = state;
	
	rexos_statemachine::ChangeStateGoal goal;
	goal.desiredState = desiredState;
	changeStateActionClient.waitForServer();
	changeStateActionClient.sendGoal(goal);
}

void ModuleProxy::changeMode(rexos_statemachine::Mode mode) {
	REXOS_INFO("ModuleProxy of %s send new mode goal %s", moduleIdentifier.toString().c_str(), rexos_statemachine::mode_txt[mode]);
	rexos_statemachine::ChangeModeGoal goal;
	goal.desiredMode = mode;
	changeModeActionClient.sendGoal(goal);
}

void ModuleProxy::setInstruction(std::string OID, Json::Value n) {
	ROS_INFO_STREAM("Sent Instruction to module: " << moduleIdentifier.toString());
	if(connectedWithNode == false) {
		ROS_ERROR("Sent intruction to module which is not connected to the ROS node");
		return;
	}
	rexos_statemachine::SetInstructionGoal goal;
	
	Json::StyledWriter writer;
	
	goal.json = writer.write(n);
	goal.OID = OID;

	setInstructionActionClient.sendGoal(goal, boost::bind(&ModuleProxy::onInstructionServiceCallback, this, _1, _2), NULL, NULL);
}
void ModuleProxy::goToNextTransitionPhase() {
	transitionActionServer.setSucceeded();
	boost::unique_lock<boost::mutex> lock(transitionPhaseMutex);
	allowedToContinue = true;
	transitionPhaseCondition.notify_one();
}

bool ModuleProxy::onStateChangeServiceCallback(StateUpdateRequest &req, StateUpdateResponse &res){
	//REXOS_INFO("ModuleProxy of %s received state change to %s", moduleNodeName.c_str(), rexos_statemachine::state_txt[currentState]);

	rexos_statemachine::State previousState = currentState;
	currentState = static_cast<rexos_statemachine::State>(req.state);

	if(moduleProxyListener != NULL){
		moduleProxyListener->onModuleStateChanged(this,currentState,previousState);
	}

	return true;
}

bool ModuleProxy::onModeChangeServiceCallback(ModeUpdateRequest &req, ModeUpdateResponse &res){
	//REXOS_INFO("ModuleProxy of %s received mode change to %s", moduleNodeName.c_str(), rexos_statemachine::Mode_txt[currentMode]);

	rexos_statemachine::Mode previousMode = currentMode;
	currentMode = static_cast<rexos_statemachine::Mode>(req.mode);

	if(moduleProxyListener != NULL){
		moduleProxyListener->onModuleModeChanged(this,currentMode,previousMode);
	}

	return true;
}

void ModuleProxy::onInstructionServiceCallback(const actionlib::SimpleClientGoalState& state, const rexos_statemachine::SetInstructionResultConstPtr& result){

	if(state == actionlib::SimpleClientGoalState::SUCCEEDED)
		moduleProxyListener->onHardwareStepCompleted(this, result->OID, true);
	else
		moduleProxyListener->onHardwareStepCompleted(this, result->OID, false);
}

void ModuleProxy::onModuleTransitionGoalCallback(const rexos_statemachine::TransitionGoalConstPtr& goal) {
	REXOS_INFO("Recieved a goal call");
	std::vector<rexos_knowledge_database::SupportedMutation> supportedMutations;
	for(int i = 0; i < goal->gainedSupportedMutations.size(); i++) {
		rexos_knowledge_database::SupportedMutation supportedMutation(
				goal->gainedSupportedMutations.at(i));
		supportedMutations.push_back(supportedMutation);
	}
	std::vector<rexos_knowledge_database::RequiredMutation> requiredMutations;
	for(int i = 0; i < goal->requiredMutationsRequiredForNextPhase.size(); i++) {
		rexos_knowledge_database::RequiredMutation requiredMutation(
				goal->requiredMutationsRequiredForNextPhase.at(i).mutation, goal->requiredMutationsRequiredForNextPhase.at(i).isOptional);
		requiredMutations.push_back(requiredMutation);
	}
	moduleProxyListener->onModuleTransitionPhaseCompleted(this, supportedMutations, requiredMutations);
	
	boost::unique_lock<boost::mutex> lock(transitionPhaseMutex);
	while(allowedToContinue == false) transitionPhaseCondition.wait(lock);
	allowedToContinue = false;
	REXOS_WARN("Leaving method");
}

void ModuleProxy::onBondCallback(rexos_bond::Bond* bond, Event event){
	if(event == FORMED) {
		REXOS_INFO("Bond has been formed");
		connectedWithNode = true;
		nodeStartupCondition.notify_one();
	} else {
		REXOS_WARN("Bond has been broken");
		moduleProxyListener->onModuleDied(this);
		connectedWithNode = false;
		delete bond;
		bond = NULL;
	}
}
void ModuleProxy::bind() {
	REXOS_INFO_STREAM("binding B on " << (equipletNamespaceName + "/bond")<< " id " << moduleNamespaceName);
	bond = new rexos_bond::Bond(equipletNamespaceName + "/bond", moduleNamespaceName, this);
	bond->start();
}
} /* namespace equiplet_node */