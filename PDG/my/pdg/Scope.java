package my.pdg;

import my.graphStructures.VarChanges;

import java.util.ArrayList;

/**
 * The Class Scope.
 */
class Scope {
	
	/** The var changes. */
	ArrayList<VarChanges> varChanges = new ArrayList<>();
	
	/** The var accesses. */
	ArrayList<VarChanges> varAccesses = new ArrayList<>();
}
