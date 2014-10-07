/**                                     ______  _______   __ _____  _____
 *                  ...++,              | ___ \|  ___\ \ / /|  _  |/  ___|
 *                .+MM9WMMN.M,          | |_/ /| |__  \ V / | | | |\ `--.
 *              .&MMMm..dM# dMMr        |    / |  __| /   \ | | | | `--. \
 *            MMMMMMMMMMMM%.MMMN        | |\ \ | |___/ /^\ \\ \_/ //\__/ /
 *           .MMMMMMM#=`.gNMMMMM.       \_| \_|\____/\/   \/ \___/ \____/
 *             7HMM9`   .MMMMMM#`		
 *                     ...MMMMMF .      
 *         dN.       .jMN, TMMM`.MM     	@file 	Job
 *         .MN.      MMMMM;  ?^ ,THM		@brief 	This class used to store the Jobs that the Equiplet has to execute.
 *          dM@      dMMM3  .ga...g,    	@date Created:	2014-05-20
 *       ..MMM#      ,MMr  .MMMMMMMMr   
 *     .dMMMM@`       TMMp   ?TMMMMMN   	@author	Tom Oosterwijk
 *   .dMMMMMF           7Y=d9  dMMMMMr    
 *  .MMMMMMF        JMMm.?T!   JMMMMM#		@section LICENSE
 *  MMMMMMM!       .MMMML .MMMMMMMMMM#  	License:	newBSD
 *  MMMMMM@        dMMMMM, ?MMMMMMMMMF    
 *  MMMMMMN,      .MMMMMMF .MMMMMMMM#`    	Copyright © 2014, HU University of Applied Sciences Utrecht. 
 *  JMMMMMMMm.    MMMMMM#!.MMMMMMMMM'.		All rights reserved.
 *   WMMMMMMMMNNN,.TMMM@ .MMMMMMMM#`.M  
 *    JMMMMMMMMMMMN,?MD  TYYYYYYY= dM     
 *                                        
 *	Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *	- Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *	- Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *	- Neither the name of the HU University of Applied Sciences Utrecht nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 *   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *   "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 *   THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 *   ARE DISCLAIMED. IN NO EVENT SHALL THE HU UNIVERSITY OF APPLIED SCIENCES UTRECHT
 *   BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *   CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 *   GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 *   HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 *   LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 *   OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 **/
package MAS.equiplet.equiplet_agent;

import generic.ProductStep;
import generic.Service;

import org.json.JSONException;
import org.json.JSONObject;


public class Job {
	
	/**
	  * @var startTime
	  * The time when the job needs to be executed.
	  */
	private String startTime;
	
	/**
	  * @var duration
	  * The time it takes to execute the job.
	  */
	private String duration;
	
	/**
	  * @var productStepId
	  * The productedStepId that was given by the ProductAgent
	  */
	private int productStepId;
	
	/**
	  * @object productStep
	  * The object productStep holds the productstep that needs to be executes for this job.
	  */
	private ProductStep productStep;
	
	/**
	  * Job()
	  * The constructor initializes the variables.
	 * @throws JSONException 
	  */
	public Job(String startTime, String duration, int productStepId, JSONObject jsonProductStep) throws JSONException{
		this.startTime = startTime;
		this.duration = duration;
		this.productStepId = productStepId;
    	Service service = new Service(jsonProductStep.getString("service"));
		productStep = new ProductStep(productStepId, jsonProductStep.getJSONObject("criteria"), service);
	}
	
	/**
	  * getStartTime()
	  * @return The String startTime
	  */
	public String getStartTime(){
		return startTime;
	}
	
	/**
	  * getDuration()
	  * @return The String duration
	  */
	public String getDuration(){
		return duration;
	}
	
	/**
	  * getProductStepId()
	  * @return The String productStepId
	  */
	public int getProductStepId(){
		return productStepId;
	}
	
	/**
	  * getProductStep()
	  * @return The object productStep
	  */
	public ProductStep getProductStep(){
		return productStep;
	}
}
