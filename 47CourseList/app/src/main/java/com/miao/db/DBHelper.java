package com.miao.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.miao.db.dao.CourseInfoDao;

public class DBHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "miao_db.db";
	private static final int DATABASE_VERSION = 1;
	
	public DBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	//数据库第一次被创建时onCreate会被调用
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS global_info" +		// 全局信息表
				"(key TEXT PRIMARY KEY, " +
				"value TEXT)");

		db.execSQL("CREATE TABLE IF NOT EXISTS global_url" +		// 全局URL表
				"(key TEXT PRIMARY KEY, " +
				"value TEXT)");
		
		db.execSQL("CREATE TABLE IF NOT EXISTS user_msg" +			// 用户信息表
				"(uid INTEGER, " +                                  // 用户ID
				"username TEXT, " +									//用户名
				"password TEXT," +									// 密码
				"gender TEXT, " +									// 性别
				"phone TEXT, " +									// 电话
				"school TEXT, "+                                     //学校
				"institute TEXT, " +								// 学院
				"major TEXT)"    								    // 专业
				);


		db.execSQL("CREATE TABLE IF NOT EXISTS course_info" +		// 课程信息表
				"(cid INTEGER, " +		// 课程ID
				"weekfrom INTEGER, " +		// 开始周次
				"weekto INTEGER, " +		// 结束周次
				"weektype INTEGER, " +		// 周次类型
				"day INTEGER, " +			// 星期几
				"lessonfrom INTEGER, " +	// 节次开始
				"lessonto INTEGER, " +		// 节次结束
				"coursename TEXT, " +				// 课程名
				"teacher TEXT, " +			// 教师
				"place TEXT )" 			// 地点
				);
				
		db.execSQL("CREATE TABLE IF NOT EXISTS user_course" +		// 课程记录表
				"(uid INTEGER, " +			// 用户ID
				"cid INTEGER )"				// 课程ID	
				);
		CourseInfoDao.setCount(0);
	}

	//如果DATABASE_VERSION值被改为2,系统发现现有数据库版本不同,即会调用onUpgrade
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}
}

