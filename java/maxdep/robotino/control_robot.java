package maxdep.robotino;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;

public class control_robot extends AppCompatActivity {
    EditText terminal;
    TextView txt_term;
    Button btn_deco;
    Button bt_avant;
    Button bt_arriere;
    SeekBar progress_value;
    String value_ordre;
    int vitesse;
    String address_device = null;
    BluetoothAdapter myBluetooth = null;
    TextView value_progress;
    private ProgressDialog progress;
    private boolean isBtConnected = false;
    BluetoothSocket btSocket = null;
    Handler myHandler = new Handler();
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_robot);
        Intent newint = getIntent();
        address_device = newint.getStringExtra(Choix_Bluetooth.EXTRA_ADDRESS); //addresse bluetooth
        terminal = (EditText) findViewById(R.id.editText);
        txt_term = (TextView) findViewById(R.id.text_command);
        btn_deco = (Button) findViewById(R.id.button_deco);
        bt_avant = (Button) findViewById(R.id.bt_avant);
        value_progress = (TextView) findViewById(R.id.textView2);
        progress_value = (SeekBar) findViewById(R.id.seekBar);
        new ConnectBT().execute();
        progress_value.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                value_progress.setText("Vitesse : " + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        btn_deco.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Disconnect();
            }
        });
        bt_avant.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                send_msg_bt("1m1" + vitesse);
            }
        });
        terminal.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    if (event.getAction() == KeyEvent.ACTION_UP) {
                        String ordre_txt = terminal.getText().toString();
                        if(ordre_txt == "On"){
                            value_ordre = "191";
                        }else if(ordre_txt == "Stop"){
                            value_ordre = "190";
                        }else{
                            value_ordre =  ordre_txt;
                        }
                        send_msg_bt(value_ordre);
                        txt_term.setText("$: " + value_ordre.getBytes());
                        terminal.setText("");
                        msg("Ordre envoyé");
                    }
                    return true;
                }
                return false;
            }
        });
    }
    private void Disconnect()
    {
        if (btSocket!=null) {
            try
            {
                btSocket.close();
            }
            catch (IOException e)
            { msg("Error");}
        }
        finish();
    }
    private void send_msg_bt(String msg)
    {
        if (btSocket!=null) {
            try
            {
                btSocket.getOutputStream().write(msg.getBytes());
                msg(msg);
            }
            catch (IOException e)
            { msg("Error");}
        }
    }
    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }
    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true;

        @Override
        protected void onPreExecute()
        {
            progress = ProgressDialog.show(control_robot.this, "Connection...", "En cours de traitement");
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            try
            {
                if (btSocket == null || !isBtConnected)
                {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address_device);//connects to the device's address and checks if it's available
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//start connection
                }
            }
            catch (IOException e)
            {
                ConnectSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess)
            {
                msg("La connexion a échoué. Est-ce un Bluetooth SPP? Réessayer.");
                finish();
            }
            else
            {
                msg("Connecté.");
                isBtConnected = true;
            }
            progress.dismiss();
        }
    }
}
