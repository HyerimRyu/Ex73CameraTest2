package kr.co.teada.ex73cameratest2;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    ImageView iv;

    //캡쳐한 이미지를 저장할 파일의 경로 Uri 객체 참조변수 (어디서든 사용가능)
    Uri imgUri=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        iv=findViewById(R.id.iv);

        //외부 동적퍼미션 : 외부 메모리 사용에 대한 퍼미션(다이얼로그로..)
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            int permissionResult=checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if(permissionResult==PackageManager.PERMISSION_DENIED){
                String[] permissions=new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
                requestPermissions(permissions, 100);
            }
        }

    }//end of onCreate

    //onRequestPermissionsResult

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case 100:
                if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this, "카메라앱 사용가능", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(this, "카메라 기능 제한", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    public void clickBtn(View view) {

        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        //이미지 경로 설정
        setImageUri();


        //추가 데이터 요청
        //캡쳐한 사진을 제공한 File 의 경로에 저장해주세요!
        if(imgUri!=null) intent.putExtra(MediaStore.EXTRA_OUTPUT, imgUri); //두 번째 파라미터: 파일이 저장된 경로 Uri
        //자동저장된 이미지는 실 사이즈의 이미지임.

        //이미지 uri 가 맞는지 확인
        //new AlertDialog.Builder(this).setMessage(imgUri.toString()).show();

      startActivityForResult(intent,10);

    }//end of clickBtn()

    //onActivityResult : clickBtn(){} 밖에

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case 10:
                if(resultCode == RESULT_OK){
                    Uri uri=null;

                    //Intent 객체(data)가 없다면
                    if (data!=null) uri=data.getData();

                    if (uri!=null){
                        Picasso.get().load(uri).into(iv);
                    }else{
                        //Bitmap 으로 섬네일 이미지 주는 방식!!
                        //putExtra() 를 이용하여 저장할 파일의 경로를 주었다면
                        //그 경로에 캡쳐된 이미지가 저장되었을 테니 그 경로를 이용하여 이미지뷰에 보여주기
                        Picasso.get().load(imgUri).into(iv);

                        //이미지뷰에 이미지가 보인다면
                        //파일 저장이 잘 된거야.
                        //다만, 갤러리앱에서 보면 없어!!!!!!!!!(디바이스마다 달러.. 없을 수도 있고 있을 수도 있어 : 대부분 없어)
                        //갤러리앱에서 사진의 존재를 모를 것이므로
                        //새로 스캔하도록 요청
                        //요청은 방송으로 해 --> Broadcast
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        intent.setData(imgUri);
                        sendBroadcast(intent);

                    }
                }

                break;
        }

    } //end of onActivityResult

    //저장될 이미지의 경로 설정 메소드
    void setImageUri(){

        //구글에서는 이미지를 외부 저장소(SD card) 에 저장하길 권장!!

        //외부저장소는 2개의 영역 중 하나 선택

        //1. 외부저장소의 앱 전용 영역 : 잘 안써 : 왜? 앱을 지우면 파일도 같이 사라져
        File path=getExternalFilesDir("photo"); //외부 메모리의 본인 패키지명으로 된 폴더가 생기고, 그 안에 files 폴더가 생기고 그 안에 "photo"라는 이름의 폴더가 생기며 그 위치를 지칭함
        if(!path.exists()) path.mkdirs(); //폴더가 없으면 생성

        //2. 외부 저장소의 공용영역 : 앱을 지워도 파일은 지워지지 않아 like 비트윈 : 이거써~~~ Environment 한테 얻어와 공용이니까
        path=Environment.getExternalStorageDirectory(); //외부 메모리의 root(최상위)경로
        path=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        //3. 저장할 파일의 이름을 포함한 경로 생성

        //파일명이 중복되지 않돋록
        //1) 날짜 이용 <---이거써!
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddhhmmss");
        String fileName="IMG_"+simpleDateFormat.format(new Date())+".jpg";

        File file = new File(path, fileName);

        //2) 자동으로 임시 파일명 만들어주는 메소드
//        try {
//            file=File.createTempFile("IMG_",".jpg",path);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        //카메라앱에 파일경로를 전달할 때는 File 객체가 아니라
        //Uri 객체를 변환하여 달라고 함 : 보통 메소드로 해놔 구글에서 찾아서 복붙

        //Nougat 버전 전과 후가 다름
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N){
            imgUri = Uri.fromFile(file);
        }else{
            //누가 버전부터 보안강화를 위해 파일의 경로 "file://" 글자가 있는 경로는 다른 앱에 전달할 수 없어
            //그래서 Uri 로 변경해서 보냈으며 이 때 사용한 메소드가 Uri.fromFile() 였는데.. 너무 쉽게 바뀌는게 위험하다고 생각해서
            //위의 메소드를 보안상 금지 시켰어. 그래서 이제부터는 File Provider 를 안 쓰면 못 쓰도록 변경! 꼭 써야해!
            imgUri=FileProvider.getUriForFile(this, "kr.co.teada.ex73cameratest2.FileProvider" ,file);

        }

    }

}//end of MainAc..
