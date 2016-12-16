/**
 * @author <a href="mailto:gery.casiez@lifl.fr">Gery Casiez</a>
 */

// adapte de http://java.sun.com/docs/books/tutorial/uiswing/examples/components/GlassPaneDemoProject/src/components/GlassPaneDemo.java

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JComponent;
import javax.swing.JMenuBar;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

/**
 * We have to provide our own glass pane so that it can paint.
 */
public class MyGlassPane extends JComponent
implements ItemListener {
	private OneDollarRecognizer recognizer;
	private boolean paintStroke = true;

	//React to change button clicks.
	public void itemStateChanged(ItemEvent e) {
		paintStroke = e.getStateChange() == ItemEvent.SELECTED;
	}

	protected void paintComponent(Graphics g) {
		if (paintStroke) {
			Vector<Tuple2> srcPoints = recognizer.getStoke();
			if (!srcPoints.isEmpty()) {
				g.setColor(Color.red);
				for (int i = 1; i < srcPoints.size(); i++) {
					g.drawLine((int)srcPoints.elementAt(i-1).x, (int)srcPoints.elementAt(i-1).y,
							(int)srcPoints.elementAt(i).x, (int)srcPoints.elementAt(i).y);
				}
			}
		}
	}

	protected void executeAction(String actionName) {
		ActionMap am = getActionMap();
		Action action = am.get(actionName);
		if ((action != null) && (action.isEnabled())) action.actionPerformed(null);
	}

	public MyGlassPane(AbstractButton aButton,
			JMenuBar menuBar,
			Container contentPane) {
		recognizer = new OneDollarRecognizer();
		CBListener listener = new CBListener(aButton, menuBar,
				this, contentPane, recognizer);
		addMouseListener(listener);
		addMouseMotionListener(listener);
	}
}



/**
 * Listen for all events that our check box is likely to be
 * interested in.  Redispatch them to the check box.
 */
class CBListener extends MouseInputAdapter {
	Toolkit toolkit;
	Component liveButton;
	JMenuBar menuBar;
	MyGlassPane glassPane;
	Container contentPane;
	OneDollarRecognizer recognizer;

	public CBListener(Component liveButton, JMenuBar menuBar,
			MyGlassPane glassPane, Container contentPane, OneDollarRecognizer recognizer) {
		toolkit = Toolkit.getDefaultToolkit();
		this.liveButton = liveButton;
		this.menuBar = menuBar;
		this.glassPane = glassPane;
		this.contentPane = contentPane;
		this.recognizer = recognizer;
	}

	public void mouseMoved(MouseEvent e) {
		redispatchMouseEvent(e, false);
	}

	public void mouseDragged(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON3) {
			recognizer.addPointToStroke(new Tuple2(e.getPoint().x, e.getPoint().y));
			redispatchMouseEvent(e, true);
		}
		redispatchMouseEvent(e, false);
	}

	public void mouseClicked(MouseEvent e) {
		redispatchMouseEvent(e, false);
	}

	public void mouseEntered(MouseEvent e) {
		redispatchMouseEvent(e, false);
	}

	public void mouseExited(MouseEvent e) {
		redispatchMouseEvent(e, false);
	}

	public void mousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON3) {
			recognizer.clearStroke();
			redispatchMouseEvent(e, true);
		}
		redispatchMouseEvent(e, false);
	}

	public void mouseReleased(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON3) {
			recognizer.recognize();
			((MyGlassPane)(e.getSource())).executeAction(recognizer.getNameTemplateFound());
			System.out.println(recognizer.getNameTemplateFound() + " " + recognizer.getScore());
			redispatchMouseEvent(e, true);
		}
		redispatchMouseEvent(e, false);   	
	}

	//A basic implementation of redispatching events.
	private void redispatchMouseEvent(MouseEvent e,
			boolean repaint) {
		Point glassPanePoint = e.getPoint();
		Container container = contentPane;
		Point containerPoint = SwingUtilities.convertPoint(
				glassPane,
				glassPanePoint,
				contentPane);
		if (containerPoint.y < 0) { //we're not in the content pane
			if (containerPoint.y + menuBar.getHeight() >= 0) { 
				//The mouse event is over the menu bar.
				//Could handle specially.
			} else { 
				//The mouse event is over non-system window 
				//decorations, such as the ones provided by
				//the Java look and feel.
				//Could handle specially.
			}
		} else {
			//The mouse event is probably over the content pane.
			//Find out exactly which component it's over.  
			Component component = 
				SwingUtilities.getDeepestComponentAt(
						container,
						containerPoint.x,
						containerPoint.y);

			if (component != null) {
				//Forward events over the check box.
				Point componentPoint = SwingUtilities.convertPoint(
						glassPane,
						glassPanePoint,
						component);
				component.dispatchEvent(new MouseEvent(component,
						e.getID(),
						e.getWhen(),
						e.getModifiers(),
						componentPoint.x,
						componentPoint.y,
						e.getClickCount(),
						e.isPopupTrigger()));
			}
		}

		//Update the glass pane if requested.
		if (repaint) {
			glassPane.repaint();
		}
	}
}
