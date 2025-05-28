package data;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "UserData.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_NAME = "user_info";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_PROFILE = "profile";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_ROLE = "role";
    private static final String COLUMN_IC = "ic";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_LOGIN_DETAIL = "login_detail";
    private static final String COLUMN_LOGIN_STATUS = "login_status";
    private static final String COLUMN_FRONT = "front";
    private static final String COLUMN_BACK = "back";
    private static final String COLUMN_CERTIFICATES = "certificates";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_PROFILE + " TEXT, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_PASSWORD + " TEXT, " +
                COLUMN_ROLE + " TEXT, " +
                COLUMN_IC + " TEXT, " +
                COLUMN_EMAIL + " TEXT, " +
                COLUMN_LOGIN_DETAIL + " TEXT, " +
                COLUMN_LOGIN_STATUS + " TEXT, " +
                COLUMN_FRONT + " TEXT, " +
                COLUMN_BACK + " TEXT, " +
                COLUMN_CERTIFICATES + " TEXT)";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // 清空表中的所有数据
    public void clearUserData() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
        db.close();
    }

    // 插入数据
    public void insertUser(String profile, String name, String password, String role, String ic, String email,
                           String loginDetail, String loginStatus, String front, String back, String certificates) {
        clearUserData(); // 清除旧数据
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PROFILE, profile);
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_PASSWORD, password);
        values.put(COLUMN_ROLE, role);
        values.put(COLUMN_IC, ic);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_LOGIN_DETAIL, loginDetail);
        values.put(COLUMN_LOGIN_STATUS, loginStatus);
        values.put(COLUMN_FRONT, front);
        values.put(COLUMN_BACK, back);
        values.put(COLUMN_CERTIFICATES, certificates);

        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    // 读取数据
    public Cursor getUserData() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_NAME, null, null, null, null, null, null);
    }
}
