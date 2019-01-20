package com.miao.db.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.miao.baseInfo.UserInfo;
import com.miao.db.DBHelper;

public class UserInfoDao {
	private DBHelper dbHelper;
	private SQLiteDatabase db;
	
	public UserInfoDao(Context context) {
		dbHelper = new DBHelper(context);
		db = dbHelper.getWritableDatabase();
	}

	public int insert(UserInfo uInfo) {
		try {
            UserInfo uInfoNew = query(uInfo.getUid());
            if(uInfoNew!=null)//如果有该记录
            {
                uInfo.setUid(uInfoNew.getUid());
                if(update(uInfo)) {//有记录就更新
                    return uInfo.getUid();
                }
                else {//更新失败
                    return 0;
                }
            }
            //没有该记录，就创建新的记录
			String sql = "INSERT INTO user_msg (uid, username, password, gender, phone, school, institute, major) " +
					"VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
			Log.d("text_view","添加数据");
			db.execSQL(sql, new Object[] {
				uInfo.getUid(),
				uInfo.getUsername(),
				uInfo.getPassword(),
				uInfo.getGender(),
				uInfo.getPhone(),
				uInfo.getSchool(),
				uInfo.getInstitute(),
				uInfo.getMajor()
			});
            Cursor c1 = db.rawQuery("SELECT last_insert_rowid()", null);
            c1.moveToFirst();
            int id = c1.getInt(c1.getColumnIndex("last_insert_rowid()"));
            c1.close();
			return id;
		} catch (Exception e) {
			return 0;
		}

	}

	public boolean delete(int uid) {
		try {
			String sql = "DELETE FROM user_msg WHERE uid = ?";
			db.execSQL(sql, new Object[] {uid});
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	

	public boolean delete(String username) {
		try {
			String sql = "DELETE FROM user_msg WHERE username = ?";
			db.execSQL(sql, new Object[] {username});
			return true;
		} catch (Exception e) {
			return false;
		}
	}


	public boolean update(UserInfo uInfo) {
		if (uInfo == null) {
			return false;
		}
		if (uInfo.getUid() == 0) {
			return false;
		}
		try {
			String sql = "UPDATE user_msg SET username=?, password=?,gender=?, phone=?, school=?, institute=?, major=?" +
					"WHERE uid=?";
			db.execSQL(sql, new Object[] {
				uInfo.getUsername(),
				uInfo.getPassword(),
				uInfo.getGender(),
				uInfo.getPhone(),
				uInfo.getSchool(),
                uInfo.getInstitute(),
                uInfo.getMajor(),
				uInfo.getUid()
			});

			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public UserInfo query(int uid) {
		try {
			UserInfo uInfo = new UserInfo();
			String sql = "SELECT * FROM user_msg WHERE uid="+uid;
			//Log.d("StartActivity",sql);
			Cursor c = db.rawQuery(sql, null);
			if (c.getCount() == 0) {
				return null;
			}
			else {
				c.moveToFirst();
				uInfo.setUid(uid);
				uInfo.setUsername(c.getString(c.getColumnIndex("username")));
				uInfo.setPassword(c.getString(c.getColumnIndex("password")));
				uInfo.setGender(c.getString(c.getColumnIndex("gender")));
				uInfo.setPhone(c.getString(c.getColumnIndex("phone")));
				uInfo.setSchool(c.getString(c.getColumnIndex("school")));
                uInfo.setInstitute(c.getString(c.getColumnIndex("institute")));
                uInfo.setMajor(c.getString(c.getColumnIndex("major")));
				return uInfo;
			}
		} catch (Exception e) {
			return null;
		}
	}

}
