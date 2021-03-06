/**
 * @file DeltaRobotMeasures.cpp
 * @brief Holds all measures for the DeltaRobot
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

#include <rexos_delta_robot/DeltaRobotMeasures.h>

#include <cmath>
#include <iostream>

#include <rexos_utilities/Utilities.h>
#include "rexos_logger/rexos_logger.h"

#include "ros/ros.h"

namespace rexos_delta_robot {
	DeltaRobotMeasures::DeltaRobotMeasures(Json::Value properties) {
		baseRadius = properties["baseRadius"].asDouble();
		REXOS_INFO_STREAM("found baseRadius " << baseRadius);
		
		hipLength = properties["hipLength"].asDouble();
		REXOS_INFO_STREAM("found hipLength " << hipLength);
		
		effectorRadius = properties["effectorRadius"].asDouble();
		REXOS_INFO_STREAM("found effectorRadius " << effectorRadius);
		
		effectorHeight = properties["effectorHeight"].asDouble();
		REXOS_INFO_STREAM("found effectorHeight " << effectorHeight);
		
		ankleLength = properties["ankleLength"].asDouble();
		REXOS_INFO_STREAM("found ankleLength " << ankleLength);
		
		maxAngleHipAnkle = rexos_utilities::degreesToRadians(properties["hipAnleMaxAngleDegrees"].asDouble());
		REXOS_INFO_STREAM("found maxAngleHipAnkle " << maxAngleHipAnkle);
		
		boundaryBoxMinX = properties["boundaryBoxMinX"].asDouble();
		REXOS_INFO_STREAM("found boundaryBoxMinX " << boundaryBoxMinX);
		
		boundaryBoxMaxX = properties["boundaryBoxMaxX"].asDouble();
		REXOS_INFO_STREAM("found boundaryBoxMaxX " << boundaryBoxMaxX);
		
		boundaryBoxMinY = properties["boundaryBoxMinY"].asDouble();
		REXOS_INFO_STREAM("found boundaryBoxMinY " << boundaryBoxMinY);
		
		boundaryBoxMaxY = properties["boundaryBoxMaxY"].asDouble();
		REXOS_INFO_STREAM("found boundaryBoxMaxY " << boundaryBoxMaxY);
		
		boundaryBoxMinZ = properties["boundaryBoxMinZ"].asDouble();
		REXOS_INFO_STREAM("found boundaryBoxMinZ " << boundaryBoxMinZ);
		
		boundaryBoxMaxZ = properties["boundaryBoxMaxZ"].asDouble();
		REXOS_INFO_STREAM("found boundaryBoxMaxZ " << boundaryBoxMaxZ);
	}
}
