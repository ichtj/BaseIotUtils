package com.wave_chtj.example.greendao;

import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;

import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.BaseIotUtils;

import java.util.List;

/**
 * Create on 2020/6/28
 * author chtj
 * desc
 */
public class DbController {
    private static final String TAG="DbController";
    /**
     * Helper
     */
    private DaoMaster.DevOpenHelper mHelper;//获取Helper对象
    /**
     * 数据库
     */
    private SQLiteDatabase db;
    /**
     * DaoMaster
     */
    private DaoMaster mDaoMaster;
    /**
     * DaoSession
     */
    private DaoSession mDaoSession;
    /**
     * dao
     */
    private PersonInforDao personInforDao;

    private static DbController mDbController;

    /**
     * 获取单例
     */
    public static DbController getInstance() {
        if (mDbController == null) {
            synchronized (DbController.class) {
                if (mDbController == null) {
                    mDbController = new DbController();
                }
            }
        }
        return mDbController;
    }

    /**
     * 初始化
     */
    public DbController() {
        mHelper = new DaoMaster.DevOpenHelper(BaseIotUtils.getContext(), "person.db", null);
        mDaoMaster = new DaoMaster(getWritableDatabase());
        mDaoSession = mDaoMaster.newSession();
        personInforDao = mDaoSession.getPersonInforDao();
    }

    /**
     * 获取可读数据库
     */
    private SQLiteDatabase getReadableDatabase() {
        if (mHelper == null) {
            mHelper = new DaoMaster.DevOpenHelper(BaseIotUtils.getContext(), "person.db", null);
        }
        SQLiteDatabase db = mHelper.getReadableDatabase();
        return db;
    }

    /**
     * 获取可写数据库
     *
     * @return
     */
    private SQLiteDatabase getWritableDatabase() {
        if (mHelper == null) {
            mHelper = new DaoMaster.DevOpenHelper(BaseIotUtils.getContext(), "person.db", null);
        }
        SQLiteDatabase db = mHelper.getWritableDatabase();
        return db;
    }

    /**
     * 会自动判定是插入还是替换
     *
     * @param personInfor
     */
    public void insertOrReplace(PersonInfor personInfor) {
        personInforDao.insertOrReplace(personInfor);
    }

    /**
     * 插入一条记录，表里面要没有与之相同的记录
     *
     * @param personInfor
     */
    public long insert(PersonInfor personInfor) {
        try{
            personInforDao.insertInTx();
            return personInforDao.insert(personInfor);
        }catch(SQLiteConstraintException e){
            e.printStackTrace();
            KLog.e(TAG,"errMeg:"+e.getMessage());
            return -1;
        }
    }
    /**
     * 插入一条记录，表里面要没有与之相同的记录
     *
     * @param personInfor
     */
    public void insertList(List<PersonInfor> personInfor) {
        try{
            personInforDao.insertInTx(personInfor);
        }catch(SQLiteConstraintException e){
            e.printStackTrace();
            KLog.e(TAG,"errMeg:"+e.getMessage());
        }
    }

    /**
     * 更新数据
     *
     * @param personInfor
     */
    public void update(PersonInfor personInfor) {
        PersonInfor mOldPersonInfor = personInforDao.queryBuilder().where(PersonInforDao.Properties.Id.eq(personInfor.getId())).build().unique();//拿到之前的记录
        if (mOldPersonInfor != null) {
            mOldPersonInfor.setName("张三");
            personInforDao.update(mOldPersonInfor);
        }
    }

    /**
     * 按条件查询
     */
    public List<PersonInfor> searchByName(String name) {
        List<PersonInfor> personInfors = mDaoSession.queryRaw(PersonInfor.class, " where name = ?", name);
        return personInfors;
    }

    /**
     * 查询所有数据
     */
    public List<PersonInfor> searchAll() {
        List<PersonInfor> personInfors = personInforDao.queryBuilder().list();
        return personInfors;
    }

    /**
     * 删除数据
     */
    public void delete(String wherecluse) {
        personInforDao.queryBuilder().where(PersonInforDao.Properties.Name.eq(wherecluse)).buildDelete().executeDeleteWithoutDetachingEntities();
    }

    /**
     * 删除全部数据
     */
    public void deleteAll() {
        personInforDao.deleteAll();
    }
}
