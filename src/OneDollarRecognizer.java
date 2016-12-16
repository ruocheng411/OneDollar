/**
 * @author <a href="mailto:gery.casiez@lifl.fr">Gery Casiez</a>
 */

import java.lang.Math;
import java.awt.Point;
import java.awt.Rectangle;

import java.util.Enumeration;
import java.util.Vector;

public class OneDollarRecognizer {
        private Vector<Tuple2> rawSrcPoints;
        private Vector<Tuple2> srcPointsToRecognize;
        private TemplateManager templateManager;
        private Vector<Template> theTemplates;
        private double Phi = 0.5 * (-1.0 + Math.sqrt(5.0));
        private double AngleRange = 45.0;
        private double AnglePrecision = 2.0;
        private final int numPoints = 64;
        private float squareSize = 250;
        private double HalfDiagonal = 0.5 * Math.sqrt(250.0 * 250.0 + 250.0 * 250.0);
        private double score = 0;
        private String nameTemplateFound = "none";

        public OneDollarRecognizer() {
                rawSrcPoints = new Vector<Tuple2>();
                srcPointsToRecognize = new Vector<Tuple2>();
                templateManager = new TemplateManager("bin/gestures.xml");
                theTemplates = templateManager.getTemplates();
                prepareTemplates();
        }

        protected void prepareTemplates() {
                Vector<Tuple2> points = new Vector<Tuple2>();
                for (int i = 0; i < theTemplates.size(); i++) {
                        points = theTemplates.get(i).getPoints();
                        points = resample(points, numPoints);
                        points = rotateToZero(points);
                        points = scaleToSquare(points, squareSize);
                        points = translateToOrigin(points);
                        theTemplates.get(i).setPoints(points);
                }
        }

        public void addPointToStroke(Tuple2 P) {
                rawSrcPoints.add(new Tuple2(P.x, P.y));
        }

        public Vector<Tuple2> getStoke() {
                return rawSrcPoints;
        }

        public void clearStroke() {
                rawSrcPoints.clear();
                score = 0;
                nameTemplateFound = "none";
        }

	public void preparePointsToRecognize(){		
		srcPointsToRecognize = resample(rawSrcPoints, numPoints);
        srcPointsToRecognize = rotateToZero(srcPointsToRecognize);
        srcPointsToRecognize = scaleToSquare(srcPointsToRecognize, squareSize);
        srcPointsToRecognize = translateToOrigin(srcPointsToRecognize);
	}
	
	public void recognize() {
		double b = Double.POSITIVE_INFINITY;
		preparePointsToRecognize();
		for (Template T : theTemplates) {
            double d = distanceAtBestAngle(srcPointsToRecognize, T, -AngleRange, AngleRange, AnglePrecision);
            if (d < b) {
                    b = d;
                    nameTemplateFound = T.getName();
                    //Template T_prime = T;
            }
	    }
	     score = 1 - b/HalfDiagonal;
		
	}
	
	 public Tuple2 getShapeCenter() {
         double sumX = 0;
         double sumY = 0;
         for (int i = 0; i < rawSrcPoints.size(); i++) {
                 sumX += rawSrcPoints.elementAt(i).x;
                 sumY += rawSrcPoints.elementAt(i).y;
         }
         return new Tuple2(sumX / rawSrcPoints.size(), sumY / rawSrcPoints.size());
 }

 public double getScore() {
         return score;
 }

 public String getNameTemplateFound() {
         return nameTemplateFound;
 }

 public void RotateRawPoints(int deltaAngle) {
         rawSrcPoints = rotateBy(rawSrcPoints, deltaAngle * Math.PI / 180);
 }

 protected Vector<Tuple2> resample(Vector<Tuple2> points, int n) {
         double I = pathLength(points) / (n - 1);
         double D = 0.0;

         Vector<Tuple2> srcPts = new Vector<Tuple2>();
         for (int i = 0; i < points.size(); i++) {
                 srcPts.add(new Tuple2(points.elementAt(i).x, points.elementAt(i).y));
         }

         Vector<Tuple2> dstPts = new Vector<Tuple2>(n);
         dstPts.addElement(new Tuple2(points.elementAt(0).x, points.elementAt(0).y));

         for (int i = 1; i < srcPts.size(); i++) {
                 Tuple2 pt1 = (Tuple2) srcPts.elementAt(i - 1);
                 Tuple2 pt2 = (Tuple2) srcPts.elementAt(i);

                 double d = Math.sqrt((pt2.x - pt1.x) * (pt2.x - pt1.x) + (pt2.y - pt1.y) * (pt2.y - pt1.y));
                 if ((D + d) >= I) {
                         double qx = pt1.x + ((I - D) / d) * (pt2.x - pt1.x);
                         double qy = pt1.y + ((I - D) / d) * (pt2.y - pt1.y);
                         Tuple2 q = new Tuple2(qx, qy);
                         dstPts.addElement(q);
                         srcPts.insertElementAt(q, i);
                         D = 0.0;
                 } else {
                         D += d;
                 }
         }

         // hack pour pbs d'arrondis
         if (dstPts.size() == n - 1) {
                 dstPts.addElement(srcPts.elementAt(srcPts.size() - 1));
         }

         Vector<Tuple2> dstPts2 = new Vector<Tuple2>();
         for (int i = 0; i < dstPts.size(); i++) {
                 dstPts2.add(new Tuple2(dstPts.elementAt(i).x, dstPts.elementAt(i).y));
         }

         return dstPts2;
 }


	protected Vector<Tuple2> rotateToZero(Vector<Tuple2> points) {

		//add the codes to write the methods 
		Tuple2 c = centroid(points);
		double theta = Math.atan2(c.y - points.get(0).y, c.x - points.get(0).x);
		Vector<Tuple2> newPoints = rotateBy(points, -theta);	
		return newPoints;
	}		

	protected Vector<Tuple2> rotateBy(Vector<Tuple2> points, double theta)
	{
		//add the codes 
		Vector<Tuple2> newPoints = new Vector<Tuple2>();
		Tuple2 c = centroid(points);
		for (Tuple2 p : points) { 
			double q_x = (p.x - c.x) * Math.cos(theta) - (p.y - c.y) * Math.sin(theta) + c.x;
            double q_y = (p.x - c.x) * Math.sin(theta) + (p.y - c.y) * Math.cos(theta) + c.y;
            Tuple2 q = new Tuple2(q_x, q_y);
            newPoints.add(q);
		}   
		return newPoints;
	}	

	  protected double distance(Tuple2 p1, Tuple2 p2) {
          double dx = p2.x - p1.x;
          double dy = p2.y - p1.y;
          return Math.sqrt(dx * dx + dy * dy);
  }

  protected double pathLength(Vector<Tuple2> points) {
          double length = 0;
          for (int i = 1; i < points.size(); i++) {
                  length += distance(points.elementAt(i - 1), points.elementAt(i));
          }
          return length;
  }

  // compute the centroid of the points given
  protected Tuple2 centroid(Vector<Tuple2> points) {
          double xsum = 0.0;
          double ysum = 0.0;

          Enumeration<Tuple2> e = points.elements();

          while (e.hasMoreElements()) {
                  Tuple2 p = (Tuple2) e.nextElement();
                  xsum += p.x;
                  ysum += p.y;
          }
          return new Tuple2(xsum / points.size(), ysum / points.size());
  }
  
  
	protected Vector<Tuple2> scaleToSquare(Vector<Tuple2> points, double size)
	{
		//add codes
		Rectangle B = boundingBox(points);
		Vector<Tuple2> newPoints= new Vector<Tuple2>();
		for(Tuple2 p:points){
			double q_x = p.x * (size / B.getWidth());
            double q_y = p.y * (size / B.getHeight());
            Tuple2 q = new Tuple2(q_x, q_y);
			newPoints.add(q);
		}
		return newPoints;
	}						

	protected Vector<Tuple2> translateToOrigin(Vector<Tuple2> points)
	{
		Tuple2 c = centroid(points);
		Vector<Tuple2> newPoints= new Vector<Tuple2>();
		for(Tuple2 p:points){
			double q_x = p.x - c.x;
            double q_y = p.y - c.y;
            Tuple2 q = new Tuple2(q_x, q_y);
			newPoints.add(q);
		}
		return newPoints;
	}			

	protected double distanceAtBestAngle(Vector<Tuple2> points, Template T, double a, double b, double threshold)
	{
		double x1 = Phi * a + (1 - Phi) * b;
		double f1 = distanceAtAngle(points, T, x1);
		double x2 = (1 - Phi) * a + Phi * b;
		double f2 = distanceAtAngle(points, T, x2);
		while (Math.abs(b-a)>threshold) {
			if(f1 < f2){
				b = x2;
				x2 = x1;
				f2 = f1;
				x1 = Phi * a + (1 - Phi) * b;
				f1 = distanceAtAngle(points, T, x1);
			}else{
				a = x1;
				x1 = x2;
				f1 = f2;
				x2 = (1 - Phi) * a + Phi * b;
				f2 = distanceAtAngle(points, T, x2);
			}
			
		}
		return Math.min(f1, f2);
	}			

	protected double distanceAtAngle(Vector<Tuple2> points, Template T, double theta)
	{
		Vector<Tuple2> newpoints = rotateBy(points, theta);
		return pathDistance(newpoints, T.getPoints());
	}		

	protected double pathDistance(Vector<Tuple2> path1, Vector<Tuple2> path2)
	{            
		double distance = 0;
		for (int i = 0; i < path1.size(); i++)
		{
			distance += distance((Tuple2) path1.elementAt(i), (Tuple2) path2.elementAt(i));
		}
		return distance / path1.size();
	}	

	//	#region Lengths and Rects	

	protected Rectangle boundingBox(Vector<Tuple2> points)
	{
		double minX = Double.MAX_VALUE;
		double maxX = Double.MIN_VALUE;
		double minY = Double.MAX_VALUE;
		double maxY = Double.MIN_VALUE;

		Enumeration<Tuple2> e = points.elements();

		while (e.hasMoreElements())
		{
			Tuple2 p = (Tuple2)e.nextElement();

			if (p.x < minX)
				minX = p.x;
			if (p.x > maxX)
				maxX = p.x;

			if (p.y < minY)
				minY = p.y;
			if (p.y > maxY)
				maxY = p.y;
		}

		return new Rectangle((int)minX, (int)minY, (int)(maxX - minX), (int)(maxY - minY));
	}

	public Vector<Tuple2> getRawSrcPoints() {
		return rawSrcPoints;
	}
}
