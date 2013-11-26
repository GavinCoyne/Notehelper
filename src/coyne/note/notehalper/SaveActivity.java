package coyne.note.notehalper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SaveActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_save);
		
		
		TextView text = (TextView) findViewById(R.id.textViewRecog);
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		
		text.setText((String) extras.get("extractedText"));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.save, menu);
		return true;
	}

	public void web(View v)
	{
		TextView text = (TextView) findViewById(R.id.textViewRecog);
		String url = "http://"+text.getText().toString();
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		startActivity(browserIntent);
	}
	
	public void sendSMS(View V)
	{
		Intent sendSMS = new Intent(Intent.ACTION_VIEW);         
		sendSMS.setData(Uri.parse("sms:"));
		
		TextView text = (TextView) findViewById(R.id.textViewRecog);
		sendSMS.putExtra("sms_body", text.getText().toString());
		startActivity(sendSMS);
	
	}
	
	public void saveFile(View v)
	{		
		try
	    {
	        File root = new File(Environment.getExternalStorageDirectory()+"/tess/", "Notes");
	        if (!root.exists()) {
	            root.mkdirs();
	        }
	        TextView text = (TextView) findViewById(R.id.textViewRecog);
	        
	        EditText fileName = (EditText) findViewById(R.id.editTextFileName);
	        
	        File gpxfile = new File(root, fileName.getText().toString()+".txt");
	        FileWriter writer = new FileWriter(gpxfile);
	        writer.append(text.getText().toString());
	        writer.flush();
	        writer.close();
	        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
	    }
	    catch(IOException e)
	    {
	         e.printStackTrace();

	    }
	}
	
	public void openFile(View v)
	{
		EditText fileName = (EditText) findViewById(R.id.editTextFileName);
		Intent intent = new Intent(Intent.ACTION_EDIT); 
		Uri uri = Uri.parse("file:///"+Environment.getExternalStorageDirectory()+"/tess/Notes/"+fileName.getText().toString()+".txt"); 
		intent.setDataAndType(uri, "text/plain"); 
		startActivity(intent);
	}
}
