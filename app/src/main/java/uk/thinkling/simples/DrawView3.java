package uk.thinkling.simples;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.*;
import android.preference.PreferenceManager;
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

import uk.thinkling.physics.CollisionManager;
import uk.thinkling.physics.MoveObj;


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
    int shadoff = 4; //shadow offset TODO - factor of coinR
    final double gravity = 0, friction = 0.07;
    final float coinRatio = 0.33f; // bed to radius friction (0.33 is 2 thirds,
    final float bedSpace=0.8f; // NB: includes end space and free bed before first line.
    int beds=9, maxCoins=5, bedScore=3;
    int coinCount= -1, winner = -1;
    int playerNum = 0;
    int[][] score = new int[2][beds+2]; // bed zero is for point score and final bed is for tracking completed
    String[] pName = new String[2];
    boolean sounds=true, bounds=true, rebounds=true, highlight=true;

    MainActivity parent;
    private final GestureDetectorCompat gdc;

    static Paint linepaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    static Paint outlinepaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    static Paint shadowpaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    Paint bmppaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    static Bitmap rawbmp, bmp; // bitmap for the coin
    Matrix matrix = new Matrix(); //matrix for bitmap


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

        float strokeSize = (w/180);  // NB this is driven by width so set in onSizeChanged
        shadowpaint.setARGB(64,0,0,0);
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

        try {
            loadPrefs();
        } catch (Exception e){
            Log.d("LOADING PREFS",  e.getMessage());
        }


        collider = new CollisionManager(w, h, friction, gravity);

        try {
            restoreData();
         } catch (Exception ex){ //could be FileNotFoundException, IOException, ClassNotFoundException
            Log.d("deserialise",ex.toString());
        }

        //TODO - if beds changed, then stored object radius may be inaccurate
        //TODO if #beds changed (ie. in prefs) and mismatch with saved data, reset score data
        //Also if bedScore changes then scoring might fail - best to restart in these cases - or all cases?
        if (score.length!=beds+2) score = new int[2][beds+2];


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
                inPlay.rSpeed = Math.random()*20-10;
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
        //draw the 2 sidebars
        canvas.drawLine(bedH, 0, bedH, screenH, linepaint);
        canvas.drawLine(screenW-bedH, 0, screenW-bedH, screenH, linepaint);


        /*Score's Text*/
        parent.HighScoreText.setText(pName[playerNum] + " turn " + (coinCount+1));
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
                // if a wall collision, play sound and may void the coin
                if (sounds) parent.player.play(parent.clunkSound, volume, volume, 2, 0, 1);
                if (bounds) coll.obja.state=-1; // if boundary rules apply, set to void
            } else {
                if (sounds) parent.player.play(parent.clinkSound, volume, volume, 2, 0, 1);
            }
        }



        // Once the collisions have been handled, draw each object and apply friction
        //use iterator to allow removal from list
        boolean motion = false;
        Iterator<MoveObj> i = objs.iterator();
        while (i.hasNext()) {
            MoveObj obj = i.next(); // must be called before you can call i.remove()
            canvas.drawCircle((float) obj.x+shadoff, (float) obj.y+shadoff, coinR, shadowpaint);
            matrix.reset();
            matrix.postTranslate(-coinR, -coinR);
            matrix.postRotate(obj.angle);
            matrix.postTranslate((float) obj.x, (float) obj.y);
            canvas.drawBitmap(bmp, matrix, bmppaint);

            //if outside the sidebars and boundary rules are on, then void the coin if already in playzone
            if (bounds && obj.state==0 && (obj.x-coinR<bedH || obj.x+coinR>screenW-bedH)) obj.state=-1;


            if (highlight) {
                if (obj.state < 0) obj.draw(canvas); //TODO - move bitmap and matrix into moveObj OUTLINE IF VOIDED
                else
                    //if the coin is within a bed, highlight it. TODO - add a temporary score to the player, or only when motion stopped
                    if (getBed((int) obj.y) > 0)
                        canvas.drawCircle((float) obj.x, (float) obj.y, coinR, outlinepaint);
            }


            if (obj.xSpeed != 0 || obj.ySpeed != 0) {
                motion = true;
                // if there is a streamID then adjust volume else start movement sound
                float volume = Math.min( (float)Math.sqrt(obj.xSpeed*obj.xSpeed+obj.ySpeed*obj.ySpeed) / 50, 1); //set the volume based on impact speed TODO const or calc

                if (obj.movingStreamID >0) {
                    parent.player.setVolume(obj.movingStreamID,volume,volume);
                    //adjust volume
                } else {
                    if (sounds) obj.movingStreamID = parent.player.play(parent.slideSound, volume, volume, 1, -1, 1);
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
        if (inPlay != null && inPlay.state == 1 && inPlay.y < startZone) inPlay.state = 0;

        //if there is no ball, or a ball in play and all motion stops, play new ball.
        if (inPlay == null || !motion && inPlay.state != 1) {
                coinCount++;
                // all coins played, so do scoring here.
                if (coinCount>=maxCoins) {
                    i = objs.iterator();
                    while (i.hasNext()) {
                        MoveObj obj = i.next(); // must be called before you can call i.remove()
                        //if the coin is within a bed, score it
                        int bed = getBed((int)obj.y);
                        if (bed>0 && obj.state>=0) { //don't include coin if voided. (ie. state is -1
                            score[playerNum][0] += bed; //add the score onto player total // used in portsmouth rules
                            //if already three, increment opponent up to three, else increment player up to three
                            if (!addPoint(playerNum, bed, true)) addPoint(1-playerNum,bed,false);
                            if (score[playerNum][beds+1]>=beds) winner = playerNum;
                            //THIS IS A WIN !!!!! reset all
                            // TODO would be good to have a "scoring" state where the coins and scores are animated

                        }
                    }
                    coinCount=0;
                    objs.clear();
                    if (winner>=0){
                        Toast.makeText(getContext(), "Won by "+pName[winner], Toast.LENGTH_LONG).show();
                        for (int f = 0; f <= score.length; f++) score[0][f] = score[1][f] = 0; /* set scores to zero */
                        playerNum = 0;
                        winner = -1;
                    } else playerNum=1-playerNum;

                    // TODO - if progressive (Oxford) then return some coins
                }

                // add a new coin - this could be first coin
                // TODO in combat mode we alternate playerNum
                inPlay = new MoveObj(11 + playerNum, coinR, screenW / 2, screenH - bedH, 5, 0);
                inPlay.wallBounce=rebounds; //enable or disable wall bounce TODO - move into constructor
            objs.add(inPlay);
                if (sounds) parent.player.play(parent.placeSound,1,1,1,0,1);
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

    private boolean addPoint(int player, int bed, boolean scorer){
        // if the bed is full cannot add so return false - point may go to opponent
        if (score[player][bed] == bedScore) return false;

        // if the bed will not get filled then add
        if (score[player][bed] < bedScore-1){
            score[player][bed]++;
            return true;
        }

        // remaining case is that a bed will bet filled. This cannot be allowed if a non-scorer would win.
        if (!scorer && score[player][beds + 1] >= beds - 1) return false;

        score[player][bed]++;
        score[player][beds + 1]++;
        return true;
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

    // TODO - pull in the cache here - rather than the onStart()
    public void loadPrefs() throws ClassCastException  {
        //Load lists from file or set defaults TODO set defaults as consts
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        pName[0] = preferences.getString("pref_player1", "Player 1");
        pName[1] = preferences.getString("pref_player2", "Player 2");

        sounds = preferences.getBoolean("pref_sounds", true);
        bounds = preferences.getBoolean("pref_bounds", true);
        rebounds = preferences.getBoolean("pref_rebounds", true);
        highlight = preferences.getBoolean("pref_highlight", true);


        maxCoins = Integer.parseInt(preferences.getString("pref_maxCoins", "5"));
        bedScore = Integer.parseInt(preferences.getString("pref_bedscore", "3"));
        beds = Integer.parseInt(preferences.getString("pref_beds", "9"));

        //Number of beds then affects bed size, coin size etc.

        bedH = Math.round(screenH * bedSpace / (beds + 3)); //2 extra beds for end and free space after flickzone
        coinR=Math.round(bedH * coinRatio);
        startZone=(beds+2)*bedH+coinR;

        rawbmp = BitmapFactory.decodeResource(getResources(), R.drawable.coin67);
        bmp = Bitmap.createScaledBitmap(rawbmp, coinR * 2, coinR * 2, true);
        bmppaint.setFilterBitmap(true);

        //TODO if #beds changed (ie. in prefs) and mismatch with saved data, reset score data

    }
}

