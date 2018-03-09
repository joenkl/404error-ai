import java.util.*;
import java.util.Map.Entry;

public class BTSolver
{

	// =================================================================
	// Properties
	// =================================================================

	private ConstraintNetwork network;
	private SudokuBoard sudokuGrid;
	private Trail trail;

	private boolean hasSolution = false;

	public String varHeuristics;
	public String valHeuristics;
	public String cChecks;

	// =================================================================
	// Constructors
	// =================================================================

	public BTSolver ( SudokuBoard sboard, Trail trail, String val_sh, String var_sh, String cc )
	{
		this.network    = new ConstraintNetwork( sboard );
		this.sudokuGrid = sboard;
		this.trail      = trail;

		varHeuristics = var_sh;
		valHeuristics = val_sh;
		cChecks       = cc;
	}

	// =================================================================
	// Consistency Checks
	// =================================================================

	// Basic consistency check, no propagation done
	private boolean assignmentsCheck ( )
	{
		for ( Constraint c : network.getConstraints() )
			if ( ! c.isConsistent() )
				return false;

		return true;
	}

	/**
	 * Part 1 TODO: Implement the Forward Checking Heuristic
	 *
	 * This function will do both Constraint Propagation and check
	 * the consistency of the network
	 *
	 * (1) If a variable is assigned then eliminate that value from
	 *     the square's neighbors.
	 *
	 * Note: remember to trail.push variables before you change their domain
	 * Return: true is assignment is consistent, false otherwise
	 */

	private boolean forwardChecking() {
		for (Variable v : network.getVariables()) {
			if (v.isAssigned()) {
				List<Constraint> mcList = network.getModifiedConstraints();

				for (Variable neighbor : network.getNeighborsOfVariable(v)) {
					//check if the neighbor is not in the recently modified constrains,
					//and we did not visit it in this iteration
					if (!mcList.contains(neighbor)) {

						if (neighbor.isAssigned() && neighbor.getAssignment() == v.getAssignment())
							return false;
						if (neighbor.getDomain().size() == 1 &&
							neighbor.getDomain().getValues().contains(v.getAssignment()))
								return false;
						if (neighbor.isChangeable()) {
							//check if domain contain the value
							//if not, there is no need to remove
							if (neighbor.getDomain().contains(v.getAssignment())) {
								neighbor.setModified(true);
								trail.push(neighbor);
								neighbor.removeValueFromDomain(v.getAssignment());
								if (neighbor.getDomain().isEmpty()) {
									return false;
								}
							}
						}
					}
				}
			}
		}
		return assignmentsCheck();
	}

	/**
	 * Part 2 TODO: Implement both of Norvig's Heuristics
	 *
	 * This function will do both Constraint Propagation and check
	 * the consistency of the network
	 *
	 * (1) If a variable is assigned then eliminate that value from
	 *     the square's neighbors.
	 *
	 * (2) If a constraint has only one possible place for a value
	 *     then put the value there.
	 *
	 * Note: remember to trail.push variables before you change their domain
	 * Return: true is assignment is consistent, false otherwise
	 */
	private boolean norvigCheck ( )
	{
		return false;
	}

	/**
	 * Optional TODO: Implement your own advanced Constraint Propagation
	 *
	 * Completing the three tourn heuristic will automatically enter
	 * your program into a tournament.
	 */
	private boolean getTournCC ( )
	{
		return false;
	}

	// =================================================================
	// Variable Selectors
	// =================================================================

	// Basic variable selector, returns first unassigned variable
	private Variable getfirstUnassignedVariable()
	{
		for ( Variable v : network.getVariables() )
			if ( ! v.isAssigned() )
				return v;

		// Everything is assigned
		return null;
	}

	/**
	 * Part 1 TODO: Implement the Minimum Remaining Value Heuristic
	 *
	 * Return: The unassigned variable with the smallest domain
	 */
	private Variable getMRV ( )
	{
		Variable mrvVariable = null;
		int minValues = 99999;
		
		for (Variable v : network.getVariables())
		if(!v.isAssigned()){
			if (v.getDomain().isEmpty())
			return null;
			
			if(v.getDomain().size() < minValues)
			{
			mrvVariable = v;
			minValues = v.getDomain().size();
			}
		}  
		return mrvVariable;
	}

	/**
	 * Part 2 TODO: Implement the Degree Heuristic
	 *
	 * Return: The unassigned variable with the most unassigned neighbors
	 */
	private Variable getDegree ( )
	{
		int constraints = 0, maxConstraints = -1;
		Variable returnValue = null;
		boolean isMax = false;
		for(Variable v: network.getVariables()){
			if(!v.isAssigned()){
				constraints = 0;
				for(Variable n : network.getNeighborsOfVariable(v)){
					if(!n.isAssigned()) constraints++;
				}
				if(constraints > maxConstraints){
					maxConstraints = constraints;
					returnValue = v;
					isMax = true;
				}
			}
		}
		return returnValue;
		
	}

	/**
	 * Part 2 TODO: Implement the Minimum Remaining Value Heuristic
	 *                with Degree Heuristic as a Tie Breaker
	 *
	 * Return: The unassigned variable with, first, the smallest domain
	 *         and, second, the most unassigned neighbors
	 */
	private int getDegreeOfVar(Variable v){
		int degree = 0;
		for(Variable n : network.getNeighborsOfVariable(v)){
			if(!n.isAssigned()){
				degree++;
			}
		}
		return degree;
	}

	private Variable MRVwithTieBreaker ( )
	{
		Variable mrvVariable = null;
		int minValues = 99999;
		
		for (Variable v : network.getVariables())
		if(!v.isAssigned()){
			if (v.getDomain().isEmpty())
			return null;
			
			if(v.getDomain().size() < minValues)
			{
				mrvVariable = v;
				minValues = v.getDomain().size();
			}

			if (v.getDomain().size() == minValues)
			{
				int vDegree = getDegreeOfVar(v);
				int mrvDegree = getDegreeOfVar(mrvVariable);

				if (vDegree < mrvDegree)
				{
					mrvVariable = v;
					minValues = v.getDomain().size();
				}
			}
		}  
		return mrvVariable;
	}

	/**
	 * Optional TODO: Implement your own advanced Variable Heuristic
	 *
	 * Completing the three tourn heuristic will automatically enter
	 * your program into a tournament.
	 */
	private Variable getTournVar ( )
	{
		return null;
	}

	// =================================================================
	// Value Selectors
	// =================================================================

	// Default Value Ordering
	public List<Integer> getValuesInOrder ( Variable v )
	{
		List<Integer> values = v.getDomain().getValues();

		Comparator<Integer> valueComparator = new Comparator<Integer>(){

			@Override
			public int compare(Integer i1, Integer i2) {
				return i1.compareTo(i2);
			}
		};
		Collections.sort(values, valueComparator);
		return values;
	}

	/**
	 * Part 1 TODO: Implement the Least Constraining Value Heuristic
	 *
	 * The Least constraining value is the one that will knock the least
	 * values out of it's neighbors domain.
	 *
	 * Return: A list of v's domain sorted by the LCV heuristic
	 *         The LCV is first and the MCV is last
	 */

	public List<Integer> getValuesLCVOrder ( Variable v )
	{
		HashMap<Integer, Integer> values = new HashMap<Integer, Integer>();
		for(Integer value : v.getDomain().getValues()) {
			int constrainsCount = 0;
			for (Variable n : network.getNeighborsOfVariable(v))
				if (n.getDomain().contains(value)){
					constrainsCount++;
				}

			values.put(value, constrainsCount);
		}
		
		Comparator<Map.Entry<Integer, Integer>> valueComparator = new Comparator<Map.Entry<Integer,Integer>>(){
		
			@Override
			public int compare(Map.Entry<Integer, Integer> v1, Map.Entry<Integer, Integer> v2) {
				return v1.getValue().compareTo(v2.getValue());
			}
		};

		List<Map.Entry<Integer, Integer>> mapList = new LinkedList<>(values.entrySet());

		Collections.sort(mapList, valueComparator);

		List<Integer> result = new ArrayList<Integer>();
		Iterator it = mapList.iterator();
		while (it.hasNext()){
			Map.Entry pair = (Map.Entry) it.next();
			result.add((int)pair.getKey());
		}
		return result;
	}

	/**
	 * Optional TODO: Implement your own advanced Value Heuristic
	 *
	 * Completing the three tourn heuristic will automatically enter
	 * your program into a tournament.
	 */
	public List<Integer> getTournVal ( Variable v )
	{
		return null;
	}

	//==================================================================
	// Engine Functions
	//==================================================================

	public void solve ( )
	{
		if ( hasSolution )
			return;

		// Variable Selection
		Variable v = selectNextVariable();

		if ( v == null )
		{
			for ( Variable var : network.getVariables() )
			{
				// If all variables haven't been assigned
				if ( ! var.isAssigned() )
				{
					System.out.println( "Error" );
					return;
				}
			}

			// Success
			hasSolution = true;
			return;
		}

		// Attempt to assign a value
		for ( Integer i : getNextValues( v ) )
		{
			// Store place in trail and push variable's state on trail
			trail.placeTrailMarker();
			trail.push( v );

			// Assign the value
			v.assignValue( i );

			// Propagate constraints, check consistency, recurse
			if ( checkConsistency() )
				solve();

			// If this assignment succeeded, return
			if ( hasSolution )
				return;

			// Otherwise backtrack
			trail.undo();
		}
	}

	private boolean checkConsistency ( )
	{
		switch ( cChecks )
		{
			case "forwardChecking":
				return forwardChecking();

			case "norvigCheck":
				return norvigCheck();

			case "tournCC":
				return getTournCC();

			default:
				return assignmentsCheck();
		}
	}

	private Variable selectNextVariable ( )
	{
		switch ( varHeuristics )
		{
			case "MinimumRemainingValue":
				return getMRV();

			case "Degree":
				return getDegree();

			case "MRVwithTieBreaker":
				return MRVwithTieBreaker();

			case "tournVar":
				return getTournVar();

			default:
				return getfirstUnassignedVariable();
		}
	}

	public List<Integer> getNextValues ( Variable v )
	{
		switch ( valHeuristics )
		{
			case "LeastConstrainingValue":
				return getValuesLCVOrder( v );

			case "tournVal":
				return getTournVal( v );

			default:
				return getValuesInOrder( v );
		}
	}

	public boolean hasSolution ( )
	{
		return hasSolution;
	}

	public SudokuBoard getSolution ( )
	{
		return network.toSudokuBoard ( sudokuGrid.getP(), sudokuGrid.getQ() );
	}

	public ConstraintNetwork getNetwork ( )
	{
		return network;
	}
}
