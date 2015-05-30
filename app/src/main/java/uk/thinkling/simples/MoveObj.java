package uk.thinkling.simples;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

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
    float xSpeed;  // if these are int, then the speed will gradually slow down due to rounding.
    float ySpeed;
    float adjSpeed; // how much speed is conserved (0 is 100% friction), above 1 will accelerate
    int radius;
    double mass;
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

            case 100:
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
        adjSpeed = 0.99f;
        mass = 4 / 3 * 3.142 * radius * radius;
    }

    // This is the default constructor - it sets the type to random values
    public MoveObj(int screenW, int screenH) {
        this(rnd.nextInt(10), screenW, screenH); // get a random integer from 0 to 9 for the 'type'
    }


    public void move(int screenW, int screenH) {

        if (type == 0 && rnd.nextInt(250) > 248) {
            xSpeed = rnd.nextInt(100) - 50;
            ySpeed = rnd.nextInt(100) - 50;
            Log.d("random fire", "on object");
        }

        //slow down due to friction
        xSpeed *= adjSpeed;
        ySpeed *= adjSpeed;

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

    public boolean collision(MoveObj obj) {

        double mass_ratio, dvx2, norm, xSpeedDiff, ySpeedDiff, fy21, sign;

        float xDist = obj.x - this.x;
        float yDist = obj.y - this.y;
        float radiusSum = this.radius + obj.radius;


        // if the balls are touching - ie. x-squared + y-squared is less that radius+radius-squared TODO - could do bounding-box check
        // we currently detect an actual collision. TODO - Should determine if they will collide, and then adjust for that.
        // needs to be more complex for non-round shapes
        if ((xDist * xDist + yDist * yDist) < (radiusSum * radiusSum)) {

            mass_ratio = obj.mass / this.mass;
            xSpeedDiff = obj.xSpeed - this.xSpeed;
            ySpeedDiff = obj.ySpeed - this.ySpeed;

            if ((xSpeedDiff * xDist + ySpeedDiff * yDist) >= 0)
                return false;  //return old velocities if balls are not approaching
            // to avoid a zero divide; (for single precision calculations, 1.0E-12 should be replaced by a larger value). **************
            fy21 = 1.0E-12 * Math.abs(yDist);
            if (Math.abs(xDist) < fy21) {
                sign = (xDist < 0) ? -1 : 1;
                xDist = (float) (fy21 * sign);
            }

            //     ***  update velocities ***
            norm = yDist / xDist;
            dvx2 = -2 * (xSpeedDiff + norm * ySpeedDiff) / ((1 + norm * norm) * (1 + mass_ratio));
            obj.xSpeed += dvx2;
            obj.ySpeed += norm * dvx2;
            this.xSpeed -= mass_ratio * dvx2;
            this.ySpeed -= norm * mass_ratio * dvx2;

            //Log.d("Collide!", "The balls have a distance of" + dist);
            return true; // collision detected, so return true

        }
        //balls not touching so return false
        return false;

    }

}
