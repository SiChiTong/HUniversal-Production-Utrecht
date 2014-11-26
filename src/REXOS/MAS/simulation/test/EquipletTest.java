package MAS.simulation.test;

import java.util.ArrayList;
import java.util.List;

import MAS.equiplet.Capability;
import MAS.equiplet.EquipletAgent;
import MAS.equiplet.Job;
import MAS.util.Pair;
import MAS.util.Position;
import MAS.util.Tick;

class EquipletTest extends EquipletAgent {
	private static final long serialVersionUID = 1L;

	public EquipletTest() {
		init(new Position(-1, -1), new ArrayList<Capability>());
	}

	@Override
	protected void execute(Job job) {
		executing = job;
	}

	protected boolean schedule(Job job) {
		return schedule.add(job);
	}

	@Override
	public double load(Tick time, Tick window) {
		return super.load(time, window);
	}

	@Override
	public double loadHistory(Tick time, Tick window) {
		return super.loadHistory(time, window);
	}

	public void history(Job job) {
		history.add(job);
	}

	@Override
	public List<Pair<Tick, Tick>> available(Tick time, Tick duration, Tick deadline) {
		return super.available(time, duration, deadline);
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("EQ schedule:");
		if (isExecuting()) {
			builder.append("\nExe\t" + executing);
		}
		for (Job j : schedule) {
			builder.append("\n\t" + j);
		}
		return builder.toString();
	}
}
