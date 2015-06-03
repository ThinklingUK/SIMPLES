package uk.thinkling.simples;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.util.*;

/**
 * SIMPLES
 * Created by Ellison on 11/04/2015.
 */
public class DrawView3 extends View {


    /*VARIABLES*/

    MoveObj player1;
    List<MoveObj> objs = new ArrayList<>();
    CollisionManager collider;

    int screenW;
    int screenH;
    int bedH;
    int coinR;
    int currObj = 0;

    MainActivity parent;
    private final GestureDetectorCompat gdc;

    int highScore = 0;
    int score = 0;
    int timeSoFar = 0;

    static Paint linepaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    static Paint outlinepaint = new Paint(Paint.ANTI_ALIAS_FLAG);


    /*VARIABLES*/


    // this is the constructor - it is called when an instance of this class is created
    public DrawView3(Context context, AttributeSet attrs) {
        super(context, attrs);
        parent = (MainActivity) this.getContext();
        gdc = new GestureDetectorCompat(parent, new MyGestureListener()); // create the gesture detector
        linepaint.setColor(Color.parseColor("#CD7F32"));
        linepaint.setStyle(Paint.Style.STROKE);
        linepaint.setStrokeWidth(3f);
        outlinepaint.setColor(Color.parseColor("#FFFFFF"));
        outlinepaint.setStyle(Paint.Style.STROKE);
        outlinepaint.setStrokeWidth(3f);
    }


    // this happens if the screen size changes - including the first timeit is measured. Here is where we get width and height
    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        Log.d("onMeasure", w + " " + h);
        screenW = w;
        screenH = h;
        bedH=h/15;
        coinR=bedH/3;

        collider = new CollisionManager(w, h);

        // create the first ball
        player1 = new MoveObj(11 + currObj % 2, coinR, screenW / 2, screenH - bedH, 5, 0);
        objs.add(player1);
    }

    /* BIT FOR TOUCHING! */
    @Override
    public boolean onTouchEvent(@NonNull final MotionEvent e) {
        return gdc.onTouchEvent(e);
    }

    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final String DEBUG_TAG = "Gestures";
        private final int SWIPE_MIN_DISTANCE = 120;
        private final int SWIPE_THRESHOLD_VELOCITY = 200;

        @Override
        public boolean onDown(MotionEvent e) {
            Log.d(DEBUG_TAG, "onDown: " + e.toString());
            if (player1.state == 1) {
                player1.xSpeed = player1.ySpeed = 0;
                player1.x = e.getX();
                player1.y = e.getY();
            }
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            // effectively a Drag detector - although would need to check that we have hit a ball first.
            Log.d(DEBUG_TAG, "onScroll: " + e2.toString());
            if (player1.state == 1) {
                player1.xSpeed = player1.ySpeed = 0;
                player1.x = e2.getX();
                player1.y = e2.getY();
            }
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            Log.d(DEBUG_TAG, "onFling: " + e1.toString() + e2.toString());
            if (player1.state == 1) {
                player1.xSpeed = velocityX / 25;
                player1.ySpeed = velocityY / 25;
                player1.state = 0;
            }
            return true;
        }

    }


    @Override
    // this is the method called when the view needs to be redrawn
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        //Timer: Every 10 Seconds Add 1 To Score
 /*       timeSoFar++;
        if(timeSoFar% 250 == 0){
           score++;
           if(score > highScore)    highScore = score;
        }*/

        /*Score's Text*/
/*        parent.HighScoreText.setText("High Score: "+highScore);
        parent.ScoreText.setText("Score: "+ score);
        parent.TimeLeftText.setText("Seconds: "+Math.round(timeSoFar/25));*/

        // handle all the collisions starting at earliest time - if 2nd obj is null, then a wall collision

        collider.collide(objs);
        for (CollisionManager.CollisionRec coll : collider.collisions) {
//            if (coll.objb == null) continue; //ignore a wall collision
//            if (coll.obja.type == 0) coll.objb.state = 0;
//            else if (coll.objb.type == 0) coll.obja.state = 0;
        }

        for (int f = 0; f < 10; f++) canvas.drawLine(0, f * bedH + 2*bedH, screenW, f * bedH + 2*bedH, linepaint);

        // Once the collisions have been handled, draw each object and apply friction
        //use iterator to allow removal from list
        boolean motion = false;
        Iterator<MoveObj> i = objs.iterator();
        while (i.hasNext()) {
            MoveObj obj = i.next(); // must be called before you can call i.remove()
            obj.draw(canvas);
            //if the coin is within a bed, highlight it. TODO - add a temporary score to the player
            if (obj.y<11*bedH&&(obj.y-2*bedH)%bedH>coinR&&(obj.y-2*bedH)%bedH<bedH-coinR)
                canvas.drawCircle(obj.x, obj.y, obj.radius, outlinepaint);
            obj.applyFriction(0.93f);
            if (obj.xSpeed != 0 || obj.ySpeed != 0) motion = true;
        }

        if (!motion && player1.state == 0) {
            currObj++;
            player1 = new MoveObj(11 + currObj % 2, coinR, screenW / 2, screenH - bedH, 5, 0);
            objs.add(player1);
        }

        //TODO total the scores

    }
}

