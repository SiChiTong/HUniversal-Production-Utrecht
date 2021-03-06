/**
 * @file DatabaseConnection.h
 * @brief Coordinate system for communication between nodes
 * @date Created: 2012-01-??  TODO: Date
 *
 * @author Tommas Bakker
 *
 * @section LICENSE
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
 *
 **/

#pragma once

#include <actionlib/client/simple_action_client.h>
#include <string>
#include <map>
#include <jsoncpp/json/value.h>

#include <rexos_module/ModuleInterfaceListener.h>
#include <rexos_module/ExecuteHardwareStepAction.h>
#include <rexos_module/AbstractModule.h>
#include <rexos_datatypes/ModuleIdentifier.h>
#include <rexos_datatypes/HardwareStep.h>

namespace rexos_module {
	typedef actionlib::SimpleActionClient<rexos_module::ExecuteHardwareStepAction> ExecuteHardwareStepClient;
	
	class ModuleInterface : public rexos_module::AbstractModule {
	public:
		ModuleInterface(std::string equipletName, rexos_datatypes::ModuleIdentifier identifier);
		ModuleInterface(std::string equipletName, rexos_datatypes::ModuleIdentifier identifier, ModuleInterfaceListener* moduleInterfaceListener);
		
		void executeHardwareStep(rexos_datatypes::HardwareStep hardwareStep);
	protected:
		void onExecuteHardwareStepCallback(const actionlib::SimpleClientGoalState& state, 
				const rexos_module::ExecuteHardwareStepResultConstPtr& result);
	protected:
		std::map<std::string, rexos_datatypes::HardwareStep> hardwareSteps;
		ModuleInterfaceListener* moduleInterfaceListener;
		ExecuteHardwareStepClient executeHardwareStepClient;
	};
}
