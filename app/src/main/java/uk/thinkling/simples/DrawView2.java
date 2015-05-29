package uk.thinkling.simples;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.Random;

/**
 * SIMPLES
 * Created by Ellison on 11/04/2015.
 */
public class DrawView2 extends View {


    /*VARIABLES*/

    Random rnd = new Random();
    MoveObj player1 = new MoveObj();
    MoveObj[] objs = new MoveObj[50];
    int screenW;
    int screenH;

    MainActivity parent;

    int highScore = 0;
    int score = 0;
    int timeSoFar = 0;

    /*VARIABLES*/

    // make a new Paint object and store it in myPaint
    Paint paint1 = new Paint();
    Paint paint2 = new Paint();
    Paint paint3 = new Paint();
    Paint paint4 = new Paint();

    // this is the constructor - it is called when an instance of this class is created
    public DrawView2(Context context, AttributeSet attrs) {
        super(context, attrs);

        parent = (MainActivity) this.getContext();

        // Use Color.parseColor to define HTML colors and set the color of the myPaint Paint object
        paint1.setColor(Color.parseColor("#000000"));
        paint2.setColor(Color.parseColor("#FFFF00"));
        paint3.setColor(Color.parseColor("#FFFFFF"));
        paint4.setColor(Color.parseColor("#000000"));
        paint4.setStyle(Paint.Style.STROKE);

        player1.x = 80;
        player1.y = 80;
        player1.xSpeed = 0;
        player1.ySpeed = 0;
        player1.radius = 40;
        player1.mass = 4 / 3 * 3.142 * 40 * 40 * 40;
        player1.type = 100;


        // initialise the objs array by creating a new MoveObj object for each entry in the array
        for (int bCount = 0; bCount < objs.length; bCount++) objs[bCount] = new MoveObj();

    }

    /* BIT FOR TOUCHING! */

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent e) {


        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.

        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (score>0){
                score--;
                player1.xSpeed = (int) (e.getX() - player1.x) / 10;
                player1.ySpeed = (int) (e.getY() - player1.y) / 10;
                }
        }

        return true;
    }


    @Override
    // this is the method called when the view needs to be redrawn
    protected void onDraw(Canvas canvas) {
        screenW = this.getWidth();
        screenH = this.getHeight();
        super.onDraw(canvas);


    //Timer: Every 10 Seconds Add 1 To Score
        timeSoFar++;

        if(timeSoFar% 250 == 0){

           score++;

           if(score > highScore){

               highScore = score;

           }

        }


        /*Score's Text*/

        parent.HighScoreText.setText("High Score: "+highScore);
        parent.ScoreText.setText("Score: "+ score);
        parent.TimeLeftText.setText("Seconds: "+Math.round(timeSoFar/25));

        // count for each entry in the objs array and then check for collision against all of the subsequent ones.

        for (int bCount1 = 0; bCount1 < objs.length; bCount1++) {
            for (int bCount2 = bCount1 + 1; bCount2 < objs.length; bCount2++) {
                collision(objs[bCount1], objs[bCount2]);
            }
        }

        // Once the collisions have been handled, move each objs, then draw it.
        // remember that the .move()  function has been programmed to detect wall collisions
        for (MoveObj obj : objs) {
            obj.move();
            obj.draw(canvas);
        }


        // Check Hero MoveObj For Collisions - TODO could avoid this duplicated code if we add hero to array.
        for (MoveObj obj : objs) if (collision(player1, obj) && (obj.type == 0)) score++;

        //move the player and then draw
        player1.move();
        player1.draw(canvas);



    }


    /* Makes MoveObj Class */

    // Gives Variable Name And Type
    public class MoveObj {

        int type;

        int x;
        int y;
        int xSpeed;  // if these are int, then the speed will gradually slow down due to rounding.
        int ySpeed;
        int radius;
        double mass;
        int angle; // rotational angle of the object
        int attack; // power used in collisions
        int defense; // defence used in collisions
        Paint paint = new Paint();

        // This is the default constructor - it sets the data for the variables to random values
        public MoveObj() {

            type = rnd.nextInt(10); // get a random integer from 0 to 9 for the 'type'

            if (type == 0) {
                paint=paint4;
                radius = 30;
            } else {
                paint.setARGB(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
                radius = rnd.nextInt(40) + 10;
            }

            x = rnd.nextInt(500) + radius;
            y = rnd.nextInt(800) + radius;
            xSpeed = rnd.nextInt(25) - 12;
            ySpeed = rnd.nextInt(25) - 12;
            mass = 4 / 3 * 3.142 * radius * radius * radius;
        }

        public void move() {

            if(type == 0 && rnd.nextInt(250)>248){
                xSpeed = rnd.nextInt(100) - 50;
                ySpeed = rnd.nextInt(100) - 50;
                Log.d("random fire", "on object");
            }

            // Makes object bounce on edges
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

            switch (type){

/*                case 0:
                    canvas.drawCircle(x, y, radius, paint4);
                    break;*/

                case 100:
                    canvas.drawCircle(player1.x, player1.y, player1.radius, paint1);
                    canvas.drawCircle(player1.x + player1.radius / 3, player1.y - player1.radius / 3, player1.radius / 4, paint3);
                    canvas.drawCircle(player1.x - player1.radius / 3, player1.y - player1.radius / 3, player1.radius / 4, paint3);
                    break;

                default:
                    canvas.drawCircle(x, y, radius, paint);

            }
        }
    }

    public boolean collision(MoveObj a, MoveObj b) {

        double mass_ratio, dvx2, norm, xSpeedDiff, ySpeedDiff, fy21, sign;

        float xDist = b.x - a.x;
        float yDist = b.y - a.y;
        float radiusSum = a.radius + b.radius;


        // if the balls are touching - ie. x-squared + y-squared is less that radius+radius-squared TODO - could do bounding-box check
        // needs to be more complex for non-round shapes
        if ((xDist * xDist + yDist * yDist) < (radiusSum * radiusSum)) {

            mass_ratio = b.mass / a.mass;
            xSpeedDiff = b.xSpeed - a.xSpeed;
            ySpeedDiff = b.ySpeed - a.ySpeed;

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
            b.xSpeed += dvx2;
            b.ySpeed += norm * dvx2;
            a.xSpeed -= mass_ratio * dvx2;
            a.ySpeed -= norm * mass_ratio * dvx2;

            //Log.d("Collide!", "The balls have a distance of" + dist);
            return true; // collision detected, so return true

        }
        //balls not touching so return false
        return false;

    }


}
