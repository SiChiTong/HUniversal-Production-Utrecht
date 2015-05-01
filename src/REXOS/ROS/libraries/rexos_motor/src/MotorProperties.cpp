/**
 * @file StepperMotorProperties.cpp
 * @brief Contains the properties of a stepper motor.
 * @date Created: 2012-10-02
 *
 * @author Tommas Bakker
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

#include <rexos_motor/MotorProperties.h>

#include <rexos_utilities/Utilities.h>

namespace rexos_motor {
	MotorProperties::MotorProperties(Json::Value properties) {
		motorMinAngle = rexos_utilities::degreesToRadians(properties["motorMinAngleDegrees"].asDouble());
		motorMaxAngle = rexos_utilities::degreesToRadians(properties["motorMaxAngleDegrees"].asDouble());
		minAcceleration = rexos_utilities::degreesToRadians(properties["minAccelerationDegrees"].asDouble());
		maxAcceleration = rexos_utilities::degreesToRadians(properties["maxAccelerationDegrees"].asDouble());
		minSpeed = rexos_utilities::degreesToRadians(properties["minSpeedDegrees"].asDouble());
		maxSpeed = rexos_utilities::degreesToRadians(properties["maxSpeedDegrees"].asDouble());
	}
}