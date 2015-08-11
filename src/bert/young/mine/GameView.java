package bert.young.mine;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class GameView extends View {

    private Paint     mPaint = new Paint();
    public GameBoard mBoard = new GameBoard(this);

    // timer to keep track of time elapsed
    private Handler mTimer  = new Handler();
    private int mTimePassed = 0;

    public GameView(Context context) {
        super(context);
        Log.d("GameView",  "Construct 1");
        // TODO Auto-generated constructor stub;
    }

    public GameView(Context context, AttributeSet attr) {
        super(context, attr);
        Log.d("GameView",  "Construct 2");
    }
    
    boolean  Init(int  width) {
        mPaint.setColor(Color.DKGRAY);
        mBoard.Init(width);
        
        Log.d("GameView",  width + " Init ");
        
        Resource.Init(getResources());
        
        return  true;
        
    }
    
    String SaveGame() {
        return mBoard.Save();
    }
    
    void  LoadGame(String  saved) {
        mBoard.Load(saved);
    }
    
//    @Override
//    public boolean onTouchEvent(MotionEvent  event) {
//        
//        int x = (int)event.getX();
//        int y = (int)event.getY();
//        Log.d("GameView", " game view on touch " + event.getAction() + ", y " + y);
//
//        mBoard.Open(mBoard.Pixel2Coord(y), mBoard.Pixel2Coord(x));
//        
//        return super.onTouchEvent(event) || true;
//    }
    public void Open(int x, int y) {
       mBoard.Open(mBoard.Pixel2Coord(y), mBoard.Pixel2Coord(x));
    }
    
    public void OpenNeighbours(int x, int y) {
        mBoard.OpenNeighbours(mBoard.Pixel2Coord(y), mBoard.Pixel2Coord(x));
    }
    
    public void Flag(int x, int y) {
        mBoard.Flag(mBoard.Pixel2Coord(y), mBoard.Pixel2Coord(x));
    }
    
    @Override
    public void onDraw(Canvas canvas) {
        int h = this.getHeight();
        int w = this.getWidth();
        Log.d("GameView",   Thread.currentThread().getId() + ":  " + h + ", w " + w);
        mBoard.Paint(mPaint, canvas);
    }
}
