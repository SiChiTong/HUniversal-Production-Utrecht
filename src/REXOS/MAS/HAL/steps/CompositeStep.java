package HAL.steps;

import generic.ProductStep;

import java.io.Serializable;

import HAL.Module;
import HAL.capabilities.Capability;

import com.google.gson.JsonObject;

/**
 * A CompositeStep is a step composed of multiple abstract {@link HardwareStep}s.
 * CompositeSteps are generated by {@link Capability} and interpeted by {@link Module}s.
 * @author Tommas Bakker
 *
 */
public class CompositeStep implements Serializable{
	private static final long serialVersionUID = 1206944727256435741L;
	private ProductStep productStep;
	private JsonObject command;
	
	public CompositeStep(ProductStep productStep, JsonObject command){
		this.command = command;
		this.productStep = productStep;
	}
	
	public ProductStep getProductStep(){
		return this.productStep;
	}
	public JsonObject getCommand(){
		return this.command;
	}
}
