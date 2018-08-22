package top.imlk.oneword.dao;

import android.app.ActivityThread;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import java.util.ArrayList;

import top.imlk.oneword.BuildConfig;
import top.imlk.oneword.bean.ApiBean;
import top.imlk.oneword.bean.WordBean;

import static android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE;
import static android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE;

/**
 * Created by imlk on 2018/5/26.
 */
public class OneWordSQLiteOpenHelper extends SQLiteOpenHelper {

    private static final String LOG_TAG = "OneWordSQLiteOpenHelper";

    private static final String DB_NAME = "oneword.db";
//    public static final String TABLE_HISTORY = "history";
//    public static final String TABLE_LIKE = "[like]";
//    public static final String TABLE_READY_TO_SHOW = "ready_to_show";

    public static final String TABLE_ALL_ONEWORD = "all_oneword";
    public static final String TABLE_FAVOR = "favor";
    public static final String TABLE_HISTORY = "history";
    public static final String TABLE_TOSHOW = "toshow";
    public static final String TABLE_API = "api";

    public static final String KEY_ID = "id";
    public static final String KEY_CONTENT = "content";
    public static final String KEY_REFERENCE = "reference";
    public static final String KEY_TARGET_URL = "target_url";
    public static final String KEY_TARGET_TEXT = "target_text";

    public static final String KEY_ONEWORD_ID = "oneword_id";
    public static final String KEY_ADDED_AT = "added_at";

    public static final String KEY_NAME = "name";
    public static final String KEY_URL = "url";
    public static final String KEY_REQ_METHOD = "req_method";
    public static final String KEY_REQ_ARGS_JSON = "req_args_json";
    public static final String KEY_RESP_FORM = "resp_form";
    public static final String KEY_ENABLED = "enabled";


//    public static final String KEY_ID = "id";
//    public static final String KEY_MSG = "msg";
//    public static final String KEY_TYPE = "type";
//    public static final String KEY_FROM = "[from]";
//    public static final String KEY_CREATOR = "creator";
//    public static final String KEY_CREATED_AT = "created_at";
//    public static final String KEY_ADDED_AT = "added_at";
//    public static final String KEY_LIKE = "[like]";


    private static OneWordSQLiteOpenHelper oneWordSQLiteOpenHelper;

    public synchronized static boolean isDataBaseClosed() {

        if (oneWordSQLiteOpenHelper == null) {
            return true;
        }
        return false;
    }

    public synchronized static void closeDataBase() {
        if (!OneWordSQLiteOpenHelper.isDataBaseClosed()) {
            OneWordSQLiteOpenHelper.getInstance().close();
        }
    }

    public static OneWordSQLiteOpenHelper getInstance() {

        if (oneWordSQLiteOpenHelper == null) {
            synchronized (OneWordSQLiteOpenHelper.class) {
                if (oneWordSQLiteOpenHelper == null) {
                    oneWordSQLiteOpenHelper = new OneWordSQLiteOpenHelper(ActivityThread.currentApplication());
                }
            }
        }
        return oneWordSQLiteOpenHelper;
    }

    private OneWordSQLiteOpenHelper(Context context) {//使用versionCode作为数据库版本
        super(context, DB_NAME, null, BuildConfig.VERSION_CODE);
    }

    /**
     * 往 {@link #TABLE_ALL_ONEWORD} 里加入数据
     * 不查重
     *
     * @return id
     */
    public int insertOneWordWithoutCheck(WordBean wordBean) {
        if (wordBean == null) {
            return -1;
        }
        synchronized (this) {
            SQLiteDatabase sqLiteDatabase = getWritableDatabase();
            ContentValues contentValues = new ContentValues();


            // id 是自增列，不需要填值
            contentValues.put(KEY_CONTENT, nullToVoid(wordBean.content));
            contentValues.put(KEY_REFERENCE, nullToVoid(wordBean.reference));
            contentValues.put(KEY_TARGET_URL, wordBean.target_url);
            contentValues.put(KEY_TARGET_TEXT, wordBean.target_text);

            int id = (int) sqLiteDatabase.insertWithOnConflict(TABLE_ALL_ONEWORD, null, contentValues, CONFLICT_IGNORE);

            wordBean.id = id;

            return id;
        }
    }

    /**
     * 查询一条一言的总表ID
     *
     * @param wordBean
     * @return 一言的总表ID，不存在则返回 -1
     */
    public int queryIdOfOneWordInAllOneWord(WordBean wordBean) {
        synchronized (this) {
            SQLiteDatabase sqLiteDatabase = getReadableDatabase();
            Cursor cursor = sqLiteDatabase.rawQuery("SELECT " + KEY_ID + " FROM " + TABLE_ALL_ONEWORD + " WHERE " + KEY_CONTENT + "=? AND " + KEY_REFERENCE + "=?", new String[]{
                    nullToVoid(wordBean.content), nullToVoid(wordBean.reference)
            });

            int result = -1;

            if (cursor.moveToNext()) {
                result = cursor.getInt(0);
            }
            wordBean.id = result;
            cursor.close();
            return result;
        }
    }

    public WordBean queryOneWordInAllOneWordById(int id) {

        if (id <= 0) {
            return null;
        }

        synchronized (this) {
            SQLiteDatabase sqLiteDatabase = getReadableDatabase();

            Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM " + TABLE_ALL_ONEWORD + " WHERE " + KEY_ID + " =?", new String[]{String.valueOf(id)});

            WordBean result = null;

            if (cursor.moveToNext()) {
                result = new WordBean(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4));
            }

            cursor.close();

            return result;
        }
    }

    /**
     * 从总表中删除一个
     * 注意由于外键关联，从总表中删除后，关联的表中的行也会删除
     *
     * @param wordBean
     */
    public void removeOneWordInAllOneWord(WordBean wordBean) {
        synchronized (this) {
            SQLiteDatabase sqLiteDatabase = getWritableDatabase();

            int id = queryIdOfOneWordInAllOneWord(wordBean);


            if (id <= 0) {
                return;
            }
            sqLiteDatabase.execSQL("DELETE FROM " + TABLE_ALL_ONEWORD + " WHERE " + KEY_ID + "=?", new Object[]{id});

        }
    }


    public void insertToHistory(WordBean wordBean) {
        insertToSubTable(TABLE_HISTORY, wordBean);
    }

    public void insertToFavor(WordBean wordBean) {
        insertToSubTable(TABLE_FAVOR, wordBean);
    }

    public void insertToToShow(WordBean wordBean) {
        insertToSubTable(TABLE_TOSHOW, wordBean);
    }

    /**
     * 插入到子表，冲突处理方式为Replace
     *
     * @param wordBean
     * @param tableName
     */

    private void insertToSubTable(String tableName, WordBean wordBean) {
        if (wordBean == null) {
            return;
        }
        synchronized (this) {
            SQLiteDatabase sqLiteDatabase = getWritableDatabase();

            int id = wordBean.id;

            if (id <= 0) {
                id = queryIdOfOneWordInAllOneWord(wordBean);
                if (id <= 0) {
                    // 未收录
                    id = insertOneWordWithoutCheck(wordBean);
                    if (id <= 0) {
                        return;
                    }
                }
            }

            ContentValues contentValues = new ContentValues();
            contentValues.put(KEY_ONEWORD_ID, id);
            contentValues.put(KEY_ADDED_AT, System.currentTimeMillis());

            sqLiteDatabase.insertWithOnConflict(tableName, null, contentValues, CONFLICT_REPLACE);
        }
    }

    public boolean checkIfInHistory(int id) {
        return checkIfInSubTable(TABLE_HISTORY, id);
    }

    public boolean checkIfInFavor(int id) {
        return checkIfInSubTable(TABLE_FAVOR, id);
    }

    public boolean checkIfInToShow(int id) {
        return checkIfInSubTable(TABLE_TOSHOW, id);
    }

    private boolean checkIfInSubTable(String tableName, int id) {

        if (id <= 0) {
            return false;
        }

        synchronized (this) {

            SQLiteDatabase sqLiteDatabase = getReadableDatabase();

            Cursor cursor = sqLiteDatabase.rawQuery("SELECT " + KEY_ONEWORD_ID + " FROM " + tableName + " WHERE " + KEY_ONEWORD_ID + "=?",
                    new String[]{String.valueOf(id)});

            int count = cursor.getCount();
            cursor.close();

            return count != 0;
        }
    }


    public void removeFromHistory(int id) {
        removeFromSubTable(TABLE_HISTORY, id);
    }

    public void removeFromFavor(int id) {
        removeFromSubTable(TABLE_FAVOR, id);
    }

    public void removeFromToShow(int id) {
        removeFromSubTable(TABLE_TOSHOW, id);
    }

    private void removeFromSubTable(String tableName, int id) {
        synchronized (this) {
            SQLiteDatabase sqLiteDatabase = getWritableDatabase();

            if (id <= 0) {
                return;
            }
            sqLiteDatabase.execSQL("DELETE FROM " + tableName + " WHERE " + KEY_ONEWORD_ID + "=?", new Object[]{id});

        }
    }


    public void clearToShow() {
        clearSubTable(TABLE_TOSHOW);
    }

    private void clearSubTable(String tableName) {
        synchronized (this) {
            SQLiteDatabase sqLiteDatabase = getWritableDatabase();

            sqLiteDatabase.execSQL("DELETE FROM " + tableName + " WHERE 1");
        }
    }

    public int countHistory() {
        return countTable(TABLE_HISTORY);
    }

    public int countFavor() {
        return countTable(TABLE_FAVOR);
    }

    public int countToShow() {
        return countTable(TABLE_TOSHOW);
    }

    private int countTable(String tableName) {
        synchronized (this) {

            SQLiteDatabase sqLiteDatabase = getReadableDatabase();

            Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM " + tableName, null);

            int t = cursor.getCount();
            cursor.close();
            return t;
        }
    }

//    public long remove_one_item(String table_name, HitokotoBean hitokotoBean) {
//        if (hitokotoBean == null) {
//            return -1;
//        }
//        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
//
//        long t = sqLiteDatabase.delete(table_name, KEY_ID + " = ? and " + KEY_TYPE + " = ?", new String[]{hitokotoBean.id + "", hitokotoBean.type});
//        return t;
//    }

    public WordBean queryOneWordFromHistoryByRandom() {
        return queryOneWordFromSubTableByRandom(TABLE_HISTORY, "random()");
    }


    public WordBean queryOneWordFromFavorByRandom() {
        return queryOneWordFromSubTableByRandom(TABLE_FAVOR, "random()");
    }


    public WordBean queryOneWordFromHistoryByDESC() {
        return queryOneWordFromSubTableByRandom(TABLE_HISTORY, KEY_ADDED_AT + " DESC");
    }


    public WordBean queryOneWordFromToShowByASC() {
        return queryOneWordFromSubTableByRandom(TABLE_TOSHOW, KEY_ADDED_AT + " ASC");
    }

    private WordBean queryOneWordFromSubTableByRandom(String tableName, String order) {
        synchronized (this) {

            SQLiteDatabase sqLiteDatabase = getReadableDatabase();
            Cursor cursor = sqLiteDatabase.rawQuery("SELECT " + KEY_ONEWORD_ID + " FROM " + tableName + " ORDER BY " + order + " LIMIT 1", null);

            WordBean result = null;

            if (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                result = queryOneWordInAllOneWordById(id);
            }
            cursor.close();

            return result;
        }
    }


    public ArrayList<WordBean> querySomeOneWordFromHistory(int start, int num) {
        return querySomeOneWordFromSubTable(TABLE_HISTORY, start, num);
    }

    public ArrayList<WordBean> querySomeOneWordFromFavor(int start, int num) {
        return querySomeOneWordFromSubTable(TABLE_FAVOR, start, num);
    }

    public ArrayList<WordBean> querySomeOneWordFromToShow(int start, int num) {
        return querySomeOneWordFromSubTable(TABLE_TOSHOW, start, num);
    }

    private ArrayList<WordBean> querySomeOneWordFromSubTable(String tableName, int start, int num) {
        synchronized (this) {
            SQLiteDatabase sqLiteDatabase = getReadableDatabase();

            Cursor cursor = sqLiteDatabase.rawQuery("SELECT " + KEY_ONEWORD_ID + " FROM " + tableName + " ORDER BY " + KEY_ADDED_AT + " DESC LIMIT ? OFFSET ?", new String[]{String.valueOf(num), String.valueOf(start)});
            ArrayList<WordBean> wordBeans = new ArrayList<>(num);

            while (cursor.moveToNext()) {
                wordBeans.add(queryOneWordInAllOneWordById(cursor.getInt(0)));
            }

            cursor.close();

            return wordBeans;
        }
    }

    public ArrayList<ApiBean> queryAllApi() {
        synchronized (this) {
            SQLiteDatabase sqLiteDatabase = getReadableDatabase();

            Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM " + TABLE_API + " ORDER BY " + KEY_ID, new String[0]);

            ArrayList<ApiBean> apiBeans = new ArrayList<>();

            while (cursor.moveToNext()) {
                apiBeans.add(new ApiBean(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getInt(6) == 1));
            }
            cursor.close();

            return apiBeans;
        }
    }


    public ApiBean queryApiById(int id) {
        if (id <= 0) {
            return null;
        }
        synchronized (this) {
            SQLiteDatabase sqLiteDatabase = getReadableDatabase();

            Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM " + TABLE_API + " WHERE " + KEY_ID + " =?", new String[]{String.valueOf(id)});

            ApiBean apiBean = null;

            if (cursor.moveToNext()) {
                apiBean = new ApiBean(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getInt(6) == 1);
            }
            cursor.close();

            return apiBean;
        }
    }

    public ApiBean queryAEnabledApiRandom() {
        synchronized (this) {
            SQLiteDatabase sqLiteDatabase = getReadableDatabase();

            Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM " + TABLE_API + " ORDER BY random()", new String[0]);

            ApiBean apiBean = null;

            while (cursor.moveToNext()) {
                if (cursor.getInt(6) == 1) {
                    apiBean = new ApiBean(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getInt(6) == 1);
                    break;
                }
            }

            cursor.close();
            return apiBean;
        }
    }

    /**
     * 插入一条API，冲突处理方式为Replace
     *
     * @return
     */
    public void inserAApi(ApiBean apiBean) {
        if (apiBean == null) {
            return;
        }

        synchronized (this) {
            SQLiteDatabase sqLiteDatabase = getWritableDatabase();

            ContentValues contentValues = new ContentValues();
            if (apiBean.id > 0) {
                contentValues.put(KEY_ID, apiBean.id);
            }
            contentValues.put(KEY_NAME, apiBean.name);
            contentValues.put(KEY_URL, apiBean.url);
            if (!TextUtils.isEmpty(apiBean.name)) {
                contentValues.put(KEY_REQ_METHOD, apiBean.req_method);
            }
            contentValues.put(KEY_REQ_ARGS_JSON, apiBean.req_args_json);
            contentValues.put(KEY_RESP_FORM, apiBean.resp_form);
            contentValues.put(KEY_ENABLED, apiBean.enabled);

            sqLiteDatabase.insertWithOnConflict(TABLE_API, null, contentValues, CONFLICT_REPLACE);

        }
    }


    public void removeApiById(int id) {
        if (id <= 0) {
            return;
        }
        synchronized (this) {
            SQLiteDatabase sqLiteDatabase = getWritableDatabase();

            sqLiteDatabase.execSQL("DELETE FROM " + TABLE_API + " WHERE " + KEY_ID + " =?", new Object[]{id});

        }
    }


    @Override
    public void onConfigure(SQLiteDatabase db) {
        // 开启外键支持
        db.execSQL("PRAGMA foreign_keys = on");

    }

    //第一次创建数据库时被调用
    @Override
    public void onCreate(SQLiteDatabase db) {


        db.execSQL("BEGIN TRANSACTION");

        db.execSQL("CREATE TABLE all_oneword (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL UNIQUE CHECK (id > 0), content TEXT, reference TEXT, target_url TEXT, target_text TEXT)");
        db.execSQL("CREATE TABLE api (id INTEGER PRIMARY KEY UNIQUE NOT NULL CHECK (id > 0), name TEXT NOT NULL, url TEXT NOT NULL, req_method TEXT NOT NULL DEFAULT 'GET', req_args_json TEXT, resp_form TEXT , enabled BOOLEAN NOT NULL DEFAULT 1)");
        db.execSQL("CREATE TABLE favor (oneword_id INTEGER REFERENCES all_oneword (id) ON DELETE CASCADE UNIQUE NOT NULL, added_at INTEGER NOT NULL COLLATE BINARY)");
        db.execSQL("CREATE TABLE history (oneword_id INTEGER REFERENCES all_oneword (id) ON DELETE CASCADE NOT NULL UNIQUE, added_at INTEGER COLLATE BINARY NOT NULL)");
        db.execSQL("CREATE TABLE toshow (oneword_id INTEGER REFERENCES all_oneword (id) ON DELETE CASCADE UNIQUE NOT NULL, added_at INTEGER NOT NULL COLLATE BINARY)");
        db.execSQL("CREATE INDEX index_alloneword_content_reference_sourceurl ON all_oneword (content, reference)");
        db.execSQL("CREATE INDEX index_history_addedat ON history (added_at DESC)");
        db.execSQL("CREATE INDEX index_history_onewordid ON history (oneword_id)");
        db.execSQL("CREATE INDEX index_like_addedat ON favor (added_at DESC)");
        db.execSQL("CREATE INDEX index_like_oneowrdid ON favor (oneword_id)");
        db.execSQL("CREATE INDEX index_toshow_addedat ON toshow (added_at DESC)");
        db.execSQL("CREATE INDEX index_toshow_onewordid ON toshow (oneword_id)");

        db.execSQL("CREATE TRIGGER clear_toshow_when_api_deleted AFTER DELETE ON api FOR EACH ROW BEGIN DELETE FROM toshow; END");
        db.execSQL("CREATE TRIGGER clear_toshow_when_api_enable_updated AFTER UPDATE OF enabled ON api FOR EACH ROW BEGIN DELETE FROM toshow; END");

        db.execSQL("COMMIT TRANSACTION");

        db.execSQL("INSERT INTO api(name,url,req_method,req_args_json,resp_form,enabled) VALUES('Hitokoto-动漫', 'https://v1.hitokoto.cn', 'GET', '{\"c\":\"a\"}', '{\n  \"hitokoto\": \"[content]\",\n  \"from\": \"[reference]\"\n}', 1)");
        db.execSQL("INSERT INTO api(name,url,req_method,req_args_json,resp_form,enabled) VALUES('Hitokoto-漫画', 'https://v1.hitokoto.cn', 'GET', '{\"c\":\"b\"}', '{\n  \"hitokoto\": \"[content]\",\n  \"from\": \"[reference]\"\n}', 1)");
        db.execSQL("INSERT INTO api(name,url,req_method,req_args_json,resp_form,enabled) VALUES('Hitokoto-游戏', 'https://v1.hitokoto.cn', 'GET', '{\"c\":\"c\"}', '{\n  \"hitokoto\": \"[content]\",\n  \"from\": \"[reference]\"\n}', 1)");
        db.execSQL("INSERT INTO api(name,url,req_method,req_args_json,resp_form,enabled) VALUES('Hitokoto-小说', 'https://v1.hitokoto.cn', 'GET', '{\"c\":\"d\"}', '{\n  \"hitokoto\": \"[content]\",\n  \"from\": \"[reference]\"\n}', 1)");
        db.execSQL("INSERT INTO api(name,url,req_method,req_args_json,resp_form,enabled) VALUES('Hitokoto-原创', 'https://v1.hitokoto.cn', 'GET', '{\"c\":\"e\"}', '{\n  \"hitokoto\": \"[content]\",\n  \"from\": \"[reference]\"\n}', 1)");
        db.execSQL("INSERT INTO api(name,url,req_method,req_args_json,resp_form,enabled) VALUES('Hitokoto-来自网络', 'https://v1.hitokoto.cn', 'GET', '{\"c\":\"f\"}', '{\n  \"hitokoto\": \"[content]\",\n  \"from\": \"[reference]\"\n}', 1)");
        db.execSQL("INSERT INTO api(name,url,req_method,req_args_json,resp_form,enabled) VALUES('Hitokoto-其他', 'https://v1.hitokoto.cn', 'GET', '{\"c\":\"g\"}', '{\n  \"hitokoto\": \"[content]\",\n  \"from\": \"[reference]\"\n}', 1)");
        db.execSQL("INSERT INTO api(name,url,req_method,req_args_json,resp_form,enabled) VALUES('yiju-一句', 'http://yiju.ml/api/word.php', 'GET', '', '——', 1)");

        insertInternalApi_v5(db);

    }


    //数据库版本发生更新后调用
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        switch (oldVersion) {
            case 1:// 第一版

                db.execSQL("ALTER TABLE history RENAME TO old_history");
                db.execSQL("ALTER TABLE [like] RENAME TO old_like");
                db.execSQL("ALTER TABLE ready_to_show RENAME TO old_ready_to_show");

                this.onCreate(db);


                // 处理旧的history表的数据
                Cursor cursor = db.rawQuery("SELECT * FROM old_history;", new String[0]);

                ContentValues contentValues_all = new ContentValues();

                ContentValues contentValues_history = new ContentValues();
                ContentValues contentValues_favor = new ContentValues();

                while (cursor.moveToNext()) {
                    contentValues_all.put(KEY_CONTENT, nullToVoid(cursor.getString(1)));
                    contentValues_all.put(KEY_REFERENCE, nullToVoid(cursor.getString(3)));

                    // 避免重复
                    Cursor cursor_0 = db.rawQuery("SELECT " + KEY_ID + " FROM " + TABLE_ALL_ONEWORD + " WHERE " + KEY_CONTENT + "=? AND " + KEY_REFERENCE + "=?", new String[]{
                            nullToVoid(cursor.getString(1)), nullToVoid(cursor.getString(3))
                    });

                    if (cursor_0.getCount() == 0) {
                        long id = db.insertWithOnConflict(TABLE_ALL_ONEWORD, null, contentValues_all, CONFLICT_IGNORE);

                        contentValues_history.put(KEY_ONEWORD_ID, id);
                        contentValues_history.put(KEY_ADDED_AT, cursor.getLong(6));

                        db.insertWithOnConflict(TABLE_HISTORY, null, contentValues_history, CONFLICT_REPLACE);

                        if (cursor.getInt(7) == 1) {// 如果点过喜欢

                            Cursor cursor_1 = db.rawQuery("SELECT * FROM old_like WHERE id=? AND type=?", new String[]{
                                    String.valueOf(cursor.getInt(0)), cursor.getString(2)
                            });

                            if (cursor_1.moveToNext()) {// 在旧的like表中存在

                                contentValues_favor.put(KEY_ONEWORD_ID, id);
                                contentValues_favor.put(KEY_ADDED_AT, cursor_1.getLong(6));

                                db.insertWithOnConflict(TABLE_FAVOR, null, contentValues_favor, CONFLICT_REPLACE);

                            }

                            cursor_1.close();
                        }
                    }
                    cursor_0.close();
                }

                cursor.close();


                db.execSQL("DROP TABLE old_history");
                db.execSQL("DROP TABLE old_like");
                db.execSQL("DROP TABLE old_ready_to_show");


                break;
            case 2:
            case 3:
            case 4:
                db.execSQL("ALTER TABLE all_oneword ADD COLUMN target_text TEXT");
                insertInternalApi_v5(db);
        }

    }


    private static void insertInternalApi_v5(SQLiteDatabase db) {

        db.execSQL("INSERT INTO api(name,url,req_method,req_args_json,resp_form,enabled) VALUES('网易云音乐歌评-华语', 'http://w4y.imlk.top/thin_api', 'GET', '{\"cat\":\"a\"}', '{\n    \"content\": \"[content]\",\n    \"reference\": \"[reference]\",\n    \"target_url\": \"[target_url]\",\n    \"target_text\": \"[target_text]\"\n}', 1)");
        db.execSQL("INSERT INTO api(name,url,req_method,req_args_json,resp_form,enabled) VALUES('网易云音乐歌评-流行', 'http://w4y.imlk.top/thin_api', 'GET', '{\"cat\":\"b\"}', '{\n    \"content\": \"[content]\",\n    \"reference\": \"[reference]\",\n    \"target_url\": \"[target_url]\",\n    \"target_text\": \"[target_text]\"\n}', 1)");
        db.execSQL("INSERT INTO api(name,url,req_method,req_args_json,resp_form,enabled) VALUES('网易云音乐歌评-摇滚', 'http://w4y.imlk.top/thin_api', 'GET', '{\"cat\":\"c\"}', '{\n    \"content\": \"[content]\",\n    \"reference\": \"[reference]\",\n    \"target_url\": \"[target_url]\",\n    \"target_text\": \"[target_text]\"\n}', 1)");
        db.execSQL("INSERT INTO api(name,url,req_method,req_args_json,resp_form,enabled) VALUES('网易云音乐歌评-民谣', 'http://w4y.imlk.top/thin_api', 'GET', '{\"cat\":\"d\"}', '{\n    \"content\": \"[content]\",\n    \"reference\": \"[reference]\",\n    \"target_url\": \"[target_url]\",\n    \"target_text\": \"[target_text]\"\n}', 1)");
        db.execSQL("INSERT INTO api(name,url,req_method,req_args_json,resp_form,enabled) VALUES('网易云音乐歌评-电子', 'http://w4y.imlk.top/thin_api', 'GET', '{\"cat\":\"e\"}', '{\n    \"content\": \"[content]\",\n    \"reference\": \"[reference]\",\n    \"target_url\": \"[target_url]\",\n    \"target_text\": \"[target_text]\"\n}', 1)");
        db.execSQL("INSERT INTO api(name,url,req_method,req_args_json,resp_form,enabled) VALUES('网易云音乐歌评-轻音乐', 'http://w4y.imlk.top/thin_api', 'GET', '{\"cat\":\"f\"}', '{\n    \"content\": \"[content]\",\n    \"reference\": \"[reference]\",\n    \"target_url\": \"[target_url]\",\n    \"target_text\": \"[target_text]\"\n}', 1)");
        db.execSQL("INSERT INTO api(name,url,req_method,req_args_json,resp_form,enabled) VALUES('网易云音乐歌评-影视原声', 'http://w4y.imlk.top/thin_api', 'GET', '{\"cat\":\"g\"}', '{\n    \"content\": \"[content]\",\n    \"reference\": \"[reference]\",\n    \"target_url\": \"[target_url]\",\n    \"target_text\": \"[target_text]\"\n}', 1)");
        db.execSQL("INSERT INTO api(name,url,req_method,req_args_json,resp_form,enabled) VALUES('网易云音乐歌评-ACG', 'http://w4y.imlk.top/thin_api', 'GET', '{\"cat\":\"h\"}', '{\n    \"content\": \"[content]\",\n    \"reference\": \"[reference]\",\n    \"target_url\": \"[target_url]\",\n    \"target_text\": \"[target_text]\"\n}', 1)");
        db.execSQL("INSERT INTO api(name,url,req_method,req_args_json,resp_form,enabled) VALUES('网易云音乐歌评-夜晚', 'http://w4y.imlk.top/thin_api', 'GET', '{\"cat\":\"i\"}', '{\n    \"content\": \"[content]\",\n    \"reference\": \"[reference]\",\n    \"target_url\": \"[target_url]\",\n    \"target_text\": \"[target_text]\"\n}', 1)");
        db.execSQL("INSERT INTO api(name,url,req_method,req_args_json,resp_form,enabled) VALUES('网易云音乐歌评-学习', 'http://w4y.imlk.top/thin_api', 'GET', '{\"cat\":\"j\"}', '{\n    \"content\": \"[content]\",\n    \"reference\": \"[reference]\",\n    \"target_url\": \"[target_url]\",\n    \"target_text\": \"[target_text]\"\n}', 1)");
        db.execSQL("INSERT INTO api(name,url,req_method,req_args_json,resp_form,enabled) VALUES('网易云音乐歌评-运动', 'http://w4y.imlk.top/thin_api', 'GET', '{\"cat\":\"k\"}', '{\n    \"content\": \"[content]\",\n    \"reference\": \"[reference]\",\n    \"target_url\": \"[target_url]\",\n    \"target_text\": \"[target_text]\"\n}', 1)");
        db.execSQL("INSERT INTO api(name,url,req_method,req_args_json,resp_form,enabled) VALUES('网易云音乐歌评-怀旧', 'http://w4y.imlk.top/thin_api', 'GET', '{\"cat\":\"l\"}', '{\n    \"content\": \"[content]\",\n    \"reference\": \"[reference]\",\n    \"target_url\": \"[target_url]\",\n    \"target_text\": \"[target_text]\"\n}', 1)");
        db.execSQL("INSERT INTO api(name,url,req_method,req_args_json,resp_form,enabled) VALUES('网易云音乐歌评-清新', 'http://w4y.imlk.top/thin_api', 'GET', '{\"cat\":\"m\"}', '{\n    \"content\": \"[content]\",\n    \"reference\": \"[reference]\",\n    \"target_url\": \"[target_url]\",\n    \"target_text\": \"[target_text]\"\n}', 1)");
        db.execSQL("INSERT INTO api(name,url,req_method,req_args_json,resp_form,enabled) VALUES('网易云音乐歌评-治愈', 'http://w4y.imlk.top/thin_api', 'GET', '{\"cat\":\"n\"}', '{\n    \"content\": \"[content]\",\n    \"reference\": \"[reference]\",\n    \"target_url\": \"[target_url]\",\n    \"target_text\": \"[target_text]\"\n}', 1)");

    }

    private static String nullToVoid(String str) {
        return str == null ? "" : str;
    }

}
