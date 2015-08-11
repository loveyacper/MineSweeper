package bert.young.mine;

import java.util.HashMap;
import java.util.Random;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

enum GridValue
{
    GridValue0,
    GridValue1,
    GridValue2,
    GridValue3,
    GridValue4,
    GridValue5,
    GridValue6,
    GridValue7,
    GridValue8,
    GridValueBomb,
}

enum GridState
{
    GridStateClosed,
    GridStateFlag,
    GridStateOpened, // has 8 values or bomb
}

enum GameState {
    GameStateNone,
    GameStatePlaying,
    GameStateWin,
    GameStateLose,
}

class GameBoard {
    private static final String TAG         = "GameBoard";
    public  static final int BOARD_WIDTH    = 12;
    public  static final int BOMB_NUM       = 25;    
    private static final Random mRand       = new Random();
    
    public  GameState mState ;
    
    class Grid   {
        GridValue value = GridValue.GridValue0;
        GridState state = GridState.GridStateClosed;
    }
    
    public GameBoard(GameView view) {
        mView  = view;
        mState = GameState.GameStateNone;
    }
    
    String  Save() {
        if (mState == GameState.GameStateNone) {
            Log.d(TAG, "Save but not playing ");    
            return  "";
        }
        
        String  result = new String();    
        for (int i = 0; i < mGrids.length; ++ i) {
            for (int j = 0; j < mGrids[i].length; ++ j) {                    
                result += mGrids[i][j].state.ordinal();
                result += mGrids[i][j].value.ordinal();
            }
        }
        
        result += mState.ordinal();
        
        result += GameActivity.Instance().SaveTimer();
        
        Log.d(TAG, "Save result " + result);    
        return   result;
    }
    
    void Load(String  saved) {
        Log.d(TAG, "Load " + saved);

        if (saved.length() == 0)
            return;
        
        mBombs = new HashMap<Integer, Boolean>();
        
        for (int i = 0; i < mGrids.length; ++ i) {
            for (int j = 0; j < mGrids[i].length; ++ j) {    
                int state = saved.charAt(2 * (i * mGrids[0].length + j)) - '0';
                int value = saved.charAt(2 * (i * mGrids[0].length + j) + 1) - '0';
                mGrids[i][j].state = GridState.values()[state];
                mGrids[i][j].value = GridValue.values()[value];
                
                if (mGrids[i][j].value == GridValue.GridValueBomb) {
                    mBombs.put(Key(i, j), mGrids[i][j].state == GridState.GridStateFlag);
                }
            }    
        }
        
        int state  = saved.charAt(2 * (mGrids.length * mGrids[0].length)) - '0';
        mState = GameState.values()[state];
        
        String savedTimer = saved.substring(2 * (mGrids.length * mGrids[0].length) + 1,
                2 * (mGrids.length * mGrids[0].length) + 7);
 
        if (mState == GameState.GameStatePlaying)
            GameActivity.Instance().StartTimer(savedTimer);
        //else
          //  GameActivity.Instance().UpdateTimeText(Long.parseLong(savedTimer.substring(3)));
        
        GameActivity.Instance().UpdateFace(mState);
        GameActivity.Instance().UpdateBombCount(BOMB_NUM - FlagsCount());
        Log.d(TAG, "Load game state " + mState);
    }
    
    private int Key(int x, int y) {
        return (x << 16) | y;
    }

    private GameView  mView;
    private Grid[][]  mGrids;
    private int       mWidth;
    private HashMap<Integer, Boolean>  mBombs;
    
    GridValue  CalcValue(int x, int y) {
        int  nBombs = 0;
        for (int i = 0; i < mOffset.length; ++ i) {
            int tmpX = x + mOffset[i][0];
            int tmpY = y + mOffset[i][1];
            if (tmpX >= 0 &&
                tmpX < mGrids.length &&
                tmpY >= 0 &&
                tmpY < mGrids[tmpX].length) {
                if (mGrids[tmpX][tmpY].value == GridValue.GridValueBomb)
                    ++ nBombs;
            }
        }
        
        Resource.Assert(nBombs <= 8, "Wrong bomb number " + nBombs);
        
        return GridValue.values()[nBombs];
    }
    
    private int FlagsCount() {
        int  cnt = 0;
        for (int i = 0; i < mGrids.length; ++ i) {
            for (int j = 0; j < mGrids[i].length; ++ j)
                if (mGrids[i][j].state == GridState.GridStateFlag)
                    ++ cnt;
        }

        return cnt < BOMB_NUM ? cnt : BOMB_NUM;
    }
    
    void Init(int width) {
        if (width  > 0 &&
            width  < 1024)
            mWidth  = width;
        
        Log.d(TAG, mView + " width is " + mWidth);
        
        mGrids = new Grid[BOARD_WIDTH][];
        for (int i = 0; i < mGrids.length; ++ i) {
            mGrids[i] = new Grid[BOARD_WIDTH];
            for (int j = 0; j < mGrids[i].length; ++ j)
                mGrids[i][j] = new Grid();
        }
        mView.invalidate();
        
        mState = GameState.GameStateNone;
        GameActivity.Instance().UpdateFace(mState);
        GameActivity.Instance().UpdateBombCount(BOMB_NUM);
    }
    
    boolean CheckWin() {
        return  !mBombs.containsValue(false);
    }
    
    private void  InitBombs(int firstX, int firstY) {
        mRand.setSeed(System.currentTimeMillis());
        mBombs = new HashMap<Integer, Boolean>();
        while (mBombs.size() != BOMB_NUM) {
            int x = -1, y = -1;
  
            do {
                 x = mRand.nextInt(BOARD_WIDTH);
                 y = mRand.nextInt(BOARD_WIDTH);
            }   while ((x == firstX && y == firstY) || 
                        mBombs.containsKey(Key(x, y))); 
            
            mBombs.put(Key(x, y), false);

            mGrids[x][y].state = GridState.GridStateClosed;
            mGrids[x][y].value = GridValue.GridValueBomb;
        }
        
        for (int i = 0; i < mGrids.length; ++ i) {
            for (int j = 0; j < mGrids[i].length; ++ j) {
                if (mGrids[i][j].value != GridValue.GridValueBomb)
                    mGrids[i][j].value = CalcValue(i, j);
                else
                    Log.d(TAG, "x = " + i + ", y = " + j + " value = " + mGrids[i][j].value);
            }
        }
        
        GameActivity.Instance().StartTimer(999 * 1000, 1000);
    }
    
    private Rect  mDst = new Rect();
    private Rect  mDraw = new Rect();
    
    private final int[][] mOffset={
            {-1, -1},
            {-1,  0},
            {-1,  1},
            { 0, -1},
            { 0,  1},
            { 1, -1},
            { 1,  0},
            { 1,  1},
    };
    
    public int  Pixel2Coord(int pixel) {
        return  pixel / mWidth;
    }
    
    //  触摸  单次
    boolean  Open(int x, int y)
    {
        if (x < 0 ||
            y < 0 ||
            x >= mGrids.length ||
            y >= mGrids[x].length)
            return false;
        
        if (mState == GameState.GameStateNone) {
            mState = GameState.GameStatePlaying;
            this.InitBombs(x, y);
        }   
        
        if (mState != GameState.GameStatePlaying) 
            return false;
        
        if (mGrids[x][y].state != GridState.GridStateClosed)
            return false;

        mGrids[x][y].state = GridState.GridStateOpened;
        
        InvalidView(x, y);
        
        if (mGrids[x][y].value == GridValue.GridValue0) {
            OpenNeighbours(x, y);
        } else if (mGrids[x][y].value == GridValue.GridValueBomb) {
            if (mState != GameState.GameStateLose) {
                mState = GameState.GameStateLose;
                InvalidView();
                GameActivity.Instance().UpdateFace(mState);
                GameActivity.Instance().StopTimer();
                Log.d(TAG, "You lose");
            }
        }
        
        return  true;
    }
    
    //  触摸 两次
    boolean  OpenNeighbours(int x, int y) {  
        if (mState != GameState.GameStatePlaying)
            return false;
        
        if (x < 0 ||
            x >= mGrids.length ||    
            y < 0 ||    
            y >= mGrids[x].length)
            return false;
        
        if (mGrids[x][y].state != GridState.GridStateOpened) {
            return false;
        }
        
        // 必须已经标记了周围地雷
        final int nBombCnt = mGrids[x][y].value.ordinal();
        int flags = 0;
        for (int i = 0; i < mOffset.length; ++ i) {
            int  xx = x + mOffset[i][0];
            int  yy = y + mOffset[i][1];
            
            if (xx < 0 ||
                xx >= mGrids.length ||
                yy < 0 ||
                yy >= mGrids[xx].length)
                continue;
            
            if (mGrids[xx][yy].state == GridState.GridStateFlag)
                ++ flags;
        } 
        
        if (flags != nBombCnt)  {
            Log.d(TAG, "Open neighbor failed, nBomb = " + nBombCnt + ", but flag " + flags);
            return false;
        }
        
        boolean result = false;
        for (int i = 0; i < mOffset.length; ++ i) {
            if (Open(x + mOffset[i][0], y + mOffset[i][1]) && !result)
                result = true;
        } 
        
        return  result;
    }
    
    private void InvalidView(int x, int y) {
        mDraw.left = y * mWidth;
        mDraw.top  = x * mWidth;
        mDraw.right  = mDraw.left + mWidth;
        mDraw.bottom = mDraw.top + mWidth;
        mView.invalidate(mDraw);
    }
    
    private void InvalidView() { 
        mView.invalidate( );
    }
    
    
    //  长按，设置/取消 地雷标志
    boolean  Flag(int x, int y) {
        if (mState != GameState.GameStatePlaying)
            return false;
        
        if (x < 0 ||
            y < 0 ||
            x >= mGrids.length ||
            y >= mGrids[x].length)
            return false;
        
        if (mGrids[x][y].state == GridState.GridStateClosed) {
            mGrids[x][y].state = GridState.GridStateFlag;

            if (mBombs.containsKey(Key(x, y)))
                mBombs.put(Key(x, y), true);
            
            InvalidView(x, y);
            
            // check win condition
            if (CheckWin()) {
                mState = GameState.GameStateWin;
                GameActivity.Instance().UpdateFace(mState);
                GameActivity.Instance().StopTimer();
                Log.d(TAG, "You win in " + GameActivity.Instance().PassedTime());
                
                GameActivity.Instance().ShowNameDlg();
            }
        }
        else if (mGrids[x][y].state == GridState.GridStateFlag) {
            mGrids[x][y].state = GridState.GridStateClosed;
            InvalidView(x, y);
        }
        else {
            return false;
        }
        
        GameActivity.Instance().UpdateBombCount(BOMB_NUM - FlagsCount());
        return true;
    }
    
    void  Paint(Paint  paint, Canvas  canvas) {
        Log.d(TAG, "Draw me");

        for (int i = 0; i < mGrids.length; ++ i) {
            for (int j = 0; j < mGrids[i].length; ++ j) {
                mDst.left = j * mWidth;
                mDst.top  = i * mWidth;
                mDst.right  = mDst.left + mWidth;
                mDst.bottom = mDst.top + mWidth;
                
                Bitmap drawing = null;
                if (mGrids[i][j].state == GridState.GridStateClosed) {
                    if (mState == GameState.GameStateLose && mGrids[i][j].value == GridValue.GridValueBomb)
                        drawing = Resource.ValueImg(GridValue.GridValueBomb.ordinal());
                    else
                        drawing = Resource.ClosedImg();
                } else if (mGrids[i][j].state == GridState.GridStateFlag) {
                    drawing = Resource.FlagImg();
                } else  {
                    drawing = Resource.ValueImg(mGrids[i][j].value.ordinal());
                }

                canvas.drawBitmap(drawing, null, mDst, paint);                
            }
        }
    }
}
