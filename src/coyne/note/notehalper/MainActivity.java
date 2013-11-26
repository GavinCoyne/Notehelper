package coyne.note.notehalper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;


@SuppressLint("SdCardPath")
public class MainActivity extends Activity {

	String root = Environment.getExternalStorageDirectory().toString();

	protected Button _button;
	protected ImageView _image;
	protected TextView _field;
	protected String _imagePath;
	protected boolean _taken;
	protected String recognizedText = "";
	
	protected static final String PHOTO_TAKEN = "photo_taken";
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	
		//make dirs and get assests if they do not exist
		File rootDirs = new File(Environment.getExternalStorageDirectory()+"/tess/tessdata/");
        if (!rootDirs.exists()) {
        	rootDirs.mkdirs();
        }
		
		copyAssets();
		
        _image = ( ImageView ) findViewById( R.id.imageView1 );
        _field = ( TextView ) findViewById( R.id.textViewRecog );
        _button = ( Button ) findViewById( R.id.buttonCamera );

        
        
        _imagePath = Environment.getExternalStorageDirectory() + "/tess/make_machine_example.jpg";


	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	
	
	public void extractText(View v)
	{
		extract(_imagePath);
	}
//	TODO: make into asynk task	
	public void extract(String _path)
	{
		try {
	
			BitmapFactory.Options options = new BitmapFactory.Options();
		    options.inSampleSize = 2;
		    	
		    
		    Bitmap bitmap = BitmapFactory.decodeFile( _path, options );
		   
			
			
			// _path = path to the image to be OCRed
			ExifInterface exif;
			
			exif = new ExifInterface(_path);
			
			int exifOrientation = exif.getAttributeInt(
			        ExifInterface.TAG_ORIENTATION,
			        ExifInterface.ORIENTATION_NORMAL);
	
			int rotate = 0;
	
			switch (exifOrientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
			    rotate = 90;
			    break;
			case ExifInterface.ORIENTATION_ROTATE_180:
			    rotate = 180;
			    break;
			case ExifInterface.ORIENTATION_ROTATE_270:
			    rotate = 270;
			    break;
			}

		    int w = bitmap.getWidth();
		    int h = bitmap.getHeight();

		    // Setting pre rotate
		    Matrix mtx = new Matrix();
		    mtx.preRotate(rotate);

		    // Rotating Bitmap & convert to ARGB_8888, required by tess
		    bitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, false);

		    bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
		
		
		   
 
			
			TessBaseAPI baseApi = new TessBaseAPI();
			
			baseApi.init(root+"/tess/", "eng");
			
			baseApi.setImage(bitmap);
			
			recognizedText = baseApi.getUTF8Text();
			baseApi.end();
		
			Toast.makeText(getApplicationContext(), recognizedText, Toast.LENGTH_LONG).show();
		} catch (Exception e) {

			Toast.makeText(getApplicationContext(), Environment.getExternalStorageDirectory().toString() + "/tess/tessdata/eng.traineddata", Toast.LENGTH_LONG).show();
		}
	}
	
	
	public void launchCamera(View v)
	{
		Log.i("MakeMachine", "startCameraActivity()" );
    	File file = new File( _imagePath );
    	Uri outputFileUri = Uri.fromFile( file );
    	
    	Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE );
    	intent.putExtra( MediaStore.EXTRA_OUTPUT, outputFileUri );
    	
    	startActivityForResult( intent, 0 );

	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) 
    {	
    	Log.i( "MakeMachine", "resultCode: " + resultCode );
    	switch( resultCode )
    	{
    		case 0:
    			Log.i( "MakeMachine", "User cancelled" );
    			break;
    			
    		case -1:
    			onPhotoTaken();
    			break;
    	}
    }
    
    protected void onPhotoTaken()
    {
    	Log.i( "MakeMachine", "onPhotoTaken" );
    	
    	_taken = true;
    	
    	BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;
    	
    	Bitmap bitmap = BitmapFactory.decodeFile( _imagePath, options );
    	
    	_image.setImageBitmap(bitmap);
    	
    	
    	
    	//extractText(_imagePath);
    }
    
    @Override 
    protected void onRestoreInstanceState( Bundle savedInstanceState){
    	Log.i( "MakeMachine", "onRestoreInstanceState()");
    	if( savedInstanceState.getBoolean( MainActivity.PHOTO_TAKEN ) ) {
    		onPhotoTaken();
    	}
    }
    
    @Override
    protected void onSaveInstanceState( Bundle outState ) {
    	outState.putBoolean( MainActivity.PHOTO_TAKEN, _taken );
    }
    
    public void saveText(View v)
    {
    	Intent intent = new Intent(getApplicationContext(), SaveActivity.class);
    	intent.putExtra("extractedText", recognizedText);
    	startActivity(intent);
    
    }
    
    private void copyAssets() {
        AssetManager assetManager = getAssets();
        String[] files = null;
        try {
            files = assetManager.list("");
        } catch (IOException e) {
            Log.e("tag", "Failed to get asset file list.", e);
        }
        for(String filename : files) {
            InputStream in = null;
            OutputStream out = null;
            try {
              in = assetManager.open(filename);
              File outFile = new File(Environment.getExternalStorageDirectory()+"/tess/tessdata/", filename);
              out = new FileOutputStream(outFile);
              copyFile(in, out);
              in.close();
              in = null;
              out.flush();
              out.close();
              out = null;
            } catch(IOException e) {
                Log.e("tag", "Failed to copy asset file: " + filename, e);
            }       
        }
    }
    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
          out.write(buffer, 0, read);
        }
    }
	
	
}
