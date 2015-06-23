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
import android.widget.Toast;

import java.io.*;
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
    final float factor = 0.93f, coinRatio = 0.33f; //friction and bed to radius factor (0.33 is 2 thirds,
    final float bedSpace=0.8f; // NB: includes end space and free bed before first line.
    final int beds=9, maxCoins=5, bedScore=3;
    int coinCount = -1;
    int playerNum, winner = 0;
    int[][] score = new int[2][beds+2]; // bed zero is for point score and final bed is for tracking completed

    MainActivity parent;
    private final GestureDetectorCompat gdc;

    static Paint linepaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    static Paint outlinepaint = new Paint(Paint.ANTI_ALIAS_FLAG);


    /*VARIABLES*/


    // this is the constructor - it is called when an instance of this class is created
    public DrawView3(Context context, AttributeSet attrs) {
        super(context, attrs);
        //if (!isInEditMode()) ;
        parent = (MainActivity) this.getContext(); //TODO - remove all references to parent to improve editor preivew
        gdc = new GestureDetectorCompat(parent, new MyGestureListener()); // create the gesture detector
        for (int f = 0; f<score.length; f++) score[0][f]=score[1][f]=0; //set scores to zero

    }


    // this happens if the screen size changes - including the first time - it is measured. Here is where we get width and height
    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        Log.d("onMeasure", w + " " + h);
        screenW = w;
        screenH = h;
        bedH = Math.round(h*bedSpace/(beds+3)); //2 extra beds for end and free space after flickzone
        coinR=Math.round(bedH*coinRatio);
        startZone=(beds+2)*bedH+coinR;
        collider = new CollisionManager(w, h);

        try {
            restoreData();
         } catch (Exception ex){
            //could be FileNotFoundException, IOException, ClassNotFoundException
            Log.d("deserialise",ex.toString());
        }

        float strokeSize = (w/180);
        linepaint.setColor(Color.parseColor("#CD7F32"));
        linepaint.setStyle(Paint.Style.STROKE);
        linepaint.setStrokeWidth(strokeSize); //TODO set based on screensize
        linepaint.setTextSize(30);
        linepaint.setDither(true);                    // set the dither to true
        linepaint.setStyle(Paint.Style.STROKE);       // set to STOKE
        //linepaint.setStrokeJoin(Paint.Join.ROUND);    // set the join to round you want
        linepaint.setStrokeCap(Paint.Cap.ROUND);      // set the paint cap to round too
       // linepaint.setPathEffect(new CornerPathEffect(10) );   // set the path effect when they join.
        linepaint.setAntiAlias(true);
        outlinepaint.set(linepaint);
        outlinepaint.setColor(Color.parseColor("#FFFFFF"));


        parent.TimeLeftText.setText("");
        parent.ScoreText.setText("");

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
               // canvas.drawText(""+score[0][beds-f],bedH/2,f * bedH + 2.6f*bedH, linepaint);
                drawScore(canvas, score[0][beds-f],0,f * bedH + 2*bedH);
                drawScore(canvas, score[1][beds-f],screenW - bedH,f * bedH + 2*bedH);

              //  canvas.drawText("" + score[1][beds - f], screenW - bedH / 2, f * bedH + 2.6f * bedH, linepaint);
            }
        }
        canvas.drawLine(bedH, 0, bedH, screenH, linepaint);
        canvas.drawLine(screenW-bedH, 0, screenW-bedH, screenH, linepaint);


        /*Score's Text*/
        parent.HighScoreText.setText("Player "+ (playerNum+1)+" turn "+(coinCount+1));
       //Portsmouth scores
       // parent.TimeLeftText.setText("P1: "+ score[0][0]);
       // parent.ScoreText.setText("P2: "+ score[1][0]);

        // bed counting
        //parent.TimeLeftText.setText("P1: "+ score[0][beds+1]);
        // parent.ScoreText.setText("P2: "+ score[1][beds+1]);



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
        if (inPlay != null && inPlay.state != 0 && inPlay.y < startZone) inPlay.state = 0;

        //if there is no ball, or a ball in play and all motion stops, play new ball.
        if (inPlay == null || !motion &&  inPlay.state == 0) {
                coinCount++;
                // all coins played, so do scoring here.
                if (coinCount>=maxCoins) {
                    i = objs.iterator();
                    while (i.hasNext()) {
                        MoveObj obj = i.next(); // must be called before you can call i.remove()
                        //if the coin is within a bed, highlight it. TODO - add a temporary score to the player
                        int bed = getBed((int)obj.y);
                        if (bed>0) {
                            score[playerNum][0] += bed; //add the score onto player total // used in portsmouth rules
                            //if already three, increment opponent up to three, else increment player up to three
                            switch (score[playerNum][bed]){
                                case bedScore: // bed already full, so add on to opponent;
                                    switch (score[1-playerNum][bed]){
                                        case bedScore: break; // bed already full, so ignore;

                                        case bedScore-1: // bed will be filled here - this does not apply in would-be-win situation
                                            if (score[1-playerNum][beds+1]<beds-1) {
                                                score[1-playerNum][bed]++;
                                                score[1-playerNum][beds + 1]++;
                                            }
                                            break;

                                        default: score[1-playerNum][bed]++;
                                    }
                                    break;

                                case bedScore-1: // bed will be filled here - this could be winning situation
                                    score[playerNum][bed]++;
                                    score[playerNum][beds+1]++;
                                    if (score[playerNum][beds+1]>=beds) {
                                        //THIS IS A WIN !!!!! reset all
                                        winner = playerNum + 1;
                                    }
                                    break;

                                default: score[playerNum][bed]++;
                            }

                            // TODO would be good to have a "scoring" state where the coins and scores are animated

                        }
                    }
                    coinCount=0;
                   objs.clear();
                    if (winner>0){
                        Toast.makeText(getContext(), "Won by Player "+winner, Toast.LENGTH_LONG).show();
                        for (int f = 0; f <= beds+1; f++)
                            score[0][f] = score[1][f] = 0; /*set scores to zero*/
                            playerNum = 0;
                    } else playerNum=1-playerNum;

                    // TODO - if progressive (Oxford) then return some coins
                }

                // add a new coin - this could be first coin
                // TODO in combat mode we alternate playerNum
                inPlay = new MoveObj(11 + playerNum, coinR, screenW / 2, screenH - bedH, 5, 0);
                objs.add(inPlay);
                    parent.player.play(parent.placeSound,1,1,1,0,1);
        }

        // TODO display potential scores


    }

    private void drawScore(Canvas c,int score, float x, float y){

        int div = bedH/bedScore; // this splits the verts
        for (int i = Math.min(score,bedScore-1); i>0; i--) {
            c.drawLine(x + i * div*01.1f, y + bedH * 0.2f, x + i * div * 0.9f, y + bedH * 0.8f, outlinepaint);
        }
        if (score == bedScore) c.drawLine(x + bedH*0.15f, y + bedH * 0.45f, x+bedH*0.85f, y + bedH * 0.55f, outlinepaint);


    }

    private int getBed(int pos){
        if (pos>startZone) return -1; // not even reached first line
        if ((pos+coinR)< 2*bedH) return 0; // in the endzone
        if ((pos-coinR)%bedH>coinR) return 0; //overlapping. so no score
        return (beds+2-((pos-coinR)/bedH));
    }



    // TODO - pull in the cache here - rather than the onStart()
    public void saveData() throws IOException {
        File file = new File(getContext().getCacheDir(), "moveObjs");
        ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(file));
        os.writeObject(objs);
        os.close();
        file = new File(getContext().getCacheDir(), "Scores");
        os = new ObjectOutputStream(new FileOutputStream(file));
        os.writeObject(score);
        os.close();
        Log.d("serialize onPause",objs.toString());
    }

    // TODO - pull in the cache here - rather than the onStart()
    public void restoreData() throws IOException,ClassNotFoundException {
        File file = new File(getContext().getCacheDir(), "moveObjs");
        ObjectInputStream is = new ObjectInputStream(new FileInputStream(file));
        objs = (ArrayList) is.readObject();
        coinCount = objs.size()-1;
        if (coinCount >= 0) {
            inPlay = objs.get(coinCount);
            playerNum=inPlay.type-11;
        }
        file = new File(getContext().getCacheDir(), "Scores");
        is = new ObjectInputStream(new FileInputStream(file));
        score= (int[][]) is.readObject();
        Log.d("deserialise", objs.toString());
    }
}

