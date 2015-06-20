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

    MoveObj inPlay;
    List<MoveObj> objs = new ArrayList<>();
    CollisionManager collider;

    int screenW, screenH, bedH, coinR, startZone;
    final float factor = 0.93f;
    final float bedSpace=0.8f; // NB: includes end space and free bed before first line.
    final int beds=9, maxCoins=5;
    int coinCount = 0;
    int playerNum = 0;
    int[][] score = new int[2][beds+1];

    MainActivity parent;
    private final GestureDetectorCompat gdc;

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
        linepaint.setStrokeWidth(3f); //TODO set based on screensize
        linepaint.setTextSize(30);
        outlinepaint.setColor(Color.parseColor("#FFFFFF"));
        outlinepaint.setStyle(Paint.Style.STROKE);
        outlinepaint.setStrokeWidth(3f);
        for (int f = 0; f<score.length; f++) score[0][f]=score[1][f]=0; //set scores to zero

    }


    // this happens if the screen size changes - including the first timeit is measured. Here is where we get width and height
    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        Log.d("onMeasure", w + " " + h);
        screenW = w;
        screenH = h;
        bedH = Math.round(h*bedSpace/(beds+3)); //2 extra beds for end and free space after flickzone
        coinR=bedH/3;
        startZone=(beds+2)*bedH+coinR;
        collider = new CollisionManager(w, h);

        // create the first ball
        inPlay = new MoveObj(11, coinR, screenW / 2, screenH - bedH, 5, 0);
        objs.add(inPlay);
        parent.player.play(parent.placeSound,1,1,1,0,1);
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
            if (inPlay.state == 1 && e.getY()> startZone) {
                inPlay.xSpeed = inPlay.ySpeed = 0;
                inPlay.x = e.getX();
                inPlay.y = e.getY();
            }
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            // effectively a Drag detector - although would need to check that we have hit a ball first.
            if (inPlay.state == 1 && e2.getY()>startZone) {
                inPlay.xSpeed = inPlay.ySpeed = 0;
                inPlay.x = e2.getX();
                inPlay.y = e2.getY();
            }
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (inPlay.state == 1 && e2.getY()>startZone) {
                inPlay.xSpeed = velocityX / 25;
                inPlay.ySpeed = velocityY / 25;
            }
            return true;
        }

    }


    @Override
    // this is the method called when the view needs to be redrawn
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);


        //Draw the sidelines and Beds (NB: the screen has a 2bed endzone, 9 full beds, a 1 bed exclusion and 3 bed fling zone)
        for (int f = 0; f <= beds; f++) {
            canvas.drawLine(0, f * bedH + 2*bedH, screenW, f * bedH + 2*bedH, linepaint);
            if (f<beds) {
                canvas.drawText(""+(beds-f),screenW/2,f * bedH + 2.6f*bedH, linepaint);
                canvas.drawText(""+score[0][beds-f],bedH/2,f * bedH + 2.6f*bedH, linepaint);
                canvas.drawText(""+score[1][beds-f],screenW-bedH/2,f * bedH + 2.6f*bedH, linepaint);
            }
        }
        canvas.drawLine(bedH, 0, bedH, screenH, linepaint);
        canvas.drawLine(screenW-bedH, 0, screenW-bedH, screenH, linepaint);


        /*Score's Text*/
        parent.HighScoreText.setText("Player "+ (playerNum+1));
        parent.TimeLeftText.setText("P1: "+ score[0][0]);
        parent.ScoreText.setText("P2: "+ score[1][0]);


        // handle all the collisions starting at earliest time - if 2nd obj is null, then a wall collision
        // Collision manager also moves the objects.
        collider.collide(objs);
        for (CollisionManager.CollisionRec coll : collider.collisions) {
            //TODO set pitch based on size of the objects.
            float volume = Math.min((float) coll.impactV / 100, 1); //set the volume based on impact speed
            if (coll.objb == null) {  //if a wall collision
                //TODO if a wall collision, may void the coin
                parent.player.play(parent.clunkSound, volume, volume, 2, 0, 1);
            } else {
                parent.player.play(parent.clinkSound, volume, volume, 2, 0, 1);
            }
        }



        // Once the collisions have been handled, draw each object and apply friction
        //use iterator to allow removal from list
        boolean motion = false;
        Iterator<MoveObj> i = objs.iterator();
        while (i.hasNext()) {
            MoveObj obj = i.next(); // must be called before you can call i.remove()
            obj.draw(canvas);
            //if the coin is within a bed, highlight it. TODO - add a temporary score to the player
            if (getBed((int)obj.y)>0)
                canvas.drawCircle(obj.x, obj.y, obj.radius, outlinepaint);
            obj.applyFrictionGravity(factor, 0);
            if (obj.xSpeed != 0 || obj.ySpeed != 0) {
                motion = true;
                // if there is a streamID then adjust volume else start movement sound
                float volume = Math.min( (float)Math.sqrt(obj.xSpeed*obj.xSpeed+obj.ySpeed*obj.ySpeed) / 50, 1); //set the volume based on impact speed TODO const or calc

                if (obj.movingStreamID >0) {
                    parent.player.setVolume(obj.movingStreamID,volume,volume);
                    //adjust volume
                } else {
                    obj.movingStreamID = parent.player.play(parent.slideSound, volume, volume, 1, -1, 1);
                }

            }
            else{
                //stop any playing sound
                if (obj.movingStreamID >0) {
                    parent.player.stop(obj.movingStreamID);
                    obj.movingStreamID=0;
                }
            }


        }
        // if ball in play exits the start zone, then it cannot be touched
        if (inPlay.state != 0 && inPlay.y < startZone) inPlay.state = 0;

        //if there is a ball in play and all motion stops, either new ball or return the ball if played short.
        if (!motion && inPlay.state == 0) {
                coinCount++;
                // all coins played, so add score
                if (coinCount>=maxCoins) {
                    i = objs.iterator();
                    while (i.hasNext()) {
                        MoveObj obj = i.next(); // must be called before you can call i.remove()
                        //if the coin is within a bed, highlight it. TODO - add a temporary score to the player
                        int bed = getBed((int)obj.y);
                        if (bed>0) {
                            score[playerNum][0] += bed; //add the score onto player total
                            //if already three, increment opponent up to three, else increment player
                            if (score[playerNum][bed]>=3) score[1-playerNum][bed]+=score[1-playerNum][bed]<3?1:0;
                            else score[playerNum][bed]++;

                        }
                    }
                    coinCount=0;
                    playerNum=1-playerNum;
                    objs.clear();

                    // TODO - if progressive then return some coins
                }

                // add a new coin
                // TODO in combat mode we alternate playerNum
                inPlay = new MoveObj(11 + playerNum, coinR, screenW / 2, screenH - bedH, 5, 0);
                objs.add(inPlay);
                parent.player.play(parent.placeSound,1,1,1,0,1);
        }

        // TODO display potential scores


    }

    private int getBed(int pos){
        if (pos>startZone) return -1; // not even reached first line
        if ((pos+coinR)< 2*bedH) return 0; // in the endzone
        if ((pos-coinR)%bedH>coinR) return 0; //overlapping. so no score
        return (beds+2-((pos-coinR)/bedH));
    }
}

