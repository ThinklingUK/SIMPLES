package uk.thinkling.simples;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.*;

/**
 * SIMPLES
 * Created by Ellison on 11/04/2015.
 */
public class DrawView4 extends View {


    /*VARIABLES*/

    MoveObj player1;
    List<MoveObj> objs = new ArrayList<>();

    int screenW;
    int screenH;

    int gameState = 0;

    MainActivity parent;

    int highScore = 0;
    int score = 0;
    int timeSoFar = 0;
    int seconds = 100;

    /*VARIABLES*/


    // this is the constructor - it is called when an instance of this class is created
    public DrawView4(Context context, AttributeSet attrs) {
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
    }

    /* BIT FOR TOUCHING! */
    @Override
    public boolean onTouchEvent(@NonNull MotionEvent e) {

        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.

        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (gameState == 1) {
                    player1.xSpeed = (int) (e.getX() - player1.x) / 10;
                    player1.ySpeed = (int) (e.getY() - player1.y) / 10;
                } else {
                    gameState = 1;
                    objs.clear();
                    objs.add(new MoveObj(100, screenW, screenH));
                    player1 = objs.get(0);
                    for (int bCount = 1; bCount < 20; bCount++) objs.add(new MoveObj(screenW, screenH));
                    objs.add(new MoveObj(0, screenW, screenH));
                    seconds=100;
                  }
        }

        return true;
    }


    @Override
    // this is the method called when the view needs to be redrawn
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        if (gameState == 0) {
            highScore = Math.max(highScore, score);
            score = 0;
        }else{

            //Timer: Every 10 Seconds Add 1 To Score
            timeSoFar++;
            if (timeSoFar % 25 == 0) {
                seconds--;
            }

        /*Score's Text*/
            parent.HighScoreText.setText("High Score: " + highScore);
            parent.ScoreText.setText("Score: " + score);
            parent.TimeLeftText.setText("Seconds: " + seconds);

            double dt = 0;

            while (dt < 1) {

                //find first collision and forward wind to that point.
                List<CollisionRec> collisions = new ArrayList<CollisionRec>();

                // count for each entry in the objs array and then check for collision against all of the subsequent ones.
                //ignore if time is more than 1 (wait for next cycle)
                for (int bCount1 = 0; bCount1 < objs.size(); bCount1++) {
                    for (int bCount2 = bCount1 + 1; bCount2 < objs.size(); bCount2++) {
                        double time = objs.get(bCount1).TimeOfClosestApproach(objs.get(bCount2));
                        if (time < 1 && time >= 0)
                            collisions.add(new CollisionRec(time, objs.get(bCount1), objs.get(bCount2)));
                    }
                }

                // check wall collisions also
                for (MoveObj obj : objs) {
                    double time = obj.wallCollisionTime(screenW, screenH);
                    if (time < 1 && time >= 0) collisions.add(new CollisionRec(time, obj, null));
                }


                Collections.sort(collisions, new Comparator<CollisionRec>() {
                    public int compare(CollisionRec a, CollisionRec b) {
                        return (a.time > b.time) ? -1 : 1;
                    }
                });

                double t;

                if (collisions.isEmpty()) {
                    t = 1 - dt;
                } else {
                    t = collisions.get(0).time;
                }

                // First, wind each object to time of first collision.
                for (MoveObj obj : objs) obj.move(screenW, screenH, t);

                // handle all the collisions starting at earliest time - if 2nd obj is null, then a wall collision
                for (CollisionRec coll : collisions) {
                    if (coll.time > t) break;
                    if (coll.objb == null) coll.obja.wallCollision(screenW, screenH);
                    else {
                        coll.obja.objCollision(coll.objb);
                        if (coll.obja.type == 0) coll.objb.state = 0;
                        else if (coll.objb.type == 0) coll.obja.state = 0;
                    }
                }

                // update dt (becomes 1 if no collisions)
                dt += t;

            }

            int balls_left = 0;

            // Once the collisions have been handled, draw each object and apply friction
            Iterator<MoveObj> i = objs.iterator();
            while (i.hasNext()) {
                MoveObj obj = i.next(); // must be called before you can call i.remove()
                balls_left += (obj.type == 0) ? 0 : 1;
                obj.draw(canvas);
                obj.applyFriction(0.985f);
                if (obj.state == 0) obj.radius -= 1;
                if (obj.radius == 0) {
                    i.remove();
                    score += 1;
                }
            }

            if (balls_left == 1) {
                score += seconds;
                seconds += 100;
                for (int bCount = 1; bCount < 20; bCount++) objs.add(new MoveObj(screenW, screenH));
            }
            if (player1.radius == 0 || seconds<=0) gameState=0;
        }
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
