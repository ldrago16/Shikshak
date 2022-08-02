//package com.example.attend;
//
//import android.graphics.Bitmap;
//import android.graphics.Canvas;
//import android.graphics.Color;
//import android.graphics.Paint;
//import android.util.LruCache;
//import android.util.TypedValue;
//import android.view.View;
//import android.widget.Button;
//import android.widget.TextView;
//
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.constraintlayout.widget.ConstraintLayout;
//import androidx.recyclerview.widget.RecyclerView;
//
//public class PrintAttand extends AppCompatActivity {
//    public Bitmap getScreenshotOfAttand(RecyclerView view) {
//        //AppCompatActivity appCompatActivity = new AppCompatActivity()
//        //appCompatActivity.setContentView(R.layout.attend_activity_takeattandance);
//        // setContentView(R.layout.attend_activity_takeattandance);
//        attend_ProgrammingAdapter adapter =(attend_ProgrammingAdapter) view.getAdapter();
//        Bitmap bigBitmap = null;
//        int height = 0;
//        int size = adapter.getItemCount();
//        Paint paint = new Paint();
//        int iHeight = 0;
//        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
//        final int cacheSize = maxMemory / 8;
//        LruCache<String, Bitmap> bitmaCache = new LruCache<>(cacheSize);
//
//
//        if (adapter != null) {
//            {
//                ConstraintLayout c = attend_TakeAttandanceActivity.yearSubTimeLayout;
//                c.setDrawingCacheEnabled(true);
//                c.buildDrawingCache(true);
//                Bitmap bitmap1Temp = c.getDrawingCache();
//                if (bitmap1Temp != null) {
//                    bitmaCache.put(String.valueOf(-2), bitmap1Temp);
//                }
//                height += bitmap1Temp.getHeight();
//            }
//
//            {
//                ConstraintLayout c = attend_TakeAttandanceActivity.studentPresentAbsentLeaveLayout;
//                TextView tv = (TextView) c.getViewById(R.id.studentNameInfo);
//                tv.setText("Student/FatherName");
//                c.setDrawingCacheEnabled(true);
//                c.buildDrawingCache(true);
//                Bitmap bitmap2Bitmap = c.getDrawingCache();
//                if (bitmap2Bitmap != null) {
//                    bitmaCache.put(String.valueOf(-1), bitmap2Bitmap);
//                }
//                height += bitmap2Bitmap.getHeight();
//            }
//
//
//            for (int i = 0; i < size; i++) {
//                attend_ProgrammingAdapter.ProgrammingViewHolder holder = adapter.onCreateViewHolder(view, adapter.getItemViewType(i));
//                adapter.bindViewHolderForPrintAttand(holder, i);
//                holder.itemView.measure(View.MeasureSpec.makeMeasureSpec(view.getWidth(), View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
//                holder.itemView.layout(0, 0, holder.itemView.getMeasuredWidth(), holder.itemView.getMeasuredHeight());
//                holder.itemView.setDrawingCacheEnabled(true);
//                holder.itemView.buildDrawingCache();
//                Bitmap drawiwngCache = holder.itemView.getDrawingCache();
//                if (drawiwngCache != null) {
//                    bitmaCache.put(String.valueOf(i), drawiwngCache);
//                }
//                height += holder.itemView.getMeasuredHeight();
//
//            }
//
//            {
//                ConstraintLayout c = attend_TakeAttandanceActivity.palTotalLayout;
//
//                Button submit = (Button) c.getViewById(R.id.submitAttandance);
////                submit.setBackgroundResource(R.color.backgroundColor);
//                submit.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25f);
//                submit.setText("TOTAL");
//
//                c.setDrawingCacheEnabled(true);
//                c.buildDrawingCache(true);
//                Bitmap bitmap3Temp = c.getDrawingCache();
//                if (bitmap3Temp != null) {
//                    bitmaCache.put(String.valueOf(-3), bitmap3Temp);
//                }
//                height += bitmap3Temp.getHeight();
//            }
//
//            bigBitmap = Bitmap.createBitmap(view.getMeasuredWidth(), height, Bitmap.Config.ARGB_8888);
//            Canvas bigCanvas = new Canvas(bigBitmap);
//            bigCanvas.drawColor(Color.WHITE);
//
//            for (int i = -2; i < size; i++) {
//                Bitmap bitmap2 = bitmaCache.get(String.valueOf(i));
//                bigCanvas.drawBitmap(bitmap2, 0f, iHeight, paint);
//                iHeight += bitmap2.getHeight();
//                bitmap2.recycle();
//            }
//
//            Bitmap bitmap2 = bitmaCache.get(String.valueOf(-3));
//            bigCanvas.drawBitmap(bitmap2, 0f, iHeight, paint);
//            bitmap2.recycle();
//        }
//        return bigBitmap;
//    }
//}