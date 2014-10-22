/*
 * Copyright (C) 2011 Paul Watts (paulcwatts@gmail.com) and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.android.map.googlemapsv2;

import org.onebusaway.android.R;
import org.onebusaway.android.io.elements.ObaReferences;
import org.onebusaway.android.io.elements.ObaRoute;
import org.onebusaway.android.io.elements.ObaStop;
import org.onebusaway.android.map.MapParams;
import org.onebusaway.android.ui.ArrivalsListActivity;
import org.onebusaway.android.util.MyTextUtils;
import org.onebusaway.android.util.UIHelp;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

/**
 * This class handles most of the display and UI for the Map Popup
 *
 * @author paulw
 */
class StopPopup {

    private static final String TAG = "StopPopup";

    private final BaseMapFragment mFragment;

    private final Context mContext;

    private final View mView;

    private final TextView mNameView;

    private final TextView mDirection;

    //private final GridView mRoutesView;

    private final RoutesAdapter mRoutesAdapter;

    private ObaStop mStop;

    private ObaReferences mReferences;

    private UIHelp.StopUserInfoMap mStopUserMap;

    StopPopup(BaseMapFragment fragment, View view) {
        mFragment = fragment;
        mContext = fragment.getActivity();
        mView = view;
        mNameView = (TextView) view.findViewById(R.id.stop_name);
        mDirection = (TextView) view.findViewById(R.id.direction);
        //mRoutesView = (GridView) view.findViewById(R.id.route_list);
        mRoutesAdapter = new RoutesAdapter(mContext);
        //mRoutesView.setAdapter(mRoutesAdapter);
        // Make sure clicks on the popup don't leak to the map.
        mView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) { /* no-op */ }
        });

        // Initialize the links
        //View showArrivals = mView.findViewById(R.id.show_arrival_info);
        //showArrivals.setOnClickListener(mOnShowArrivals);

        //View showRoutes = mView.findViewById(R.id.show_routes);
        //showRoutes.setOnClickListener(mOnShowRoutes);
    }

    void show(ObaStop stop) {
        if (!stop.equals(mStop)) {
            mStop = stop;
            // Fill in the information for this stop and show the popup
            if (mStopUserMap != null) {
                mStopUserMap.setView2(mNameView, stop.getId(), stop.getName(), false);
            } else {
                Log.e(TAG, "Calling show with null stop user map");
                mNameView.setText(MyTextUtils.toTitleCase(stop.getName()));
            }

            UIHelp.setStopDirection(mDirection, stop.getDirection(), false);
            if (mView.getVisibility() != View.VISIBLE) {
                mView.startAnimation(AnimationUtils.loadAnimation(
                        mContext, android.R.anim.fade_in));
                mView.setVisibility(View.VISIBLE);
            }

//            if (mRoutesView.isShown()) {
//                List<ObaRoute> routes = mReferences.getRoutesForLocation(mStop.getRouteDisplayNames());
//                mRoutesAdapter.setData(routes);
//            }
        }
    }

    void hide() {
        mView.startAnimation(AnimationUtils.loadAnimation(
                mContext, android.R.anim.fade_out));
        mView.setVisibility(View.GONE);
        mStop = null;
    }

    void setReferences(ObaReferences refs) {
        mReferences = refs;
    }

    void setStopUserMap(UIHelp.StopUserInfoMap map) {
        mStopUserMap = map;
    }

    private final OnClickListener mOnShowArrivals = new OnClickListener() {
        @Override
        public void onClick(View v) {
            HashMap<String, ObaRoute> routes = new HashMap<String, ObaRoute>();
            for (ObaRoute r : mReferences.getRoutes()) {
                routes.put(r.getId(), r);
            }
            ArrivalsListActivity.start(mContext, mStop, routes);
        }
    };

    private final OnClickListener mOnShowRoutes = new OnClickListener() {
        @Override
        public void onClick(View v) {
            toggleShowRoutes((TextView) v);
        }
    };

    private void toggleShowRoutes(TextView text) {
//        if (text == null) {
//            text = (TextView) mView.findViewById(R.id.show_routes);
//        }
//        if (!mRoutesView.isShown() && mStop != null) {
//            // Update the routes
//            List<ObaRoute> routes = mReferences.getRoutesForLocation(mStop.getRouteDisplayNames());
//            mRoutesAdapter.setData(routes);
//
//            mRoutesView.setVisibility(View.VISIBLE);
//            text.setText(R.string.main_hide_routes);
//        } else {
//            mRoutesView.setVisibility(View.GONE);
//            text.setText(R.string.main_show_routes);
//        }
    }

    private class RoutesAdapter extends ArrayAdapter<ObaRoute> {

        private final LayoutInflater mInflater;

        public RoutesAdapter(Context context) {
            super(context, R.layout.main_popup_route_item);
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @TargetApi(11)
        private void setData(List<ObaRoute> data) {
            clear();
            if (data != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    addAll(data);
                } else {
                    for (ObaRoute info : data) {
                        add(info);
                    }
                }
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;

            if (convertView == null) {
                view = mInflater.inflate(R.layout.main_popup_route_item, parent, false);
            } else {
                view = convertView;
            }

            final ObaRoute route = getItem(position);
            TextView shortName = (TextView) view.findViewById(R.id.short_name);
            shortName.setText(UIHelp.getRouteDisplayName(route));
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bundle args = new Bundle();
                    args.putString(MapParams.ROUTE_ID, route.getId());
                    mFragment.setMapMode(MapParams.MODE_ROUTE, args);
                }
            });

            return view;
        }
    }
}