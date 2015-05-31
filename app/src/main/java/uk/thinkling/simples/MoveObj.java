package uk.thinkling.simples;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.Random;

/**
 * SIMPLES
 * Created by ergo on 29/05/2015.
 */ /* Makes MoveObj Class */
// Gives Variable Name And Type

public class MoveObj {
    int type;
    float x;
    float y;
    double xSpeed;  // if these are int, then the speed will gradually slow down due to rounding.
    double ySpeed;
    int radius;
    double mass;
    int state=1;
    float angle; // rotational angle of the object
    int attack; // power used in collisions
    int defense; // defence used in collisions
    Paint paint = new Paint();
    static Random rnd = new Random();
    // Use Color.parseColor to define HTML colors and set the color of the myPaint Paint object
    static Paint eyepaint = new Paint();


    // This is the type specific constructor - it sets the data for the variables to random values
    public MoveObj(int type, int screenW, int screenH) {

        //TODO should set radius multiple based on screen size

        this.type = type;
        switch (type) {
            case 0:
                radius = 30;
                paint.setColor(Color.parseColor("#000000"));
                paint.setStyle(Paint.Style.STROKE);
                break;

            case 11:  //red ball
                radius = 40;
                paint.setColor(Color.parseColor("#FF0000"));
                break;

            case 12: //blue ball
                radius = 40;
                paint.setColor(Color.parseColor("#0000FF"));
                break;

            case 100: //hero ball
                radius = 40;
                paint.setColor(Color.parseColor("#000000"));
                eyepaint.setColor(Color.parseColor("#FFFFFF"));
                break;

            default:
                radius = rnd.nextInt(40) + 10;
                paint.setARGB(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
        }

        x = rnd.nextInt(screenW - radius * 2) + radius;
        y = rnd.nextInt(screenH - radius * 2) + radius;
        xSpeed = rnd.nextInt(25) - 12;
        ySpeed = rnd.nextInt(25) - 12;
        mass = 4 / 3 * 3.142 * radius * radius;
    }

    // This is the default constructor - it sets the type to random values
    public MoveObj(int screenW, int screenH) {
        this(rnd.nextInt(10), screenW, screenH); // get a random integer from 0 to 9 for the 'type'
    }


    public String toString() {
        return "MoveObj{" +
                "type=" + type +
                ", x=" + x +
                ", y=" + y +
                ", xSpeed=" + xSpeed +
                ", ySpeed=" + ySpeed +
                ", radius=" + radius +
                '}';
    }

    public void draw(Canvas canvas) {

        switch (type) {

/*                case 0:
                canvas.drawCircle(x, y, radius, paint);
                break;*/

            case 100:
                canvas.drawCircle(x, y, radius, paint);
                canvas.drawCircle(x + radius / 3, y - radius / 3, radius / 4, eyepaint);
                canvas.drawCircle(x - radius / 3, y - radius / 3, radius / 4, eyepaint);
                break;

            default:
                canvas.drawCircle(x, y, radius, paint);

        }
    }

    public void applyFriction(double factor) {

        // slow the object based on factor
        xSpeed*=factor;
        ySpeed*=factor;
    }

    public void move(int screenW, int screenH, double time) {

        // move the object based on speed
        x += xSpeed*time;
        y += ySpeed*time;

        x=Math.max(radius, Math.min(x, screenW - radius));
        y=Math.max(radius,Math.min(y,screenH-radius));

    }

    public void move(int screenW, int screenH, float speedAdj) {

        //slow down due to friction
        xSpeed *= speedAdj;
        ySpeed *= speedAdj;

        // Makes object bounce on edges (if it was about to hit - this introduces errors at high speed.
        if (x + xSpeed < radius || x + xSpeed + radius > screenW) {
            xSpeed = -xSpeed;
        }
        if (y + ySpeed < radius || y + ySpeed + radius > screenH) {
            ySpeed = -ySpeed;
        }

        // move the object based on speed
        x += xSpeed;
        y += ySpeed;

    }

    public double wallCollisionTime(int screenW, int screenH) {

        //detect earliest wall collision, ie. biggest dt (from y or X strike)
        double dt=-1;
        if (x + xSpeed < radius) dt = Math.max(dt, (x - radius) / xSpeed);
        else if (x + xSpeed + radius > screenW) dt = Math.max(dt, (x + radius - screenW) / xSpeed);
        if (y + ySpeed < radius) dt = Math.max(dt, (y - radius) / ySpeed);
        else if (y + ySpeed + radius > screenH) dt = Math.max(dt, (y + radius - screenH) / ySpeed);

        return dt;

    }

    public void wallCollision(int screenW, int screenH) {

        // Makes object bounce on edges (if it was about to hit - this introduces errors at high speed.
        if (x + xSpeed <= radius || x + xSpeed + radius >= screenW) {
            xSpeed = -xSpeed;
        }

        if (y + ySpeed <= radius || y + ySpeed + radius >= screenH) {
            ySpeed = -ySpeed;
        }
    }

    public double objCollisionTime(MoveObj obj) {

        // double mass_ratio, dvx2, norm, xSpeedDiff, ySpeedDiff, fy21, sign;

        double dX = obj.x - this.x;
        double dY = obj.y - this.y;
        double d=Math.sqrt(dX*dX+dY*dY);

        double radiusSum = this.radius + obj.radius;


        if (d > radiusSum ) return -1;  // this check should look at the intersecting vectors to avoid pass thru.
        // see http://hamaluik.com/posts/swept-aabb-collision-detection-using-the-minkowski-difference/
        // or http://garethrees.org/2009/02/17/physics/


        // First calculate the component of velocity of each object
        //NB: these values could be very small
        double vp1= this.xSpeed*dX/d+this.ySpeed*dY/d;
        double vp2= obj.xSpeed*dX/d+obj.ySpeed*dY/d;
        double dt= (radiusSum-d)/(vp1-vp2);

        return dt;
        // Collision should have happened dt before you have detected r1+r2 and dt =(r1+r2-d)/(vp1-vp2);
        // the collision should have occurred at t-dt (Actually this is also an approximation).

    }


    public void objCollision(MoveObj obj) {


        double mass_ratio, dvx2, norm, xSpeedDiff, ySpeedDiff, fy21, sign;

        float dX = obj.x - this.x;
        float dY = obj.y - this.y;


        mass_ratio = obj.mass / this.mass;
        xSpeedDiff = obj.xSpeed - this.xSpeed;
        ySpeedDiff = obj.ySpeed - this.ySpeed;

        if ((xSpeedDiff * dX + ySpeedDiff * dY) >= 0)
            return;  //return old velocities if balls are not approaching

        // to avoid a zero divide; (for single precision calculations, 1.0E-12 should be replaced by a larger value). **************
        fy21 = 1.0E-12 * Math.abs(dY);
        if (Math.abs(dX) < fy21) {
            sign = (dX < 0) ? -1 : 1;
            dX = (float) (fy21 * sign);
        }

        //     ***  update velocities ***
        norm = dY / dX;
        dvx2 = -2 * (xSpeedDiff + norm * ySpeedDiff) / ((1 + norm * norm) * (1 + mass_ratio));
        obj.xSpeed += dvx2;
        obj.ySpeed += norm * dvx2;
        this.xSpeed -= mass_ratio * dvx2;
        this.ySpeed -= norm * mass_ratio * dvx2;

//TODO - limit velocities so they're not too high (e.g. screen size/25)




/*        double dx = obj.x - this.x, dy = obj.y - this.y;
        double distance = Math.sqrt(dx*dx+dy*dy);


            // Unit vector in the direction of the collision
            double ax = dx / distance, ay = dy / distance;
            // Projection of the velocities in these axes
            double va1 = (this.xSpeed * ax + this.ySpeed * ay), vb1 = (-this.xSpeed * ay + this.ySpeed * ax);
            double va2 = (obj.xSpeed * ax + obj.ySpeed * ay), vb2 = (-obj.xSpeed * ay + obj.ySpeed * ax);
            // New velocities in these axes (after collision): ed<=1,  for elastic collision ed=1
            double ed=1;
            double vaP1 = va1 + (1 + ed) * (va2 - va1) / (1 + this.mass / obj.mass);
            double vaP2 = va2 + (1 + ed) * (va1 - va2) / (1 + obj.mass / this.mass);

            // Undo the projections
            this.xSpeed = vaP1 * ax - vb1 * ay;
            this.ySpeed = vaP1 * ay + vb1 * ax;// new vx,vy for ball 1 after collision
            obj.xSpeed = vaP2 * ax - vb2 * ay;
            obj.ySpeed = vaP2 * ay + vb2 * ax;// new vx,vy for ball 2 after collision*/

    }

    public boolean objCollisionRewind(MoveObj obj) {

        // double mass_ratio, dvx2, norm, xSpeedDiff, ySpeedDiff, fy21, sign;

        double dX = obj.x - this.x;
        double dY = obj.y - this.y;
        double d=Math.sqrt(dX*dX+dY*dY);

        double radiusSum = this.radius + obj.radius;

        if (d <= radiusSum ) {

            // First calculate the component of velocity of each object
            double vp1= this.xSpeed*dX/d+this.ySpeed*dY/d;
            double vp2= obj.xSpeed*dX/d+obj.ySpeed*dY/d;

            // Collision should have happened dt before you have detected r1+r2 and dt =(r1+r2-d)/(vp1-vp2);
            double dt =(radiusSum-d)/(vp1-vp2); // the collision should have occurred at t-dt (Actually this is also an approximation).

            //So rewind the movement by dt to position objects at point of impact
            this.x -= this.xSpeed*dt;
            this.y -= this.ySpeed*dt;
            obj.x -= obj.xSpeed*dt;
            obj.y -= obj.ySpeed*dt;

            // Now the distance between center of the two balls is d'=r1+r2;
            double dx = obj.x - this.x, dy = obj.y - this.y;
            double distance = Math.sqrt(dx*dx+dy*dy);

            // Unit vector in the direction of the collision
            double ax = dx / distance, ay = dy / distance;
            // Projection of the velocities in these axes
            double va1 = (this.xSpeed * ax + this.ySpeed * ay), vb1 = (-this.xSpeed * ay + this.ySpeed * ax);
            double va2 = (obj.xSpeed * ax + obj.ySpeed * ay), vb2 = (-obj.xSpeed * ay + obj.ySpeed * ax);
            // New velocities in these axes (after collision): ed<=1,  for elastic collision ed=1
            double ed=1;
            double vaP1 = va1 + (1 + ed) * (va2 - va1) / (1 + this.mass / obj.mass);
            double vaP2 = va2 + (1 + ed) * (va1 - va2) / (1 + obj.mass / this.mass);

            // Undo the projections
            this.xSpeed = vaP1 * ax - vb1 * ay;
            this.ySpeed = vaP1 * ay + vb1 * ax;// new vx,vy for ball 1 after collision
            obj.xSpeed = vaP2 * ax - vb2 * ay;
            obj.ySpeed = vaP2 * ay + vb2 * ax;// new vx,vy for ball 2 after collision

            //Because we have move time backward dt, we need to move time forward dt.
            this.x += this.xSpeed*dt;
            this.y += this.ySpeed*dt;
            obj.x += obj.xSpeed*dt;
            obj.y += obj.ySpeed*dt;


            return true;
        }

        //balls not touching so return false
        return false;
    }



    /// Calculate the time of closest approach of two moving circles.  Also determine if the circles collide.
    ///
    /// Input:
    /// Pa - Position of circle A.
    /// Pb - Position of circle B.
    /// Va - Velocity of circle A.
    /// Vb - Velocity of circle B.
    /// Ra - Radius of circle A.
    /// Rb - Radius of circle B.
    ///
    /// Returns:
    /// collision - Returns True if a collision occured, else False.
    /// The method returns the time to impact if collision=true, else it returns the time of closest approach.
    ///
    /// Notes:
    /// This algorithm will work in any dimension.  Simply change the Vector2's to Vector3's to make this work
    /// for spheres.  You can also set the radii to 0 to work with points/rays.
    ///

    public double TimeOfClosestApproach(MoveObj obj)
    {
/*
        Vector2 Pab = Pa - Pb;
        Vector2 Vab = Va - Vb;
        float a = Vector2.Dot(Vab, Vab);
        float b = 2 * Vector2.Dot(Pab, Vab);
        float c = Vector2.Dot(Pab, Pab) - (Ra + Rb) * (Ra + Rb);
                //NB: 2D dot product = x1*x2 + y1*y2

        */

        // vector Pab
        float dX = obj.x - this.x;
        float dY = obj.y - this.y;

        // vector Vab
        double vX = obj.xSpeed - this.xSpeed;
        double vY = obj.ySpeed - this.ySpeed;

        // the velocity from the relative velocity dot product
        double a = vX*vX+vY*vY;
        double b = 2 * (dX*vX+dY*vY);
        double c = (dX*dX+dY*dY) - (this.radius + obj.radius) * (this.radius + obj.radius);


        // The quadratic discriminant.
        double discriminant = b * b - 4 * a * c;

        // Case 1:
        // If the discriminant is negative, then there are no real roots, so there is no collision.  The time of
        // closest approach is then given by the average of the imaginary roots, which is:  t = -b / 2a
        double t;
        if (discriminant < 0)
        {
            return -1;
        }
        else
        {
            // Case 2 and 3:
            // If the discriminant is zero, then there is exactly one real root, meaning that the circles just grazed each other.  If the
            // discriminant is positive, then there are two real roots, meaning that the circles penetrate each other.  In that case, the
            // smallest of the two roots is the initial time of impact.  We handle these two cases identically.
            double t0 = (-b + Math.sqrt(discriminant)) / (2 * a);
            double t1 = (-b - Math.sqrt(discriminant)) / (2 * a);
            t = Math.min(t0, t1);

            // We also have to check if the time to impact is negative.  If it is negative, then that means that the collision
            // occurred in the past.  Since we're only concerned about future events, we say that no collision occurs if t < 0.
        }

        // Finally, if the time is negative, then set it to zero, because, again, we want this function to respond only to future events.


        return t;
    }
}




