package HAL.capabilities;

import generic.Criteria;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import util.log.LogLevel;
import util.log.LogSection;
import util.log.Logger;
import HAL.Capability;
import HAL.ModuleActor;
import HAL.exceptions.CapabilityException;
import HAL.factories.ModuleFactory;
import HAL.steps.CompositeStep;
import HAL.steps.HardwareStep;
import HAL.steps.OriginPlacement;
import HAL.steps.OriginPlacementType;

/**
 * 
 * @author Aristides Ayala Mendoza
 *
 */
public class Draw extends Capability {	
	public final static String SERVICE_IDENTIFIER = "draw";
	/**
	 * 
	 * @param moduleFactory
	 */
	public Draw(ModuleFactory moduleFactory) {
		super(moduleFactory, "Draw");
	}

	/**
	 * @see Capability#translateProductStep(String, JSONObject)
	 */
	@Override
	public ArrayList<HardwareStep> translateProductStep(String service, JSONObject criteria) throws CapabilityException {
		try {
			JSONObject target = criteria.getJSONObject(Criteria.TARGET);
			
			if(service.equals(SERVICE_IDENTIFIER) == false) {
				String message = "Received a service (" + service + "which is not supported by this capability.";
				Logger.log(LogSection.HAL_CAPABILITIES, LogLevel.ERROR, message);
				throw new IllegalArgumentException(message);	
			}
			if(target == null) {
				String message = "Received a illegaly formatted job: " + service + " with criteria " + criteria;
				Logger.log(LogSection.HAL_CAPABILITIES, LogLevel.ERROR, message);
				throw new IllegalArgumentException(message);
			}
			
			// draw
			JSONObject targetMoveCommand = target.getJSONObject("move");
			
			JSONObject drawCommand = new JSONObject();
			drawCommand.put("draw", JSONObject.NULL);
			drawCommand.put("move", targetMoveCommand);
			
			JSONObject drawOriginPlacementParameters = new JSONObject();
			drawOriginPlacementParameters.put("identifier", target.getString(CompositeStep.IDENTIFIER));
			OriginPlacement drawOriginPlacement = new OriginPlacement(OriginPlacementType.RELATIVE_TO_PART_ORIGIN, drawOriginPlacementParameters);
			
			CompositeStep draw = new CompositeStep(service, drawCommand, drawOriginPlacement);
			Logger.log(LogSection.HAL_CAPABILITIES, LogLevel.DEBUG, "draw: " + draw);
					
			// Translate to hardwareSteps
			ArrayList<ModuleActor> modules = moduleFactory.getBottomModulesForFunctionalModuleTree(this, 1);
			ArrayList<CompositeStep> compositeSteps = new ArrayList<CompositeStep>();
			compositeSteps.add(draw);
				
			ArrayList<HardwareStep> hardwareSteps = translateCompositeStep(modules, compositeSteps);
			Logger.log(LogSection.HAL_CAPABILITIES, LogLevel.INFORMATION, "Translated hardware steps: " + hardwareSteps.toString());
			return hardwareSteps;
		} catch(JSONException ex) {
			Logger.log(LogSection.HAL_CAPABILITIES, LogLevel.ERROR, "Unable to translate due to illegally formatted JSON", ex);
			throw new CapabilityException("Unable to translate due to illegally formatted JSON", ex);
		}
	}

}
