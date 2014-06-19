package HAL;

import java.net.UnknownHostException;
import java.util.ArrayList;

import com.google.gson.JsonObject;

import HAL.exceptions.FactoryException;
import HAL.exceptions.ModuleExecutingException;
import HAL.exceptions.ModuleTranslatingException;
import HAL.factories.ModuleFactory;
import HAL.listeners.ModuleListener;
import HAL.listeners.ProcessListener;
import HAL.steps.CompositeStep;
import HAL.steps.HardwareStep;
import HAL.steps.HardwareStep.HardwareStepStatus;
import libraries.blackboard_client.data_classes.GeneralMongoException;
import libraries.blackboard_client.data_classes.InvalidDBNamespaceException;
import libraries.blackboard_client.data_classes.InvalidJSONException;
import libraries.knowledgedb_client.KnowledgeException;
import libraries.math.Matrix;
import libraries.math.RotationAngles;
import libraries.math.Vector3;
import libraries.log.LogLevel;
import libraries.log.LogSection;
import libraries.log.Logger;
/**
 * Abstract representation of a actor module in HAL 
 * @author Bas Voskuijlen
 *
 */
public abstract class ModuleActor extends Module {
	protected static final String MODULE_COMMAND = "module_command";
	protected static final String APPROACH = "approach";
	protected static final String DESTINATION = "destination";
	protected static final String NULL = "NULL";
	protected static final String MAX_ACCELERATION = "maxAcceleration";
	protected static final String FORCE_STRAIGHT_LINE = "forceStraightLine";
	protected static final String MOVE_X = "x";
	protected static final String MOVE_Y = "y";
	protected static final String MOVE_Z = "z";
	
	protected static final String ROTATION_X = "rotationX";
	protected static final String ROTATION_Y = "rotationY";
	protected static final String ROTATION_Z = "rotationZ";
	
	protected static final String MOVE = "move";
	
	
	/**
	 * Constructs a new ModuleActor and connects to the blackboard.
	 * @param moduleIdentifier
	 * @param moduleFactory
	 * @param moduleListener
	 * @throws KnowledgeException
	 * @throws UnknownHostException
	 * @throws GeneralMongoException
	 */
	public ModuleActor(ModuleIdentifier moduleIdentifier, ModuleFactory moduleFactory, ModuleListener moduleListener) 
			throws KnowledgeException, UnknownHostException, GeneralMongoException {
		super(moduleIdentifier, moduleFactory,moduleListener);
	}
	public void setModuleListener(ModuleListener moduleListener){
		this.moduleListener = moduleListener;
	}
	/**
	 * Executes a command by inserting it in the blackboard.
	 * @param command
	 * @throws ModuleExecutingException
	 */
	protected void executeMongoCommand(JsonObject command){
		try {
			moduleFactory.getHAL().getBlackBoardHandler().postHardwareStep(command);
		} catch (InvalidJSONException ex) {
			throw new RuntimeException("Executing invalid JSON", ex);
		} catch (InvalidDBNamespaceException ex) {
			throw new RuntimeException("Executing invalid DBNamespace", ex);
		} catch (GeneralMongoException ex) {
			throw new RuntimeException("General mongo exception while trying to execute", ex);
		}
	}
	
	/**
	 * Forwards the remainder of the {@link CompositeStep} to the parent after this module has translated the CompositeStep
	 * @see http://wiki.agilemanufacturing.nl/index.php/HAL#Translation
	 * @param compositeStep
	 * @return The hardware steps resulted from the translation of the CompositeStep done by the parent modules.
	 * @throws ModuleTranslatingException if the CompositeStep could not completely be translated (which is the case if there is no parent module and the CompositeStep is not empty)  
	 * @throws FactoryException
	 */
	protected ArrayList<HardwareStep> forwardCompositeStep(CompositeStep compositeStep) throws ModuleTranslatingException, FactoryException {
		ModuleActor moduleActor = (ModuleActor) getParentModule();
		if (moduleActor != null) {
			ArrayList<HardwareStep> hardwareSteps = moduleActor.translateCompositeStep(compositeStep);
			if (hardwareSteps != null){
				return hardwareSteps;
			}
			return new ArrayList<HardwareStep>();
		} else {
			// root module, no more parents			
			// if commands remain then the modules were not able to fully translate the compositeStep
			// TODO better comparison method
			if (!compositeStep.getCommand().get(HardwareStep.COMMAND).getAsJsonObject().toString().trim().equalsIgnoreCase("{}")){
				throw new ModuleTranslatingException("The compositestep isn't completely empty." + 
						compositeStep.getCommand().get(HardwareStep.COMMAND).getAsJsonObject(), compositeStep);
			} else {
				//TODO
				//Logger
				return new ArrayList<HardwareStep>();
			}
		}
	}
	/**
	 * This method will execute the hardware step and forward any result to the {@link ProcessListener}
	 * @param processListener
	 * @param hardwareStep
	 * @throws ModuleExecutingException
	 */
	public void executeHardwareStep(ProcessListener processListener, HardwareStep hardwareStep) {
		this.processListener = processListener;
		JsonObject command = hardwareStep.toJSON();
		executeMongoCommand(command);
	}
	/**
	 * This method will translate the {@link CompositeStep} and forward the remainder to its parent.
	 * @param compositeStep
	 * @return The hardware steps resulted from the translation of the CompositeStep. 
	 * @throws ModuleTranslatingException
	 * @throws FactoryException
	 */
	abstract public ArrayList<HardwareStep> translateCompositeStep(CompositeStep compositeStep) throws ModuleTranslatingException, FactoryException;
	/**
	 * This method will forward the changed MAST module state to the {@link ModuleListener}
	 * Do not call this method!
	 */
	public void onModuleStateChanged(String state){
		moduleListener.onModuleStateChanged(state, this);
	}
	/**
	 * This method will forward the changed MAST module mode to the {@link ModuleListener}
	 * Do not call this method!
	 */
	public void onModuleModeChanged(String mode){
		moduleListener.onModuleModeChanged(mode, this);
	}
	
	/**
	 * Returns -1 if not found.
	 * 
	 */
	protected int getPlaceholderID(ArrayList<HardwareStep> hardwareSteps){
		if (hardwareSteps != null){
			for (int i=0;i<hardwareSteps.size();i++){
				if (hardwareSteps.get(i) == null){
					return i;
				}
			}
		}
		return -1;
	}
	
	protected CompositeStep adjustMoveWithDimensions(CompositeStep compositeStep, Vector3 offsetVector){
		JsonObject command = compositeStep.getCommand();
		command = adjustMoveWithDimensions(command, offsetVector);
		return new CompositeStep(compositeStep.getProductStep(), command, compositeStep.getRelativeTo());
	}
	protected JsonObject adjustMoveWithDimensions(JsonObject compositeCommand, Vector3 offsetVector){
		return adjustMoveWithDimensions(compositeCommand, offsetVector, new RotationAngles(0, 0, 0));
	}
	protected JsonObject adjustMoveWithDimensions(JsonObject compositeCommand, Vector3 offsetVector, RotationAngles directionAngles){
		Logger.log(LogSection.HAL_MODULES, LogLevel.DEBUG, "Adjusting move with dimentions: " + compositeCommand.toString() + 
				", offsetVector: " + offsetVector + " directionAngles: " + directionAngles);
		
		JsonObject originalMove = compositeCommand.remove(MOVE).getAsJsonObject();
		if (originalMove != null){
			double originalX = originalMove.get(MOVE_X).getAsDouble();
			double originalY = originalMove.get(MOVE_Y).getAsDouble();
			double originalZ = originalMove.get(MOVE_Z).getAsDouble();
			
			Matrix rotationMatrix = directionAngles.generateRotationMatrix();
			
			Vector3 originalVector = offsetVector;
			Vector3 rotatedVector = originalVector.rotate(rotationMatrix);
			
			JsonObject adjustedMove = new JsonObject();
			adjustedMove.addProperty(MOVE_X, originalX + rotatedVector.x);
			adjustedMove.addProperty(MOVE_Y, originalY + rotatedVector.y);
			adjustedMove.addProperty(MOVE_Z, originalZ + rotatedVector.z);
			
			compositeCommand.add(MOVE, adjustedMove);
		}
		else {
			Logger.log(LogSection.HAL_TRANSLATION, LogLevel.NOTIFICATION, 
					"CompositeStep command does not contain any move key to adjust. " + compositeCommand);
		}
		return compositeCommand;
	}
	


	@Override
	public void onProcessStatusChanged(String status) {
		if(processListener != null){
			processListener.onProcessStateChanged(status, 0, this);
			if(status.equals(HardwareStepStatus.DONE) || status.equals(HardwareStepStatus.FAILED)){
				processListener = null;
			}
		}
	}
}
