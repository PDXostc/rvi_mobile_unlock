package com.jaguarlandrover.auto.remote.vehicleentry;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;


/**
 * A placeholder fragment containing a simple view.
 */
public class KeyShareActivityFragment extends Fragment
{
    int year_x, month_x, day_x, hour_x, min_x, hour_end, min_end;
    String am_pm;
    TextView startdate, enddate, starttime, endtime;
    static final int dateDialog = 0;
    static final int timeDialog = 1;
    private TextView   activeDialog;
    private TextView   activeTime;
    private TextView   userHeader;
    private Button     shareKeyBtn;
    private Switch     lock_unlock;
    private Switch     engine_start;
    private Switch     trunk_lights;
    private ViewPager  userPages;

    Vehicle mSelectedVehicle = new Vehicle();
    User mSnapshotUser = new User();

    int[] users = {R.drawable.lilli,
            R.drawable.magnus,
            R.drawable.anson,
    };

    List<String> mGuestUsers = new ArrayList<>();

    private ShareFragmentButtonListener buttonListener;

    public KeyShareActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_key_share, container, false);

        shareKeyBtn = (Button) view.findViewById(R.id.ShareBtn);
        lock_unlock = (Switch) view.findViewById(R.id.lockUnlock);
        engine_start = (Switch) view.findViewById(R.id.engine);
        trunk_lights = (Switch) view.findViewById(R.id.trunk_lights);
        userPages = (ViewPager) view.findViewById(R.id.userscroll);
        userHeader = (TextView) view.findViewById(R.id.user);

        userHeader.setText(mSnapshotUser.getDisplayName());

        shareKeyBtn.setOnClickListener(l);
        buttonListener = (ShareFragmentButtonListener) getActivity();

        lock_unlock.setChecked(true);

        //List<String> guestUsers = new ArrayList<String>();
        for (User user : mSnapshotUser.getGuests()) {
            mGuestUsers.add(user.getDisplayName() + "(" + user.getUserName() + ")");
        }

//        ArrayAdapter<String> guestUserAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_selectable_list_item, guestUsers);
//        ListView listView = (ListView) view.findViewById(R.id.guestUsers);
//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
//        {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                mPosition = position;
//                alertMessage();
//            }
//        });
//
//        listView.setAdapter(adapter);


        return view;
    }

    public interface ShareFragmentButtonListener
    {
        public void onButtonCommand(String cmd);
    }

    private View.OnClickListener l = new View.OnClickListener()
    {
        @Override
        public void onClick(View v) {
            switch(v.getId()) {
                case R.id.ShareBtn:
                    buttonListener.onButtonCommand("share");
                    break;
            }
        }
    };

    public User getSharingUser() throws JSONException {
        String start = null;
        String end = null;
        String end_time = null;
        String start_time = null;

        try {
            start_time = convertTime(starttime.getText().toString());
            end_time = convertTime(endtime.getText().toString());
        } catch (Exception e) {
        }

        String start_date = startdate.getText().toString() + " " + start_time;
        String end_date = enddate.getText().toString() + " " + end_time;
        try {
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.000Z'");
            SimpleDateFormat inputFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");
            outputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

            Date newStart = inputFormat.parse(start_date);
            Date newEnd = inputFormat.parse(end_date);
            start = outputFormat.format(newStart);
            end = outputFormat.format(newEnd);
        } catch (Exception e) {
            Log.d("DATE", "ERROR IN DATE FORMAT");
            e.printStackTrace();
        }

        Integer selectedUserIndex = userPages.getCurrentItem();
        if (selectedUserIndex >= mSnapshotUser.getGuests().size())
            return null;

        User sharingGuest = new User(mSnapshotUser.getGuests().get(selectedUserIndex).getUserName());

        Vehicle sharingVehicle = new Vehicle(mSelectedVehicle.getVehicleId());

        try {
            sharingVehicle.setValidFrom(start);
            sharingVehicle.setValidTo(end);
        } catch (Exception e) {
            e.printStackTrace();
        }

        sharingVehicle.getAuthorizedServices().setEngine(engine_start.isChecked());
        sharingVehicle.getAuthorizedServices().setLights(trunk_lights.isChecked());
        sharingVehicle.getAuthorizedServices().setLock(lock_unlock.isChecked());
        sharingVehicle.getAuthorizedServices().setTrunk(trunk_lights.isChecked());

        sharingGuest.addVehicle(sharingVehicle);

        Log.d("SHARE_NEW", sharingGuest.toString());

        return sharingGuest;
    }

    public void setSelectedVehicle(Vehicle selectedVehicle) {
        this.mSelectedVehicle = selectedVehicle;
    }

    public void setSnapshotUser(User snapshotUser) {
        mSnapshotUser = snapshotUser;
        userHeader.setText(mSnapshotUser.getDisplayName());

        for (User user : mSnapshotUser.getGuests()) {
            mGuestUsers.add(user.getDisplayName() + "\n(" + user.getUserName() + ")");
        }
    }

    public void showUserSelect() {
        KeyShareGuestListScrollingPagerAdapter userPageAdapter = new KeyShareGuestListScrollingPagerAdapter(getActivity(), mGuestUsers);
        userPages.setAdapter(userPageAdapter);
        userPages.setOffscreenPageLimit(2);
        Log.d("ScrollPager", "Users");
        userPages.setHorizontalFadingEdgeEnabled(true);
    }

    public String convertTime(String time) throws Exception{
        SimpleDateFormat displayFormat = new SimpleDateFormat("HH:mm");
        SimpleDateFormat parseFormat = new SimpleDateFormat("hh:mm a");
        Date date = parseFormat.parse(time);
        return displayFormat.format(date);
    }

    public void showDialog() {
        final Calendar cal = Calendar.getInstance();
        year_x = cal.get(Calendar.YEAR);
        month_x = cal.get(Calendar.MONTH);
        month_x = month_x+1;
        day_x = cal.get(Calendar.DAY_OF_MONTH);
        hour_x = cal.get(Calendar.HOUR_OF_DAY);
        min_x = cal.get(Calendar.MINUTE);
        cal.add(Calendar.MINUTE, 3);
        hour_end = cal.get(Calendar.HOUR);
        min_end = cal.get(Calendar.MINUTE);

        String newDay;
        String newMonth;
        String startHour;
        String startMin;
        String endHour;
        String endMin;
        String ampm="am";

        newDay = String.valueOf(day_x);
        newMonth = String.valueOf(month_x);
        startHour = String.valueOf(hour_x);
        startMin = String.valueOf(min_x);
        endHour = String.valueOf(hour_end);
        endMin = String.valueOf(min_end);

        if (min_x < 10) {
            startMin = "0" + startMin;
        }

        if (hour_x > 11) {
            ampm = "pm";
            if (hour_x == 12) {
            } else {
                startHour = String.valueOf(hour_x - 12);
                if (hour_x - 12 < 10) {
                    startHour = "0" + startHour;
                }
            }
        }

        if (hour_x == 0) {
            startHour = String.valueOf(hour_x + 12);
        }

        if (hour_x < 10) {
            startHour = "0" + startHour;
        }

        if (min_end < 10) {
            endMin = "0" + min_end;
        }

        if (hour_end > 11) {
            ampm = "pm";

            if (hour_end == 12) {
            } else {
                endHour = String.valueOf(hour_end - 12);
                if (hour_end - 12 < 10) {
                    endHour = "0" + endHour;
                }
            }
        }

        if (hour_end == 0) {
            endHour = String.valueOf(hour_end + 12);
        }

        if (hour_end < 10) {
            endHour = "0" + hour_end;
        }

        if (day_x < 10) {
            newDay = "0" + newDay;
        }

        if (month_x < 10) {
            newMonth = "0" + newMonth;
        }

        startdate = new TextView(getActivity());
        enddate = new TextView(getActivity());
        starttime = new TextView(getActivity());
        endtime = new TextView(getActivity());

        startdate = (TextView) getActivity().findViewById(R.id.startlblDate);
        enddate = (TextView) getActivity().findViewById(R.id.endlblDate);
        starttime = (TextView) getActivity().findViewById(R.id.starttimeLbl);
        endtime = (TextView) getActivity().findViewById(R.id.endtimeLbl);

        starttime.setText(startHour + ":" + startMin + " " + ampm);
        endtime.setText(endHour + ":" + endMin + " " + ampm);
        startdate.setText(newMonth + "/" + newDay + "/" + year_x);
        enddate.setText(newMonth + "/" + newDay + "/" + year_x);

        startdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activeDialog = startdate;
                getActivity().showDialog(dateDialog);
            }
        });

        enddate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activeDialog = enddate;
                getActivity().showDialog(dateDialog);
            }
        });

        starttime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activeTime = starttime;
                getActivity().showDialog(timeDialog);
            }
        });
        endtime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activeTime = endtime;
                getActivity().showDialog(timeDialog);
            }
        });

    }

    private DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayofMonth) {
            year_x = year;
            month_x = monthOfYear + 1;
            day_x = dayofMonth;
            updateDisplay(activeDialog);

        }
    };

    private TimePickerDialog.OnTimeSetListener timePickerListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            am_pm = "am";
            hour_x = hourOfDay;

            if (hourOfDay > 11) {
                am_pm = "pm";
                if (hourOfDay == 12) {
                } else {
                    hour_x = hourOfDay - 12;
                }
            }
            if (hourOfDay == 0) {
                hour_x = hourOfDay + 12;
            }

            min_x = minute;

            updateTime(activeTime);
        }
    };

    private void updateDisplay(TextView dateDisplay) {
        String newDay;
        String newMonth;
        newDay = String.valueOf(day_x);
        newMonth = String.valueOf(month_x);
        if (day_x < 10) {
            newDay = "0" + newDay;
        }
        if (month_x < 10) {
            newMonth = "0" + newMonth;
        }
        dateDisplay.setText(newMonth + "/" + newDay + "/" + year_x);
    }

    private void updateTime(TextView timeDisplay) {
        String newMin;
        String newHour;
        newHour = String.valueOf(hour_x);
        newMin = String.valueOf(min_x);
        if (min_x < 10) {
            newMin = "0" + newMin;
        }
        if (hour_x < 10) {
            newHour = "0" + newHour;
        }
        timeDisplay.setText(newHour + ":" + newMin + " " + am_pm);
    }

    public DatePickerDialog.OnDateSetListener getDatePickerListener() {
        return datePickerListener;
    }

    public TimePickerDialog.OnTimeSetListener getTimePickerListener() {
        return timePickerListener;
    }

    public int getYear() {
        return year_x;
    }

    public int getMonth() {
        return month_x;
    }

    public int getDay() {
        return day_x;
    }

    public int getHour() {
        return hour_x;
    }

    public int getMin() {
        return min_x;
    }
}
