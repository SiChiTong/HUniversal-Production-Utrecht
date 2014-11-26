package MAS.util;

public class Settings {

	/**
	 * VERBOSITY gives the level of simulation output
	 * 0: none output is given while running the simulation
	 * 1: important simulation debug is printed to the console
	 * 2: the gui is used for information during the simulation
	 * 3: the gui is used in combination with the debug information in the console
	 * 4: all above with addition of scheduling information
	 */
	public static int VERBOSITY = 3;

	/**
	 * whether the simulation uses stochastic processing times and other time consuming variables.
	 */
	public final static boolean STOCHASTICS = true;

	/**
	 * whether equiplets can breakdown
	 */
	public final static boolean BREAKDOWNS = false;

	/**
	 * time penalty of a reconfiguration. the time it takes to (re) config an equiplet
	 */
	public static final double RECONFIGATION_TIME = 200;

	/**
	 * the number of jobs an just arrives job can overtake in the queue
	 */
	public static final int QUEUE_JUMP = 0;

	/**
	 * Scheduling algorithm used by product agent to schedule his product step
	 */
	public final static SchedulingAlgorithm SCHEDULING = SchedulingAlgorithm.MATRIX;

	/**
	 * input and output locations of the simulation variables
	 */
	public final static String SIMULATION_CONFIG = "simulation/simulation.xml";
	public final static String SIMULATION_EQUIPLET_CONFIG = "simulation/equiplets.csv";
	public final static String SIMULATION_OUTPUT = "simulation/output";

	/**
	 * name of the traffic controller agent
	 */
	public final static String TRAFFIC_AGENT = "traffic-controller";

	/**
	 * Communication time out, the time an agent wait when he is expecting a communication message until he continues
	 */
	public final static long COMMUNICATION_TIMEOUT = 30000;

}