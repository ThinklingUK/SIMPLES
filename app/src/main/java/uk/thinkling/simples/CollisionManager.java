package uk.thinkling.simples;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * SIMPLES
 * Created by ergo on 01/06/2015.
 */
public class CollisionManager {

    private int width;
    private int height;

    public List<CollisionRec> collisions = new ArrayList<>();


    public CollisionManager(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void collide(List<MoveObj> objs){
        List<CollisionRec> nextCollisions = new ArrayList<>();
        double dt = 0;
        double t;
        collisions.clear();

        while (dt < 1) {

            //find first collision and forward wind to that point.
            nextCollisions.clear();

            // count for each entry in the objs array and then check for collision against all of the subsequent ones.
            //ignore if time is more than 1 (wait for next cycle)
            for (int bCount1 = 0; bCount1 < objs.size(); bCount1++) {
                // check wall collisions
                t = wallCollisionTime(objs.get(bCount1));
                if (t < 1 && t >= 0) nextCollisions.add(new CollisionRec(t, objs.get(bCount1), null));

                for (int bCount2 = bCount1 + 1; bCount2 < objs.size(); bCount2++) {
                    t = objCollisionTime(objs.get(bCount1), objs.get(bCount2));
                    if (t < 1 && t >= 0)
                        nextCollisions.add(new CollisionRec(t, objs.get(bCount1), objs.get(bCount2)));
                }
            }


            Collections.sort(nextCollisions, new Comparator<CollisionRec>() {
                public int compare(CollisionRec a, CollisionRec b) {
                    return (a.time > b.time) ? -1 : 1;
                }
            });

            if (nextCollisions.isEmpty()) {
                t = 1 - dt;
            } else {
                t = nextCollisions.get(0).time;
            }

            // First, wind each object to time of first collision.
            for (MoveObj obj : objs) obj.move(width, height, t);

            // handle all the collisions starting at earliest time - if 2nd obj is null, then a wall collision
            for (CollisionRec coll : nextCollisions) {
                if (coll.time > t) break; // if non-consecutive collision, ignore all subsequent collisions
//                if (doCollision(coll.obja, coll.objb)) collisions.add(coll); // add this collision to list for analysis
                if (coll.doCollision()) collisions.add(coll); // add this collision to list for analysis
            }

            // update dt (becomes 1 if no collisions)
            dt += t;

        }
    }



    public class CollisionRec {
        double time;
        MoveObj obja;
        MoveObj objb;
        double impactV=0;


        private CollisionRec(double time, MoveObj obja, MoveObj objb) {
            this.time = time;
            this.obja = obja;
            this.objb = objb;
        }

        public boolean doCollision() {

            //could be a wall bounce
            if (objb == null){
                // Makes object bounce on edges by reversing velocity (if it was about to hit - this introduces errors at high speed.
                if (obja.x + obja.xSpeed <= obja.radius || obja.x + obja.xSpeed + obja.radius >= width) obja.xSpeed = -obja.xSpeed;
                if (obja.y + obja.ySpeed <= obja.radius || obja.y + obja.ySpeed + obja.radius >= height) obja.ySpeed = -obja.ySpeed;
                // store impact velocity for sound
                impactV = Math.sqrt(obja.xSpeed*obja.xSpeed+obja.ySpeed*obja.ySpeed);
                return true; //true if tracking wall bounces
            }

            double mass_ratio, dvx2, norm, fy21, sign;

            float dX = objb.x - obja.x;
            float dY = objb.y - obja.y;


            mass_ratio = objb.mass / obja.mass;
            double vX = objb.xSpeed - obja.xSpeed;
            double vY = objb.ySpeed - obja.ySpeed;

            impactV = Math.sqrt(vX*vX+vY*vY);

            if ((vX * dX + vY * dY) >= 0)
                return false;  //return false with unchanged speeds if balls are not approaching

            // to avoid a zero divide; (for single precision calculations, 1.0E-12 should be replaced by a larger value). **************
            fy21 = 1.0E-12 * Math.abs(dY);
            if (Math.abs(dX) < fy21) {
                sign = (dX < 0) ? -1 : 1;
                dX = (float) (fy21 * sign);
            }

            //     ***  update velocities ***
            norm = dY / dX;
            dvx2 = -2 * (vX + norm * vY) / ((1 + norm * norm) * (1 + mass_ratio));
            objb.xSpeed += dvx2;
            objb.ySpeed += norm * dvx2;
            obja.xSpeed -= mass_ratio * dvx2;
            obja.ySpeed -= norm * mass_ratio * dvx2;

//TODO - limit velocities so they're not too high (e.g. screen size/25)
            return true;

        }

    }

    /// Calculate the time of impact of an object with bounding walls.
    /// Returns: Positive Time if a collision will occur, else negative (ie. has already occurred or no collision.
    public double wallCollisionTime(MoveObj obj) {

        //detect earliest wall collision, ie. biggest dt (from y or X strike)
        double dt=-1;
        if (obj.x + obj.xSpeed < obj.radius) dt = Math.max(dt, (obj.x - obj.radius) / obj.xSpeed);
        else if (obj.x + obj.xSpeed + obj.radius > width) dt = Math.max(dt, (obj.x + obj.radius - width) / obj.xSpeed);

        if (obj.y + obj.ySpeed < obj.radius) dt = Math.max(dt, (obj.y - obj.radius) / obj.ySpeed);
        else if (obj.y + obj.ySpeed + obj.radius > height) dt = Math.max(dt, (obj.y + obj.radius - height) / obj.ySpeed);

        return dt;

    }

    /// Calculate the time of closest approach of two moving circles.  Also determine if the circles collide.
    /// Returns: Positive Time if a collision will occur, else negative (ie. has already occurred or no collision.
    private double objCollisionTime(MoveObj obj1, MoveObj obj2)
    {
        // vector Pab
        float dX = obj2.x - obj1.x;
        float dY = obj2.y - obj1.y;

        // vector Vab
        double vX = obj2.xSpeed - obj1.xSpeed;
        double vY = obj2.ySpeed - obj1.ySpeed;

        // the velocity from the relative velocity dot product
        double a = vX*vX+vY*vY;
        double b = 2 * (dX*vX+dY*vY);
        double c = (dX*dX+dY*dY) - (obj1.radius + obj2.radius) * (obj1.radius + obj2.radius);


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

    public boolean doCollision(MoveObj obj1, MoveObj obj2) {
        // NB: now moved into collisionrec
        //could be a wall bounce
        if (obj2 == null){
            // Makes object bounce on edges by reversing velocity (if it was about to hit - this introduces errors at high speed.
            if (obj1.x + obj1.xSpeed <= obj1.radius || obj1.x + obj1.xSpeed + obj1.radius >= width) obj1.xSpeed = -obj1.xSpeed;
            if (obj1.y + obj1.ySpeed <= obj1.radius || obj1.y + obj1.ySpeed + obj1.radius >= height) obj1.ySpeed = -obj1.ySpeed;
            return false; //currently not tracking wall bounces - may want to return true at some point
        }

        double mass_ratio, dvx2, norm, fy21, sign;

        float dX = obj2.x - obj1.x;
        float dY = obj2.y - obj1.y;


        mass_ratio = obj2.mass / obj1.mass;
        double vX = obj2.xSpeed - obj1.xSpeed;
        double vY = obj2.ySpeed - obj1.ySpeed;

        if ((vX * dX + vY * dY) >= 0)
            return false;  //return false with unchanged speeds if balls are not approaching

        // to avoid a zero divide; (for single precision calculations, 1.0E-12 should be replaced by a larger value). **************
        fy21 = 1.0E-12 * Math.abs(dY);
        if (Math.abs(dX) < fy21) {
            sign = (dX < 0) ? -1 : 1;
            dX = (float) (fy21 * sign);
        }

        //     ***  update velocities ***
        norm = dY / dX;
        dvx2 = -2 * (vX + norm * vY) / ((1 + norm * norm) * (1 + mass_ratio));
        obj2.xSpeed += dvx2;
        obj2.ySpeed += norm * dvx2;
        obj1.xSpeed -= mass_ratio * dvx2;
        obj1.ySpeed -= norm * mass_ratio * dvx2;

//TODO - limit velocities so they're not too high (e.g. screen size/25)
        return true;




/*       alternate method
        double dx = obj2.x - obj1.x, dy = obj2.y - obj1.y;
        double distance = Math.sqrt(dx*dx+dy*dy);


            // Unit vector in the direction of the collision
            double ax = dx / distance, ay = dy / distance;
            // Projection of the velocities in these axes
            double va1 = (obj1.xSpeed * ax + obj1.ySpeed * ay), vb1 = (-obj1.xSpeed * ay + obj1.ySpeed * ax);
            double va2 = (obj2.xSpeed * ax + obj2.ySpeed * ay), vb2 = (-obj2.xSpeed * ay + obj2.ySpeed * ax);
            // New velocities in these axes (after collision): ed<=1,  for elastic collision ed=1
            double ed=1;
            double vaP1 = va1 + (1 + ed) * (va2 - va1) / (1 + obj1.mass / obj2.mass);
            double vaP2 = va2 + (1 + ed) * (va1 - va2) / (1 + obj2.mass / obj1.mass);

            // Undo the projections
            obj1.xSpeed = vaP1 * ax - vb1 * ay;
            obj1.ySpeed = vaP1 * ay + vb1 * ax;// new vx,vy for ball 1 after collision
            obj2.xSpeed = vaP2 * ax - vb2 * ay;
            obj2.ySpeed = vaP2 * ay + vb2 * ax;// new vx,vy for ball 2 after collision*/

    }

}
