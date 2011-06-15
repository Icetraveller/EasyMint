package com.easymint.ui;



import com.easymint.R;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class DashboardFragment extends Fragment {

    public void fireTrackerEvent(String label) {
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dashboard, container);

        // Press the blotter button
        root.findViewById(R.id.home_btn_blotter).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                    startActivity(new Intent(getActivity(), BlotterActivity.class));
            }
            
        });

//        root.findViewById(R.id.home_btn_sessions).setOnClickListener(new View.OnClickListener() {
//            public void onClick(View view) {
//                fireTrackerEvent("Sessions");
//                // Launch sessions list
//                    final Intent intent = new Intent(Intent.ACTION_VIEW,
//                            ScheduleContract.Tracks.CONTENT_URI);
//                    intent.putExtra(Intent.EXTRA_TITLE, getString(R.string.title_session_tracks));
//                    intent.putExtra(TracksFragment.EXTRA_NEXT_TYPE,
//                            TracksFragment.NEXT_TYPE_SESSIONS);
//                    startActivity(intent);
//
//            }
//        });
        
        // lunched when pressed budget button
        root.findViewById(R.id.home_btn_budget).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                // Launch list of sessions and vendors the user has starred
                startActivity(new Intent(getActivity(), BudgetActivity.class));                
            }
        });

//        root.findViewById(R.id.home_btn_vendors).setOnClickListener(new View.OnClickListener() {
//            public void onClick(View view) {
//                fireTrackerEvent("Sandbox");
//                // Launch vendors list
//                    final Intent intent = new Intent(Intent.ACTION_VIEW,
//                            ScheduleContract.Tracks.CONTENT_URI);
//                    intent.putExtra(Intent.EXTRA_TITLE, getString(R.string.title_vendor_tracks));
//                    intent.putExtra(TracksFragment.EXTRA_NEXT_TYPE,
//                            TracksFragment.NEXT_TYPE_VENDORS);
//                    startActivity(intent);
//            }
//        });

     // lunched when pressed report button
        root.findViewById(R.id.home_btn_report).setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {
                        Intent intent = new Intent(getActivity(), ReportActivity.class);
                        startActivity(intent);
                    }
                });
        
     // lunched when pressed debt button
        root.findViewById(R.id.home_btn_debt).setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {
//                        Intent intent = new Intent(getActivity(), DebtActivity.class);
                        Intent intent = new Intent(getActivity(), DebtActivity.class);
                        startActivity(intent);
                    }
                });

        return root;
    }
}
