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
package com.hms.explorehms.huawei.feature_distributionanalysis;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.os.Bundle;

import com.hms.explorehms.Util;
import com.hms.explorehms.huawei.feature_distributionanalysis.adapter.DistributionAnalysisPagerAdapter;
import com.hms.explorehms.huawei.feature_distributionanalysis.fragments.DownloadAndInstallsFragment;
import com.hms.explorehms.huawei.feature_distributionanalysis.fragments.InAppPurchaseFragment;
import com.hms.explorehms.huawei.feature_distributionanalysis.fragments.OverviewFragment;
import com.hms.explorehms.huawei.feature_distributionanalysis.fragments.PaidApplicationDetailsFragment;
import com.hms.explorehms.huawei.feature_distributionanalysis.fragments.QuickAppRunningReportFragment;
import com.hms.explorehms.huawei.feature_distributionanalysis.fragments.QuickAppUserReportFragment;
import com.google.android.material.tabs.TabLayout;
import com.huawei.hms.feature.dynamicinstall.FeatureCompat;

import java.util.LinkedHashMap;

public class DistributionMainActivity extends AppCompatActivity {
    private final LinkedHashMap<Fragment, String> fragmentList = new LinkedHashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_distribution_main);
        setupToolbar();
        fragmentList.put(new OverviewFragment(), "Overview");
        fragmentList.put(new DownloadAndInstallsFragment(), "Download And Installs");
        fragmentList.put(new InAppPurchaseFragment(), "In App Purchase");
        fragmentList.put(new PaidApplicationDetailsFragment(), "Paid Application Details");
        fragmentList.put(new QuickAppUserReportFragment(), "Quick App User Report");
        fragmentList.put(new QuickAppRunningReportFragment(), "Quick App Running Report");

        TabLayout tabLayout = findViewById(R.id.tablayout);
        ViewPager viewPager2 = findViewById(R.id.viewpager);
        tabLayout.setupWithViewPager(viewPager2);
        DistributionAnalysisPagerAdapter adapter =
                new DistributionAnalysisPagerAdapter(
                        getSupportFragmentManager(),
                        FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,
                        fragmentList
                );

        viewPager2.setAdapter(adapter);

    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        Util.setToolbar(this, toolbar, getString(R.string.url_txt_distranalyse));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        // Initialize the SDK in the activity.
        FeatureCompat.install(newBase);
    }

}