package jp.techacademy.noriko.seri.autoslideshowapp;

import android.Manifest;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.content.pm.PackageManager;
import android.os.Build;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.ImageView;

import java.util.Timer;
import java.util.TimerTask;
import android.os.Handler;
import android.widget.TextView;;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    Timer mTimer;

    Button mPrevButton;
    Button mStartButton;
    Button mNextButton;

    Cursor mCursor;


    Handler mHandler = new Handler();


    private static final int PERMISSIONS_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPrevButton = (Button) findViewById(R.id.button1);
        mPrevButton.setOnClickListener(this);

        mStartButton = (Button) findViewById(R.id.button2);
        mStartButton.setOnClickListener(this);

        mNextButton = (Button) findViewById(R.id.button3);
        mNextButton.setOnClickListener(this);

        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo();
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo();
        }

    }

    @Override
    protected void onDestroy(){

        super.onDestroy();

        if(mCursor != null) {
            mCursor.close();
        }
    }

    @Override
    public void onClick(View v) {

        if( mCursor != null ) {
            // 戻るボタン
            if (v.getId() == R.id.button1) {

                getPrevInfo();

                //再生・停止ボタン
            } else if (v.getId() == R.id.button2) {


                // 再生
                if(mStartButton.getText().toString().equals("再生")){
                    // タイマー開始
                    mTimer = new Timer();
                    mTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {

                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    getNextInfo();
                                }
                            });
                        }
                    }, 2000, 2000);

                    // テキスト変更
                    mStartButton.setText("停止");

                    // 進む・戻るボタン無効
                    mPrevButton.setEnabled(false);
                    mNextButton.setEnabled(false);

                }
                // 停止
                else {
                    // タイマー停止
                    mTimer.cancel();

                    // テキスト変更
                    mStartButton.setText("再生");

                    // 進む・戻るボタン有効
                    mPrevButton.setEnabled(true);
                    mNextButton.setEnabled(true);


                }


                //進むボタン
            } else if (v.getId() == R.id.button3) {

                getNextInfo();

            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo();
                } else {
                    TextView textView = (TextView)findViewById(R.id.message);
                    textView.setVisibility(View.VISIBLE);
                }
                break;
            default:
                TextView textView = (TextView)findViewById(R.id.message);
                textView.setVisibility(View.VISIBLE);

                break;
        }
    }

    // 画像情報を取得
    private void getContentsInfo() {

        // 画像の情報を取得する
        if(mCursor == null) {
            ContentResolver resolver = getContentResolver();
            mCursor = resolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
                    null, // 項目(null = 全項目)
                    null, // フィルタ条件(null = フィルタなし)
                    null, // フィルタ用パラメータ
                    null // ソート (null ソートなし)
            );


            mCursor.moveToFirst();

        }

        // 最初の画像を表示
        getPicture();


    }

    // 最初の画像表示
    private void getPicture() {

        int fieldIndex = mCursor.getColumnIndex(MediaStore.Images.Media._ID);
        Long id = mCursor.getLong(fieldIndex);
        Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

        ImageView imageVIew = (ImageView) findViewById(R.id.imageView);
        imageVIew.setImageURI(imageUri);

    }

    // 戻るボタン
    private void getPrevInfo() {

        if (mCursor.moveToPrevious()) {
            getPicture();

        } else {

            mCursor.moveToLast();
            getPicture();
        }

    }

    // 次へボタン
    private void getNextInfo() {

        if (mCursor.moveToNext()) {

            getPicture();

        } else {

            mCursor.moveToFirst();
            getPicture();

        }

    }

}
