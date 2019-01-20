package com.miao.ui;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.miao.R;
import com.miao.baseInfo.CourseInfo;
import com.miao.baseInfo.UserCourse;
import com.miao.common.Utility;
import com.miao.db.dao.CourseInfoDao;
import com.miao.db.dao.UserCourseDao;

public class EditActivity extends AppCompatActivity {
    private static Context context;
    private EditText CourseEditText;
    private EditText TeacherEditText;
    private EditText TimeEditText;
    private EditText WeekEditText;
    private EditText SpaceEditText;
    private Button OkBtn;
    private Button CancelBtn;
    private CourseInfo cInfo;
    protected String v1,v2,v3,v4,v5;
    CourseInfoDao cInfoDao;
    private View backView;
    UserCourseDao ucDao;
    private int uid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_course_detail);
        initView();
        initListener();
    }

    private void initListener() {

        OkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Log.d("text_view","进入");
                v1=CourseEditText.getText().toString();//course
                v2=TeacherEditText.getText().toString();//teacher
                v3=TimeEditText.getText().toString();//time
                int day=CourseActivity.Big2Digit.get(v3.substring(0,3));
                int a = v3.indexOf("第", 0);
                int b = v3.indexOf("节", 0);
                int lessonf = Integer.parseInt(v3.substring(a+1, b));
                int c = v3.indexOf("第", b+1);
                int d = v3.indexOf("节", b+1);
                int lessont = Integer.parseInt(v3.substring(c+1, d));
                v4=WeekEditText.getText().toString();//week
                a = v4.indexOf("第", 0);
                b = v4.indexOf("周", 0);
                int weekf = Integer.parseInt(v4.substring(a+1, b));
                c = v4.indexOf("第", b);
                d= v4.indexOf("周", c);
                int weekt = Integer.parseInt(v4.substring(c+1, d));
                int type;
                if(v4.contains("单周"))
                {
                    type=2;
                }
                else if(v4.contains("双周"))
                {
                    type=3;
                }
                else
                {
                    type=1;
                }
                v5=SpaceEditText.getText().toString();//space
                Log.d("text_view",v5);
                CourseInfo temp=new CourseInfo();
                temp.setCid(cInfo.getCid());
                temp.setCoursename(v1);
                temp.setTeacher(v2);
                temp.setPlace(v5);
                temp.setDay(day);
                if (lessonf<=0)
                {
                    lessonf=1;
                }
                temp.setLessonfrom(lessonf);
                if (lessont>=13)
                {
                    lessont=12;
                }
                temp.setLessonto(lessont);
                if (weekf<=0)
                {
                    weekf=1;
                }
                temp.setWeekfrom(weekf);
                if (weekt>=26)
                {
                    weekt=25;
                }
                temp.setWeekto(weekt);
                temp.setWeektype(type);
                cInfoDao.delete(temp.getCid());
                UserCourse userCourse=new UserCourse();
                userCourse.setCid(cInfo.getCid());
                userCourse.setUid(uid);
                ucDao.delete(userCourse);
                cInfoDao.insert(temp);
                ucDao.insert(userCourse);
                Intent intent = new Intent();
                intent.setClass(EditActivity.this, CourseActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out);
                finish();
                Toast.makeText(EditActivity.this, "修改成功", Toast.LENGTH_SHORT).show();
            }
        });
        CancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(EditActivity.this, CourseActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out);
                finish();
            }
        });
        backView.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(EditActivity.this, CourseActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out);
                finish();
            }
        });
    }

    private void initView() {
        backView = findViewById(R.id.Btn_Course_Back);
        CourseEditText=(EditText)findViewById(R.id.CourseEditText);
        TeacherEditText=(EditText)findViewById(R.id.TeacherEditText);
        TimeEditText=(EditText)findViewById(R.id.TimeEditText);
        WeekEditText=(EditText)findViewById(R.id.WeekEditText);
        SpaceEditText=(EditText)findViewById(R.id.SpaceEditText);
        OkBtn=(Button)findViewById(R.id.Btn_Ok);
        CancelBtn=(Button)findViewById(R.id.Btn_Cancel);
        cInfo = (CourseInfo) getIntent().getSerializableExtra("courseInfo");
        uid =(Integer) getIntent().getSerializableExtra("uid");
        CourseEditText.setText(cInfo.getCoursename());
        TeacherEditText.setText(cInfo.getTeacher());
        TimeEditText.setText("星期"+Utility.getDayStr(cInfo.getDay()) + " 第" + cInfo.getLessonfrom() + "节 - 第" + cInfo.getLessonto() + "节");
        if(cInfo.getWeektype()==1) {
            WeekEditText.setText("第" +  cInfo.getWeekfrom() + "周 - 第" + cInfo.getWeekto() + "周");
        }else if(cInfo.getWeektype()==2) {
            WeekEditText.setText("第" +  cInfo.getWeekfrom() + "周 - 第" + cInfo.getWeekto()+ "周 单周");
        }else if(cInfo.getWeektype()==3) {
            WeekEditText.setText("第" +  cInfo.getWeekfrom() + "周 - 第" + cInfo.getWeekto()+ "周 双周");
        }
        SpaceEditText.setText(cInfo.getPlace());
        context = getApplicationContext();
        cInfoDao = new CourseInfoDao(context);
        ucDao = new UserCourseDao(context);
    }
}
