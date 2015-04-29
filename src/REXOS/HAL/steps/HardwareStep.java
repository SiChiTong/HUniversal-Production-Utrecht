package HAL.steps;

import org.json.JSONException;
import org.json.JSONObject;

import util.log.LogLevel;
import util.log.LogSection;
import util.log.Logger;
import HAL.Module;
import HAL.dataTypes.ModuleIdentifier;

/**
 * A HardwareStep is a step that is completely translated and can be processed by the ROS node corresponding to this HardwareStep.
 * HardwareSteps are generated by {@link Module}s and interpreted by ROS.
 * @author Bas Voskuijlen
 *
 */
public class HardwareStep implements Cloneable{
	public enum HardwareStepStatus {
		DONE,
		FAILED,
		IN_PROGRESS,
		WAITING
	}
	
	public static final String ORIGIN_PLACEMENT = "originPlacement";
	public static final String INSTRUCTION_DATA = "instructionData";
	public static final String PAYLOAD = "payload";
	public static final String STATUS = "status";
	public static final String MODULE_IDENTIFIER = "moduleIdentifier";
	
	/*
	public static final String MODULE_IDENTIFIER_MANUFACTURER = "manufacturer";
	public static final String MODULE_IDENTIFIER_TYPE_NUMBER = "typeNumber";
	public static final String MODULE_IDENTIFIER_SERIAL_NUMBER = "serialNumber";
*/

	private ModuleIdentifier moduleIdentifier;
	private CompositeStep compositeStep;
	private HardwareStepStatus status;

	private JSONObject instructionData;
	private OriginPlacement originPlacement;

	
	public HardwareStep(ModuleIdentifier moduleIdentifier, CompositeStep compositeStep, 
			HardwareStepStatus status, JSONObject instructionData) {
		javaIsGayConstructor(moduleIdentifier, compositeStep, status, instructionData, null);
	}
	public HardwareStep(ModuleIdentifier moduleIdentifier, CompositeStep compositeStep, 
			JSONObject instructionData, OriginPlacement originPlacement) {
		javaIsGayConstructor(moduleIdentifier, compositeStep, HardwareStepStatus.WAITING, instructionData, originPlacement);
	}
	public HardwareStep(ModuleIdentifier moduleIdentifier, CompositeStep compositeStep, 
			HardwareStepStatus status, JSONObject instructionData, OriginPlacement originPlacement) {
		javaIsGayConstructor(moduleIdentifier, compositeStep, status, instructionData, originPlacement);
	}
	
	private void javaIsGayConstructor(ModuleIdentifier moduleIdentifier, CompositeStep compositeStep, HardwareStepStatus status, JSONObject instructionData, OriginPlacement originPlacement) {
		this.moduleIdentifier = moduleIdentifier;
		this.compositeStep = compositeStep;
		this.instructionData = instructionData;
		this.status = status;
		this.originPlacement = originPlacement;
	}
	
	public ModuleIdentifier getModuleIdentifier() {
		return this.moduleIdentifier;
	}
	public CompositeStep getCompositeStep() {
		return this.compositeStep;
	}
	public HardwareStepStatus getStatus() {
		return this.status;
	}
	public void setStatus(HardwareStepStatus status) {
		this.status = status;
		
	}
	public JSONObject getInstructionData() {
		return this.instructionData;
	}
	public OriginPlacement getOriginPlacement() {
		return this.originPlacement;
	}
	
	
	public JSONObject toJSON() {
		JSONObject returnValue = new JSONObject();
		try {
			returnValue.put(MODULE_IDENTIFIER, moduleIdentifier.serialize());
			returnValue.put(STATUS, status.toString());
			returnValue.put(INSTRUCTION_DATA, instructionData);
			if(originPlacement != null) {
				returnValue.put(ORIGIN_PLACEMENT, originPlacement.toJSON());
			} else {
				returnValue.put(ORIGIN_PLACEMENT, JSONObject.NULL);
			}
		} catch (JSONException ex) {
			Logger.log(LogSection.HAL, LogLevel.EMERGENCY, "Error occurred which is considered to be impossible", ex);
		}
		return returnValue;
	}
	
	public String toString() {
		return toJSON().toString();
	}
	public HardwareStep clone() {
		try {
			return new HardwareStep(moduleIdentifier, compositeStep, status, new JSONObject(instructionData.toString()), originPlacement);
		} catch (JSONException ex) {
			Logger.log(LogSection.HAL, LogLevel.EMERGENCY, "Error occurred which is considered to be impossible", ex);
			return null;
		}
	}
}
