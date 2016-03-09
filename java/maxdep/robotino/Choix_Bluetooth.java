package maxdep.robotino;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class Choix_Bluetooth extends AppCompatActivity {
    //widgets
    ListView devicelist;
    //Bluetooth
    private BluetoothAdapter myBluetooth = null;
    private Set<BluetoothDevice> Bluetooth_devices;
    public static String EXTRA_ADDRESS = "device_address";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choix__bluetooth);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //apelle widgets
        devicelist = (ListView)findViewById(R.id.listView);

        //si le smartphone à le bluetooth
        myBluetooth = BluetoothAdapter.getDefaultAdapter();

        if(myBluetooth == null)
        {
            //affiche un message
            Toast.makeText(getApplicationContext(), "Bluetooth non disponible", Toast.LENGTH_LONG).show();

            //close l'app
            finish();
        }
        else if(!myBluetooth.isEnabled())
        {
            //demande l'ativation du blueooth
            Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnBTon,1);
        }
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Recherche des robots...", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                rechercheBluetoothList();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_choix__bluetooth, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private void rechercheBluetoothList()
    {
        Bluetooth_devices = myBluetooth.getBondedDevices();
        ArrayList list = new ArrayList();

        if (Bluetooth_devices.size()>0)
        {
            for(BluetoothDevice bt : Bluetooth_devices)
            {
                list.add(bt.getName() + "\n" + bt.getAddress()); //Get le nom et l'adresse
            }
        }
        else
        {
            Toast.makeText(getApplicationContext(), "Aucun robot détecté.", Toast.LENGTH_LONG).show();
        }

        final ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, list);
        devicelist.setAdapter(adapter);
        devicelist.setOnItemClickListener(Device_choice); //Method si un device est choisi

    }
    private AdapterView.OnItemClickListener Device_choice = new AdapterView.OnItemClickListener()
    {
        public void onItemClick (AdapterView<?> av, View v, int arg2, long arg3)
        {
            // Get mac adress
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            // lance l'autre fenetre
            Intent i = new Intent(Choix_Bluetooth.this, control_robot.class);

            //Change the activity.
            i.putExtra(EXTRA_ADDRESS, address); //this will be received at ledControl (class) Activity
            startActivity(i);
        }
    };
}

