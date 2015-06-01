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
    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    static Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);

    static Random rnd = new Random();

    public MoveObj(int type, int radius, float x, float y, double xSpeed, double ySpeed) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.xSpeed = xSpeed;
        this.ySpeed = ySpeed;
        this.radius = radius;
        this.mass= 4 / 3 * 3.142 * radius * radius;;

        switch (type) {
            case 0:
                paint.setColor(Color.parseColor("#FFFFFF"));
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(5f);
                break;

            case 11:  //red ball
                paint.setColor(Color.parseColor("#c0c0c0"));
                break;

            case 12: //blue ball
                paint.setColor(Color.parseColor("#c5b358"));
                break;

            case 100: //hero ball
                paint.setColor(Color.parseColor("#000000"));
                stroke.setColor(Color.parseColor("#FFFFFF"));
                stroke.setStyle(Paint.Style.STROKE);
                stroke.setStrokeWidth(2f);
                break;

            default:
                paint.setARGB(255, rnd.nextInt(200)+55, rnd.nextInt(200)+55, rnd.nextInt(200)+55);
        }

        paint.setAntiAlias(true);

    }

    // Based on radius, sets random position and speed
    public MoveObj(int type, int radius, int screenW, int screenH) {
        this(type, radius, rnd.nextInt(screenW - radius * 2) + radius, rnd.nextInt(screenH - radius * 2) + radius, rnd.nextInt(25) - 12, rnd.nextInt(25) - 12); // get a random integer from 0 to 9 for the 'type'
    }

    // This is the type specific constructor - it sets radius to random values
    public MoveObj(int type, int screenW, int screenH) {
        this(type, type==0?40:rnd.nextInt(40) + 10, screenW, screenH); // get a random integer from 0 to 9 for the 'type'
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
                canvas.drawCircle(x, y, radius, stroke);
                canvas.drawCircle(x + radius / 3, y - radius / 3, radius / 4, stroke);
                canvas.drawCircle(x - radius / 3, y - radius / 3, radius / 4, stroke);
                break;

            default:
                canvas.drawCircle(x, y, radius, paint);
        }
    }

    public void applyFriction(double factor) {

        // slow the object based on factor
        xSpeed*=factor;
        ySpeed*=factor;
        if (Math.abs(xSpeed)<0.01f) xSpeed=0;
        if (Math.abs(ySpeed)<0.01f) ySpeed=0;

    }

    public void move(int screenW, int screenH, double time) {

        // move the object based on speed
        x += xSpeed*time;
        y += ySpeed*time;

        x=Math.max(radius, Math.min(x, screenW - radius));
        y=Math.max(radius,Math.min(y,screenH-radius));

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
        if (x + xSpeed <= radius || x + xSpeed + radius >= screenW) xSpeed = -xSpeed;
        if (y + ySpeed <= radius || y + ySpeed + radius >= screenH) ySpeed = -ySpeed;
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

    /// Returns:
    /// collision - Returns Positive Time if a collision will occur, else negative (ie. has already occurred or no collision.

    public double TimeOfClosestApproach(MoveObj obj)
    {
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

        if (discriminant < 0) return -1;

        // Case 2 and 3:
        // If the discriminant is zero, then there is exactly one real root, meaning that the circles just grazed each other.  If the
        // discriminant is positive, then there are two real roots, meaning that the circles penetrate each other.  In that case, the
        // smallest of the two roots is the initial time of impact.  We handle these two cases identically.
        double t0 = (-b + Math.sqrt(discriminant)) / (2 * a);
        double t1 = (-b - Math.sqrt(discriminant)) / (2 * a);

        // We also have to check if the time to impact is negative.  If it is negative, then that means that the collision
        // occurred in the past.  Since we're only concerned about future events, we say that no collision occurs if t < 0.
        return Math.min(t0, t1);
    }
}




