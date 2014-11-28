package MAS.simulation.mas.product;

import MAS.simulation.util.Position;
import MAS.simulation.util.Tick;

public interface IProductSim {

	void onProductArrived(Tick time);

	void onProductStarted(Tick time, int index);

	Position getPosition();

	Tick getCreated();

	Tick getDeadline();

	void kill();
}
