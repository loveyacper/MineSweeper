package bert.young.mine;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import android.app.ListActivity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.os.Bundle;

public class RankActivity extends ListActivity{

    private static final String RECORD_FILE = "MineSweeperRankList.txt";
    private static final String TAG         = "RankActivity";
    public  static final int    MAX_RANK    = 5;
    InputStream   mInFile;
    RankAdapter   mAdapter;
    
    @Override
    protected void onCreate(Bundle saved) {
        super.onCreate(saved);
        
        if (mAdapter == null) {
            Log.d(TAG, "OnCreate adapter");
            mAdapter = new RankAdapter(this);
            setListAdapter(mAdapter);
        }
        Log.d(TAG, "OnCreate");
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        if (mAdapter != null) {

            for (int i = 0; i < mAdapter.mRankList.size(); ++ i) 
                Log.d(TAG, "Resume name " + mAdapter.mRankList.get(i).name + ", date " + mAdapter.mRankList.get(i).date);
            //mAdapter.LoadData();
            
           // mAdapter.notifyDataSetChanged();
        }
    }
    
    static   class Record implements Comparable{
        String  name;
        long    score;
        String  date;
        
        @Override
        public String toString() {
         
            
            String fmtScore = null;
            if (score < 10) {
                fmtScore = score + "   秒";
            } else if (score < 100) {
                fmtScore = score + "  秒";
            } else {
                fmtScore = score + " 秒";
            }
            
            return name + " " + fmtScore + " " + date;
        }
        
        @Override
        public int compareTo(Object obj) {
            if (obj == null || !(obj instanceof Record))
                return 1;
            
            Record other = (Record)obj;
            if (score == other.score)
                return 0;
            else if (score < other.score)
                return -1;
            else
                return 1;
        }
        
        
    }
    
    class RankAdapter extends BaseAdapter {        
        private LayoutInflater     mInflater;
        private ArrayList<Record>  mRankList;
        
        public RankAdapter(Context  cxt) {
            mInflater = LayoutInflater.from(cxt);
            mRankList = new ArrayList<Record>();
            LoadData();
        }
        

        public boolean LoadData() {
            return RankActivity.LoadData(mRankList);
        }

        @Override
        public int getCount() {
            return mRankList.size();
        }
        
        @Override
        public Record getItem(int i) {
            return mRankList.get(i);
        }
        
        @Override
        public long getItemId(int i) {
            return i;
        }
        
        public class ViewHolder {
            Record  record;
            TextView  desc;
        }
        
        @Override
        public View getView(int pos, View view, ViewGroup parent) {
            final ViewHolder  holder;
            
            if (view == null || view.getTag() == null) {
                view = mInflater.inflate(R.layout.rank_item, null);
                holder = new ViewHolder();
                holder.desc = (TextView)view.findViewById(R.id.record_item);
            } else {
                holder = (ViewHolder)view.getTag();
            }
            
            holder.record = getItem(pos);
            holder.desc.setText((pos + 1) + ". " + holder.record.toString());

            view.setTag(holder);    
            
            return  view;
        } 
    }
    
    private static DataInputStream  mDis = null;
    private static DataOutputStream mDos = null;
    
    private static void OpenInput() {
       // Resource.Assert(mDis == null, "Must be null input stream");
        if (null != mDis) {
            try {
                mDis.close();
            } catch (IOException e) {
            }
        }

        try {
            Log.d(TAG, "Open input stream");
            mDis = new DataInputStream(MineSweeper.Instance().openFileInput(RECORD_FILE));
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "Open file for read failed ");
        }
    }
    
    private static void OpenOutput() {
        if (mDos != null) {
            try {
                mDos.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        try {                
            mDos = new DataOutputStream(MineSweeper.Instance().openFileOutput(RECORD_FILE, MODE_APPEND));
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "Open file for write failed ");
        }
    }
    
    static {
        OpenInput();
        OpenOutput();
        Log.d(TAG, "Static init file stream");
    }
    
    public static int GetRank(long score) {
        ArrayList<Record>  list = new ArrayList<Record>();
        LoadData(list);

        int  rank = 0;
        for (int i = 0; i < list.size(); ++ i) {
            Log.d(TAG, "compare " + list.get(i).score);
            if (list.get(i).score <= score) // 时间相等的话，先来后到
                ++ rank;
        }

        Log.d(TAG, "GetRank " + rank + ", time " + score);
        
        return rank;
    }

    public static boolean LoadData(ArrayList<Record> rankList) {
       
        OpenInput();
        
        rankList.clear();
    
        try {
            while (rankList.size() < MAX_RANK) {
                Record rec = new Record();
                rec.name   = mDis.readUTF();
                rec.score  = mDis.readLong();
                rec.date   = mDis.readUTF(); 
            
                Log.d(TAG, "Load name " + rec.name + ", date " + rec.date);
                rankList.add(rec);
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (e instanceof EOFException)
                return true;
        
            Log.d(TAG, "Can not read record");
        } finally {
            // sort by score
            List<Record> tmpList = rankList.subList(0, rankList.size());
            java.util.Collections.sort(tmpList);
        }

        Log.d(TAG, "Try loaddata " + rankList.size());
        
        return  true;
    }
    
    public static final int MAX_NAME_LEN = 8;
    public  static boolean Save(String  name, long score, int rank) {
        Resource.Assert(rank >= 0 && rank < MAX_RANK, "Wrong rank " + rank);
        
        if (name.length() > MAX_NAME_LEN)
            name = name.substring(0, MAX_NAME_LEN);
        
        ArrayList<Record> tmp = new ArrayList<Record>();
        LoadData(tmp);
        List<Record>  list = tmp.subList(0, tmp.size());
        
        Record rec = new Record();
        rec.name = name;
        rec.score = score;
        rec.date = new Date().toLocaleString();
        list.add(rank, rec);

        try {
            String cleanStr = "";
            FileOutputStream os = MineSweeper.Instance().openFileOutput(RECORD_FILE, MODE_PRIVATE);
            os.write(cleanStr.getBytes());
            os.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        
        OpenOutput();
        
        try {
            for (int i = 0; i < MAX_RANK && i < list.size(); ++ i) {
                mDos.writeUTF(list.get(i).name);   // name
                mDos.writeLong(list.get(i).score); // used time
                mDos.writeUTF(list.get(i).date);   // date
                Log.d(TAG, "Save " + list.get(i).name + " date " + list.get(i).date);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "Can not save record");
        }
        
        return  true;
    }
}
