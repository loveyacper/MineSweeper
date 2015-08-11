package bert.young.mine;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MineSweeper extends Activity {
    public static final String  TAG = "MineSweeper";
    private static MineSweeper mInstance = null;
    
    public static MineSweeper Instance() {
        return  mInstance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
       
        mInstance = this;
    }

    public void onClickButton(View v) {
        switch (v.getId()) {
        case R.id.start_id:
            Log.d(TAG, "on click new game");
            startGame(0);
            break;
            
        case R.id.continue_id:
            Log.d(TAG, "on click continue game");
            startGame(1);
            break;
            
        case R.id.rank_id:
            ShowRankList();
            break;
            
        case R.id.about_id:
            startActivity(new Intent(this, About.class));
            break;

        default:
            Log.d(TAG, "fuck you error");
            break;
        }   
    }

    public static final int MAX_RANK  = 5;
    
    public void ShowRankList() {
        Intent intent = new Intent(MineSweeper.this, RankActivity.class);
        startActivity(intent);
    }

    private boolean startGame(int continued) {
        Intent intent = new Intent(MineSweeper.this, GameActivity.class);
        intent.putExtra(GameActivity.PREF_CONTINUE, continued);
        startActivity(intent);
        return  true;
    }
}
