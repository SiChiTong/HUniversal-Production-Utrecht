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

#include <rexos_model_spawner/ModelSpawner.h>

#include <unistd.h>
#include <zip.h>
#include <iostream>
#include <fstream>
#include <unistd.h>

#include <boost/filesystem/operations.hpp>
#include <boost/algorithm/string/replace.hpp>
#include <boost/algorithm/string.hpp>
#include <jsoncpp/json/reader.h>
#include <jsoncpp/json/value.h>

#include <rexos_knowledge_database/Module.h>
#include <rexos_knowledge_database/Part.h>
#include <rexos_zip/ZipExtractor.h>
#include <gazebo_msgs/SpawnModel.h>

namespace rexos_model_spawner {
	ModelSpawner::ModelSpawner(std::string equipletName, bool isShadow) :
		equipletName(equipletName), isShadow(isShadow), equiplet(equipletName)
	{
	}
	void ModelSpawner::spawnModuleModel(rexos_datatypes::ModuleIdentifier moduleIdentifier) {
		REXOS_INFO_STREAM("Spawning model for " << moduleIdentifier);
		rexos_knowledge_database::GazeboModel gazeboModel = rexos_knowledge_database::GazeboModel(moduleIdentifier);
		
		std::string modelName = moduleIdentifier.getManufacturer() + "|" + 
				moduleIdentifier.getTypeNumber() + "|" + 
				moduleIdentifier.getSerialNumber();
		
		// acquire parent module
		rexos_knowledge_database::GazeboModel* parentGazeboModel;
		std::string parentModelName;
		double childPositionX = 0;
		double childPositionY = 0;
		double childPositionZ = 0;
		
		rexos_knowledge_database::Module module(moduleIdentifier);
		rexos_knowledge_database::Module* parentModule = module.getParentModule();
		if(parentModule == NULL) {
			// parent is equiplet
			parentGazeboModel = new rexos_knowledge_database::GazeboModel(equipletName);
			parentModelName = equipletName;
			childPositionX = parentGazeboModel->getChildLinkOffsetX() + equiplet.getMountPointDistanceX() * module.getMountPointX();
			childPositionY = parentGazeboModel->getChildLinkOffsetY();
			childPositionZ = parentGazeboModel->getChildLinkOffsetZ() - equiplet.getMountPointDistanceY() * module.getMountPointY();
		} else {
			// parent is other module
			rexos_datatypes::ModuleIdentifier parentModuleIdentifier = parentModule->getModuleIdentifier();
			parentGazeboModel = new rexos_knowledge_database::GazeboModel(parentModuleIdentifier);
			
			parentModelName = parentModuleIdentifier.getManufacturer() + "|" + 
					parentModuleIdentifier.getTypeNumber() + "|" + 
					parentModuleIdentifier.getSerialNumber();
		}
		
		// spawn the model
		extractGazeboModel(gazeboModel);
		std::string gazeboSdfFileString = getSdfFileContents(gazeboModel);
		
		std::string baseDir = ZIP_ARCHIVE_PATH + boost::lexical_cast<std::string>(gazeboModel.getId()) + "/";
		boost::algorithm::replace_all(gazeboSdfFileString, "{baseDir}", baseDir);
		if(isShadow == true) {
			boost::algorithm::replace_all(gazeboSdfFileString, "{isShadow}", "shadow/");
		} else {
			boost::algorithm::replace_all(gazeboSdfFileString, "{isShadow}", "");
		}
		boost::algorithm::replace_all(gazeboSdfFileString, "{equipletName}", equipletName);
		boost::algorithm::replace_all(gazeboSdfFileString, "{manufacturer}", moduleIdentifier.getManufacturer());
		boost::algorithm::replace_all(gazeboSdfFileString, "{typeNumber}", moduleIdentifier.getTypeNumber());
		boost::algorithm::replace_all(gazeboSdfFileString, "{serialNumber}", moduleIdentifier.getSerialNumber());
		boost::algorithm::replace_all(gazeboSdfFileString, "{parentModel}", parentModelName);
		boost::algorithm::replace_all(gazeboSdfFileString, "{parentLink}", parentGazeboModel->getChildLink());
		boost::algorithm::replace_all(gazeboSdfFileString, "{childLink}", gazeboModel.getParentLink());
		
		ros::ServiceClient client = nodeHandle.serviceClient<gazebo_msgs::SpawnModel>("/gazebo/spawn_sdf_model/");
		gazebo_msgs::SpawnModel serviceCall;
		serviceCall.request.model_name = modelName;
		serviceCall.request.model_xml = gazeboSdfFileString;
		serviceCall.request.reference_frame = parentModelName + "::" + parentGazeboModel->getChildLink();
		// convert from millimetres to metres
		serviceCall.request.initial_pose.position.x = childPositionX / 1000;
		serviceCall.request.initial_pose.position.y = childPositionY / 1000;
		serviceCall.request.initial_pose.position.z = childPositionZ / 1000;
		
		if(isShadow == true) {
			serviceCall.request.robot_namespace = "shadow";
		}
		REXOS_INFO_STREAM("Call: " << serviceCall.request);
		client.waitForExistence();
		client.call(serviceCall);
		
		delete parentGazeboModel;
	}
	void ModelSpawner::spawnEquipletModel(double gridPositionX, double gridPositionY) {
		REXOS_INFO_STREAM("Spawning model for " << equipletName << " at " << gridPositionX << " " << gridPositionY);
		rexos_knowledge_database::GazeboModel gazeboModel = rexos_knowledge_database::GazeboModel(equipletName);
		
		// spawn the model
		extractGazeboModel(gazeboModel);
		std::string gazeboSdfFileString = getSdfFileContents(gazeboModel);
		
		std::string baseDir = ZIP_ARCHIVE_PATH + boost::lexical_cast<std::string>(gazeboModel.getId()) + "/";
		boost::algorithm::replace_all(gazeboSdfFileString, "{baseDir}", baseDir);
		if(isShadow == true) {
			boost::algorithm::replace_all(gazeboSdfFileString, "{isShadow}", "shadow/");
		} else {
			boost::algorithm::replace_all(gazeboSdfFileString, "{isShadow}", "");
		}
		boost::algorithm::replace_all(gazeboSdfFileString, "{equipletName}", equipletName);
		
		ros::ServiceClient client = nodeHandle.serviceClient<gazebo_msgs::SpawnModel>("/gazebo/spawn_sdf_model/");
		gazebo_msgs::SpawnModel serviceCall;
		serviceCall.request.model_name = equipletName;
		serviceCall.request.model_xml = gazeboSdfFileString;
		serviceCall.request.initial_pose.position.x = gridPositionX;
		serviceCall.request.initial_pose.position.y = gridPositionY;
		
		client.waitForExistence();
		client.call(serviceCall);
	}
	void ModelSpawner::spawnPartModel(std::string partName, OriginPlacementType originPlacementType, 
			double positionX, double positionY, double positionZ, 
			double rotationX, double rotationY, double rotationZ, std::string relativeTo, bool spawnChildParts) {
		REXOS_INFO_STREAM("Spawning model for " << partName);
		rexos_knowledge_database::Part part = rexos_knowledge_database::Part(partName);
		rexos_knowledge_database::GazeboModel gazeboModel = rexos_knowledge_database::GazeboModel(part);
		
		// spawn the model
		extractGazeboModel(gazeboModel, part.getPartName());
		std::string gazeboSdfFileString = getSdfFileContents(gazeboModel, part.getPartName());
		
		std::string baseDir = ZIP_ARCHIVE_PATH + part.getPartName() + "/" + 
					boost::lexical_cast<std::string>(gazeboModel.getId()) + "/";
		boost::algorithm::replace_all(gazeboSdfFileString, "{baseDir}", baseDir);
		
		ros::ServiceClient client = nodeHandle.serviceClient<gazebo_msgs::SpawnModel>("/gazebo/spawn_sdf_model/");
		gazebo_msgs::SpawnModel serviceCall;
		serviceCall.request.model_name = partName;
		serviceCall.request.model_xml = gazeboSdfFileString;
		
		// create the qrCode texture
		if(part.hasQrCodeFile() == true) {
			std::istream* qrCodeFile = part.getQrCodeFile();
			std::ofstream targetFile;
			std::string path = baseDir + QR_CODE_FILENAME;
			targetFile.open(path, std::ios::out | std::ios::binary);
			if (targetFile.good() != true) {
				throw std::runtime_error("Unable to open fstream with path" + path);
			}
			
			char buf[100];
			while (qrCodeFile->eof() == false) {
				qrCodeFile->read(buf, sizeof(buf));
				targetFile.write(buf, qrCodeFile->gcount());
			}
			targetFile.close();
		}
		
		// relative to
		if(originPlacementType == RELATIVE_TO_EQUIPLET_ORIGIN) {
			// The equiplet origin is at the same position as the childLinkOffset of the equiplet model
			rexos_knowledge_database::GazeboModel equipletGazeboModel = rexos_knowledge_database::GazeboModel(relativeTo);
			positionX += equipletGazeboModel.getChildLinkOffsetX();
			positionY += equipletGazeboModel.getChildLinkOffsetY();
			positionZ += equipletGazeboModel.getChildLinkOffsetZ();
			serviceCall.request.reference_frame = relativeTo + "::" + equipletGazeboModel.getParentLink();
		} else if(originPlacementType == RELATIVE_TO_MODULE_ORIGIN) {
			// The origin of the gazebo model (and thus the reference frame) is at the mount point. The module origin is at mount point + midpoint
			std::vector<std::string> identifierSegments;
			boost::split(identifierSegments, relativeTo, boost::is_any_of("|"));
			if(identifierSegments.size() != 3) {
				throw std::runtime_error("Unable to parse module identifier");
			}
			rexos_datatypes::ModuleIdentifier identifier(identifierSegments[0], identifierSegments[1], identifierSegments[2]);
			
			rexos_knowledge_database::ModuleType moduleType = rexos_knowledge_database::ModuleType(identifier);
			rexos_knowledge_database::GazeboModel modelGazeboModel = rexos_knowledge_database::GazeboModel(identifier);
			Json::Reader reader;
			Json::Value properties;
			if(reader.parse(moduleType.getModuleTypeProperties(), properties) == false) {
				throw std::runtime_error("Unable to parse properties");
			}
			
			positionX += properties["midPointX"].asDouble();
			positionY += properties["midPointY"].asDouble();
			positionZ += properties["midPointZ"].asDouble();
			
			// midPoint is calculated from the moint position, thus we need the parent link
			serviceCall.request.reference_frame = relativeTo + "::" + modelGazeboModel.getParentLink();
		} else if (originPlacementType == RELATIVE_TO_PART_ORIGIN) {
			rexos_knowledge_database::Part part = rexos_knowledge_database::Part(relativeTo);
			rexos_knowledge_database::GazeboModel partGazeboModel = rexos_knowledge_database::GazeboModel(part);
			serviceCall.request.reference_frame = relativeTo + "::" + partGazeboModel.getParentLink();
		}
		// nothing to do for RELATIVE_TO_WORLD_ORIGIN
		
		// convert from milis to meters
		serviceCall.request.initial_pose.position.x = positionX / 1000;
		serviceCall.request.initial_pose.position.y = positionY / 1000;
		serviceCall.request.initial_pose.position.z = positionZ / 1000;
		serviceCall.request.initial_pose.orientation.x = rotationX;
		serviceCall.request.initial_pose.orientation.y = rotationY;
		serviceCall.request.initial_pose.orientation.z = rotationZ;
		
		client.waitForExistence();
		client.call(serviceCall);
		
		// spawn child parts
		if(spawnChildParts == true) {
			std::vector<std::string> childNames = part.getChildPartNames();
			for(uint i = 0; i < childNames.size(); i++) {
				rexos_knowledge_database::Part part(childNames[i]);
				spawnPartModel(part.getPartName(), RELATIVE_TO_PART_ORIGIN, 
						part.getPositionX(), part.getPositionY(), part.getPositionZ(),
						part.getRotationX(), part.getRotationY(), part.getRotationZ(), partName, true);
			}
		}
	}
	
	std::string ModelSpawner::getSdfFileContents(rexos_knowledge_database::GazeboModel& gazeboModel, std::string uniqueName) {
		boost::filesystem::path gazeboSdfFilePath(ZIP_ARCHIVE_PATH + uniqueName + "/" + 
				boost::lexical_cast<std::string>(gazeboModel.getId()) + "/" + gazeboModel.getSdfFilename());
		
		std::ifstream gazeboSdfFile(gazeboSdfFilePath.string(), std::ios::in);
		if (gazeboSdfFile == NULL) throw std::runtime_error("Unable to open SDF file");
		
		std::string output((std::istreambuf_iterator<char>(gazeboSdfFile)), (std::istreambuf_iterator<char>()));
		return output;
	}
	
	void ModelSpawner::extractGazeboModel(rexos_knowledge_database::GazeboModel& gazeboModel, std::string uniqueName) {
		std::string baseName = boost::lexical_cast<std::string>(gazeboModel.getId());
		rexos_zip::ZipExtractor::extractZipArchive(gazeboModel.getModelFile(), baseName, 
				boost::filesystem::path(ZIP_ARCHIVE_PATH + uniqueName + "/"), false);
	}
}