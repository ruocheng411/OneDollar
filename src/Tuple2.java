
public class Tuple2 {
	public double x,y;
	
	public Tuple2() {
		x=0;y=0;
	}

	public Tuple2(double xx,double yy) {
		x=xx;y=yy;
	}
	
	public Tuple2(Tuple2 a) {
		x=a.x;y=a.y;
	}
	
	
	public Tuple2 add(Tuple2 a) {
		x+=a.x;
		y+=a.y;
		return this;
	}

  public Tuple2 sub(Tuple2 a) {
  	x-=a.x;
   	y-=a.y;
   	return this;
  }
  
  public double norm() {
	  return Math.sqrt(x*x+y*y);
  }

  public void normalize() {
	  double n = norm();
	  x*=1.0/n;
	  y*=1.0/n;
  }
  
  public Tuple2 add(Tuple2 a,Tuple2 b) {
   	x=a.x+b.x;
   	y=a.y+b.y;
   	return this;
   }
    
   public void set(Tuple2 a) {
   	x=a.x;y=a.y;
   }
    
	public void set(Tuple2 a,Tuple2 b) {
		set(b);
		sub(a);
	}

	/// copy
    public void copy(Tuple2 a) {
    	a.x=x;a.y=y;
    }
    

}
