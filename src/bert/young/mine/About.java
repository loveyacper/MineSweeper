package bert.young.mine;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;

public class About extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
    }
    
    /** ���� ���������� */
    @Override
    public boolean onTouchEvent(MotionEvent  event) {
        if (event.getAction() != MotionEvent.ACTION_UP)
            return   super.onTouchEvent(event);
        
        finish();
        return  true;
    }
}