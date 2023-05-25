package gangulwar.callscheduler;

import android.Manifest;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CALL_PHONE_PERMISSION = 1;
    private static final int REQUEST_SELECT_CONTACT = 2;
    private TextView selectedContactTextView;
    private TextView selectedTimeTextView;
    private int selectedHour;
    private int selectedMinute;
    private String phone_number;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        selectedContactTextView = findViewById(R.id.selectedContactTextView);
        selectedTimeTextView = findViewById(R.id.selectedTimeTextView);

        findViewById(R.id.scheduleCallButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL_PHONE_PERMISSION);
                } else {
                    startCall(phone_number);
                }
            }
        });


        findViewById(R.id.selectContactButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectContact();
            }
        });

        findViewById(R.id.selectTimeButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timePicker();
            }
        });

        findViewById(R.id.scheduleCallButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scheduleCall();
            }
        });
    }

    private void selectContact() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        startActivityForResult(intent, REQUEST_SELECT_CONTACT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_SELECT_CONTACT && resultCode == RESULT_OK) {
            Uri contactUri = data.getData();
            Cursor cursor = getContentResolver().query(contactUri, null, null, null, null);

            if (cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                String contactName = cursor.getString(nameIndex);
                int phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                phone_number = cursor.getString(phoneIndex);
                System.out.println(phone_number);
                selectedContactTextView.setText("Selected Contact: " + contactName);
            }

            cursor.close();
        }
    }

    private void timePicker() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                selectedHour = hourOfDay;
                selectedMinute = minute;
                selectedTimeTextView.setVisibility(View.VISIBLE);
                selectedTimeTextView.setText("Selected time: " + formatTime(hourOfDay, minute));
            }
        }, hour, minute, false);

        timePickerDialog.show();
    }


    private void scheduleCall() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
        calendar.set(Calendar.MINUTE, selectedMinute);
        calendar.set(Calendar.SECOND, 0);

        long delay = calendar.getTimeInMillis() - System.currentTimeMillis();

        if (delay > 0) {
            new android.os.Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startCall(phone_number);
                }
            }, delay);

            selectedTimeTextView.setText("Call Scheduled At: " + formatTime(selectedHour, selectedMinute));
            selectedTimeTextView.setVisibility(View.VISIBLE);
            // Display success message
            toastMessage("Call scheduled successfully!");
        }
    }

    private String formatTime(int hour, int minute) {
        String amPm;
        if (hour < 12) {
            amPm = "AM";
        } else {
            amPm = "PM";
        }
        int hourIn12Format = hour % 12;
        if (hourIn12Format == 0) {
            hourIn12Format = 12;
        }

        String hourString = String.valueOf(hourIn12Format);
        String minuteString = String.valueOf(minute);

        if (hourString.length() == 1) {
            hourString = "0" + hourString;
        }

        if (minuteString.length() == 1) {
            minuteString = "0" + minuteString;
        }

        return hourString + ":" + minuteString + " " + amPm;
    }

    private void toastMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CALL_PHONE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //String phoneNumber = selectedContactTextView.getText().toString();
                startCall(phone_number);
            } else {
                toastMessage("Permission Declined");
            }
        }
    }

    private void startCall(String phoneNumber) {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + phoneNumber));
        startActivity(callIntent);
    }
}