package com.miao.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.MediaStore;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.miao.R;
import com.miao.baseInfo.CourseInfo;
import com.miao.baseInfo.UserInfo;

public class LoginActivity extends AppCompatActivity {

    private EditText school_login;
    private EditText id_login;
    private EditText password_login;
    private ImageView avatar_login;
    private CheckBox rememberpassword_login;
    private CheckBox auto_login;
    private Button button_login;
    private SharedPreferences sp;
    private String idvalue;
    private String passwordvalue;
    private String schoolvalue;
    private static final int PASSWORD_MIWEN = 0x81;
    private int count=0;
    protected static UserInfo person_login;
    private RadioGroup rg;
    private String sex="男";
    private int flag=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sp = this.getSharedPreferences("userInfo", Context.MODE_WORLD_READABLE);
        setContentView(R.layout.login);
        String s =  (String)getIntent().getSerializableExtra("logout");
        if (s.equals("false"))
        {
            flag=1;
            Log.d("text_view","不是注销");
        }
        else
        {
            flag=0;
            Log.d("text_view","是注销");
        }
        person_login=new UserInfo();
        school_login=(EditText)findViewById(R.id.schoolname);
        id_login=(EditText) findViewById(R.id.et_account);
        password_login=(EditText) findViewById(R.id.et_password);
        avatar_login=(ImageView) findViewById(R.id.iv_see_password);
        rememberpassword_login=(CheckBox) findViewById(R.id.checkBox_password);
        auto_login=(CheckBox) findViewById(R.id.checkBox_login);
        button_login=(Button) findViewById(R.id.btn_login);
        rg = (RadioGroup) findViewById(R.id.rg);

        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                if(checkedId==R.id.male){
                    sex="男";
                    //Toast.makeText(LoginActivity.this, "男", Toast.LENGTH_SHORT).show();
                }else {
                    sex="女";
                    //.makeText(LoginActivity.this, "女", Toast.LENGTH_SHORT).show();
                }
            }
        });
        if (sp.getBoolean("ischeck",false)){
            rememberpassword_login.setChecked(true);
            school_login.setText(sp.getString("SCHOOL",""));
            id_login.setText(sp.getString("ID",""));
            password_login.setText(sp.getString("PASSWORD",""));
            sex=sp.getString("SEX","");
            if (sex.equals("男"))
            {
                rg.check(R.id.male);

            }
            else
            {
                rg.check(R.id.female);

            }
            if(flag==0){
                if (auto_login.isChecked()) {
                    System.out.println("自动登录已选中");
                    sp.edit().putBoolean("auto_ischeck", true).commit();
                } else {
                    System.out.println("自动登录没有选中");
                    sp.edit().putBoolean("auto_ischeck", false).commit();
                }
            }
            //密文密码
            password_login.setInputType(PASSWORD_MIWEN);
            if (sp.getBoolean("auto_ischeck",false) && flag==1){
                Log.d("text_view","自动登录");
                auto_login.setChecked(true);
                person_login.setUid(Integer.parseInt(sp.getString("ID","").substring(1,2)+sp.getString("ID","").substring(9,11)));
                person_login.setUsername(sp.getString("ID",""));
                person_login.setPassword(sp.getString("PASSWOAD",""));
                person_login.setSchool(sp.getString("SCHOOL",""));
                person_login.setInstitute("数理与电子信息工程学院");
                person_login.setMajor("电子信息科学与技术");
                person_login.setGender(sp.getString("SEX",""));
                person_login.setPhone("10086");
                Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LoginActivity.this, StartActivity.class);
                startActivity(intent);
                finish();
            }
        }

        button_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                school_login.getPaint().setFlags(0);
                schoolvalue=school_login.getText().toString();
                id_login.getPaint().setFlags(0);
                idvalue=id_login.getText().toString();
                password_login.getPaint().setFlags(0);
                passwordvalue=password_login.getText().toString();


                if (idvalue.contains("15211110")&&passwordvalue.length()<=8 &&schoolvalue.equals("温州大学")){

                    person_login.setUid(Integer.parseInt(idvalue.substring(1,2)+idvalue.substring(9,11)));
                    person_login.setUsername(idvalue);
                    person_login.setPassword(passwordvalue);
                    person_login.setSchool(schoolvalue);
                    person_login.setInstitute("数理与电子信息工程学院");
                    person_login.setMajor("电子信息科学与技术");
                    person_login.setGender(sex);
                    person_login.setPhone("10086");
                    if (rememberpassword_login.isChecked()){
                        SharedPreferences.Editor editor=sp.edit();
                        editor.putString("SCHOOL",schoolvalue);
                        editor.putString("ID",idvalue);
                        editor.putString("PASSWORD",passwordvalue);
                        editor.putString("SEX",sex);
                        editor.commit();
                    }
                    Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this,StartActivity.class);
                    startActivity(intent);
                    finish();
                }else{
                    Toast.makeText(LoginActivity.this, "信息错误，请重新登录", Toast.LENGTH_SHORT).show();
                }
            }
        });

        avatar_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(count==0) {
                    password_login.setInputType(1);
                    count=1;
                }
                else
                {
                    password_login.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    count=0;
                }
            }
        });

        rememberpassword_login.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (rememberpassword_login.isChecked()){
                    System.out.println("记住密码已选中");
                    sp.edit().putBoolean("ischeck",true).commit();
                }
                else {
                    System.out.println("记住密码没有选中");
                    sp.edit().putBoolean("ischeck",false).commit();
                }
            }
        });

        auto_login.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (auto_login.isChecked()){
                    System.out.println("自动登录已选中");
                    sp.edit().putBoolean("auto_ischeck",true).commit();
                }else {
                    System.out.println("自动登录没有选中");
                    sp.edit().putBoolean("auto_ischeck",false).commit();
                }
            }
        });
    }
}
