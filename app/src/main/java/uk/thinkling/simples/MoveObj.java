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
        if (Math.abs(xSpeed)<0.1f) xSpeed=0;
        if (Math.abs(ySpeed)<0.1f) ySpeed=0;

    }

    public void move(int screenW, int screenH, double time) {

        // move the object based on speed
        x += xSpeed*time;
        y += ySpeed*time;

        x=Math.max(radius, Math.min(x, screenW - radius));
        y=Math.max(radius,Math.min(y,screenH-radius));

    }
}




