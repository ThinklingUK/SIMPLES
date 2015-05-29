package uk.thinkling.simples;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * SIMPLES
 * Created by Ellison on 11/04/2015.
 */
public class DrawView2 extends View {


    /*VARIABLES*/

    MoveObj player1;
    MoveObj[] objs = new MoveObj[50];
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
        for (int bCount = 0; bCount < objs.length; bCount++) objs[bCount] = new MoveObj( w, h);
        player1 = new MoveObj(100, w, h);
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

        // count for each entry in the objs array and then check for collision against all of the subsequent ones.
        for (int bCount1 = 0; bCount1 < objs.length; bCount1++) {
            for (int bCount2 = bCount1 + 1; bCount2 < objs.length; bCount2++) {
                objs[bCount1].collision(objs[bCount2]);
            }
        }

        // Once the collisions have been handled, move each object, then draw it.
        // remember that the .move()  function has been programmed to detect wall collisions
        for (MoveObj obj : objs) {
            obj.move(screenW, screenH);
            obj.draw(canvas);
        }

        // Check Hero MoveObj For Collisions - TODO could avoid this duplicated code if we add hero to array.
        for (MoveObj obj : objs) if (player1.collision(obj) && (obj.type == 0)) score++;

        //move the player and then draw
        player1.move(screenW, screenH);
        player1.draw(canvas);

    }


}
