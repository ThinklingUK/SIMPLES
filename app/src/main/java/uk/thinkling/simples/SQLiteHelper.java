/*
package uk.thinkling.simples;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.format.Time;
import android.util.Log;

import java.util.ArrayList;

public class SQLiteHelper extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 5;
    // Database Name
    private static final String DATABASE_NAME = "MoveObjDB";

    // Items table name
    private static final String TABLE_MO = "moveobj";


    // Items Table Columns names
    private static final String MO_KEY_ID = "id";
    private static final String MO_KEY_TYPE = "type";
    private static final String MO_KEY_X = "x";
    private static final String MO_KEY_Y = "y";
    private static final String MO_KEY_XV = "xv";
    private static final String MO_KEY_YV = "yv";
    private static final String MO_KEY_RV = "rv";
    private static final String MO_KEY_RADIUS = "radius";
    private static final String MO_KEY_STATE = "state";
    private static final String MO_KEY_COLOUR = "colour";


    private static final String[] ITEM_COLUMNS = {MO_KEY_ID, MO_KEY_TYPE , MO_KEY_X ,MO_KEY_Y , MO_KEY_XV , MO_KEY_YV , MO_KEY_RV , MO_KEY_RADIUS , MO_KEY_STATE , MO_KEY_COLOUR};

    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // SQL statement to create label table
        String CREATE_LABEL_TABLE = "CREATE TABLE labels ( " +
                "id INTEGER PRIMARY KEY, " +
                "title TEXT, "+
                "colour TEXT, "+
                "parent INTEGER, "+
                "state INTEGER" +
                ")";

        // create items table
        db.execSQL(CREATE_LABEL_TABLE);

        // SQL statement to create item table
        String CREATE_ITEM_TABLE = "CREATE TABLE items ( " +
                "id INTEGER PRIMARY KEY, " +
                "title TEXT, "+
                "start INTEGER, "+
                "end INTEGER, " +
                "labels STRING, " +
                "running INTEGER " +
                ")";

        // create items table
        db.execSQL(CREATE_ITEM_TABLE);



    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older items table if existed
        db.execSQL("DROP TABLE IF EXISTS items");
        db.execSQL("DROP TABLE IF EXISTS labels");

        // create fresh items table
        this.onCreate(db);
    }
    //---------------------------------------------------------------------

    public void dropTables() {
        // Drop older items table if existed
        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL("DROP TABLE IF EXISTS items");
        db.execSQL("DROP TABLE IF EXISTS labels");

        // create fresh items table
        this.onCreate(db);
    }
    //---------------------------------------------------------------------

    */
/**
     * CRUD operations (create "add", read "get", update, delete) item + get all items + delete all items
     *//*



    public void addItem(Item item){
        Log.d("addItem", item.toString());
        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        //values.put(ITEM_KEY_ID, item.getId()); // get title
        values.put(ITEM_KEY_TITLE, item.getTitle()); // get title
        values.put(ITEM_KEY_START, item.getStart().toMillis(true)/1000); // get start but as seconds - otherwise stores -ve
        values.put(ITEM_KEY_END, item.getEnd().toMillis(true)/1000); // get end but as seconds
        values.put(ITEM_KEY_LABELS, item.getLabelIds()); // get labels
        values.put(ITEM_KEY_RUNNING, (item.getRunning())? 1 : 0); // get labels

        // 3. insert
        item.setId((int) db.insert(TABLE_ITEMS, // table
                null, //nullColumnHack
                values)); // key/value -> keys = column names/ values = column values

        // 4. close
        db.close();
    }

    public Item getItem(int id){

        // 1. get reference to readable DB
        SQLiteDatabase db = this.getReadableDatabase();

        // 2. build query
        Cursor cursor =
                db.query(TABLE_ITEMS, // a. table
                        ITEM_COLUMNS, // b. column names
                        " id = ?", // c. selections
                        new String[] { String.valueOf(id) }, // d. selections args
                        null, // e. group by
                        null, // f. having
                        null, // g. order by
                        null); // h. limit

        // 3. if we got results get the first one
        if (cursor != null)
            cursor.moveToFirst();

        // 4. build item object
        Item item = new Item();
        item.setId(Integer.parseInt(cursor.getString(0)));
        item.setTitle(cursor.getString(1));
        item.setStart((long) cursor.getInt(2) * 1000);  //NB: convert to millis and cast to long - else overflows
        item.setEnd((long) cursor.getInt(3) * 1000);
        item.setLabels(cursor.getString(4));
        item.setRunning((cursor.getInt(5) == 1)? true : false); //NB: if true, this will update FathmApp.runningItem

        ThinkLog.log("loaded with start L:" + cursor.getLong(2) + " D:"+cursor.getDouble(2) + " I:"+cursor.getInt(2));
        db.close();

        // 5. return item
        return item;
    }

    // Get all Items for a given day
    public void getAllItems(Time time) {
        int dayMSecs = 24*60*60; //HACK - const
        long date = (long) time.toMillis(true)/1000;
        // 1. build the query
        // NB: looking for items with start and end within the day. - to handle day spanning means start within day OR end within day
        String query = "SELECT  * FROM " + TABLE_ITEMS + " WHERE "+ ITEM_KEY_START + " BETWEEN " + date +" AND "+ (date+dayMSecs) + " ORDER BY "+ ITEM_KEY_START + " ASC";
        //String query = "SELECT  * FROM " + TABLE_ITEMS + " ORDER BY "+ ITEM_KEY_START + " ASC";
        ThinkLog.log("trying: " + query);

        // Ditch existing list HACK - check memory clearup
        FathmApp.getItemList().items=new ArrayList<Item>();

        //store ID of the running item - if exists - else zero
        int runningID = (FathmApp.runningItem==null)?0:FathmApp.runningItem.getId();

        // 2. get reference to writable DB
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        // 3. go over each row, build item and add it to list
        Item item,prevItem=null;
        if (cursor.moveToFirst()) {
            do {
                //          if (!cursor.getString(1).isEmpty()) { //if no title then assume a blank entry and ignore - TODO 2 these should be tidied up later - need to insert new items if gaps exist. not for db though. perhaps only create db entry once title is given.
                //if this is the runningID, then already loaded - so link in to list.
                if (Integer.parseInt(cursor.getString(0))==runningID) item = FathmApp.runningItem;
                else {
                    item = new Item();
                    item.setId(Integer.parseInt(cursor.getString(0)));
                    item.setTitle(cursor.getString(1));
                    item.setStart((long) cursor.getInt(2) * 1000);  //NB: convert to millis and cast to long - else overflows
                    item.setEnd((long) cursor.getInt(3) * 1000);
                    item.setLabels(cursor.getString(4));
                    item.setRunning(cursor.getInt(5) == 1); //NB: if true, this will update FathmApp.runningItem
                }
                item.prev = prevItem;
                if (prevItem != null) prevItem.next = item;

                // Add item to items
                ThinkLog.log("Loading " + item.toString());
                FathmApp.getItemList().items.add(item);
                prevItem = item;
                //              }
            } while (cursor.moveToNext());
        }
        db.close();

        Log.d("getAllItems()", "Loaded");

    }
    // Get running item if one exists
    public Item getRunningItem() {

        Item item=null;

        // 1. build the query
        // NB: looking for items marked as running
        String query = "SELECT  * FROM " + TABLE_ITEMS + " WHERE "+ ITEM_KEY_RUNNING + " = 1 ORDER BY "+ ITEM_KEY_START + " DESC";
        ThinkLog.log("trying: " + query);

        // Ditch existing list HACK - check memory clearup
        // 2. get reference to writable DB
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        // 3. go over each row, build item and add it to list
        if (cursor.moveToFirst()) {

                item = new Item();
                item.setId(Integer.parseInt(cursor.getString(0)));
                item.setTitle(cursor.getString(1));
                item.setStart((long) cursor.getInt(2) * 1000);  //NB: convert to millis and cast to long - else overflows
                item.setEnd((long) cursor.getInt(3) * 1000);
                item.setLabels(cursor.getString(4));
                item.setRunning(true);

                // Add item to items
                ThinkLog.log("Loading running item " + item.toString());

            //TODO 3 - check if running item is from a past day. set end (midnight) for that day and stop.

        }
        if (cursor.moveToNext()) {
            Log.d("getRunningItem()", "Found additional running item(s)");
            //TODO 3 should run db query to clear out running items (live one will then re-update)

        }
        db.close();

        Log.d("getRunningItem()", "Loaded");
        return item;
    }

    // Updating single item
    public int updateItem(Item item) {

        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(ITEM_KEY_TITLE, item.getTitle()); // get title
        values.put(ITEM_KEY_START, item.getStart().toMillis(true)/1000); // get start
        values.put(ITEM_KEY_END, item.getEnd().toMillis(true)/1000); // get end
        values.put(ITEM_KEY_LABELS, item.getLabelIds()); // get end
        values.put(ITEM_KEY_RUNNING, (item.getRunning())? 1 : 0); // get labels

        // 3. updating row
        int i = db.update(TABLE_ITEMS, //table
                values, // column/value
                ITEM_KEY_ID + " = ?", // selections
                new String[]{String.valueOf(item.getId())}); //selection args

        // 4. close
        db.close();

        return i;

    }

    // Deleting single item
    public void deleteItem(Item item) {

        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. delete
        db.delete(TABLE_ITEMS,
                ITEM_KEY_ID+" = ?",
                new String[] { String.valueOf(item.getId()) });

        // 3. close
        db.close();

        Log.d("deleteItem", item.toString());

    }

    // Deleting all items
    public void deleteAllItems() {

        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. delete
        db.delete(TABLE_ITEMS, null, null);
        db.execSQL("DROP TABLE IF EXISTS items");

        // 3. close
        db.close();

        Log.d("deleteAllItem","all items deleted");

    }

    //---------------------------------------------------------------------
    */
/** LABELS
     * CRUD operations (create "add", read "get", update, delete) label + get all labels + delete all labels
     *//*


    // labels table name
    private static final String TABLE_LABELS = "labels";

    // Labels Table Columns names
    private static final String LABEL_KEY_ID = "id";
    private static final String LABEL_KEY_TITLE = "title";
    private static final String LABEL_KEY_COLOUR = "colour";
    private static final String LABEL_KEY_PARENT = "parent";
    private static final String LABEL_KEY_STATE = "state";

    private static final String[] LABEL_COLUMNS = {LABEL_KEY_ID,LABEL_KEY_TITLE, LABEL_KEY_COLOUR, LABEL_KEY_PARENT, LABEL_KEY_STATE};

    public void addLabel(Label label){
        Log.d("addLabel", label.toString());
        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(LABEL_KEY_ID, label.getId()); // get title
        values.put(LABEL_KEY_TITLE, label.getTitle()); // get title
        values.put(LABEL_KEY_COLOUR, label.getColourString()); // get colour
        values.put(LABEL_KEY_PARENT, label.getParentID()); // get parent
        values.put(LABEL_KEY_STATE, label.getState()); // get state

        // 3. insert
        db.insert(TABLE_LABELS, // table
                null, //nullColumnHack
                values); // key/value -> keys = column names/ values = column values

        // 4. close
        db.close();
    }

    public Label getLabel(int id){

        // 1. get reference to readable DB
        SQLiteDatabase db = this.getReadableDatabase();

        // 2. build query
        Cursor cursor =
                db.query(TABLE_LABELS, // a. table
                        LABEL_COLUMNS, // b. column names
                        " id = ?", // c. selections
                        new String[] { String.valueOf(id) }, // d. selections args
                        null, // e. group by
                        null, // f. having
                        null, // g. order by
                        null); // h. limit

        // 3. if we got results get the first one
        if (cursor != null)
            cursor.moveToFirst();

        // 4. build label object
        Label label = new Label();
        label.setId(Integer.parseInt(cursor.getString(0)));
        label.setTitle(cursor.getString(1));
        label.setColour(cursor.getString(2));
        //label.setParent(cursor.getInt(3)); need to find parent
        label.setState(cursor.getInt(4));

        Log.d("getLabel("+id+")", label.toString());
        db.close();

        // 5. return label
        return label;
    }

    // Get All Labels
    public void loadAllLabels() {

        // 1. build the query
        String query = "SELECT  * FROM " + TABLE_LABELS + " ORDER BY " + LABEL_KEY_ID + " ASC";
        Label parent;
        // 2. get reference to writable DB
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        // 3. go over each row, build label and add it to list
        Label label = null;
        if (cursor.moveToFirst()) {
            do {
                label = new Label();
                label.setId(Integer.parseInt(cursor.getString(0)));
                label.setTitle(cursor.getString(1));
                label.setColour(cursor.getString(2));
                Log.d("getAllLabels()", "adding label "+ label.getTitle()+ " with ID "+label.getId());

                try {
                    if (cursor.getInt(3)>0) {
                        parent = FathmApp.allLabels.get(cursor.getInt(3));
                        Log.d("getAllLabels()", "adding label "+ cursor.getString(1)+ " to "+parent.getTitle());
                        parent.addChild(label);
                    } else {
                        FathmApp.getLabelList().add(label);
                    }
                } catch (Exception e) {
                    Log.d("DB load","Could not find parent for "+label.getTitle());
                    FathmApp.getLabelList().add(label); //add orphan entry at top level
                }

                label.setState(cursor.getInt(4));
                Label.idCounter=Math.max(Label.idCounter,label.getId()+1); //set the id counter for new labels based on highest id

                // Add to allLabels in case needed to find as parent
                FathmApp.allLabels.put(label.id,label);

            } while (cursor.moveToNext());
        }

        db.close();

        Log.d("getAllLabels()", "OK");



    }

    // Updating single label
    public int updateLabel(Label label) {

        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(LABEL_KEY_TITLE, label.getTitle()); // get title
        values.put(LABEL_KEY_COLOUR, label.getColourString()); // get start
        values.put(LABEL_KEY_PARENT, label.getParentID()); // get end
        values.put(LABEL_KEY_STATE, label.getState()); // get end

        // 3. updating row
        int i = db.update(TABLE_LABELS, //table
                values, // column/value
                LABEL_KEY_ID+" = ?", // selections
                new String[] { String.valueOf(label.getId()) }); //selection args

        // 4. close
        db.close();

        return i;

    }

    // Deleting single label
    public void deleteLabel(Label label) {

        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. delete
        db.delete(TABLE_LABELS,
                LABEL_KEY_ID+" = ?",
                new String[] { String.valueOf(label.getId()) });

        // 3. close
        db.close();

        Log.d("deleteLabel", label.toString());

    }

    // Deleting all labels
    public void deleteAllLabels() {

        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. delete
        db.delete(TABLE_LABELS, null, null);
        db.execSQL("DROP TABLE IF EXISTS labels");

        // 3. close
        db.close();

        Log.d("deleteAllLabels","all labels deleted");

    }

}*/
