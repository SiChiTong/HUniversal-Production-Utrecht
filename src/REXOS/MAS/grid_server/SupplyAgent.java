/**                                     ______  _______   __ _____  _____
 *                  ...++,              | ___ \|  ___\ \ / /|  _  |/  ___|
 *                .+MM9WMMN.M,          | |_/ /| |__  \ V / | | | |\ `--.
 *              .&MMMm..dM# dMMr        |    / |  __| /   \ | | | | `--. \
 *            MMMMMMMMMMMM%.MMMN        | |\ \ | |___/ /^\ \\ \_/ //\__/ /
 *           .MMMMMMM#=`.gNMMMMM.       \_| \_|\____/\/   \/ \___/ \____/
 *             7HMM9`   .MMMMMM#`		
 *                     ...MMMMMF .      
 *         dN.       .jMN, TMMM`.MM     	@file 	PartsAgent
 *         .MN.      MMMMM;  ?^ ,THM		@brief 	This agent is responsible for finding the parts that a Product Agent requires.
 *          dM@      dMMM3  .ga...g,    	@date Created:	2014-05-26
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
package MAS.grid_server;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import MAS.agents.data_classes.MessageType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * PartsAgent that communicates with ProductAgent to locate the needed parts.
 **/
public class SupplyAgent extends Agent{
	
	private static double[] lookUpTable = new double[32];
	
	private static final long serialVersionUID = 1L;
	private static final double maxStep = 16.5;
	private static final double smallStep = 5.5;
	private static String targetCrate = "GC4x4MB_6";
	private static final int PICK_PLACE_ROTATION_SERVICE_ID = 30;
	private static final int PICK_PLACE__SERVICE_ID = 20;
	private static final int DRAW_SERVICE_ID = 10;
	private static final int APPROACH_OFFSET =75;
	private boolean hasRotate = false;
	
	private static String[] GC4x4MB_1 = new String[16];
	private static String[] GC4x4MB_4 = new String[16];
	private static String[] GC4x4MB_3 = new String[16];
	
	protected void setup(){	
		lookUpTable[0]=-16.5;
		lookUpTable[1]=16.5;
		
		lookUpTable[2]=-5.5;
		lookUpTable[3]=16.5;
		
		lookUpTable[4]=5.5;
		lookUpTable[5]=16.5;
		
		lookUpTable[6]=16.5;
		lookUpTable[7]=16.5;
		
		lookUpTable[8]=-16.5;
		lookUpTable[9]=5.5;
		
		lookUpTable[10]=-5.5;
		lookUpTable[11]=5.5;
		
		lookUpTable[12]=5.5;
		lookUpTable[13]=5.5;
		
		lookUpTable[14]=16.5;
		lookUpTable[15]=5.5;
		
		lookUpTable[16]=-16.5;
		lookUpTable[17]=-5.5;
		
		lookUpTable[18]=-5.5;
		lookUpTable[19]=-5.5;
		
		lookUpTable[20]=5.5;
		lookUpTable[21]=-5.5;
		
		lookUpTable[22]=16.5;
		lookUpTable[23]=-5.5;
		
		lookUpTable[24]=-16.5;
		lookUpTable[25]=-16.5;
		
		lookUpTable[26]=-5.5;
		lookUpTable[27]=-16.5;
		
		lookUpTable[28]=5.5;
		lookUpTable[29]=-16.5;
		
		lookUpTable[30]=16.5;
		lookUpTable[31]=-16.5;
		for(int i = 0; i < 16; i++){
			GC4x4MB_1[i]="";
			GC4x4MB_4[i]="";
			GC4x4MB_3[i]="";
		}
		
		for(int i = 0; i < 16; i++){
			GC4x4MB_1[i]="blue";
		}
		
		for(int i = 0; i < 6; i++){
			GC4x4MB_4[i]="green";
		}
		
		for(int i = 0; i < 12; i++){
			GC4x4MB_3[i]="red";
		}
		addBehaviour(new CyclicBehaviour() {
			private static final long serialVersionUID = 1L;

			public void action() {
				try {
					ACLMessage msg = receive();
					if (msg!=null) {
						System.out.println("SA is now handling request: " + msg.getContent());
						if (msg.getPerformative() == MessageType.SUPPLIER_REQUEST){
							JSONObject partRequest = new JSONObject(new JSONTokener(msg.getContent()));
							partRequest.getJSONObject("target").remove("identifier");
							partRequest.getJSONObject("target").put("identifier", targetCrate);
							JSONArray parts =findPart(partRequest.getJSONObject("subjects").getString("color"));
							
							partRequest.remove("subjects");
							partRequest.put("subjects", parts);
							ACLMessage reply = msg.createReply();
		                    reply.setPerformative( MessageType.SUPPLIER_REQUEST_REPLY );
		                    reply.setContent(partRequest.toString());
		                    send(reply);
		                    System.out.println(this.myAgent.getLocalName() + " has replied to a message");
						}
						else {
							System.out.println(	"FAILED PARTSAGENTS");
						}
					}
					block();
				} catch (JSONException ex) {
					ex.printStackTrace();
				}
			}  
		});  
	}
	
	private JSONArray findPart(String color) throws JSONException{
		JSONArray subjects = new JSONArray();
		JSONObject subject = new JSONObject();
		JSONObject subjectMove = new JSONObject();
		
		if(color.equals("red")){
			for(int i =0; i < GC4x4MB_3.length; i++){
				if(GC4x4MB_3[i].equals("red")){
					subjectMove.put("x", (lookUpTable[(i*2)]));
					subjectMove.put("y", (lookUpTable[(i*2)+1]));
					subjectMove.put("z", "13.8");					
					subject.put("move",subjectMove);
					subject.put("identifier", "GC4x4MB_3");
					subjects.put(subject);
					GC4x4MB_3[i]="";
					break;
				}
			}				

		} else if(color.equals("blue")){
			for(int i =0; i < GC4x4MB_1.length; i++){	
				if(GC4x4MB_1[i].equals("blue")){
					subjectMove.put("identifier", "GC4x4MB_1");
					subjectMove.put("x", (lookUpTable[(i*2)]));
					subjectMove.put("y", (lookUpTable[(i*2)+1]));
					subjectMove.put("z", "13.8");
					subject.put("move",subjectMove);
					subject.put("identifier", "GC4x4MB_1");
					subjects.put(subject);
					GC4x4MB_1[i]="";
					break;
				}
			}
			
		} else if(color.equals("green")){
			for(int i =0; i < GC4x4MB_4.length; i++){	
				if(GC4x4MB_4[i].equals("green")){
					subjectMove.put("identifier", "GC4x4MB_4");
					subjectMove.put("x", (lookUpTable[(i*2)]));
					subjectMove.put("y", (lookUpTable[(i*2)+1]));
					subjectMove.put("z", "13.8");
					subject.put("move",subjectMove);
					subject.put("identifier", "GC4x4MB_4");
					subjects.put(subject);
					GC4x4MB_4[i]="";
					break;
				}
			}
			
		}
		return subjects;		
	}
}
