/**
 * @author <a href="mailto:gery.casiez@lifl.fr">Gery Casiez</a>
 */

import java.awt.event.*;
import javax.swing.*;
import javax.swing.undo.UndoManager;


public class TestGUI {
	// Actions
	ActionSupprimer actionSuppr = new ActionSupprimer();
	ActionAjouter actionAjout = new ActionAjouter();
	ActionAnnuler actionAnnuler = new ActionAnnuler();
	ActionRefaire actionRefaire = new ActionRefaire();

	private DefaultListModel listModel;
	private JTextField text;
	private JList list;
	UndoManager manager;

	TestGUI() {
		manager = new UndoManager();


		JFrame fen = new JFrame("Reconnaissance de gestes");
		fen.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		fen.setLocationRelativeTo(null);

		listModel = new DefaultListModel();
		listModel.addElement("Université Lille 1");
		listModel.addElement("Master 1 informatique");
		listModel.addElement("IHM");
		list = new JList(listModel);
		list.setDragEnabled(true);
		list.setDropMode(DropMode.INSERT);
		JScrollPane listScroller = new JScrollPane(list);

		text = new JTextField();
		text.setAction(actionAjout);

		JButton boutonAjouter = new JButton("Ajouter");
		boutonAjouter.setAction(actionAjout);
		boutonAjouter.setAlignmentX(JComponent.LEFT_ALIGNMENT);

		JCheckBox changeButton = new JCheckBox("Visualiser les gestes");
		changeButton.setSelected(true);

		// Menu
		JMenuBar menuBar = new JMenuBar();
		JMenu menu = new JMenu("Edition");
		menuBar.add(menu);
		JMenuItem menuItem = new JMenuItem(actionAnnuler);
		menu.add(menuItem);
		menuItem = new JMenuItem(actionRefaire);
		menu.add(menuItem);

		// ToolBar
		JToolBar toolBar = new JToolBar("Barre d'outils");
		JButton boutonSupprimer = new JButton("Supprimer");
		boutonSupprimer.setAction(actionSuppr);
		JButton boutonAnnuler = new JButton("Annuler");
		boutonAnnuler.setAction(actionAnnuler);
		JButton boutonRefaire = new JButton("Refaire");
		boutonRefaire.setAction(actionRefaire);
		toolBar.add(boutonSupprimer);
		toolBar.add(boutonAnnuler);
		toolBar.add(boutonRefaire);

		fen.setJMenuBar(menuBar);
		fen.add(toolBar);
		fen.getContentPane().setLayout(new BoxLayout(fen.getContentPane(),BoxLayout.Y_AXIS));
		fen.getContentPane().add(listScroller);
		fen.getContentPane().add(text);
		fen.getContentPane().add(boutonAjouter);
		fen.getContentPane().add(changeButton);

		// Glass pane
		MyGlassPane myGlassPane = new MyGlassPane(changeButton, menuBar, fen.getContentPane());
		changeButton.addItemListener(myGlassPane);
		fen.setGlassPane(myGlassPane);
		myGlassPane.setVisible(true);
		
		// Correspondance entre actions et gestes
		ActionMap actionMap = new ActionMap();
		actionMap.put("delete", actionSuppr);
		actionMap.put("triangle", actionAnnuler);
		actionMap.put("circle", actionRefaire);
		myGlassPane.setActionMap(actionMap);

		fen.pack();
		fen.setVisible(true);	
	}

	public static void main(String[] args) {
	    //Schedule a job for the event-dispatching thread:
	    //creating and showing this application's GUI.
	    javax.swing.SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
			new TestGUI();
		    }
		});
	}

	private void updateUndoRedoButton() {
		if (manager.canUndo()) actionAnnuler.setEnabled(true); else actionAnnuler.setEnabled(false);
		if (manager.canRedo()) actionRefaire.setEnabled(true); else actionRefaire.setEnabled(false);
		actionAnnuler.putValue(Action.SHORT_DESCRIPTION, manager.getUndoPresentationName());
		actionRefaire.putValue(Action.SHORT_DESCRIPTION, manager.getRedoPresentationName());
	}	

	private class ActionSupprimer extends AbstractAction {
		ActionSupprimer() {
			super();
			putValue(Action.NAME, "Supprimer");
		}

		public void actionPerformed(ActionEvent e) {
			int index = list.getSelectedIndex();
			if (index != -1) {
				manager.addEdit(new UndoableList(listModel, index));
				listModel.remove(index);
				updateUndoRedoButton();
			}
		}
	}	

	private class ActionAjouter extends AbstractAction {
		ActionAjouter() {
			super();
			putValue(Action.NAME, "Ajouter");

		}

		public void actionPerformed(ActionEvent e) {
			if (text.getText().length() > 0) {
				listModel.addElement(text.getText());
				manager.addEdit(new UndoableList(listModel, text.getText(), listModel.getSize()-1));
				text.setText("");
				updateUndoRedoButton();
			}
		}
	}	


	private class ActionAnnuler extends AbstractAction {
		ActionAnnuler() {
			super();
			putValue(Action.NAME, "Annuler");
			setEnabled(false);
		}

		public void actionPerformed(ActionEvent e) {
			manager.undo();
			updateUndoRedoButton();
		}
	}

	private class ActionRefaire extends AbstractAction {
		ActionRefaire() {
			super();
			putValue(Action.NAME, "Refaire");
			setEnabled(false);	
		}

		public void actionPerformed(ActionEvent e) {
			manager.redo();
			updateUndoRedoButton();
		}
	}		
}
