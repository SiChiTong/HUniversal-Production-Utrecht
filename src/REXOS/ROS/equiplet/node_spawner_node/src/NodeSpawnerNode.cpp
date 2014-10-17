/**
 * @file EquipletNode.cpp
 * @brief Symbolizes an entire EquipletNode.
 * @date Created: 2012-10-12
 *
 * @author Dennis Koole
 * @author Alexander Streng
 *
 * @section LICENSE
 * License: newBSD
 *
 * Copyright © 2012, HU University of Applied Sciences Utrecht.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * - Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * - Neither the name of the HU University of Applied Sciences Utrecht nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE HU UNIVERSITY OF APPLIED SCIENCES UTRECHT
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 **/

#include <unistd.h>
#include <node_spawner_node/NodeSpawnerNode.h>
#include <rexos_knowledge_database/Equiplet.h>
using namespace node_spawner_node;


/**
 * Create a new EquipletNode
 * @param id The unique identifier of the Equiplet
 **/
NodeSpawnerNode::NodeSpawnerNode(std::string equipletName, bool spawnEquipletNode) :
		equipletName(equipletName),
		NodeSpawner(equipletName)
{
	if(spawnEquipletNode == true) {
		REXOS_INFO("spawning equiplet node");
		NodeSpawner::spawnEquipletNode();
	}
	spawnNodeServer = nh.advertiseService("spawnNode", &NodeSpawnerNode::spawnNode, this);
	
	REXOS_INFO("node_spawner_node has been started");
}

/**
 * Destructor for the NodeSpawnerNode
 **/
NodeSpawnerNode::~NodeSpawnerNode(){
}

bool NodeSpawnerNode::spawnNode(spawnNode::Request &request, spawnNode::Response &response) {
	rexos_knowledge_database::ModuleIdentifier identifier(request.manufacturer, request.typeNumber, request.serialNumber);
	NodeSpawner::spawnNode(identifier);
	return true;
}
std::vector<rexos_knowledge_database::ModuleIdentifier> NodeSpawnerNode::getModuleIdentifiersOfAttachedModules() {
	rexos_knowledge_database::Equiplet equiplet = rexos_knowledge_database::Equiplet(equipletName);
	return equiplet.getModuleIdentifiersOfAttachedModules();
}