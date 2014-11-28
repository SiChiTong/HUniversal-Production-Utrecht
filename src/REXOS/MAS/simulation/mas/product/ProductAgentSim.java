package MAS.simulation.mas.product;

import java.util.LinkedList;

import org.json.JSONException;

import MAS.simulation.simulation.ISimulation;
import MAS.simulation.util.Parser;
import MAS.simulation.util.Position;
import MAS.simulation.util.Settings;
import MAS.simulation.util.Tick;

public class ProductAgentSim extends ProductAgent implements IProductSim {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ISimulation simulation;

	// TODO created has become a Tick, so maybe it is not needed to store here, but let the real agent handle the tick, or describe way this is here
	private Tick created;

	public ProductAgentSim(ISimulation simulation, LinkedList<ProductStep> productSteps, Position position, Tick time, Tick deadline) {
		try {
			Object[] args = new Object[] { Parser.parseProductConfiguration(productSteps, position, deadline) };
			setArguments(args);
		} catch (JSONException e) {
			System.err.printf("PA: failed to create product: %s.\n", e.getMessage());
		}

		this.simulation = simulation;
		this.created = time;
	}

	@Override
	public void kill() {
		// TODO handle properly, let the equiplet know that I been killed
		System.out.printf("PA:%s terminating\n", getLocalName());
		doDelete();
	}

	@Override
	public void onProductArrived(Tick time) {
		super.onProductArrived(time);

		// getStart() could be due - processing time.
		// or the start from ((ps + 1) - (travel time ps to ps + 1) + (ps processing time))
		ProductionStep step = getCurrentStep();
		simulation.notifyProductShouldStart(getLocalName(), step.getStart(), step.getIndex());
	}

	@Override
	public Position getPosition() {
		return super.getPosition();
	}

	@Override
	public Tick getCreated() {
		return created;
	}

	@Override
	public Tick getDeadline() {
		return super.getDeadline();
	}

	@Override
	protected void onProductStepFinished(Tick time) {
		super.onProductStepFinished(time);

		// After regular behaviour when a product step is finished, inform also the simulation
		if (getProductState() == ProductState.FINISHED) {
			// notify the simulation that the product is finished
			simulation.notifyProductFinished(getLocalName());
			simulation.log(Settings.PRODUCT_LOG, getLocalName(), "PA:" + getLocalName() + " finished: " + history);
		} else if (getProductState() == ProductState.TRAVELING) {
			// notify the simulation that the product is traveling
			simulation.notifyProductTraveling(getLocalName(), getCurrentStep().getEquipletName());
		}
	}

	@Override
	protected void onProductProcessing(Tick time) {
		super.onProductProcessing(time);

		// notify the simulation that processing begins
		ProductionStep step = getCurrentStep();
		step.updateStart(time);
		simulation.notifyProductProcessing(getLocalName(), step.getEquipletName(), step.getService(), step.getIndex());
	}

	int retry = 0;
	@Override
	protected void schedulingFinished(Tick time, boolean succeeded) {
		System.out.printf("PA:%s scheduling finished %b. \n", getLocalName(), succeeded);
		// let the simulation know that the creation of product agent failed
		if (reschedule && succeeded) {
			reschedule = false;
			simulation.notifyProductRescheduled(getLocalName(), getCurrentStep().getEquipletName(), succeeded);
		} else if (reschedule) {
//			throw new IllegalArgumentException("FUCK");
			// retry++;
			// reschedule with new deadline
			Tick deadline = getDeadline().add(getDeadline().minus(getCreated()).multiply(++retry));
			System.out.printf("PA:%s try rescheduling again at %s with new deadline %s. \n", getLocalName(), time, deadline);
			reschedule(time, deadline);
		} else if (succeeded) {
			simulation.notifyProductCreated(getLocalName(), getCurrentStep().getEquipletName());
		} else {
			simulation.notifyProductCreationFailed(getLocalName());
		}
	}

	@Override
	public void onProductStarted(Tick time, int index) {
		if (getCurrentStep().getIndex() == index && (state == ProductState.ERROR || state == ProductState.SCHEDULING || state == ProductState.TRAVELING)) {
			// throw new IllegalArgumentException("on product started event given in wrong state " + getProductState() + "");
		} else {
			super.onProductStarted(time, index);
		}

		if (!reschedule) {
			simulation.notifyProductRescheduled(false);
		}
	}
}
