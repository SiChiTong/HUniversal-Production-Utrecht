package simulation.simulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import org.jfree.data.gantt.Task;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.time.SimpleTimePeriod;
import org.jfree.ui.RefineryUtilities;

import simulation.graphics.GanttChart;
import simulation.mas.Product;
import simulation.mas.scheduling.Graph;
import simulation.mas.scheduling.Node;
import simulation.util.Position;
import simulation.util.ProductStep;
import simulation.util.ProductionStep;

public class Test {
	
	public static void main(String[] args) {
		productScheduling();	
	}
	
	
	public void testGraph() {
		Graph<TNode> graph = new Graph<TNode>();

		TNode source = new TNode("source");
		TNode sink = new TNode("sink");

		//rij 1
		TNode n1 = new TNode("node 1");
		TNode n2 = new TNode("node 2");
		TNode n3 = new TNode("node 3");
		//rij 2
		TNode n4 = new TNode("node 4");
		TNode n5 = new TNode("node 5");
		// rij 3
		TNode n6 = new TNode("node 6");
		TNode n7 = new TNode("node 7");
		TNode n8 = new TNode("node 8");

		graph.add(source, n1, 0);
		graph.add(source, n2, 0);
		graph.add(source, n3, 0);

		graph.add(n1, n4, 10);
		graph.add(n1, n5, 12);

		graph.add(n2, n4, 5);
		graph.add(n3, n5, 5);

		graph.add(n4, n6, 10);
		graph.add(n5, n7, 9);
		graph.add(n5, n8, 8);

		graph.add(n6, sink, 0);
		graph.add(n7, sink, 0);
		graph.add(n8, sink, 0);

		LinkedList<TNode> path = graph.optimumPath(source, sink);
		System.out.println("graph: " + graph);
		System.out.println("path: " + path);
	}

	public static void productScheduling() {
		System.out.println("Start product scheduling test...");

		System.out.println("TEst NODE + " + new Node(4));
		LinkedList<ProductStep> productSteps = new LinkedList<ProductStep>();
		productSteps.add(new ProductStep("screw", new HashMap<String, Object>(), 30));
		productSteps.add(new ProductStep("glue", new HashMap<String, Object>(), 40));

		Product p0 = new Product("P0", 0, productSteps, new Position(0,0));
		System.out.println("P1: " + p0 + "\n");
		
		Product p1 = new Product("P1", 0, productSteps, new Position(0,0));
		System.out.println("P1: " + p1 + "\n");

		Product p2 = new Product("P2", 10, productSteps, new Position(0,0));
		System.out.println("P2: " + p2 + "\n");

		Product p3 = new Product("P3", 20, productSteps, new Position(0,0));
		System.out.println("P3: " + p3 + "\n");

		Product p4 = new Product("P4", 30, productSteps, new Position(0,0));
		System.out.println("P4: " + p4 + "\n");

		Product p5 = new Product("P5", 40, productSteps, new Position(0,0));
		System.out.println("P5: " + p5 + "\n");

		ArrayList<Product> agents = new ArrayList<>();
		agents.add(p1);
		agents.add(p2);
		agents.add(p3);
		agents.add(p4);
		agents.add(p5);
		output(agents);
	}

	/**
	 * This test whether the scheduling of a product fails when there are no
	 * capable equiplets
	 */
	public void productSchedulingFailed() {
		System.out.println("Start product scheduling test...");

		LinkedList<ProductStep> productSteps = new LinkedList<ProductStep>();
		productSteps.add(new ProductStep("Fails", new HashMap<String, Object>(), 30));
		Product p1 = new Product("PFAILED", 0, productSteps, new Position(0,0));

		System.out.println("P1: " + p1);

		Grid grid = Grid.getInstance();
		System.out.println("grid: " + grid);
	}

	/**
	 * give the output of the product agents
	 * 
	 * @param agents
	 */
	private static void output(ArrayList<Product> agents) {
		ArrayList<TaskSeries> tasks = new ArrayList<>();

		for (Product agent : agents) {
			LinkedList<ProductionStep> path = agent.getProductionPath();
			TaskSeries serie = new TaskSeries(agent.getName());
			for (ProductionStep step : path) {
				serie.add(new Task(step.getEquiplet(), new SimpleTimePeriod((long) step.getTime(), (long)(step.getTime() + step.getDuration()))));
			}
			tasks.add(serie);
		}

		final GanttChart graph = new GanttChart("Schedule Chart", tasks);
		graph.pack();
		RefineryUtilities.centerFrameOnScreen(graph);
		graph.setVisible(true);
	}

	/**
	 * @author laurens
	 *         Test node for the graph
	 */
	static class TNode {
		String name;

		public TNode(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}
}