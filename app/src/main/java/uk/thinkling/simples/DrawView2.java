package uk.thinkling.simples;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * SIMPLES
 * Created by Ellison on 11/04/2015.
 */
public class DrawView2 extends View {


    /*VARIABLES*/

    MoveObj player1;
    MoveObj[] objs = new MoveObj[21];
    int screenW;
    int screenH;

    MainActivity parent;

    int highScore = 0;
    int score = 0;
    int timeSoFar = 0;

    /*VARIABLES*/


    // this is the constructor - it is called when an instance of this class is created
    public DrawView2(Context context, AttributeSet attrs) {
        super(context, attrs);
        parent = (MainActivity) this.getContext();
    }


    // this happens if the scren size changes - including the first timeit is measured. Here is where we get width and height
    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        Log.d("onMeasure", w + " "+ h);
        screenW = w;
        screenH = h;
        // initialise the objs array by creating a new MoveObj object for each entry in the array
        // TODO - maybe only do this if null
        player1 = objs[0] = new MoveObj(100, w, h);
        for (int bCount = 1; bCount < objs.length; bCount++) objs[bCount] = new MoveObj( w, h);
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

        super.onDraw(canvas);

        //Timer: Every 10 Seconds Add 1 To Score
        timeSoFar++;
        if(timeSoFar% 250 == 0){
           score++;
           if(score > highScore)    highScore = score;
        }

        /*Score's Text*/
        parent.HighScoreText.setText("High Score: "+highScore);
        parent.ScoreText.setText("Score: "+ score);
        parent.TimeLeftText.setText("Seconds: "+Math.round(timeSoFar/25));


        double dt=1;
        while (dt>=0) {
            // First, move each object forward in time
            for (MoveObj obj : objs) obj.move(screenW,screenH,dt);
            dt=-1;

            //find first collision and rewind to that point.
            List<CollisionRec> collisions = new ArrayList<CollisionRec>();

            // count for each entry in the objs array and then check for collision against all of the subsequent ones.
            for (int bCount1 = 0; bCount1 < objs.length; bCount1++) {
                for (int bCount2 = bCount1 + 1; bCount2 < objs.length; bCount2++) {
                    double time = objs[bCount1].objCollisionTime(objs[bCount2]);
                    if (time>1) { Log.d("error","time of "+time + " on "+objs[bCount1] + objs[bCount2]); time=0; }
                    if (time >= 0) collisions.add(new CollisionRec(time,objs[bCount1],objs[bCount2]));
                }
            }

            // check wall collisions also
            for (MoveObj obj : objs) {
                double time = obj.wallCollisionTime(screenW, screenH);
                if (time>1) { time=0; Log.d("error","on "+obj);}
                if (time >= 0) collisions.add(new CollisionRec(time,obj,null));

            }

            Collections.sort(collisions, new Comparator<CollisionRec>() {
                public int compare(CollisionRec a, CollisionRec b) {
                    return (a.time > b.time)?-1:1;
                }
            });

            if (!collisions.isEmpty()) {

                dt=collisions.get(0).time;

                // First, rewind each object back to time of first collision.
                Log.d("rewind to collision", "at: " + dt);
                for (MoveObj obj : objs) obj.move(screenW,screenH,-dt);

                // handle all the collisions starting at earliest time - if 2nd obj is null, then a wall collision
                for (CollisionRec coll : collisions) {
                    if (coll.time<dt) break;
                    if (coll.objb == null) coll.obja.wallCollision(screenW, screenH);
                    else coll.obja.objCollision(coll.objb);
                }
            }
        }

        // Once the collisions have been handled, draw each object.
        for (MoveObj obj : objs) obj.draw(canvas);


    }


    public class CollisionRec {
        double time;
        MoveObj obja;
        MoveObj objb;


        public CollisionRec(double time, MoveObj obja, MoveObj objb) {
            this.time = time;
            this.obja = obja;
            this.objb = objb;
        }
    }


}
