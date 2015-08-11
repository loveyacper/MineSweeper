package bert.young.mine;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

class FaceButton extends ImageButton {

    public FaceButton(Context context) {
        super(context);
        Log.d("GameActivity",  "Construct 1");
    }
    
    public FaceButton(Context cxt, AttributeSet as) {
        super(cxt, as);
        Log.d("GameActivity",  "Construct 2");

    }
    
    void SetNormal(){
        setImageResource(R.drawable.face_selector);
        //setImageResource(R.drawable.face);
    }
    
    void SetPressed() {
        setImageResource(R.drawable.face_press);
    }
    
    void SetCry() {
        setImageResource(R.drawable.face_cry);
    }
    
    void SetLaugh() {
        setImageResource(R.drawable.face_laugh);
    }
}

public class GameActivity extends Activity implements OnTouchListener {
    private static final String TAG = "GameActivity";
    
    public static final String PREF_SAVEGAME = "MineSweeper";
    public static final String PREF_CONTINUE = "Continue";
    
    private GameView  mView;
    private FaceButton mFace;
    private Rect      mViewRect;
    private TextView  mBombText;
    private TextView  mTimeText;
    
    void UpdateBombCount(int count) {
        CharSequence  text ;
        if (count < 10) 
            text = "00" + count;
        else if (count < 100)
            text = "0" + count;
        else
            text = "" + count;
        
        mBombText.setText(text);
    }
    
    void UpdateFace(GameState state) {
        switch (state) {
        case GameStateNone:
        case GameStatePlaying:
            mFace.SetNormal();
            break;
            
        case GameStateWin:
            mFace.SetLaugh();
            break;
            
        case GameStateLose:
            mFace.SetCry();
            break;
        }
    }
    
    private class Timer extends CountDownTimer {
        private long mTotal;
        private long mSavePassed;
        private long mPassed;
        
        public Timer(long total, long interval){
            super(total, interval); 
            
            if (total > 999 * 1000)
                total = 999 * 1000;
            mTotal = total;
            mPassed= total;
        }
        
        @Override
        public void onTick(long remained) {
            mPassed = mTotal - remained;
            
            long passed = (mPassed + mSavePassed) / 1000;
            
            CharSequence text = "";
            if (passed < 10) 
                text = "00" + passed;
            else if (passed < 100)
                text ="0" + passed;
            else
                text = "" + passed;
            
            mTimeText.setText(text);
        }
        
        @Override
        public void onFinish() {
            Log.d(TAG, "onFinish");
            mPassed = mTotal;
            mTimeText.setText("" + mPassed / 1000);
        }
    }
    
    private Timer mTimer;
    public void StartTimer(long total, long interval) {
        if (mTimer != null)  mTimer.cancel();

        Log.d(TAG, "Start timer total " + total + ", interval " + interval);
        mTimer = new Timer(total, interval);
        mTimer.start();
    }
    
//    public void UpdateTimeText(long  passed) {
//        String text ="000";
//        if (passed < 10)
//            text = "00" + passed;
//        else if (passed < 100)
//            text = "0" + passed;
//        else
//            text = "" + passed;
//        
//        mTimeText.setText(text);
//    }
    
    public void StartTimer(String saved) {
        if (mTimer != null)  mTimer.cancel();
        
        String total  = saved.substring(0, 3);
        String passed = saved.substring(3);
        Log.d(TAG, "Start timer total " + total + ", passed " + passed);
        
        mTimer = new Timer(Long.parseLong(total) * 1000, 1000);
        mTimer.mSavePassed = Long.parseLong(passed) * 1000;
        mTimer.start();
    }
    
    public void StopTimer() {
        if (mTimer != null)
            mTimer.cancel();
    }
    
    public long PassedTime() {
        if (mTimer != null)
            return (mTimer.mSavePassed + mTimer.mPassed) / 1000;
        
        return 0;
    }
    
    public String SaveTimer() {
        if (mTimer == null)
            return "999000";
        
        String total = new String();
        long   totalSec = mTimer.mTotal / 1000;
        if (totalSec < 10)
            total = "00" + totalSec;
        else if (mTimer.mTotal < 100)
            total = "0" + totalSec;
        else
            total = "" + totalSec;
        
        String passed = new String();
        long   passSec = (mTimer.mSavePassed + mTimer.mPassed) / 1000 ;
        if (passSec < 10)
            passed = "00" + passSec;
        else if (passSec < 100)
            passed = "0" + passSec;
        else
            passed = "" + passSec;
        
        Log.d(TAG, "Save timer " + total + passed);
        
        return total + passed;
    }
    
    private static  GameActivity  s_act;
    public  static  GameActivity Instance() {
        return  s_act;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        s_act = this;
        
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        final int width  = metric.widthPixels;     // ÆÁÄ»¿í¶È£¨ÏñËØ£©
        
        int marginTotal = width % GameBoard.BOARD_WIDTH;
        if (marginTotal == 0)
            marginTotal = GameBoard.BOARD_WIDTH;

        Log.d(TAG, this + " OnCreate: Screen density = "  + metric.density + " width = " + width + ", margin total " + marginTotal);
        
        LinearLayout   root  = (LinearLayout)LayoutInflater.from(this).inflate(R.layout.game, null);
        LinearLayout.LayoutParams  params  =  new LinearLayout.LayoutParams(width, width);
        params.leftMargin =  params.topMargin = params.rightMargin = params.bottomMargin = marginTotal / 2;  
        
        mFace = (FaceButton)root.findViewById(R.id.face_btn);
        mFace.setOnTouchListener(this);
        
        mBombText = (TextView)root.findViewById(R.id.mines_cnt);
        mTimeText = (TextView)root.findViewById(R.id.timer);
        
        mView =  (GameView)root.findViewById(R.id.gameview);
        mView.setLayoutParams(params);
        mView.Init((width - marginTotal) / GameBoard.BOARD_WIDTH);
        mView.setOnTouchListener(this);
        
        setContentView(root);
    }

    private GestureDetector mGestureDetector = new GestureDetector(new MyGestureListener());
    
    @Override
    public boolean onTouch(View view, MotionEvent event) {  
        if (mViewRect == null) {
            mViewRect = new Rect();
            mView.getGlobalVisibleRect(mViewRect, new Point());
            Log.d(TAG, "view left " + mViewRect.left + ", top " + mViewRect.top);
        }

        final int viewId = view.getId();
        final int action = event.getAction();

        if (viewId == R.id.face_btn) {
            view.setPressed(action != MotionEvent.ACTION_UP);
            if (action == MotionEvent.ACTION_DOWN) {
                mView.mBoard.Init(0);
                StopTimer();
                mTimer = null;
                mTimeText.setText("000");
            }

            return  true;
        }

        return mGestureDetector.onTouchEvent(event);
    }
    
    private class MyGestureListener extends SimpleOnGestureListener {
        @Override
        public  boolean onDoubleTap(MotionEvent event) {
            Log.d(TAG, "onDoubleTap");
            
            int x = (int)event.getRawX();
            int y = (int)event.getRawY();
            x -= mViewRect.left;
            y -= mViewRect.top;
            
            mView.OpenNeighbours(x, y);
            
            return  true;
        }
        
        @Override 
        public boolean onSingleTapConfirmed(MotionEvent event) {
            Log.d(TAG, "onSingleTapConfirmed " + event.getRawY());
            int x = (int)event.getRawX();
            int y = (int)event.getRawY();
            x -= mViewRect.left;
            y -= mViewRect.top;
            
            mView.Open(x, y);
            if (mView.mBoard.mState == GameState.GameStateLose) {
                mFace.SetCry();
            }
            return true;
        }
        
        @Override 
        public void onLongPress(MotionEvent event) {
            
            Log.d(TAG, "onLongPress");
            int x = (int)event.getRawX();
            int y = (int)event.getRawY();
            x -= mViewRect.left;
            y -= mViewRect.top;
            
            mView.Flag(x, y);
        }
        
        void test() {
            this.onDoubleTap(null);//
            this.onDoubleTapEvent(null);
            this.onDown(null);
            this.onLongPress(null);//
            this.onSingleTapConfirmed(null);//
            this.onSingleTapUp(null);
        }
    
    }
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "OnResume");
        
        //Reload game
        String  saved = getPreferences(MODE_PRIVATE).getString(PREF_SAVEGAME,  "");
        mView.LoadGame(saved);
    }
    
    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "OnRestart");
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        
        int isContinue = getIntent().getIntExtra(PREF_CONTINUE, 0);
        Log.d(TAG, "OnStart, game continued ? " + isContinue);

        if (isContinue == 0) {
            getPreferences(MODE_PRIVATE).edit().putString(PREF_SAVEGAME, "").commit();
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "Pause ");
        
        StopTimer();
        // Save game
        getPreferences(MODE_PRIVATE).edit().putString(PREF_SAVEGAME,
                mView.SaveGame()).commit();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Destroy");
    }
    
    private final int NAME_DLG_ID = 123;
    public void ShowNameDlg() {
        showDialog(NAME_DLG_ID);
    }

    @Override
    public Dialog onCreateDialog(int id) {
        if (id != NAME_DLG_ID)  return null;
       
        final int rank = RankActivity.GetRank(GameActivity.Instance().PassedTime());
        if (rank >= RankActivity.MAX_RANK) {
            Log.d(TAG, "Return null dlg because rank is " + rank);
            return null;
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.record_text);
        final View view = LayoutInflater.from(this).inflate(R.layout.record_name, null);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view)
        // Add action buttons
               .setPositiveButton("È·¶¨", new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int id) {
                       TextView text = (TextView)view.findViewById(R.id.playername);
                      
                       Log.d(TAG, "onclick " +  text.getText());
                       GameActivity.Instance().SaveRecord(text.getText().toString(),
                               GameActivity.Instance().PassedTime(),
                               rank);
                       MineSweeper.Instance().ShowRankList();
                   }
               });      
        return builder.create();
    }
    
    public void SaveRecord(String  name, long  score, int rank) {
        //if (RankActivity.GetRank(score) < RankActivity.MAX_RANK)
            RankActivity.Save(name, score, rank);
    }
}
