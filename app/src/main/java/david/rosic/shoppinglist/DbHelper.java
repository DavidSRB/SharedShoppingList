package david.rosic.shoppinglist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DbHelper extends SQLiteOpenHelper {

    //Used for database table of users
    private final String TABLE_USERS = "Users";
    public static final String COLUMN_USERNAME = "Username";
    public static final String COLUMN_EMAIL = "Email";
    public static final String COLUMN_PASSWORD = "Password";

    //Used for database table of lists
    private final String TABLE_LISTS = "Lists";
    public static final String COLUMN_LIST_NAME = "List_name";
    public static final String COLUMN_SHARED = "Shared";

    //Used for database table of items in lists
    private final String TABLE_ITEMS = "Items";
    public static final String COLUMN_ITEM_NAME = "Item_name";
    public static final String COLUMN_TICKED = "Ticked";
    public static final String COLUMN_ITEM_ID = "Id";


    public DbHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        //Creates table of users
        db.execSQL("CREATE TABLE " + TABLE_USERS +
                " (" + COLUMN_USERNAME + " TEXT PRIMARY KEY, " +
                COLUMN_EMAIL + " TEXT, " +
                COLUMN_PASSWORD + " TEXT);");

        //Creates table of lists
        db.execSQL("CREATE TABLE " + TABLE_LISTS +
                " (" + COLUMN_LIST_NAME + " TEXT PRIMARY KEY, " +
                COLUMN_USERNAME + " TEXT, " +
                COLUMN_SHARED + " INTEGER);");

        //Creates table of items
        db.execSQL("CREATE TABLE " + TABLE_ITEMS +
                " (" +
                COLUMN_ITEM_ID + " INTEGER, " +
                COLUMN_ITEM_NAME + " TEXT, " +
                COLUMN_LIST_NAME + " TEXT, " +
                COLUMN_TICKED + " INTEGER" +
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    // ---- Functions associated with Users (Main Activity) ---- //

    /**
     * This function checks if a username exists in the database.
     *
     * @param username
     * @return true if the username exists.
     */
    private boolean doesUserExist(String username) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null, COLUMN_USERNAME + " =?", new String[]{username}, null, null, null);

        if (cursor.getCount() <= 0) {
            close();
            return false;
        }

        close();
        return true;
    }

    /**
     * This function registers the user in the database.
     *
     * @param username
     * @param email
     * @param password
     * @return false if the user already exists, true if not.
     */
    public boolean registerUser(String username, String email, String password) {

        if (doesUserExist(username)) {
            return false;
        }

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PASSWORD, password);

        db.insert(TABLE_USERS, null, values);
        close();
        return true;
    }

    /**
     * This function checks the database for user credentials.
     *
     * @param username
     * @param password
     * @return 0 on successful login, 1 on non-existant user, 2 on wrong password.
     */
    public int loginUser(String username, String password) {
        if (!doesUserExist(username)) {
            return 1;
        }

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null, COLUMN_USERNAME + " =?", new String[]{username}, null, null, null);

        if (cursor.getCount() <= 0) {
            close();
            return 1;
        }

        cursor.moveToFirst();

        String dbPassword = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD));

        if (password.equals(dbPassword)) {
            close();
            return 0;
        }

        //Wrong password
        close();
        return 2;
    }

    // ---- Functions associated with Shopping Lists (Welcome Activity) ---- //

    /**
     * This function checks if a list exists in the database.
     *
     * @param listName
     * @return true if the list exists.
     */
    public boolean doesListExist(String listName) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_LISTS, null, COLUMN_LIST_NAME + " =?", new String[]{listName}, null, null, null);

        if (cursor.getCount() <= 0) {
            close();
            return false;
        }

        close();
        return true;
    }

    /**
     * This function checks if a list is owned by a user.
     *
     * @param username
     * @param listName
     * @return 0 if user is owner, 1 if user is not the owner, 2 if the list doesn't exist.
     */
    public int isListOwnedByUser(String username, String listName) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_LISTS, null, COLUMN_LIST_NAME + " = ?", new String[]{listName}, null, null, null);

        if (cursor.getCount() <= 0) {
            close();
            return 2;
        }

        cursor.moveToFirst();
        String dbUsername = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME));

        if (username.equals(dbUsername)) {
            close();
            return 0;
        }
        close();
        return 1;
    }

    /**
     * This function creates a ShoppingList object based on the data in the Cursor object.
     *
     * @param cursor
     * @return a new ShoppingList object with values retrieved from the Cursor.
     */
    private ShoppingList createListInstance(Cursor cursor) {
        String listName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LIST_NAME));
        int shared = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SHARED));

        return new ShoppingList(listName, (shared != 0) ? true : false);
    }

    /**
     * This function creates a new shopping list with params in the database.
     *
     * @param listName
     * @param username
     * @param shared
     * @return true if the list is created, false if the list already exists in the database.
     */
    public boolean createList(String listName, String username, Boolean shared) {
        if (doesListExist(listName)) {
            return false;
        }

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_LIST_NAME, listName);
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_SHARED, shared);

        db.insert(TABLE_LISTS, null, values);
        close();
        return true;
    }

    /**
     * This function deletes specified shopping list from the database if the user is the owner of the list.
     *
     * @param listName
     * @param username
     * @return true if the list was successfully deleted, false if the user does not own the list or the list does not exist in the database.
     */
    public boolean deleteList(String listName, String username) {
        int listInfo = isListOwnedByUser(username, listName);

        if (listInfo == 1 || listInfo == 2) {
            return false;
        }

        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_LISTS, COLUMN_LIST_NAME + " = ?", new String[]{listName});
        db.delete(TABLE_ITEMS, COLUMN_LIST_NAME + " = ?", new String[]{listName});
        close();
        return true;
    }

    /**
     * This function returns a list of all the shopping lists that are accessible to the user
     * including all user owned lists and any shared lists.
     *
     * @param username
     * @return a list of all the shopping lists accessible to the user.
     */
    public ShoppingList[] getAllAccessibleLists(String username) {
        SQLiteDatabase db = getReadableDatabase();
        String selection = COLUMN_USERNAME + " = ? OR " + COLUMN_SHARED + " = 1";
        Cursor cursor = db.query(TABLE_LISTS, null, selection, new String[]{username}, null, null, null);

        if (cursor.getCount() <= 0) {
            close();
            return null;
        }

        ShoppingList[] list = new ShoppingList[cursor.getCount()];
        int i = 0;
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            list[i++] = createListInstance(cursor);
        }

        close();
        return list;
    }

    /**
     * This function returns a list of all user owned shopping lists.
     *
     * @param username
     * @return a list of all the shoppping lists that belong to the user.
     */
    public ShoppingList[] getAllUserLists(String username) {
        SQLiteDatabase db = getReadableDatabase();
        String selection = COLUMN_USERNAME + " = ?";
        Cursor cursor = db.query(TABLE_LISTS, null, selection, new String[]{username}, null, null, null);

        if (cursor.getCount() <= 0) {
            close();
            return null;
        }

        ShoppingList[] list = new ShoppingList[cursor.getCount()];
        int i = 0;
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            list[i++] = createListInstance(cursor);
        }

        close();
        return list;
    }

    // ---- Functions associated with Items (ShowList Activity) ---- //

    /**
     * This function returns an instance of the Task(Item) object with values pointed to by Cursor instance
     *
     * @param cursor
     * @return Task object instance
     */
    private Task createItemInstance(Cursor cursor) {
        String itemName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ITEM_NAME));
        int ticked = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TICKED));
        long id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ITEM_ID));

        return new Task(itemName, (ticked != 0) ? true : false, id);
    }

    /**
     * This function creates a new item from function parameters in the database with a database consistent id.
     *
     * @param itemName
     * @param listName
     * @return long id generated from the database
     */
    public boolean createItem(String itemName, String listName, long id) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_ITEM_NAME, itemName);
        values.put(COLUMN_LIST_NAME, listName);
        values.put(COLUMN_ITEM_ID, id);
        values.put(COLUMN_TICKED, 0);

        long row = db.insert(TABLE_ITEMS, null, values);
        close();

        if (row == -1) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * This function deletes specified item from the database.
     *
     * @param id database id of the item that should be deleted
     * @return true if the item was successfully deleted, false if the item with specified id does not exist in the database.
     */
    public boolean deleteItem(long id) {
        SQLiteDatabase db = getWritableDatabase();
        int numRowsAffected = db.delete(TABLE_ITEMS, COLUMN_ITEM_ID + " = ?", new String[]{Long.toString(id)});

        if (numRowsAffected == 1) {
            close();
            return true;
        } else {
            close();
            return false;
        }
    }

    /**
     * This function updates the ticked state of an item
     *
     * @param id     of item that will be updated
     * @param ticked new state
     * @return true if the update was successful, false if not
     */
    public boolean setItemState(long id, boolean ticked) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_TICKED, (ticked ? 1 : 0));

        int numRowsAffected = db.update(TABLE_ITEMS, values, COLUMN_ITEM_ID + " = ?", new String[]{String.valueOf(id)});

        if (numRowsAffected == 1) {
            close();
            return true;
        } else {
            close();
            return false;
        }
    }

    /**
     * This function returns a list of all the items in list specified by listName
     *
     * @param listName list from which items will be returned
     * @return returns a list of Task objects, items in specified list
     */
    public Task[] getListItems(String listName) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_ITEMS, null, COLUMN_LIST_NAME + " = ?", new String[]{listName}, null, null, null);

        if (cursor.getCount() <= 0) {
            return null;
        }

        Task[] items = new Task[cursor.getCount()];
        int i = 0;
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            items[i++] = createItemInstance(cursor);
        }

        close();
        return items;
    }

}
