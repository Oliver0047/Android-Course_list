package com.miao.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.miao.R;
import com.miao.baseInfo.GlobalInfo;
import com.miao.baseInfo.UserInfo;
import com.miao.db.dao.GlobalInfoDao;
import com.miao.db.dao.UserInfoDao;

import java.util.Calendar;


public class StartActivity extends Activity {

	private UserInfoDao uInfoDao;
	private UserInfo uInfo;
	private GlobalInfoDao gInfoDao;
	private GlobalInfo gInfo;
	private static Context context;
	private final int SPLASH_DISPLAY_LENGHT = 0;

	private long timeStart;
    private int uid;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		timeStart = System.currentTimeMillis();
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start);

		// 获取上下文
		context = getApplicationContext();

		// 初始化Dao成员变量
		gInfoDao = new GlobalInfoDao(context);
        uInfoDao = new UserInfoDao(context);

		// 初始化数据模型变量
		gInfo = gInfoDao.query();
		uInfo=LoginActivity.person_login;
		// 自定义函数
		initGInfo(context);
		// 如果初始化消耗的时间小于预定时间
		long timeInit = System.currentTimeMillis()-timeStart;
		if (timeInit < SPLASH_DISPLAY_LENGHT) {
			new Handler().postDelayed(new Runnable(){
		         @Override
		         public void run() {
		        	 jumpToMain();
		         }
		    }, SPLASH_DISPLAY_LENGHT-timeInit);
		}
		else {
            jumpToMain();
		}

	}

	@Override
    protected void onPause() {
       super.onPause(); 
    }
    
    @Override
    protected void onStop() {
    	super.onStop();
    }
    
    @Override
    protected void onResume() {
        super.onResume(); 
    }

	private void initGInfo(Context context) {
		gInfo=null;
    	if (gInfo == null) {//第一次使用app

	    	int version = 0;
	    	String vsersionStr = "";
			try {
				PackageInfo pi=context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
				version = pi.versionCode;
				vsersionStr = pi.versionName;
			} catch (Exception e) {
				version = 1;
				vsersionStr = "1.0";
			}
			
			Calendar calendar = Calendar.getInstance();
			int month = calendar.get(Calendar.MONTH)+1;
			int year = calendar.get(Calendar.YEAR);

			gInfo = new GlobalInfo();
			gInfo.setVersion(version);
			gInfo.setVersionStr(vsersionStr);
			
			// 初始化时默认的开学时间，后面可以加上修改时间模块
			if (month<=6 && month>=2) {
				gInfo.setTermBegin("2018-03-01");
			}
			else
			{
				gInfo.setTermBegin("2018-09-01");
			}

			// 下半学期
			if (month < 8) {
				gInfo.setYearFrom(year-1);
				gInfo.setYearTo(year);
				gInfo.setTerm(2);
			}
			// 上半学期
			else {
				gInfo.setYearFrom(year);
				gInfo.setYearTo(year+1);
				gInfo.setTerm(1);
			}
			gInfo.setFirstUse(1);//设为1则为是第一次使用
            uid = uInfo.getUid();
            gInfo.setActiveUserUid(uid);
            gInfoDao.insert(gInfo); //插入global_info表
            uInfoDao.insert(uInfo); //插入user_info表
            Log.v("StartActivity",String.valueOf(uid));

		}
    }
	
	private void jumpToMain() {
		Intent intent = new Intent(StartActivity.this, CourseActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
        finish();
	}
}
