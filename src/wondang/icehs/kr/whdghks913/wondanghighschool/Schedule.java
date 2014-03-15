package wondang.icehs.kr.whdghks913.wondanghighschool;

import java.lang.ref.WeakReference;
import java.util.Calendar;

import com.tistory.whdghks913.croutonhelper.CroutonHelper;

import de.keyboardsurfer.android.widget.crouton.Style;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.ListView;
import android.widget.TextView;

public class Schedule extends Activity {
	private final String loadingList = "데이터를 가져오고 있습니다..";
	private final String monthError = "올바르지 않습니다";
	private final String noData = "데이터가 존재하지 않습니다,\n추후 업데이트로 데이터가 추가됩니다";

	private ScheduleListViewAdapter mAdapter;
	private ListView mListView;
	private Handler mHandler;

	private CroutonHelper mHelper;

	private ProgressDialog mDialog;

	private Calendar mCalendar;
	private SharedPreferences ScheduleList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_schedule);

		mListView = (ListView) findViewById(R.id.mScheduleList);
		mAdapter = new ScheduleListViewAdapter(this);
		mListView.setAdapter(mAdapter);

		mHandler = new MyHandler(this);

		mCalendar = Calendar.getInstance();

		ScheduleList = getSharedPreferences("March", 0);
		if (ScheduleList.getInt("days", 0) == 0) {
			// 데이터 복사
			PreferenceData mData = new PreferenceData();

			mData.copyDB(this, getPackageName(), "March.xml", true);
			mData.copyDB(this, getPackageName(), "April.xml", true);
			mData.copyDB(this, getPackageName(), "May.xml", true);
			mData.copyDB(this, getPackageName(), "June.xml", true);
			mData.copyDB(this, getPackageName(), "July.xml", true);
			mData.copyDB(this, getPackageName(), "August.xml", true);
		}

		sync();

		mHelper = new CroutonHelper(this);
		mHelper.setText("학교 일정 내용 입니다");
		mHelper.setStyle(Style.INFO);
		mHelper.show();
	}

	private void sync() {
		mAdapter.clearData();

		new Thread() {

			@Override
			public void run() {
				mHandler.sendEmptyMessage(0);
				mHandler.sendEmptyMessage(1);
			}
		}.start();
	}

	private String getMonth(int month) {
		switch (month) {
		case 0:
			return "January";
		case 1:
			return "February";
		case 2:
			return "March";
		case 3:
			return "April";
		case 4:
			return "May";
		case 5:
			return "June";
		case 6:
			return "July";
		case 7:
			return "August";
		case 8:
			return "September";
		case 9:
			return "October";
		case 10:
			return "November";
		case 11:
			return "December";
		}
		return null;
	}

	private String getMonthKorean(int month) {
		switch (month) {
		case 0:
			return "1월";
		case 1:
			return "2월";
		case 2:
			return "3월";
		case 3:
			return "4월";
		case 4:
			return "5월";
		case 5:
			return "6월";
		case 6:
			return "7월";
		case 7:
			return "8월";
		case 8:
			return "9월";
		case 9:
			return "10월";
		case 10:
			return "11월";
		case 11:
			return "12월";
		}
		return null;
	}

	@Override
	protected void onPause() {
		super.onPause();

		if (mDialog != null)
			mDialog.dismiss();

		mHelper.cencle(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.schedule, menu);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(android.view.MenuItem item) {
		int ItemId = item.getItemId();

		if (ItemId == R.id.back) {
			int year = mCalendar.get(Calendar.YEAR);
			int month = mCalendar.get(Calendar.MONTH);
			int day = mCalendar.get(Calendar.DAY_OF_MONTH);

			if (--month < 0) {
				mHelper.clearCroutonsForActivity();
				mHelper.setText(monthError);
				mHelper.setStyle(Style.ALERT);
				mHelper.show();
			} else {
				mCalendar.set(year, month, day);
				sync();
			}

		} else if (ItemId == R.id.forward) {
			int year = mCalendar.get(Calendar.YEAR);
			int month = mCalendar.get(Calendar.MONTH);
			int day = mCalendar.get(Calendar.DAY_OF_MONTH);

			if (++month > 11) {
				mHelper.clearCroutonsForActivity();
				mHelper.setText(monthError);
				mHelper.setStyle(Style.ALERT);
				mHelper.show();
			} else {
				mCalendar.set(year, month, day);
				sync();
			}

		} else if (ItemId == R.id.sync) {
			sync();
		}

		return super.onOptionsItemSelected(item);
	}

	private class MyHandler extends Handler {
		private final WeakReference<Schedule> mActivity;

		public MyHandler(Schedule Schedule) {
			mActivity = new WeakReference<Schedule>(Schedule);
		}

		@Override
		public void handleMessage(Message msg) {
			Schedule activity = mActivity.get();
			if (activity != null) {

				if (msg.what == 0) {
					if (mDialog == null) {
						mDialog = ProgressDialog.show(Schedule.this, "",
								loadingList);
					}

				} else if (msg.what == 1) {
					ScheduleList = getSharedPreferences(
							getMonth(mCalendar.get(Calendar.MONTH)), 0);

					int days = ScheduleList.getInt("days", 9999);
					if (days != 9999) {
						for (int i = 1; i < days; i++) {
							String Schedule = ScheduleList.getString(
									Integer.toString(i), null);
							if (Schedule != null) {
								String toString = Integer.toString(i);
								String dayOfWeek = ScheduleList.getString(
										toString + "_Day", null);
								if (i < 10)
									toString = "0" + toString;
								mAdapter.addItem(toString + "일", dayOfWeek,
										Schedule);
							}
						}
					} else {
						mHelper.clearCroutonsForActivity();
						mHelper.setText(noData);
						mHelper.setStyle(Style.ALERT);
						mHelper.setAutoTouchCencle(true);
						mHelper.show();
					}

					((TextView) findViewById(R.id.mMonth))
							.setText(getMonthKorean(mCalendar
									.get(Calendar.MONTH)));

					mAdapter.notifyDataSetChanged();
					mDialog.dismiss();
				}
			}
		}
	}
}
