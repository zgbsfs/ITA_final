package it.t;

import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;


public class MainActivity extends ActionBarActivity {

    Button btnShowLocation,decode;
    // GPSTracker class
    GPSTracker gps;
    final String[] projection = null;
    final String selection = null;
    ArrayList<Long> timelist = new ArrayList<Long>();
    ArrayList<Integer> normaltime = new ArrayList<Integer>();
    ArrayList<Integer> tet = new ArrayList<Integer>();
    char[] latitudearr;
    char[] longitudearr;

    final String sortOrder = android.provider.CallLog.Calls.DATE + " DESC";
    double latitude;
    double longitude;

    boolean start=false;
    int bits=-1;
    int i;
    private String la,subla,lo,sublo;
    Long timestand=0L;

    PhoneStateReceiver PhoneStateReceiver=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PhoneStateReceiver = new PhoneStateReceiver();
        btnShowLocation = (Button) findViewById(R.id.btnShowLocation);
        decode = (Button) findViewById(R.id.decode);


        tet.add(0,0);

        decode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                final TextView time = (TextView) findViewById(R.id.timer);
                int b =0;
                Toast.makeText(getApplicationContext(), "decode ", Toast.LENGTH_LONG).show();
                Cursor cursor = getContentResolver().query(CallLog.Calls.CONTENT_URI, projection,selection, null, sortOrder);
                while (cursor.moveToNext()&&b<44) {
                    String callLogID = cursor.getString(cursor.getColumnIndex(android.provider.CallLog.Calls._ID));
                    String callNumber = cursor.getString(cursor.getColumnIndex(android.provider.CallLog.Calls.NUMBER));

                    String callDate = cursor.getString(cursor.getColumnIndex(android.provider.CallLog.Calls.DATE));
                    String callType = cursor.getString(cursor.getColumnIndex(android.provider.CallLog.Calls.TYPE));
                    String isCallNew = cursor.getString(cursor.getColumnIndex(android.provider.CallLog.Calls.NEW));
                    Long callDay = Long.valueOf(callDate);
                    // Long teste = 1433512286614L-11L;
                    Date date=new Date(callDay);
                     Log.v("" , "");
                    //if(Integer.parseInt(callType) == android.provider.CallLog.Calls.MISSED_TYPE && Integer.parseInt(isCallNew) > 0){
                    if(Integer.parseInt(callType)==android.provider.CallLog.Calls.MISSED_TYPE){

                        if(callNumber.equals("sender")&&timestand-callDay<660000) {
                            if(b==0)
                                timestand = callDay;
                            timelist.add(callDay);
                            Log.v("Missed Call Found: " + callNumber + "  " + date + "  WTF " + callDay, "");
                            b++;
                        }

                    }


                }

                for(i=0;i<timelist.size();i++)
                    Log.v("",timelist.get(i)+"");
                for(i=1;i<timelist.size();i++)
                    normaltime.add((int)((timelist.get(i-1)-timelist.get(i))/100));
                for(i=0;i<normaltime.size();i++)
                    Log.v("",normaltime.get(i)+"");
                String recover="";
                for(i=0;i<normaltime.size();i++) {
                    int c =normaltime.get(i);
                    while (c>160) {
                        recover += "0";
                        c=c-140;
                    }
                    recover += "1";
                }
                Log.e("give me",recover.substring(0,19)+"\n"+recover.substring(19,42));
                String laprefix="10000011";
                String loprefix="100001011110";
                float fa = GetFloat32( laprefix+recover.substring(19,42));
                float fo = GetFloat32( loprefix+recover.substring(0,19));
                time.setTextSize(12.0f);
                time.setText("Your son's Location is - \nLat: " + fa + "\nLong: " + fo + "\n");
                Log.e("give me",loprefix+recover.substring(0,19));
                Log.e("What to say ","lagitude"+fa+"\nlongitude"+fo);
            }
        });
        btnShowLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // create class object

                final TextView time = (TextView) findViewById(R.id.timer);
                gps = new GPSTracker(MainActivity.this);
                double a=0.3123;
                // check if GPS enabled
                if(gps.canGetLocation()){
                    latitude = gps.getLatitude();
                    longitude = gps.getLongitude();
                    start =true;
                    Log.d("click","start = "+start);

                    la = String.valueOf(latitude);
                    subla = la.substring(0,la.length()-2);
                    lo = String.valueOf(longitude);
                    sublo = lo.substring(0,lo.length()-2);

                    //Float.parseFloat(sublo);
                      String stra = GetBinary32( Float.parseFloat(subla) ).substring(8);
                     latitudearr =stra.toCharArray();
                    Log.d("la size?",latitudearr.length+"");
                    for(i=22;i>=0;i--){
                        tet.add(23-i,(int)latitudearr[i]-48);
                    }


                    //float fa = GetFloat32( stra );
                      String stro = GetBinary32( Float.parseFloat(sublo) ).substring(12);
                    longitudearr =stro.toCharArray();
                    Log.d("long size?",(longitudearr.length+latitudearr.length)+"");
                    for(i=18;i>=0;i--){
                        tet.add(42-i,(int)longitudearr[i]-48);
                    }
                    //float fo = GetFloat32( stro );
                     System.out.println( "latitude Decimal equivalent of " + subla + ":" +stra);
                     System.out.println( "long Decimal equivalent of " + sublo + ":" +stro);
                    //   System.out.println( f );
                    // Log.i("msg",String.valueOf(latitude));
                     Log.d("yesLA?",latitude+"");
                     Log.d("yesLO?",longitude+"");
                    Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude + "\n", Toast.LENGTH_LONG).show();
                       Intent intentDial = new Intent("android.intent.action.CALL", Uri.parse("tel:" + "receive"));
                        registerReceiver(PhoneStateReceiver, new IntentFilter("s"));
                          intentDial.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                          intentDial.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    time.setTextSize(12.0f);
                    time.setText("Your Location is - \nLat: " + latitude + "\nLong: " + longitude + "\n");
                        startActivity(intentDial);
                        for(i=0;i<tet.size();i++)
                            Log.v("put in tet ["+i+"] ",tet.get(i)+"");
                    tet.add(43,1);
                    tet.add(44,0);
                    Log.v(" tet size",tet.size()+"");
                }else{
                    gps.showSettingsAlert();
                }

            }
        });
    }
    // Convert the 32-bit binary into the decimal
    private static float GetFloat32( String Binary )
    {
        int intBits = Integer.parseInt(Binary, 2);
        float myFloat = Float.intBitsToFloat(intBits);
        return myFloat;
    }

    // Get 32-bit IEEE 754 format of the decimal value
    private static String GetBinary32( float value )
    {
        int intBits = Float.floatToIntBits(value);
        String binary = Integer.toBinaryString(intBits);
        return binary;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onResume() {
        Log.d("when Resume","WTF");
        bits++;
        if(start&&bits<44&&tet.get(bits)==1) {
            Intent intentDial = new Intent("android.intent.action.CALL", Uri.parse("tel:" + "receiver"));
            registerReceiver(PhoneStateReceiver, new IntentFilter("s"));
            intentDial.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intentDial.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intentDial);
            Log.e("start=true",start+""+bits+"  tet="+tet.get(bits));

        }
        if(tet.get(bits)==0&&start&&bits<44){
            Intent intentDial = new Intent("android.intent.action.CALL", Uri.parse("tel:" + "sender self"));
            registerReceiver(PhoneStateReceiver, new IntentFilter("s"));
            intentDial.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intentDial.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intentDial);
            Log.e("start=true",start+""+bits+"  tet="+tet.get(bits));

        }
        super.onResume();  // Always call the superclass method first
    }
}
