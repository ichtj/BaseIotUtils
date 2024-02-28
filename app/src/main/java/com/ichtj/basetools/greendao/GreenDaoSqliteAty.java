package com.ichtj.basetools.greendao;

import android.os.Bundle;
import android.view.View;

import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.ToastUtils;
import com.ichtj.basetools.R;
import com.ichtj.basetools.base.BaseActivity;
import com.ichtj.basetools.entity.PersonInfor;

import java.util.ArrayList;
import java.util.List;

/**
 * Create on 2020/6/28
 * author chtj
 * desc sqlite数据库操作
 * 使用的是 https://github.com/greenrobot/greenDAO 第三方包
 */
public class GreenDaoSqliteAty extends BaseActivity implements View.OnClickListener {
    private DbController mDbController;
    private static final String TAG="GreenDaoSqliteAty";
    PersonInfor personInfor1;
    PersonInfor personInfor2;
    PersonInfor personInfor3;
    PersonInfor personInfor4;
    PersonInfor personInfor5;
    PersonInfor personInfor6;
    PersonInfor personInfor7;
    PersonInfor personInfor8;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_greendao);
        //初始化
        mDbController = DbController.getInstance();
        //初始化数据
        initData();
    }

    public void initData(){
        personInfor1= new PersonInfor(null, "001", "王大宝", "男");
        personInfor2= new PersonInfor(null, "002", "李晓丽", "女");
        personInfor3= new PersonInfor(null, "003", "王麻麻", "男");
        personInfor4= new PersonInfor(null, "004", "王大锤", "女");
        personInfor5= new PersonInfor(null, "005", "A大锤", "女");
        personInfor6= new PersonInfor(null, "006", "B大锤", "男");
        personInfor7= new PersonInfor(null, "007", "C大锤", "女");
        personInfor8= new PersonInfor(null, "008", "D大锤", "男");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add://增加记录
                mDbController.insert(personInfor1);
                mDbController.insert(personInfor2);
                mDbController.insert(personInfor3);
                mDbController.insert(personInfor4);
                break;
            case R.id.btn_addList://多个添加
                List<PersonInfor> personInforLis=new ArrayList<>();
                personInforLis.add(personInfor5);
                personInforLis.add(personInfor6);
                personInforLis.add(personInfor7);
                personInforLis.add(personInfor8);
                mDbController.insertList(personInforLis);
                break;
            case R.id.btn_del://删除记录
                mDbController.delete("王麻麻");
                break;
            case R.id.btn_update://修改记录
                mDbController.update(personInfor1);
                break;
            case R.id.btn_search://查询记录
                List<PersonInfor> personInfors = mDbController.searchAll();
                ToastUtils.success(personInfors.size()+"");
                for (int i = 0; i < personInfors.size(); i++) {
                    KLog.d(TAG, ": list["+(i+1)+"]>>> Id="+personInfors.get(i).getId()+",PerNo="+personInfors.get(i).getPerNo()+",Name="+personInfors.get(i).getName()+",Sex="+personInfors.get(i).getSex());
                }
                break;
            case R.id.btn_deleteall://删除全部
                mDbController.deleteAll();
                ToastUtils.error("删除全部！");
                break;
            case R.id.btn_condition://删除全部
                List<PersonInfor> personInfors1 =mDbController.searchByName("李晓丽");
                ToastUtils.success(personInfors1.size()+"");
                break;
        }
    }
}