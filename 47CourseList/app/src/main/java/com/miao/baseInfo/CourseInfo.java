package com.miao.baseInfo;

import com.miao.common.Utility;

import java.io.Serializable;

// CourseInfo用于记录一节课的具体信息
public class CourseInfo extends BaseInfo implements Serializable{

    private static final long serialVersionUID = 2074656067805712769L;
	
	private int cid;		// 课程标记id
	private String coursename; //课程名
	private String teacher;		// 教师

	public int getCid() {
		return cid;
	}
	public void setCid(int cid) {
		this.cid = cid;
	}
	public String getCoursename() {
		return coursename;
	}
	public void setCoursename(String coursename) {
		this.coursename = coursename;
	}
	public String getTeacher() {
		return teacher;
	}
	public void setTeacher(String teacher) {
		this.teacher = teacher;
	}
	public String getCourseInfo(){
		String s=new String();

		s+=getCoursename()+"\r\n"+"周"+ Utility.getDayStr(getDay()) + " 第" + getLessonfrom()
				+ "节 - 第" + getLessonto() + "节"+"\r\n";
		if(getWeektype()==1) {
			s+="{第" +  getWeekfrom() + "周 - 第" + getWeekto() + "周}"+"\r\n";
		}else if(getWeektype()==2) {
			s+="{第" +  getWeekfrom() + "周 - 第" + getWeekto()+ "周 单周}"+"\r\n";
		}else if(getWeektype()==3) {
			s+="{第" +  getWeekfrom() + "周 - 第" + getWeekto()+ "周 双周}"+"\r\n";
		}
		s+=getTeacher()+"\r\n"+getPlace()+"\r\n";
		return s;
	}
}