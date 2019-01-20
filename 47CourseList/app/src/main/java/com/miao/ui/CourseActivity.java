package com.miao.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import jxl.Sheet;
import jxl.Workbook;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

import com.miao.R;
import com.miao.baseInfo.CourseInfo;
import com.miao.baseInfo.GlobalInfo;
import com.miao.baseInfo.UserCourse;
import com.miao.baseInfo.UserInfo;
import com.miao.common.Utility;
import com.miao.db.dao.CourseInfoDao;
import com.miao.db.dao.GlobalInfoDao;
import com.miao.db.dao.UserCourseDao;
import com.miao.db.dao.UserInfoDao;
import com.miao.ui.adapter.CourseInfoAdapter;
import com.miao.ui.adapter.InfoGallery;
import com.miao.ui.widget.BorderTextView;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CourseActivity extends FragmentActivity implements View.OnClickListener {

	private static Context context;
    private int status=0;
	private final int dayCourseNum = 12;
    private DrawerLayout mDrawerLayout;
	private ProgressDialog progressDialog;

    protected View menuView;
    protected ImageView headshotView;
    protected TextView nameTextView;
    protected TextView insTextView;
    protected TextView majorTextView;
    protected View refreshView;
    protected TextView logoutTextView;
    protected TextView dateTextView;//当前日期
    protected TextView weekTextView;//标题栏周数
    protected ListView weekListView;//显示周数的ListView
    protected PopupWindow weekListWindow;//选择周数弹出窗口
    protected View popupWindowLayout;//选择周数弹出窗口Layout

    private TextView weekDaysTextView[];
    protected TextView empty;//第一个无内容的格子,用于定位
    protected RelativeLayout table_layout;//课程表body部分布局

    GlobalInfoDao gInfoDao;
    UserInfoDao uInfoDao;

	CourseInfoDao cInfoDao;
	UserCourseDao uCourseDao;

	GlobalInfo gInfo;//需要isFirstUse和activeUserUid
	UserInfo uInfo;//需要username昵称,gender，phone，headshot，institute，major，year

	private LinkedList<CourseInfo> courseInfoList;//课程信息链表，存储有包括cid在内的完整信息
    private Map<String, List<CourseInfo>> courseInfoMap;//课程信息，key为星期几，value是这一天的课程信息

    private List<TextView> courseTextViewList;//保存显示课程信息的TextView
    private Map<Integer, List<CourseInfo>> textviewCourseInfoMap;//保存每个textview对应的课程信息 map,key为哪一天（如星期一则key为1）

	private int uid;
    private SharedPreferences courseSettings; //课程信息设置
    private int cw;//存储当前选择的周数currentWeek
	private int currWeek;//储存当前周

    protected int aveWidth;//课程格子平均宽度
    protected int screenWidth;//屏幕宽度
    protected int gridHeight = 80;//格子高度
    protected static HashMap<String,Integer> Big2Digit;
    protected CourseInfo courseInfo;
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_course);

		context = getApplicationContext();
        Log.d("text_view","进入Course");
        gInfoDao = new GlobalInfoDao(context);
        uInfoDao = new UserInfoDao((context));
		cInfoDao = new CourseInfoDao(context);
		uCourseDao = new UserCourseDao(context);
        courseInfo = new CourseInfo();

		// 初始化数据模型变量
		gInfo = gInfoDao.query();
        uid = gInfo.getActiveUserUid();
        Log.d("text_view","查询性别");
        uInfo = uInfoDao.query(uid);
        Log.d("text_view",uInfo.getGender());
        courseInfoList = new LinkedList<CourseInfo>();
        courseTextViewList = new ArrayList<TextView>();
        textviewCourseInfoMap = new HashMap<Integer, List<CourseInfo>>();
        currWeek = Utility.getWeeks(gInfo.getTermBegin());
        cw = Utility.getWeeks(gInfo.getTermBegin());

        //获取课表配置信息
        courseSettings = getSharedPreferences("course_setting", MODE_PRIVATE);
        Big2Digit=new HashMap<String,Integer>();
        Big2Digit.put("星期一",1);
        Big2Digit.put("星期二",2);
        Big2Digit.put("星期三",3);
        Big2Digit.put("星期四",4);
        Big2Digit.put("星期五",5);
        Big2Digit.put("星期六",6);
        Big2Digit.put("星期日",7);
        // 自定义函数
        initMenu();
        initDate();//显示在menu中的当前日期
        initView();//初始化CourseActivity界面
        initTable();//初始化课表
        initListener();//初始化事件监听器
        try {
            refresh();//刷新课表信息
        } catch (IOException e) {
            e.printStackTrace();
        } catch (BiffException e) {
            e.printStackTrace();
        }
    }


    //长按对话框
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.add(1,1000,0,"添加课程");
        menu.add(1,1001,1,"关于47号课程表");
        super.onCreateContextMenu(menu, v, menuInfo);
    }
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1000:
                CourseAdd();
                break;
            case 1001:
                Toast.makeText(this, "【制作人】：陈**、章*", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
        return super.onContextItemSelected(item);
    }

    private void CourseAdd() {
        Intent intent = new Intent();
        Bundle mBundle = new Bundle();
        CourseInfo temp = new CourseInfo();

        temp.setCid(CourseInfoDao.getCount()+1);
        temp.setCoursename("");
        temp.setWeektype(2);
        temp.setTeacher("");
        temp.setWeekfrom(1);
        temp.setWeekto(20);
        temp.setLessonfrom(1);
        temp.setLessonto(12);
        temp.setDay(1);
        temp.setPlace("南1-B206");
        mBundle.putSerializable("courseInfo", temp);
        mBundle.putSerializable("uid",uid);
        intent.putExtras(mBundle);
        intent.setClass(CourseActivity.this, EditActivity.class);
        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
        startActivity(intent);
        finish();
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(mDrawerLayout.isDrawerOpen(GravityCompat.START)){
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }

        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {

    }
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(mDrawerLayout.isDrawerOpen(GravityCompat.START)){
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }
		return super.onKeyDown(keyCode, event);
	}

	//初始化侧滑菜单
    private void initMenu() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navView = (NavigationView) findViewById(R.id.nav_view);
        headshotView = (ImageView) navView.getHeaderView(0).findViewById(R.id.icon_image);
        if(uInfo.getGender().equals("女")){
            headshotView.setImageResource(R.drawable.nav_icon_female);
        }
        else{
            headshotView.setImageResource(R.drawable.nav_icon_male);
        }
        nameTextView = (TextView)navView.getHeaderView(0).findViewById(R.id.Menu_main_name);
        nameTextView.setText(uInfo.getUsername()+"，你好");
        insTextView = (TextView)navView.getHeaderView(0).findViewById(R.id.Menu_main_institute);
        insTextView.setText(uInfo.getInstitute());
        majorTextView = (TextView)navView.getHeaderView(0).findViewById(R.id.Menu_main_major);
        majorTextView.setText(uInfo.getMajor());
        dateTextView = (TextView) navView.getHeaderView(0).findViewById(R.id.Menu_main_textDate);
        logoutTextView=(TextView) navView.getHeaderView(0).findViewById(R.id.logout_view);
        logoutTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CourseActivity.this, LoginActivity.class);
                Bundle mBundle = new Bundle();
                mBundle.putSerializable("logout","true");
                intent.putExtras(mBundle);
                startActivity(intent);
                finish();
            }
        });
        if (uid!=528) {
            navView.getMenu().getItem(5).setVisible(false);
        }
        else
        {
            navView.getMenu().getItem(5).setVisible(true);
        }
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.excelin_setting:
                        try {
                            jumpToCourseIn();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (BiffException e) {
                            e.printStackTrace();
                        }
                        mDrawerLayout.closeDrawer(GravityCompat.START);
                        Toast.makeText(CourseActivity.this, "导入成功", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.excelout_setting:
                        try {
                            jumpToCourseOut();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (WriteException e) {
                            e.printStackTrace();
                        }
                        mDrawerLayout.closeDrawer(GravityCompat.START);
                        Toast.makeText(CourseActivity.this, "导出成功:/data/data/com.miao/cache/", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.day_setting:
                        initTable_today();
                        mDrawerLayout.closeDrawer(GravityCompat.START);
                        status=1;
                        break;
                    case R.id.week_setting:
                        Intent intent = new Intent(CourseActivity.this,CourseActivity.class);
                        startActivity(intent);
                        finish();
                        mDrawerLayout.closeDrawer(GravityCompat.START);
                        status=0;
                        break;
                    case R.id.blog_looking:
                        Uri uri = Uri.parse("http://www.chenhn.club");
                        Intent intent1 = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent1);
                        break;
                    case R.id.blog_writing:
                        Uri uri1 = Uri.parse("http://www.chenhn.club/admin/");
                        Intent intent2 = new Intent(Intent.ACTION_VIEW, uri1);
                        startActivity(intent2);
                        break;
                    default:
                }
                return true;
            }
        });

    }

    //初始化日期
    @SuppressLint("SimpleDateFormat")
    private void initDate() {
        Date currentTime = new Date();
        String[] weekDays = {"日", "一", "二", "三", "四", "五", "六"};
        Calendar cal = Calendar.getInstance();
        cal.setTime(currentTime);
        int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (w < 0)  w = 0;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日  ");
        String dateString = formatter.format(currentTime);
        dateTextView.setText(dateString + "星期" + weekDays[w]);
    }

    //获取现在或者接下来的课程信息
    private String get_course(int i) {
        Log.d("text_view","进入获取课程信息");
        double now;
        Date currentTime = new Date();
        int[] weekDays = {7,1,2,3, 4, 5,6 };
        Calendar cal = Calendar.getInstance();
        cal.setTime(currentTime);
        int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
        List<CourseInfo> today = new LinkedList<CourseInfo>();
        today= courseInfoMap.get(String.valueOf(weekDays[w]));
        int h=cal.get(Calendar.HOUR_OF_DAY);
        int m=cal.get(Calendar.MINUTE);
        Log.d("text_view",h+":"+m);
        now=judgeid(h,m);
        Log.d("text_view","获取ID完毕");
        CourseInfo re=null;
        //Log.d("text_view","今日课程大小:"+today.size());
        if (i==1) {
            for (int j =0;j<today.size();j++) {
                CourseInfo c=today.get(j);
                if (currWeek>= c.getWeekfrom() && currWeek <= c.getWeekto()) {
                    if (now >= c.getLessonfrom() && now <= c.getLessonto()) {
                        return c.getCourseInfo();
                    }
                }
            }
            return "现在没有课哦！";
        }else if (i==2){
            double minn=100;
            for (CourseInfo c : today) {
                if (currWeek>= c.getWeekfrom() && currWeek <= c.getWeekto()) {
                    if (now<c.getLessonfrom()) {
                        if (c.getLessonfrom()-now<minn)
                        {
                            minn=c.getLessonfrom()-now;
                            re=c;
                        }
                    }
                }
            }
            if (re!=null)
            {
                return re.getCourseInfo();
            }
            else{
                return "接下来没有课哦！";
            }
        }
        return "出错啦!";
    }

    //根据实践判断现在是第几节课
    private double judgeid(int h, int m) {
        int sm=h*60+m;
        int firs=8*60+10,fire=9*60+50;
        int secs=10*60+10,sece=11*60+50;
        int tirs=13*60+30,tire=15*60+10;
        int fous=15*60+30,foue=17*60+10;
        int fivs=18*60+30,five=20*60+10;
        int sixs=20*60+30,sixe=21*60+10;
        if(sm<firs)
        {
            return 0;
        }
        else if (sm>=firs && sm<=firs+45)
        {
            return 1;
        }
        else if (sm>firs+45 && sm<fire-45)
        {
            return 1.5;
        }
        else if (sm>=fire-45 && sm<=fire)
        {
            return 2;
        }
        else if (sm>fire && sm<secs)
        {
            return 2.5;
        }
        else if (sm>=secs && sm<=secs+45)
        {
            return 3;
        }
        else if (sm>secs+45 && sm<sece-45)
        {
            return 3.5;
        }
        else if (sm>=sece-45 && sm<=sece)
        {
            return 4;
        }
        else if (sm>sece && sm<tirs)
        {
            return 4.5;
        }
        else if (sm>=tirs && sm<=tirs+45)
        {
            return 5;
        }
        else if (sm>tirs+45 && sm<tire-45)
        {
            return 5.5;
        }
        else if (sm>=tire-45 && sm<=tire)
        {
            return 6;
        }
        else if (sm>tire && sm<fous)
        {
            return 6.5;
        }
        else if (sm>=fous && sm<=fous+45)
        {
            return 7;
        }
        else if (sm>fous+45 && sm<foue-45)
        {
            return 7.5;
        }
        else if (sm>=foue-45 && sm<=foue)
        {
            return 8;
        }
        else if (sm>foue && sm<fivs)
        {
            return 8.5;
        }
        else if (sm>=fivs && sm<=fivs+45)
        {
            return 9;
        }
        else if (sm>fivs+45 && sm<=five-45)
        {
            return 9.5;
        }
        else if (sm>=five-45 && sm<=five)
        {
            return 10;
        }
        else if (sm>five && sm<sixs)
        {
            return 10.5;
        }
        else if (sm>=sixs && sm<=sixs+45)
        {
            return 11;
        }
        else if (sm>sixs+45 && sm<sixe-45)
        {
            return 11.5;
        }
        else if (sm>=sixe-45 && sm<=sixe)
        {
            return 12;
        }
        else {
            return 13;
        }
    }

    //初始化标题栏
    private void initView() {
        menuView = findViewById(R.id.Btn_Course_Menu);
        refreshView = findViewById(R.id.Btn_Course_Refresh);
        //设置标题栏周数样式
        weekTextView = (TextView)findViewById(R.id.Menu_main_textWeeks);
        weekTextView.setTextSize(20);
        weekTextView.setPadding(15,2,15,2);
        //右边白色倒三角
        Drawable down = getResources().getDrawable(R.drawable.title_down);
        down.setBounds(0,0,down.getMinimumWidth(),down.getMinimumHeight());
        weekTextView.setCompoundDrawables(null,null,down,null);
        weekTextView.setCompoundDrawablePadding(2);
        //计算并显示上周数
        weekTextView.setText("第" + Utility.getWeeks(gInfo.getTermBegin()) + "周(本周)");

    	weekDaysTextView = new TextView[7];
    	weekDaysTextView[0] = (TextView) findViewById(R.id.Text_Course_Subhead_Mon);
    	weekDaysTextView[1] = (TextView) findViewById(R.id.Text_Course_Subhead_Tue);
    	weekDaysTextView[2] = (TextView) findViewById(R.id.Text_Course_Subhead_Wed);
    	weekDaysTextView[3] = (TextView) findViewById(R.id.Text_Course_Subhead_Thu);
    	weekDaysTextView[4] = (TextView) findViewById(R.id.Text_Course_Subhead_Fri);
    	weekDaysTextView[5] = (TextView) findViewById(R.id.Text_Course_Subhead_Sat);
    	weekDaysTextView[6] = (TextView) findViewById(R.id.Text_Course_Subhead_Sun);

        empty = (TextView) this.findViewById(R.id.test_empty);
        empty.getBackground().setAlpha(0);//0~255透明度值;
	}

	//返回上下文
    public static Context getContext() {
        return context;
    }

    //初始化本周课程表格
    private void initTable() {
        // 列表布局文件
        table_layout = (RelativeLayout) this.findViewById(R.id.test_course_rl);
        table_layout.removeAllViews();
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        //屏幕宽度
        int width = dm.widthPixels;
        //平均宽度
        int aveWidth = width / 8;
        //给列头设置宽度
        this.screenWidth = width;
        this.aveWidth = aveWidth;

        //屏幕高度
        int height = dm.heightPixels;
        gridHeight = height / dayCourseNum;

        //设置课表界面，动态生成8 * dayCourseNum个textview
        for (int i = 1; i <= dayCourseNum; i++) {

            for (int j = 1; j <= 8; j++) {
                BorderTextView tx = new BorderTextView(this);
                tx.setId((i - 1) * 8 + j);
                //相对布局参数
                RelativeLayout.LayoutParams rp = new RelativeLayout.LayoutParams(
                        aveWidth * 33 / 32 + 1,
                        gridHeight);
                //文字对齐方式
                tx.setGravity(Gravity.CENTER);
                //字体样式
                tx.setTextAppearance(this, R.style.courseTableText);
                //如果是第一列，需要设置课的序号（1 到 12）
                if (j == 1) {
                    tx.setBackgroundDrawable(getResources().getDrawable(R.drawable.main_table_first_colum));
                    tx.setText(String.valueOf(i));
                    rp.width = aveWidth * 3 / 4;
                    //设置他们的相对位置
                    if (i == 1)
                        rp.addRule(RelativeLayout.BELOW, empty.getId());
                    else
                        rp.addRule(RelativeLayout.BELOW, (i - 1) * 8);
                } else {
                    rp.addRule(RelativeLayout.RIGHT_OF, (i - 1) * 8 + j - 1);
                    rp.addRule(RelativeLayout.ALIGN_TOP, (i - 1) * 8 + j - 1);
                    tx.setText("");
                }

                tx.setLayoutParams(rp);
                table_layout.addView(tx);
            }
        }

    }

    //初始化当日课程表格
    private void initTable_today() {
        // 列表布局文件
        table_layout = (RelativeLayout) this.findViewById(R.id.test_course_rl);
        table_layout.removeAllViews();
        table_layout.setBackgroundResource(R.drawable.lufei);
        table_layout.getBackground().setAlpha(100);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        //屏幕宽度
        int width = dm.widthPixels;
        //平均宽度
        int aveWidth = width/2;
        //给列头设置宽度
        this.screenWidth = width;
        this.aveWidth = aveWidth;

        //屏幕高度
        int height = dm.heightPixels;
        gridHeight = height / 2;

        //设置课表界面，动态生成2 * 1个textview
        for (int i = 1; i <= 2; i++) {

            for (int j = 1; j <= 2; j++) {
                BorderTextView tx = new BorderTextView(this);
                tx.setId((i - 1) * 2 + j);
                //相对布局参数
                RelativeLayout.LayoutParams rp = new RelativeLayout.LayoutParams(
                        aveWidth * 33 / 32 + 1,
                        gridHeight);
                //文字对齐方式
                tx.setGravity(Gravity.CENTER);
                //字体样式
                tx.setTextAppearance(this, R.style.courseTableText);
                //如果是第一列，需要设置课的序号（1 到 12）
                if (j == 1) {
                    tx.setBackgroundDrawable(getResources().getDrawable(R.drawable.main_table_first_colum));
                    tx.setTextSize(20);
                    rp.width = aveWidth * 1 / 2;
                    //设置他们的相对位置
                    if (i == 1) {
                        tx.setText("Now");
                        rp.addRule(RelativeLayout.BELOW, empty.getId());
                    }
                    else {
                        tx.setText("Next");
                        rp.addRule(RelativeLayout.BELOW, (i - 1) * 2);
                    }
                } else {
                    rp.width = aveWidth * 3 / 2;
                    rp.addRule(RelativeLayout.RIGHT_OF, (i - 1) * 2 + j - 1);
                    rp.addRule(RelativeLayout.ALIGN_TOP, (i - 1) * 2 + j - 1);
                    tx.setText(get_course(i));
                    tx.setTextSize(30);
                }

                tx.setLayoutParams(rp);
                table_layout.addView(tx);
            }
        }

    }

    //对顶部三个组件设置监听器
    private void initListener() {
        menuView.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v) {
                mDrawerLayout.openDrawer(GravityCompat.START);
                //mMenuDrawer.toggleMenu();
            }
        });
        menuView.setOnCreateContextMenuListener(this);
        refreshView.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                courseSettings.edit().putBoolean("needRefresh_" + uid, true).commit();//设置信息需要从服务器获取的标志
                courseInfoList.clear();
                //删掉textView，清空信息，再次添加
                Log.v("refresh:", "清空信息，再次添加");
                for(TextView tx : courseTextViewList) {
                    table_layout.removeView(tx);
                }
                courseTextViewList.clear();

                cw= Utility.getWeeks(gInfo.getTermBegin());
                //计算并显示上周数
                weekTextView.setText("第" + Utility.getWeeks(gInfo.getTermBegin()) + "周(本周)");
                try {
                    refresh();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (BiffException e) {
                    e.printStackTrace();
                }
            }
        });
        //设置点击事件
        weekTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                showWeekListWindow(weekTextView);
            }
        });

    }

    //刷新
    private void refresh() throws IOException, BiffException {
        // 显示状态对话框
        Log.d("text_view","进入刷新函数");
        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getResources().getString(R.string.loading_tip));
        progressDialog.setCancelable(true);
        progressDialog.show();
        Log.d("text_view","开始刷新");
        if (status==0) {
            getFromLocal(cw);
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }
        else{
            initTable_today();
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }
    }

    //直接从本地数据库缓存提取数据显示
    private void getFromLocal(int cur){
        courseInfoList = uCourseDao.query(uid);
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        //初始化课表
        initCourse();
        //显示课表内容
        initCourseTableBody(cur);
    }

     //将课程列表存入数据库
    private boolean saveCourse() {
        if (uCourseDao.clear(uid)) {
            for (CourseInfo cInfo : courseInfoList) {
                Log.d("StartActivity",String.valueOf(cInfo.getDay())+cInfo.getCoursename());
                cInfoDao.insert(cInfo);
                int cid = cInfo.getCid();
                if (cid == 0) {
                    return false;
                }
                UserCourse uCourse = new UserCourse();
                uCourse.setUid(uid);
                uCourse.setCid(cid);
                if(!uCourseDao.insert(uCourse)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    //初始化课表，分配空间，将courseInfoList中的课程放入courseInfoMap中
    private void initCourse() {
        courseInfoMap = new HashMap<String, List<CourseInfo>>();
        for (int i =1 ; i <= 7; i++) {
            LinkedList<CourseInfo> dayCourses = new LinkedList<CourseInfo>();
            for (CourseInfo courseInfo : courseInfoList) {
                int day = courseInfo.getDay();
                //Log.d("StartActivity",String.valueOf(day)+courseInfo.getCoursename());
                if(day==i) {
                    dayCourses.add(courseInfo);
                }
            }
            courseInfoMap.put(String.valueOf(i),dayCourses);
        }
    }

    //显示课程
    private void initCourseTableBody(int currentWeek){
        for(Map.Entry<String, List<CourseInfo>> entry: courseInfoMap.entrySet())
        {
            //查找出最顶层的课程信息（顶层课程信息即显示在最上层的课程，最顶层的课程信息满足两个条件 1、当前周数在该课程的周数范围内 2、该课程的节数跨度最大
            CourseInfo upperCourse = null;
            //list里保存的是一周内某 一天的课程
            final List<CourseInfo> list = new ArrayList<CourseInfo>(entry.getValue());
            //按开始的时间（哪一节）进行排序
            Collections.sort(list, new Comparator<CourseInfo>(){
                @Override
                public int compare(CourseInfo arg0, CourseInfo arg1) {

                    if(arg0.getLessonfrom() < arg1.getLessonfrom())
                        return -1;
                    else
                        return 1;
                }

            });
            int lastListSize;
            do {

                lastListSize = list.size();
                Iterator<CourseInfo> iter = list.iterator();
                //先查找出第一个在周数范围内的课
                while(iter.hasNext())
                {
                    CourseInfo c = iter.next();
                    if(((c.getWeekfrom() <= currentWeek && c.getWeekto() >= currentWeek) || currentWeek == -1) && c.getLessonto() <= 12)
                    {
                        //判断当前周是否要放置该课程（该课程是否符合当前周单双周上课要求）
                        if(Utility.isCurrWeek(c,currentWeek)) {
                            //从list中移除该项，并设置这节课为顶层课
                            iter.remove();
                            upperCourse = c;
                            break;
                        }
                    }
                }
                if(upperCourse != null)
                {
                    List<CourseInfo> cInfoList = new ArrayList<CourseInfo>();
                    cInfoList.add(upperCourse);
                    int index = 0;
                    iter = list.iterator();
                    //查找这一天有哪些课与刚刚查找出来的顶层课相交
                    while(iter.hasNext())
                    {
                        CourseInfo c = iter.next();
                        //先判断该课程与upperCourse是否相交，如果相交加入cInfoList中
                        if((c.getLessonfrom() <= upperCourse.getLessonfrom()
                                &&upperCourse.getLessonfrom() < c.getLessonto())
                                ||(upperCourse.getLessonfrom() <= c.getLessonfrom()
                                && c.getLessonfrom() < upperCourse.getLessonto()))
                        {
                            //cInfoList.add(c);
                            iter.remove();
                            //在判断哪个跨度大，跨度大的为顶层课程信息
                            if((c.getLessonto() - c.getLessonto()) > (upperCourse.getLessonto() - upperCourse.getLessonfrom())
                                    && ((c.getWeekfrom() <= currentWeek && c.getWeekto() >= currentWeek) || currentWeek == -1))
                            {
                                upperCourse = c;
                                index ++;
                            }

                        }

                    }

                    //五种颜色的背景
                    int[] background = {R.drawable.main_course1, R.drawable.main_course2,
                            R.drawable.main_course3, R.drawable.main_course4,
                            R.drawable.main_course5};
                    //记录顶层课程在cInfoList中的索引位置
                    final int upperCourseIndex = index;
                    // 动态生成课程信息TextView
                    TextView courseInfo = new TextView(this);
                    courseInfo.setId(1000 + upperCourse.getDay() * 100 + upperCourse.getLessonfrom() * 10 + upperCourse.getCid());//设置id区分不同课程
                    int id = courseInfo.getId();
                    textviewCourseInfoMap.put(id, cInfoList);
                    courseInfo.setText(upperCourse.getCoursename() + "\n@" + upperCourse.getPlace());
                    //该textview的高度根据其节数的跨度来设置
                    RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(
                            aveWidth * 31 / 32,
                            (gridHeight - 5) * 2 + (upperCourse.getLessonto() - upperCourse.getLessonfrom() - 1) * gridHeight);
                    //textview的位置由课程开始节数和上课的时间（day of week）确定
                    rlp.topMargin = 5 + (upperCourse.getLessonfrom() - 1) * gridHeight;
                    rlp.leftMargin = 1;
                    // 前面生成格子时的ID就是根据Day来设置的，偏移由这节课是星期几决定
                    rlp.addRule(RelativeLayout.RIGHT_OF, upperCourse.getDay());
                    //字体居中中
                    courseInfo.setGravity(Gravity.CENTER);
                    //选择一个颜色背景
                    int colorIndex = ((upperCourse.getLessonfrom() - 1) * 8 + upperCourse.getDay()) % (background.length - 1);
                    courseInfo.setBackgroundResource(background[colorIndex]);
                    courseInfo.setTextSize(12);
                    courseInfo.setLayoutParams(rlp);
                    courseInfo.setTextColor(Color.WHITE);
                    //设置不透明度
                    courseInfo.getBackground().setAlpha(200);
                    // 设置监听事件
                    courseInfo.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View arg0) {

                            //Log.v("text_view", String.valueOf(arg0.getId()));
                            Map<Integer, List<CourseInfo>> map = textviewCourseInfoMap;
                            final List<CourseInfo> tempList = map.get(arg0.getId());
                           if(tempList.size() > 1)
                            {
                                //如果有多个课程，则设置点击弹出gallery 3d 对话框
                                LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                //Log.v("text_view", String.valueOf(0));
                                View galleryView = layoutInflater.inflate(R.layout.info_gallery_layout, null);
                                //Log.v("text_view", String.valueOf(1));
                                final Dialog coursePopupDialog = new AlertDialog.Builder(CourseActivity.this).create();
                                //Log.v("text_view", String.valueOf(2));
                                coursePopupDialog.setCanceledOnTouchOutside(true);
                                //Log.v("text_view", String.valueOf(3));
                                coursePopupDialog.setCancelable(true);
                                //Log.v("text_view", String.valueOf(4));
                                coursePopupDialog.show();
                                //Log.v("text_view", String.valueOf(5));
                                WindowManager.LayoutParams params = coursePopupDialog.getWindow().getAttributes();
                                params.width = WindowManager.LayoutParams.MATCH_PARENT;
                                coursePopupDialog.getWindow().setAttributes(params);
                                CourseInfoAdapter adapter = new CourseInfoAdapter(CourseActivity.this, tempList, screenWidth, cw);
                                InfoGallery gallery = (InfoGallery) galleryView.findViewById(R.id.info_gallery);
                                gallery.setSpacing(10);
                                gallery.setAdapter(adapter);
                                gallery.setSelection(upperCourseIndex);

                                gallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(
                                            AdapterView<?> arg0, View arg1,
                                            int arg2, long arg3) {
                                        CourseInfo courseInfo = tempList.get(arg2);
                                        Intent intent = new Intent();
                                        Bundle mBundle = new Bundle();
                                        mBundle.putSerializable("courseInfo", courseInfo);
                                        intent.putExtras(mBundle);
                                        intent.setClass(CourseActivity.this, CourseDetailInfoActivity.class);
                                        startActivity(intent);
                                        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
                                        coursePopupDialog.dismiss();
                                        finish();
                                    }
                                });
                                coursePopupDialog.setContentView(galleryView);
                            }
                            else
                            {
                                Intent intent = new Intent();
                                Bundle mBundle = new Bundle();
                                mBundle.putSerializable("courseInfo", tempList.get(0));
                                intent.putExtras(mBundle);
                                intent.setClass(CourseActivity.this, CourseDetailInfoActivity.class);
                                overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
                                startActivity(intent);
                                finish();
                            }

                        }

                    });
                    table_layout.addView(courseInfo);
                    courseTextViewList.add(courseInfo);

                    upperCourse = null;
                }
            } while(list.size() < lastListSize && list.size() != 0);
        }

    }

    //显示周列表
    private void showWeekListWindow(View parent){

        if(weekListWindow == null)
        {
            LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            //获取layout
            popupWindowLayout = layoutInflater.inflate(R.layout.week_list_layout, null);
            popupWindowLayout.setBackgroundColor(Color.rgb(216,216,216));
            weekListView = (ListView) popupWindowLayout.findViewById(R.id.week_list_view_body);

            List<Map<String, Object>> weekList = new ArrayList<Map<String, Object>>();
            //默认25周
            for(int i = 1; i <= 25; i ++)
            {
                Map<String, Object> rowData = new HashMap<String, Object>();
                rowData.put("week_index", "第" + i + "周");
                weekList.add(rowData);
            }

            //设置listview的adpter
            SimpleAdapter listAdapter = new SimpleAdapter(this,
                    weekList, R.layout.week_list_item_layout,
                    new String[]{"week_index"},
                    new int[]{R.id.week_list_item});

            //设置recyclerview类型的listview的adpter()
//            WeekAdapter listAdapter = new WeekAdapter(weekList);
            weekListView.setAdapter(listAdapter);
            weekListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adpater, View arg1,
                                        int arg2, long arg3) {
                    int index = 0;
                    String indexStr = weekTextView.getText().toString();
                    indexStr = indexStr.replace("第", "").replace("周(本周)", "");
                    indexStr = indexStr.replace("周(非本周)", "");
                    if(!indexStr.equals("全部"))//没啥用
                        index = Integer.parseInt(indexStr);
                    if(currWeek == (arg2 + 1)){
                        weekTextView.setText("第" + (arg2 + 1) + "周(本周)");
                    }
                    else{
                        weekTextView.setText("第" + (arg2 + 1) + "周(非本周)");
                    }
                    weekListWindow.dismiss();
                    if((arg2 + 1) != index)
                    {
                        cw = arg2+1;
                        Log.v("courseActivity", "cw值改变："+ cw);
                        Log.v("courseActivity", "清空当前课程信息");
                        for(TextView tx : courseTextViewList)
                        {
                            table_layout.removeView(tx);
                        }
                        courseTextViewList.clear();
                        //重新设置课程信息
                        initCourse();
                        initCourseTableBody(cw);

                    }
                }
            });
            int width = weekTextView.getWidth();
            //实例化一个popupwindow
            weekListWindow = new PopupWindow(popupWindowLayout, width + 100, width + 120);

        }

        weekListWindow.setFocusable(true);
        //设置点击外部可消失
        weekListWindow.setOutsideTouchable(true);
        weekListWindow.setBackgroundDrawable(new BitmapDrawable());
        //消失的时候恢复按钮的背景（消除"按下去"的样式）
        weekListWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                weekTextView.setBackgroundDrawable(null);
            }
        });
        weekListWindow.showAsDropDown(parent, -50, 0);
    }

    //添加课程
    private void jumpToCourseIn() throws IOException, BiffException {
        uCourseDao.clear(uid);
        courseInfoList.clear();
        for(TextView tx : courseTextViewList) {
            table_layout.removeView(tx);
        }
        courseTextViewList.clear();
        InputStream inputStream = null;//输入流
        FileOutputStream outputStream = null;//输出流
        Workbook book = null;
        int flag=0,count=0;
        //courseInfoList
        inputStream = context.getAssets().open("course.xls");
        File tempFile = new File(context.getCacheDir(), "test.xls");
        outputStream = new FileOutputStream(tempFile);
        byte[] buf = new byte[1024];
        int len;
        while ((len = inputStream.read(buf)) > 0) {
            outputStream.write(buf, 0, len);
        }
        outputStream.close();
        inputStream.close();
        book = Workbook .getWorkbook(tempFile);//用读取到的表格文件来实例化工作簿对象（符合常理，我们所希望操作的就是Excel工作簿文件）
        Sheet[] sheets = book.getSheets(); //得到所有的工作表

        for (int m = 0; m < sheets.length; m++) {
            Sheet sheet = book.getSheet(m);
            int Rows = sheet.getRows();//得到当前工作表的行数
            int Cols = sheet.getColumns(); //得到当前工作表的列数
            for (int i = 0; i < Cols; i++) {  // 注意：这里是按列读取的！！！
                for (int j = 0; j < Rows; j++) {
                    String content = sheet.getCell(i, j).getContents();//结果是String类型的，根据具体需求进行类型转换
                    if (content.length()!=0 && content!=null && content!="") {
                        //Log.d("StartActivity",content+" "+String.valueOf(flag));
                        if (content.contains("星期") && flag == 0) {
                            count += 1;
                            courseInfo=new CourseInfo();
                            courseInfo.setCid(count);
                            courseInfo.setDay(Big2Digit.get(content));
                            //Log.d("StartActivity",String.valueOf(Big2Digit.get(content)));
                            flag = 1;
                        } else if (flag == 1) {
                            //Log.d("StartActivity",content);
                            courseInfo.setCoursename(content);
                            flag = 2;
                        } else if (flag == 2) {
                            //Log.d("StartActivity",content);
                            int a = content.indexOf("第", 0);
                            int b = content.indexOf(",", 0);
                            int ab = Integer.parseInt(content.substring(a+1, b));
                            int c = content.indexOf("节", 0);
                            int bc = Integer.parseInt(content.substring(b+1, c));
                            int d = content.indexOf("第", c);
                            int e = content.indexOf("-", c);
                            int de = Integer.parseInt(content.substring(d+1, e));
                            int f = content.indexOf("周", c);
                            int ef = Integer.parseInt(content.substring(e+1, f));
                            courseInfo.setLessonfrom(ab);
                            courseInfo.setLessonto(bc);
                            courseInfo.setWeekfrom(de);
                            courseInfo.setWeekto(ef);
                            if (content.contains("单周")) {
                                courseInfo.setWeektype(2);
                            } else if (content.contains("双周")) {
                                courseInfo.setWeektype(3);
                            } else {
                                courseInfo.setWeektype(1);
                            }
                            flag = 3;
                            /*Log.d("StartActivity", content.substring(a+1, b));
                            Log.d("StartActivity", content.substring(b+1, c));
                            Log.d("StartActivity", content.substring(d+1, e));
                            Log.d("StartActivity", content.substring(e+1, f));*/
                        } else if (flag == 3) {
                            //Log.d("StartActivity",content);
                            courseInfo.setTeacher(content);
                            flag = 4;
                        } else if (flag == 4) {
                            //Log.d("StartActivity",content);
                            courseInfo.setPlace(content);
                            flag = 5;
                        } else if (flag == 5) {
                            if (content.contains("(调") || content.contains("(停") || content.contains("(补")) {
                                continue;
                            } else if (content.contains("星期")==false) {
                                if(!courseInfoList.contains(courseInfo))
                                    courseInfoList.add(courseInfo);
                                    cInfoDao.delete(courseInfo.getCid());
                                //Log.d("StartActivity",String.valueOf(courseInfoList.toArray().length));
                                int k=courseInfo.getDay();
                                //Log.d("StartActivity",String.valueOf(courseInfo.getDay())+courseInfo.getCoursename());
                                count += 1;
                                courseInfo=new CourseInfo();
                                courseInfo.setCid(count);
                                courseInfo.setDay(k);
                                courseInfo.setCoursename(content);
                                //Log.d("StartActivity",content);
                                flag = 2;
                            }else if (content.contains("星期")==true){
                                //Log.d("StartActivity",courseInfo.toString());
                                if(!courseInfoList.contains(courseInfo))
                                    courseInfoList.add(courseInfo);
                                    cInfoDao.delete(courseInfo.getCid());
                                //Log.d("StartActivity",String.valueOf(courseInfoList.toArray().length));
                                //Log.d("StartActivity",String.valueOf(courseInfo.getDay())+courseInfo.getCoursename());
                                count += 1;
                                courseInfo=new CourseInfo();
                                courseInfo.setCid(count);
                                courseInfo.setDay(Big2Digit.get(content));
                                //Log.d("StartActivity",String.valueOf(Big2Digit.get(content)));
                                flag = 1;
                            }
                        }
                    }
                }
            }
        }
        saveCourse();
        refresh();
    }

    //导出课程
    private void jumpToCourseOut() throws IOException, WriteException {
        WritableWorkbook wwb;
        FileOutputStream os = null;//输出流
        Workbook book = null;
        File tempFile = new File(context.getCacheDir(), "course_out.xls");
        os = new FileOutputStream(tempFile);
        wwb = Workbook.createWorkbook(os);
        WritableSheet sheet = wwb.createSheet("课程表", 0);
        String[] title = {"时间","","星期一","星期二","星期三","星期四","星期五","星期六","星期日"};
        Label label;
        //Log.d("text_view",String.valueOf(sheet.getColumnView(0).getSize()));
        //Log.d("text_view",String.valueOf(sheet.getRowView(0).getSize()));
        for (int i=2;i<=8;i++) {
            sheet.setColumnView(i, 13);
        }
        for (int j=2;j<60;j++) {
            sheet.setRowView(j, 350);
        }
        for (int i = 0; i < title.length; i++) {
            // Label(x,y,z) 代表单元格的第x+1列，第y+1行, 内容z
            // 在Label对象的子对象中指明单元格的位置和内容
            label = new Label(i, 0, title[i], getHeader(0));
            // 将定义好的单元格添加到工作表中
            sheet.addCell(label);
        }
        sheet.mergeCells(0,0,1,0);
        String[] title1={"早晨","","","","","","","",""};
        for (int i = 0; i < title1.length; i++) {
            // Label(x,y,z) 代表单元格的第x+1列，第y+1行, 内容z
            // 在Label对象的子对象中指明单元格的位置和内容
            label = new Label(i, 1, title1[i], getHeader(0));
            // 将定义好的单元格添加到工作表中
            sheet.addCell(label);
        }
        sheet.mergeCells(0,1,1,1);
        sheet.mergeCells(0,2,0,17);
        label = new Label(0, 2,"上午", getHeader(0));
        sheet.addCell(label);
        sheet.mergeCells(0,18,0,33);
        label = new Label(0, 18,"下午", getHeader(0));
        sheet.addCell(label);
        sheet.mergeCells(0,34,0,49);
        label = new Label(0, 34,"晚上", getHeader(0));
        sheet.addCell(label);
        for (int i=1;i<=12;i++)
        {
            sheet.mergeCells(1,2+4*(i-1),1,2+4*(i-1)+3);
            label = new Label(1, 2+4*(i-1),"第"+i+"节", getHeader(0));
            sheet.addCell(label);
        }
        initCourse();
        int f,t;
        String s;
        int[] lab=new int[12];
        int row=13,p;//周次不分，时间冲突的课
        for (int i=1;i<=7;i++)
        {
            for (int k=0;k<12;k++)
            {
                lab[k]=0;
            }
            p=0;
            List<CourseInfo> dayCourses = new LinkedList<CourseInfo>();
            dayCourses=courseInfoMap.get(String.valueOf(i));
            for (CourseInfo courseInfo : dayCourses)
            {
                f=courseInfo.getLessonfrom();
                t=courseInfo.getLessonto();
                for (int k=f-1;k<=t-1;k++)
                {
                    if (lab[k]==1)
                    {
                        t=t-f+row;
                        f=row;
                        row+=1;
                        p=1;
                        break;
                    }
                }
                if (p==0) {
                    for (int k = f - 1; k <= t - 1; k++) {
                        lab[k] = 1;
                    }
                }
                sheet.mergeCells(i+1,2+4*(f - 1), i + 1, 2 + 4 * (f - 1) + (t-f+1)*4-1);
                s=courseInfo.getCoursename()+"\r\n"+"周"+Utility.getDayStr(courseInfo.getDay()) + " 第" + courseInfo.getLessonfrom()
                        + "节 - 第" + courseInfo.getLessonto() + "节"+"\r\n";
                if(courseInfo.getWeektype()==1) {
                    s+="{第" +  courseInfo.getWeekfrom() + "周 - 第" + courseInfo.getWeekto() + "周}"+"\r\n";
                }else if(courseInfo.getWeektype()==2) {
                    s+="{第" +  courseInfo.getWeekfrom() + "周 - 第" + courseInfo.getWeekto()+ "周 单周}"+"\r\n";
                }else if(courseInfo.getWeektype()==3) {
                    s+="{第" +  courseInfo.getWeekfrom() + "周 - 第" + courseInfo.getWeekto()+ "周 双周}"+"\r\n";
                }
                s+=courseInfo.getTeacher()+"\r\n"+courseInfo.getPlace()+"\r\n";
                label = new Label(i+1, 2 + (4 * (f - 1)), s, getHeader(0));
                sheet.addCell(label);
                p=0;
            }
            for (int k=0;k<12;k++)
            {
                if (lab[k]==0)
                {
                    sheet.mergeCells(i+1,2+4*k,i+1,5+4*k);
                    label = new Label(i+1,2+4*k,"", getHeader(0));
                    sheet.addCell(label);
                }
            }
        }
        wwb.write();
        wwb.close();
    }
    public static WritableCellFormat getHeader(int type) {
        WritableFont font;
        if(type==0) {
            font = new WritableFont(WritableFont.TIMES, 10,
                    WritableFont.BOLD);// 定义字体
        }
        else
        {
            font = new WritableFont(WritableFont.TIMES, 8,
                    WritableFont.BOLD);// 定义字体
        }
        try {
            font.setColour(Colour.BLACK);// 黑色字体
        } catch (WriteException e1) {
            e1.printStackTrace();
        }
        WritableCellFormat format = new WritableCellFormat(font);
        try {
            format.setAlignment(jxl.format.Alignment.CENTRE);// 左右居中
            format.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE);// 上下居中
            format.setBorder(Border.ALL, BorderLineStyle.THIN,
                    Colour.BLACK);// 黑色边框
            format.setBackground(Colour.WHITE);// 白色背景
            format.setWrap(true);
        } catch (WriteException e) {
            e.printStackTrace();
        }
        return format;
    }


}
