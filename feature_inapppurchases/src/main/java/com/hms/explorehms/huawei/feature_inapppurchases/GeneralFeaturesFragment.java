/*
 *
 *   Copyright 2020. Explore in HMS. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   You may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package com.hms.explorehms.huawei.feature_inapppurchases;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import static android.graphics.text.LineBreaker.JUSTIFICATION_MODE_INTER_WORD;


public class GeneralFeaturesFragment extends Fragment {

    TextView explanationTextView;
    String explanation1;
    String explanation2;




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        explanation1 = getString(R.string._in_app_purchases_explanation1);
        explanation2 = getString(R.string._in_app_purchases_explanation3);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view1 = inflater.inflate(R.layout.fragment_general_features_iap, container,false);

        ImageView walletKitDefault = view1.findViewById(R.id.imageView);
        walletKitDefault.setImageDrawable(getResources().getDrawable(R.drawable.in_app_purchases_main_picture));

        explanationTextView = view1.findViewById(R.id.explanationTextView);
        explanationTextView.setJustificationMode(JUSTIFICATION_MODE_INTER_WORD);
        explanationTextView.setText(new StringBuilder(explanation1 + explanation2));

        return view1;
    }
}