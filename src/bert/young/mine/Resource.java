package bert.young.mine;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

final class Resource {
    private static Bitmap   mClosed  = null;   
    private static Bitmap   mFlag    = null;
    private static Bitmap[] mValues  = new Bitmap[10];

    /** 资源初始化 */
    static  boolean Init(Resources  resource) {
        if (null == resource)
            return  false;

        mClosed = BitmapFactory.decodeResource(resource, R.drawable.closed);
        mFlag   = BitmapFactory.decodeResource(resource, R.drawable.flag);
        mValues[0] = BitmapFactory.decodeResource(resource, R.drawable.value_0);
        mValues[1] = BitmapFactory.decodeResource(resource, R.drawable.value_1);
        mValues[2] = BitmapFactory.decodeResource(resource, R.drawable.value_2);
        mValues[3] = BitmapFactory.decodeResource(resource, R.drawable.value_3);
        mValues[4] = BitmapFactory.decodeResource(resource, R.drawable.value_4);
        mValues[5] = BitmapFactory.decodeResource(resource, R.drawable.value_5);
        mValues[6] = BitmapFactory.decodeResource(resource, R.drawable.value_6);
        mValues[7] = BitmapFactory.decodeResource(resource, R.drawable.value_7);
        mValues[8] = BitmapFactory.decodeResource(resource, R.drawable.value_8);
        mValues[9] = BitmapFactory.decodeResource(resource, R.drawable.value_bomb);
        
        if (mClosed == null ||
            mFlag   == null) {
            return false;
        }

        return  true;
    }

    static int ImgSize() {
        return  mClosed.getHeight() / 2;
    }
    
    static Bitmap  ClosedImg() {
        return mClosed;
    }
    
    static Bitmap  FlagImg() {
        return mFlag;
    }
    
    static Bitmap ValueImg(int i) {
        return mValues[i];
    }
    
    /** test only 不会用Android的assert，没法。。。*/
    static final int Assert(boolean condition , String str) {
        if (condition)
            return 0;

        System.out.println("BUG ASSERT : " + str);
        int a  = 5 / 0;
        return  a;
    }
}
