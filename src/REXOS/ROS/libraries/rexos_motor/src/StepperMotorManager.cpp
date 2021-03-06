/**
 * @file MotorManager.cpp
 * @brief Motor management for concurrent movement.
 * @date Created: 2012-10-02
 *
 * @author Koen Braham
 * @author Dennis Koole
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

#include <rexos_motor/StepperMotorManager.h>
#include <rexos_motor/MotorException.h>
#include <rexos_motor/StepperMotorProperties.h>
#include <rexos_motor/CRD514KD.h>

extern "C"{
	#include <modbus/modbus.h>
}

namespace rexos_motor{
	StepperMotorManager::StepperMotorManager(rexos_io::RtuModbusInputOutputController* ioController, std::vector<MotorInterface*> motors):
			MotorManager(motors), ioController(ioController) {
	}
	
	void StepperMotorManager::startMovement(){
		startMovement(DEFAULT_MOTION_SLOT);
	}
	void StepperMotorManager::startMovement(int motionSlot){
		if(!poweredOn) {
			throw MotorException("motor manager is not powered on");
		}
		
		// Wait for previous movement to finish
		for(uint i = 0; i < motors.size(); ++i) {
			if(motors[i]->isPoweredOn()) {
				motors[i]->waitTillReady();
			}
		}
		
		// Start movement
		ioController->writeU16(CRD514KD::SlaveAddresses::BROADCAST, CRD514KD::Registers::CMD_1, 
				motionSlot | CRD514KD::CMD1Bits::EXCITEMENT_ON | CRD514KD::CMD1Bits::START, false);
		ioController->writeU16(CRD514KD::SlaveAddresses::BROADCAST, CRD514KD::Registers::CMD_1, 
				CRD514KD::CMD1Bits::EXCITEMENT_ON, false);
		
		for(uint i = 0; i < motors.size(); i++) {
			if(motors[i]->isPoweredOn()) {
				motors[i]->updateAngle();
			}
		}
	}
}
