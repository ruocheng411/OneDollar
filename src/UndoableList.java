/**
 * @author <a href="mailto:gery.casiez@lifl.fr">Gery Casiez</a>
 */

import javax.swing.DefaultListModel;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;


public class UndoableList extends AbstractUndoableEdit { 
	private final DefaultListModel modelList;
	private final String item;
	private final int index;
	private final String operation;
	
	// Ajout d'un element dans la liste
	public UndoableList(DefaultListModel modelList, String item, int index) {
		this.modelList = modelList;
		this.index = index;
		this.operation = "ajout";
		this.item = item;
	} 

	// Suppression d'un element de la liste
	public UndoableList(DefaultListModel modelList, int index) {
		this.modelList = modelList;
		this.index = index;
		this.operation = "suppression";
		this.item = (String) modelList.getElementAt(index);
	} 
	
	public String getPresentationName() {
		return operation + " " + item;
	}
	
	public void redo() throws CannotRedoException { 
		super.redo(); 
		if (operation.compareTo("ajout") == 0) modelList.insertElementAt(item, index);
		if (operation.compareTo("suppression") == 0) modelList.remove(index);
	} 
	public void undo() throws CannotUndoException { 
		super.undo();
		if (operation.compareTo("ajout") == 0) modelList.remove(index);
		if (operation.compareTo("suppression") == 0) modelList.insertElementAt(item, index);
	} 
}
